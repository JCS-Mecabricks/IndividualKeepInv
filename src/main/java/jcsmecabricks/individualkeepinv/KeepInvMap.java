package jcsmecabricks.individualkeepinv;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static jcsmecabricks.individualkeepinv.IndividualKeepInv.MOD_ID;

public class KeepInvMap extends SavedData {
    public final HashMap<UUID, Boolean> invStateMap = new HashMap<>();
    public boolean keepInvDefault = false;

    public static KeepInvMap kim;

    // --- Codec ---
    public static final Codec<KeepInvMap> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("keepInvDefault").forGetter(map -> map.keepInvDefault),
                    Codec.unboundedMap(Codec.STRING, Codec.BOOL).fieldOf("playerStates").forGetter(map ->
                            map.invStateMap.entrySet().stream()
                                    .collect(Collectors.toMap(e -> e.getKey().toString(), Map.Entry::getValue))
                    )
            ).apply(instance, (keepInvDefault, playerStates) -> {
                KeepInvMap map = new KeepInvMap();
                map.keepInvDefault = keepInvDefault;
                playerStates.forEach((k, v) -> map.invStateMap.put(UUID.fromString(k), v));
                return map;
            })
    );

    public static final SavedDataType<KeepInvMap> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(MOD_ID, "keepinv"), KeepInvMap::new, CODEC, DataFixTypes.PLAYER);

    public KeepInvMap() {}

    // --- Utility methods ---
    public static KeepInvMap get(ServerLevel world) {
        kim = world.getDataStorage().computeIfAbsent(TYPE);
        return kim;
    }

    private static void ensureLoaded(Player player) {
        if (kim == null && player.level() instanceof ServerLevel serverWorld) {
            get(serverWorld);
        }
    }

    public static boolean getPlayerState(Player player) {
        ensureLoaded(player);
        return kim.invStateMap.getOrDefault(player.getUUID(), kim.keepInvDefault);
    }

    public static void setPlayerState(Player player, boolean value) {
        ensureLoaded(player);
        kim.invStateMap.put(player.getUUID(), value);
        kim.setDirty();
    }

    public static void setDefaultState(boolean value) {
        if (kim != null) {
            kim.keepInvDefault = value;
            kim.setDirty();
        }
    }

    public static void onJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
        kim = get(server.overworld());
        UUID uuid = handler.player.getUUID();
        if (!kim.invStateMap.containsKey(uuid)) {
            kim.invStateMap.put(uuid, kim.keepInvDefault);
            kim.setDirty();
        }
    }

    public static void onRespawn(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
        ensureLoaded(newPlayer);
        boolean keepInv = kim.invStateMap.getOrDefault(oldPlayer.getUUID(), kim.keepInvDefault);

        if (!alive && keepInv) {
            newPlayer.restoreFrom(oldPlayer, true);
            newPlayer.setHealth(20.0f);
        } else if (!alive) {
            newPlayer.experienceLevel = 0;
            newPlayer.totalExperience = 0;
            newPlayer.experienceProgress = 0.0f;
        }
    }
}
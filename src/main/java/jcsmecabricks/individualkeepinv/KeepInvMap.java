package jcsmecabricks.individualkeepinv;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static jcsmecabricks.individualkeepinv.IndividualKeepInv.MOD_ID;

public class KeepInvMap extends PersistentState {
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

    public static final PersistentStateType<KeepInvMap> TYPE = new PersistentStateType<>(MOD_ID, KeepInvMap::new, CODEC, DataFixTypes.PLAYER);

    public KeepInvMap() {}

    // --- Utility methods ---
    public static KeepInvMap get(ServerWorld world) {
        kim = world.getPersistentStateManager().getOrCreate(TYPE);
        return kim;
    }

    private static void ensureLoaded(PlayerEntity player) {
        if (kim == null && player.getEntityWorld() instanceof ServerWorld serverWorld) {
            get(serverWorld);
        }
    }

    public static boolean getPlayerState(PlayerEntity player) {
        ensureLoaded(player);
        return kim.invStateMap.getOrDefault(player.getUuid(), kim.keepInvDefault);
    }

    public static void setPlayerState(PlayerEntity player, boolean value) {
        ensureLoaded(player);
        kim.invStateMap.put(player.getUuid(), value);
        kim.markDirty();
    }

    public static void setDefaultState(boolean value) {
        if (kim != null) {
            kim.keepInvDefault = value;
            kim.markDirty();
        }
    }

    public static void onJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        kim = get(server.getOverworld());
        UUID uuid = handler.player.getUuid();
        if (!kim.invStateMap.containsKey(uuid)) {
            kim.invStateMap.put(uuid, kim.keepInvDefault);
            kim.markDirty();
        }
    }

    public static void onRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        ensureLoaded(newPlayer);
        boolean keepInv = kim.invStateMap.getOrDefault(oldPlayer.getUuid(), kim.keepInvDefault);

        if (!alive && keepInv) {
            newPlayer.copyFrom(oldPlayer, true);
            newPlayer.setHealth(20.0f);
        } else if (!alive) {
            newPlayer.experienceLevel = 0;
            newPlayer.totalExperience = 0;
            newPlayer.experienceProgress = 0.0f;
        }
    }
}

package jcsmecabricks.individualkeepinv;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.UUID;

import static jcsmecabricks.individualkeepinv.IndividualKeepInv.MOD_ID;

public class KeepInvMap extends PersistentState {

    public final HashMap<UUID, Boolean> invStateMap = new HashMap<>();
    public boolean keepInvDefault = false;

    public static KeepInvMap kim;

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

    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        NbtCompound playersNbt = new NbtCompound();
        for (var entry : invStateMap.entrySet()) {
            NbtCompound playerData = new NbtCompound();
            playerData.putBoolean("invBool", entry.getValue());
            playersNbt.put(entry.getKey().toString(), playerData);
        }
        nbt.put("invStateCompound", playersNbt);
        nbt.putBoolean("keepInvDefault", keepInvDefault);
        return nbt;
    }

    public static KeepInvMap fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        KeepInvMap map = new KeepInvMap();
        NbtCompound compound = nbt.getCompound("invStateCompound").get();

        for (String key : compound.getKeys()) {
            UUID uuid = UUID.fromString(key);
            boolean keepInv = compound.getCompound(key).flatMap(nbt1 -> nbt.getBoolean("invBool")).orElse(false);
            map.invStateMap.put(uuid, keepInv);
        }

        map.keepInvDefault = nbt.getBoolean("keepInvDefault").get();
        return map;
    }

    public static final PersistentStateType<KeepInvMap> TYPE =
            new PersistentStateType<>(
                    MOD_ID,
                    ctx -> new KeepInvMap(),
                    ctx -> Codec.unit(new KeepInvMap()),
                    DataFixTypes.PLAYER
            );


    public static KeepInvMap get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }


    private static void ensureLoaded(PlayerEntity player) {
        if (kim == null && player.getWorld() instanceof ServerWorld serverWorld) {
            get(serverWorld);
        }
    }

    public static void onJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        kim = get(server.getOverworld());

        if (!kim.invStateMap.containsKey(handler.player.getUuid())) {
            kim.invStateMap.put(handler.player.getUuid(), kim.keepInvDefault);
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

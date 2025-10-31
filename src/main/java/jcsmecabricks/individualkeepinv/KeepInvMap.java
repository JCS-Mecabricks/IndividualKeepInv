package jcsmecabricks.individualkeepinv;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

import static jcsmecabricks.individualkeepinv.IndividualKeepInv.MOD_ID;

public class KeepInvMap extends PersistentState {

    public HashMap<UUID, Boolean> invStateMap = new HashMap<>();
    public boolean keepInvDefault = false;
    public static KeepInvMap kim = new KeepInvMap();

    public static boolean getPlayerState (PlayerEntity player) { return kim.invStateMap.get(player.getUuid()); }
    public static void setPlayerState (PlayerEntity player, boolean bool) { kim.invStateMap.put(player.getUuid(), bool); }
    public static void setDefaultState (boolean bool) { kim.keepInvDefault = bool; }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        NbtCompound playersNbtCompound = new NbtCompound();
        kim.invStateMap.forEach((UUID, Boolean) -> {
            NbtCompound pInvStateNbt = new NbtCompound();
            pInvStateNbt.putBoolean("invBool", kim.invStateMap.get(UUID));
            playersNbtCompound.put(String.valueOf(UUID), pInvStateNbt);
        });
        nbt.put("invStateCompound", playersNbtCompound);
        nbt.putBoolean("keepInvDefault", kim.keepInvDefault);
        return nbt;
    }

    public static KeepInvMap createFromNbt (NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        NbtCompound nbt1 = nbt.getCompound("invStateCompound");
        for (String key : nbt1.getKeys()) {
            boolean keepInvBool = nbt1.getCompound(key).getBoolean("invBool");
            UUID uuid = UUID.fromString(key);
            kim.invStateMap.put(uuid,keepInvBool);
        }
        kim.keepInvDefault = nbt.getBoolean("keepInvDefault");
        kim.markDirty();
        return kim;
    }

    public static KeepInvMap getInvStates(MinecraftServer server) {
        PersistentStateManager psm = server.getWorld(World.OVERWORLD).getPersistentStateManager();
        return psm.getOrCreate(new Type<>(KeepInvMap::new, KeepInvMap::createFromNbt, DataFixTypes.PLAYER), MOD_ID);
    }

    public static void onJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        kim = KeepInvMap.getInvStates(handler.player.getWorld().getServer());

        if (!kim.invStateMap.containsKey(handler.player.getUuid())) {
            kim.invStateMap.put(handler.player.getUuid(), kim.keepInvDefault);
            kim.markDirty();
        }
    }

    public static void onRespawn(ServerPlayerEntity oldPlayer, ServerPlayerEntity newPlayer, boolean alive) {
        if (!alive && kim.invStateMap.get(oldPlayer.getUuid())) {
            newPlayer.copyFrom(oldPlayer, true);
            newPlayer.setHealth(20.0f);
        }
        if (!alive && !kim.invStateMap.get(oldPlayer.getUuid())) {
            newPlayer.experienceLevel = 0;
            newPlayer.totalExperience = 0;
            newPlayer.experienceProgress = 0.0f;
        }
    }
}
package jcsmecabricks.individualkeepinv.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static jcsmecabricks.individualkeepinv.KeepInvMap.kim;

@Mixin(LivingEntity.class)
public abstract class PlayerXpMixin {

    @Inject(method = "dropExperience", at = @At("HEAD"), cancellable = true)
    private void onDropExperience(ServerLevel level, Entity attacker, CallbackInfo ci) {
        if (!((Object)this instanceof Player player)) return;

        if (kim.invStateMap.get(player.getUUID()) || player.isSpectator()) {
            ci.cancel();
        }
    }
}
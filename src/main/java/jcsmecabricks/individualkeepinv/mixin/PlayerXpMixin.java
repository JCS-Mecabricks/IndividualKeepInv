package jcsmecabricks.individualkeepinv.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static jcsmecabricks.individualkeepinv.KeepInvMap.kim;

import net.minecraft.world.entity.player.Player;

@Mixin(LivingEntity.class)
public abstract class PlayerXpMixin {

    @Inject(method = "dropExperience", at = @At("HEAD"), cancellable = true)
    public void ongetExperienceToDrop(CallbackInfoReturnable<Integer> info) {
        Player player = ((Player) (Object) this);
        if (kim.invStateMap.get(player.getUUID()) || player.isSpectator()) {
            info.setReturnValue(0);
        }
        else {
            int i = player.experienceLevel * 7;
            if (i > 100) {
                info.setReturnValue(100);
            }
            info.setReturnValue(i);
        }
    }
}

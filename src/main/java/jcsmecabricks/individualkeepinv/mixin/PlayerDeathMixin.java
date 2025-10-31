package jcsmecabricks.individualkeepinv.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static jcsmecabricks.individualkeepinv.KeepInvMap.kim;

@Mixin(PlayerEntity.class)
    public abstract class PlayerDeathMixin {
    @Final @Shadow PlayerInventory inventory;
    @Shadow protected void vanishCursedItems() {}

    @Inject(method = "dropInventory", at = @At("HEAD"), cancellable = true)
    public void onDeath(CallbackInfo info) {
        PlayerEntity player = ((PlayerEntity) (Object) this);
            if (!kim.invStateMap.get(player.getUuid())) {
                this.vanishCursedItems();
                this.inventory.dropAll();
            }
            info.cancel();
    }
}

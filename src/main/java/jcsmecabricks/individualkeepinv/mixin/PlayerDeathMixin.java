package jcsmecabricks.individualkeepinv.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static jcsmecabricks.individualkeepinv.KeepInvMap.kim;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

@Mixin(Player.class)
    public abstract class PlayerDeathMixin {
    @Final @Shadow Inventory inventory;
    @Shadow protected void destroyVanishingCursedItems() {}

    @Inject(method = "dropEquipment", at = @At("HEAD"), cancellable = true)
    public void onDeath(CallbackInfo info) {
        Player player = ((Player) (Object) this);
            if (!kim.invStateMap.get(player.getUUID())) {
                this.destroyVanishingCursedItems();
                this.inventory.dropAll();
            }
            info.cancel();
    }
}

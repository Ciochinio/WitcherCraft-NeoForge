package net.redboltmedia.witchercraft.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.redboltmedia.witchercraft.InventoryShell;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

/**
 * Replaces vanilla's dark full-screen gradient with the shell background and
 * navbar, for the decorated vanilla inventory only (see {@link InventoryShell}).
 * InventoryScreen draws that gradient and then its panel inside one method, so
 * this is the only point where the shell can go UNDER the panel.
 *
 * HAND-MAINTAINED: not an MCreator element. Registered in witchercraft.mixins.json.
 */
@Mixin(Screen.class)
public abstract class InventoryShellBackgroundMixin {
	@Inject(method = "extractTransparentBackground", at = @At("HEAD"), cancellable = true)
	private void witchercraft$inventoryShellBackground(GuiGraphicsExtractor graphics, CallbackInfo ci) {
		Screen self = (Screen) (Object) this;
		if (InventoryShell.isDecorated(self)) {
			InventoryShell.drawBackground(graphics, self);
			ci.cancel();
		}
	}
}

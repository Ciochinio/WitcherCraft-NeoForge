/*
 * The code of this mod element is always locked.
 *
 * Cat potion vision grade.
 *
 * The Witcher's Cat is not night vision. It never touches light levels, mob
 * spawning or fog - it is a camera grade: everything is collapsed to greyscale
 * and exposure is pushed hard, so dark terrain lifts into readable mid-greys and
 * anything already bright clips to white. That keeps it clearly distinct from
 * vanilla NIGHT_VISION, which lies about light level and flattens the scene.
 *
 * The whole look lives in assets/witchercraft/post_effect/cat.json, which chains
 * two of Mojang's OWN fragment shaders (minecraft:post/color_convolve, then
 * minecraft:post/blit). We deliberately ship no GLSL of our own: post effects are
 * data above the blaze3d GpuDevice abstraction, so if the render backend ever
 * changes, Mojang ports their shaders and our JSON keeps referencing them by name.
 * Tuning the strength means editing the three vec3 rows in that JSON, not this file.
 *
 * This class only decides WHEN the grade is on. It is client-only: the effect is
 * read off the local player, so nobody else's view is touched.
 */

package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.init.WitchercraftModMobEffects;

import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.Minecraft;

@EventBusSubscriber(Dist.CLIENT)
public class CatVision {

	private static final Identifier CAT_POST_EFFECT = Identifier.parse("witchercraft:cat");

	/**
	 * Re-asserted every tick rather than only on state change. GameRenderer.setPostEffect
	 * is two field writes, and the post chain itself is cached by the ShaderManager, so the
	 * cost is nil. Doing it unconditionally means we automatically recover from anything
	 * that clears the effect behind our back - checkEntityPostEffect on a dimension or
	 * camera-entity change, or the F4 debug toggle.
	 */
	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft minecraft = Minecraft.getInstance();
		GameRenderer renderer = minecraft.gameRenderer;
		LocalPlayer player = minecraft.player;

		boolean active = player != null && minecraft.level != null && player.hasEffect(WitchercraftModMobEffects.CAT_EFFECT);

		if (active)
			renderer.setPostEffect(CAT_POST_EFFECT);
		else if (CAT_POST_EFFECT.equals(renderer.currentPostEffect()))
			renderer.clearPostEffect();
	}
}

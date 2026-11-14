package net.redboltmedia.witchercraft;

import net.redboltmedia.witchercraft.procedures.WitchercraftCritConditionProcedure;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.WeakHashMap;
import java.util.Map;

/**
 * CLIENTBOUND: the entity the player is aiming at within melee reach, if a charged melee hit on it would be Precise
 * right now, else -1. Precise conditions are mostly effects on the target (Axii, Yrden, Maimed), which vanilla never
 * syncs to watching clients, so the server answers with WitchercraftCritCondition itself, the same procedure the
 * damage code uses, and the marker can't disagree with the hit. Checked every server tick, sent only on change.
 * WitchercraftHud draws the marker while vanilla's own crosshair pick is that same entity.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record WitchercraftPreciseMarkerMessage(int entityId) implements CustomPacketPayload {
	public static final Type<WitchercraftPreciseMarkerMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "precise_marker"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WitchercraftPreciseMarkerMessage> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, WitchercraftPreciseMarkerMessage::entityId,
			WitchercraftPreciseMarkerMessage::new);

	/** Client main thread only: the Precise-exposed entity under this player's aim, or -1. */
	public static int preciseTarget = -1;

	private static final Map<ServerPlayer, Integer> SENT = new WeakHashMap<>();

	@Override
	public Type<WitchercraftPreciseMarkerMessage> type() {
		return TYPE;
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player))
			return;
		int id = -1;
		if (!player.isSpectator()) {
			LivingEntity target = aimedTarget(player);
			if (target != null && WitchercraftCritConditionProcedure.execute(player.level(), target, player))
				id = target.getId();
		}
		Integer last = SENT.get(player);
		if (last != null && last == id)
			return;
		SENT.put(player, id);
		PacketDistributor.sendToPlayer(player, new WitchercraftPreciseMarkerMessage(id));
	}

	/** The living entity under the crosshair within entity reach, not behind a block (vanilla's own pick, server side). */
	private static LivingEntity aimedTarget(ServerPlayer player) {
		double range = player.entityInteractionRange();
		Vec3 eye = player.getEyePosition();
		Vec3 look = player.getViewVector(1.0F);
		Vec3 end = eye.add(look.scale(range));
		HitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
		double maxSq = block.getType() == HitResult.Type.MISS ? range * range : eye.distanceToSqr(block.getLocation());
		AABB box = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
		EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, box, e -> !e.isSpectator() && e.isPickable(), maxSq);
		return hit != null && hit.getEntity() instanceof LivingEntity living && living.isAlive() ? living : null;
	}

	public static void handleData(WitchercraftPreciseMarkerMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.CLIENTBOUND)
			return;
		context.enqueueWork(() -> preciseTarget = message.entityId());
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WitchercraftPreciseMarkerMessage::handleData);
	}
}

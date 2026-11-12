package net.redboltmedia.witchercraft;

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.Entity;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.client.Minecraft;

import java.util.Map;
import java.util.HashMap;

/**
 * CLIENTBOUND: player `entityId` is casting a Sign for the next `ticks`, so their off-hand arm is held
 * out (WitchercraftCastPoseClient). Sent to the caster and everyone tracking them; a held Sign keeps
 * re-sending it, throttled so a hold costs a packet every few ticks rather than every tick.
 *
 * HAND-MAINTAINED: locked code element.
 */
@EventBusSubscriber
public record WitchercraftCastPoseMessage(int entityId, int ticks) implements CustomPacketPayload {
	public static final Type<WitchercraftCastPoseMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "cast_pose"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WitchercraftCastPoseMessage> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, WitchercraftCastPoseMessage::entityId, ByteBufCodecs.VAR_INT, WitchercraftCastPoseMessage::ticks, WitchercraftCastPoseMessage::new);

	/** Client main thread only: entity id -> client game time the pose ends. */
	public static final Map<Integer, Long> POSED_UNTIL = new HashMap<>();
	/** Server only: entity id -> server game time the last sent pose ends. */
	private static final Map<Integer, Long> SENT_UNTIL = new HashMap<>();

	@Override
	public Type<WitchercraftCastPoseMessage> type() {
		return TYPE;
	}

	public static void send(Entity entity, int ticks) {
		if (entity == null || entity.level().isClientSide())
			return;
		long now = entity.level().getGameTime();
		Long sent = SENT_UNTIL.get(entity.getId());
		if (sent != null && sent - now >= 3)
			return;
		SENT_UNTIL.put(entity.getId(), now + ticks);
		PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new WitchercraftCastPoseMessage(entity.getId(), ticks));
	}

	public static void handleData(WitchercraftCastPoseMessage message, IPayloadContext context) {
		if (context.flow() != PacketFlow.CLIENTBOUND)
			return;
		context.enqueueWork(() -> {
			if (Minecraft.getInstance().level == null)
				return;
			POSED_UNTIL.put(message.entityId(), Minecraft.getInstance().level.getGameTime() + message.ticks());
		});
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		WitchercraftMod.addNetworkMessage(TYPE, STREAM_CODEC, WitchercraftCastPoseMessage::handleData);
	}
}

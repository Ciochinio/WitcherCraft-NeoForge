package net.redboltmedia.witchercraft;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** Server response containing one authorized layered terrain tile. */
@EventBusSubscriber
public record WorldMapTileDataMessage(int chunkX, int chunkZ, long capturedGameTime,
	short[] groundHeights, byte[] groundColors, byte[] groundTintKinds, int[] groundTints,
	short[] foliageHeights, byte[] foliageColors, byte[] foliageTintKinds, int[] foliageTints,
	short[] waterHeights, int[] waterTints, byte[] decorationKinds, byte[] decorationColors, byte[] decorationTintKinds, int[] decorationTints,
	String[] blockStatePalette, short[] groundStateIndices, short[] foliageStateIndices, short[] decorationStateIndices) implements CustomPacketPayload {
	public static final Type<WorldMapTileDataMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_tile_data"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapTileDataMessage> STREAM_CODEC = StreamCodec.of((b,m) -> {
		b.writeInt(m.chunkX); b.writeInt(m.chunkZ); b.writeLong(m.capturedGameTime);
		b.writeVarInt(m.blockStatePalette.length); for(String state:m.blockStatePalette)b.writeUtf(state,WorldMapTerrainTile.MAX_STATE_LENGTH);
		for(int i=0;i<WorldMapTerrainTile.SAMPLE_COUNT;i++){
			b.writeShort(m.groundHeights[i]); b.writeByte(m.groundColors[i]); b.writeByte(m.groundTintKinds[i]); b.writeInt(m.groundTints[i]);
			b.writeShort(m.foliageHeights[i]); b.writeByte(m.foliageColors[i]); b.writeByte(m.foliageTintKinds[i]); b.writeInt(m.foliageTints[i]);
			b.writeShort(m.waterHeights[i]); b.writeInt(m.waterTints[i]);
			b.writeByte(m.decorationKinds[i]); b.writeByte(m.decorationColors[i]); b.writeByte(m.decorationTintKinds[i]); b.writeInt(m.decorationTints[i]);
			b.writeShort(m.groundStateIndices[i]); b.writeShort(m.foliageStateIndices[i]); b.writeShort(m.decorationStateIndices[i]);
		}
	}, b -> {
		int x=b.readInt(),z=b.readInt(); long time=b.readLong(); int paletteSize=b.readVarInt(); if(paletteSize<=0||paletteSize>WorldMapTerrainTile.MAX_PALETTE_SIZE)throw new IllegalArgumentException("Invalid world-map block-state palette size"); String[] palette=new String[paletteSize]; for(int i=0;i<paletteSize;i++)palette[i]=b.readUtf(WorldMapTerrainTile.MAX_STATE_LENGTH); int n=WorldMapTerrainTile.SAMPLE_COUNT;
		short[] gh=new short[n],fh=new short[n],wh=new short[n]; byte[] gc=new byte[n],gtk=new byte[n],fc=new byte[n],ftk=new byte[n],dk=new byte[n],dc=new byte[n],dtk=new byte[n]; int[] gt=new int[n],ft=new int[n],wt=new int[n],dt=new int[n];
		short[] gsi=new short[n],fsi=new short[n],dsi=new short[n];
		for(int i=0;i<n;i++){gh[i]=b.readShort();gc[i]=b.readByte();gtk[i]=b.readByte();gt[i]=b.readInt();fh[i]=b.readShort();fc[i]=b.readByte();ftk[i]=b.readByte();ft[i]=b.readInt();wh[i]=b.readShort();wt[i]=b.readInt();dk[i]=b.readByte();dc[i]=b.readByte();dtk[i]=b.readByte();dt[i]=b.readInt();gsi[i]=b.readShort();fsi[i]=b.readShort();dsi[i]=b.readShort();}
		return new WorldMapTileDataMessage(x,z,time,gh,gc,gtk,gt,fh,fc,ftk,ft,wh,wt,dk,dc,dtk,dt,palette,gsi,fsi,dsi);
	});

	public WorldMapTileDataMessage {
		int n=WorldMapTerrainTile.SAMPLE_COUNT;
		if(groundHeights.length!=n||groundColors.length!=n||groundTintKinds.length!=n||groundTints.length!=n||foliageHeights.length!=n||foliageColors.length!=n||foliageTintKinds.length!=n||foliageTints.length!=n||waterHeights.length!=n||waterTints.length!=n||decorationKinds.length!=n||decorationColors.length!=n||decorationTintKinds.length!=n||decorationTints.length!=n||groundStateIndices.length!=n||foliageStateIndices.length!=n||decorationStateIndices.length!=n)
			throw new IllegalArgumentException("A world-map tile packet must contain exactly 256 layered samples");
		if(blockStatePalette.length==0||blockStatePalette.length>WorldMapTerrainTile.MAX_PALETTE_SIZE||!blockStatePalette[0].isEmpty())throw new IllegalArgumentException("Invalid world-map block-state palette");
		for(String state:blockStatePalette)if(state==null||state.length()>WorldMapTerrainTile.MAX_STATE_LENGTH)throw new IllegalArgumentException("Invalid world-map block-state entry");
		validateIndices(groundStateIndices,blockStatePalette.length);validateIndices(foliageStateIndices,blockStatePalette.length);validateIndices(decorationStateIndices,blockStatePalette.length);
		groundHeights=groundHeights.clone();groundColors=groundColors.clone();groundTintKinds=groundTintKinds.clone();groundTints=groundTints.clone();
		foliageHeights=foliageHeights.clone();foliageColors=foliageColors.clone();foliageTintKinds=foliageTintKinds.clone();foliageTints=foliageTints.clone();waterHeights=waterHeights.clone();waterTints=waterTints.clone();decorationKinds=decorationKinds.clone();decorationColors=decorationColors.clone();decorationTintKinds=decorationTintKinds.clone();decorationTints=decorationTints.clone();
		blockStatePalette=blockStatePalette.clone();groundStateIndices=groundStateIndices.clone();foliageStateIndices=foliageStateIndices.clone();decorationStateIndices=decorationStateIndices.clone();
	}
	private static void validateIndices(short[] indices,int paletteSize){for(short index:indices)if(Short.toUnsignedInt(index)>=paletteSize)throw new IllegalArgumentException("World-map block-state index is outside its palette");}
	public static WorldMapTileDataMessage from(WorldMapTerrainTile t){return new WorldMapTileDataMessage(t.chunkX(),t.chunkZ(),t.capturedGameTime(),t.groundHeights(),t.groundColors(),t.groundTintKinds(),t.groundTints(),t.foliageHeights(),t.foliageColors(),t.foliageTintKinds(),t.foliageTints(),t.waterHeights(),t.waterTints(),t.decorationKinds(),t.decorationColors(),t.decorationTintKinds(),t.decorationTints(),t.blockStatePalette(),t.groundStateIndices(),t.foliageStateIndices(),t.decorationStateIndices());}
	@Override public Type<WorldMapTileDataMessage> type(){return TYPE;}
	public static void handleData(WorldMapTileDataMessage m,IPayloadContext c){if(c.flow()==PacketFlow.CLIENTBOUND)c.enqueueWork(()->WorldMapClientTileCache.accept(m));}
	@SubscribeEvent public static void registerMessage(FMLCommonSetupEvent event){WitchercraftMod.addNetworkMessage(TYPE,STREAM_CODEC,WorldMapTileDataMessage::handleData);}
}

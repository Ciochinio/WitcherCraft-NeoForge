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
	short[] waterHeights, int[] waterTints) implements CustomPacketPayload {
	public static final Type<WorldMapTileDataMessage> TYPE = new Type<>(Identifier.fromNamespaceAndPath(WitchercraftMod.MODID, "world_map_tile_data"));
	public static final StreamCodec<RegistryFriendlyByteBuf, WorldMapTileDataMessage> STREAM_CODEC = StreamCodec.of((b,m) -> {
		b.writeInt(m.chunkX); b.writeInt(m.chunkZ); b.writeLong(m.capturedGameTime);
		for(int i=0;i<WorldMapTerrainTile.SAMPLE_COUNT;i++){
			b.writeShort(m.groundHeights[i]); b.writeByte(m.groundColors[i]); b.writeByte(m.groundTintKinds[i]); b.writeInt(m.groundTints[i]);
			b.writeShort(m.foliageHeights[i]); b.writeByte(m.foliageColors[i]); b.writeByte(m.foliageTintKinds[i]); b.writeInt(m.foliageTints[i]);
			b.writeShort(m.waterHeights[i]); b.writeInt(m.waterTints[i]);
		}
	}, b -> {
		int x=b.readInt(),z=b.readInt(); long time=b.readLong(); int n=WorldMapTerrainTile.SAMPLE_COUNT;
		short[] gh=new short[n],fh=new short[n],wh=new short[n]; byte[] gc=new byte[n],gtk=new byte[n],fc=new byte[n],ftk=new byte[n]; int[] gt=new int[n],ft=new int[n],wt=new int[n];
		for(int i=0;i<n;i++){gh[i]=b.readShort();gc[i]=b.readByte();gtk[i]=b.readByte();gt[i]=b.readInt();fh[i]=b.readShort();fc[i]=b.readByte();ftk[i]=b.readByte();ft[i]=b.readInt();wh[i]=b.readShort();wt[i]=b.readInt();}
		return new WorldMapTileDataMessage(x,z,time,gh,gc,gtk,gt,fh,fc,ftk,ft,wh,wt);
	});

	public WorldMapTileDataMessage {
		int n=WorldMapTerrainTile.SAMPLE_COUNT;
		if(groundHeights.length!=n||groundColors.length!=n||groundTintKinds.length!=n||groundTints.length!=n||foliageHeights.length!=n||foliageColors.length!=n||foliageTintKinds.length!=n||foliageTints.length!=n||waterHeights.length!=n||waterTints.length!=n)
			throw new IllegalArgumentException("A world-map tile packet must contain exactly 256 layered samples");
		groundHeights=groundHeights.clone();groundColors=groundColors.clone();groundTintKinds=groundTintKinds.clone();groundTints=groundTints.clone();
		foliageHeights=foliageHeights.clone();foliageColors=foliageColors.clone();foliageTintKinds=foliageTintKinds.clone();foliageTints=foliageTints.clone();waterHeights=waterHeights.clone();waterTints=waterTints.clone();
	}
	public static WorldMapTileDataMessage from(WorldMapTerrainTile t){return new WorldMapTileDataMessage(t.chunkX(),t.chunkZ(),t.capturedGameTime(),t.groundHeights(),t.groundColors(),t.groundTintKinds(),t.groundTints(),t.foliageHeights(),t.foliageColors(),t.foliageTintKinds(),t.foliageTints(),t.waterHeights(),t.waterTints());}
	@Override public Type<WorldMapTileDataMessage> type(){return TYPE;}
	public static void handleData(WorldMapTileDataMessage m,IPayloadContext c){if(c.flow()==PacketFlow.CLIENTBOUND)c.enqueueWork(()->WorldMapClientTileCache.accept(m));}
	@SubscribeEvent public static void registerMessage(FMLCommonSetupEvent event){WitchercraftMod.addNetworkMessage(TYPE,STREAM_CODEC,WorldMapTileDataMessage::handleData);}
}

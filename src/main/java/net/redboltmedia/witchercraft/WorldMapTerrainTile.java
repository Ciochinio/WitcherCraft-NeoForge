package net.redboltmedia.witchercraft;

import java.io.*;
import java.nio.file.*;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.CRC32;

/** Versioned, fixed-resolution layered terrain sample for one 16 by 16 chunk. */
public record WorldMapTerrainTile(int chunkX, int chunkZ, long capturedGameTime,
	short[] groundHeights, byte[] groundColors, byte[] groundTintKinds, int[] groundTints,
	short[] foliageHeights, byte[] foliageColors, byte[] foliageTintKinds, int[] foliageTints,
	short[] waterHeights, int[] waterTints) {
	public static final int SAMPLE_COUNT = 256;
	public static final short NO_HEIGHT = Short.MIN_VALUE;
	private static final int MAGIC = 0x5743544D, VERSION = 2, MAX_FILE_BYTES = 16 * 1024;

	public WorldMapTerrainTile {
		check(groundHeights, groundColors, groundTintKinds, groundTints, foliageHeights, foliageColors,
			foliageTintKinds, foliageTints, waterHeights, waterTints);
		groundHeights = groundHeights.clone(); groundColors = groundColors.clone(); groundTintKinds = groundTintKinds.clone(); groundTints = groundTints.clone();
		foliageHeights = foliageHeights.clone(); foliageColors = foliageColors.clone(); foliageTintKinds = foliageTintKinds.clone(); foliageTints = foliageTints.clone();
		waterHeights = waterHeights.clone(); waterTints = waterTints.clone();
	}

	private static void check(short[] gh, byte[] gc, byte[] gtk, int[] gt, short[] fh, byte[] fc, byte[] ftk, int[] ft, short[] wh, int[] wt) {
		if (gh.length != SAMPLE_COUNT || gc.length != SAMPLE_COUNT || gtk.length != SAMPLE_COUNT || gt.length != SAMPLE_COUNT
			|| fh.length != SAMPLE_COUNT || fc.length != SAMPLE_COUNT || ftk.length != SAMPLE_COUNT || ft.length != SAMPLE_COUNT
			|| wh.length != SAMPLE_COUNT || wt.length != SAMPLE_COUNT)
			throw new IllegalArgumentException("A world-map tile must contain exactly 256 layered samples");
	}

	@Override public short[] groundHeights() { return groundHeights.clone(); }
	@Override public byte[] groundColors() { return groundColors.clone(); }
	@Override public byte[] groundTintKinds() { return groundTintKinds.clone(); }
	@Override public int[] groundTints() { return groundTints.clone(); }
	@Override public short[] foliageHeights() { return foliageHeights.clone(); }
	@Override public byte[] foliageColors() { return foliageColors.clone(); }
	@Override public byte[] foliageTintKinds() { return foliageTintKinds.clone(); }
	@Override public int[] foliageTints() { return foliageTints.clone(); }
	@Override public short[] waterHeights() { return waterHeights.clone(); }
	@Override public int[] waterTints() { return waterTints.clone(); }

	public void writeAtomically(Path target) throws IOException {
		byte[] encoded = encode(); Files.createDirectories(target.getParent());
		Path temporary = target.resolveSibling(target.getFileName() + ".tmp-" + UUID.randomUUID()); Files.write(temporary, encoded);
		try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
		catch (AtomicMoveNotSupportedException ignored) { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); }
	}

	public static Optional<WorldMapTerrainTile> read(Path source, int expectedX, int expectedZ) {
		try {
			long size = Files.size(source); if (size <= 0 || size > MAX_FILE_BYTES) return Optional.empty();
			byte[] file = Files.readAllBytes(source); if (file.length < 4) return Optional.empty();
			int bodyLength = file.length - 4; CRC32 crc = new CRC32(); crc.update(file, 0, bodyLength);
			try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(file))) {
				if (in.readInt() != MAGIC || in.readInt() != VERSION) return Optional.empty();
				int x = in.readInt(), z = in.readInt(); if (x != expectedX || z != expectedZ || in.readInt() != SAMPLE_COUNT) return Optional.empty();
				long time = in.readLong(); short[] gh = shorts(), fh = shorts(), wh = shorts();
				byte[] gc = bytes(), gtk = bytes(), fc = bytes(), ftk = bytes(); int[] gt = ints(), ft = ints(), wt = ints();
				for (int i = 0; i < SAMPLE_COUNT; i++) {
					gh[i]=in.readShort(); gc[i]=in.readByte(); gtk[i]=in.readByte(); gt[i]=in.readInt();
					fh[i]=in.readShort(); fc[i]=in.readByte(); ftk[i]=in.readByte(); ft[i]=in.readInt();
					wh[i]=in.readShort(); wt[i]=in.readInt();
				}
				if (in.readInt() != (int)crc.getValue() || in.available() != 0) return Optional.empty();
				return Optional.of(new WorldMapTerrainTile(x,z,time,gh,gc,gtk,gt,fh,fc,ftk,ft,wh,wt));
			}
		} catch (IOException | RuntimeException ignored) { return Optional.empty(); }
	}

	private byte[] encode() throws IOException {
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(body)) {
			out.writeInt(MAGIC); out.writeInt(VERSION); out.writeInt(chunkX); out.writeInt(chunkZ); out.writeInt(SAMPLE_COUNT); out.writeLong(capturedGameTime);
			for (int i=0;i<SAMPLE_COUNT;i++) {
				out.writeShort(groundHeights[i]); out.writeByte(groundColors[i]); out.writeByte(groundTintKinds[i]); out.writeInt(groundTints[i]);
				out.writeShort(foliageHeights[i]); out.writeByte(foliageColors[i]); out.writeByte(foliageTintKinds[i]); out.writeInt(foliageTints[i]);
				out.writeShort(waterHeights[i]); out.writeInt(waterTints[i]);
			}
		}
		byte[] bytes=body.toByteArray(); CRC32 crc=new CRC32(); crc.update(bytes); ByteArrayOutputStream file=new ByteArrayOutputStream(bytes.length+4); file.write(bytes);
		try(DataOutputStream out=new DataOutputStream(file)){out.writeInt((int)crc.getValue());} return file.toByteArray();
	}
	private static short[] shorts(){short[] a=new short[SAMPLE_COUNT]; java.util.Arrays.fill(a,NO_HEIGHT); return a;}
	private static byte[] bytes(){return new byte[SAMPLE_COUNT];}
	private static int[] ints(){return new int[SAMPLE_COUNT];}
}

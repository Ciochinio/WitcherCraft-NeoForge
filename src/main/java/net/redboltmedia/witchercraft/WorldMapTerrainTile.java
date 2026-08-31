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
	short[] waterHeights, int[] waterTints, byte[] decorationKinds, byte[] decorationColors, byte[] decorationTintKinds, int[] decorationTints,
	String[] blockStatePalette, short[] groundStateIndices, short[] foliageStateIndices, short[] decorationStateIndices) {
	public static final int SAMPLE_COUNT = 256;
	public static final int MAX_PALETTE_SIZE = SAMPLE_COUNT * 3 + 1;
	public static final int MAX_STATE_LENGTH = 1024;
	public static final short NO_HEIGHT = Short.MIN_VALUE;
	private static final int MAGIC = 0x5743544D, VERSION = 4, OLDEST_READABLE_VERSION = 1, MAX_FILE_BYTES = 1024 * 1024;

	public WorldMapTerrainTile {
		check(groundHeights, groundColors, groundTintKinds, groundTints, foliageHeights, foliageColors,
			foliageTintKinds, foliageTints, waterHeights, waterTints, decorationKinds, decorationColors, decorationTintKinds, decorationTints,
			blockStatePalette, groundStateIndices, foliageStateIndices, decorationStateIndices);
		groundHeights = groundHeights.clone(); groundColors = groundColors.clone(); groundTintKinds = groundTintKinds.clone(); groundTints = groundTints.clone();
		foliageHeights = foliageHeights.clone(); foliageColors = foliageColors.clone(); foliageTintKinds = foliageTintKinds.clone(); foliageTints = foliageTints.clone();
		waterHeights = waterHeights.clone(); waterTints = waterTints.clone();
		decorationKinds = decorationKinds.clone(); decorationColors = decorationColors.clone(); decorationTintKinds = decorationTintKinds.clone(); decorationTints = decorationTints.clone();
		blockStatePalette = blockStatePalette.clone(); groundStateIndices = groundStateIndices.clone(); foliageStateIndices = foliageStateIndices.clone(); decorationStateIndices = decorationStateIndices.clone();
	}

	private static void check(short[] gh, byte[] gc, byte[] gtk, int[] gt, short[] fh, byte[] fc, byte[] ftk, int[] ft, short[] wh, int[] wt, byte[] dk, byte[] dc, byte[] dtk, int[] dt,
		String[] palette, short[] gsi, short[] fsi, short[] dsi) {
		if (gh.length != SAMPLE_COUNT || gc.length != SAMPLE_COUNT || gtk.length != SAMPLE_COUNT || gt.length != SAMPLE_COUNT
			|| fh.length != SAMPLE_COUNT || fc.length != SAMPLE_COUNT || ftk.length != SAMPLE_COUNT || ft.length != SAMPLE_COUNT
			|| wh.length != SAMPLE_COUNT || wt.length != SAMPLE_COUNT || dk.length != SAMPLE_COUNT || dc.length != SAMPLE_COUNT || dtk.length != SAMPLE_COUNT || dt.length != SAMPLE_COUNT
			|| gsi.length != SAMPLE_COUNT || fsi.length != SAMPLE_COUNT || dsi.length != SAMPLE_COUNT)
			throw new IllegalArgumentException("A world-map tile must contain exactly 256 layered samples");
		if (palette.length == 0 || palette.length > MAX_PALETTE_SIZE || !palette[0].isEmpty()) throw new IllegalArgumentException("Invalid world-map block-state palette");
		for (String state : palette) if (state == null || state.length() > MAX_STATE_LENGTH) throw new IllegalArgumentException("Invalid world-map block-state entry");
		validateIndices(gsi, palette.length); validateIndices(fsi, palette.length); validateIndices(dsi, palette.length);
	}
	private static void validateIndices(short[] indices, int paletteSize) { for (short index : indices) if (Short.toUnsignedInt(index) >= paletteSize) throw new IllegalArgumentException("World-map block-state index is outside its palette"); }

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
	@Override public byte[] decorationKinds() { return decorationKinds.clone(); }
	@Override public byte[] decorationColors() { return decorationColors.clone(); }
	@Override public byte[] decorationTintKinds() { return decorationTintKinds.clone(); }
	@Override public int[] decorationTints() { return decorationTints.clone(); }
	@Override public String[] blockStatePalette() { return blockStatePalette.clone(); }
	@Override public short[] groundStateIndices() { return groundStateIndices.clone(); }
	@Override public short[] foliageStateIndices() { return foliageStateIndices.clone(); }
	@Override public short[] decorationStateIndices() { return decorationStateIndices.clone(); }

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
				if (in.readInt() != MAGIC) return Optional.empty();
				int version = in.readInt();
				if (version < OLDEST_READABLE_VERSION || version > VERSION) return Optional.empty();
				int x = in.readInt(), z = in.readInt(); if (x != expectedX || z != expectedZ || in.readInt() != SAMPLE_COUNT) return Optional.empty();
				long time = in.readLong(); String[] palette = { "" };
				if (version >= 4) { int count = in.readUnsignedShort(); if (count == 0 || count > MAX_PALETTE_SIZE) return Optional.empty(); palette = new String[count]; for (int i=0;i<count;i++) { palette[i]=in.readUTF(); if (palette[i].length()>MAX_STATE_LENGTH) return Optional.empty(); } if (!palette[0].isEmpty()) return Optional.empty(); }
				short[] gh = shorts(), fh = shorts(), wh = shorts(), gsi = shortsZero(), fsi = shortsZero(), dsi = shortsZero();
				byte[] gc = bytes(), gtk = bytes(), fc = bytes(), ftk = bytes(), dk = bytes(), dc = bytes(), dtk = bytes(); int[] gt = ints(), ft = ints(), wt = ints(), dt = ints();
				for (int i = 0; i < SAMPLE_COUNT; i++) {
					gh[i]=in.readShort(); gc[i]=in.readByte(); gtk[i]=in.readByte(); gt[i]=in.readInt();
					if (version >= 2) {
						fh[i]=in.readShort(); fc[i]=in.readByte(); ftk[i]=in.readByte(); ft[i]=in.readInt();
						wh[i]=in.readShort(); wt[i]=in.readInt();
					}
					if (version >= 3) {
						dk[i]=in.readByte(); dc[i]=in.readByte(); dtk[i]=in.readByte(); dt[i]=in.readInt();
					}
					if (version >= 4) { gsi[i]=in.readShort(); fsi[i]=in.readShort(); dsi[i]=in.readShort(); }
				}
				if (in.readInt() != (int)crc.getValue() || in.available() != 0) return Optional.empty();
				return Optional.of(new WorldMapTerrainTile(x,z,time,gh,gc,gtk,gt,fh,fc,ftk,ft,wh,wt,dk,dc,dtk,dt,palette,gsi,fsi,dsi));
			}
		} catch (IOException | RuntimeException ignored) { return Optional.empty(); }
	}

	private byte[] encode() throws IOException {
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		try (DataOutputStream out = new DataOutputStream(body)) {
			out.writeInt(MAGIC); out.writeInt(VERSION); out.writeInt(chunkX); out.writeInt(chunkZ); out.writeInt(SAMPLE_COUNT); out.writeLong(capturedGameTime);
			out.writeShort(blockStatePalette.length); for (String state : blockStatePalette) out.writeUTF(state);
			for (int i=0;i<SAMPLE_COUNT;i++) {
				out.writeShort(groundHeights[i]); out.writeByte(groundColors[i]); out.writeByte(groundTintKinds[i]); out.writeInt(groundTints[i]);
				out.writeShort(foliageHeights[i]); out.writeByte(foliageColors[i]); out.writeByte(foliageTintKinds[i]); out.writeInt(foliageTints[i]);
				out.writeShort(waterHeights[i]); out.writeInt(waterTints[i]);
				out.writeByte(decorationKinds[i]); out.writeByte(decorationColors[i]); out.writeByte(decorationTintKinds[i]); out.writeInt(decorationTints[i]);
				out.writeShort(groundStateIndices[i]); out.writeShort(foliageStateIndices[i]); out.writeShort(decorationStateIndices[i]);
			}
		}
		byte[] bytes=body.toByteArray(); CRC32 crc=new CRC32(); crc.update(bytes); ByteArrayOutputStream file=new ByteArrayOutputStream(bytes.length+4); file.write(bytes);
		try(DataOutputStream out=new DataOutputStream(file)){out.writeInt((int)crc.getValue());} return file.toByteArray();
	}
	private static short[] shorts(){short[] a=new short[SAMPLE_COUNT]; java.util.Arrays.fill(a,NO_HEIGHT); return a;}
	private static short[] shortsZero(){return new short[SAMPLE_COUNT];}
	private static byte[] bytes(){return new byte[SAMPLE_COUNT];}
	private static int[] ints(){return new int[SAMPLE_COUNT];}
}

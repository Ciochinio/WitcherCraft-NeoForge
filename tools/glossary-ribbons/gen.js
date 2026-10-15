// Generates the Glossary's bookmark ribbons: a flat light grey with a little per-pixel noise and
// a swallowtail cut, one texel per GUI design pixel. They are grey so the game can tint one
// texture per category (GlossaryPage blits them with a colour). Seeded, so reruns are identical.
//
//   node tools/glossary-ribbons/gen.js <out dir>
//
// Outputs (sizes must match GlossaryLayout SIDE_W x SIDE_H, RIGHT_W x RIGHT_H, TOP_W x TOP_H):
//   glossary_ribbon_side.png       106x22  points LEFT (swallowtail on the left)
//   glossary_ribbon_side_right.png 106x22  the same, mirrored: points RIGHT
//   glossary_ribbon_top.png         30x36  points UP (swallowtail on top)
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');

const out = process.argv[2] || '.';

// ---- tiny PNG writer (RGBA, no filtering) --------------------------------------
const CRC = new Int32Array(256).map((_, n) => {
	let c = n;
	for (let k = 0; k < 8; k++)
		c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
	return c;
});
function crc32(buf) {
	let c = -1;
	for (const b of buf)
		c = CRC[(c ^ b) & 255] ^ (c >>> 8);
	return (c ^ -1) >>> 0;
}
function chunk(type, data) {
	const len = Buffer.alloc(4);
	len.writeUInt32BE(data.length);
	const td = Buffer.concat([Buffer.from(type), data]);
	const crc = Buffer.alloc(4);
	crc.writeUInt32BE(crc32(td));
	return Buffer.concat([len, td, crc]);
}
function png(w, h, rgba) {
	const ihdr = Buffer.alloc(13);
	ihdr.writeUInt32BE(w, 0);
	ihdr.writeUInt32BE(h, 4);
	ihdr[8] = 8;
	ihdr[9] = 6;
	const raw = Buffer.alloc((w * 4 + 1) * h);
	for (let y = 0; y < h; y++)
		rgba.copy(raw, y * (w * 4 + 1) + 1, y * w * 4, (y + 1) * w * 4);
	return Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw, {level: 9})), chunk('IEND', Buffer.alloc(0))]);
}

function rng(seed) {
	return () => {
		seed = (seed * 1664525 + 1013904223) >>> 0;
		return seed / 4294967296;
	};
}

const BASE = 218, NOISE = 14; // grey level, and the full spread of the noise around it

/** A ribbon along u (0 = the notched end) and across v: a grey level, or -1 where it is cut away. */
function makeRibbon(len, wid, notch, seed) {
	const r = rng(seed), mid = (wid - 1) / 2;
	const noise = Array.from({length: len * wid}, () => Math.round((r() - 0.5) * NOISE));
	return (u, v) => {
		const cut = Math.round(notch * (1 - Math.abs(v - mid) / mid));
		return u < cut ? -1 : BASE + noise[u * wid + v];
	};
}

function render(w, h, texel) {
	const buf = Buffer.alloc(w * h * 4);
	for (let y = 0; y < h; y++) {
		for (let x = 0; x < w; x++) {
			const g = texel(x, y), i = (y * w + x) * 4;
			if (g < 0)
				continue;
			buf.fill(g, i, i + 3);
			buf[i + 3] = 255;
		}
	}
	return buf;
}

fs.mkdirSync(out, {recursive: true});
const SIDE_W = 106, SIDE_H = 22, TOP_W = 30, TOP_H = 36;
const side = makeRibbon(SIDE_W, SIDE_H, 7, 7);
fs.writeFileSync(path.join(out, 'glossary_ribbon_side.png'), png(SIDE_W, SIDE_H, render(SIDE_W, SIDE_H, (x, y) => side(x, y))));
fs.writeFileSync(path.join(out, 'glossary_ribbon_side_right.png'), png(SIDE_W, SIDE_H, render(SIDE_W, SIDE_H, (x, y) => side(SIDE_W - 1 - x, y))));
const top = makeRibbon(TOP_H, TOP_W, 9, 11);
fs.writeFileSync(path.join(out, 'glossary_ribbon_top.png'), png(TOP_W, TOP_H, render(TOP_W, TOP_H, (x, y) => top(y, x))));
console.log('wrote ribbons to ' + out);

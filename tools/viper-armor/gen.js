// Writes ViperArmor.bbmodel, viper_armor.png and viper_armor.rgba (for render.js). Usage: node tools/viper-armor/gen.js <outDir>
const fs = require('fs');
const path = require('path');
const zlib = require('zlib');
const buildViper = require('./viper_core.js');

function png(w, h, rgba) {
  const crcT = new Int32Array(256).map((_, n) => { let c = n; for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1; return c; });
  const crc = b => { let c = -1; for (const x of b) c = crcT[(c ^ x) & 255] ^ (c >>> 8); return (c ^ -1) >>> 0; };
  const chunk = (t, d) => { const len = Buffer.alloc(4); len.writeUInt32BE(d.length); const td = Buffer.concat([Buffer.from(t), d]); const c = Buffer.alloc(4); c.writeUInt32BE(crc(td)); return Buffer.concat([len, td, c]); };
  const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(w, 0); ihdr.writeUInt32BE(h, 4); ihdr[8] = 8; ihdr[9] = 6;
  const raw = Buffer.alloc((w * 4 + 1) * h);
  for (let y = 0; y < h; y++) { raw[y * (w * 4 + 1)] = 0; Buffer.from(rgba.buffer, y * w * 4, w * 4).copy(raw, y * (w * 4 + 1) + 1); }
  return Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw)), chunk('IEND', Buffer.alloc(0))]);
}

const OUT = process.argv[2] || __dirname;
let pngBuf;
const { model, img } = buildViper((rgba, w, h) => { pngBuf = png(w, h, rgba); return 'data:image/png;base64,' + pngBuf.toString('base64'); });
fs.writeFileSync(path.join(OUT, 'viper_armor.png'), pngBuf);
fs.writeFileSync(path.join(OUT, 'viper_armor.rgba'), Buffer.from(img.buffer));
fs.writeFileSync(path.join(OUT, 'ViperArmor.bbmodel'), JSON.stringify(model));
console.log('elements', model.elements.length, 'png', pngBuf.length, 'bytes');

// Offline preview: renders a box-UV .bbmodel over a magenta mannequin (magenta showing = skin poking through).
// Usage: node render.js <model.bbmodel> <texture.rgba> <out.png> [scale] [views]   (views: front,back,left,right,q34,walk,top; NOHEAD=1 hides the head, as in first person)
const fs = require('fs');
const zlib = require('zlib');
const [, , MODEL, RGBA, OUTP, SCALE = '12', VIEWS = 'front,back,left,q34,walk'] = process.argv;
const m = JSON.parse(fs.readFileSync(MODEL, 'utf8'));
const tex = new Uint8Array(fs.readFileSync(RGBA));
const TW = Math.round(Math.sqrt(tex.length / 4));
const UVS = TW / m.resolution.width;
const SC = +SCALE;

const deg = a => a * Math.PI / 180;
function rot(p, o, r) { // BB element/group rotation, degrees, ZYX
  let [x, y, z] = [p[0] - o[0], p[1] - o[1], p[2] - o[2]];
  if (r[0]) { const c = Math.cos(deg(r[0])), s = Math.sin(deg(r[0])); [y, z] = [y * c - z * s, y * s + z * c]; }
  if (r[1]) { const c = Math.cos(deg(r[1])), s = Math.sin(deg(r[1])); [x, z] = [x * c + z * s, -x * s + z * c]; }
  if (r[2]) { const c = Math.cos(deg(r[2])), s = Math.sin(deg(r[2])); [x, y] = [x * c - y * s, x * s + y * c]; }
  return [x + o[0], y + o[1], z + o[2]];
}
function boxQuads(from, to, inf, uvOff, mirror, sampler) {
  const x0 = from[0] - inf, y0 = from[1] - inf, z0 = from[2] - inf, x1 = to[0] + inf, y1 = to[1] + inf, z1 = to[2] + inf;
  const w = to[0] - from[0], h = to[1] - from[1], d = to[2] - from[2];
  const [u, v] = uvOff;
  const R = {
    east: [u, v + d, u + d, v + d + h], north: [u + d, v + d, u + d + w, v + d + h],
    west: [u + d + w, v + d, u + 2 * d + w, v + d + h], south: [u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h],
    up: [u + d, v, u + d + w, v + d], down: [u + d + w, v, u + d + 2 * w, v + d],
  };
  const C = {
    north: [[x1, y1, z0], [x0, y1, z0], [x0, y0, z0], [x1, y0, z0]],
    south: [[x0, y1, z1], [x1, y1, z1], [x1, y0, z1], [x0, y0, z1]],
    east: [[x1, y1, z1], [x1, y1, z0], [x1, y0, z0], [x1, y0, z1]],
    west: [[x0, y1, z0], [x0, y1, z1], [x0, y0, z1], [x0, y0, z0]],
    up: [[x1, y1, z1], [x0, y1, z1], [x0, y1, z0], [x1, y1, z0]],
    down: [[x1, y0, z0], [x0, y0, z0], [x0, y0, z1], [x1, y0, z1]],
  };
  const N = { north: [0, 0, -1], south: [0, 0, 1], east: [1, 0, 0], west: [-1, 0, 0], up: [0, 1, 0], down: [0, -1, 0] };
  return Object.keys(C).map(f => {
    let r = R[mirror ? (f === 'east' ? 'west' : f === 'west' ? 'east' : f) : f];
    const uv = mirror ? [[r[2], r[1]], [r[0], r[1]], [r[0], r[3]], [r[2], r[3]]] : [[r[0], r[1]], [r[2], r[1]], [r[2], r[3]], [r[0], r[3]]];
    return { pts: C[f], uv, n: N[f], sampler };
  });
}
const texSampler = (u, v) => { const px = Math.min(TW - 1, Math.max(0, Math.floor(u * UVS))), py = Math.min(TW - 1, Math.max(0, Math.floor(v * UVS))); const i = (py * TW + px) * 4; return tex[i + 3] < 128 ? null : [tex[i], tex[i + 1], tex[i + 2]]; };
const solid = c => () => c;

// mannequin (player model, Blockbench coords)
const MAN = [
  { g: 'head', from: [-4, 24, -4], to: [4, 32, 4], c: [196, 160, 130] },
  { g: 'body', from: [-4, 12, -2], to: [4, 24, 2], c: [255, 0, 255] },
  { g: 'arm_r', from: [4, 12, -2], to: [8, 24, 2], c: [255, 0, 255] },
  { g: 'arm_l', from: [-8, 12, -2], to: [-4, 24, 2], c: [255, 0, 255] },
  { g: 'leg_r', from: [-0.1, 0, -2], to: [3.9, 12, 2], c: [230, 0, 230] },
  { g: 'leg_l', from: [-3.9, 0, -2], to: [0.1, 12, 2], c: [255, 0, 255] },
];
const PIVOT = { head: [0, 24, 0], body: [0, 24, 0], arm_r: [5, 22, 0], arm_l: [-5, 22, 0], leg_r: [1.9, 12, 0], leg_l: [-1.9, 12, 0] };
const GROUP_PART = g => ({ gauntlet_r: 'arm_r', gauntlet_l: 'arm_l', boot_r: 'leg_r', boot_l: 'leg_l', helmet: 'head' }[g] || g);

function scene(pose) {
  const quads = [];
  const poseOf = part => pose[part] || [0, 0, 0];
  for (const b of MAN.filter(b => !(process.env.NOHEAD && b.g === 'head'))) for (const q of boxQuads(b.from, b.to, 0, [0, 0], false, solid(b.c))) {
    q.pts = q.pts.map(p => rot(p, PIVOT[b.g], poseOf(b.g))); q.n = rot(q.n, [0, 0, 0], poseOf(b.g)); quads.push(q);
  }
  const byId = Object.fromEntries(m.elements.map(e => [e.uuid, e]));
  for (const o of m.outliner) {
    const g = m.groups.find(x => x.uuid === o.uuid);
    const part = GROUP_PART(g.name);
    for (const id of o.children) {
      const e = byId[id];
      for (const q of boxQuads(e.from, e.to, e.inflate || 0, e.uv_offset, !!e.mirror_uv, texSampler)) {
        q.pts = q.pts.map(p => rot(rot(p, e.origin, e.rotation || [0, 0, 0]), PIVOT[part], poseOf(part)));
        q.n = rot(rot(q.n, [0, 0, 0], e.rotation || [0, 0, 0]), [0, 0, 0], poseOf(part));
        q.armor = true; quads.push(q);
      }
    }
  }
  return quads;
}
const norm = v => { const l = Math.hypot(...v); return v.map(x => x / l); };
const dot = (a, b) => a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
const cross = (a, b) => [a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0]];
const L1 = norm([0.2, 1, -0.7]), L2 = norm([-0.2, 1, 0.7]);

function render(quads, fwd, W, H, cx, cy) {
  const f = norm(fwd), r = norm(cross(f, [0, 1, 0])), u = cross(r, f); // camera right and up
  const col = new Uint8Array(W * H * 3).fill(150), zb = new Float32Array(W * H).fill(1e9);
  for (const q of quads) {
    const P = q.pts.map(p => [cx + dot(p, r) * SC, cy - dot(p, u) * SC, dot(p, f)]);
    const lit = Math.min(1, 0.45 + 0.55 * (Math.max(0, dot(q.n, L1)) + Math.max(0, dot(q.n, L2))));
    for (const [a, b, c] of [[0, 1, 2], [0, 2, 3]]) {
      const A = P[a], B = P[b], Cc = P[c];
      const minx = Math.max(0, Math.floor(Math.min(A[0], B[0], Cc[0]))), maxx = Math.min(W - 1, Math.ceil(Math.max(A[0], B[0], Cc[0])));
      const miny = Math.max(0, Math.floor(Math.min(A[1], B[1], Cc[1]))), maxy = Math.min(H - 1, Math.ceil(Math.max(A[1], B[1], Cc[1])));
      const den = (B[1] - Cc[1]) * (A[0] - Cc[0]) + (Cc[0] - B[0]) * (A[1] - Cc[1]);
      if (Math.abs(den) < 1e-9) continue;
      for (let y = miny; y <= maxy; y++) for (let x = minx; x <= maxx; x++) {
        const px = x + 0.5, py = y + 0.5;
        const wa = ((B[1] - Cc[1]) * (px - Cc[0]) + (Cc[0] - B[0]) * (py - Cc[1])) / den;
        const wb = ((Cc[1] - A[1]) * (px - Cc[0]) + (A[0] - Cc[0]) * (py - Cc[1])) / den;
        const wc = 1 - wa - wb;
        if (wa < -1e-6 || wb < -1e-6 || wc < -1e-6) continue;
        const z = wa * A[2] + wb * B[2] + wc * Cc[2] - (q.armor ? 0.0001 : 0);
        const i = y * W + x;
        if (z >= zb[i]) continue;
        const u = wa * q.uv[a][0] + wb * q.uv[b][0] + wc * q.uv[c][0], v = wa * q.uv[a][1] + wb * q.uv[b][1] + wc * q.uv[c][1];
        const t = q.sampler(u, v); if (!t) continue;
        zb[i] = z; col[i * 3] = t[0] * lit; col[i * 3 + 1] = t[1] * lit; col[i * 3 + 2] = t[2] * lit;
      }
    }
  }
  return col;
}
const VIEWDEF = {
  front: { f: [0, 0, 1], pose: {} }, back: { f: [0, 0, -1], pose: {} }, left: { f: [1, 0, 0], pose: {} },
  right: { f: [-1, 0, 0], pose: {} }, q34: { f: [1, 0, 1.4], pose: {} },
  walk: { f: [1, 0, 0.35], pose: { arm_r: [35, 0, 0], arm_l: [-35, 0, 0], leg_l: [40, 0, 0], leg_r: [-40, 0, 0] } },
  top: { f: [0.35, -1, 0.7], pose: {} }, // from above and in front, like looking down at the shoulders
};
const views = VIEWS.split(',');
const VW = Math.round(26 * SC), VH = Math.round(36 * SC);
const W = VW * views.length, H = VH;
const out = new Uint8Array(W * H * 3);
views.forEach((name, k) => {
  const vd = VIEWDEF[name];
  const c = render(scene(vd.pose), vd.f, VW, VH, VW / 2, VH - 2 * SC);
  for (let y = 0; y < VH; y++) for (let x = 0; x < VW; x++) for (let ch = 0; ch < 3; ch++) out[(y * W + k * VW + x) * 3 + ch] = c[(y * VW + x) * 3 + ch];
});
function png(w, h, rgb) {
  const crcT = new Int32Array(256).map((_, n) => { let c = n; for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1; return c; });
  const crc = b => { let c = -1; for (const x of b) c = crcT[(c ^ x) & 255] ^ (c >>> 8); return (c ^ -1) >>> 0; };
  const chunk = (t, d) => { const len = Buffer.alloc(4); len.writeUInt32BE(d.length); const td = Buffer.concat([Buffer.from(t), d]); const c = Buffer.alloc(4); c.writeUInt32BE(crc(td)); return Buffer.concat([len, td, c]); };
  const ihdr = Buffer.alloc(13); ihdr.writeUInt32BE(w, 0); ihdr.writeUInt32BE(h, 4); ihdr[8] = 8; ihdr[9] = 2;
  const raw = Buffer.alloc((w * 3 + 1) * h);
  for (let y = 0; y < h; y++) Buffer.from(rgb.buffer, y * w * 3, w * 3).copy(raw, y * (w * 3 + 1) + 1);
  return Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', ihdr), chunk('IDAT', zlib.deflateSync(raw)), chunk('IEND', Buffer.alloc(0))]);
}
fs.writeFileSync(OUTP, png(W, H, out));
console.log('wrote', OUTP, W + 'x' + H);

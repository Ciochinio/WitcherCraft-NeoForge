// v4: the v3 design (dark palette, snakeskin chest, wavy mail, ring-mail belly, stitched leather) redrawn
// as 1-pixel tiles at the mod's armor resolution: 128x128 texture on the 64x64 box-UV layout (2 px per unit).
function buildViper(encodePng) {
  const S = 2, TW = 64 * S;
  const hex = h => [parseInt(h.slice(0, 2), 16), parseInt(h.slice(2, 4), 16), parseInt(h.slice(4, 6), 16)];
  const mix = (a, b, t) => a.map((v, i) => v + (b[i] - v) * t);
  const mul = (c, f) => c.map(v => v * f);
  const P = (lo, base, hi, edge) => ({ lo: hex(lo), base: hex(base), hi: hex(hi), edge: hex(edge || hi) });
  const L = P('121110', '24221F', '3E3A34', '58534B');   // black leather
  const ST = P('17120D', '30261D', '52422F', '66523C');  // strap leather
  const OL = P('1C1B14', '34312A', '4E4A36', '625C44');  // olive leather (knee wraps)
  const CL = P('120F0C', '1F1A15', '2E2720');            // trousers cloth
  const BT = P('141210', '2C2926', '48433C', '5A544C');  // boots
  const TN = P('3A2E1E', '5A4A32', '7A6646');            // tan glove bands
  const THREAD = hex('6A5A44'), LACE = hex('5E4A38');
  const BR = P('2A2018', '5E4A38', '7A6048');            // brown leather accent
  const STEEL_D = hex('3A4442'), STEEL = hex('6F7A78'), STEEL_HI = hex('A9B6B2');
  const GOLD = hex('A07C34'), GOLD_HI = hex('D8B460'), CORD = hex('5E4630'), SOLE = hex('1E1610'), WELT = hex('3A2C20');
  const MAIL = [hex('36332D'), hex('4B473F'), hex('4B473F'), hex('6C675B')];   // Griffin's mail: dark, mid, mid, light
  const BELLY = [hex('2C3533'), hex('414D4B'), hex('414D4B'), hex('5E6E6A')];   // same mail, teal tint
  const SNAKE = [hex('25281F'), hex('363A2D'), hex('3C4033'), hex('585D48')];  // outline, shade, body, highlight
  const QUILT = [hex('1C1B15'), hex('2C2A21'), hex('38352A'), hex('504B39')];

  // ---------- texture ----------
  const img = new Uint8Array(TW * TW * 4);
  function hash(x, y) { let h = (x * 374761393 + y * 668265263) ^ 0x5bd1e995; h = (h ^ (h >>> 13)) * 1274126177; h ^= h >>> 16; return ((h >>> 0) % 1000) / 500 - 1; }
  function put(px, py, c) {
    const n = 1 + hash(px, py) * 0.02;
    const i = (py * TW + px) * 4;
    for (let k = 0; k < 3; k++) img[i + k] = Math.max(0, Math.min(255, Math.round(c[k] * n)));
    img[i + 3] = 255;
  }
  // structured shading: lighter in the middle of a face, darker at the side edges, light from above
  function shade(x, y, w, h, amt = 0.16, vg = 0.10) {
    const cx = w > 2 ? 2 * (x + 0.5) / w - 1 : 0;
    return (1 - amt * cx * cx) * (1 + vg / 2 - vg * (y + 0.5) / h);
  }
  const lea = (M, x, y, w, h, amt = 0.2, vg = 0.1) => mul(M.base, shade(x, y, w, h, amt, vg));
  const stitch = (M, i) => (i % 2 === 0 ? mix(M.hi, THREAD, 0.6) : M.hi); // 1-on 1-off stitches along a lit edge
  // 1-pixel tiles
  const mail = (x, y) => ((x + y) % 2 ? BELLY[0] : BELLY[3]); // zigzag link columns
  const rings = (x, y) => ((x + y) % 2 ? BELLY[0] : BELLY[3]);
  function diamond(x, y, T) { // 4-px diamond scales: outline, lit top, shaded right/bottom
    const a = ((x + y) % 4 + 4) % 4, b = ((x - y) % 4 + 4) % 4;
    if (a === 0 || b === 0) return T[0];
    if (a === 1 && b === 3) return T[3];
    if ((a === 2 && b === 2) || (a === 1 && b === 1)) return T[2];
    return T[1];
  }

  function faces(u, v, w, h, d) {
    return {
      east: { x: u * S, y: (v + d) * S, w: d * S, h: h * S },
      north: { x: (u + d) * S, y: (v + d) * S, w: w * S, h: h * S },
      west: { x: (u + d + w) * S, y: (v + d) * S, w: d * S, h: h * S },
      south: { x: (u + 2 * d + w) * S, y: (v + d) * S, w: w * S, h: h * S },
      up: { x: (u + d) * S, y: v * S, w: w * S, h: d * S },
      down: { x: (u + d + w) * S, y: v * S, w: w * S, h: d * S },
    };
  }
  function paint(r, fn) {
    for (let y = 0; y < r.h; y++) for (let x = 0; x < r.w; x++) {
      const c = fn(x, y, r.w, r.h);
      if (c) put(r.x + x, r.y + y, c);
    }
  }
  const all = (F, fn) => Object.values(F).forEach(r => paint(r, fn));

  // ---------- parts ----------
  // Coordinates are Blockbench's: -x is the player's left, -z is the front. Left-side parts are painted;
  // right-side copies share the UV with mirror_uv (outer face = west on the left side).
  const parts = [];
  function part(group, name, from, to, inflate, uv, extra = {}) { parts.push({ group, name, from, to, inflate, uv, ...extra }); }
  function pair(gl, gr, name, from, to, infL, infR, uv, extra = {}) {
    part(gl, name, from, to, infL, uv, extra.left || {});
    part(gr, name, [-to[0], from[1], from[2]], [-from[0], to[1], to[2]], infR, uv, { mirror: true, ...(extra.right || {}) });
  }
  const size = p => [p.to[0] - p.from[0], p.to[1] - p.from[1], p.to[2] - p.from[2]];

  // BODY
  part('body', 'jacket', [-4, 12, -2], [4, 24, 2], 0.45, [0, 0]);
  part('body', 'chest', [-4, 19, -2], [4, 24, 2], 0.6, [0, 16]);
  part('body', 'collar', [-3, 23.5, -2.5], [3, 24.5, 2.5], 0.2, [0, 34]); // top at 24.7, clear of the chest piece's top at 24.6
  part('body', 'baldric', [-5.5, 20.4, -3.15], [4.5, 21.4, -2.15], -0.05, [0, 42], { rot: [0, 0, 48], origin: [-0.5, 20.9, -2.65] });
  part('body', 'baldric_back', [-5.5, 20.4, 2.15], [4.5, 21.4, 3.15], -0.05, [0, 42], { rot: [0, 0, 48], origin: [-0.5, 20.9, 2.65] });
  part('body', 'belt', [-5, 13.2, -3], [5, 15.2, 3], -0.4, [0, 26]);
  part('body', 'buckle', [-1, 13.7, -3], [1, 14.7, -2], -0.1, [56, 0]);
  part('body', 'tassel', [-4, 11.9, -2.95], [-3, 13.9, -1.95], -0.2, [56, 2]);
  part('body', 'tassel', [3, 11.9, -2.95], [4, 13.9, -1.95], -0.2, [56, 2], { mirror: true });
  // ARMS (arm_l = -x)
  pair('arm_l', 'arm_r', 'sleeve', [-8, 12, -2], [-4, 24, 2], 0.36, 0.36, [24, 0]);
  pair('arm_l', 'arm_r', 'pauldron', [-9, 21.5, -2.5], [-4, 24.5, 2.5], 0.3, 0.3, [40, 16],
    { left: { rot: [0, 0, 5], origin: [-4, 25, 0] }, right: { rot: [0, 0, -5], origin: [4, 25, 0] } });
  pair('arm_l', 'arm_r', 'pauldron_low', [-8, 19.5, -2], [-4, 21.5, 2], 0.4, 0.4, [48, 24]);
  pair('arm_l', 'arm_r', 'cuff', [-8, 16, -2], [-4, 18, 2], 0.55, 0.55, [48, 30]);
  pair('arm_l', 'arm_r', 'glove', [-8, 12, -2], [-4, 16, 2], 0.42, 0.42, [32, 26]);
  // LEGS (leg_l = -x); left/right inflate differs so the legs never z-fight
  pair('leg_l', 'leg_r', 'trousers', [-3.9, 0, -2], [0.1, 12, 2], 0.4, 0.35, [40, 0]);
  pair('leg_l', 'leg_r', 'knee_wrap', [-3.9, 6.6, -2], [0.1, 8.6, 2], 0.5, 0.45, [22, 34]);
  // BOOTS
  pair('boot_l', 'boot_r', 'boot', [-3.9, 0, -2], [0.1, 5, 2], 0.5, 0.45, [24, 16]);
  pair('boot_l', 'boot_r', 'boot_cuff', [-4.4, 4.5, -2.5], [0.6, 5.5, 2.5], 0.2, 0.15, [38, 36]);
  pair('boot_l', 'boot_r', 'toe', [-3.9, -0.45, -3.45], [0.1, 0.55, -2.45], 0.15, 0.12, [24, 42]);

  // ---------- UV overlap check ----------
  {
    const seen = new Map();
    const occ = new Array(64 * 64).fill(null);
    for (const p of parts) {
      const key = p.uv.join(',');
      const [w, h, d] = size(p);
      if (seen.has(key)) { const q = seen.get(key); if (size(q).join() !== size(p).join()) throw new Error('uv shared with different size: ' + p.name); continue; }
      seen.set(key, p);
      const fw = 2 * d + 2 * w, fh = d + h;
      const [u, v] = p.uv;
      if (u + fw > 64 || v + fh > 64) throw new Error('uv out of bounds: ' + p.name);
      for (let y = v; y < v + fh; y++) for (let x = u; x < u + fw; x++) {
        const inUp = y < v + d && ((x >= u + d && x < u + d + 2 * w));
        const inSide = y >= v + d;
        if (!inUp && !inSide) continue;
        if (occ[y * 64 + x]) throw new Error(`uv overlap: ${p.name} with ${occ[y * 64 + x]} at ${x},${y}`);
        occ[y * 64 + x] = p.name;
      }
    }
  }
  const F = name => { const p = parts.find(q => q.name === name); const [w, h, d] = size(p); return faces(p.uv[0], p.uv[1], w, h, d); };

  // ---------- painting ----------
  // jacket, 24 rows over 11.55..24.45: chest hem at row 11, belt rows 18-20, hem row 23
  {
    const J = F('jacket');
    const zones = (c, y) => (y === 11 ? mul(c, 0.55) : y === 12 ? mul(c, 0.8) : y === 21 ? mul(c, 0.7) : y === 23 ? L.lo : c);
    paint(J.north, (x, y, w, h) => {
      let c;
      if (x >= 4 && x <= 11) {
        c = mul(rings(x + 1, y), shade(x - 4, y - 11, 8, 13, 0.2, 0.1));
        if (x === 4 || x === 11) c = y % 2 === 0 ? LACE : mul(c, 0.6);       // laces through the panel edge
      } else if (x === 3 || x === 12) c = L.edge;                              // rolled panel edge over the mail
      else {
        const inner = x < 4 ? 2 - x : x - 13;                                  // 0 next to the edge, 2 at the side
        c = mul(L.base, 1.05 - 0.1 * inner);
        if (inner === 1 && y % 2 === 0) c = mix(c, THREAD, 0.4);               // stitched panel seam
      }
      return zones(c, y);
    });
    paint(J.south, (x, y, w, h) => zones(lea(L, x, y, w, h, 0.25), y));
    for (const s of [J.east, J.west]) paint(s, (x, y, w, h) => zones(lea(L, x, y, w, h, 0.3), y));
    paint(J.up, () => L.lo); paint(J.down, () => L.lo);
  }
  // chest piece, 10 rows over 18.4..24.6: snakeskin, V-straps with buckles, stitched hem; wavy mail on the back
  {
    const C = F('chest');
    const hem = (c, x, y, h) => (y === h - 2 ? stitch(L, x) : y === h - 1 ? L.lo : c);
    paint(C.north, (x, y, w, h) => {
      return hem(mul(diamond(x, y + 1, SNAKE), shade(x, y, w, h, 0.2, 0.12)), x, y, h);
    });
    for (const s of [C.east, C.west]) paint(s, (x, y, w, h) => hem(mul(diamond(x, y + 1, SNAKE), shade(x, y, w, h, 0.3, 0.12)), x, y, h));
    paint(C.south, (x, y, w, h) => hem(mul(mail(x + 1, y), shade(x, y, w, h, 0.22, 0.12)), x, y, h));
    paint(C.up, (x, y) => mail(x, y));
    paint(C.down, () => L.lo);
  }
  // collar: only its lower edge shows under the head
  {
    const K = F('collar');
    all(K, (x, y, w, h) => (y === 0 ? L.hi : lea(L, x, y, w, h)));
    paint(K.north, (x, y, w, h) => (y === 0 ? L.hi : (x === 5 || x === 6) ? L.lo : lea(L, x, y, w, h)));
    // top: a leather rim round the neck, open in the middle so the chest piece's mail top shows through
    paint(K.up, (x, y, w, h) => (x === 0 || x === w - 1 || y === 0 || y === h - 1 ? L.edge : null));
    for (let y = 1; y < K.up.h - 1; y++) for (let x = 1; x < K.up.w - 1; x++) img[((K.up.y + y) * TW + K.up.x + x) * 4 + 3] = 0;
  }
  // baldric: stitched top edge, steel buckle near the low end (front)
  {
    const B = F('baldric');
    all(B, (x, y) => (y === 0 ? stitch(ST, x) : ST.base));
    paint(B.north, (x, y) => (x === 13 ? (y === 0 ? STEEL_HI : STEEL) : x === 14 ? (y === 0 ? STEEL : STEEL_D) : x === 15 ? mul(y === 0 ? ST.hi : ST.base, 0.7) : y === 0 ? stitch(ST, x) : ST.base));
  }
  // belt: lit edge, stitch row, dark bottom
  {
    const B = F('belt');
    all(B, (x, y, w, h) => mul(y === 0 ? ST.edge : y === 1 ? (x % 2 === 0 ? mix(ST.base, THREAD, 0.5) : ST.base) : y === 2 ? ST.base : ST.lo, shade(x, y, w, h, 0.12, 0)));
    paint(B.up, () => ST.lo); paint(B.down, () => ST.lo);
  }
  // buckle: steel frame
  {
    const B = F('buckle');
    all(B, () => STEEL);
    paint(B.north, (x, y) => (y === 0 ? (x === 3 ? STEEL : STEEL_HI) : x === 0 ? STEEL : x === 3 ? STEEL_D : ST.lo));
  }
  // tassel: cord and gold tip
  {
    const T = F('tassel');
    all(T, (x, y) => (y === 0 ? mul(CORD, x === 0 ? 1.25 : 1) : y === 1 ? mul(CORD, x === 0 ? 1 : 0.8) : y === 2 ? (x === 0 ? GOLD_HI : GOLD) : (x === 0 ? GOLD : mul(GOLD, 0.7))));
  }
  // sleeve: mail under the pauldrons, dark stitched sleeve below
  {
    const SL = F('sleeve');
    all(SL, (x, y, w, h) => {
      if (y <= 8) return mul(mail(x, y), 0.85);
      let c = mul(QUILT[1], shade(x, y - 9, w, h - 9, 0.2, 0.1));
      if ((x === 2 || x === 5) && y % 2 === 0) c = mix(c, THREAD, 0.35);
      if (y === 9) c = mul(c, 0.55);
      return c;
    });
  }
  // pauldron: teal mail, rolled top edge, black leather trim at the bottom
  {
    const Pd = F('pauldron');
    const side = (x, y, w, h) => (y === 0 ? L.edge : y === h - 1 ? L.lo : y === h - 2 ? L.hi : mul(mail(x, y), shade(x, y, w, h, 0.15, 0.1)));
    paint(Pd.west, side); paint(Pd.north, side); paint(Pd.south, side);
    paint(Pd.east, () => mul(L.base, 0.75));
    paint(Pd.up, (x, y, w, h) => (x === w - 1 ? L.edge : x === 0 ? mul(L.base, 0.75) : y === 0 || y === h - 1 ? L.hi : mail(x, y)));
    paint(Pd.down, () => L.lo);
  }
  // lower pauldron: mail, shadowed under the cap, open fringe at the bottom like Griffin's mail
  {
    const Pl = F('pauldron_low');
    const face = (x, y, w, h) => (y === h - 1 ? (x % 2 === 0 ? mail(x, y + 1) : null) : y === 0 ? mul(mail(x, y + 1), 0.6) : mail(x, y + 1));
    paint(Pl.west, face); paint(Pl.north, face); paint(Pl.south, face);
    paint(Pl.east, () => mul(L.base, 0.75));
    paint(Pl.up, (x, y) => mail(x, y)); paint(Pl.down, () => L.lo);
  }
  // cuff: flared olive leather with a black binding; the outer and inner faces end in a point over the glove
  {
    const Q = F('cuff');
    const straight = (x, y, w, h) => (y === 0 ? L.edge : y === h - 1 ? mul(QUILT[1], 0.75) : mul(y === 1 ? QUILT[3] : QUILT[2], shade(x, y, w, h, 0.2, 0)));
    const pointed = (x, y, w, h) => {
      if (y === 0) return L.edge;
      const half = [4, 2, 1][y - 1];                              // olive half-width per row: 8, 4, 2 px
      if (Math.abs(x + 0.5 - w / 2) > half) return mul(L.base, 0.6);
      return y === 1 ? QUILT[3] : y === 2 ? mul(QUILT[2], shade(x, y, w, h, 0.2, 0)) : mul(QUILT[2], 0.8);
    };
    paint(Q.north, straight); paint(Q.south, straight); paint(Q.west, pointed); paint(Q.east, pointed);
    paint(Q.up, () => L.lo); paint(Q.down, () => QUILT[0]);
  }
  // glove: tan wrist strap with a steel buckle on the outside, raised back-of-hand plate, finger lines
  {
    const G = F('glove');
    const base = (x, y, w, h) => {
      if (y <= 1) return mul(L.base, 0.55);                       // under the cuff
      if (y === 2) return mul(L.base, 0.8);
      if (y === 3) return mul(TN.hi, shade(x, 0, w, 1, 0.2, 0));  // wrist strap
      if (y === 4) return mul(L.base, 0.8);
      return mul(L.base, 1.1 * shade(x, y, w, h, 0.22, 0));
    };
    paint(G.west, (x, y, w, h) => {                                // back of the hand
      if ((y === 3 || y === 4) && (x === 3 || x === 4)) return y === 3 ? (x === 3 ? STEEL_HI : STEEL) : (x === 3 ? STEEL : STEEL_D);
      if (y >= 5 && x >= 1 && x <= 6) {
        if (y === 5) return L.edge;
        if (x === 1) return L.hi;
        if (x === 6) return mul(L.base, 0.85);
        if (y === 7 && (x === 2 || x === 4)) return L.lo;          // knuckle gaps
        return mul(L.base, 1.25);
      }
      return base(x, y, w, h);
    });
    const fingers = (x, y, w, h) => (y >= 6 && (x === 2 || x === 5) ? L.lo : base(x, y, w, h));
    paint(G.north, fingers); paint(G.south, fingers); paint(G.east, base);
    paint(G.up, () => L.lo); paint(G.down, (x, y) => (x === 2 || x === 5 ? mul(L.lo, 0.8) : L.lo));
  }
  // trousers: padded channels down the front of the thigh, a seam down the outside
  {
    const T = F('trousers');
    const cloth = (x, y, w, h) => { let c = mul(CL.base, shade(x, y, w, h, 0.25, 0.12)); if (y === 0) c = mul(c, 0.6); if (y === 1) c = mul(c, 0.85); return c; };
    const under = (c, y) => (y === 0 ? mul(c, 0.6) : y === 1 ? mul(c, 0.85) : c);
    all(T, cloth);
    const CH = [0.8, 1.22, 0.62, 1.05, 1.22, 0.62, 1.05, 0.8];      // edge, puff, seam, puff, puff, seam, puff, edge
    paint(T.north, (x, y, w, h) => under(mul(CL.base, CH[x] * (1.05 - 0.1 * (y + 0.5) / h)), y));
    paint(T.west, (x, y, w, h) => { const c = cloth(x, y, w, h); return x === 4 ? mul(c, 0.65) : x === 3 ? mul(c, 1.15) : c; });
  }
  // knee wrap: rounded olive knee pad on the front, held by a dark leather strap round the leg
  {
    const K = F('knee_wrap');
    const C0 = mul(CL.base, 0.9);
    const strap = (y) => (y === 1 ? ST.hi : ST.base);
    all(K, (x, y, w, h) => (y === 1 || y === 2 ? mul(strap(y), shade(x, 0, w, 1, 0.2, 0)) : C0));
    const PAD = [
      [C0, C0, OL.edge, OL.edge, OL.edge, OL.edge, C0, C0],
      [ST.hi, ST.hi, OL.hi, OL.hi, OL.hi, OL.base, ST.hi, ST.hi],
      [ST.base, ST.base, OL.base, OL.base, OL.base, OL.lo, ST.base, ST.base],
      [C0, C0, C0, OL.lo, OL.lo, C0, C0, C0],
    ];
    paint(K.north, (x, y) => PAD[y][x]);
    paint(K.up, () => CL.lo); paint(K.down, () => CL.lo);
  }
  // boots: charcoal, ankle crease, welt and sole; rolled cuff; toe cap
  {
    const B = F('boot');
    all(B, (x, y, w, h) => {
      if (y === h - 1) return SOLE;
      if (y === h - 2) return WELT;
      let c = mul(BT.base, shade(x, y, w, h, 0.22, -0.14));
      if (y === 6) c = mul(c, 0.75);
      if (y === 5) c = mul(c, 1.12);
      return c;
    });
    paint(B.up, () => BT.lo); paint(B.down, () => SOLE);
    const C = F('boot_cuff');
    all(C, (x, y, w, h) => mul(y === 0 ? BT.edge : (x % 2 === 0 ? mix(BT.base, THREAD, 0.35) : BT.base), shade(x, 0, w, 1, 0.14, 0)));
    paint(C.up, () => BT.lo); paint(C.down, () => BT.lo);
    const T = F('toe');
    all(T, (x, y) => (y === 1 ? SOLE : mix(BT.hi, BT.edge, 0.4)));
    paint(T.up, () => BT.hi);
    paint(T.down, () => SOLE);
  }

  // ---------- symmetry: boxes centred on the body get their right half mirrored from the left ----------
  function symmetrize(name) {
    const B = F(name);
    const get = (x, y) => img.slice((y * TW + x) * 4, (y * TW + x) * 4 + 4);
    const setp = (x, y, v) => img.set(v, (y * TW + x) * 4);
    for (const k of ['north', 'south', 'up', 'down']) { const r = B[k]; for (let y = 0; y < r.h; y++) for (let x = 0; x < r.w / 2; x++) setp(r.x + r.w - 1 - x, r.y + y, get(r.x + x, r.y + y)); }
    const e = B.east, wf = B.west;
    for (let y = 0; y < e.h; y++) for (let x = 0; x < e.w; x++) setp(wf.x + wf.w - 1 - x, wf.y + y, get(e.x + x, e.y + y));
  }
  for (const n of ['jacket', 'chest', 'collar', 'belt', 'buckle']) symmetrize(n);

  const pngDataUrl = encodePng(img, TW, TW);
  // ---------- bbmodel ----------
  const uuid = () => globalThis.crypto.randomUUID();
  function bbFaces(p) {
    const [w, h, d] = size(p); const [u, v] = p.uv;
    const r = {
      east: [u, v + d, u + d, v + d + h], north: [u + d, v + d, u + d + w, v + d + h],
      west: [u + d + w, v + d, u + 2 * d + w, v + d + h], south: [u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h],
      up: [u + d + w, v + d, u + d, v], down: [u + d + 2 * w, v, u + d + w, v + d],
    };
    if (p.mirror) {
      const fl = a => [a[2], a[1], a[0], a[3]];
      const e = r.east, wst = r.west;
      r.east = fl(wst); r.west = fl(e); r.north = fl(r.north); r.south = fl(r.south); r.up = fl(r.up); r.down = fl(r.down);
    }
    return Object.fromEntries(Object.entries(r).map(([k, uv]) => [k, { uv, texture: 0 }]));
  }
  const groupDefs = [
    ['helmet', [0, 24, 0]], ['body', [0, 24, 0]], ['arm_r', [5, 22, 0]], ['arm_l', [-5, 22, 0]],
    ['gauntlet_r', [5, 22, 0]], ['gauntlet_l', [-5, 22, 0]], ['leg_r', [1.9, 12, 0]], ['leg_l', [-1.9, 12, 0]],
    ['boot_r', [1.9, 12, 0]], ['boot_l', [-1.9, 12, 0]],
  ];
  const groups = groupDefs.map(([name, origin]) => ({ name, uuid: uuid(), export: true, locked: false, scope: 0, selected: false, origin, rotation: [0, 0, 0], color: 1, children: [], reset: false, shade: true, mirror_uv: false, visibility: true, autouv: 0, isOpen: true, primary_selected: false }));
  const elements = parts.map(p => {
    const e = { name: p.name, box_uv: true, render_order: 'default', locked: false, export: true, scope: 0, allow_mirror_modeling: true, from: p.from, to: p.to, autouv: 0, color: 1, inflate: p.inflate };
    if (p.rot) e.rotation = p.rot;
    e.origin = p.origin || groupDefs.find(g => g[0] === p.group)[1];
    e.uv_offset = p.uv;
    if (p.mirror) e.mirror_uv = true;
    e.faces = bbFaces(p); e.type = 'cube'; e.uuid = uuid();
    return e;
  });
  const outliner = groups.map(g => ({ uuid: g.uuid, isOpen: true, children: elements.filter((e, i) => parts[i].group === g.name).map(e => e.uuid) }));
  const model = {
    meta: { format_version: '5.0', model_format: 'modded_entity', box_uv: true },
    name: 'ViperArmor', model_identifier: 'viper_armor', modded_entity_entity_class: '', modded_entity_version: '1.17', modded_entity_flip_y: true,
    visible_box: [1, 1, 0], variable_placeholders: '', multi_file_ruleset: '', variable_placeholder_buttons: [], timeline_setups: [], unhandled_root_fields: {},
    resolution: { width: 64, height: 64 }, elements, groups, outliner,
    textures: [{ name: 'viper_armor', path: '', folder: 'entity', namespace: '', id: '0', group: '', scope: 0, width: TW, height: TW, uv_width: 64, uv_height: 64, particle: false, use_as_default: false, layers_enabled: false, sync_to_project: '', file_format: 'png', render_mode: 'default', render_sides: 'auto', wrap_mode: 'limited', pbr_channel: 'color', fps: 7, frame_time: 1, frame_order_type: 'loop', frame_order: '', frame_interpolate: false, visible: true, internal: true, saved: false, uuid: uuid(), source: pngDataUrl }],
    animations: [],
  };

  return { model, img };
}
if (typeof module !== 'undefined') module.exports = buildViper;

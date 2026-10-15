// Lists pairs of faces from different cubes (any bone, rest pose) that lie in the same plane and overlap,
// which flicker in game (z-fighting). Rotated cubes are skipped. Usage: node zfight.js <model.bbmodel>
const fs = require('fs');
const m = JSON.parse(fs.readFileSync(process.argv[2], 'utf8'));
const byId = Object.fromEntries(m.elements.map(e => [e.uuid, e]));
const EPS = 0.001;
let found = 0;
const faces = [];
for (const o of m.outliner) {
  const g = m.groups.find(x => x.uuid === o.uuid);
  const cubes = o.children.map(id => byId[id]).filter(e => !(e.rotation && e.rotation.some(r => r)));

  for (const e of cubes) {
    const i = e.inflate || 0, lo = e.from.map(v => v - i), hi = e.to.map(v => v + i);
    for (let ax = 0; ax < 3; ax++) for (const [side, c] of [['min', lo[ax]], ['max', hi[ax]]]) {
      const [a, b] = [0, 1, 2].filter(k => k !== ax);
      faces.push({ e, g: g.name, ax, side, c, r: [lo[a], hi[a], lo[b], hi[b]] });
    }
  }
}
for (let p = 0; p < faces.length; p++) for (let q = p + 1; q < faces.length; q++) {
  const A = faces[p], B = faces[q];
  if (A.e === B.e || A.ax !== B.ax || Math.abs(A.c - B.c) > EPS) continue;
  const ox = Math.min(A.r[1], B.r[1]) - Math.max(A.r[0], B.r[0]), oy = Math.min(A.r[3], B.r[3]) - Math.max(A.r[2], B.r[2]);
  if (ox <= EPS || oy <= EPS) continue;
  found++;
  console.log(`${A.g}/${A.e.name} ${'xyz'[A.ax]}${A.side} and ${B.g}/${B.e.name} ${'xyz'[B.ax]}${B.side} at ${A.c.toFixed(3)} (overlap ${ox.toFixed(2)} x ${oy.toFixed(2)})`);
}
console.log(found ? `${found} coplanar overlap(s)` : 'no coplanar overlaps');

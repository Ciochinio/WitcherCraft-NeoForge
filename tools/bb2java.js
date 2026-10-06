// Converts a modded_entity .bbmodel into an MCreator-style Mojmap Java model (26.1 API).
// Mirrors Blockbench's export: X and Y are negated (Y measured from 24), Z kept, Z-rotation sign kept,
// X/Y rotations negated. Rotated cubes get their own child part.
// Usage (from the repo root, Node.js required):
//   node tools/bb2java.js models/blockbench/WolvenArmor.bbmodel ModelWolvenArmor model_wolven_armor models/mojmap-1.21.x/ModelWolvenArmor.java src/main/java/net/redboltmedia/witchercraft/client/model/ModelWolvenArmor.java net.redboltmedia.witchercraft.client.model
const fs = require('fs');
const [, , IN, CLS, LAYER, STORED, SRC, PKG] = process.argv;
const m = JSON.parse(fs.readFileSync(IN, 'utf8'));
const byId = Object.fromEntries(m.elements.map(e => [e.uuid, e]));
const groups = m.outliner.map(o => ({ ...m.groups.find(g => g.uuid === o.uuid), children: o.children }));

const f = n => { let s = (Math.round(n * 100000) / 100000).toString(); if (!s.includes('.') && !s.includes('e')) s += '.0'; return (s === '-0.0' ? '0.0' : s) + 'F'; };
const rad = d => d * Math.PI / 180;

function box(e, px, py, pz) {
  const [fx, fy, fz] = e.from, [tx, ty, tz] = e.to;
  const w = tx - fx, h = ty - fy, d = tz - fz;
  const mir = !!e.mirror_uv;
  const [u, v] = e.uv_offset || [0, 0];
  return `.texOffs(${u}, ${v})${mir ? '.mirror()' : ''}.addBox(${f(px - tx)}, ${f(py - ty)}, ${f(fz - pz)}, ${f(w)}, ${f(h)}, ${f(d)}, new CubeDeformation(${f(e.inflate || 0)}))${mir ? '.mirror(false)' : ''}`;
}

const lines = [];
const fields = [];
let rc = 0;
for (const g of groups) {
  const [ox, oy, oz] = g.origin;
  fields.push(g.name);
  const cubes = g.children.map(id => byId[id]);
  const plain = cubes.filter(e => !(e.rotation && e.rotation.some(r => r !== 0)));
  const rotated = cubes.filter(e => e.rotation && e.rotation.some(r => r !== 0));
  const list = plain.length ? 'CubeListBuilder.create()' + plain.map(e => box(e, ox, oy, oz)).join('\n\t\t') : 'CubeListBuilder.create()';
  lines.push(`\t\tPartDefinition ${g.name} = partdefinition.addOrReplaceChild("${g.name}", ${list}, PartPose.offset(${f(-ox)}, ${f(24 - oy)}, ${f(oz)}));`);
  for (const e of rotated) {
    const [rx, ry, rz] = e.origin;
    const [ax, ay, az] = e.rotation;
    rc++;
    lines.push(`\t\t${g.name}.addOrReplaceChild("${e.name}_r${rc}", CubeListBuilder.create()${box(e, rx, ry, rz)}, PartPose.offsetAndRotation(${f(ox - rx)}, ${f(oy - ry)}, ${f(rz - oz)}, ${f(-rad(ax))}, ${f(-rad(ay))}, ${f(rad(az))}));`);
  }
  lines.push('');
}

const tw = m.resolution.width, th = m.resolution.height;
const body = (withSuper) => `public class ${CLS} extends EntityModel<LivingEntityRenderState> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("witchercraft", "${LAYER}"), "main");
${fields.map(n => `	public final ModelPart ${n};`).join('\n')}

	public ${CLS}(ModelPart root) {
${withSuper ? '\t\tsuper(root);\n' : ''}${fields.map(n => `		this.${n} = root.getChild("${n}");`).join('\n')}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
${lines.join('\n')}
		return LayerDefinition.create(meshdefinition, ${tw}, ${th});
	}

	@Override public void setupAnim(LivingEntityRenderState state) {}
}
`;
const imports = `package ${PKG};

import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.EntityModel;

`;
fs.writeFileSync(STORED, body(false));
fs.writeFileSync(SRC, imports + body(true));
console.log('parts', fields.join(','), 'rotated', rc);

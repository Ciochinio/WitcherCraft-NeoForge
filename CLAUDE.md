# CLAUDE.md

<!-- TODO: fill in project overview, feature list, conventions, etc. -->

## Project

WitcherCraft — a NeoForge Minecraft mod built with **MCreator**, targeting generator `neoforge-26.1.2`.

## Rules

- **Don't use MDASHES "—", just standard hyphens "-".
- **Never use the section-sign symbol (U+00A7).**
- **Propose before implementing.** For anything with a design/architecture
  choice in it (new systems, where state lives, data-model shape), present
  the approach in text and wait for explicit confirmation before writing
  code — design and balance decisions aren't Claude's to make unilaterally.
  Small, fully-specified follow-ups to already-approved work can be built
  directly, without a fresh proposal round.
- **Discussion is the default; it is not a runway to code.** Answering a
  question, sketching an approach, weighing options, or hearing approval of
  *one* idea is never itself a green light to start building. A go-ahead is
  explicit and in-the-moment ("do it", "build it", "go ahead"). Unsure
  whether one was given? Then it wasn't — ask. The cost of getting this
  wrong is asymmetric: starting before the design is settled produces
  half-finished work that has to be torn out, which is worse than waiting.
- **Argue the point when it's worth arguing.** State disagreements, risks, and
  better alternatives plainly and up front — don't soften them, defer them, or
  agree by default. Skip only what's genuinely obvious or already settled here.
  A partner that only validates is worthless; the value is the pushback.
- **Never `git commit` (or push).** Commits happen manually, only when the
  state is judged ready - finished, verified work still stays uncommitted in
  the working tree. Summarizing what's uncommitted at wrap-up is welcome;
  running the commit is not, unless explicitly asked in the moment (a past
  "you can commit" does not carry over to later work).
- **Name every procedure you edit, and say where it lives in MCreator.** After
  creating or changing any MCreator procedure (or other element), list the
  affected element names explicitly in that same response - a short "procedures
  touched: X, Y, Z" line - so nothing gets lost track of when the workspace is
  next opened in MCreator. Format each name as a clickable markdown link to its
  `elements/<Name>.mod.json` (or the generated Java file) so it can be opened
  directly. For EACH listed element, also state its location in the MCreator
  element browser - the `"path"` from its `mod_elements` entry in
  `witchercraft.mcreator` (e.g. `~/Signs/Quen`), or "Variables" for a
  player/global variable - so it can be found in the GUI. Note whether it's new
  or edited.
- **Track changes per commit and keep the GDD current.**
  `GAME_DESIGN_DOCUMENT.md` is the living design doc and must not drift from the
  build. Whenever work changes gameplay, content, or balance (a new or edited
  sign, potion, decoction, oil, bomb, perk, armor effect, formula, or tuning
  value), update the affected GDD section in the same round of work. When a
  commit is prepared, its message should state what changed, and the GDD edits
  describing those changes belong in that same commit - so each commit carries
  both the change and its documentation.

## Creating and editing MCreator procedures

This project is edited in the **MCreator GUI**, not by hand-writing Java. An MCreator "element" (procedure, item, mob, GUI, etc.) is defined in three places that must stay in sync:

1. **`elements/<Name>.mod.json`** — the element definition. For a `procedure`, the logic lives as Blockly XML in `definition.procedurexml` — this is the exact format the MCreator block editor reads and writes, so a correctly written file opens as normal drag-and-drop blocks, fully editable in the GUI.
2. **`witchercraft.mcreator`** — the master registry (`mod_elements` array). Every element needs an entry here (`name`, `type`, `registry_name`, `metadata.dependencies`, `metadata.files`). Omit `"path"` to place the element at the root of the element browser; set it (e.g. `"~/Signs"`) to file it under a folder.
3. **Generated Java** under `src/main/java/net/redboltmedia/witchercraft/procedures/` — MCreator regenerates this from the element definition automatically. You can hand-write a first-pass version to keep Gradle buildable immediately, but treat it as disposable — MCreator will overwrite it with its own generated code the next time it opens/saves the workspace, and that's expected, not a bug.

**MCreator holds `witchercraft.mcreator` in memory and rewrites the whole file on save/close.** If it's running while you edit that one file, your registry addition can get silently lost the next time it saves — `elements/*.mod.json` and the Java files are not affected by this, only `witchercraft.mcreator` itself. So:
- Before editing, check if MCreator is running (e.g. a `mcreator`/`MCreator` process, platform-appropriate check) — don't ask the user first, just check.
- If it's **not** running: edit all three files directly as normal.
- If it **is** running: still write `elements/<Name>.mod.json` and the generated Java directly (safe either way). For the `witchercraft.mcreator` registry entry specifically, write the snippet to `.claude/pending-mcreator-registry/<Name>.json` instead of editing the live file, and tell the user in that same response that the registry entry is staged and will be applied automatically once MCreator is closed — don't make them ask again.
- At the start of any later procedure-editing task, check `.claude/pending-mcreator-registry/` for leftover staged entries; if MCreator is now closed, merge them into `witchercraft.mcreator` first (and delete the staged file) before doing new work.

**Don't invent Blockly block types/fields from memory** — get them from real sources, in this priority order:
- An existing, working element in `elements/*.mod.json` that does something similar (best: proven-valid in this exact project).
- The locally installed MCreator plugin files, which contain the ground-truth block definitions and code-generation templates for this exact generator version (`neoforge-26.1.2`). Installed location is machine-dependent (this repo is used on both Windows and macOS) — locate it rather than assuming a fixed path:
  - Windows: typically `C:\Program Files\Pylo\MCreator\plugins\` (confirmed on the Windows dev machine); otherwise search e.g. `Get-ChildItem -Recurse -Filter mcreator-core.zip C:\ -ErrorAction SilentlyContinue`.
  - macOS: typically under `/Applications/MCreator.app/Contents/...`; otherwise search e.g. `mdfind -name mcreator-core.zip` or `find /Applications ~/Library -iname 'mcreator-core.zip' 2>/dev/null`.
  - Look for `plugins/mcreator-core.zip` (block schemas, under `procedures/*.json`) and `plugins/generator-26.1.x.zip` (exact generated Java per block, under `neoforge-26.1.2/procedures/*.java.ftl`).
  - Unzip and `grep`/read these directly rather than guessing; a wrong block `type` string or field name won't crash MCreator but will show up as a broken/red block when opened.
- If MCreator isn't installed on the current machine, fall back to web search for the block definition, and treat the result as unverified until confirmed by opening the project in MCreator.

Match the project's existing conventions: `_fv: 89` (format version — check `grep -o "\"_fv\": [0-9]*" elements/*.mod.json | sort | uniq -c` if this ever changes), and reuse variable/dependency naming patterns already used by similar elements (e.g. `entity`/`sourceentity`/`world` for on-hit procedures).

After editing, validate both JSON files actually parse (`ConvertFrom-Json` in PowerShell or similar) before telling the user it's ready — a syntax slip here fails silently until MCreator opens the file.

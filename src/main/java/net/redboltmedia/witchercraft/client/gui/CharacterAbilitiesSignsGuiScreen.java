package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.CharacterAbilitiesSignsGuiMenu;
import net.redboltmedia.witchercraft.procedures.*;
import net.redboltmedia.witchercraft.network.CharacterAbilitiesSignsGuiButtonMessage;
import net.redboltmedia.witchercraft.init.WitchercraftModScreens;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;

public class CharacterAbilitiesSignsGuiScreen extends AbstractContainerScreen<CharacterAbilitiesSignsGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private Button button_back;
	private Button button_general;
	private Button button_combat;
	private Button button_alchemy;
	private Button button_signs;
	private ImageButton imagebutton_farreachingaard;
	private ImageButton imagebutton_aardintensity;
	private ImageButton imagebutton_shockwave;
	private ImageButton imagebutton_firestream;
	private ImageButton imagebutton_igniintensity;
	private ImageButton imagebutton_pyromaniac;
	private ImageButton imagebutton_sustainedglyphs;
	private ImageButton imagebutton_yrdenintensity;
	private ImageButton imagebutton_magictrap;
	private ImageButton imagebutton_explodingshield;
	private ImageButton imagebutton_quenintensity;
	private ImageButton imagebutton_quendischarge;
	private ImageButton imagebutton_delusion;
	private ImageButton imagebutton_axiiintensity;
	private ImageButton imagebutton_domination;
	private static final ResourceLocation BACKGROUND = ResourceLocation.parse("witchercraft:textures/screens/character_abilities_signs_gui.png");
	private static final ResourceLocation IMAGE_0 = ResourceLocation.parse("witchercraft:textures/screens/farreachingaardbought.png");
	private static final ResourceLocation IMAGE_1 = ResourceLocation.parse("witchercraft:textures/screens/aardintensitybought.png");
	private static final ResourceLocation IMAGE_2 = ResourceLocation.parse("witchercraft:textures/screens/shockwavebought.png");
	private static final ResourceLocation IMAGE_3 = ResourceLocation.parse("witchercraft:textures/screens/firestreambought.png");
	private static final ResourceLocation IMAGE_4 = ResourceLocation.parse("witchercraft:textures/screens/ingniintensitybought.png");
	private static final ResourceLocation IMAGE_5 = ResourceLocation.parse("witchercraft:textures/screens/pyromaniacbought.png");
	private static final ResourceLocation IMAGE_6 = ResourceLocation.parse("witchercraft:textures/screens/sustainedglyphsbought.png");
	private static final ResourceLocation IMAGE_7 = ResourceLocation.parse("witchercraft:textures/screens/yrdenintensitybought.png");
	private static final ResourceLocation IMAGE_8 = ResourceLocation.parse("witchercraft:textures/screens/magictrapbought.png");
	private static final ResourceLocation IMAGE_9 = ResourceLocation.parse("witchercraft:textures/screens/explodingshildbought.png");
	private static final ResourceLocation IMAGE_10 = ResourceLocation.parse("witchercraft:textures/screens/quenintensitybought.png");
	private static final ResourceLocation IMAGE_11 = ResourceLocation.parse("witchercraft:textures/screens/quendischargebought.png");
	private static final ResourceLocation IMAGE_12 = ResourceLocation.parse("witchercraft:textures/screens/delusionbought.png");
	private static final ResourceLocation IMAGE_13 = ResourceLocation.parse("witchercraft:textures/screens/axiiintensitybought.png");
	private static final ResourceLocation IMAGE_14 = ResourceLocation.parse("witchercraft:textures/screens/dominationbought.png");
	private static final ResourceLocation IMAGE_15 = ResourceLocation.parse("witchercraft:textures/screens/skillpoint.png");

	public CharacterAbilitiesSignsGuiScreen(CharacterAbilitiesSignsGuiMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 190;
		this.imageHeight = 166;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		menuStateUpdateActive = false;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		boolean customTooltipShown = false;
		if (mouseX > leftPos + 8 && mouseX < leftPos + 32 && mouseY > topPos + 12 && mouseY < topPos + 36) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_knocks_back_every_mob_in_diamet"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 9 && mouseX < leftPos + 33 && mouseY > topPos + 74 && mouseY < topPos + 98) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_increases_knockback"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 10 && mouseX < leftPos + 34 && mouseY > topPos + 128 && mouseY < topPos + 152) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_aard_deals_x_damage"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 43 && mouseX < leftPos + 67 && mouseY > topPos + 10 && mouseY < topPos + 34) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_increase_diameter_of_effect"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 42 && mouseX < leftPos + 66 && mouseY > topPos + 75 && mouseY < topPos + 99) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_increases_burning_duration"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 43 && mouseX < leftPos + 67 && mouseY > topPos + 128 && mouseY < topPos + 152) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_deals_additional_burnign_damage"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 79 && mouseX < leftPos + 103 && mouseY > topPos + 10 && mouseY < topPos + 34) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_increases_sign_duration_by_5_sec"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 79 && mouseX < leftPos + 103 && mouseY > topPos + 76 && mouseY < topPos + 100) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_increases_slow"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 79 && mouseX < leftPos + 103 && mouseY > topPos + 127 && mouseY < topPos + 151) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_increases_trap_radius_by_1_block"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 115 && mouseX < leftPos + 139 && mouseY > topPos + 10 && mouseY < topPos + 34) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_when_end_deal_damage_to_everyon"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 115 && mouseX < leftPos + 139 && mouseY > topPos + 75 && mouseY < topPos + 99) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_increases_max_absorbion_by_x"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 116 && mouseX < leftPos + 140 && mouseY > topPos + 127 && mouseY < topPos + 151) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_deals_absorbet_amount_of_dmg_to"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 156 && mouseX < leftPos + 180 && mouseY > topPos + 10 && mouseY < topPos + 34) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_gives_hero_of_the_village_effec"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 154 && mouseX < leftPos + 178 && mouseY > topPos + 76 && mouseY < topPos + 100) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_increases_sign_duration"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 154 && mouseX < leftPos + 178 && mouseY > topPos + 126 && mouseY < topPos + 150) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_signs_gui.tooltip_increases_dmg_dealt_to_mob"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (!customTooltipShown)
			this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_0, this.leftPos + 4, this.topPos + 7, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_1, this.leftPos + 4, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_2, this.leftPos + 4, this.topPos + 124, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_3, this.leftPos + 40, this.topPos + 7, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_4, this.leftPos + 40, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_5, this.leftPos + 40, this.topPos + 124, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_6, this.leftPos + 76, this.topPos + 7, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_7, this.leftPos + 76, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_8, this.leftPos + 76, this.topPos + 124, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_9, this.leftPos + 112, this.topPos + 7, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_10, this.leftPos + 112, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_11, this.leftPos + 112, this.topPos + 124, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_12, this.leftPos + 148, this.topPos + 7, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_13, this.leftPos + 148, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_14, this.leftPos + 148, this.topPos + 124, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_15, this.leftPos + 202, this.topPos + 7, 0, 0, 50, 25, 50, 25);
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, CharacterAbilitesSkillPointsAvailableProcedure.execute(entity), 193, 25, -12829636, false);
		guiGraphics.drawString(this.font, CharacterAbilitiesSignsGuiSkillPointsUsedProcedure.execute(entity), 193, 34, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		button_back = Button.builder(Component.translatable("gui.witchercraft.character_abilities_signs_gui.button_back"), e -> {
			int x = CharacterAbilitiesSignsGuiScreen.this.x;
			int y = CharacterAbilitiesSignsGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(0, x, y, z));
				CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 256, this.topPos + 169, 46, 20).build();
		this.addRenderableWidget(button_back);
		button_general = Button.builder(Component.translatable("gui.witchercraft.character_abilities_signs_gui.button_general"), e -> {
			int x = CharacterAbilitiesSignsGuiScreen.this.x;
			int y = CharacterAbilitiesSignsGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(1, x, y, z));
				CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		}).bounds(this.leftPos + -68, this.topPos + 7, 61, 20).build();
		this.addRenderableWidget(button_general);
		button_combat = Button.builder(Component.translatable("gui.witchercraft.character_abilities_signs_gui.button_combat"), e -> {
			int x = CharacterAbilitiesSignsGuiScreen.this.x;
			int y = CharacterAbilitiesSignsGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(2, x, y, z));
				CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 2, x, y, z);
			}
		}).bounds(this.leftPos + -68, this.topPos + 34, 61, 20).build();
		this.addRenderableWidget(button_combat);
		button_alchemy = Button.builder(Component.translatable("gui.witchercraft.character_abilities_signs_gui.button_alchemy"), e -> {
			int x = CharacterAbilitiesSignsGuiScreen.this.x;
			int y = CharacterAbilitiesSignsGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(3, x, y, z));
				CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 3, x, y, z);
			}
		}).bounds(this.leftPos + -68, this.topPos + 61, 61, 20).build();
		this.addRenderableWidget(button_alchemy);
		button_signs = Button.builder(Component.translatable("gui.witchercraft.character_abilities_signs_gui.button_signs"), e -> {
			int x = CharacterAbilitiesSignsGuiScreen.this.x;
			int y = CharacterAbilitiesSignsGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(4, x, y, z));
				CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 4, x, y, z);
			}
		}).bounds(this.leftPos + -68, this.topPos + 88, 61, 20).build();
		this.addRenderableWidget(button_signs);
		imagebutton_farreachingaard = new ImageButton(this.leftPos + 4, this.topPos + 7, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/farreachingaard.png"), ResourceLocation.parse("witchercraft:textures/screens/farreachingaard.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (FarReachingAardShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(5, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 5, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (FarReachingAardShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_farreachingaard);
		imagebutton_aardintensity = new ImageButton(this.leftPos + 4, this.topPos + 70, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/aardintensity.png"), ResourceLocation.parse("witchercraft:textures/screens/aardintensity.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (AardIntensityShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(6, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 6, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (AardIntensityShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_aardintensity);
		imagebutton_shockwave = new ImageButton(this.leftPos + 4, this.topPos + 124, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/shockwave.png"), ResourceLocation.parse("witchercraft:textures/screens/shockwave.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (ShockWaveShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(7, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 7, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (ShockWaveShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_shockwave);
		imagebutton_firestream = new ImageButton(this.leftPos + 40, this.topPos + 7, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/firestream.png"), ResourceLocation.parse("witchercraft:textures/screens/firestream.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (FirestreamShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(8, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 8, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (FirestreamShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_firestream);
		imagebutton_igniintensity = new ImageButton(this.leftPos + 40, this.topPos + 70, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/igniintensity.png"), ResourceLocation.parse("witchercraft:textures/screens/igniintensity.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (IgniIntensityShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(9, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 9, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (IgniIntensityShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_igniintensity);
		imagebutton_pyromaniac = new ImageButton(this.leftPos + 40, this.topPos + 124, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/pyromaniac.png"), ResourceLocation.parse("witchercraft:textures/screens/pyromaniac.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (PyromaniacShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(10, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 10, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (PyromaniacShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_pyromaniac);
		imagebutton_sustainedglyphs = new ImageButton(this.leftPos + 76, this.topPos + 7, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/sustainedglyphs.png"), ResourceLocation.parse("witchercraft:textures/screens/sustainedglyphs.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (SustainedGlyphsShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(11, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 11, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (SustainedGlyphsShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_sustainedglyphs);
		imagebutton_yrdenintensity = new ImageButton(this.leftPos + 76, this.topPos + 70, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/yrdenintensity.png"), ResourceLocation.parse("witchercraft:textures/screens/yrdenintensity.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (YrdenIntensityShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(12, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 12, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (YrdenIntensityShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_yrdenintensity);
		imagebutton_magictrap = new ImageButton(this.leftPos + 76, this.topPos + 124, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/magictrap.png"), ResourceLocation.parse("witchercraft:textures/screens/magictrap.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (MagicTrapShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(13, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 13, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (MagicTrapShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_magictrap);
		imagebutton_explodingshield = new ImageButton(this.leftPos + 112, this.topPos + 7, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/exploadingshield.png"), ResourceLocation.parse("witchercraft:textures/screens/exploadingshield.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (ExploadingShildshowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(14, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 14, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (ExploadingShildshowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_explodingshield);
		imagebutton_quenintensity = new ImageButton(this.leftPos + 112, this.topPos + 70, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/quenintensity.png"), ResourceLocation.parse("witchercraft:textures/screens/quenintensity.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (QuenIntensityShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(15, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 15, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (QuenIntensityShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_quenintensity);
		imagebutton_quendischarge = new ImageButton(this.leftPos + 112, this.topPos + 124, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/quendischarge.png"), ResourceLocation.parse("witchercraft:textures/screens/quendischarge.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (QuenDiscahrgeShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(16, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 16, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (QuenDiscahrgeShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_quendischarge);
		imagebutton_delusion = new ImageButton(this.leftPos + 148, this.topPos + 7, 32, 32, new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/delusion.png"), ResourceLocation.parse("witchercraft:textures/screens/delusion.png")),
				e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (DelusionShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(17, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 17, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (DelusionShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_delusion);
		imagebutton_axiiintensity = new ImageButton(this.leftPos + 148, this.topPos + 70, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/axiiintensity.png"), ResourceLocation.parse("witchercraft:textures/screens/axiiintensity.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (AxiiIntensityShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(18, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 18, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (AxiiIntensityShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_axiiintensity);
		imagebutton_domination = new ImageButton(this.leftPos + 148, this.topPos + 124, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/domination.png"), ResourceLocation.parse("witchercraft:textures/screens/domination.png")), e -> {
					int x = CharacterAbilitiesSignsGuiScreen.this.x;
					int y = CharacterAbilitiesSignsGuiScreen.this.y;
					if (DominationShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesSignsGuiButtonMessage(19, x, y, z));
						CharacterAbilitiesSignsGuiButtonMessage.handleButtonAction(entity, 19, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharacterAbilitiesSignsGuiScreen.this.x;
				int y = CharacterAbilitiesSignsGuiScreen.this.y;
				if (DominationShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_domination);
	}
}
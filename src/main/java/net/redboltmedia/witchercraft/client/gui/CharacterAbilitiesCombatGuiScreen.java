package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.CharacterAbilitiesCombatGuiMenu;
import net.redboltmedia.witchercraft.procedures.*;
import net.redboltmedia.witchercraft.network.CharacterAbilitiesCombatGuiButtonMessage;
import net.redboltmedia.witchercraft.init.WitchercraftModScreens;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mojang.blaze3d.platform.InputConstants;

public class CharacterAbilitiesCombatGuiScreen extends AbstractContainerScreen<CharacterAbilitiesCombatGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private Button button_back;
	private Button button_general;
	private Button button_combat;
	private Button button_alchemy;
	private Button button_signs;
	private ImageButton imagebutton_muslememory;
	private ImageButton imagebutton_preciseblows;
	private ImageButton imagebutton_cripplingstrikes;
	private ImageButton imagebutton_strengthtraining;
	private ImageButton imagebutton_crushingblows;
	private ImageButton imagebutton_sunderarmor;
	private ImageButton imagebutton_fleetfooted;
	private ImageButton imagebutton_defense;
	private ImageButton imagebutton_deadlyprecision;
	private ImageButton imagebutton_coldblood;
	private ImageButton imagebutton_anatomicalknowledge;
	private ImageButton imagebutton_cripplingshot;
	private ImageButton imagebutton_floodofanger;
	private ImageButton imagebutton_razorfocus;
	private ImageButton imagebutton_undying;
	private static final Identifier BACKGROUND = Identifier.parse("witchercraft:textures/screens/character_abilities_combat_gui.png");
	private static final Identifier IMAGE_0 = Identifier.parse("witchercraft:textures/screens/muslemomorybought.png");
	private static final Identifier IMAGE_1 = Identifier.parse("witchercraft:textures/screens/strengtraingbought.png");
	private static final Identifier IMAGE_2 = Identifier.parse("witchercraft:textures/screens/fleetfootedbougth.png");
	private static final Identifier IMAGE_3 = Identifier.parse("witchercraft:textures/screens/coldbloodbought.png");
	private static final Identifier IMAGE_4 = Identifier.parse("witchercraft:textures/screens/floodofangerbought.png");
	private static final Identifier IMAGE_5 = Identifier.parse("witchercraft:textures/screens/precoseblowsbought.png");
	private static final Identifier IMAGE_6 = Identifier.parse("witchercraft:textures/screens/crushingblowsbought.png");
	private static final Identifier IMAGE_7 = Identifier.parse("witchercraft:textures/screens/defensebought.png");
	private static final Identifier IMAGE_8 = Identifier.parse("witchercraft:textures/screens/anatomicalknowledgebought.png");
	private static final Identifier IMAGE_9 = Identifier.parse("witchercraft:textures/screens/razorfocusbought.png");
	private static final Identifier IMAGE_10 = Identifier.parse("witchercraft:textures/screens/cripplingstrikesbought.png");
	private static final Identifier IMAGE_11 = Identifier.parse("witchercraft:textures/screens/sunderarmorbought.png");
	private static final Identifier IMAGE_12 = Identifier.parse("witchercraft:textures/screens/deadlyprecisonbought.png");
	private static final Identifier IMAGE_13 = Identifier.parse("witchercraft:textures/screens/cripplingshotbought.png");
	private static final Identifier IMAGE_14 = Identifier.parse("witchercraft:textures/screens/undyingbought.png");
	private static final Identifier IMAGE_15 = Identifier.parse("witchercraft:textures/screens/skillpoint.png");

	public CharacterAbilitiesCombatGuiScreen(CharacterAbilitiesCombatGuiMenu container, Inventory inventory, Component text) {
		super(container, inventory, text, 190, 166);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		menuStateUpdateActive = false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (mouseX > leftPos + 7 && mouseX < leftPos + 31 && mouseY > topPos + 20 && mouseY < topPos + 44) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_add_3_to_dmg_dealt"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 7 && mouseX < leftPos + 31 && mouseY > topPos + 74 && mouseY < topPos + 98) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_increase_crit_rate_by_12_and_cr"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 6 && mouseX < leftPos + 30 && mouseY > topPos + 118 && mouseY < topPos + 142) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_apply_bleed_effect_on_hit"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 44 && mouseX < leftPos + 68 && mouseY > topPos + 19 && mouseY < topPos + 43) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_add_10_to_dmg_dealt"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 45 && mouseX < leftPos + 69 && mouseY > topPos + 76 && mouseY < topPos + 100) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_increase_crit_rate_by_8_and_cri"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 46 && mouseX < leftPos + 70 && mouseY > topPos + 120 && mouseY < topPos + 144) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_sunder_armoradd_20_to_dmg_dea"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 76 && mouseX < leftPos + 100 && mouseY > topPos + 16 && mouseY < topPos + 40) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_add_movement_speed"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 80 && mouseX < leftPos + 104 && mouseY > topPos + 75 && mouseY < topPos + 99) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_add_4_to_max_hp"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 81 && mouseX < leftPos + 105 && mouseY > topPos + 120 && mouseY < topPos + 144) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_add_1_chance_to_insta_kill"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 115 && mouseX < leftPos + 139 && mouseY > topPos + 19 && mouseY < topPos + 43) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_if_noone_nearby5_blocks_add"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 117 && mouseX < leftPos + 141 && mouseY > topPos + 77 && mouseY < topPos + 101) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_if_main_hand_weapon_ranged_add"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 115 && mouseX < leftPos + 139 && mouseY > topPos + 120 && mouseY < topPos + 144) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_if_main_hand_weapon_ranged_add1"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 153 && mouseX < leftPos + 177 && mouseY > topPos + 21 && mouseY < topPos + 45) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_if_someone_nearby5_blocks_add"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 154 && mouseX < leftPos + 178 && mouseY > topPos + 77 && mouseY < topPos + 101) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_add_10_chance_to_dodge_attack"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 153 && mouseX < leftPos + 177 && mouseY > topPos + 120 && mouseY < topPos + 144) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_combat_gui.tooltip_if_killed_restore_hp_to_50_onc"), mouseX, mouseY);
		}
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_0, this.leftPos + 4, this.topPos + 16, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_1, this.leftPos + 40, this.topPos + 16, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_2, this.leftPos + 76, this.topPos + 16, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_3, this.leftPos + 112, this.topPos + 16, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_4, this.leftPos + 148, this.topPos + 16, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_5, this.leftPos + 4, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_6, this.leftPos + 40, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_7, this.leftPos + 76, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_8, this.leftPos + 112, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_9, this.leftPos + 148, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_10, this.leftPos + 4, this.topPos + 115, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_11, this.leftPos + 40, this.topPos + 115, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_12, this.leftPos + 76, this.topPos + 115, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_13, this.leftPos + 112, this.topPos + 115, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_14, this.leftPos + 148, this.topPos + 115, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_15, this.leftPos + 193, this.topPos + 16, 0, 0, 50, 25, 50, 25);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = InputConstants.getKey(event).getValue();
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		guiGraphics.text(this.font, CharacterAbilitiesSkillPointsAvailableProcedure.execute(entity), 193, 25, -12829636, false);
		guiGraphics.text(this.font, CharacterAbilitiesCombatGuiSkillPointsUsedProcedure.execute(entity), 193, 34, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		button_back = Button.builder(Component.translatable("gui.witchercraft.character_abilities_combat_gui.button_back"), e -> {
			int x = CharacterAbilitiesCombatGuiScreen.this.x;
			int y = CharacterAbilitiesCombatGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(0, x, y, z));
				CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 256, this.topPos + 169, 46, 20).build();
		this.addRenderableWidget(button_back);
		button_general = Button.builder(Component.translatable("gui.witchercraft.character_abilities_combat_gui.button_general"), e -> {
			int x = CharacterAbilitiesCombatGuiScreen.this.x;
			int y = CharacterAbilitiesCombatGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(1, x, y, z));
				CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		}).bounds(this.leftPos + -68, this.topPos + 7, 61, 20).build();
		this.addRenderableWidget(button_general);
		button_combat = Button.builder(Component.translatable("gui.witchercraft.character_abilities_combat_gui.button_combat"), e -> {
			int x = CharacterAbilitiesCombatGuiScreen.this.x;
			int y = CharacterAbilitiesCombatGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(2, x, y, z));
				CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 2, x, y, z);
			}
		}).bounds(this.leftPos + -68, this.topPos + 34, 61, 20).build();
		this.addRenderableWidget(button_combat);
		button_alchemy = Button.builder(Component.translatable("gui.witchercraft.character_abilities_combat_gui.button_alchemy"), e -> {
			int x = CharacterAbilitiesCombatGuiScreen.this.x;
			int y = CharacterAbilitiesCombatGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(3, x, y, z));
				CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 3, x, y, z);
			}
		}).bounds(this.leftPos + -68, this.topPos + 61, 61, 20).build();
		this.addRenderableWidget(button_alchemy);
		button_signs = Button.builder(Component.translatable("gui.witchercraft.character_abilities_combat_gui.button_signs"), e -> {
			int x = CharacterAbilitiesCombatGuiScreen.this.x;
			int y = CharacterAbilitiesCombatGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(4, x, y, z));
				CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 4, x, y, z);
			}
		}).bounds(this.leftPos + -68, this.topPos + 88, 61, 20).build();
		this.addRenderableWidget(button_signs);
		imagebutton_muslememory = new ImageButton(this.leftPos + 4, this.topPos + 16, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/muslememory.png"), Identifier.parse("witchercraft:textures/screens/muslememory.png")),
				e -> {
					int x = CharacterAbilitiesCombatGuiScreen.this.x;
					int y = CharacterAbilitiesCombatGuiScreen.this.y;
					if (MuscleMemoryShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(5, x, y, z));
						CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 5, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_muslememory);
		imagebutton_preciseblows = new ImageButton(this.leftPos + 4, this.topPos + 70, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/preciseblows.png"), Identifier.parse("witchercraft:textures/screens/preciseblows.png")),
				e -> {
					int x = CharacterAbilitiesCombatGuiScreen.this.x;
					int y = CharacterAbilitiesCombatGuiScreen.this.y;
					if (PreciseBlowsShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(6, x, y, z));
						CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 6, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_preciseblows);
		imagebutton_cripplingstrikes = new ImageButton(this.leftPos + 4, this.topPos + 115, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/cripplingstrikes.png"), Identifier.parse("witchercraft:textures/screens/cripplingstrikes.png")), e -> {
					int x = CharacterAbilitiesCombatGuiScreen.this.x;
					int y = CharacterAbilitiesCombatGuiScreen.this.y;
					if (CripplingStrikesShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(7, x, y, z));
						CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 7, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_cripplingstrikes);
		imagebutton_strengthtraining = new ImageButton(this.leftPos + 40, this.topPos + 16, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/strengthtraining.png"), Identifier.parse("witchercraft:textures/screens/strengthtraining.png")), e -> {
					int x = CharacterAbilitiesCombatGuiScreen.this.x;
					int y = CharacterAbilitiesCombatGuiScreen.this.y;
					if (StrengthTrainingShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(8, x, y, z));
						CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 8, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_strengthtraining);
		imagebutton_crushingblows = new ImageButton(this.leftPos + 40, this.topPos + 70, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/crushingblows.png"), Identifier.parse("witchercraft:textures/screens/crushingblows.png")), e -> {
					int x = CharacterAbilitiesCombatGuiScreen.this.x;
					int y = CharacterAbilitiesCombatGuiScreen.this.y;
					if (CrushingBlowsShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(9, x, y, z));
						CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 9, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_crushingblows);
		imagebutton_sunderarmor = new ImageButton(this.leftPos + 40, this.topPos + 115, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/sunderarmor.png"), Identifier.parse("witchercraft:textures/screens/sunderarmor.png")),
				e -> {
					int x = CharacterAbilitiesCombatGuiScreen.this.x;
					int y = CharacterAbilitiesCombatGuiScreen.this.y;
					if (SunderArmorShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(10, x, y, z));
						CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 10, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_sunderarmor);
		imagebutton_fleetfooted = new ImageButton(this.leftPos + 76, this.topPos + 16, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/fleetfooted.png"), Identifier.parse("witchercraft:textures/screens/fleetfooted.png")),
				e -> {
					int x = CharacterAbilitiesCombatGuiScreen.this.x;
					int y = CharacterAbilitiesCombatGuiScreen.this.y;
					if (FleetFootedShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(11, x, y, z));
						CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 11, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_fleetfooted);
		imagebutton_defense = new ImageButton(this.leftPos + 76, this.topPos + 70, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/defense.png"), Identifier.parse("witchercraft:textures/screens/defense.png")), e -> {
			int x = CharacterAbilitiesCombatGuiScreen.this.x;
			int y = CharacterAbilitiesCombatGuiScreen.this.y;
			if (DefenceShowProcedure.execute(entity)) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(12, x, y, z));
				CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 12, x, y, z);
			}
		}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_defense);
		imagebutton_deadlyprecision = new ImageButton(this.leftPos + 76, this.topPos + 115, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/deadlyprecision.png"), Identifier.parse("witchercraft:textures/screens/deadlyprecision.png")), e -> {
					int x = CharacterAbilitiesCombatGuiScreen.this.x;
					int y = CharacterAbilitiesCombatGuiScreen.this.y;
					if (DeadlyPrecisionShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(13, x, y, z));
						CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 13, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_deadlyprecision);
		imagebutton_coldblood = new ImageButton(this.leftPos + 112, this.topPos + 16, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/coldblood.png"), Identifier.parse("witchercraft:textures/screens/coldblood.png")), e -> {
			int x = CharacterAbilitiesCombatGuiScreen.this.x;
			int y = CharacterAbilitiesCombatGuiScreen.this.y;
			if (ColdBloodShowProcedure.execute(entity)) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(14, x, y, z));
				CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 14, x, y, z);
			}
		}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_coldblood);
		imagebutton_anatomicalknowledge = new ImageButton(this.leftPos + 112, this.topPos + 70, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/anatomicalknowledge.png"), Identifier.parse("witchercraft:textures/screens/anatomicalknowledge.png")), e -> {
					int x = CharacterAbilitiesCombatGuiScreen.this.x;
					int y = CharacterAbilitiesCombatGuiScreen.this.y;
					if (AnatomicalKnowledgeShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(15, x, y, z));
						CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 15, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_anatomicalknowledge);
		imagebutton_cripplingshot = new ImageButton(this.leftPos + 112, this.topPos + 115, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/cripplingshot.png"), Identifier.parse("witchercraft:textures/screens/cripplingshot.png")), e -> {
					int x = CharacterAbilitiesCombatGuiScreen.this.x;
					int y = CharacterAbilitiesCombatGuiScreen.this.y;
					if (CripplingShotShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(16, x, y, z));
						CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 16, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_cripplingshot);
		imagebutton_floodofanger = new ImageButton(this.leftPos + 148, this.topPos + 16, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/floodofanger.png"), Identifier.parse("witchercraft:textures/screens/floodofanger.png")), e -> {
					int x = CharacterAbilitiesCombatGuiScreen.this.x;
					int y = CharacterAbilitiesCombatGuiScreen.this.y;
					if (FloodOfAngerShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(17, x, y, z));
						CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 17, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_floodofanger);
		imagebutton_razorfocus = new ImageButton(this.leftPos + 148, this.topPos + 70, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/razorfocus.png"), Identifier.parse("witchercraft:textures/screens/razorfocus.png")),
				e -> {
					int x = CharacterAbilitiesCombatGuiScreen.this.x;
					int y = CharacterAbilitiesCombatGuiScreen.this.y;
					if (RazorFocusShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(18, x, y, z));
						CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 18, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_razorfocus);
		imagebutton_undying = new ImageButton(this.leftPos + 148, this.topPos + 115, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/undying.png"), Identifier.parse("witchercraft:textures/screens/undying.png")), e -> {
			int x = CharacterAbilitiesCombatGuiScreen.this.x;
			int y = CharacterAbilitiesCombatGuiScreen.this.y;
			if (UndyingShowProcedure.execute(entity)) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesCombatGuiButtonMessage(19, x, y, z));
				CharacterAbilitiesCombatGuiButtonMessage.handleButtonAction(entity, 19, x, y, z);
			}
		}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_undying);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		this.imagebutton_muslememory.visible = MuscleMemoryShowProcedure.execute(entity);
		this.imagebutton_preciseblows.visible = PreciseBlowsShowProcedure.execute(entity);
		this.imagebutton_cripplingstrikes.visible = CripplingStrikesShowProcedure.execute(entity);
		this.imagebutton_strengthtraining.visible = StrengthTrainingShowProcedure.execute(entity);
		this.imagebutton_crushingblows.visible = CrushingBlowsShowProcedure.execute(entity);
		this.imagebutton_sunderarmor.visible = SunderArmorShowProcedure.execute(entity);
		this.imagebutton_fleetfooted.visible = FleetFootedShowProcedure.execute(entity);
		this.imagebutton_defense.visible = DefenceShowProcedure.execute(entity);
		this.imagebutton_deadlyprecision.visible = DeadlyPrecisionShowProcedure.execute(entity);
		this.imagebutton_coldblood.visible = ColdBloodShowProcedure.execute(entity);
		this.imagebutton_anatomicalknowledge.visible = AnatomicalKnowledgeShowProcedure.execute(entity);
		this.imagebutton_cripplingshot.visible = CripplingShotShowProcedure.execute(entity);
		this.imagebutton_floodofanger.visible = FloodOfAngerShowProcedure.execute(entity);
		this.imagebutton_razorfocus.visible = RazorFocusShowProcedure.execute(entity);
		this.imagebutton_undying.visible = UndyingShowProcedure.execute(entity);
	}
}
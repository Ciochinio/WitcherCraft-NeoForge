package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.CharacterAbilitiesAlchemyGuiMenu;
import net.redboltmedia.witchercraft.procedures.*;
import net.redboltmedia.witchercraft.network.CharacterAbilitiesAlchemyGuiButtonMessage;
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

public class CharacterAbilitiesAlchemyGuiScreen extends AbstractContainerScreen<CharacterAbilitiesAlchemyGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private Button button_back;
	private Button button_general;
	private Button button_combat;
	private Button button_alchemy;
	private Button button_signs;
	private ImageButton imagebutton_refreshment;
	private ImageButton imagebutton_delayedrecovery;
	private ImageButton imagebutton_sideeffects;
	private ImageButton imagebutton_poisonedblade;
	private ImageButton imagebutton_protectivecoating;
	private ImageButton imagebutton_hunterinstincts;
	private ImageButton imagebutton_pyrotechnics;
	private ImageButton imagebutton_efficency;
	private ImageButton imagebutton_clusterbombs;
	private static final Identifier BACKGROUND = Identifier.parse("witchercraft:textures/screens/character_abilities_alchemy_gui.png");
	private static final Identifier IMAGE_0 = Identifier.parse("witchercraft:textures/screens/refreshmentbought.png");
	private static final Identifier IMAGE_1 = Identifier.parse("witchercraft:textures/screens/delayedrecoverybought.png");
	private static final Identifier IMAGE_2 = Identifier.parse("witchercraft:textures/screens/sideeffectsbought.png");
	private static final Identifier IMAGE_3 = Identifier.parse("witchercraft:textures/screens/poisonedbladesbought.png");
	private static final Identifier IMAGE_4 = Identifier.parse("witchercraft:textures/screens/protectivecoatingbought.png");
	private static final Identifier IMAGE_5 = Identifier.parse("witchercraft:textures/screens/hunterinstinctsbought.png");
	private static final Identifier IMAGE_6 = Identifier.parse("witchercraft:textures/screens/pyrotechnicsbought.png");
	private static final Identifier IMAGE_7 = Identifier.parse("witchercraft:textures/screens/efficencybought.png");
	private static final Identifier IMAGE_8 = Identifier.parse("witchercraft:textures/screens/clusterbombsbought.png");
	private static final Identifier IMAGE_9 = Identifier.parse("witchercraft:textures/screens/skillpoint.png");

	public CharacterAbilitiesAlchemyGuiScreen(CharacterAbilitiesAlchemyGuiMenu container, Inventory inventory, Component text) {
		super(container, inventory, text, 176, 166);
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
		if (mouseX > leftPos + 6 && mouseX < leftPos + 30 && mouseY > topPos + 16 && mouseY < topPos + 40) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.tooltip_each_potion_heals_10_max_hp"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 6 && mouseX < leftPos + 30 && mouseY > topPos + 52 && mouseY < topPos + 76) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.tooltip_increases_potion_duration_time_b"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 6 && mouseX < leftPos + 30 && mouseY > topPos + 88 && mouseY < topPos + 112) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.tooltip_imbibing_potion_gives_gives_33"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 42 && mouseX < leftPos + 66 && mouseY > topPos + 16 && mouseY < topPos + 40) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.tooltip_oils_give_5_chance_to_poison_o"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 42 && mouseX < leftPos + 66 && mouseY > topPos + 52 && mouseY < topPos + 76) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.tooltip_reduces_damage_taken_from_mob_ty"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 42 && mouseX < leftPos + 66 && mouseY > topPos + 88 && mouseY < topPos + 112) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.tooltip_deals_additional_damage_to_mob"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 78 && mouseX < leftPos + 102 && mouseY > topPos + 16 && mouseY < topPos + 40) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.tooltip_bombs_deal_additional_damage"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 78 && mouseX < leftPos + 102 && mouseY > topPos + 52 && mouseY < topPos + 76) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.tooltip_bombs_have_chance_to_remain_in_i"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 78 && mouseX < leftPos + 102 && mouseY > topPos + 88 && mouseY < topPos + 112) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.tooltip_bombs_have_increased_area_of_eff"), mouseX, mouseY);
		}
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_0, this.leftPos + 6, this.topPos + 16, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_1, this.leftPos + 6, this.topPos + 52, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_2, this.leftPos + 6, this.topPos + 88, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_3, this.leftPos + 42, this.topPos + 16, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_4, this.leftPos + 42, this.topPos + 52, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_5, this.leftPos + 42, this.topPos + 88, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_6, this.leftPos + 78, this.topPos + 16, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_7, this.leftPos + 78, this.topPos + 52, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_8, this.leftPos + 78, this.topPos + 88, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_9, this.leftPos + 195, this.topPos + 7, 0, 0, 50, 25, 50, 25);
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
		guiGraphics.text(this.font, CharacterAbilitiesSkillPointsAvailableProcedure.execute(entity), 186, 7, -12829636, false);
		guiGraphics.text(this.font, CharacterAbilitiesAlchemyGuiSkillPointsUsedProcedure.execute(entity), 186, 16, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		button_back = Button.builder(Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.button_back"), e -> {
			int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
			int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(0, x, y, z));
				CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 249, this.topPos + 169, 46, 20).build();
		this.addRenderableWidget(button_back);
		button_general = Button.builder(Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.button_general"), e -> {
			int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
			int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(1, x, y, z));
				CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		}).bounds(this.leftPos + -66, this.topPos + -2, 61, 20).build();
		this.addRenderableWidget(button_general);
		button_combat = Button.builder(Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.button_combat"), e -> {
			int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
			int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(2, x, y, z));
				CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 2, x, y, z);
			}
		}).bounds(this.leftPos + -66, this.topPos + 25, 61, 20).build();
		this.addRenderableWidget(button_combat);
		button_alchemy = Button.builder(Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.button_alchemy"), e -> {
			int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
			int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(3, x, y, z));
				CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 3, x, y, z);
			}
		}).bounds(this.leftPos + -66, this.topPos + 52, 61, 20).build();
		this.addRenderableWidget(button_alchemy);
		button_signs = Button.builder(Component.translatable("gui.witchercraft.character_abilities_alchemy_gui.button_signs"), e -> {
			int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
			int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(4, x, y, z));
				CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 4, x, y, z);
			}
		}).bounds(this.leftPos + -66, this.topPos + 79, 61, 20).build();
		this.addRenderableWidget(button_signs);
		imagebutton_refreshment = new ImageButton(this.leftPos + 6, this.topPos + 16, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/refreshment.png"), Identifier.parse("witchercraft:textures/screens/refreshment.png")),
				e -> {
					int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
					int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
					if (RefreshmentShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(5, x, y, z));
						CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 5, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_refreshment);
		imagebutton_delayedrecovery = new ImageButton(this.leftPos + 6, this.topPos + 52, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/delayedrecovery.png"), Identifier.parse("witchercraft:textures/screens/delayedrecovery.png")), e -> {
					int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
					int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
					if (DelayedRecoveryShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(6, x, y, z));
						CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 6, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_delayedrecovery);
		imagebutton_sideeffects = new ImageButton(this.leftPos + 6, this.topPos + 88, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/sideeffects.png"), Identifier.parse("witchercraft:textures/screens/sideeffects.png")),
				e -> {
					int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
					int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
					if (SideEffectsShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(7, x, y, z));
						CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 7, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_sideeffects);
		imagebutton_poisonedblade = new ImageButton(this.leftPos + 42, this.topPos + 16, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/poisonedblade.png"), Identifier.parse("witchercraft:textures/screens/poisonedblade.png")), e -> {
					int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
					int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
					if (PoisonedBladesShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(8, x, y, z));
						CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 8, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_poisonedblade);
		imagebutton_protectivecoating = new ImageButton(this.leftPos + 42, this.topPos + 52, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/protectivecoating.png"), Identifier.parse("witchercraft:textures/screens/protectivecoating.png")), e -> {
					int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
					int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
					if (ProtectiveCoatingShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(9, x, y, z));
						CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 9, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_protectivecoating);
		imagebutton_hunterinstincts = new ImageButton(this.leftPos + 42, this.topPos + 88, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/hunterinstincts.png"), Identifier.parse("witchercraft:textures/screens/hunterinstincts.png")), e -> {
					int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
					int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
					if (HunterInstinctShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(10, x, y, z));
						CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 10, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_hunterinstincts);
		imagebutton_pyrotechnics = new ImageButton(this.leftPos + 78, this.topPos + 16, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/pyrotechnics.png"), Identifier.parse("witchercraft:textures/screens/pyrotechnics.png")),
				e -> {
					int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
					int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
					if (PyrotechnicsShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(11, x, y, z));
						CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 11, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_pyrotechnics);
		imagebutton_efficency = new ImageButton(this.leftPos + 78, this.topPos + 52, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/efficency.png"), Identifier.parse("witchercraft:textures/screens/efficency.png")), e -> {
			int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
			int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
			if (EfficiencyShowProcedure.execute(entity)) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(12, x, y, z));
				CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 12, x, y, z);
			}
		}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_efficency);
		imagebutton_clusterbombs = new ImageButton(this.leftPos + 78, this.topPos + 88, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/clusterbombs.png"), Identifier.parse("witchercraft:textures/screens/clusterbombs.png")),
				e -> {
					int x = CharacterAbilitiesAlchemyGuiScreen.this.x;
					int y = CharacterAbilitiesAlchemyGuiScreen.this.y;
					if (ClusterBombsShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesAlchemyGuiButtonMessage(13, x, y, z));
						CharacterAbilitiesAlchemyGuiButtonMessage.handleButtonAction(entity, 13, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_clusterbombs);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		this.imagebutton_refreshment.visible = RefreshmentShowProcedure.execute(entity);
		this.imagebutton_delayedrecovery.visible = DelayedRecoveryShowProcedure.execute(entity);
		this.imagebutton_sideeffects.visible = SideEffectsShowProcedure.execute(entity);
		this.imagebutton_poisonedblade.visible = PoisonedBladesShowProcedure.execute(entity);
		this.imagebutton_protectivecoating.visible = ProtectiveCoatingShowProcedure.execute(entity);
		this.imagebutton_hunterinstincts.visible = HunterInstinctShowProcedure.execute(entity);
		this.imagebutton_pyrotechnics.visible = PyrotechnicsShowProcedure.execute(entity);
		this.imagebutton_efficency.visible = EfficiencyShowProcedure.execute(entity);
		this.imagebutton_clusterbombs.visible = ClusterBombsShowProcedure.execute(entity);
	}
}
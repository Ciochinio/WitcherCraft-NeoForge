package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.CharacterAbilitiesGeneralGuiMenu;
import net.redboltmedia.witchercraft.procedures.*;
import net.redboltmedia.witchercraft.network.CharacterAbilitiesGeneralGuiButtonMessage;
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

import java.util.stream.Collectors;
import java.util.Arrays;

import com.mojang.blaze3d.platform.InputConstants;

public class CharacterAbilitiesGeneralGuiScreen extends AbstractContainerScreen<CharacterAbilitiesGeneralGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private Button button_back;
	private Button button_general;
	private Button button_alchemy;
	private Button button_combat;
	private Button button_signs;
	private ImageButton imagebutton_sunandstars;
	private ImageButton imagebutton_survivalinstict;
	private ImageButton imagebutton_gourment;
	private ImageButton imagebutton_catschooltechniques;
	private ImageButton imagebutton_griffinschool;
	private ImageButton imagebutton_bearschool;
	private static final Identifier BACKGROUND = Identifier.parse("witchercraft:textures/screens/character_abilities_general_gui.png");
	private static final Identifier IMAGE_0 = Identifier.parse("witchercraft:textures/screens/sunandstarsbought.png");
	private static final Identifier IMAGE_1 = Identifier.parse("witchercraft:textures/screens/survivalinstinctsbought.png");
	private static final Identifier IMAGE_2 = Identifier.parse("witchercraft:textures/screens/gourmentbought.png");
	private static final Identifier IMAGE_3 = Identifier.parse("witchercraft:textures/screens/catschooltechniquesbought.png");
	private static final Identifier IMAGE_4 = Identifier.parse("witchercraft:textures/screens/griffinschoolbought.png");
	private static final Identifier IMAGE_5 = Identifier.parse("witchercraft:textures/screens/bearschoolbought.png");
	private static final Identifier IMAGE_6 = Identifier.parse("witchercraft:textures/screens/skillpoint.png");

	public CharacterAbilitiesGeneralGuiScreen(CharacterAbilitiesGeneralGuiMenu container, Inventory inventory, Component text) {
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
		if (mouseX > leftPos + 69 && mouseX < leftPos + 93 && mouseY > topPos + 25 && mouseY < topPos + 49) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_general_gui.tooltip_increases_max_hp_by_15"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 123 && mouseX < leftPos + 147 && mouseY > topPos + 25 && mouseY < topPos + 49) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_general_gui.tooltip_1_to_stamina_regen"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 6 && mouseX < leftPos + 30 && mouseY > topPos + 70 && mouseY < topPos + 94) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_general_gui.tooltip_while_school_of_the_cat_effect"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 69 && mouseX < leftPos + 93 && mouseY > topPos + 70 && mouseY < topPos + 94) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_general_gui.tooltip_while_school_of_the_griffin_ef"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 123 && mouseX < leftPos + 147 && mouseY > topPos + 70 && mouseY < topPos + 94) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.character_abilities_general_gui.tooltip_while_school_of_the_bear_effec"), mouseX, mouseY);
		}
		if (mouseX > leftPos + 7 && mouseX < leftPos + 37 && mouseY > topPos + 25 && mouseY < topPos + 56) {
			String hoverText = SunAndStarsTooltipProcedure.execute();
			if (hoverText != null) {
				guiGraphics.setComponentTooltipForNextFrame(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
			}
		}
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_0, this.leftPos + 6, this.topPos + 25, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_1, this.leftPos + 69, this.topPos + 25, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_2, this.leftPos + 123, this.topPos + 25, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_3, this.leftPos + 6, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_4, this.leftPos + 69, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_5, this.leftPos + 123, this.topPos + 70, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_6, this.leftPos + 186, this.topPos + -2, 0, 0, 50, 25, 50, 25);
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
		guiGraphics.text(this.font, CharacterAbilitiesSkillPointsAvailableProcedure.execute(entity), 187, 5, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		button_back = Button.builder(Component.translatable("gui.witchercraft.character_abilities_general_gui.button_back"), e -> {
			int x = CharacterAbilitiesGeneralGuiScreen.this.x;
			int y = CharacterAbilitiesGeneralGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesGeneralGuiButtonMessage(0, x, y, z));
				CharacterAbilitiesGeneralGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 249, this.topPos + 169, 46, 20).build();
		this.addRenderableWidget(button_back);
		button_general = Button.builder(Component.translatable("gui.witchercraft.character_abilities_general_gui.button_general"), e -> {
			int x = CharacterAbilitiesGeneralGuiScreen.this.x;
			int y = CharacterAbilitiesGeneralGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesGeneralGuiButtonMessage(1, x, y, z));
				CharacterAbilitiesGeneralGuiButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		}).bounds(this.leftPos + -66, this.topPos + -2, 61, 20).build();
		this.addRenderableWidget(button_general);
		button_alchemy = Button.builder(Component.translatable("gui.witchercraft.character_abilities_general_gui.button_alchemy"), e -> {
			int x = CharacterAbilitiesGeneralGuiScreen.this.x;
			int y = CharacterAbilitiesGeneralGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesGeneralGuiButtonMessage(2, x, y, z));
				CharacterAbilitiesGeneralGuiButtonMessage.handleButtonAction(entity, 2, x, y, z);
			}
		}).bounds(this.leftPos + -66, this.topPos + 52, 61, 20).build();
		this.addRenderableWidget(button_alchemy);
		button_combat = Button.builder(Component.translatable("gui.witchercraft.character_abilities_general_gui.button_combat"), e -> {
			int x = CharacterAbilitiesGeneralGuiScreen.this.x;
			int y = CharacterAbilitiesGeneralGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesGeneralGuiButtonMessage(3, x, y, z));
				CharacterAbilitiesGeneralGuiButtonMessage.handleButtonAction(entity, 3, x, y, z);
			}
		}).bounds(this.leftPos + -66, this.topPos + 25, 61, 20).build();
		this.addRenderableWidget(button_combat);
		button_signs = Button.builder(Component.translatable("gui.witchercraft.character_abilities_general_gui.button_signs"), e -> {
			int x = CharacterAbilitiesGeneralGuiScreen.this.x;
			int y = CharacterAbilitiesGeneralGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesGeneralGuiButtonMessage(4, x, y, z));
				CharacterAbilitiesGeneralGuiButtonMessage.handleButtonAction(entity, 4, x, y, z);
			}
		}).bounds(this.leftPos + -66, this.topPos + 79, 61, 20).build();
		this.addRenderableWidget(button_signs);
		imagebutton_sunandstars = new ImageButton(this.leftPos + 6, this.topPos + 25, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/sunandstars.png"), Identifier.parse("witchercraft:textures/screens/sunandstars.png")),
				e -> {
					int x = CharacterAbilitiesGeneralGuiScreen.this.x;
					int y = CharacterAbilitiesGeneralGuiScreen.this.y;
					if (SunAndStarsShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesGeneralGuiButtonMessage(5, x, y, z));
						CharacterAbilitiesGeneralGuiButtonMessage.handleButtonAction(entity, 5, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_sunandstars);
		imagebutton_survivalinstict = new ImageButton(this.leftPos + 69, this.topPos + 25, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/survivalinstict.png"), Identifier.parse("witchercraft:textures/screens/survivalinstict.png")), e -> {
					int x = CharacterAbilitiesGeneralGuiScreen.this.x;
					int y = CharacterAbilitiesGeneralGuiScreen.this.y;
					if (SurvivalInstinctShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesGeneralGuiButtonMessage(6, x, y, z));
						CharacterAbilitiesGeneralGuiButtonMessage.handleButtonAction(entity, 6, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_survivalinstict);
		imagebutton_gourment = new ImageButton(this.leftPos + 123, this.topPos + 25, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/gourment.png"), Identifier.parse("witchercraft:textures/screens/gourment.png")), e -> {
			int x = CharacterAbilitiesGeneralGuiScreen.this.x;
			int y = CharacterAbilitiesGeneralGuiScreen.this.y;
			if (GourmetShowProcedure.execute(entity)) {
				ClientPacketDistributor.sendToServer(new CharacterAbilitiesGeneralGuiButtonMessage(7, x, y, z));
				CharacterAbilitiesGeneralGuiButtonMessage.handleButtonAction(entity, 7, x, y, z);
			}
		}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_gourment);
		imagebutton_catschooltechniques = new ImageButton(this.leftPos + 6, this.topPos + 70, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/catschool.png"), Identifier.parse("witchercraft:textures/screens/catschool.png")),
				e -> {
					int x = CharacterAbilitiesGeneralGuiScreen.this.x;
					int y = CharacterAbilitiesGeneralGuiScreen.this.y;
					if (CatSchoolShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesGeneralGuiButtonMessage(8, x, y, z));
						CharacterAbilitiesGeneralGuiButtonMessage.handleButtonAction(entity, 8, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_catschooltechniques);
		imagebutton_griffinschool = new ImageButton(this.leftPos + 69, this.topPos + 70, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/griffinschool.png"), Identifier.parse("witchercraft:textures/screens/griffinschool.png")), e -> {
					int x = CharacterAbilitiesGeneralGuiScreen.this.x;
					int y = CharacterAbilitiesGeneralGuiScreen.this.y;
					if (GriffinSchoolShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesGeneralGuiButtonMessage(9, x, y, z));
						CharacterAbilitiesGeneralGuiButtonMessage.handleButtonAction(entity, 9, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_griffinschool);
		imagebutton_bearschool = new ImageButton(this.leftPos + 123, this.topPos + 70, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/bearschool.png"), Identifier.parse("witchercraft:textures/screens/bearschool.png")),
				e -> {
					int x = CharacterAbilitiesGeneralGuiScreen.this.x;
					int y = CharacterAbilitiesGeneralGuiScreen.this.y;
					if (BearSchoolShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharacterAbilitiesGeneralGuiButtonMessage(10, x, y, z));
						CharacterAbilitiesGeneralGuiButtonMessage.handleButtonAction(entity, 10, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_bearschool);
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		this.imagebutton_sunandstars.visible = SunAndStarsShowProcedure.execute(entity);
		this.imagebutton_survivalinstict.visible = SurvivalInstinctShowProcedure.execute(entity);
		this.imagebutton_gourment.visible = GourmetShowProcedure.execute(entity);
		this.imagebutton_catschooltechniques.visible = CatSchoolShowProcedure.execute(entity);
		this.imagebutton_griffinschool.visible = GriffinSchoolShowProcedure.execute(entity);
		this.imagebutton_bearschool.visible = BearSchoolShowProcedure.execute(entity);
	}
}
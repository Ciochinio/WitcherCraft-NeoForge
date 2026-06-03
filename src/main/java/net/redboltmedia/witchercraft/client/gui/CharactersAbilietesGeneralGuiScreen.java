package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.CharactersAbilietesGeneralGuiMenu;
import net.redboltmedia.witchercraft.procedures.*;
import net.redboltmedia.witchercraft.network.CharactersAbilietesGeneralGuiButtonMessage;
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

import java.util.stream.Collectors;
import java.util.Arrays;

public class CharactersAbilietesGeneralGuiScreen extends AbstractContainerScreen<CharactersAbilietesGeneralGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
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
	private static final ResourceLocation BACKGROUND = ResourceLocation.parse("witchercraft:textures/screens/characters_abilietes_general_gui.png");
	private static final ResourceLocation IMAGE_0 = ResourceLocation.parse("witchercraft:textures/screens/sunandstarsbought.png");
	private static final ResourceLocation IMAGE_1 = ResourceLocation.parse("witchercraft:textures/screens/survivalinstinctsbought.png");
	private static final ResourceLocation IMAGE_2 = ResourceLocation.parse("witchercraft:textures/screens/gourmentbought.png");
	private static final ResourceLocation IMAGE_3 = ResourceLocation.parse("witchercraft:textures/screens/catschooltechniquesbought.png");
	private static final ResourceLocation IMAGE_4 = ResourceLocation.parse("witchercraft:textures/screens/griffinschoolbought.png");
	private static final ResourceLocation IMAGE_5 = ResourceLocation.parse("witchercraft:textures/screens/bearschoolbought.png");
	private static final ResourceLocation IMAGE_6 = ResourceLocation.parse("witchercraft:textures/screens/skillpoint.png");

	public CharactersAbilietesGeneralGuiScreen(CharactersAbilietesGeneralGuiMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
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
		if (mouseX > leftPos + 69 && mouseX < leftPos + 93 && mouseY > topPos + 25 && mouseY < topPos + 49) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.characters_abilietes_general_gui.tooltip_increases_max_hp_by_15"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 123 && mouseX < leftPos + 147 && mouseY > topPos + 25 && mouseY < topPos + 49) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.characters_abilietes_general_gui.tooltip_1_to_stamina_regen"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 6 && mouseX < leftPos + 30 && mouseY > topPos + 70 && mouseY < topPos + 94) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.characters_abilietes_general_gui.tooltip_while_school_of_the_cat_effect"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 69 && mouseX < leftPos + 93 && mouseY > topPos + 70 && mouseY < topPos + 94) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.characters_abilietes_general_gui.tooltip_while_school_of_the_griffin_ef"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 123 && mouseX < leftPos + 147 && mouseY > topPos + 70 && mouseY < topPos + 94) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.characters_abilietes_general_gui.tooltip_while_school_of_the_bear_effec"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (mouseX > leftPos + 7 && mouseX < leftPos + 37 && mouseY > topPos + 25 && mouseY < topPos + 56) {
			String hoverText = SunAndStarsTooltipProcedure.execute();
			if (hoverText != null) {
				guiGraphics.setComponentTooltipForNextFrame(font, Arrays.stream(hoverText.split("\n")).map(Component::literal).collect(Collectors.toList()), mouseX, mouseY);
			}
			customTooltipShown = true;
		}
		if (!customTooltipShown)
			this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
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
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(key, b, c);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(this.font, CharacterAbilitesSkillPointsAvailableProcedure.execute(entity), 187, 5, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		button_back = Button.builder(Component.translatable("gui.witchercraft.characters_abilietes_general_gui.button_back"), e -> {
			int x = CharactersAbilietesGeneralGuiScreen.this.x;
			int y = CharactersAbilietesGeneralGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharactersAbilietesGeneralGuiButtonMessage(0, x, y, z));
				CharactersAbilietesGeneralGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 249, this.topPos + 169, 46, 20).build();
		this.addRenderableWidget(button_back);
		button_general = Button.builder(Component.translatable("gui.witchercraft.characters_abilietes_general_gui.button_general"), e -> {
			int x = CharactersAbilietesGeneralGuiScreen.this.x;
			int y = CharactersAbilietesGeneralGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharactersAbilietesGeneralGuiButtonMessage(1, x, y, z));
				CharactersAbilietesGeneralGuiButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		}).bounds(this.leftPos + -66, this.topPos + -2, 61, 20).build();
		this.addRenderableWidget(button_general);
		button_alchemy = Button.builder(Component.translatable("gui.witchercraft.characters_abilietes_general_gui.button_alchemy"), e -> {
			int x = CharactersAbilietesGeneralGuiScreen.this.x;
			int y = CharactersAbilietesGeneralGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharactersAbilietesGeneralGuiButtonMessage(2, x, y, z));
				CharactersAbilietesGeneralGuiButtonMessage.handleButtonAction(entity, 2, x, y, z);
			}
		}).bounds(this.leftPos + -66, this.topPos + 52, 61, 20).build();
		this.addRenderableWidget(button_alchemy);
		button_combat = Button.builder(Component.translatable("gui.witchercraft.characters_abilietes_general_gui.button_combat"), e -> {
			int x = CharactersAbilietesGeneralGuiScreen.this.x;
			int y = CharactersAbilietesGeneralGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharactersAbilietesGeneralGuiButtonMessage(3, x, y, z));
				CharactersAbilietesGeneralGuiButtonMessage.handleButtonAction(entity, 3, x, y, z);
			}
		}).bounds(this.leftPos + -66, this.topPos + 25, 61, 20).build();
		this.addRenderableWidget(button_combat);
		button_signs = Button.builder(Component.translatable("gui.witchercraft.characters_abilietes_general_gui.button_signs"), e -> {
			int x = CharactersAbilietesGeneralGuiScreen.this.x;
			int y = CharactersAbilietesGeneralGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharactersAbilietesGeneralGuiButtonMessage(4, x, y, z));
				CharactersAbilietesGeneralGuiButtonMessage.handleButtonAction(entity, 4, x, y, z);
			}
		}).bounds(this.leftPos + -66, this.topPos + 79, 61, 20).build();
		this.addRenderableWidget(button_signs);
		imagebutton_sunandstars = new ImageButton(this.leftPos + 6, this.topPos + 25, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/sunandstars.png"), ResourceLocation.parse("witchercraft:textures/screens/sunandstars.png")), e -> {
					int x = CharactersAbilietesGeneralGuiScreen.this.x;
					int y = CharactersAbilietesGeneralGuiScreen.this.y;
					if (SunAndStarsShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharactersAbilietesGeneralGuiButtonMessage(5, x, y, z));
						CharactersAbilietesGeneralGuiButtonMessage.handleButtonAction(entity, 5, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharactersAbilietesGeneralGuiScreen.this.x;
				int y = CharactersAbilietesGeneralGuiScreen.this.y;
				if (SunAndStarsShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_sunandstars);
		imagebutton_survivalinstict = new ImageButton(this.leftPos + 69, this.topPos + 25, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/survivalinstict.png"), ResourceLocation.parse("witchercraft:textures/screens/survivalinstict.png")), e -> {
					int x = CharactersAbilietesGeneralGuiScreen.this.x;
					int y = CharactersAbilietesGeneralGuiScreen.this.y;
					if (SurvivalInstinctShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharactersAbilietesGeneralGuiButtonMessage(6, x, y, z));
						CharactersAbilietesGeneralGuiButtonMessage.handleButtonAction(entity, 6, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharactersAbilietesGeneralGuiScreen.this.x;
				int y = CharactersAbilietesGeneralGuiScreen.this.y;
				if (SurvivalInstinctShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_survivalinstict);
		imagebutton_gourment = new ImageButton(this.leftPos + 123, this.topPos + 25, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/gourment.png"), ResourceLocation.parse("witchercraft:textures/screens/gourment.png")), e -> {
					int x = CharactersAbilietesGeneralGuiScreen.this.x;
					int y = CharactersAbilietesGeneralGuiScreen.this.y;
					if (GourmetShowIconProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharactersAbilietesGeneralGuiButtonMessage(7, x, y, z));
						CharactersAbilietesGeneralGuiButtonMessage.handleButtonAction(entity, 7, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharactersAbilietesGeneralGuiScreen.this.x;
				int y = CharactersAbilietesGeneralGuiScreen.this.y;
				if (GourmetShowIconProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_gourment);
		imagebutton_catschooltechniques = new ImageButton(this.leftPos + 6, this.topPos + 70, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/catschool.png"), ResourceLocation.parse("witchercraft:textures/screens/catschool.png")), e -> {
					int x = CharactersAbilietesGeneralGuiScreen.this.x;
					int y = CharactersAbilietesGeneralGuiScreen.this.y;
					if (CatSchoolShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharactersAbilietesGeneralGuiButtonMessage(8, x, y, z));
						CharactersAbilietesGeneralGuiButtonMessage.handleButtonAction(entity, 8, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharactersAbilietesGeneralGuiScreen.this.x;
				int y = CharactersAbilietesGeneralGuiScreen.this.y;
				if (CatSchoolShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_catschooltechniques);
		imagebutton_griffinschool = new ImageButton(this.leftPos + 69, this.topPos + 70, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/griffinschool.png"), ResourceLocation.parse("witchercraft:textures/screens/griffinschool.png")), e -> {
					int x = CharactersAbilietesGeneralGuiScreen.this.x;
					int y = CharactersAbilietesGeneralGuiScreen.this.y;
					if (GriffinSchoolShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharactersAbilietesGeneralGuiButtonMessage(9, x, y, z));
						CharactersAbilietesGeneralGuiButtonMessage.handleButtonAction(entity, 9, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharactersAbilietesGeneralGuiScreen.this.x;
				int y = CharactersAbilietesGeneralGuiScreen.this.y;
				if (GriffinSchoolShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_griffinschool);
		imagebutton_bearschool = new ImageButton(this.leftPos + 123, this.topPos + 70, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/bearschool.png"), ResourceLocation.parse("witchercraft:textures/screens/bearschool.png")), e -> {
					int x = CharactersAbilietesGeneralGuiScreen.this.x;
					int y = CharactersAbilietesGeneralGuiScreen.this.y;
					if (BearSchoolShowProcedure.execute(entity)) {
						ClientPacketDistributor.sendToServer(new CharactersAbilietesGeneralGuiButtonMessage(10, x, y, z));
						CharactersAbilietesGeneralGuiButtonMessage.handleButtonAction(entity, 10, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				int x = CharactersAbilietesGeneralGuiScreen.this.x;
				int y = CharactersAbilietesGeneralGuiScreen.this.y;
				if (BearSchoolShowProcedure.execute(entity))
					guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_bearschool);
	}
}
package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.RotfiendGuiMenu;
import net.redboltmedia.witchercraft.network.RotfiendGuiButtonMessage;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mojang.blaze3d.platform.InputConstants;

public class RotfiendGuiScreen extends AbstractContainerScreen<RotfiendGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private ImageButton imagebutton_bookmark;
	private ImageButton imagebutton_arrowback;
	private ImageButton imagebutton_arrowforword;
	private static final Identifier IMAGE_0 = Identifier.parse("witchercraft:textures/screens/book.png");
	private static final Identifier IMAGE_1 = Identifier.parse("witchercraft:textures/screens/68747470733a2f2f726564626f6c746d656469612e6769746875622e696f2f73746f726167652f6d6f64732f7769746368657263726166742f726f746669656e6462672e706e67.png");
	private static final Identifier IMAGE_2 = Identifier.parse("witchercraft:textures/screens/bookmarkleft.png");
	private static final Identifier IMAGE_3 = Identifier.parse("witchercraft:textures/screens/drownerface.png");

	public RotfiendGuiScreen(RotfiendGuiMenu container, Inventory inventory, Component text) {
		super(container, inventory, text, 270, 166);
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
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_0, this.leftPos + -37, this.topPos + -38, 0, 0, 319, 222, 319, 222);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_1, this.leftPos + 152, this.topPos + 7, 0, 0, 91, 106, 91, 106);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_2, this.leftPos + -28, this.topPos + -11, 0, 0, 60, 23, 60, 23);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_3, this.leftPos + -15, this.topPos + -8, 0, 0, 16, 16, 16, 16);
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
		guiGraphics.text(this.font, Component.translatable("gui.witchercraft.rotfiend_gui.label_rotfiend"), 17, -11, -12829636, false);
		guiGraphics.text(this.font, Component.translatable("gui.witchercraft.rotfiend_gui.label_type_hostile"), 17, 7, -12829636, false);
		guiGraphics.text(this.font, Component.translatable("gui.witchercraft.rotfiend_gui.label_health_40"), 17, 34, -12829636, false);
		guiGraphics.text(this.font, Component.translatable("gui.witchercraft.rotfiend_gui.label_atack_damage_4"), 17, 61, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		imagebutton_bookmark = new ImageButton(this.leftPos + 196, this.topPos + -22, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/bookmark.png"), Identifier.parse("witchercraft:textures/screens/bookmark.png")), e -> {
			int x = RotfiendGuiScreen.this.x;
			int y = RotfiendGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new RotfiendGuiButtonMessage(0, x, y, z));
				RotfiendGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_bookmark);
		imagebutton_arrowback = new ImageButton(this.leftPos + 17, this.topPos + 124, 32, 32, new WidgetSprites(Identifier.parse("witchercraft:textures/screens/arrowback.png"), Identifier.parse("witchercraft:textures/screens/arrowback.png")), e -> {
			int x = RotfiendGuiScreen.this.x;
			int y = RotfiendGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new RotfiendGuiButtonMessage(1, x, y, z));
				RotfiendGuiButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_arrowback);
		imagebutton_arrowforword = new ImageButton(this.leftPos + 206, this.topPos + 124, 32, 32,
				new WidgetSprites(Identifier.parse("witchercraft:textures/screens/arrowforword.png"), Identifier.parse("witchercraft:textures/screens/arrowforword.png")), e -> {
					int x = RotfiendGuiScreen.this.x;
					int y = RotfiendGuiScreen.this.y;
					if (true) {
						ClientPacketDistributor.sendToServer(new RotfiendGuiButtonMessage(2, x, y, z));
						RotfiendGuiButtonMessage.handleButtonAction(entity, 2, x, y, z);
					}
				}) {
			@Override
			public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_arrowforword);
	}
}
package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.BestiaryMenuGuiMenu;
import net.redboltmedia.witchercraft.network.BestiaryMenuGuiButtonMessage;
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

public class BestiaryMenuGuiScreen extends AbstractContainerScreen<BestiaryMenuGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private Button button_back;
	private ImageButton imagebutton_drownerface;
	private static final ResourceLocation IMAGE_0 = ResourceLocation.parse("witchercraft:textures/screens/book.png");

	public BestiaryMenuGuiScreen(BestiaryMenuGuiMenu container, Inventory inventory, Component text) {
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
	public boolean isPauseScreen() {
		return true;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		boolean customTooltipShown = false;
		if (mouseX > leftPos + -39 && mouseX < leftPos + -6 && mouseY > topPos + -2 && mouseY < topPos + 30) {
			guiGraphics.setTooltipForNextFrame(font, Component.translatable("gui.witchercraft.bestiary_menu_gui.tooltip_necrophages"), mouseX, mouseY);
			customTooltipShown = true;
		}
		if (!customTooltipShown)
			this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_0, this.leftPos + -84, this.topPos + -38, 0, 0, 319, 222, 319, 222);
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
		guiGraphics.drawString(this.font, Component.translatable("gui.witchercraft.bestiary_menu_gui.label_bestiary"), -39, -20, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		button_back = Button.builder(Component.translatable("gui.witchercraft.bestiary_menu_gui.button_back"), e -> {
			int x = BestiaryMenuGuiScreen.this.x;
			int y = BestiaryMenuGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new BestiaryMenuGuiButtonMessage(0, x, y, z));
				BestiaryMenuGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 249, this.topPos + 169, 46, 20).build();
		this.addRenderableWidget(button_back);
		imagebutton_drownerface = new ImageButton(this.leftPos + -39, this.topPos + -2, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/drownerface.png"), ResourceLocation.parse("witchercraft:textures/screens/drownerface.png")), e -> {
					int x = BestiaryMenuGuiScreen.this.x;
					int y = BestiaryMenuGuiScreen.this.y;
					if (true) {
						ClientPacketDistributor.sendToServer(new BestiaryMenuGuiButtonMessage(1, x, y, z));
						BestiaryMenuGuiButtonMessage.handleButtonAction(entity, 1, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_drownerface);
	}
}
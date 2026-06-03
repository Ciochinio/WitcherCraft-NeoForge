package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.HigherVampireGuiMenu;
import net.redboltmedia.witchercraft.network.HigherVampireGuiButtonMessage;
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
import net.minecraft.client.gui.GuiGraphics;

public class HigherVampireGuiScreen extends AbstractContainerScreen<HigherVampireGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private ImageButton imagebutton_arrowforword;
	private ImageButton imagebutton_bookmark;
	private ImageButton imagebutton_bookmarkleft;
	private ImageButton imagebutton_drownerfacesmall;
	private ImageButton imagebutton_bookmarkerleftbought;
	private ImageButton imagebutton_arrowback;
	private static final ResourceLocation IMAGE_0 = ResourceLocation.parse("witchercraft:textures/screens/book.png");
	private static final ResourceLocation IMAGE_1 = ResourceLocation
			.parse("witchercraft:textures/screens/68747470733a2f2f726564626f6c746d656469612e6769746875622e696f2f73746f726167652f6d6f64732f7769746368657263726166742f64726f776e657262672e706e67.png");

	public HigherVampireGuiScreen(HigherVampireGuiMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 270;
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
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_0, this.leftPos + -37, this.topPos + -38, 0, 0, 319, 222, 319, 222);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_1, this.leftPos + 139, this.topPos + 7, 0, 0, 91, 106, 91, 106);
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
		guiGraphics.drawString(this.font, Component.translatable("gui.witchercraft.higher_vampire_gui.label_drowner"), 17, -11, -12829636, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.witchercraft.higher_vampire_gui.label_type_hostal"), 17, 7, -12829636, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.witchercraft.higher_vampire_gui.label_health_20"), 17, 34, -12829636, false);
		guiGraphics.drawString(this.font, Component.translatable("gui.witchercraft.higher_vampire_gui.label_atack_damage_5"), 17, 61, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		imagebutton_arrowforword = new ImageButton(this.leftPos + 206, this.topPos + 124, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/arrowforword.png"), ResourceLocation.parse("witchercraft:textures/screens/arrowforword.png")), e -> {
					int x = HigherVampireGuiScreen.this.x;
					int y = HigherVampireGuiScreen.this.y;
					if (true) {
						ClientPacketDistributor.sendToServer(new HigherVampireGuiButtonMessage(0, x, y, z));
						HigherVampireGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_arrowforword);
		imagebutton_bookmark = new ImageButton(this.leftPos + 204, this.topPos + -23, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/bookmark.png"), ResourceLocation.parse("witchercraft:textures/screens/bookmark.png")), e -> {
					int x = HigherVampireGuiScreen.this.x;
					int y = HigherVampireGuiScreen.this.y;
					if (true) {
						ClientPacketDistributor.sendToServer(new HigherVampireGuiButtonMessage(1, x, y, z));
						HigherVampireGuiButtonMessage.handleButtonAction(entity, 1, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_bookmark);
		imagebutton_bookmarkleft = new ImageButton(this.leftPos + -31, this.topPos + 4, 60, 23,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/bookmarkleft.png"), ResourceLocation.parse("witchercraft:textures/screens/bookmarkleft.png")), e -> {
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_bookmarkleft);
		imagebutton_drownerfacesmall = new ImageButton(this.leftPos + -15, this.topPos + 7, 16, 16,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/drownerfacesmall.png"), ResourceLocation.parse("witchercraft:textures/screens/drownerfacesmall.png")), e -> {
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_drownerfacesmall);
		imagebutton_bookmarkerleftbought = new ImageButton(this.leftPos + -28, this.topPos + 34, 60, 23,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/bookmarkerleftbought.png"), ResourceLocation.parse("witchercraft:textures/screens/bookmarkerleftbought.png")), e -> {
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_bookmarkerleftbought);
		imagebutton_arrowback = new ImageButton(this.leftPos + 17, this.topPos + 124, 32, 32,
				new WidgetSprites(ResourceLocation.parse("witchercraft:textures/screens/arrowback.png"), ResourceLocation.parse("witchercraft:textures/screens/arrowback.png")), e -> {
					int x = HigherVampireGuiScreen.this.x;
					int y = HigherVampireGuiScreen.this.y;
					if (true) {
						ClientPacketDistributor.sendToServer(new HigherVampireGuiButtonMessage(5, x, y, z));
						HigherVampireGuiButtonMessage.handleButtonAction(entity, 5, x, y, z);
					}
				}) {
			@Override
			public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
				guiGraphics.blit(RenderPipelines.GUI_TEXTURED, sprites.get(isActive(), isHoveredOrFocused()), getX(), getY(), 0, 0, width, height, width, height);
			}
		};
		this.addRenderableWidget(imagebutton_arrowback);
	}
}
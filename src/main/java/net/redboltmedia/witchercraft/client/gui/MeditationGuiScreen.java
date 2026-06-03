package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.MeditationGuiMenu;
import net.redboltmedia.witchercraft.procedures.*;
import net.redboltmedia.witchercraft.network.MeditationGuiButtonMessage;
import net.redboltmedia.witchercraft.init.WitchercraftModScreens;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphics;

public class MeditationGuiScreen extends AbstractContainerScreen<MeditationGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private Button button_back;
	private Button button_meditate;
	private ExtendedSlider MeditationTime;
	private static final ResourceLocation IMAGE_0 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow24.png");
	private static final ResourceLocation IMAGE_1 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow23.png");
	private static final ResourceLocation IMAGE_2 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow22.png");
	private static final ResourceLocation IMAGE_3 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow21.png");
	private static final ResourceLocation IMAGE_4 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow20.png");
	private static final ResourceLocation IMAGE_5 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow19.png");
	private static final ResourceLocation IMAGE_6 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow18.png");
	private static final ResourceLocation IMAGE_7 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow17.png");
	private static final ResourceLocation IMAGE_8 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow16.png");
	private static final ResourceLocation IMAGE_9 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow15.png");
	private static final ResourceLocation IMAGE_10 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow14.png");
	private static final ResourceLocation IMAGE_11 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow13.png");
	private static final ResourceLocation IMAGE_12 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow12.png");
	private static final ResourceLocation IMAGE_13 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow11.png");
	private static final ResourceLocation IMAGE_14 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow10.png");
	private static final ResourceLocation IMAGE_15 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow9.png");
	private static final ResourceLocation IMAGE_16 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow8.png");
	private static final ResourceLocation IMAGE_17 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow7.png");
	private static final ResourceLocation IMAGE_18 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow6.png");
	private static final ResourceLocation IMAGE_19 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow5.png");
	private static final ResourceLocation IMAGE_20 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow4.png");
	private static final ResourceLocation IMAGE_21 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow3.png");
	private static final ResourceLocation IMAGE_22 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow2.png");
	private static final ResourceLocation IMAGE_23 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandnow1.png");
	private static final ResourceLocation IMAGE_24 = ResourceLocation.parse("witchercraft:textures/screens/meditationhandmiddle.png");
	private static final ResourceLocation IMAGE_25 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand24.png");
	private static final ResourceLocation IMAGE_26 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand23.png");
	private static final ResourceLocation IMAGE_27 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand22.png");
	private static final ResourceLocation IMAGE_28 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand21.png");
	private static final ResourceLocation IMAGE_29 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand20.png");
	private static final ResourceLocation IMAGE_30 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand19.png");
	private static final ResourceLocation IMAGE_31 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand18.png");
	private static final ResourceLocation IMAGE_32 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand17.png");
	private static final ResourceLocation IMAGE_33 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand16.png");
	private static final ResourceLocation IMAGE_34 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand15.png");
	private static final ResourceLocation IMAGE_35 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand14.png");
	private static final ResourceLocation IMAGE_36 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand13.png");
	private static final ResourceLocation IMAGE_37 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand12.png");
	private static final ResourceLocation IMAGE_38 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand11.png");
	private static final ResourceLocation IMAGE_39 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand10.png");
	private static final ResourceLocation IMAGE_40 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand9.png");
	private static final ResourceLocation IMAGE_41 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand8.png");
	private static final ResourceLocation IMAGE_42 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand7.png");
	private static final ResourceLocation IMAGE_43 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand6.png");
	private static final ResourceLocation IMAGE_44 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand5.png");
	private static final ResourceLocation IMAGE_45 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand4.png");
	private static final ResourceLocation IMAGE_46 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand3.png");
	private static final ResourceLocation IMAGE_47 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand2.png");
	private static final ResourceLocation IMAGE_48 = ResourceLocation.parse("witchercraft:textures/screens/meditationhand1.png");
	private static final ResourceLocation IMAGE_49 = ResourceLocation.parse("witchercraft:textures/screens/meditationguitimenight.png");
	private static final ResourceLocation IMAGE_50 = ResourceLocation.parse("witchercraft:textures/screens/meditationguitimenoon.png");
	private static final ResourceLocation IMAGE_51 = ResourceLocation.parse("witchercraft:textures/screens/meditationguitimedawn.png");
	private static final ResourceLocation IMAGE_52 = ResourceLocation.parse("witchercraft:textures/screens/meditationguitimedusk.png");

	public MeditationGuiScreen(MeditationGuiMenu container, Inventory inventory, Component text) {
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
		if (elementType == 2 && elementState instanceof Number n) {
			if (name.equals("MeditationTime"))
				MeditationTime.setValue(n.doubleValue());
		}
		menuStateUpdateActive = false;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		if (MeditationGuiTimeNow24Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_0, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow23Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_1, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow22Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_2, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow21Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_3, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow20Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_4, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow19Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_5, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow18Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_6, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow17Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_7, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow16Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_8, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow15Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_9, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow14Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_10, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow13Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_11, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow12Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_12, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow11Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_13, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow10Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_14, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow9Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_15, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow8Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_16, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow7Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_17, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow6Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_18, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow5Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_19, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow4Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_20, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow3Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_21, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow2Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_22, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTimeNow1Procedure.execute(world)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_23, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_24, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		if (MeditationGuiTime24Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_25, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime23Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_26, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime22Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_27, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime21Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_28, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime20Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_29, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime19Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_30, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime18Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_31, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime17Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_32, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime16Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_33, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime15Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_34, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime14Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_35, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime13Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_36, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime12Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_37, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime11Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_38, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime10Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_39, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime9Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_40, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime8Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_41, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime7Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_42, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime6Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_43, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime5Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_44, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime4Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_45, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime3Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_46, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime2Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_47, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		if (MeditationGuiTime1Procedure.execute(entity)) {
			guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_48, this.leftPos + 22, this.topPos + 17, 0, 0, 131, 131, 131, 131);
		}
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_49, this.leftPos + 71, this.topPos + 143, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_50, this.leftPos + 72, this.topPos + -11, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_51, this.leftPos + -5, this.topPos + 67, 0, 0, 32, 32, 32, 32);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, IMAGE_52, this.leftPos + 149, this.topPos + 66, 0, 0, 32, 32, 32, 32);
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
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return (this.getFocused() != null && this.isDragging() && button == 0) ? this.getFocused().mouseDragged(mouseX, mouseY, button, dragX, dragY) : super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void init() {
		super.init();
		button_back = Button.builder(Component.translatable("gui.witchercraft.meditation_gui.button_back"), e -> {
			int x = MeditationGuiScreen.this.x;
			int y = MeditationGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new MeditationGuiButtonMessage(0, x, y, z));
				MeditationGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 250, this.topPos + 174, 46, 20).build();
		this.addRenderableWidget(button_back);
		button_meditate = Button.builder(Component.translatable("gui.witchercraft.meditation_gui.button_meditate"), e -> {
			int x = MeditationGuiScreen.this.x;
			int y = MeditationGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new MeditationGuiButtonMessage(1, x, y, z));
				MeditationGuiButtonMessage.handleButtonAction(entity, 1, x, y, z);
			}
		}).bounds(this.leftPos + 52, this.topPos + 174, 67, 20).build();
		this.addRenderableWidget(button_meditate);
		MeditationTime = new ExtendedSlider(this.leftPos + 16, this.topPos + 156, 143, 20, Component.translatable("gui.witchercraft.meditation_gui.MeditationTime_prefix"),
				Component.translatable("gui.witchercraft.meditation_gui.MeditationTime_suffix"), 1, 24, 12, 1, 0, true) {
			@Override
			protected void applyValue() {
				if (!menuStateUpdateActive)
					menu.sendMenuStateUpdate(entity, 2, "MeditationTime", this.getValue(), false);
			}
		};
		this.addRenderableWidget(MeditationTime);
		if (!menuStateUpdateActive)
			menu.sendMenuStateUpdate(entity, 2, "MeditationTime", MeditationTime.getValue(), false);
	}
}
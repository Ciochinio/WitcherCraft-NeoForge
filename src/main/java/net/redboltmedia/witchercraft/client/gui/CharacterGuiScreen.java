package net.redboltmedia.witchercraft.client.gui;

import net.redboltmedia.witchercraft.world.inventory.CharacterGuiMenu;
import net.redboltmedia.witchercraft.procedures.*;
import net.redboltmedia.witchercraft.network.CharacterGuiButtonMessage;
import net.redboltmedia.witchercraft.init.WitchercraftModScreens;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import com.mojang.blaze3d.platform.InputConstants;

public class CharacterGuiScreen extends AbstractContainerScreen<CharacterGuiMenu> implements WitchercraftModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private Button button_back;
	private static final Identifier BACKGROUND = Identifier.parse("witchercraft:textures/screens/character_gui.png");

	public CharacterGuiScreen(CharacterGuiMenu container, Inventory inventory, Component text) {
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
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		if (PlayerModelDisplayProcedure.execute(entity) instanceof LivingEntity livingEntity) {
			InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, this.leftPos + -1065, this.topPos + -919, this.leftPos + 935, this.topPos + 1081, 30, -livingEntity.getBbHeight() / (2.0f * livingEntity.getScale()),
					0f + (float) Math.atan((this.leftPos + -65 - mouseX) / 40.0), (float) Math.atan((this.topPos + 32 - mouseY) / 40.0), livingEntity);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, this.imageWidth, this.imageHeight);
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
		guiGraphics.text(this.font, CharacterGuiMovementSpeedProcedure.execute(entity), 87, 16, -12829636, false);
		guiGraphics.text(this.font, Component.translatable("gui.witchercraft.character_gui.label_movement_speed"), 6, 16, -12829636, false);
		guiGraphics.text(this.font, Component.translatable("gui.witchercraft.character_gui.label_attack_speed"), 6, 25, -12829636, false);
		guiGraphics.text(this.font, CharacterGuiAttackSpeedProcedure.execute(entity), 87, 25, -12829636, false);
		guiGraphics.text(this.font, Component.translatable("gui.witchercraft.character_gui.label_crit_chance"), 6, 34, -12829636, false);
		guiGraphics.text(this.font, CharacterGuiCritChanceProcedure.execute(entity), 87, 34, -12829636, false);
		guiGraphics.text(this.font, Component.translatable("gui.witchercraft.character_gui.label_crit_damage"), 6, 43, -12829636, false);
		guiGraphics.text(this.font, CharacterGuiCritDamageProcedure.execute(entity), 87, 43, -12829636, false);
		guiGraphics.text(this.font, Component.translatable("gui.witchercraft.character_gui.label_health"), 6, 52, -12829636, false);
		guiGraphics.text(this.font, CharacterGuiHealthProcedure.execute(entity), 87, 52, -12829636, false);
		guiGraphics.text(this.font, CharacterLevelProcedure.execute(entity), 6, 142, -12829636, false);
		guiGraphics.text(this.font, CharacterExperienceProcedure.execute(entity), 6, 151, -12829636, false);
		guiGraphics.text(this.font, Component.translatable("gui.witchercraft.character_gui.label_passivehealthregen"), 6, 61, -12829636, false);
		guiGraphics.text(this.font, CharacterGuiPassiveHealthRegenProcedure.execute(entity), 87, 61, -12829636, false);
		guiGraphics.text(this.font, Component.translatable("gui.witchercraft.character_gui.label_staminaregen"), 6, 70, -12829636, false);
		guiGraphics.text(this.font, CharacterGuiPassiveStaminaRegenProcedure.execute(entity), 87, 70, -12829636, false);
	}

	@Override
	public void init() {
		super.init();
		button_back = Button.builder(Component.translatable("gui.witchercraft.character_gui.button_back"), e -> {
			int x = CharacterGuiScreen.this.x;
			int y = CharacterGuiScreen.this.y;
			if (true) {
				ClientPacketDistributor.sendToServer(new CharacterGuiButtonMessage(0, x, y, z));
				CharacterGuiButtonMessage.handleButtonAction(entity, 0, x, y, z);
			}
		}).bounds(this.leftPos + 249, this.topPos + 169, 46, 20).build();
		this.addRenderableWidget(button_back);
	}
}
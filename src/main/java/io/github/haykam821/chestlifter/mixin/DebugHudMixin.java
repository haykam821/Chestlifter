package io.github.haykam821.chestlifter.mixin;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.haykam821.chestlifter.Main;
import io.github.haykam821.chestlifter.component.LiftedComponent;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.state.property.Property;
import net.minecraft.tag.RegistryTagManager;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
	@Shadow
	private MinecraftClient client;

	@Shadow
	public abstract String propertyToString(Entry<Property<?>, Comparable<?>> propertyEntry);

	@Inject(method = "getRightText", at = @At("RETURN"))
	public void addLiftedBlockInfo(CallbackInfoReturnable<List<String>> ci) {
		List<String> rightText = ci.getReturnValue();

		LiftedComponent component = Main.LIFTED_COMPONENT.get(this.client.player);
		if (!component.isEmpty()) {
			BlockState liftedState = component.getLiftedBlock().getBlockState();

			// Header
			rightText.add("");
			rightText.add(Formatting.UNDERLINE + "Lifted Block");

			// Identifier
			Identifier liftedId = Registry.BLOCK.getId(liftedState.getBlock());
			rightText.add(liftedId.toString());
			
			// Blockstates
			Iterator<Entry<Property<?>, Comparable<?>>> propertyIterator = liftedState.getEntries().entrySet().iterator();

			while (propertyIterator.hasNext()) {
				Entry<Property<?>, Comparable<?>> propertyEntry = propertyIterator.next();
				rightText.add(this.propertyToString(propertyEntry));
			}

			// Tags
			RegistryTagManager tagManager = this.client.getNetworkHandler().getTagManager();
			Iterator<Identifier> tagIterator = tagManager.blocks().getTagsFor(liftedState.getBlock()).iterator();

			while (tagIterator.hasNext()) {
				Identifier tagId = tagIterator.next();
				rightText.add("#" + tagId);
			}
		}
	}
}
package io.github.haykam821.chestlifter;

import io.github.haykam821.chestlifter.component.LiftedComponent;
import nerdhub.cardinal.components.api.ComponentRegistry;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.event.EntityComponentCallback;
import nerdhub.cardinal.components.api.util.EntityComponents;
import nerdhub.cardinal.components.api.util.RespawnCopyStrategy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class Main implements ModInitializer {
	private static final String MOD_ID = "chestlifter";

	private static final Identifier LIFTED_ID = new Identifier(MOD_ID, "lifted");
	public static final ComponentType<LiftedComponent> LIFTED_COMPONENT = ComponentRegistry.INSTANCE.registerIfAbsent(LIFTED_ID, LiftedComponent.class);

	private static final Identifier UNLIFTABLE_ID = new Identifier(MOD_ID, "unliftable");
	public static final Tag<Block> UNLIFTABLE = TagRegistry.block(UNLIFTABLE_ID);

	@Override
	public void onInitialize() {
		EntityComponentCallback.event(PlayerEntity.class).register((player, components) -> {
			components.put(LIFTED_COMPONENT, new LiftedComponent(player));
		});
		EntityComponents.setRespawnCopyStrategy(LIFTED_COMPONENT, RespawnCopyStrategy.INVENTORY);

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (hand != Hand.MAIN_HAND) return ActionResult.PASS;

			// Require empty hands
			if (!player.getMainHandStack().isEmpty()) return ActionResult.PASS;
			if (!player.getOffHandStack().isEmpty()) return ActionResult.PASS;

			LiftedComponent liftedComponent = LIFTED_COMPONENT.get(player);
			if (liftedComponent.isEmpty()) {
				// Require sneaking 3.5 blocks away from the block
				if (player.squaredDistanceTo(hitResult.getPos()) > 3.5) return ActionResult.PASS;
				if (!player.isSneaking()) return ActionResult.PASS;

				return liftedComponent.pickUpBlock(player, world, hand, hitResult);
			} else {
				return liftedComponent.placeDownBlock(player, world, hand, hitResult);
			}
		});
	}
}
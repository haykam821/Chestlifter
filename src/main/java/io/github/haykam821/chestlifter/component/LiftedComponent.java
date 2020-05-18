package io.github.haykam821.chestlifter.component;

import io.github.haykam821.chestlifter.Main;
import nerdhub.cardinal.components.api.ComponentType;
import nerdhub.cardinal.components.api.util.sync.EntitySyncedComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LiftedComponent implements EntitySyncedComponent {
	private final PlayerEntity player;

	private LiftedBlock liftedBlock;

	public LiftedComponent(PlayerEntity player) {
		this.player = player;
	}

	public LiftedBlock getLiftedBlock() {
		return this.liftedBlock;
	}

	public void setLiftedBlock(BlockState blockState, BlockEntity blockEntity) {
		this.liftedBlock = new LiftedBlock(blockState, blockEntity);
	}

	public void clearLiftedBlock() {
		this.liftedBlock = null;
	}

	public boolean isEmpty() {
		return this.liftedBlock == null;
	}

	public ActionResult pickUpBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		BlockPos pos = hitResult.getBlockPos();

		BlockState blockState = world.getBlockState(pos);
		if (blockState == null) return ActionResult.PASS;

		BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity == null) return ActionResult.PASS;

		// Clear existing block
		world.removeBlockEntity(pos);
		world.setBlockState(pos, world.getFluidState(pos).getBlockState());

		// Set lifted block
		this.setLiftedBlock(blockState, blockEntity);

		return ActionResult.SUCCESS;
	}

	public ActionResult placeDownBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
		BlockState blockState = this.getLiftedBlock().getBlockState();
		BlockPos placePos = hitResult.getBlockPos().offset(hitResult.getSide());

		if (!world.canPlace(blockState, placePos, EntityContext.of(player))) return ActionResult.PASS;
		if (!blockState.canPlaceAt(world, placePos)) return ActionResult.PASS;
		
		CompoundTag blockEntityTag = this.getLiftedBlock().getBlockEntityTagAtPosition(placePos);
		
		// Set world block
		world.setBlockState(placePos, blockState);
		world.getBlockEntity(placePos).fromTag(blockEntityTag);

		// Clear lifted block
		this.clearLiftedBlock();

		return ActionResult.SUCCESS;
	}

	@Override
	public void fromTag(CompoundTag tag) {
		if (tag.contains("LiftedBlock", 10)) {
			this.liftedBlock = LiftedBlock.createFromTag(tag.getCompound("LiftedBlock"));
		}
	}

	@Override
	public CompoundTag toTag(CompoundTag tag) {
		if (this.liftedBlock != null) {
			CompoundTag liftedBlockTag = new CompoundTag();
			tag.put("LiftedBlock", this.liftedBlock.toTag(liftedBlockTag));
		}

		return tag;
	}

	@Override
	public ComponentType<LiftedComponent> getComponentType() {
		return Main.LIFTED_COMPONENT;
	}

	@Override
	public Entity getEntity() {
		return this.player;
	}
}
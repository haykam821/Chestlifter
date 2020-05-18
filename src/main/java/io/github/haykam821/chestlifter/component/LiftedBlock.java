package io.github.haykam821.chestlifter.component;

import com.mojang.datafixers.Dynamic;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

public class LiftedBlock {
	private final BlockState blockState;
	private final BlockEntity blockEntity;

	public LiftedBlock(BlockState blockState, BlockEntity blockEntity) {
		this.blockState = blockState;
		this.blockEntity = blockEntity;
	}

	public BlockState getBlockState() {
		return this.blockState;
	}

	public BlockEntity getBlockEntity() {
		return this.blockEntity;
	}

	public CompoundTag getBlockEntityTagAtPosition(BlockPos pos) {
		CompoundTag blockEntityTag = this.blockEntity.toTag(new CompoundTag());

		blockEntityTag.putInt("x", pos.getX());
		blockEntityTag.putInt("y", pos.getY());
		blockEntityTag.putInt("z", pos.getZ());

		return blockEntityTag;
	}

	public CompoundTag toTag(CompoundTag tag) {
		tag.put("BlockState", BlockState.serialize(NbtOps.INSTANCE, this.blockState).getValue());

		CompoundTag blockEntityTag = new CompoundTag();
		tag.put("BlockEntity", this.blockEntity.toTag(blockEntityTag));

		return tag;
	}

	public static LiftedBlock createFromTag(CompoundTag tag) {
		if (!tag.contains("BlockState", 10) || !tag.contains("BlockEntity", 10))
			return null;

		CompoundTag blockStateTag = tag.getCompound("BlockState");
		BlockState blockState = BlockState.deserialize(new Dynamic<>(NbtOps.INSTANCE, blockStateTag));
		if (blockState == null) return null;

		CompoundTag blockEntityTag = tag.getCompound("BlockEntity");
		BlockEntity blockEntity = BlockEntity.createFromTag(blockEntityTag);
		if (blockEntity == null) return null;
		
		return new LiftedBlock(blockState, blockEntity);
	}
}
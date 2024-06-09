package com.bug1312.magicians_hat.common;

import com.bug1312.magicians_hat.Register;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HatBlockEntity extends BlockEntity {

	public HatBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(Register.HAT_BLOCK_ENTITY, blockPos, blockState);
	}

}

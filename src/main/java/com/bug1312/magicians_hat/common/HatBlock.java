package com.bug1312.magicians_hat.common;

import com.bug1312.magicians_hat.Register;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HatBlock extends BaseEntityBlock {

	private static final VoxelShape SHAPE = Shapes.or(Block.box(4, 0, 4, 12, 10, 12), Block.box(2, 9, 2, 14, 10, 14));
	
	public HatBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}
	
	@Override
	protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return SHAPE;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
		if (itemStack.is(Items.STICK)) {
			BlockEntity be = level.getBlockEntity(blockPos);
			if (!level.isClientSide() && be != null && be instanceof HatBlockEntity) {
				HatBlockEntity hat = (HatBlockEntity) be;
				BundleContents contents = hat.components().get(DataComponents.BUNDLE_CONTENTS);
				if (contents != null && !contents.isEmpty()) {
					BundleContents.Mutable mutable = new BundleContents.Mutable(contents);
					ItemStack givenStack = mutable.removeOne();
					
					if (givenStack != null) {
						Item givenItem = givenStack.getItem();
						ServerLevel server = (ServerLevel) level;
						Position pos = blockPos.getCenter().relative(Direction.UP, 3/16d);
						
						if (givenItem instanceof SpawnEggItem) {
			                EntityType<?> entityType = ((SpawnEggItem) givenItem).getType(givenStack);
			                Entity entity = entityType.spawn(server, givenStack, null, blockPos.above(), MobSpawnType.DISPENSER, false, false);
			                if (entity != null) {
			                	givenStack.shrink(1);
			                	entity.push(0, 0.4d, 0);
			                	level.gameEvent(null, GameEvent.ENTITY_PLACE, blockPos.above());
			                }
			                mutable.tryInsert(givenStack);
						} else if (givenStack.is(Items.FIREWORK_ROCKET) && givenItem instanceof ProjectileItem) {
							ProjectileItem projectileItem = ((ProjectileItem) givenItem);
					        Projectile projectile = projectileItem.asProjectile(level, pos, givenStack, Direction.UP);
							projectileItem.shoot(projectile, 0, 1, 0, 1.1f, 6.0f);
					        level.addFreshEntity(projectile);

							givenStack.shrink(1);
			                mutable.tryInsert(givenStack);
						} else {
							DefaultDispenseItemBehavior.spawnItem(server, givenStack, 6, Direction.UP, pos);
						}
						
						level.playSound(null, blockPos, Register.APPEAR, SoundSource.BLOCKS);
						
						DataComponentMap.Builder mapBuilder = DataComponentMap.builder();
						mapBuilder.addAll(hat.components());
						mapBuilder.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
						hat.setComponents(mapBuilder.build());
						
						level.playSound(null, blockPos, SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.BLOCKS);
					}
				}
			}

			return ItemInteractionResult.sidedSuccess(level.isClientSide());
		}

		return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos var1, BlockState var2) {
		return Register.HAT_BLOCK_ENTITY.create(var1, var2);
	}

	@Override
	protected RenderShape getRenderShape(BlockState blockState) {
		return RenderShape.MODEL;
	}

	public static final MapCodec<HatBlock> CODEC = HatBlock.simpleCodec(HatBlock::new);
	@Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }


}

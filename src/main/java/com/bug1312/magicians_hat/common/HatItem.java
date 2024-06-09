package com.bug1312.magicians_hat.common;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.math.Fraction;

import com.bug1312.magicians_hat.Register;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public class HatItem extends BlockItem implements Equipable {

	public HatItem(Block block, Properties properties) {
		super(block, properties);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext useOnContext) {
		if (useOnContext.getPlayer().isShiftKeyDown())
			return super.useOn(useOnContext);
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
		ItemStack hat = player.getItemInHand(hand);
		ItemStack stack = player.getItemInHand(otherHand);

		BundleContents contents = hat.get(DataComponents.BUNDLE_CONTENTS);
		if (contents == null) return InteractionResultHolder.fail(hat);
		BundleContents.Mutable mutable = new BundleContents.Mutable(contents);

        player.awardStat(Stats.ITEM_USED.get(this));

		if (stack.isEmpty()) {
			ItemStack givenItem = mutable.removeOne();

			if (givenItem != null) {
				this.playRemoveSound(player);
				player.setItemInHand(otherHand, givenItem);
			} else if (hand == InteractionHand.MAIN_HAND) return this.swapWithEquipmentSlot(hat.getItem(), level, player, hand);
			else return InteractionResultHolder.pass(hat);
			
		} else {
			int i = mutable.tryInsert(stack);
			if (i <= 0) return InteractionResultHolder.fail(hat);
			this.playInsertSound(player);
		}

		hat.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());

		return InteractionResultHolder.sidedSuccess(hat, level.isClientSide());
	}
	
	@Override 
	public EquipmentSlot getEquipmentSlot() { 
		return EquipmentSlot.HEAD;
	}

	// Yoinked and tweaked from BundleItem
    private static final int BAR_COLOR = Mth.color(240/255f, 49/255f, 153/255f);

    public static float getFullnessDisplay(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return bundleContents.weight().floatValue();
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
        if (clickAction != ClickAction.SECONDARY) {
            return false;
        }
        BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null) {
            return false;
        }
        ItemStack itemStack2 = slot.getItem();
        BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
        if (itemStack2.isEmpty() && !bundleContents.isEmpty()) {
            this.playRemoveSound(player);
            ItemStack itemStack3 = mutable.removeOne();
            if (itemStack3 != null) {
                ItemStack itemStack4 = slot.safeInsert(itemStack3);
                mutable.tryInsert(itemStack4);
            }
        } else if (itemStack2.getItem().canFitInsideContainerItems() && mutable.tryTransfer(slot, player) > 0) {
            this.playInsertSound(player);
        }
        itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack2, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
        if (clickAction != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }
        BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null) {
            return false;
        }
        BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
        if (itemStack2.isEmpty()) {
            ItemStack itemStack3 = mutable.removeOne();
            if (itemStack3 != null) {
                this.playRemoveSound(player);
                slotAccess.set(itemStack3);
            }
        } else {
            int i = mutable.tryInsert(itemStack2);
            if (i > 0) {
                this.playInsertSound(player);
            }
        }
        itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
        return true;
    }

    @Override
    public boolean isBarVisible(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return bundleContents.weight().compareTo(Fraction.ZERO) > 0;
    }

    @Override
    public int getBarWidth(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return Math.min(1 + Mth.mulAndTruncate(bundleContents.weight(), 12), 13);
    }

    @Override
    public int getBarColor(ItemStack itemStack) {
        return BAR_COLOR;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        if (itemStack.has(DataComponents.HIDE_TOOLTIP) || itemStack.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
            return Optional.empty();
        }
        return Optional.ofNullable(itemStack.get(DataComponents.BUNDLE_CONTENTS)).map(BundleTooltip::new);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents != null) {
            int i = Mth.mulAndTruncate(bundleContents.weight(), 64);
            list.add(Component.translatable("item.minecraft.bundle.fullness", i, 64).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        BundleContents bundleContents = itemEntity.getItem().get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null) {
            return;
        }
        itemEntity.getItem().set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        ItemUtils.onContainerDestroyed(itemEntity, bundleContents.itemsCopy());
    }

    private void playRemoveSound(Entity entity) {
        entity.playSound(Register.APPEAR, 0.8f, 0.8f + entity.level().getRandom().nextFloat() * 0.4f);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(Register.DISAPPEAR, 0.8f, 0.8f + entity.level().getRandom().nextFloat() * 0.4f);
    }

}

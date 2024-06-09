package com.bug1312.magicians_hat;

import com.bug1312.magicians_hat.common.HatBlock;
import com.bug1312.magicians_hat.common.HatBlockEntity;
import com.bug1312.magicians_hat.common.HatItem;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class Register {

	public static final SoundEvent APPEAR = Register.registerSound("item.magicians_hat.appear");
	public static final SoundEvent DISAPPEAR = Register.registerSound("item.magicians_hat.disappear");
	
	public static final HatBlock HAT_BLOCK = Register.register(BuiltInRegistries.BLOCK, "magicians_hat",
			new HatBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_WOOL).strength(0.3f)));

	public static final BlockEntityType<HatBlockEntity> HAT_BLOCK_ENTITY = Register.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, "magicians_hat",
			BlockEntityType.Builder.of(HatBlockEntity::new).build());

	public static final Item HAT_ITEM = Register.register(BuiltInRegistries.ITEM, "magicians_hat",
			new HatItem(HAT_BLOCK, new Item.Properties().stacksTo(1).equipmentSlot((e, s) -> EquipmentSlot.HEAD).component(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY)));

	public static final void initialize() {}

	private static final <V, T extends V> T register(Registry<V> registry, String name, T entry) {
		return Registry.register(registry, ResourceLocation.fromNamespaceAndPath(MagiciansHat.MOD_ID, name), entry);
	}
	
	private static final SoundEvent registerSound(String name) {
		ResourceLocation path = ResourceLocation.fromNamespaceAndPath(MagiciansHat.MOD_ID, name);
		return Registry.register(BuiltInRegistries.SOUND_EVENT, path, SoundEvent.createVariableRangeEvent(path));
	}
}

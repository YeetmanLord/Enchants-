package com.yeetmanlord.somanyenchants.common.enchantments.blocks;

import com.yeetmanlord.somanyenchants.core.config.Config;
import com.yeetmanlord.somanyenchants.core.init.EnchantmentTypesInit;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class CamouflageEnchant extends Enchantment {

	public CamouflageEnchant(Rarity rarityIn, EquipmentSlot... slots) 
	{
		super(rarityIn, EnchantmentTypesInit.TRAPPED_CHEST, slots);
	}
	
	@Override
	public boolean canEnchant(ItemStack stack)
	{
		return EnchantmentTypesInit.TRAPPED_CHEST.canEnchant(stack.getItem());
	}
	
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack) {
		return false;
	}
	
	@Override
	public int getMinCost(int enchantmentLevel) 
	{
		return 30;
	}
	
	@Override
	public int getMaxCost(int enchantmentLevel)
	{
		return this.getMinCost(enchantmentLevel) + 40;
	}
	
	@Override
	public int getMaxLevel() 
	{
		if(Config.camouflage.isEnabled.get() == false)
		 {
			 return 0;
		 }
		 else return Config.camouflage.maxLevel.get();
	}

}

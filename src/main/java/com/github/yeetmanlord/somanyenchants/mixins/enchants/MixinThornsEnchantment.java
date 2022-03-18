package com.github.yeetmanlord.somanyenchants.mixins.enchants;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.github.yeetmanlord.somanyenchants.core.config.Config;

import net.minecraft.world.item.enchantment.ThornsEnchantment;

@Mixin(ThornsEnchantment.class)
public class MixinThornsEnchantment {

	@Overwrite
	public int getMaxLevel() {

		if (Config.thorns.isEnabled.get() == false) {
			return 3;
		}
		else return Config.thorns.maxLevel.get();

	}

}

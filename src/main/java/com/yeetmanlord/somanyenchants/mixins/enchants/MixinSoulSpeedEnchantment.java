package com.yeetmanlord.somanyenchants.mixins.enchants;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.yeetmanlord.somanyenchants.core.config.Config;

import net.minecraft.world.item.enchantment.SoulSpeedEnchantment;

@Mixin(SoulSpeedEnchantment.class)
public class MixinSoulSpeedEnchantment {
	@Inject(at = @At("HEAD"), method = "getMaxLevel()I", cancellable = true)
	private void getMaxLevel(CallbackInfoReturnable<Integer> callback) {
		if (Config.soulSpeed.isEnabled.get() == false) {
			callback.setReturnValue(3);
		} else
			callback.setReturnValue(Config.soulSpeed.maxLevel.get());
	}
}

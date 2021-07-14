package com.yeetmanlord.enchantsplus.core.events;

import java.util.Random;

import javax.annotation.Nullable;

import com.yeetmanlord.enchantsplus.Main;
import com.yeetmanlord.enchantsplus.core.init.AttributeInit;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TieredItem;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effects;
import net.minecraft.stats.Stats;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.event.InputEvent.ClickInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Main.MOD_ID, bus = Bus.FORGE)
public class ExtraReach 
{
	@SubscribeEvent
	public static void extraReach(final ClickInputEvent click)
	{
		Minecraft mc = Minecraft.getInstance();
		PlayerEntity player = mc.player;
		if(click.isAttack() && !click.isCanceled())
		{
			double reachDist = player.getAttribute(AttributeInit.ATTACK_DISTANCE.get()).getValue();
			Vector3d startVector = player.getEyePosition(1.0F);
			Vector3d lookVector = player.getLook(1.0F);
			Vector3d endVector = startVector.add(lookVector.x * reachDist, lookVector.y * reachDist, lookVector.z * reachDist);	
			AxisAlignedBB axisalignedbb = player.getBoundingBox().expand(lookVector.scale(reachDist)).grow(1.0D, 1.0D, 1.0D);
			EntityRayTraceResult entityRayTrace = ProjectileHelper.rayTraceEntities(player, startVector, endVector, axisalignedbb, (p_215312_0_) -> {
	               return !p_215312_0_.isSpectator() && p_215312_0_.canBeCollidedWith();
	            }, reachDist * reachDist);
			if(entityRayTrace != null && !player.isCreative())
			{
				Entity tracedEntity = entityRayTrace.getEntity();
				if(player.getDistance(tracedEntity) > 4.0D && player.getDistance(tracedEntity) <= reachDist)
				{
					attackEntityAsPlayer(player, tracedEntity);
				}
			} else if(entityRayTrace != null && player.isCreative())
			{
				Entity tracedEntity = entityRayTrace.getEntity();
				if(player.getDistance(tracedEntity) > 4.0D && player.getDistance(tracedEntity) <= reachDist)
				{
					attackEntityAsPlayer(player, tracedEntity);
				}
			} else if(entityRayTrace == null)
			{
				
			} else
			{
				Main.LOGGER.error("Ray trace failed. This is not a good thing!!");
				Main.LOGGER.info(entityRayTrace);
			}
		}
		
	}
	
	
	private static void attackEntityAsPlayer(PlayerEntity player,@Nullable Entity targetEntity)
	{
		float f = (float) player.getAttributeManager().getAttributeValue(Attributes.ATTACK_DAMAGE);
		if(player.getHeldItemMainhand() != ItemStack.EMPTY)
		{
			Item weapon = player.getHeldItemMainhand().getItem();	
			if(weapon instanceof TieredItem)
			{
				TieredItem item = (TieredItem) weapon;
				if(item instanceof SwordItem)
				{
					f = ((SwordItem) item).getAttackDamage() + 1.0F;
				} else if(item instanceof PickaxeItem)
				{
					f = item.getTier().getAttackDamage() + 2.0F;
				} else if(item instanceof AxeItem)
				{
					AxeItem axe = (AxeItem)item;
					f = axe.getAttackDamage() + 1.0F;
				} else if(item instanceof ShovelItem)
				{
					f = item.getTier().getAttackDamage() + 2.5F;
				} else
				{
					f = 1.0F;
				}
			}
		}
		if(player.getActivePotionEffect(Effects.STRENGTH) != null) 
		{
			float strength = 3 + (player.getActivePotionEffect(Effects.STRENGTH).getAmplifier() * 3);
			if(strength != 0)
			{
				f = f + strength;
			}
		}
		if(targetEntity != null)
		{
			float f1;
			if (targetEntity instanceof LivingEntity) 
			{
	               f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), ((LivingEntity)targetEntity).getCreatureAttribute());
	        } else
	        {
	               f1 = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), CreatureAttribute.UNDEFINED);
	        }
			
			float f2 = player.getCooledAttackStrength(0.5F);
            f = f * (0.2F + f2 * f2 * 0.8F);
            f1 = f1 * f2;
            player.resetCooldown();
            
            if (f > 0.0F || f1 > 0.0F) 
            {
                boolean flag = f2 > 0.9F;
                boolean flag1 = false;
                int i = 3;
                i = i + EnchantmentHelper.getKnockbackModifier(player);
                if (player.isSprinting() && flag) 
                {
                	player.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, player.getSoundCategory(), 1.0F, 1.0F);
                   ++i;
                   flag1 = true;
                }
                
                boolean flag2 = flag && player.fallDistance > 0.0F && !player.isOnGround() && !player.isOnLadder() && !player.isInWater() && !player.isPotionActive(Effects.BLINDNESS) && !player.isPassenger() && targetEntity instanceof LivingEntity;
                flag2 = flag2 && !player.isSprinting();
                net.minecraftforge.event.entity.player.CriticalHitEvent hitResult = net.minecraftforge.common.ForgeHooks.getCriticalHit(player, targetEntity, flag2, flag2 ? 1.5F : 1.0F);
                flag2 = hitResult != null;
                if (flag2) 
                {
                   f *= hitResult.getDamageModifier();
                }
                
                f = f + f1;
                boolean flag3 = false;
                double d0 = (double)(player.distanceWalkedModified - player.prevDistanceWalkedModified);
                if (flag && !flag2 && !flag1 && player.isOnGround() && d0 < (double)player.getAIMoveSpeed()) 
                {
                   ItemStack itemstack = player.getHeldItem(Hand.MAIN_HAND);
                   if (itemstack.getItem() instanceof SwordItem)
                   {
                      flag3 = true;
                   }
                }
                
                float f4 = 0.0F;
                boolean flag4 = false;
                int j = EnchantmentHelper.getFireAspectModifier(player);
                if (targetEntity instanceof LivingEntity)
                {
                   f4 = ((LivingEntity)targetEntity).getHealth();
                   if (j > 0 && !targetEntity.isBurning())
                   {
                      flag4 = true;
                      targetEntity.setFire(1);
                   }
                }
                
                Vector3d vector3d = targetEntity.getMotion();
                boolean flag5 = attackEntityFrom(DamageSource.causePlayerDamage(player), f, targetEntity, player);
                if (flag5) {
                   if (i > 0) {
                      if (targetEntity instanceof LivingEntity) {
                         applyKnockback((float)i * 0.5F, (double)MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F))),(LivingEntity)targetEntity);
                      } else {
                         targetEntity.addVelocity((double)(-MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)) * (float)i * 0.5F), 0.1D, (double)(MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F)) * (float)i * 0.5F));
                      }
                      player.setMotion(player.getMotion().mul(0.6D, 1.0D, 0.6D));
                      player.setSprinting(false);
                   }
                   if (flag3) {
                      float f3 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * f;

                      for(LivingEntity livingentity : player.world.getEntitiesWithinAABB(LivingEntity.class, targetEntity.getBoundingBox().grow(1.0D, 0.25D, 1.0D))) {
                         if (livingentity != player && livingentity != targetEntity && !player.isOnSameTeam(livingentity) && (!(livingentity instanceof ArmorStandEntity) || !((ArmorStandEntity)livingentity).hasMarker()) && player.getDistanceSq(livingentity) < 9.0D) 
                         {
                            applyKnockback(0.4F, (double)MathHelper.sin(player.rotationYaw * ((float)Math.PI / 180F)), (double)(-MathHelper.cos(player.rotationYaw * ((float)Math.PI / 180F))), livingentity);
                            livingentity.attackEntityFrom(DamageSource.causePlayerDamage(player), f3);
                         }
                      }

                      player.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
                      player.spawnSweepParticles();
                   }

                   if (targetEntity instanceof ServerPlayerEntity && targetEntity.velocityChanged) {
                      ((ServerPlayerEntity)targetEntity).connection.sendPacket(new SEntityVelocityPacket(targetEntity));
                      targetEntity.velocityChanged = false;
                      targetEntity.setMotion(vector3d);
                   }

                   if (flag2) {
                      player.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                      player.onCriticalHit(targetEntity);
                   }

                   if (!flag2 && !flag3) {
                      if (flag) {
                         player.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                      } else {
                         player.world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                      }
                   }

                   if (f1 > 0.0F) {
                      player.onEnchantmentCritical(targetEntity);
                   }

                   player.setLastAttackedEntity(targetEntity);
                   if (targetEntity instanceof LivingEntity) {
                      EnchantmentHelper.applyThornEnchantments((LivingEntity)targetEntity, player);
                   }

                   EnchantmentHelper.applyArthropodEnchantments(player, targetEntity);
                   ItemStack itemstack1 = player.getHeldItemMainhand();
                   Entity entity = targetEntity;
                   if (targetEntity instanceof net.minecraftforge.entity.PartEntity) {
                      entity = ((net.minecraftforge.entity.PartEntity<?>) targetEntity).getParent();
                   }

                   if (!player.world.isRemote && !itemstack1.isEmpty() && entity instanceof LivingEntity) {
                      ItemStack copy = itemstack1.copy();
                      itemstack1.hitEntity((LivingEntity)entity, player);
                      if (itemstack1.isEmpty()) {
                         net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copy, Hand.MAIN_HAND);
                         player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                      }
                   }

                   if (targetEntity instanceof LivingEntity) {
                      float f5 = f4 - ((LivingEntity)targetEntity).getHealth();
                      player.addStat(Stats.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                      if (j > 0) {
                         targetEntity.setFire(j * 4);
                      }

                      if (player.world instanceof ServerWorld && f5 > 2.0F) {
                         int k = (int)((double)f5 * 0.5D);
                         ((ServerWorld)player.world).spawnParticle(ParticleTypes.DAMAGE_INDICATOR, targetEntity.getPosX(), targetEntity.getPosYHeight(0.5D), targetEntity.getPosZ(), k, 0.1D, 0.0D, 0.1D, 0.2D);
                      }
                   }

                   player.addExhaustion(0.1F);
                }
                
            }
		}
	}
	
	public static boolean attackEntityFrom(DamageSource source, float amount, Entity entityThingy, PlayerEntity player) 
	{
		if (entityThingy.isInvulnerableTo(source)) {
	         return false;
	      } else if (!entityThingy.world.isRemote) {
	         return false;
	      } else if (entityThingy instanceof LivingEntity)
	    	  {
	    	  	LivingEntity living = (LivingEntity) entityThingy;
	    	  	if (living.getShouldBeDead())
	    	  	{
	    	  		return false;
	    	  	} else if (source.isFireDamage() && living.isPotionActive(Effects.FIRE_RESISTANCE)) 
	    	  	{
	    	  		return false;
	    	  	} else {
	    	  		if (living.isSleeping() && !living.world.isRemote) 
	    	  		{
	    	  			living.wakeUp();
	    	  		}
	    	         living.setIdleTime(0);
	    	         float f = amount;
	    	         Random rand = new Random();
	    	         if ((source == DamageSource.ANVIL || source == DamageSource.FALLING_BLOCK) && !living.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()) {
	    	            living.getItemStackFromSlot(EquipmentSlotType.HEAD).damageItem((int)(amount * 4.0F + rand.nextFloat() * amount * 2.0F), living, (p_233653_0_) -> {
	    	               p_233653_0_.sendBreakAnimation(EquipmentSlotType.HEAD);
	    	            });
	    	            amount *= 0.75F;
	    	         }

	    	         boolean flag = false;
	    	         float f1 = 0.0F;
	    	         if (amount > 0.0F && living.canBlockDamageSource(source, living)) {
	    	            living.damageShield(amount, living);
	    	            f1 = amount;
	    	            amount = 0.0F;
	    	            if (!source.isProjectile()) {
	    	               Entity entity = source.getImmediateSource();
	    	               if (entity instanceof LivingEntity) {
	    	                  living.blockUsingShieldPublic((LivingEntity)entity);
	    	               }
	    	            }

	    	            flag = true;
	    	         }

	    	         living.limbSwingAmount = 1.5F;
	    	         boolean flag1 = true;
	    	         if ((float)living.hurtResistantTime > 10.0F) {
	    	            if (amount <= living.getLastDamage()) {
	    	               return false;
	    	            }
	    	            damageEntity(source, amount - living.getLastDamage(), living);
	    	            SoundEvent soundevent = living.getHurtSound(source, living);
	    	            if (soundevent != null) 
	    	            {
	    	            	living.playSound(soundevent, living.getSoundVolume(living), living.getSoundPitch(living));
	    	            }
	    	            living.setLastDamage(amount);
	    	            flag1 = false;
	    	         } else {
	    	            living.setLastDamage(amount);
	    	            living.hurtResistantTime = 20;
	    	            damageEntity(source, amount, living);
	    	            living.maxHurtTime = 10;
	    	            living.hurtTime = living.maxHurtTime;
	    	            SoundEvent soundevent = living.getHurtSound(source, living);
	    	            if (soundevent != null) 
	    	            {
	    	            	playSound(soundevent, living.getSoundVolume(living), living.getSoundPitch(living), living, player);
	    	            }
	    	         }

	    	         living.attackedAtYaw = 0.0F;
	    	         if (player != null) {
	    	               living.setRevengeTarget(player, living);
	    	               living.setRecentlyHit(100);
	    	               living.setAttackingPlayer(player);
	    	            }

	    	         if (flag1) {
	    	            if (flag) {
	    	               living.world.setEntityState(living, (byte)29);
	    	            } else if (source instanceof EntityDamageSource && ((EntityDamageSource)source).getIsThornsDamage()) {
	    	               living.world.setEntityState(living, (byte)33);
	    	            } else {
	    	               byte b0;
	    	               if (source == DamageSource.DROWN) {
	    	                  b0 = 36;
	    	               } else if (source.isFireDamage()) {
	    	                  b0 = 37;
	    	               } else if (source == DamageSource.SWEET_BERRY_BUSH) {
	    	                  b0 = 44;
	    	               } else {
	    	                  b0 = 2;
	    	               }

	    	               living.world.setEntityState(living, b0);
	    	            }

	    	            if (source != DamageSource.DROWN && (!flag || amount > 0.0F)) {
	    	               living.markVelocityChanged(living);
	    	            }

	    	            if (player != null) {
	    	            	
	    	            	Random random = new Random();
	    	            	
	    	            	double d0 = random.nextDouble();
	    	            	double d1 = random.nextDouble();
	    	            	
	    	            	double mult = random.nextDouble();
	    	                living.attackedAtYaw = (float)(MathHelper.atan2(d0, d1) * (double)(180F / (float)Math.PI) - (double)living.rotationYaw);
	    	                applyKnockback(0.4F, d0 * mult, d1 * mult, living);
	    	            } else {
	    	               living.attackedAtYaw = (float)((int)(Math.random() * 2.0D) * 180);
	    	            }
	    	         }

	    	         if (living.getShouldBeDead()) {
	    	            if (!living.checkTotemDeathProtectionPublic(source)) {
	    	               SoundEvent soundevent = living.getDeathSound(living);
	    	               if (flag1 && soundevent != null) {
	    	                  playSound(soundevent, living.getSoundVolume(living), living.getSoundPitch(living), living, player);
	    	               }
	    	               Main.LOGGER.info(living);
	    	               living.onDeath(source, living);
	    	            }
	    	         } else if (flag1) {
	    	            living.playHurtSoundPublic(source);
	    	         }

	    	         boolean flag2 = !flag || amount > 0.0F;
	    	         if (flag2) {
	    	            living.setLastDamageSource(source);
	    	            living.setLastDamageStamp(living.world.getGameTime());
	    	         }

	    	         if (living instanceof ServerPlayerEntity) {
	    	            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayerEntity)living, source, f, amount, flag);
	    	            if (f1 > 0.0F && f1 < 3.4028235E37F) {
	    	               ((ServerPlayerEntity)living).addStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(f1 * 10.0F));
	    	            }
	    	         }

	    	         if (player instanceof ServerPlayerEntity) {
	    	            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayerEntity)player, living, source, f, amount, flag);
	    	         }

	    	         return flag2;
	    	  	}
	      } else {
	    	  return true;
	      }
	}
	
	protected static void damageEntity(DamageSource damageSrc, float damageAmount, LivingEntity living) 
	{
	      if (!living.isInvulnerableTo(damageSrc)) {
	         damageAmount = net.minecraftforge.common.ForgeHooks.onLivingHurt(living, damageSrc, damageAmount);
	         if (damageAmount <= 0) {
	        	 return;
	         }
	         damageAmount = living.applyArmorCalculations(damageSrc, damageAmount, living);
	         damageAmount = living.applyPotionDamageCalculations(damageSrc, damageAmount, living);
	         float f2 = Math.max(damageAmount - living.getAbsorptionAmount(), 0.0F);
	         living.setAbsorptionAmount(living.getAbsorptionAmount() - (damageAmount - f2));
	         float f = damageAmount - f2;
	         if (f > 0.0F && f < 3.4028235E37F && damageSrc.getTrueSource() instanceof ServerPlayerEntity) {
	            ((ServerPlayerEntity)damageSrc.getTrueSource()).addStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(f * 10.0F));
	         }

	         f2 = net.minecraftforge.common.ForgeHooks.onLivingDamage(living, damageSrc, f2);
	         if (f2 != 0.0F) {
	            float f1 = living.getHealth();
	            living.getCombatTracker().trackDamage(damageSrc, f1, f2);
	            living.setHealth(f1 - f2); // Forge: moved to fix MC-121048
	            living.setAbsorptionAmount(living.getAbsorptionAmount() - f2);
	         }
	      }
	}
	
	
	private static void playSound(SoundEvent soundIn, float volume, float pitch, Entity entity, PlayerEntity player) {
	      if (!entity.isSilent()) {
	    	  entity.world.playSound(player, entity.getPosX(), entity.getPosY(), entity.getPosZ(), soundIn, entity.getSoundCategory(), volume, pitch);
	      }

	}
	
	public static void applyKnockback(float strength, double ratioX, double ratioZ, LivingEntity living) {
	      net.minecraftforge.event.entity.living.LivingKnockBackEvent event = net.minecraftforge.common.ForgeHooks.onLivingKnockBack(living, strength, ratioX, ratioZ);
	      if(event.isCanceled()) return;
	      strength = event.getStrength();
	      ratioX = event.getRatioX();
	      ratioZ = event.getRatioZ();
	      strength = (float)((double)strength * (1.0D - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
	      if (!(strength <= 0.0F)) {
	    	  living.isAirBorne = true;
	         Vector3d vector3d = living.getMotion();
	         Vector3d vector3d1 = (new Vector3d(ratioX, 0.0D, ratioZ)).normalize().scale((double)strength);
	         living.setMotion(vector3d.x / 2.0D - vector3d1.x, living.isOnGround() ? Math.min(0.4D, vector3d.y / 2.0D + (double)strength) : vector3d.y, vector3d.z / 2.0D - vector3d1.z);
	      }
	   }
}

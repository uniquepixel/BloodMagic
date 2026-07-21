package wayoftime.bloodmagic.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import wayoftime.bloodmagic.common.item.BMItems;
import wayoftime.bloodmagic.common.item.potion.AlchemyFlaskItem;
import wayoftime.bloodmagic.common.item.potion.AlchemyFlaskLingeringItem;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Full-fidelity port of 1.20.1's {@code EntityPotionFlask}, rewritten against vanilla's own
 * {@code ThrownPotion} (which solves the exact same "splash vs. lingering, extinguish fire on
 * plain-water hit" problem in this MC version) rather than the old NBT/PotionUtils-based logic.
 * Whether a hit produces a splash or a lingering cloud is read from the item class of the carried
 * stack ({@link #isLingering()}), matching vanilla's {@code getItem().is(Items.LINGERING_POTION)}
 * pattern instead of 1.20.1's separately-stored boolean flag.
 * <p>
 * Not ported: 1.20.1's {@code BlockProtectionHelper} claim/permission check around fire dousing -
 * that helper class doesn't exist yet on this branch (it's part of the ritual/claims system being
 * ported separately this session), so fire dousing here is plain vanilla block removal.
 */
public class PotionFlaskEntity extends ThrowableItemProjectile
{
	private static final Predicate<LivingEntity> WATER_SENSITIVE_OR_ON_FIRE = e -> e.isSensitiveToWater() || e.isOnFire();

	public PotionFlaskEntity(EntityType<? extends PotionFlaskEntity> type, Level level)
	{
		super(type, level);
	}

	public PotionFlaskEntity(Level level, LivingEntity thrower)
	{
		super(BMEntities.POTION_FLASK.get(), thrower, level);
	}

	public PotionFlaskEntity(Level level, double x, double y, double z)
	{
		super(BMEntities.POTION_FLASK.get(), x, y, z, level);
	}

	@Override
	protected Item getDefaultItem()
	{
		return BMItems.ALCHEMY_FLASK_THROWABLE.get();
	}

	@Override
	protected double getDefaultGravity()
	{
		return 0.05;
	}

	private boolean isLingering()
	{
		return getItem().getItem() instanceof AlchemyFlaskLingeringItem;
	}

	private List<MobEffectInstance> getCarriedEffects()
	{
		ItemStack stack = getItem();
		if (stack.getItem() instanceof AlchemyFlaskItem flaskItem)
		{
			return flaskItem.getEffectiveMobEffects(stack);
		}
		return List.of();
	}

	@Override
	protected void onHitBlock(BlockHitResult result)
	{
		super.onHitBlock(result);
		if (!level().isClientSide && getCarriedEffects().isEmpty())
		{
			Direction direction = result.getDirection();
			BlockPos hitPos = result.getBlockPos();
			BlockPos pos = hitPos.relative(direction);
			dowseFire(pos);
			dowseFire(pos.relative(direction.getOpposite()));

			for (Direction dir : Direction.Plane.HORIZONTAL)
			{
				dowseFire(pos.relative(dir));
			}
		}
	}

	@Override
	protected void onHit(HitResult result)
	{
		super.onHit(result);
		if (!level().isClientSide)
		{
			List<MobEffectInstance> effects = getCarriedEffects();
			if (effects.isEmpty())
			{
				applyWater();
			}
			else if (isLingering())
			{
				makeAreaOfEffectCloud(effects);
			}
			else
			{
				applySplash(effects, result.getType() == HitResult.Type.ENTITY ? ((EntityHitResult) result).getEntity() : null);
			}

			boolean hasInstant = effects.stream().anyMatch(instance -> instance.getEffect().value().isInstantenous());
			level().levelEvent(hasInstant ? 2007 : 2002, blockPosition(), PotionContents.getColor(effects));
			discard();
		}
	}

	private void applyWater()
	{
		AABB aabb = getBoundingBox().inflate(4.0, 2.0, 4.0);

		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, aabb, WATER_SENSITIVE_OR_ON_FIRE))
		{
			double distSq = distanceToSqr(living);
			if (distSq < 16.0)
			{
				if (living.isSensitiveToWater())
				{
					living.hurt(damageSources().indirectMagic(this, getOwner()), 1.0F);
				}

				if (living.isOnFire() && living.isAlive())
				{
					living.extinguishFire();
				}
			}
		}
	}

	private void applySplash(List<MobEffectInstance> sourceEffects, @Nullable Entity directHit)
	{
		AABB aabb = getBoundingBox().inflate(4.0, 2.0, 4.0);
		List<LivingEntity> list = level().getEntitiesOfClass(LivingEntity.class, aabb);
		if (list.isEmpty())
		{
			return;
		}

		Entity effectSource = getEffectSource();
		for (LivingEntity living : list)
		{
			if (!living.isAffectedByPotions())
			{
				continue;
			}

			double distSq = distanceToSqr(living);
			if (distSq >= 16.0)
			{
				continue;
			}

			double falloff = living == directHit ? 1.0 : 1.0 - Math.sqrt(distSq) / 4.0;
			for (MobEffectInstance sourceInstance : sourceEffects)
			{
				Holder<MobEffect> effect = sourceInstance.getEffect();
				if (effect.value().isInstantenous())
				{
					effect.value().applyInstantenousEffect(this, getOwner(), living, sourceInstance.getAmplifier(), falloff);
				}
				else
				{
					int duration = sourceInstance.mapDuration(d -> (int) (falloff * d + 0.5));
					MobEffectInstance scaled = new MobEffectInstance(effect, duration, sourceInstance.getAmplifier(),
							sourceInstance.isAmbient(), sourceInstance.isVisible());
					if (!scaled.endsWithin(20))
					{
						living.addEffect(scaled, effectSource);
					}
				}
			}
		}
	}

	private void makeAreaOfEffectCloud(List<MobEffectInstance> effects)
	{
		AreaEffectCloud cloud = new AreaEffectCloud(level(), getX(), getY(), getZ());
		if (getOwner() instanceof LivingEntity living)
		{
			cloud.setOwner(living);
		}

		cloud.setRadius(3.0F);
		cloud.setRadiusOnUse(-0.5F);
		cloud.setWaitTime(10);
		cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
		cloud.setPotionContents(new PotionContents(Optional.empty(), Optional.empty(), effects));
		level().addFreshEntity(cloud);
	}

	private void dowseFire(BlockPos pos)
	{
		BlockState state = level().getBlockState(pos);
		if (state.is(BlockTags.FIRE))
		{
			level().destroyBlock(pos, false, this);
		}
		else if (AbstractCandleBlock.isLit(state))
		{
			AbstractCandleBlock.extinguish(null, state, level(), pos);
		}
		else if (CampfireBlock.isLitCampfire(state))
		{
			level().levelEvent(null, 1009, pos, 0);
			CampfireBlock.dowse(getOwner(), level(), pos, state);
			level().setBlockAndUpdate(pos, state.setValue(CampfireBlock.LIT, Boolean.FALSE));
		}
	}
}

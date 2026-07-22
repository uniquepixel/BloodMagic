package wayoftime.bloodmagic.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.util.FakePlayer;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.api.BMIdentifiers;
import wayoftime.bloodmagic.common.blockentity.BloodAltarTile;
import wayoftime.bloodmagic.common.datamap.BMDataMaps;
import wayoftime.bloodmagic.util.AltarUtil;

/**
 * Full-fidelity port of 1.20.1's {@code ItemDaggerOfSacrifice} - distinct from the already-ported
 * self-sacrifice {@link SacrificialDaggerItem} ("Sacrificial Dagger"): killing a non-player mob
 * with this dagger instantly fills the nearest Blood Altar with LP scaled to that mob's health
 * times a per-entity-type ratio (ported as {@link BMDataMaps#SACRIFICE_LP_RATIO}, a data-map
 * equivalent of 1.20.1's {@code sacrificialValues}/{@code defaultSacrificeValue} config pair - see
 * that data map's javadoc), then finishes the kill off directly via the Sacrifice damage type.
 * <p>
 * 1.20.1's original does NOT suppress the mob's normal loot table drops - there's no
 * {@code LivingDropsEvent} cancellation anywhere in {@code ItemDaggerOfSacrifice}, so the mob
 * dies and drops loot exactly as if it had been killed with any other weapon; the dagger's only
 * special behaviour is the LP conversion and guaranteeing the kill happens within this hit. This
 * port preserves that (unsuppressed loot) precisely - it would be a behavioural regression to
 * suppress drops here despite that being an easy assumption to make from the item's name.
 */
public class DaggerOfSacrificeItem extends Item {
    public DaggerOfSacrificeItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof FakePlayer) {
            return false;
        }

        Level level = attacker.level();
        if (level.isClientSide || (attacker instanceof Player && !(attacker instanceof net.minecraft.server.level.ServerPlayer))) {
            return false;
        }

        if (target instanceof Player) {
            return false;
        }

        if (target.isBaby() && !(target instanceof Enemy)) {
            return false;
        }

        if (!target.isAlive() || target.getHealth() < 0.5F) {
            return false;
        }

        Integer configuredRatio = target.getType().builtInRegistryHolder().getData(BMDataMaps.SACRIFICE_LP_RATIO);
        int lifeEssenceRatio = configuredRatio != null ? configuredRatio : BloodMagic.SERVER_CONFIG.DEFAULT_ENTITY_SACRIFICE_RATIO.get();

        if (lifeEssenceRatio <= 0) {
            return false;
        }

        int lifeEssence = (int) (lifeEssenceRatio * target.getHealth());
        if (target.isBaby()) {
            lifeEssence *= 0.5F;
        }

        BlockPos altarPos = AltarUtil.findAltar(level, target.blockPosition(), 2);
        if (altarPos == null) {
            return false;
        }

        BlockEntity be = level.getBlockEntity(altarPos);
        if (!(be instanceof BloodAltarTile altar)) {
            return false;
        }

        altar.sacrificialDaggerCall(lifeEssence, true);

        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS,
                0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        target.setHealth(0.00001f);
        target.invulnerableTime = 0;
        target.hurt(level.damageSources().source(BMIdentifiers.DamageTypes.SACRIFICE, attacker), 10);

        return false;
    }
}

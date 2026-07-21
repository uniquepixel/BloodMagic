package wayoftime.bloodmagic.common.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Grants creative-style flight for as long as it's active - the effect behind the Ritual of the
 * Condor. Toggles {@code Abilities.mayfly} on and syncs it to the client each tick it's present,
 * and turns flight back off (if the player isn't otherwise allowed to fly) once it expires.
 */
public class FlightMobEffect extends MobEffect {
    public FlightMobEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE1F5FF);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            boolean expiring = player.getEffect(BMPotions.FLIGHT) != null && player.getEffect(BMPotions.FLIGHT).getDuration() <= 1;

            if (expiring) {
                if (!player.isCreative() && !player.isSpectator()) {
                    player.getAbilities().mayfly = false;
                    player.getAbilities().flying = false;
                }
            } else {
                player.getAbilities().mayfly = true;
            }

            player.onUpdateAbilities();
        }

        return true;
    }
}

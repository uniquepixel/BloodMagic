package wayoftime.bloodmagic.common.sigil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;
import wayoftime.bloodmagic.api.BMIdentifiers.Sigils;
import wayoftime.bloodmagic.api.sigil.SigilEffect;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.item.SigilItem;

/**
 * Simplified take on the Ritual/Sigil of Suppression: instead of the original's "spectral block"
 * system (temporarily replacing every block in range with an invisible marker block that reverts
 * after a timeout), this cancels mob spawn attempts within range directly via a global spawn-check
 * event. Functionally equivalent for the player (no mobs spawn nearby while active) without the
 * world-mutation machinery.
 */
public record SuppressionEffect(int radius, int upkeep) implements SigilEffect {
    public static final MapCodec<SuppressionEffect> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("radius").forGetter(SuppressionEffect::radius),
            Codec.INT.fieldOf("upkeep_cost").forGetter(SuppressionEffect::upkeep)
    ).apply(builder, SuppressionEffect::new));

    @Override
    public boolean isActivatable() {
        return true;
    }

    @Override
    public int activeTick(ItemStack sigil, Level level, Player player) {
        return upkeep;
    }

    @Override
    public MapCodec<? extends SigilEffect> codec() {
        return CODEC;
    }

    public static void onMobSpawnPositionCheck(MobSpawnEvent.PositionCheck event) {
        ServerLevel level = event.getLevel().getLevel();
        double x = event.getX();
        double y = event.getY();
        double z = event.getZ();

        for (ServerPlayer player : level.players()) {
            if (isSuppressing(player, x, y, z)) {
                event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
                return;
            }
        }
    }

    private static boolean isSuppressing(ServerPlayer player, double x, double y, double z) {
        for (ItemStack stack : player.getInventory().items) {
            if (matches(stack, player, x, y, z)) {
                return true;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (matches(stack, player, x, y, z)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matches(ItemStack stack, ServerPlayer player, double x, double y, double z) {
        if (!stack.has(BMDataComponents.SIGIL_ACTIVE)) {
            return false;
        }

        ResourceKey<SigilEffect> key = stack.getOrDefault(BMDataComponents.SIGIL_EFFECT, Sigils.DIVINATION);
        if (!key.equals(Sigils.SUPPRESSION)) {
            return false;
        }

        SigilEffect effect = SigilItem.getEffect(stack, player.registryAccess());
        if (!(effect instanceof SuppressionEffect suppression)) {
            return false;
        }

        return player.distanceToSqr(x, y, z) <= (double) suppression.radius() * suppression.radius();
    }
}

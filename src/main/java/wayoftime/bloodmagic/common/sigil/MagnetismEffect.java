package wayoftime.bloodmagic.common.sigil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.api.sigil.SigilEffect;

import java.util.List;

public record MagnetismEffect(int range, int upkeep) implements SigilEffect {
    public static final MapCodec<MagnetismEffect> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("range").forGetter(MagnetismEffect::range),
            Codec.INT.fieldOf("upkeep_cost").forGetter(MagnetismEffect::upkeep)
    ).apply(builder, MagnetismEffect::new));

    @Override
    public boolean isActivatable() {
        return true;
    }

    @Override
    public int activeTick(ItemStack sigil, Level level, Player player) {
        if (level.isClientSide) {
            return 0;
        }

        AABB area = new AABB(player.getX() - 0.5, player.getY() - player.getEyeHeight() - 0.5, player.getZ() - 0.5,
                player.getX() + 0.5, player.getY() - player.getEyeHeight() + 0.5, player.getZ() + 0.5)
                .inflate(range, range, range);

        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);
        for (ItemEntity item : items) {
            if (item.isAlive()) {
                item.playerTouch(player);
            }
        }

        List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, area);
        for (ExperienceOrb orb : orbs) {
            if (orb.isAlive()) {
                orb.playerTouch(player);
            }
        }

        return upkeep;
    }

    @Override
    public MapCodec<? extends SigilEffect> codec() {
        return CODEC;
    }
}

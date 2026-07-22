package wayoftime.bloodmagic.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import wayoftime.bloodmagic.common.blockentity.CrystalClusterTile;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;

import java.util.List;

/**
 * Full-fidelity port of 1.20.1's 5 {@code ItemCrystalCatalyst} variants (raw/corrosive/destructive/
 * steadfast/vengeful): right-clicked onto a Will Crystal Cluster of the matching type, consumes 1
 * of the stack to inject a temporary growth-speed boost (see
 * {@link CrystalClusterTile#applyCatalyst}). All 5 tiers share 1.20.1's exact constants (only the
 * Will type differs): 200 injected Will, a 10x speed multiplier while that buffer lasts, capped at
 * a 400 max buffer.
 */
public class CrystalCatalystItem extends Item {
    private static final double INJECTED_WILL = 200;
    private static final double SPEED_MULTIPLIER = 10;
    private static final double MAX_INJECTED_WILL = 400;

    public final EnumWillType type;

    public CrystalCatalystItem(EnumWillType type) {
        super(new Properties());
        this.type = type;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable("tooltip.bloodmagic.crystalCatalyst"));
        super.appendHoverText(stack, context, tooltip, tooltipFlag);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();

        BlockEntity tile = level.getBlockEntity(pos);
        if (!(tile instanceof CrystalClusterTile crystalTile)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (crystalTile.applyCatalyst(type, INJECTED_WILL, SPEED_MULTIPLIER, MAX_INJECTED_WILL)) {
            if (level instanceof ServerLevel server) {
                ItemStack particleStack = new ItemStack(this);
                ItemParticleOption particleData = new ItemParticleOption(ParticleTypes.ITEM, particleStack);
                for (int i = 0; i < 8; ++i) {
                    server.sendParticles(particleData, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 1, 0.2, 0.2, 0.2, 0.03);
                }
            }
            stack.shrink(1);
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1, 1);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }
}

package wayoftime.bloodmagic.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IRitualStone;

/**
 * One of seven rune-stone blocks (one per {@link EnumRuneType}) placed around a Master Ritual
 * Stone to form a ritual's multiblock pattern. Ported from 1.20.1's BlockRitualStone: each rune
 * type is its own registered block (rather than a single block with a blockstate property), and
 * "changing" a stone's rune type physically swaps in the other block.
 */
public class RitualStoneBlock extends Block implements IRitualStone {
    private final EnumRuneType type;

    public RitualStoneBlock(EnumRuneType type) {
        super(BlockBehaviour.Properties.of().strength(2.0F, 5.0F).sound(SoundType.STONE).requiresCorrectToolForDrops());
        this.type = type;
    }

    public EnumRuneType getRuneType() {
        return type;
    }

    @Override
    public boolean isRuneType(Level level, BlockPos pos, EnumRuneType runeType) {
        return type.equals(runeType);
    }

    @Override
    public void setRuneType(Level level, BlockPos pos, EnumRuneType runeType) {
        Block runeBlock = switch (runeType) {
            case AIR -> BMBlocks.RITUAL_STONE_AIR.block().get();
            case BLANK -> BMBlocks.RITUAL_STONE_BLANK.block().get();
            case DAWN -> BMBlocks.RITUAL_STONE_DAWN.block().get();
            case DUSK -> BMBlocks.RITUAL_STONE_DUSK.block().get();
            case EARTH -> BMBlocks.RITUAL_STONE_EARTH.block().get();
            case FIRE -> BMBlocks.RITUAL_STONE_FIRE.block().get();
            case WATER -> BMBlocks.RITUAL_STONE_WATER.block().get();
        };

        if (runeBlock != this) {
            level.setBlockAndUpdate(pos, runeBlock.defaultBlockState());
        }
    }
}

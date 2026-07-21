package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static wayoftime.bloodmagic.common.block.BlockAlternator.ACTIVE;

/**
 * Ported from 1.20.1's common/tile/TileDungeonAlternator.java. Backs BlockAlternator - a decorative
 * dungeon puzzle/switch block that flips its ACTIVE redstone-signal state every 40 ticks (2 seconds),
 * acting as a simple built-in redstone clock. 1.20.1's version extended a "TileTicking" base class
 * (onUpdate()/shouldTick() scaffolding this branch doesn't have); this branch's blockentities instead
 * use the plain static-tick-method + BlockEntityHelper.getTicker convention (see BlockAlternator and
 * e.g. AlchemyTableTile), so the cooldown counter just lives directly on this class instead.
 */
public class TileDungeonAlternator extends BaseTile {
    private int cooldown = 0;

    public TileDungeonAlternator(BlockPos pos, BlockState state) {
        super(BMTiles.DUNGEON_ALTERNATOR_TYPE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TileDungeonAlternator tile) {
        if (tile.cooldown >= 40) {
            level.setBlockAndUpdate(pos, state.setValue(ACTIVE, !state.getValue(ACTIVE)));
            tile.cooldown = 0;
        }
        tile.cooldown++;
    }
}

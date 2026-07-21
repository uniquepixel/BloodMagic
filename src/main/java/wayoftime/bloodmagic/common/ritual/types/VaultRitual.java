package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.loot.BMLootTables;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import java.util.function.Consumer;

/**
 * Heavily simplified take on the 1.20.1 Demon Dungeon system: the original procedurally stitches
 * together dozens of hand-built NBT room templates (with their own full brick/slab/stair/wall
 * block sets per Will type) into a branching multi-room dungeon via a room-pool synthesizer. None
 * of that is ported. This carves out a single small fixed vault instead - one room, Bloodstone
 * Brick walls, one loot chest - built in one shot the first time the ritual runs (it's a no-op on
 * later pulses once the chest is already there). Porting the real generator is tracked separately.
 */
public class VaultRitual extends Ritual {
    private static final int VERTICAL_OFFSET = 15;
    private static final int WIDTH = 5;
    private static final int DEPTH = 5;
    private static final int HEIGHT = 4;

    public VaultRitual() {
        super(RitualRegistry.rl("vault"), 1, 200000, "ritual.bloodmagic.vault");
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        BlockPos pos = masterRitualStone.getMasterBlockPos();

        BlockPos corner = pos.offset(-WIDTH / 2, -VERTICAL_OFFSET, -DEPTH / 2);
        BlockPos chestPos = corner.offset(WIDTH / 2, 1, DEPTH / 2);

        if (level.getBlockState(chestPos).is(Blocks.CHEST)) {
            return;
        }

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < DEPTH; z++) {
                    BlockPos target = corner.offset(x, y, z);
                    boolean shell = x == 0 || x == WIDTH - 1 || y == 0 || y == HEIGHT - 1 || z == 0 || z == DEPTH - 1;
                    level.setBlockAndUpdate(target, shell
                            ? BMBlocks.BLOODSTONE_BRICK.block().get().defaultBlockState()
                            : Blocks.AIR.defaultBlockState());
                }
            }
        }

        level.setBlockAndUpdate(chestPos, Blocks.CHEST.defaultBlockState());
        if (level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest) {
            chest.setLootTable(BMLootTables.DEMON_VAULT);
        }
    }

    @Override
    public int getRefreshCost() {
        return 0;
    }

    @Override
    public int getRefreshTime() {
        return 200;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addOffsetRunes(components, 1, 2, 0, EnumRuneType.DUSK);
        addCornerRunes(components, 1, 0, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new VaultRitual();
    }
}

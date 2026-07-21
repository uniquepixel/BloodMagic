package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Feller ({@code RitualFelling}): scans its felling
 * range once, caching every log/leaf block found, then chops them down one at a time each pulse,
 * inserting the drops into the inventory in its chest range (falling back to dropping an
 * {@link ItemEntity} if it's full/absent) rather than felling an entire tree from a single break.
 * Uses NeoForge's item-handler capability (the same one {@code ItemRouterTile} uses) in place of
 * the original's Forge {@code IItemHandler}/protection-mod hook.
 */
public class FellingRitual extends Ritual {
    public static final String FELLING_RANGE = "fellingRange";
    public static final String CHEST_RANGE = "chest";

    private static final ItemStack MOCK_AXE = new ItemStack(Items.DIAMOND_AXE);

    private final List<BlockPos> treePartsCache = new ArrayList<>();
    private Iterator<BlockPos> blockPosIterator;
    private boolean cached = false;

    public FellingRitual() {
        super(RitualRegistry.rl("felling"), 0, 20000, "ritual.bloodmagic.felling");
        addBlockRange(FELLING_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-10, -3, -10), new BlockPos(11, 27, 11)));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(FELLING_RANGE, 14000, 15, 30);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        if (level.isClientSide || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();
        BlockPos masterPos = masterRitualStone.getMasterBlockPos();
        AreaDescriptor chestRange = masterRitualStone.getBlockRange(CHEST_RANGE);
        BlockPos chestPos = chestRange.getContainedPositions(masterPos).get(0);

        IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, Direction.DOWN);

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        if (!cached || treePartsCache.isEmpty()) {
            treePartsCache.clear();
            for (BlockPos blockPos : masterRitualStone.getBlockRange(FELLING_RANGE).getContainedPositions(masterPos)) {
                BlockState state = level.getBlockState(blockPos);
                if (!level.isEmptyBlock(blockPos) && (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES))) {
                    treePartsCache.add(blockPos);
                }
            }

            cached = true;
            blockPosIterator = treePartsCache.iterator();
        }

        if (blockPosIterator.hasNext()) {
            BlockPos currentPos = blockPosIterator.next();
            blockPosIterator.remove();

            masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost()));
            BlockState state = level.getBlockState(currentPos);
            placeInInventory(state, serverLevel, currentPos, inventory);
            spawnParticlesAndSound(serverLevel, currentPos, state);

            level.setBlockAndUpdate(currentPos, Blocks.AIR.defaultBlockState());
        }
    }

    private void spawnParticlesAndSound(ServerLevel level, BlockPos pos, BlockState state) {
        SoundType soundtype = state.getSoundType();
        level.playSound(null, pos, soundtype.getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

        BlockParticleOption particleData = new BlockParticleOption(ParticleTypes.BLOCK, state);
        level.sendParticles(particleData, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.03);
    }

    private void placeInInventory(BlockState choppedState, ServerLevel level, BlockPos choppedPos, IItemHandler inventory) {
        List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(choppedState, level, choppedPos, level.getBlockEntity(choppedPos), null, MOCK_AXE);

        for (ItemStack stack : drops) {
            ItemStack remainder = inventory == null ? stack : ItemHandlerHelper.insertItem(inventory, stack, false);
            if (!remainder.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, choppedPos.getX() + 0.4, choppedPos.getY() + 2, choppedPos.getZ() + 0.4, remainder));
            }
        }
    }

    @Override
    public int getRefreshCost() {
        return 10;
    }

    @Override
    public int getRefreshTime() {
        return 1;
    }

    @Override
    public void gatherComponents(Consumer<RitualComponent> components) {
        addCornerRunes(components, 1, 0, EnumRuneType.EARTH);
        addCornerRunes(components, 1, 1, EnumRuneType.EARTH);
    }

    @Override
    public Ritual getNewCopy() {
        return new FellingRitual();
    }
}

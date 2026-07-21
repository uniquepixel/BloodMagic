package wayoftime.bloodmagic.common.incense;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.fluid.BMFluids;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Full-fidelity-in-spirit port of 1.20.1's {@code IncenseTranquilityRegistry} (block-predicate
 * handler list) merged with {@code BloodMagicValueManager}'s per-block tranquility map and
 * {@code BloodMagicCorePlugin}'s default registrations - on 1.20.1 those were split across an
 * addon-facing API (IBloodMagicAPI/BloodMagicAPI), a value manager and a core plugin bootstrap
 * class; this branch has no such addon API layer yet, so it's all folded into one bootstrap-style
 * registry, mirroring how {@link wayoftime.bloodmagic.common.ritual.RitualRegistry} centralizes
 * ritual registration.
 * <p>
 * Not ported: the JSON/config-driven override path ({@code IBloodMagicValueManager#setTranquility}
 * taking a String type name) that let modpacks retune values via config - there's no equivalent
 * config-overlay system on this branch yet, so registrations here are code-only, same as
 * {@link wayoftime.bloodmagic.common.ritual.RitualRegistry}.
 */
public class IncenseTranquilityRegistry {
    private static final List<ITranquilityHandler> HANDLERS = new ArrayList<>();
    private static final Map<Block, TranquilityStack> BLOCK_TRANQUILITY = new HashMap<>();

    public static void registerTranquilityHandler(ITranquilityHandler handler) {
        HANDLERS.add(handler);
    }

    public static void registerTranquilityHandler(Predicate<BlockState> predicate, EnumTranquilityType type, double value) {
        registerTranquilityHandler((world, pos, block, state) -> predicate.test(state) ? new TranquilityStack(type, value) : null);
    }

    public static void registerBlock(Block block, EnumTranquilityType type, double value) {
        BLOCK_TRANQUILITY.put(block, new TranquilityStack(type, value));
    }

    public static TranquilityStack getTranquilityOfBlock(Level world, BlockPos pos, Block block, BlockState state) {
        TranquilityStack direct = BLOCK_TRANQUILITY.get(block);
        if (direct != null) {
            return direct;
        }

        for (ITranquilityHandler handler : HANDLERS) {
            TranquilityStack tranq = handler.getTranquilityOfBlock(world, pos, block, state);
            if (tranq != null) {
                return tranq;
            }
        }

        return null;
    }

    public static void bootstrap() {
        registerBlock(Blocks.LAVA, EnumTranquilityType.LAVA, 1.2D);
        registerBlock(Blocks.WATER, EnumTranquilityType.WATER, 1.0D);
        // Life Essence Block is a DeferredHolder - resolving .get() here (bootstrap() runs from the
        // mod constructor, before RegisterEvent has bound it) throws. Registered as a lazily-evaluated
        // predicate handler instead, matching by block identity only once a scan actually runs.
        registerTranquilityHandler(state -> state.getBlock() == BMFluids.LIFE_ESSENCE_BLOCK.get(), EnumTranquilityType.WATER, 1.5D);
        registerBlock(Blocks.NETHERRACK, EnumTranquilityType.FIRE, 0.5D);
        registerBlock(Blocks.DIRT, EnumTranquilityType.EARTHEN, 0.25D);
        registerBlock(Blocks.FARMLAND, EnumTranquilityType.EARTHEN, 1.0D);
        registerBlock(Blocks.POTATOES, EnumTranquilityType.CROP, 1.0D);
        registerBlock(Blocks.CARROTS, EnumTranquilityType.CROP, 1.0D);
        registerBlock(Blocks.WHEAT, EnumTranquilityType.CROP, 1.0D);
        registerBlock(Blocks.NETHER_WART, EnumTranquilityType.CROP, 1.0D);
        registerBlock(Blocks.BEETROOTS, EnumTranquilityType.CROP, 1.0D);
        // WEAK_TAU/STRONG_TAU (Demon Will crops) aren't ported to this branch yet - skipped.

        // Added some blocks to the list at Tara's suggestion. (kept verbatim from 1.20.1)
        registerBlock(Blocks.CRIMSON_NYLIUM, EnumTranquilityType.FIRE, 0.75D);
        registerBlock(Blocks.WARPED_NYLIUM, EnumTranquilityType.FIRE, 0.75D);
        registerBlock(Blocks.NETHER_WART_BLOCK, EnumTranquilityType.PLANT, 1.0D);
        registerBlock(Blocks.WARPED_WART_BLOCK, EnumTranquilityType.PLANT, 1.0D);
        registerBlock(Blocks.SOUL_SAND, EnumTranquilityType.EARTHEN, 0.75D);
        registerBlock(Blocks.SOUL_SOIL, EnumTranquilityType.EARTHEN, 0.75D);
        registerBlock(Blocks.SOUL_FIRE, EnumTranquilityType.FIRE, 1.2D);
        registerBlock(Blocks.VINE, EnumTranquilityType.PLANT, 0.25D);
        registerBlock(Blocks.WEEPING_VINES, EnumTranquilityType.PLANT, 0.5D);
        registerBlock(Blocks.TWISTING_VINES, EnumTranquilityType.PLANT, 0.5D);
        registerBlock(Blocks.CAVE_VINES, EnumTranquilityType.PLANT, 0.5D);

        registerTranquilityHandler(state -> state.is(BlockTags.CANDLES), EnumTranquilityType.FIRE, 0.2D);
        registerTranquilityHandler(state -> state.getBlock() instanceof LeavesBlock, EnumTranquilityType.PLANT, 1.0D);
        registerTranquilityHandler(state -> state.getBlock() instanceof FireBlock, EnumTranquilityType.FIRE, 1.0D);
        registerTranquilityHandler(state -> state.getBlock() instanceof GrassBlock, EnumTranquilityType.EARTHEN, 0.5D);
        registerTranquilityHandler(state -> state.is(BlockTags.LOGS), EnumTranquilityType.TREE, 1.0D);
        registerTranquilityHandler(state -> state.getBlock() instanceof SimpleWaterloggedBlock && state.getFluidState().isSource(), EnumTranquilityType.WATER, 1.0D);
    }
}

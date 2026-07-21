package wayoftime.bloodmagic.common.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.blockentity.MasterRitualStoneTile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Ported from 1.20.1's {@code util.helper.RitualHelper}: rune multiblock validation, direction
 * detection, and ritual-stone placement. The old {@code IRitualStoneTile}/capability fallback
 * paths for tile-entity-based rune blocks aren't ported - every rune stone in this branch is a
 * plain {@link IRitualStone} block, so only the direct block-instance path is needed.
 */
public class RitualHelper {
    public static boolean canCrystalActivate(Ritual ritual, int crystalLevel) {
        return ritual.getCrystalLevel() <= crystalLevel;
    }

    /**
     * Checks every registered ritual (in all 4 horizontal directions) to see which one's rune
     * pattern matches what's actually placed around {@code pos}.
     */
    public static ResourceLocation getValidRitual(Level world, BlockPos pos) {
        for (Ritual ritual : RitualRegistry.all()) {
            for (int i = 0; i < 4; i++) {
                Direction direction = Direction.from2DDataValue(i);
                if (checkValidRitual(world, pos, ritual, direction)) {
                    return ritual.getId();
                }
            }
        }

        return null;
    }

    public static Direction getDirectionOfRitual(Level world, BlockPos pos, Ritual ritual) {
        for (int i = 0; i < 4; i++) {
            Direction direction = Direction.from2DDataValue(i);
            if (checkValidRitual(world, pos, ritual, direction)) {
                return direction;
            }
        }

        return null;
    }

    public static boolean checkValidRitual(Level world, BlockPos pos, Ritual ritual, Direction direction) {
        if (ritual == null) {
            return false;
        }

        List<RitualComponent> components = new ArrayList<>();
        ritual.gatherComponents(components::add);

        for (RitualComponent component : components) {
            BlockPos newPos = pos.offset(component.getOffset(direction));
            if (!isRuneType(world, newPos, component.getRuneType())) {
                return false;
            }
        }

        return true;
    }

    public static boolean isRuneType(Level world, BlockPos pos, EnumRuneType type) {
        if (world == null) {
            return false;
        }

        Block block = world.getBlockState(pos).getBlock();
        if (block instanceof IRitualStone ritualStone) {
            return ritualStone.isRuneType(world, pos, type);
        }

        return false;
    }

    public static boolean isRune(Level world, BlockPos pos) {
        return world != null && world.getBlockState(pos).getBlock() instanceof IRitualStone;
    }

    public static void setRuneType(Level world, BlockPos pos, EnumRuneType type) {
        if (world == null) {
            return;
        }

        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof IRitualStone ritualStone) {
            ritualStone.setRuneType(world, pos, type);
        }
    }

    public static boolean createRitual(Level world, BlockPos pos, Direction direction, Ritual ritual, boolean safe) {
        List<RitualComponent> components = new ArrayList<>();
        ritual.gatherComponents(components::add);

        if (abortConstruction(world, pos, direction, safe, components)) {
            return false;
        }

        world.setBlockAndUpdate(pos, BMBlocks.MASTER_RITUAL_STONE.block().get().defaultBlockState());
        setRitualStones(direction, world, pos, components);
        return true;
    }

    public static boolean abortConstruction(Level world, BlockPos pos, Direction direction, boolean safe, List<RitualComponent> components) {
        for (RitualComponent component : components) {
            BlockPos newPos = pos.offset(component.getOffset(direction));
            if (world.isOutsideBuildHeight(newPos) || (safe && !world.isEmptyBlock(newPos))) {
                return true;
            }
        }

        return false;
    }

    public static boolean repairRitualFromRuins(MasterRitualStoneTile tile, boolean safe) {
        Ritual ritual = tile.getCurrentRitual();
        Direction direction;

        if (ritual == null) {
            Pair<Ritual, Direction> pair = getRitualFromRuins(tile);
            ritual = pair.getKey();
            direction = pair.getValue();
        } else {
            direction = tile.getDirection();
        }

        if (ritual == null || direction == null) {
            return false;
        }

        Level world = tile.getWorldObj();
        BlockPos pos = tile.getMasterBlockPos();

        List<RitualComponent> components = new ArrayList<>();
        ritual.gatherComponents(components::add);

        if (abortConstruction(world, pos, direction, safe, components)) {
            return false;
        }

        setRitualStones(direction, world, pos, components);
        return true;
    }

    public static void setRitualStones(Direction direction, Level world, BlockPos pos, List<RitualComponent> gatheredComponents) {
        for (RitualComponent component : gatheredComponents) {
            BlockPos newPos = pos.offset(component.getOffset(direction));
            setRuneType(world, newPos, component.getRuneType());
            if (!isRune(world, newPos)) {
                world.setBlockAndUpdate(newPos, BMBlocks.RITUAL_STONE_BLANK.block().get().defaultBlockState());
                setRuneType(world, newPos, component.getRuneType());
            }
        }
    }

    public static Pair<Ritual, Direction> getRitualFromRuins(MasterRitualStoneTile tile) {
        BlockPos pos = tile.getMasterBlockPos();
        Level world = tile.getWorldObj();
        Ritual possibleRitual = tile.getCurrentRitual();
        Direction possibleDirection = tile.getDirection();
        int highestCount = 0;

        if (possibleRitual == null || possibleDirection == null) {
            for (Ritual ritual : RitualRegistry.all()) {
                for (int i = 0; i < 4; i++) {
                    Direction direction = Direction.from2DDataValue(i);
                    List<RitualComponent> components = new ArrayList<>();
                    ritual.gatherComponents(components::add);
                    int currentCount = 0;

                    for (RitualComponent component : components) {
                        BlockPos newPos = pos.offset(component.getOffset(direction));
                        if (isRuneType(world, newPos, component.getRuneType())) {
                            currentCount++;
                        }
                    }

                    if (currentCount > highestCount) {
                        highestCount = currentCount;
                        possibleRitual = ritual;
                        possibleDirection = direction;
                    }
                }
            }
        }

        return Pair.of(possibleRitual, possibleDirection);
    }

    /**
     * @return total rune count and a per-{@link EnumRuneType} breakdown for the given ritual's pattern.
     */
    public static Tuple<Integer, Map<EnumRuneType, Integer>> countRunes(Ritual ritual) {
        Map<EnumRuneType, Integer> runeMap = new EnumMap<>(EnumRuneType.class);
        List<RitualComponent> components = new ArrayList<>();
        ritual.gatherComponents(components::add);
        int totalRunes = components.size();
        for (RitualComponent component : components) {
            runeMap.compute(component.getRuneType(), (k, v) -> v == null ? 1 : v + 1);
        }

        return new Tuple<>(totalRunes, runeMap);
    }
}

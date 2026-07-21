package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of the Ellipsoid Builder ({@code RitualEllipsoid}): scans
 * the shell of an ellipsoid (sized from the player-resizable spheroid range) one cell per pulse -
 * resuming across ticks via a cached offset like {@link SphereCreateRitual} - and, for every empty
 * shell cell it finds, pulls one {@link BlockItem} out of the chest range's inventory and places it.
 * Uses NeoForge's item-handler capability in place of the original's Forge one, and places the
 * pulled block directly via {@link Level#setBlockAndUpdate} instead of the original's
 * {@code BlockProtectionHelper.tryPlaceBlock} (no protection-mod compat layer exists in this
 * branch).
 */
public class EllipsoidRitual extends Ritual {
    public static final String SPHEROID_RANGE = "spheroidRange";
    public static final String CHEST_RANGE = "chest";

    @Nullable
    private BlockPos currentPos;

    public EllipsoidRitual() {
        super(RitualRegistry.rl("ellipsoid"), 0, 20000, "ritual.bloodmagic.ellipsoid");
        addBlockRange(SPHEROID_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-10, -10, -10), new BlockPos(11, 11, 11)));
        addBlockRange(CHEST_RANGE, new AreaDescriptor.Rectangle(new BlockPos(0, 1, 0), 1));

        setMaximumVolumeAndDistanceOfRange(SPHEROID_RANGE, 0, 32, 32);
        setMaximumVolumeAndDistanceOfRange(CHEST_RANGE, 1, 3, 3);
    }

    @Override
    public void readFromNBT(CompoundTag tag) {
        super.readFromNBT(tag);
        if (tag.contains("curX")) {
            currentPos = new BlockPos(tag.getInt("curX"), tag.getInt("curY"), tag.getInt("curZ"));
        }
    }

    @Override
    public void writeToNBT(CompoundTag tag) {
        super.writeToNBT(tag);
        if (currentPos != null) {
            tag.putInt("curX", currentPos.getX());
            tag.putInt("curY", currentPos.getY());
            tag.putInt("curZ", currentPos.getZ());
        }
    }

    @Override
    public void performRitual(IMasterRitualStone masterRitualStone) {
        Level level = masterRitualStone.getWorldObj();
        int currentEssence = masterRitualStone.getOwnerNetwork().getCurrentEssence();

        BlockPos masterPos = masterRitualStone.getMasterBlockPos();
        BlockPos chestPos = masterRitualStone.getBlockRange(CHEST_RANGE).getContainedPositions(masterPos).get(0);
        IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, chestPos, Direction.DOWN);

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
            return;
        }

        if (itemHandler == null || itemHandler.getSlots() <= 0) {
            return;
        }

        AreaDescriptor sphereRange = masterRitualStone.getBlockRange(SPHEROID_RANGE);
        AABB sphereBB = sphereRange.getAABB(masterPos);
        int minX = (int) (masterPos.getX() - sphereBB.minX);
        int maxX = (int) (sphereBB.maxX - masterPos.getX()) - 1;
        int minY = (int) (masterPos.getY() - sphereBB.minY);
        int maxY = (int) (sphereBB.maxY - masterPos.getY()) - 1;
        int minZ = (int) (masterPos.getZ() - sphereBB.minZ);
        int maxZ = (int) (sphereBB.maxZ - masterPos.getZ()) - 1;

        int blockSlot = -1;
        for (int invSlot = 0; invSlot < itemHandler.getSlots(); invSlot++) {
            ItemStack stack = itemHandler.extractItem(invSlot, 1, true);
            if (stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) {
                continue;
            }
            blockSlot = invSlot;
            break;
        }

        if (blockSlot == -1) {
            return;
        }

        int xR = Math.max(maxX, minX);
        int yR = Math.max(maxY, minY);
        int zR = Math.max(maxZ, minZ);

        int j = -minX;
        int i = -minY;
        int k = -minZ;

        if (currentPos != null) {
            j = currentPos.getY();
            i = Math.min(xR, Math.max(-minX, currentPos.getX()));
            k = Math.min(zR, Math.max(-minZ, currentPos.getZ()));
        }

        int checks = 0;
        int maxChecks = 100;

        while (j <= maxY) {
            while (i <= maxX) {
                while (k <= maxZ) {
                    checks++;
                    if (checks >= maxChecks) {
                        this.currentPos = new BlockPos(i, j, k);
                        return;
                    }

                    if (checkIfEllipsoidShell(xR, yR, zR, i, j, k)) {
                        BlockPos newPos = masterPos.offset(i, j, k);

                        if (!level.isEmptyBlock(newPos)) {
                            k++;
                            continue;
                        }

                        BlockState placeState = Block.byItem(itemHandler.getStackInSlot(blockSlot).getItem()).defaultBlockState();
                        level.setBlockAndUpdate(newPos, placeState);
                        itemHandler.extractItem(blockSlot, 1, false);

                        masterRitualStone.getOwnerNetwork().syphon(masterRitualStone.ticket(getRefreshCost()));
                        k++;
                        this.currentPos = new BlockPos(i, j, k);
                        return;
                    }
                    k++;
                }
                i++;
                k = -minZ;
            }
            j++;
            i = -minX;
            this.currentPos = new BlockPos(i, j, k);
            return;
        }

        this.currentPos = new BlockPos(i, -minY, k);
    }

    public boolean checkIfEllipsoidShell(int xR, int yR, int zR, int xOff, int yOff, int zOff) {
        if (!checkIfEllipsoid(xR, yR, zR, xOff, yOff, zOff)) {
            return false;
        }

        return !((checkIfEllipsoid(xR, yR, zR, xOff + 1, yOff, zOff) && checkIfEllipsoid(xR, yR, zR, xOff - 1, yOff, zOff))
                && (checkIfEllipsoid(xR, yR, zR, xOff, yOff + 1, zOff) && checkIfEllipsoid(xR, yR, zR, xOff, yOff - 1, zOff))
                && (checkIfEllipsoid(xR, yR, zR, xOff, yOff, zOff + 1) && checkIfEllipsoid(xR, yR, zR, xOff, yOff, zOff - 1)));
    }

    public boolean checkIfEllipsoid(float xR, float yR, float zR, float xOff, float yOff, float zOff) {
        float possOffset = 0.5f;
        return xOff * xOff / ((xR + possOffset) * (xR + possOffset)) + yOff * yOff / ((yR + possOffset) * (yR + possOffset)) + zOff * zOff / ((zR + possOffset) * (zR + possOffset)) <= 1;
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
        addCornerRunes(components, 1, 0, EnumRuneType.DUSK);

        addRune(components, 4, 0, 0, EnumRuneType.FIRE);
        addRune(components, 5, 0, 0, EnumRuneType.FIRE);
        addRune(components, 5, 0, -1, EnumRuneType.FIRE);
        addRune(components, 5, 0, -2, EnumRuneType.FIRE);
        addRune(components, -4, 0, 0, EnumRuneType.FIRE);
        addRune(components, -5, 0, 0, EnumRuneType.FIRE);
        addRune(components, -5, 0, 1, EnumRuneType.FIRE);
        addRune(components, -5, 0, 2, EnumRuneType.FIRE);

        addRune(components, 0, 0, 4, EnumRuneType.AIR);
        addRune(components, 0, 0, 5, EnumRuneType.AIR);
        addRune(components, 1, 0, 5, EnumRuneType.AIR);
        addRune(components, 2, 0, 5, EnumRuneType.AIR);
        addRune(components, 0, 0, -4, EnumRuneType.AIR);
        addRune(components, 0, 0, -5, EnumRuneType.AIR);
        addRune(components, -1, 0, -5, EnumRuneType.AIR);
        addRune(components, -2, 0, -5, EnumRuneType.AIR);

        addRune(components, 3, 0, 1, EnumRuneType.EARTH);
        addRune(components, 3, 0, 2, EnumRuneType.EARTH);
        addRune(components, 3, 0, 3, EnumRuneType.EARTH);
        addRune(components, 2, 0, 3, EnumRuneType.EARTH);
        addRune(components, -3, 0, -1, EnumRuneType.EARTH);
        addRune(components, -3, 0, -2, EnumRuneType.EARTH);
        addRune(components, -3, 0, -3, EnumRuneType.EARTH);
        addRune(components, -2, 0, -3, EnumRuneType.EARTH);

        addRune(components, 1, 0, -3, EnumRuneType.WATER);
        addRune(components, 2, 0, -3, EnumRuneType.WATER);
        addRune(components, 3, 0, -3, EnumRuneType.WATER);
        addRune(components, 3, 0, -2, EnumRuneType.WATER);
        addRune(components, -1, 0, 3, EnumRuneType.WATER);
        addRune(components, -2, 0, 3, EnumRuneType.WATER);
        addRune(components, -3, 0, 3, EnumRuneType.WATER);
        addRune(components, -3, 0, 2, EnumRuneType.WATER);
    }

    @Override
    public Ritual getNewCopy() {
        return new EllipsoidRitual();
    }
}

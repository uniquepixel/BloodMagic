package wayoftime.bloodmagic.common.ritual.types;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import wayoftime.bloodmagic.api.ritual.AreaDescriptor;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.IMasterRitualStone;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Full-fidelity port of 1.20.1's Ritual of Sphere Creation ({@code RitualSphereCreate}): carves an
 * ellipsoid landing crater by relocating (not destroying) each non-air block inside it straight
 * down past the crater's far edge, one block per pulse - resuming across ticks (capped at 100
 * checks per tick) via a cached offset - until the whole volume has been swept, at which point it
 * deactivates itself.
 */
public class SphereCreateRitual extends Ritual {
    public static final String SPHEROID_RANGE = "spheroidRange";

    @Nullable
    private BlockPos currentPos;

    public SphereCreateRitual() {
        super(RitualRegistry.rl("sphere_create"), 0, 20000, "ritual.bloodmagic.sphere_create");
        addBlockRange(SPHEROID_RANGE, new AreaDescriptor.Rectangle(new BlockPos(-16, -35, -16), new BlockPos(17, -2, 17)));
        setMaximumVolumeAndDistanceOfRange(SPHEROID_RANGE, 0, 32, 70);
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

        if (currentEssence < getRefreshCost()) {
            masterRitualStone.causeNausea();
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

        double sphereCenterY = (maxY - minY) / 2.0;
        int yTeleportOffset = (int) (-sphereCenterY * 2);

        double xR = (maxX + minX) / 2.0;
        double yR = (maxY + minY) / 2.0;
        double zR = (maxZ + minZ) / 2.0;
        double sphereCenterX = xR;
        double sphereCenterZ = zR;

        int j = -minY;
        int i = -minX;
        int k = -minZ;

        if (currentPos != null) {
            j = currentPos.getY();
            i = currentPos.getX();
            k = currentPos.getZ();
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

                    if (checkIfEllipsoid(xR, yR, zR, i - sphereCenterX, j - sphereCenterY, k - sphereCenterZ)) {
                        BlockPos newPos = masterPos.offset(i, j, k);

                        if (level.isEmptyBlock(newPos)) {
                            k++;
                            continue;
                        }

                        BlockPos swapPos = newPos.offset(0, yTeleportOffset, 0);
                        swapLocations(level, newPos, swapPos);

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

        masterRitualStone.setActive(false);
    }

    private static void swapLocations(Level level, BlockPos a, BlockPos b) {
        BlockState stateA = level.getBlockState(a);
        BlockState stateB = level.getBlockState(b);
        level.setBlockAndUpdate(a, stateB);
        level.setBlockAndUpdate(b, stateA);
    }

    public boolean checkIfEllipsoid(double xR, double yR, double zR, double xOff, double yOff, double zOff) {
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
        addRune(components, 1, 0, 1, EnumRuneType.EARTH);
        addRune(components, 1, 0, -1, EnumRuneType.EARTH);
        addRune(components, -1, 0, 1, EnumRuneType.EARTH);
        addRune(components, -1, 0, -1, EnumRuneType.EARTH);
        addRune(components, 2, 1, 0, EnumRuneType.EARTH);
        addRune(components, 0, 1, 2, EnumRuneType.EARTH);
        addRune(components, -2, 1, 0, EnumRuneType.EARTH);
        addRune(components, 0, 1, -2, EnumRuneType.EARTH);
        addRune(components, 2, 1, 2, EnumRuneType.AIR);
        addRune(components, 2, 1, -2, EnumRuneType.AIR);
        addRune(components, -2, 1, 2, EnumRuneType.AIR);
        addRune(components, -2, 1, -2, EnumRuneType.AIR);
        addRune(components, 2, 2, 0, EnumRuneType.FIRE);
        addRune(components, 0, 2, 2, EnumRuneType.FIRE);
        addRune(components, -2, 2, 0, EnumRuneType.FIRE);
        addRune(components, 0, 2, -2, EnumRuneType.DUSK);
    }

    @Override
    public Ritual getNewCopy() {
        return new SphereCreateRitual();
    }
}

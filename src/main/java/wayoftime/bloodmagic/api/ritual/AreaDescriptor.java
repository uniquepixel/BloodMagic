package wayoftime.bloodmagic.api.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Ported from 1.20.1 as-is: a player-customizable, NBT-persisted range descriptor a ritual can
 * name (e.g. "waterRange", "damageRange"). Three shapes: an axis-aligned box a player can resize
 * by clicking two corners, a hemisphere, and a flat cross.
 */
public abstract class AreaDescriptor implements Iterator<BlockPos> {
    public List<BlockPos> getContainedPositions(BlockPos pos) {
        return new ArrayList<>();
    }

    public AABB getAABB(BlockPos pos) {
        return null;
    }

    public abstract void resetCache();

    public abstract boolean isWithinArea(BlockPos pos);

    public abstract void resetIterator();

    public void readFromNBT(CompoundTag tag) {
    }

    public void writeToNBT(CompoundTag tag) {
    }

    public abstract AreaDescriptor copy();

    public abstract int getVolumeForOffsets(BlockPos offset1, BlockPos offset2);

    public abstract boolean isWithinRange(BlockPos offset1, BlockPos offset2, int verticalLimit, int horizontalLimit);

    public abstract int getVolume();

    public abstract int getHeight();

    public abstract boolean isWithinRange(int verticalLimit, int horizontalLimit);

    /**
     * Changes the area descriptor so its range matches the two clicked-block positions.
     */
    public abstract void modifyAreaByBlockPositions(BlockPos pos1, BlockPos pos2);

    public abstract boolean intersects(AreaDescriptor descriptor);

    public abstract AreaDescriptor offset(BlockPos offset);

    public abstract AreaDescriptor rotateDescriptor(StructurePlaceSettings settings);

    public static class Rectangle extends AreaDescriptor {
        protected BlockPos minimumOffset;
        protected BlockPos maximumOffset; // non-inclusive
        private BlockPos currentPosition;

        private ArrayList<BlockPos> blockPosCache;
        private BlockPos cachedPosition;

        private boolean cache = true;

        public Rectangle(BlockPos minimumOffset, BlockPos maximumOffset) {
            setOffsets(minimumOffset, maximumOffset);
        }

        public Rectangle(BlockPos minimumOffset, int sizeX, int sizeY, int sizeZ) {
            this(minimumOffset, minimumOffset.offset(sizeX, sizeY, sizeZ));
        }

        public Rectangle(BlockPos minimumOffset, int size) {
            this(minimumOffset, size, size, size);
        }

        public Rectangle(Rectangle rectangle) {
            this(rectangle.minimumOffset, rectangle.maximumOffset);
        }

        @Override
        public Rectangle copy() {
            return new Rectangle(this);
        }

        @Override
        public List<BlockPos> getContainedPositions(BlockPos pos) {
            if (!cache || !pos.equals(cachedPosition) || blockPosCache.isEmpty()) {
                ArrayList<BlockPos> posList = new ArrayList<>();

                for (int j = minimumOffset.getY(); j < maximumOffset.getY(); j++) {
                    for (int i = minimumOffset.getX(); i < maximumOffset.getX(); i++) {
                        for (int k = minimumOffset.getZ(); k < maximumOffset.getZ(); k++) {
                            posList.add(pos.offset(i, j, k));
                        }
                    }
                }

                blockPosCache = posList;
                cachedPosition = pos;
            }

            return Collections.unmodifiableList(blockPosCache);
        }

        @Override
        public AABB getAABB(BlockPos pos) {
            AABB tempAABB = new AABB(minimumOffset.getX(), minimumOffset.getY(), minimumOffset.getZ(), maximumOffset.getX(), maximumOffset.getY(), maximumOffset.getZ());
            return tempAABB.move(pos.getX(), pos.getY(), pos.getZ());
        }

        @Override
        public int getHeight() {
            return this.maximumOffset.getY() - this.minimumOffset.getY();
        }

        public BlockPos getMinimumOffset() {
            return minimumOffset;
        }

        public BlockPos getMaximumOffset() {
            return maximumOffset;
        }

        /**
         * Sets the offsets so minimumOffset is always the lowest corner.
         */
        public void setOffsets(BlockPos offset1, BlockPos offset2) {
            this.minimumOffset = new BlockPos(Math.min(offset1.getX(), offset2.getX()), Math.min(offset1.getY(), offset2.getY()), Math.min(offset1.getZ(), offset2.getZ()));
            this.maximumOffset = new BlockPos(Math.max(offset1.getX(), offset2.getX()), Math.max(offset1.getY(), offset2.getY()), Math.max(offset1.getZ(), offset2.getZ()));
            blockPosCache = new ArrayList<>();
        }

        @Override
        public void resetCache() {
            this.blockPosCache = new ArrayList<>();
        }

        @Override
        public boolean isWithinArea(BlockPos pos) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            return x >= minimumOffset.getX() && x < maximumOffset.getX() && y >= minimumOffset.getY() && y < maximumOffset.getY() && z >= minimumOffset.getZ() && z < maximumOffset.getZ();
        }

        @Override
        public boolean hasNext() {
            return currentPosition == null || !(currentPosition.getX() + 1 == maximumOffset.getX() && currentPosition.getY() + 1 == maximumOffset.getY() && currentPosition.getZ() + 1 == maximumOffset.getZ());
        }

        @Override
        public BlockPos next() {
            if (currentPosition != null) {
                int nextX = currentPosition.getX() + 1 >= maximumOffset.getX() ? minimumOffset.getX() : currentPosition.getX() + 1;
                int nextZ = nextX != minimumOffset.getX() ? currentPosition.getZ() : (currentPosition.getZ() + 1 >= maximumOffset.getZ() ? minimumOffset.getZ() : currentPosition.getZ() + 1);
                int nextY = (nextZ != minimumOffset.getZ() || nextX != minimumOffset.getX()) ? currentPosition.getY() : (currentPosition.getY() + 1);
                currentPosition = new BlockPos(nextX, nextY, nextZ);
            } else {
                currentPosition = minimumOffset;
            }

            return currentPosition;
        }

        @Override
        public void remove() {
        }

        @Override
        public void resetIterator() {
            currentPosition = null;
        }

        @Override
        public void modifyAreaByBlockPositions(BlockPos pos1, BlockPos pos2) {
            setOffsets(pos1, pos2);
            maximumOffset = maximumOffset.offset(1, 1, 1);

            resetIterator();
            resetCache();
        }

        @Override
        public void readFromNBT(CompoundTag tag) {
            minimumOffset = new BlockPos(tag.getInt("xmin"), tag.getInt("ymin"), tag.getInt("zmin"));
            maximumOffset = new BlockPos(tag.getInt("xmax"), tag.getInt("ymax"), tag.getInt("zmax"));
        }

        @Override
        public void writeToNBT(CompoundTag tag) {
            tag.putInt("xmin", minimumOffset.getX());
            tag.putInt("ymin", minimumOffset.getY());
            tag.putInt("zmin", minimumOffset.getZ());
            tag.putInt("xmax", maximumOffset.getX());
            tag.putInt("ymax", maximumOffset.getY());
            tag.putInt("zmax", maximumOffset.getZ());
        }

        @Override
        public int getVolumeForOffsets(BlockPos offset1, BlockPos offset2) {
            BlockPos minPos = new BlockPos(Math.min(offset1.getX(), offset2.getX()), Math.min(offset1.getY(), offset2.getY()), Math.min(offset1.getZ(), offset2.getZ()));
            BlockPos maxPos = new BlockPos(Math.max(offset1.getX(), offset2.getX()), Math.max(offset1.getY(), offset2.getY()), Math.max(offset1.getZ(), offset2.getZ()));

            maxPos = maxPos.offset(1, 1, 1);

            return (maxPos.getX() - minPos.getX()) * (maxPos.getY() - minPos.getY()) * (maxPos.getZ() - minPos.getZ());
        }

        @Override
        public boolean isWithinRange(BlockPos offset1, BlockPos offset2, int verticalLimit, int horizontalLimit) {
            BlockPos minPos = new BlockPos(Math.min(offset1.getX(), offset2.getX()), Math.min(offset1.getY(), offset2.getY()), Math.min(offset1.getZ(), offset2.getZ()));
            BlockPos maxPos = new BlockPos(Math.max(offset1.getX(), offset2.getX()), Math.max(offset1.getY(), offset2.getY()), Math.max(offset1.getZ(), offset2.getZ()));

            return minPos.getY() >= -verticalLimit && maxPos.getY() <= verticalLimit && minPos.getX() >= -horizontalLimit && maxPos.getX() <= horizontalLimit && minPos.getZ() >= -horizontalLimit && maxPos.getZ() <= horizontalLimit;
        }

        @Override
        public int getVolume() {
            return (maximumOffset.getX() - minimumOffset.getX()) * (maximumOffset.getY() - minimumOffset.getY()) * (maximumOffset.getZ() - minimumOffset.getZ());
        }

        @Override
        public boolean isWithinRange(int verticalLimit, int horizontalLimit) {
            return minimumOffset.getY() >= -verticalLimit && maximumOffset.getY() <= verticalLimit + 1 && minimumOffset.getX() >= -horizontalLimit && maximumOffset.getX() <= horizontalLimit + 1 && minimumOffset.getZ() >= -horizontalLimit && maximumOffset.getZ() <= horizontalLimit + 1;
        }

        @Override
        public boolean intersects(AreaDescriptor descriptor) {
            if (descriptor instanceof Rectangle rectangle) {
                return !(minimumOffset.getX() >= rectangle.maximumOffset.getX() || minimumOffset.getY() >= rectangle.maximumOffset.getY() || minimumOffset.getZ() >= rectangle.maximumOffset.getZ() || rectangle.minimumOffset.getX() >= maximumOffset.getX() || rectangle.minimumOffset.getY() >= maximumOffset.getY() || rectangle.minimumOffset.getZ() >= maximumOffset.getZ());
            }

            return false;
        }

        @Override
        public AreaDescriptor offset(BlockPos offset) {
            return new Rectangle(this.minimumOffset.offset(offset), this.maximumOffset.offset(offset));
        }

        @Override
        public AreaDescriptor rotateDescriptor(StructurePlaceSettings settings) {
            BlockPos rotatePos1 = StructureTemplate.calculateRelativePosition(settings, minimumOffset);
            BlockPos rotatePos2 = StructureTemplate.calculateRelativePosition(settings, maximumOffset.offset(-1, -1, -1));

            Rectangle rectangle = new Rectangle(this.minimumOffset, 1);
            rectangle.modifyAreaByBlockPositions(rotatePos1, rotatePos2);

            return rectangle;
        }

        public void setDoCache(boolean doCache) {
            this.cache = doCache;
        }
    }

    public static class HemiSphere extends AreaDescriptor {
        private BlockPos minimumOffset;
        private int radius;

        private ArrayList<BlockPos> blockPosCache;
        private BlockPos cachedPosition;

        private final boolean cache = true;

        public HemiSphere(BlockPos minimumOffset, int radius) {
            setRadius(minimumOffset, radius);
        }

        public HemiSphere(HemiSphere hemiSphere) {
            this(hemiSphere.minimumOffset, hemiSphere.radius);
        }

        @Override
        public HemiSphere copy() {
            return new HemiSphere(this);
        }

        public void setRadius(BlockPos minimumOffset, int radius) {
            this.minimumOffset = minimumOffset;
            this.radius = radius;
            blockPosCache = new ArrayList<>();
        }

        @Override
        public int getHeight() {
            return this.radius * 2;
        }

        @Override
        public List<BlockPos> getContainedPositions(BlockPos pos) {
            if (!cache || !pos.equals(cachedPosition) || blockPosCache.isEmpty()) {
                ArrayList<BlockPos> posList = new ArrayList<>();

                int i = -radius;
                int j = minimumOffset.getY();
                int k = -radius;

                while (i <= radius) {
                    while (j <= radius) {
                        while (k <= radius) {
                            if (i * i + j * j + k * k >= (radius + 0.5F) * (radius + 0.5F)) {
                                k++;
                                continue;
                            }

                            posList.add(pos.offset(i, j, k));
                            k++;
                        }

                        k = -radius;
                        j++;
                    }

                    j = minimumOffset.getY();
                    i++;
                }

                blockPosCache = posList;
                cachedPosition = pos;
            }

            return Collections.unmodifiableList(blockPosCache);
        }

        /**
         * Since you can't make a box using a sphere, this returns null.
         */
        @Override
        public AABB getAABB(BlockPos pos) {
            return null;
        }

        @Override
        public void resetCache() {
            this.blockPosCache = new ArrayList<>();
        }

        @Override
        public boolean isWithinArea(BlockPos pos) {
            return blockPosCache.contains(pos);
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public BlockPos next() {
            return null;
        }

        @Override
        public void remove() {
        }

        @Override
        public void resetIterator() {
        }

        @Override
        public void modifyAreaByBlockPositions(BlockPos pos1, BlockPos pos2) {
        }

        @Override
        public int getVolumeForOffsets(BlockPos pos1, BlockPos pos2) {
            return 0;
        }

        @Override
        public boolean isWithinRange(BlockPos offset1, BlockPos offset2, int verticalLimit, int horizontalLimit) {
            return false;
        }

        @Override
        public int getVolume() {
            return 0;
        }

        @Override
        public boolean isWithinRange(int verticalLimit, int horizontalLimit) {
            return false;
        }

        @Override
        public boolean intersects(AreaDescriptor descriptor) {
            return false;
        }

        @Override
        public AreaDescriptor offset(BlockPos offset) {
            return new HemiSphere(minimumOffset.offset(offset), radius);
        }

        @Override
        public AreaDescriptor rotateDescriptor(StructurePlaceSettings settings) {
            return this;
        }
    }

    public static class Cross extends AreaDescriptor {
        private ArrayList<BlockPos> blockPosCache;
        private BlockPos cachedPosition;

        private final BlockPos centerPos;
        private final int size;

        private final boolean cache = true;

        public Cross(BlockPos center, int size) {
            this.centerPos = center;
            this.size = size;
            this.blockPosCache = new ArrayList<>();
        }

        public Cross(Cross cross) {
            this(cross.centerPos, cross.size);
        }

        @Override
        public Cross copy() {
            return new Cross(this);
        }

        @Override
        public int getHeight() {
            return this.size * 2 + 1;
        }

        @Override
        public List<BlockPos> getContainedPositions(BlockPos pos) {
            if (!cache || !pos.equals(cachedPosition) || blockPosCache.isEmpty()) {
                resetCache();

                blockPosCache.add(centerPos.offset(pos));
                for (int i = 1; i <= size; i++) {
                    blockPosCache.add(centerPos.offset(pos).offset(i, 0, 0));
                    blockPosCache.add(centerPos.offset(pos).offset(0, 0, i));
                    blockPosCache.add(centerPos.offset(pos).offset(-i, 0, 0));
                    blockPosCache.add(centerPos.offset(pos).offset(0, 0, -i));
                }
            }

            cachedPosition = pos;

            return Collections.unmodifiableList(blockPosCache);
        }

        @Override
        public void resetCache() {
            blockPosCache = new ArrayList<>();
        }

        @Override
        public boolean isWithinArea(BlockPos pos) {
            return blockPosCache.contains(pos);
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public BlockPos next() {
            return null;
        }

        @Override
        public void remove() {
        }

        @Override
        public void resetIterator() {
        }

        @Override
        public void modifyAreaByBlockPositions(BlockPos pos1, BlockPos pos2) {
        }

        @Override
        public int getVolumeForOffsets(BlockPos pos1, BlockPos pos2) {
            return 0;
        }

        @Override
        public boolean isWithinRange(BlockPos offset1, BlockPos offset2, int verticalLimit, int horizontalLimit) {
            return false;
        }

        @Override
        public int getVolume() {
            return 0;
        }

        @Override
        public boolean isWithinRange(int verticalLimit, int horizontalLimit) {
            return false;
        }

        @Override
        public boolean intersects(AreaDescriptor descriptor) {
            return false;
        }

        @Override
        public AreaDescriptor offset(BlockPos offset) {
            return new Cross(centerPos.offset(offset), size);
        }

        @Override
        public AreaDescriptor rotateDescriptor(StructurePlaceSettings settings) {
            return this;
        }
    }
}

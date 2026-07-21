package wayoftime.bloodmagic.common.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.common.meteor.MeteorDefinition;
import wayoftime.bloodmagic.common.meteor.MeteorDefinitions;

/**
 * Full-fidelity port of 1.20.1's {@code EntityMeteor}: a thrown projectile that carries a single
 * item's worth of "payload" and, once it lands inside a solid block, looks up the matching
 * {@link MeteorDefinition} for the item it's carrying and detonates that definition's layered sphere
 * of blocks into the world before removing itself.
 * <p>
 * The original resolved its payload via a datapack recipe registry ({@code RecipeMeteor}); this branch
 * resolves it via {@link MeteorDefinitions#pickFor(ItemStack)} instead - see that class for why.
 * Everything else (carrying an NBT-saved item stack, triggering only on landing inside an occluding
 * block, discarding itself afterward) is unchanged.
 */
public class EntityMeteor extends ThrowableProjectile {
    private static final String NBT_CONTAINED_ITEM = "ContainedItem";

    private ItemStack containedStack = ItemStack.EMPTY;

    public EntityMeteor(EntityType<? extends EntityMeteor> type, Level level) {
        super(type, level);
    }

    public EntityMeteor(Level level, LivingEntity thrower) {
        super(BMEntities.METEOR.get(), thrower, level);
    }

    public EntityMeteor(Level level, double x, double y, double z) {
        super(BMEntities.METEOR.get(), x, y, z, level);
    }

    public void setContainedStack(ItemStack stack) {
        this.containedStack = stack;
    }

    public ItemStack getContainedStack() {
        return containedStack;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (!containedStack.isEmpty()) {
            compound.put(NBT_CONTAINED_ITEM, containedStack.save(this.registryAccess()));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains(NBT_CONTAINED_ITEM)) {
            containedStack = ItemStack.parseOptional(this.registryAccess(), compound.getCompound(NBT_CONTAINED_ITEM));
        }
    }

    @Override
    protected void onInsideBlock(BlockState state) {
        if (level().isClientSide) {
            return;
        }

        if (!state.canOcclude()) {
            return;
        }

        int i = Mth.floor(position().x);
        int j = Mth.floor(position().y);
        int k = Mth.floor(position().z);
        BlockPos blockpos = new BlockPos(i, j, k);

        MeteorDefinition definition = MeteorDefinitions.pickFor(containedStack);
        if (definition != null) {
            definition.spawnMeteorInWorld(level(), blockpos);
        }

        discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No extra synced state - the payload is only needed server-side to resolve the impact,
        // and the client only ever needs to render the entity flying in.
    }
}

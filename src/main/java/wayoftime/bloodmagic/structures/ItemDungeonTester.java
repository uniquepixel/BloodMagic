package wayoftime.bloodmagic.structures;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Debug tool ported from 1.20.1's ItemDungeonTester - right-clicking triggers a single dungeon room's
 * generation immediately at the player's position/dimension, without needing to go through a Vault
 * ritual and get teleported into the dedicated dungeon dimension for real (see VaultRitual, the
 * genuine end-to-end entry point, for that full flow). This is exactly what "testing without needing
 * to enter the dungeon dimension for real" means in practice - it lets you eyeball whether
 * DungeonSynthesizer and the decorative block palette (see BMBlocks) actually produce a sane-looking
 * room, in creative in the overworld, without any altar/ritual setup.
 * <p>
 * 1.20.1's version (and its sibling DungeonTester helper class, not ported here) never actually got
 * this far - its real body was entirely commented out even there, only ever constructing a
 * DungeonSynthesizer and picking a ResourceLocation before giving up. This uses the exact same
 * {@link DungeonSynthesizer#generateInitialRoom} entry point {@code VaultRitual} now calls for real,
 * since that's the modern equivalent of what the old commented-out call was reaching for.
 */
public class ItemDungeonTester extends Item {
    public ItemDungeonTester() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            DungeonSynthesizer dungeon = new DungeonSynthesizer();
            BlockPos spawnPos = player.blockPosition().relative(player.getDirection(), 2);
            dungeon.generateInitialRoom(ModRoomPools.MINI_DUNGEON_ENTRANCES, level.random, serverLevel, spawnPos);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}

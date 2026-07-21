package wayoftime.bloodmagic.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.util.ChatUtil;

import java.util.List;
import java.util.Objects;

/**
 * Ported from 1.20.1's TileMimic. Stores the BlockState this Mimic is disguised as; the actual
 * disguise rendering lives in the dynamic baked model (client/model/mimic), which reads the
 * MIMIC model property populated by {@link #getModelData()} below.
 * <p>
 * Simplified from 1.20.1: the original additionally reconstructed a full in-memory "shadow"
 * BlockEntity for whatever was being mimicked (getTileFromStackWithTag/mimicedTile) and exposed a
 * generic 6-sided IItemHandler capability via its TileInventory base class. Neither is ever
 * actually read back into anything observable upstream (no GUI opens it, nothing renders it, and
 * dropItems() never drops its contents - Containers.dropContents is commented out even in 1.20.1),
 * so both are dropped here as dead weight; the two ItemStack slots (the disguise's display stack
 * and the creative-only potion-tuning stack) are kept as plain fields instead of a full Container.
 */
public class TileMimic extends BaseTile {
	public static final ModelProperty<BlockState> MIMIC = new ModelProperty<>();

	@Nullable
	private BlockState mimic;

	private ItemStack displayStack = ItemStack.EMPTY;
	private ItemStack potionStack = ItemStack.EMPTY;

	public boolean dropItemsOnBreak = true;
	public CompoundTag tileTag = new CompoundTag();

	// Creative-mode-only tuning fields ported as-is from 1.20.1: performSpecialAbility lets a
	// creative player adjust these by right-clicking sides while holding a potion, but (matching
	// upstream, where this is dead/unfinished) nothing ever actually reads them back to spawn a
	// periodic potion effect - so this remains cosmetic/vestigial on this branch too.
	public int playerCheckRadius = 5;
	public int potionSpawnRadius = 5;
	public int potionSpawnInterval = 40;

	public TileMimic(BlockPos pos, BlockState state) {
		super(BMTiles.MIMIC_TYPE.get(), pos, state);
	}

	public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, ItemStack heldItem, Direction side) {
		if (!heldItem.isEmpty() && player.isCreative()) {
			PotionContents potionContents = heldItem.get(DataComponents.POTION_CONTENTS);
			if (potionContents != null && potionContents.hasEffects()) {
				if (!world.isClientSide) {
					potionStack = heldItem.copy();
					setChanged();
					world.sendBlockUpdated(pos, state, state, 3);
					sendNoSpam(player, Component.translatable("chat.bloodmagic.mimic.potionSet"));
				}
				return true;
			}
		}

		if (performSpecialAbility(player, side)) {
			return true;
		}

		if (player.isShiftKeyDown())
			return false;

		if (!player.getItemInHand(hand).isEmpty() && player.getItemInHand(hand).getItem() == BMBlocks.MIMIC.asItem())
			return false;

		if (!displayStack.isEmpty() && !player.getItemInHand(hand).isEmpty())
			return false;

		if (!dropItemsOnBreak && !player.isCreative())
			return false;

		ItemStack stack = player.getItemInHand(hand);
		if (mimic == null || mimic == Blocks.AIR.defaultBlockState()) {
			if (!stack.isEmpty() && stack.getItem() instanceof BlockItem blockItem && !world.isClientSide) {
				Block block = blockItem.getBlock();
				BlockState mimicState = block.defaultBlockState();
				if (!mimicState.is(BMBlocks.MIMIC.block().get()) && !mimicState.is(BMBlocks.ETHEREAL_MIMIC.block().get())) {
					this.setMimic(mimicState);
				}
			}
		}

		if (player.isCreative()) {
			dropItemsOnBreak = displayStack.isEmpty();
		}

		return true;
	}

	public boolean performSpecialAbility(Player player, Direction sideHit) {
		if (!player.isCreative()) {
			return false;
		}

		if (player.getUseItem().isEmpty() && !potionStack.isEmpty()) {
			switch (sideHit) {
			case EAST: // When the block is clicked on the EAST or WEST side, potionSpawnRadius is edited.
			case WEST:
				if (player.isShiftKeyDown()) {
					potionSpawnRadius = Math.max(potionSpawnRadius - 1, 0);
					sendNoSpam(player, Component.translatable("chat.bloodmagic.mimic.potionSpawnRadius.down", potionSpawnRadius));
				} else {
					potionSpawnRadius++;
					sendNoSpam(player, Component.translatable("chat.bloodmagic.mimic.potionSpawnRadius.up", potionSpawnRadius));
				}
				break;
			case NORTH: // When the block is clicked on the NORTH or SOUTH side, detectRadius is edited.
			case SOUTH:
				if (player.isShiftKeyDown()) {
					playerCheckRadius = Math.max(playerCheckRadius - 1, 0);
					sendNoSpam(player, Component.translatable("chat.bloodmagic.mimic.detectRadius.down", playerCheckRadius));
				} else {
					playerCheckRadius++;
					sendNoSpam(player, Component.translatable("chat.bloodmagic.mimic.detectRadius.up", playerCheckRadius));
				}
				break;
			case UP: // When the block is clicked on the UP or DOWN side, potionSpawnInterval is edited.
			case DOWN:
				if (player.isShiftKeyDown()) {
					potionSpawnInterval = Math.max(potionSpawnInterval - 1, 1);
					sendNoSpam(player, Component.translatable("chat.bloodmagic.mimic.potionInterval.down", potionSpawnInterval));
				} else {
					potionSpawnInterval++;
					sendNoSpam(player, Component.translatable("chat.bloodmagic.mimic.potionInterval.up", potionSpawnInterval));
				}
				break;
			default:
				break;
			}

			return true;
		}

		return false;
	}

	private static void sendNoSpam(Player player, Component message) {
		ChatUtil.sendChatNoSpam(player, List.of(message));
	}

	public ItemStack getDisplayStack() {
		return displayStack;
	}

	public void setDisplayStack(ItemStack stack) {
		this.displayStack = stack;
		setChanged();
	}

	public void dropItems() {
		if (dropItemsOnBreak) {
			displayStack = ItemStack.EMPTY;
			potionStack = ItemStack.EMPTY;
		}
	}

	public void setMimic(BlockState mimic) {
		this.mimic = mimic;
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
		}
	}

	@Nullable
	public BlockState getMimic() {
		return mimic;
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
		BlockState oldMimic = mimic;
		CompoundTag tag = pkt.getTag();
		if (tag != null) {
			loadWithComponents(tag, registries);
		}
		if (!Objects.equals(oldMimic, mimic)) {
			requestModelDataUpdate();
			if (level != null) {
				level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
			}
		}
	}

	@Override
	public ModelData getModelData() {
		return ModelData.builder().with(MIMIC, mimic).build();
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);

		dropItemsOnBreak = tag.getBoolean("dropItemsOnBreak");
		tileTag = tag.getCompound("tileTag");
		readMimic(tag, registries);
		playerCheckRadius = tag.getInt("playerCheckRadius");
		potionSpawnRadius = tag.getInt("potionSpawnRadius");
		potionSpawnInterval = Math.max(1, tag.getInt("potionSpawnInterval"));

		displayStack = tag.contains("displayStack") ? ItemStack.parseOptional(registries, tag.getCompound("displayStack")) : ItemStack.EMPTY;
		potionStack = tag.contains("potionStack") ? ItemStack.parseOptional(registries, tag.getCompound("potionStack")) : ItemStack.EMPTY;
	}

	private void readMimic(CompoundTag tag, HolderLookup.Provider registries) {
		if (tag.contains("mimic")) {
			mimic = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("mimic"));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);

		tag.putBoolean("dropItemsOnBreak", dropItemsOnBreak);
		tag.put("tileTag", tileTag);
		tag.putInt("playerCheckRadius", playerCheckRadius);
		tag.putInt("potionSpawnRadius", potionSpawnRadius);
		tag.putInt("potionSpawnInterval", potionSpawnInterval);
		writeMimic(tag);

		if (!displayStack.isEmpty()) {
			tag.put("displayStack", displayStack.save(registries));
		}
		if (!potionStack.isEmpty()) {
			tag.put("potionStack", potionStack.save(registries));
		}
	}

	private void writeMimic(CompoundTag tag) {
		if (mimic != null) {
			tag.put("mimic", NbtUtils.writeBlockState(mimic));
		}
	}
}

package wayoftime.bloodmagic.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.block.BMBlocks;
import wayoftime.bloodmagic.common.blockentity.MasterRitualStoneTile;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.ritual.RitualHelper;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Full-fidelity port of 1.20.1's Ritual Diviner: right-click a Master Ritual Stone to build the
 * selected ritual's rune pattern one stone at a time (consuming blank ritual stones from the
 * player's inventory), shift-click in air to cycle the selected ritual, plain-click in air to
 * cycle the facing direction it'll be built in. The tier (normal/dusk/dawn) gates which rune
 * types it's allowed to place, matching the original's three diviner items.
 * <p>
 * Not ported: the client-side rune-placement hologram and the "hold right click to auto-place"
 * tick behavior - both are rendering/UX polish on top of this same, fully-functional placement
 * logic, which now runs one rune per click instead.
 */
public class RitualDivinerItem extends Item {
    private final int tier;

    public RitualDivinerItem() {
        this(0);
    }

    public RitualDivinerItem(int tier) {
        super(new Properties().stacksTo(1).component(BMDataComponents.DIVINER_DIRECTION, Direction.NORTH.get3DDataValue()));
        this.tier = tier;
    }

    @javax.annotation.Nullable
    public ResourceLocation getCurrentRitual(ItemStack stack) {
        return stack.get(BMDataComponents.DIVINER_RITUAL);
    }

    public void setCurrentRitual(ItemStack stack, ResourceLocation id) {
        stack.set(BMDataComponents.DIVINER_RITUAL, id);
    }

    public Direction getDirection(ItemStack stack) {
        Integer dataValue = stack.get(BMDataComponents.DIVINER_DIRECTION);
        return dataValue == null ? Direction.NORTH : Direction.values()[dataValue];
    }

    public void setDirection(ItemStack stack, Direction direction) {
        stack.set(BMDataComponents.DIVINER_DIRECTION, direction.get3DDataValue());
    }

    public boolean canPlaceRitualStone(EnumRuneType rune) {
        return switch (rune) {
            case BLANK, AIR, EARTH, FIRE, WATER -> true;
            case DUSK -> tier >= 1;
            case DAWN -> tier >= 2;
        };
    }

    /**
     * Called by {@link wayoftime.bloodmagic.common.block.MasterRitualStoneBlock} when this item is
     * used on a Master Ritual Stone.
     */
    public void handleUseOnMasterRitualStone(ItemStack stack, Level level, BlockPos pos, Player player, MasterRitualStoneTile tile) {
        if (player.isShiftKeyDown()) {
            ResourceLocation ritualId = getCurrentRitual(stack);
            Ritual ritual = ritualId == null ? null : RitualRegistry.get(ritualId);
            player.displayClientMessage(ritual == null
                    ? Component.translatable("chat.bloodmagic.diviner.none")
                    : Component.translatable(ritual.getTranslationKey()), true);
            return;
        }

        if (addRuneToRitual(stack, level, pos, player)) {
            level.playSound(null, pos, SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    /**
     * Finds the first component of the selected ritual's pattern that isn't correctly placed yet
     * (relative to {@code pos} and this stack's stored direction) and places/replaces it.
     *
     * @return true if a rune was placed or replaced.
     */
    public boolean addRuneToRitual(ItemStack stack, Level level, BlockPos pos, Player player) {
        ResourceLocation ritualId = getCurrentRitual(stack);
        Ritual ritual = ritualId == null ? null : RitualRegistry.get(ritualId);
        if (ritual == null) {
            return false;
        }

        Direction direction = getDirection(stack);
        List<RitualComponent> components = new ArrayList<>();
        ritual.gatherComponents(components::add);

        for (RitualComponent component : components) {
            if (!canPlaceRitualStone(component.getRuneType())) {
                return false;
            }

            BlockPos newPos = pos.offset(component.getOffset(direction));
            if (RitualHelper.isRune(level, newPos)) {
                if (!RitualHelper.isRuneType(level, newPos, component.getRuneType())) {
                    RitualHelper.setRuneType(level, newPos, component.getRuneType());
                    return true;
                }
            } else {
                BlockState state = level.getBlockState(newPos);
                if (state.canBeReplaced()) {
                    if (!consumeStone(player)) {
                        return false;
                    }

                    level.setBlockAndUpdate(newPos, BMBlocks.RITUAL_STONE_BLANK.block().get().defaultBlockState());
                    RitualHelper.setRuneType(level, newPos, component.getRuneType());
                    return true;
                } else {
                    player.displayClientMessage(Component.translatable("chat.bloodmagic.diviner.blockedBuild", newPos.getX(), newPos.getY(), newPos.getZ()), true);
                    return false;
                }
            }
        }

        return false;
    }

    public boolean consumeStone(Player player) {
        if (player.isCreative()) {
            return true;
        }

        for (ItemStack invStack : player.getInventory().items) {
            if (invStack.isEmpty()) {
                continue;
            }

            if (invStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof wayoftime.bloodmagic.common.block.RitualStoneBlock) {
                invStack.shrink(1);
                return true;
            }
        }

        return false;
    }

    public boolean canDivinerPerformRitual(Ritual ritual) {
        if (ritual == null) {
            return false;
        }

        List<RitualComponent> components = new ArrayList<>();
        ritual.gatherComponents(components::add);
        for (RitualComponent component : components) {
            if (!canPlaceRitualStone(component.getRuneType())) {
                return false;
            }
        }

        return true;
    }

    public void cycleRitual(ItemStack stack, Player player, boolean reverse) {
        List<Ritual> rituals = new ArrayList<>(RitualRegistry.all());
        if (reverse) {
            Collections.reverse(rituals);
        }

        ResourceLocation key = getCurrentRitual(stack);
        ResourceLocation firstId = null;
        boolean foundId = false;

        for (Ritual ritual : rituals) {
            if (!canDivinerPerformRitual(ritual)) {
                continue;
            }

            if (firstId == null) {
                firstId = ritual.getId();
            }

            if (foundId) {
                setCurrentRitual(stack, ritual.getId());
                notifyRitualChange(ritual, player);
                return;
            } else if (ritual.getId().equals(key)) {
                foundId = true;
            }
        }

        if (firstId != null) {
            setCurrentRitual(stack, firstId);
            notifyRitualChange(RitualRegistry.get(firstId), player);
        }
    }

    public void notifyRitualChange(Ritual ritual, Player player) {
        if (ritual != null) {
            player.displayClientMessage(Component.translatable(ritual.getTranslationKey()), true);
        }
    }

    public void cycleDirection(ItemStack stack, Player player) {
        Direction direction = getDirection(stack);
        Direction newDirection = switch (direction) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            default -> Direction.NORTH;
        };

        setDirection(stack, newDirection);
        player.displayClientMessage(Component.translatable("tooltip.bloodmagic.diviner.currentDirection", newDirection.getName()), true);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                cycleRitual(stack, player, false);
            } else {
                cycleDirection(stack, player);
            }
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ResourceLocation ritualId = getCurrentRitual(stack);
        Ritual ritual = ritualId == null ? null : RitualRegistry.get(ritualId);
        if (ritual == null) {
            super.appendHoverText(stack, context, tooltip, flag);
            return;
        }

        tooltip.add(Component.translatable("tooltip.bloodmagic.diviner.currentRitual", Component.translatable(ritual.getTranslationKey())).withStyle(ChatFormatting.GRAY));

        Tuple<Integer, java.util.Map<EnumRuneType, Integer>> runeCount = RitualHelper.countRunes(ritual);
        tooltip.add(Component.literal(""));
        for (EnumRuneType type : EnumRuneType.values()) {
            int count = runeCount.getB().getOrDefault(type, 0);
            if (count > 0) {
                tooltip.add(Component.literal(count + "x " + type.name().toLowerCase(Locale.ROOT)).withStyle(type.colorCode));
            }
        }
        tooltip.add(Component.translatable("tooltip.bloodmagic.diviner.totalRune", runeCount.getA()).withStyle(ChatFormatting.GRAY));

        super.appendHoverText(stack, context, tooltip, flag);
    }
}

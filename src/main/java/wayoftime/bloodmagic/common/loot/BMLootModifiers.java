package wayoftime.bloodmagic.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.api.BMTags;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;

import java.util.Optional;

/**
 * Full-fidelity port of 1.20.1's {@code wayoftime.bloodmagic.loot.GlobalLootModifier} (its 5
 * {@code data/bloodmagic/loot_modifiers/*.json} entries: fortune, looting, silk_touch_bamboo,
 * smelt, voiding) onto NeoForge's modern codec-based {@link IGlobalLootModifier} API - registered
 * via a {@link DeferredRegister} on {@link NeoForgeRegistries.Keys#GLOBAL_LOOT_MODIFIER_SERIALIZERS}
 * instead of the old {@code LootTableLoadEvent} approach, with instances wired up from
 * {@code data/neoforge/loot_modifiers/global_loot_modifiers.json}.
 * <p>
 * Adapted for this branch's anointment redesign: 1.20.1 read anointment levels off an
 * {@code AnointmentHolder} NBT blob; this branch instead has {@link AnointmentItem} write a plain
 * "uses remaining" int into one of the {@code ANOINTMENT_*_USES} data components on the target
 * item (see {@link BMDataComponents}) - so these modifiers read/decrement those components instead,
 * matching the same pattern already used by {@code AnointmentEventHandler#onBlockDrops} for
 * handheld tools. Unlike that event-based handler (which only ever sees normal player mining, since
 * it hooks NeoForge's {@code BlockDropsEvent} - fired from {@code Block#dropResources}), these are
 * real loot modifiers: they run inside {@code LootTable#getRandomItems}, which is also exactly what
 * {@code ExplosiveChargeTile#breakAndCollectDrops} calls when a Charge detonates. That's what makes
 * an augmented Charge's synthetic harvesting tool (see {@code ExplosiveChargeTile#getHarvestingTool})
 * actually matter: only a real loot modifier reaches it. onBlockDrops's fortune/silk-touch/
 * smelting/voiding handling was removed once these were added, to avoid double-processing the same
 * block break (see AnointmentEventHandler's class javadoc).
 */
public class BMLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> GLM =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, BloodMagic.MODID);

    public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<SilkTouchModifier>> SILK_TOUCH =
            GLM.register("silk_touch_bamboo", () -> SilkTouchModifier.CODEC);
    public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<FortuneModifier>> FORTUNE =
            GLM.register("fortune", () -> FortuneModifier.CODEC);
    public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<LootingModifier>> LOOTING =
            GLM.register("looting", () -> LootingModifier.CODEC);
    public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<SmeltingModifier>> SMELT =
            GLM.register("smelt", () -> SmeltingModifier.CODEC);
    public static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<VoidingModifier>> VOID =
            GLM.register("voiding", () -> VoidingModifier.CODEC);

    public static void register(IEventBus modBus) {
        GLM.register(modBus);
    }

    /**
     * Re-queries the loot table currently being resolved with a swapped-in tool stack, the same
     * trick 1.20.1's Fortune/Silk Touch modifiers used. Uses {@code getRandomItemsRaw} rather than
     * {@code getRandomItems} so this nested query does NOT run global loot modifiers again (avoids
     * re-entering these same modifiers in a loop) - the same precaution NeoForge's own
     * {@code AddTableLootModifier} takes when rolling a sub-table.
     */
    @SuppressWarnings("deprecation") // intentionally using the "Raw" variant - see the javadoc above
    private static ObjectArrayList<ItemStack> requeryWithTool(LootContext context, ItemStack fakeTool) {
        ServerLevel level = context.getLevel();
        ResourceLocation tableId = context.getQueriedLootTableId();
        ResourceKey<LootTable> tableKey = ResourceKey.create(Registries.LOOT_TABLE, tableId);
        Optional<LootTable> table = context.getResolver().get(Registries.LOOT_TABLE, tableKey).map(Holder.Reference::value);

        ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
        if (table.isEmpty()) {
            return result;
        }

        LootParams.Builder params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, context.getParam(LootContextParams.ORIGIN))
                .withParameter(LootContextParams.BLOCK_STATE, context.getParam(LootContextParams.BLOCK_STATE))
                .withParameter(LootContextParams.TOOL, fakeTool)
                .withOptionalParameter(LootContextParams.BLOCK_ENTITY, context.getParamOrNull(LootContextParams.BLOCK_ENTITY))
                .withOptionalParameter(LootContextParams.THIS_ENTITY, context.getParamOrNull(LootContextParams.THIS_ENTITY))
                .withLuck(context.getLuck());

        LootContext newContext = new LootContext.Builder(params.create(LootContextParamSets.BLOCK)).create(Optional.empty());
        table.get().getRandomItemsRaw(newContext, result::add);
        return result;
    }

    private static void decrement(ItemStack stack, DataComponentType<Integer> component, int uses) {
        uses--;
        if (uses <= 0) {
            stack.remove(component);
        } else {
            stack.set(component, uses);
        }
    }

    private static Holder<Enchantment> enchantment(ServerLevel level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(key);
    }

    /**
     * Ported from 1.20.1's {@code SilkTouchTestModifier} (registered under the id
     * {@code silk_touch_bamboo}, a leftover test name from upstream that this port keeps for 1:1
     * data-file parity). Re-rolls the block's own loot table with Silk Touch applied to a copy of
     * the tool, whenever the tool carries {@code ANOINTMENT_SILK_TOUCH_USES}.
     */
    public static class SilkTouchModifier extends LootModifier {
        public static final MapCodec<SilkTouchModifier> CODEC =
                RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, SilkTouchModifier::new));

        public SilkTouchModifier(LootItemCondition[] conditionsIn) {
            super(conditionsIn);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            ItemStack ctxTool = context.getParamOrNull(LootContextParams.TOOL);
            if (ctxTool == null || ctxTool.isEmpty() || ctxTool.is(BMTags.Items.CHARGES)) {
                return generatedLoot;
            }

            // Charges apply anointments through their own synthetic harvesting tool; skip BE drops
            // same as 1.20.1 did.
            if (context.getParamOrNull(LootContextParams.BLOCK_ENTITY) != null) {
                return generatedLoot;
            }

            ServerLevel level = context.getLevel();
            Holder<Enchantment> silkTouch = enchantment(level, Enchantments.SILK_TOUCH);
            if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, ctxTool) > 0) {
                // already silk-touched (e.g. a real Silk Touch book) - avoid recomputing forever.
                return generatedLoot;
            }

            int uses = ctxTool.getOrDefault(BMDataComponents.ANOINTMENT_SILK_TOUCH_USES.get(), 0);
            if (uses <= 0) {
                return generatedLoot;
            }

            ItemStack fakeTool = ctxTool.copy();
            EnchantmentHelper.updateEnchantments(fakeTool, mutable -> mutable.set(silkTouch, 1));

            ObjectArrayList<ItemStack> recomputed = requeryWithTool(context, fakeTool);
            decrement(ctxTool, BMDataComponents.ANOINTMENT_SILK_TOUCH_USES.get(), uses);
            return recomputed;
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
    }

    /**
     * Ported from 1.20.1's {@code FortuneModifier}. Cedes to Silk Touch when both anointments are
     * present on the same tool (matching the original's intent - silk touch and fortune are
     * mutually exclusive on a single break), then re-rolls the block's loot table with Fortune
     * boosted by the anointment's "uses" count.
     */
    public static class FortuneModifier extends LootModifier {
        public static final MapCodec<FortuneModifier> CODEC =
                RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, FortuneModifier::new));

        public FortuneModifier(LootItemCondition[] conditionsIn) {
            super(conditionsIn);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            ItemStack ctxTool = context.getParamOrNull(LootContextParams.TOOL);
            if (ctxTool == null || ctxTool.isEmpty() || ctxTool.is(BMTags.Items.CHARGES)) {
                return generatedLoot;
            }

            if (context.getParamOrNull(LootContextParams.BLOCK_ENTITY) != null) {
                return generatedLoot;
            }

            // Silk Touch (real enchant or anointment) takes priority over Fortune, same as 1.20.1.
            if (ctxTool.getOrDefault(BMDataComponents.ANOINTMENT_SILK_TOUCH_USES.get(), 0) > 0) {
                return generatedLoot;
            }

            ServerLevel level = context.getLevel();
            Holder<Enchantment> silkTouch = enchantment(level, Enchantments.SILK_TOUCH);
            if (EnchantmentHelper.getItemEnchantmentLevel(silkTouch, ctxTool) > 0) {
                return generatedLoot;
            }

            int uses = ctxTool.getOrDefault(BMDataComponents.ANOINTMENT_FORTUNE_USES.get(), 0);
            if (uses <= 0) {
                return generatedLoot;
            }

            Holder<Enchantment> fortune = enchantment(level, Enchantments.FORTUNE);
            int baseFortune = EnchantmentHelper.getItemEnchantmentLevel(fortune, ctxTool);

            ItemStack fakeTool = ctxTool.copy();
            EnchantmentHelper.updateEnchantments(fakeTool, mutable -> mutable.set(fortune, baseFortune + uses));

            ObjectArrayList<ItemStack> recomputed = requeryWithTool(context, fakeTool);
            decrement(ctxTool, BMDataComponents.ANOINTMENT_FORTUNE_USES.get(), uses);
            return recomputed;
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
    }

    /**
     * Ported from 1.20.1's {@code LootingModifier} - which, in upstream, was entirely dead code
     * (its whole body was commented out, unconditionally returning {@code generatedLoot}
     * unchanged). Kept as a faithful no-op for 1:1 data-file parity; the actually-working looting
     * bonus on this branch lives in {@code AnointmentEventHandler#onLivingDrops} (a
     * {@code LivingDropsEvent} hook, unrelated to this GLM).
     */
    public static class LootingModifier extends LootModifier {
        public static final MapCodec<LootingModifier> CODEC =
                RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, LootingModifier::new));

        public LootingModifier(LootItemCondition[] conditionsIn) {
            super(conditionsIn);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            return generatedLoot;
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
    }

    /**
     * Ported from 1.20.1's {@code SmeltingModifier}. Unlike Fortune/Silk Touch, this doesn't
     * re-roll the loot table - it maps whatever's already in {@code generatedLoot} (which, thanks
     * to global_loot_modifiers.json's ordering, already includes Fortune/Silk Touch's results)
     * through the furnace recipe manager, same as the original.
     */
    public static class SmeltingModifier extends LootModifier {
        public static final MapCodec<SmeltingModifier> CODEC =
                RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, SmeltingModifier::new));

        public SmeltingModifier(LootItemCondition[] conditionsIn) {
            super(conditionsIn);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            ItemStack ctxTool = context.getParamOrNull(LootContextParams.TOOL);
            if (ctxTool == null || ctxTool.isEmpty() || ctxTool.is(BMTags.Items.CHARGES)) {
                return generatedLoot;
            }

            int uses = ctxTool.getOrDefault(BMDataComponents.ANOINTMENT_SMELTING_USES.get(), 0);
            if (uses <= 0) {
                return generatedLoot;
            }

            ServerLevel level = context.getLevel();
            ObjectArrayList<ItemStack> smelted = new ObjectArrayList<>();
            for (ItemStack stack : generatedLoot) {
                smelted.add(smelt(stack, level));
            }

            decrement(ctxTool, BMDataComponents.ANOINTMENT_SMELTING_USES.get(), uses);
            return smelted;
        }

        private static ItemStack smelt(ItemStack stack, ServerLevel level) {
            return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
                    .map(holder -> {
                        ItemStack result = holder.value().getResultItem(level.registryAccess()).copy();
                        result.setCount(stack.getCount() * result.getCount());
                        return result;
                    })
                    .filter(result -> !result.isEmpty())
                    .orElse(stack);
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
    }

    /**
     * Ported from 1.20.1's {@code VoidingModifier}: only discards drops from blocks tagged
     * {@link BMTags.Blocks#MUNDANE_BLOCK}, so a Voiding anointment can't accidentally eat something
     * valuable.
     */
    public static class VoidingModifier extends LootModifier {
        public static final MapCodec<VoidingModifier> CODEC =
                RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, VoidingModifier::new));

        public VoidingModifier(LootItemCondition[] conditionsIn) {
            super(conditionsIn);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
            BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);
            if (blockState == null || !blockState.is(BMTags.Blocks.MUNDANE_BLOCK)) {
                return generatedLoot;
            }

            ItemStack ctxTool = context.getParamOrNull(LootContextParams.TOOL);
            if (ctxTool == null || ctxTool.isEmpty() || ctxTool.is(BMTags.Items.CHARGES)) {
                return generatedLoot;
            }

            int uses = ctxTool.getOrDefault(BMDataComponents.ANOINTMENT_VOIDING_USES.get(), 0);
            if (uses <= 0) {
                return generatedLoot;
            }

            decrement(ctxTool, BMDataComponents.ANOINTMENT_VOIDING_USES.get(), uses);
            return new ObjectArrayList<>();
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
    }
}

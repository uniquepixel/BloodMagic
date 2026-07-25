package wayoftime.bloodmagic.common.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import wayoftime.bloodmagic.api.BMIdentifiers;
import wayoftime.bloodmagic.common.datacomponent.BMDataComponents;
import wayoftime.bloodmagic.common.datacomponent.UpgradeTome;
import wayoftime.bloodmagic.common.living.LivingUpgrade;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Modern (codec-based) replacement for 1.20.1's {@code wayoftime.bloodmagic.common.loot.SetLivingUpgrade}.
 * Applied to a dropped {@link wayoftime.bloodmagic.common.item.UpgradeTomeItem} stack: picks one
 * {@link LivingUpgrade} at random out of a curated, per-loot-table candidate list (1.20.1 always
 * passed either a single upgrade, or - for the mines/smithy_loot table this consolidates - two
 * thematically related ones) and rolls an amount of stored exp from a {@link NumberProvider}
 * (1.20.1's {@code UniformGenerator} "points" range), then writes both into
 * {@link BMDataComponents#UPGRADE_TOME_DATA} as an {@link UpgradeTome}. Without this function
 * running, {@code UPGRADE_TOME_DATA} is left {@code null} and {@link
 * wayoftime.bloodmagic.common.item.UpgradeTomeItem#use} silently no-ops - see that class and {@code
 * wayoftime.bloodmagic.datagen.content.loot.DungeonChestLoot} for the call sites.
 * <p>
 * Dungeon-loot tomes are deliberately restricted to real, player-beneficial upgrades - never a
 * downgrade (1.20.1's dungeon loot tables never referenced any of its 9 "bad" living upgrades
 * (battle_hungry, crippled_arm, dig_slowdown, ...) via {@code SetLivingUpgrade}; every single
 * loot-table use passed one of the positive upgrade ids). Downgrade tomes on this branch are instead
 * only obtainable via the dedicated {@code downgrade_tome_*} Blood Altar recipes (see
 * {@code BMRecipeProvider}), which is a deliberate, deterministic crafting choice rather than random
 * loot.
 */
public class SetUpgradeTomeFunction extends LootItemConditionalFunction {
	public static final MapCodec<SetUpgradeTomeFunction> CODEC = RecordCodecBuilder.mapCodec(inst -> commonFields(inst)
			.and(inst.group(
					ResourceKey.codec(BMIdentifiers.RegistryKeys.LIVING_UPGRADES).listOf().fieldOf("upgrades").forGetter(f -> f.upgrades),
					NumberProviders.CODEC.fieldOf("exp").forGetter(f -> f.exp)
			))
			.apply(inst, SetUpgradeTomeFunction::new));

	private final List<ResourceKey<LivingUpgrade>> upgrades;
	private final NumberProvider exp;

	private SetUpgradeTomeFunction(List<LootItemCondition> conditions, List<ResourceKey<LivingUpgrade>> upgrades, NumberProvider exp) {
		super(conditions);
		this.upgrades = List.copyOf(upgrades);
		this.exp = exp;
	}

	@Override
	public LootItemFunctionType<SetUpgradeTomeFunction> getType() {
		return BMLootItemFunctions.SET_UPGRADE_TOME.get();
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return exp.getReferencedContextParams();
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext context) {
		if (upgrades.isEmpty()) {
			return stack;
		}

		ResourceKey<LivingUpgrade> chosen = upgrades.size() == 1 ? upgrades.get(0)
				: upgrades.get(context.getRandom().nextInt(upgrades.size()));

		ServerLevel level = context.getLevel();
		Holder<LivingUpgrade> holder = level.registryAccess().lookupOrThrow(BMIdentifiers.RegistryKeys.LIVING_UPGRADES).getOrThrow(chosen);
		float rolledExp = exp.getFloat(context);

		stack.set(BMDataComponents.UPGRADE_TOME_DATA.get(), new UpgradeTome(holder, rolledExp));
		return stack;
	}

	/**
	 * Datagen-side builder - see {@code DungeonChestLoot} for the call sites. Mirrors 1.20.1's
	 * {@code SetLivingUpgrade.withRange(UniformGenerator, ResourceLocation...)}: pass more than one
	 * candidate to have the actual upgrade picked randomly at loot-generation time (used only for
	 * {@code mines/smithy_loot}, which this branch consolidated from 1.20.1's two separate
	 * arrow_protect/physical_protect entries into one).
	 */
	@SafeVarargs
	public static LootItemConditionalFunction.Builder<?> setUpgradeTome(NumberProvider exp, ResourceKey<LivingUpgrade>... candidates) {
		List<ResourceKey<LivingUpgrade>> upgradeList = new ArrayList<>(List.of(candidates));
		return simpleBuilder(conditions -> new SetUpgradeTomeFunction(conditions, upgradeList, exp));
	}
}

package wayoftime.bloodmagic.common.loot;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wayoftime.bloodmagic.BloodMagic;

/**
 * Registry of this mod's custom {@link net.minecraft.world.level.storage.loot.functions.LootItemFunction}
 * types, following the exact same {@code DeferredRegister} convention as {@link BMLootModifiers}
 * (there registered on NeoForge's {@code GLOBAL_LOOT_MODIFIER_SERIALIZERS} registry; here on
 * vanilla's {@link Registries#LOOT_FUNCTION_TYPE}). Registered from {@code BloodMagic}'s constructor
 * alongside every other {@code DeferredRegister} - never read from a static bootstrap method, so
 * there's no risk of the "Trying to access unbound value" NPE that comes from calling
 * {@code DeferredHolder#get()} before {@code RegisterEvent} fires.
 */
public class BMLootItemFunctions {
	public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS =
			DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, BloodMagic.MODID);

	public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<SetUpgradeTomeFunction>> SET_UPGRADE_TOME =
			LOOT_FUNCTIONS.register("set_upgrade_tome", () -> new LootItemFunctionType<>(SetUpgradeTomeFunction.CODEC));

	public static void register(IEventBus modBus) {
		LOOT_FUNCTIONS.register(modBus);
	}
}

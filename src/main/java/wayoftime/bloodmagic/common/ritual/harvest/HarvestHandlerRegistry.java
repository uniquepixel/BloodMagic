package wayoftime.bloodmagic.common.ritual.harvest;

import java.util.ArrayList;
import java.util.List;

/**
 * Static registry of {@link IHarvestHandler}s consulted by
 * {@link wayoftime.bloodmagic.common.ritual.types.HarvestRitual}, populated once via
 * {@link #bootstrap()} - the same "static list populated at startup" pattern
 * {@code wayoftime.bloodmagic.common.ritual.RitualRegistry} uses for rituals. Ported from 1.20.1's
 * {@code HarvestRegistry} - the range-amplifier and per-block crop/tall-crop/stem-crop registration
 * maps aren't ported, since every handler here recognizes its vanilla blocks directly instead of
 * needing addon mods to register lookup entries into a shared table.
 */
public class HarvestHandlerRegistry {
	private static final List<IHarvestHandler> HANDLERS = new ArrayList<>();

	public static void bootstrap() {
		register(new HarvestHandlerCrop());
		register(new HarvestHandlerStem());
		register(new HarvestHandlerTallPlant());
		register(new HarvestHandlerNetherWart());
		register(new HarvestHandlerBerryBush());
		register(new HarvestHandlerGrowingPlant());
		register(new HarvestHandlerVine());

		// 1.20.1 also registered a HarvestHandlerAgricraft (hooking AgriCraft's CropBlockEntity) when
		// the "agricraft" mod was loaded. AgriCraft isn't a dependency of this branch (no reference in
		// build.gradle), so there's no CropBlockEntity API to hook into - skipped, not deleted.
	}

	public static void register(IHarvestHandler handler) {
		if (!HANDLERS.contains(handler)) {
			HANDLERS.add(handler);
		}
	}

	public static List<IHarvestHandler> getHandlers() {
		return List.copyOf(HANDLERS);
	}
}

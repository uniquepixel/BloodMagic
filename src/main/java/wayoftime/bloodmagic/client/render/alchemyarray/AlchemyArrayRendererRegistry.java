package wayoftime.bloodmagic.client.render.alchemyarray;

import net.minecraft.resources.ResourceLocation;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.recipe.array.AlchemyArrayRecipe;

import java.util.HashMap;
import java.util.Map;

/**
 * Ported from 1.20.1's {@code AlchemyArrayRendererRegistry} (which keyed its explicit overrides
 * off array *texture*, only special-casing Binding that way) and the renderer registrations
 * 1.20.1's {@code ClientEvents} made for movement/updraft/spike/day/night/bounce.
 * <p>
 * This branch keys everything off recipe id instead, matching the precedent
 * {@code AlchemyArrayEffects} (the server-side effect-dispatch equivalent) already established:
 * a recipe's array texture can be shared/reused across multiple recipes (1.20.1 itself reused
 * {@code sightsigil.png} for both the Seer and Holding sigils), so it isn't a reliable unique key
 * the way the recipe id always is.
 * <p>
 * Recipes with no explicit entry here fall back to a plain {@link AlchemyCircleRenderer} using
 * whatever texture the matched {@code AlchemyArrayRecipe} declares (or
 * {@link AlchemyArrayRecipe#DEFAULT_TEXTURE} if nothing matched at all) - this covers every
 * sigil-from-reagent recipe datagenned in {@code BMRecipeProvider}.
 */
public class AlchemyArrayRendererRegistry {
    public static final AlchemyCircleRenderer DEFAULT_RENDERER = new AlchemyCircleRenderer(AlchemyArrayRecipe.DEFAULT_TEXTURE);

    private static final Map<ResourceLocation, AlchemyCircleRenderer> BY_RECIPE_ID = new HashMap<>();

    static {
        register("array/movement", new StaticAlchemyCircleRenderer(texture("movementarray.png")));
        register("array/updraft", new BeaconAlchemyCircleRenderer(texture("updraftarray.png")));
        register("array/spike", new LowStaticAlchemyCircleRenderer(texture("spikearray.png")));
        register("array/day", new DayAlchemyCircleRenderer(texture("sunarray.png"), texture("sunarrayspikes.png"), texture("sunarraycircle.png")));
        register("array/night", new NightAlchemyCircleRenderer(texture("moonarrayoutside.png"), texture("moonarraysymbols.png"), texture("moonarrayinside.png")));
        register("array/bounce", new LowStaticAlchemyCircleRenderer(texture("bouncearray.png")));

        // All 5 of 1.20.1's living_* Binding arrays share one renderer instance, exactly like
        // 1.20.1 (which keyed all of them off the single AlchemyArrayRegistry.BINDING_ARRAY
        // texture value).
        BindingAlchemyCircleRenderer binding = new BindingAlchemyCircleRenderer();
        register("array/living_helmet", binding);
        register("array/living_plate", binding);
        register("array/living_leggings", binding);
        register("array/living_boots", binding);
        register("array/living_trainer", binding);
    }

    private static ResourceLocation texture(String fileName) {
        return BloodMagic.rl("textures/models/alchemyarrays/" + fileName);
    }

    private static void register(String recipePath, AlchemyCircleRenderer renderer) {
        BY_RECIPE_ID.put(BloodMagic.rl(recipePath), renderer);
    }

    /**
     * @param recipeId the matched recipe's id, or {@code null} if no recipe currently matches the
     *                 array's contents.
     * @param recipe   the matched recipe, or {@code null}.
     */
    public static AlchemyCircleRenderer getRenderer(ResourceLocation recipeId, AlchemyArrayRecipe recipe) {
        if (recipeId != null) {
            AlchemyCircleRenderer explicit = BY_RECIPE_ID.get(recipeId);
            if (explicit != null) {
                return explicit;
            }
        }

        if (recipe != null) {
            return new AlchemyCircleRenderer(recipe.getTexture());
        }

        return DEFAULT_RENDERER;
    }
}

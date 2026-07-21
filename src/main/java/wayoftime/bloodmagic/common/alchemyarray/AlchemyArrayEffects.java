package wayoftime.bloodmagic.common.alchemyarray;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.common.recipe.array.AlchemyArrayRecipe;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Ported from 1.20.1's {@code AlchemyArrayRegistry}: resolves which {@link AlchemyArrayEffect}
 * an {@code AlchemyArrayRecipe} drives once its base/added inputs are matched.
 * <p>
 * Six of the eight special, non-simple-crafting effects (movement/updraft/spike/day/night/
 * bounce) are keyed off a fixed recipe id here, exactly like 1.20.1 keyed them off
 * {@code array/<name>} recipe ids in {@code AlchemyArrayRegistry.registerBaseArrays()}. The
 * seventh, Binding, was originally picked by checking the recipe's array *texture* rather than
 * its id ({@code AlchemyArrayRegistry#BINDING_ARRAY}) - the {@code AlchemyArrayRecipe} format on
 * this branch has no texture field (no per-recipe array art has been ported yet), so Binding is
 * keyed off a set of recipe ids here instead, the same mechanism used for the other six.
 * <p>
 * Everything else (anything not in {@link #SPECIAL_EFFECTS} or {@link #BINDING_IDS}) falls back
 * to the plain {@link AlchemyArrayEffectCrafting}, using the recipe's declared output - this is
 * what drives all of the existing sigil-from-reagent array recipes datagenned in
 * {@code BMRecipeProvider}.
 */
public class AlchemyArrayEffects {
    private static final Map<ResourceLocation, Supplier<AlchemyArrayEffect>> SPECIAL_EFFECTS = Map.of(
            BloodMagic.rl("array/movement"), AlchemyArrayEffectMovement::new,
            BloodMagic.rl("array/updraft"), AlchemyArrayEffectUpdraft::new,
            BloodMagic.rl("array/spike"), AlchemyArrayEffectSpike::new,
            BloodMagic.rl("array/day"), AlchemyArrayEffectDay::new,
            BloodMagic.rl("array/night"), AlchemyArrayEffectNight::new,
            BloodMagic.rl("array/bounce"), AlchemyArrayEffectBounce::new
    );

    // No recipes are datagenned against these ids yet - Living armor's Binding array requires a
    // "reagent_binding" item that doesn't exist on this branch (BMItems.java is out of scope for
    // this port), so these recipes can't be added without a new item. The dispatch is wired up
    // and ready for whenever that item lands.
    private static final Set<ResourceLocation> BINDING_IDS = Set.of(
            BloodMagic.rl("array/living_helmet"),
            BloodMagic.rl("array/living_plate"),
            BloodMagic.rl("array/living_leggings"),
            BloodMagic.rl("array/living_boots"),
            BloodMagic.rl("array/living_trainer")
    );

    public static AlchemyArrayEffect getEffect(RecipeHolder<AlchemyArrayRecipe> holder) {
        ResourceLocation id = holder.id();

        Supplier<AlchemyArrayEffect> special = SPECIAL_EFFECTS.get(id);
        if (special != null) {
            return special.get();
        }

        if (BINDING_IDS.contains(id)) {
            return new AlchemyArrayEffectBinding(holder.value().getOutput().copy());
        }

        return new AlchemyArrayEffectCrafting(holder.value().getOutput().copy());
    }
}

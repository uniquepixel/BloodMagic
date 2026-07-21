package wayoftime.bloodmagic.common.meteor;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Replaces 1.20.1's datapack-driven meteor recipe system ({@code RecipeMeteor} /
 * {@code MeteorRecipeSerializer} / {@code BloodMagicRecipeType.METEOR}, populated via
 * {@code MeteorRecipeProvider} + {@code MeteorRecipeBuilder} into {@code data/bloodmagic/recipes/meteor/*.json}).
 * <p>
 * That whole chain - a custom {@code RecipeType}/{@code RecipeSerializer} registered through this
 * branch's not-yet-ported recipe-registry infrastructure, plus JSON (de)serialization and network sync
 * for every {@link MeteorLayer}/container just so datapacks could redefine meteor payloads - is
 * disproportionate to port for what is fundamentally "which item throws which meteor," so per the
 * task's explicit allowance this is simplified to a fixed, code-defined payload list instead. Datapacks
 * can no longer add/edit meteor payloads; anyone wanting that back would need to reintroduce the
 * recipe-type registration this branch is missing.
 * <p>
 * The payloads below are new but deliberately mirror a representative slice of the originals in
 * {@code MeteorRecipeProvider} (iron/stone/diamond/nether) plus one using a fluid fill/shell (the
 * commented-out "ice" example from the original provider), so all three {@link RandomBlockContainer}
 * strategies ({@link StaticBlockContainer}, {@link RandomBlockTagContainer}, {@link FluidBlockContainer})
 * and {@link MeteorLayer}'s shell/weight/multi-layer nesting are exercised. Ore variety is drawn from
 * NeoForge's common {@code c:} block tags (this branch has no {@code BloodMagicTags} ore-dictionary
 * equivalent of the original's per-mod ore tags, e.g. tin/silver/lead), same substitution
 * {@link wayoftime.bloodmagic.common.ritual.types.GeodeRitual} already makes elsewhere.
 */
public class MeteorDefinitions {
    private static final List<MeteorDefinition> DEFINITIONS = List.of(
            // Iron meteor: an iron-ore-cored inner sphere behind a cobblestone shell, a wider outer
            // sphere of stone sprinkled with iron/copper/coal.
            new MeteorDefinition(Ingredient.of(Tags.Items.STORAGE_BLOCKS_IRON), 200_000, 6f, List.of(
                    new MeteorLayer(3, 0, Blocks.IRON_ORE)
                            .addShellBlock(Tags.Blocks.COBBLESTONES)
                            .addWeightedBlock(Blocks.GOLD_ORE, 30)
                            .addWeightedTag(Tags.Blocks.ORES_COPPER, 150)
                            .addWeightedTag(Tags.Blocks.ORES_REDSTONE, 60)
                            .addWeightedTag(Tags.Blocks.ORES_LAPIS, 50),
                    new MeteorLayer(6, 100, Blocks.STONE)
                            .setMinWeight(500)
                            .addWeightedBlock(Blocks.IRON_ORE, 300)
                            .addWeightedTag(Tags.Blocks.ORES_COPPER, 150)
                            .addWeightedTag(Tags.Blocks.ORES_COAL, 150)
            )),

            // Stone meteor: big, mostly-stone sphere with a cobblestone shell and low-value ore
            // sprinkled through it, roughly equivalent to mining at high y-levels.
            new MeteorDefinition(Ingredient.of(Items.STONE), 150_000, 10f, List.of(
                    new MeteorLayer(10, 0, Blocks.STONE)
                            .setMinWeight(300)
                            .addShellBlock(Tags.Blocks.COBBLESTONES)
                            .addWeightedTag(Tags.Blocks.ORES_COAL, 150)
                            .addWeightedTag(Tags.Blocks.ORES_IRON, 60)
                            .addWeightedTag(Tags.Blocks.ORES_REDSTONE, 40)
            )),

            // Diamond meteor: tiny solid-diamond-ore core, wider outer core of assorted gem ores.
            new MeteorDefinition(Ingredient.of(Tags.Items.GEMS_DIAMOND), 750_000, 6f, List.of(
                    new MeteorLayer(2, 0, Blocks.DIAMOND_ORE),
                    new MeteorLayer(5, 0, Blocks.COBBLESTONE)
                            .setMinWeight(600)
                            .addWeightedTag(Tags.Blocks.ORES_DIAMOND, 100)
                            .addWeightedTag(Tags.Blocks.ORES_EMERALD, 80)
                            .addWeightedTag(Tags.Blocks.ORES_QUARTZ, 60)
            )),

            // Nether meteor: netherrack outer shell with glowstone/quartz/nether gold, inner
            // blackstone core with a shot at ancient debris.
            new MeteorDefinition(Ingredient.of(Items.GLOWSTONE_DUST), 400_000, 8f, List.of(
                    new MeteorLayer(7, 0, Blocks.NETHERRACK)
                            .setMinWeight(400)
                            .addWeightedBlock(Blocks.GLOWSTONE, 100)
                            .addWeightedBlock(Blocks.NETHER_QUARTZ_ORE, 120)
                            .addWeightedBlock(Blocks.NETHER_GOLD_ORE, 60),
                    new MeteorLayer(4, 0, Blocks.BLACKSTONE)
                            .addShellBlock(Blocks.GLOWSTONE)
                            .setMinWeight(600)
                            .addWeightedBlock(Blocks.ANCIENT_DEBRIS, 50)
                            .addWeightedBlock(Blocks.GILDED_BLACKSTONE, 150)
                            .addWeightedTag(Tags.Blocks.ORES_NETHERITE_SCRAP, 60)
            )),

            // Ice meteor: no explosion, a water-filled pocket shelled in ice - exercises
            // FluidBlockContainer as both the layer fill and a weighted alternative.
            new MeteorDefinition(Ingredient.of(Items.ICE), 50_000, 0f, List.of(
                    new MeteorLayer(5, 0, Fluids.WATER)
                            .addShellBlock(Blocks.ICE)
                            .addWeightedFluid(Fluids.WATER, 30)
            ))
    );

    private MeteorDefinitions() {
    }

    /**
     * Finds the first meteor payload whose input ingredient matches the given stack, mirroring the
     * original's {@code BloodMagicAPI.getRecipeRegistrar().getMeteor(world, stack)} lookup (first
     * match wins there too).
     */
    @Nullable
    public static MeteorDefinition pickFor(ItemStack stack) {
        for (MeteorDefinition definition : DEFINITIONS) {
            if (definition.matches(stack)) {
                return definition;
            }
        }
        return null;
    }
}

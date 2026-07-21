package wayoftime.bloodmagic.datagen.provider;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.api.BMIdentifiers;
import wayoftime.bloodmagic.api.BMIdentifiers.Sigils;
import wayoftime.bloodmagic.api.sigil.SigilEffect;
import wayoftime.bloodmagic.common.datacomponent.EnumWillType;
import wayoftime.bloodmagic.common.item.BMItems;

import java.util.Set;
import java.util.function.Supplier;

public class BMItemModelProvider extends ItemModelProvider {
    // See registerModels()'s comment on the L/XL anointment tiers for why these are excluded from
    // the generic BASIC_ITEMS loop and textured explicitly instead.
    private static final Set<Item> ANOINTMENT_TIER_ITEMS = Set.of(
            BMItems.ANOINTMENT_MELEE_DAMAGE_L.get(), BMItems.ANOINTMENT_MELEE_DAMAGE_XL.get(),
            BMItems.ANOINTMENT_LOOTING_L.get(), BMItems.ANOINTMENT_LOOTING_XL.get(),
            BMItems.ANOINTMENT_BOW_POWER_L.get(), BMItems.ANOINTMENT_BOW_POWER_XL.get(),
            BMItems.ANOINTMENT_BOW_VELOCITY_L.get(), BMItems.ANOINTMENT_BOW_VELOCITY_XL.get(),
            BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE_L.get(), BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE_XL.get(),
            BMItems.ANOINTMENT_HOLY_WATER_L.get(), BMItems.ANOINTMENT_HOLY_WATER_XL.get(),
            BMItems.ANOINTMENT_QUICK_DRAW_L.get(), BMItems.ANOINTMENT_QUICK_DRAW_XL.get(),
            BMItems.ANOINTMENT_SILK_TOUCH_L.get(), BMItems.ANOINTMENT_SILK_TOUCH_XL.get(),
            BMItems.ANOINTMENT_FORTUNE_L.get(), BMItems.ANOINTMENT_FORTUNE_XL.get(),
            BMItems.ANOINTMENT_SMELTING_L.get(), BMItems.ANOINTMENT_SMELTING_XL.get(),
            BMItems.ANOINTMENT_VOIDING_L.get(), BMItems.ANOINTMENT_VOIDING_XL.get(),
            BMItems.ANOINTMENT_WEAPON_REPAIR_L.get(), BMItems.ANOINTMENT_WEAPON_REPAIR_XL.get());

    public BMItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BloodMagic.MODID, existingFileHelper);
    }

    private void reuseTexture(Item item, String texturePath) {
        String path = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        getBuilder(path)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("item/" + texturePath));
    }

    @Override
    protected void registerModels() {
        // reagent_binding has no dedicated texture yet, so it's excluded from the generic loop and
        // instead reuses reagent_holding's texture as a placeholder below. The L/XL anointment tiers
        // are excluded the same way - 1.20.1 told them apart with composited vial/ribbon overlays
        // this branch never ported (see BMItems for why), so each tier reuses its own family's
        // tier-1 icon instead of going textureless.
        BMItems.BASIC_ITEMS.getEntries().stream().map(Supplier::get)
                .filter(item -> item != BMItems.REAGENT_BINDING.get())
                .filter(item -> !ANOINTMENT_TIER_ITEMS.contains(item))
                .forEach(this::basicItem);
        getBuilder(BMItems.REAGENT_BINDING.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("item/reagent_holding"));
        reuseTexture(BMItems.ANOINTMENT_MELEE_DAMAGE_L.get(), "anointment_melee_damage");
        reuseTexture(BMItems.ANOINTMENT_MELEE_DAMAGE_XL.get(), "anointment_melee_damage");
        reuseTexture(BMItems.ANOINTMENT_LOOTING_L.get(), "anointment_looting");
        reuseTexture(BMItems.ANOINTMENT_LOOTING_XL.get(), "anointment_looting");
        reuseTexture(BMItems.ANOINTMENT_BOW_POWER_L.get(), "anointment_bow_power");
        reuseTexture(BMItems.ANOINTMENT_BOW_POWER_XL.get(), "anointment_bow_power");
        reuseTexture(BMItems.ANOINTMENT_BOW_VELOCITY_L.get(), "anointment_bow_velocity");
        reuseTexture(BMItems.ANOINTMENT_BOW_VELOCITY_XL.get(), "anointment_bow_velocity");
        reuseTexture(BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE_L.get(), "anointment_hidden_knowledge");
        reuseTexture(BMItems.ANOINTMENT_HIDDEN_KNOWLEDGE_XL.get(), "anointment_hidden_knowledge");
        reuseTexture(BMItems.ANOINTMENT_HOLY_WATER_L.get(), "anointment_holy_water");
        reuseTexture(BMItems.ANOINTMENT_HOLY_WATER_XL.get(), "anointment_holy_water");
        reuseTexture(BMItems.ANOINTMENT_QUICK_DRAW_L.get(), "anointment_quick_draw");
        reuseTexture(BMItems.ANOINTMENT_QUICK_DRAW_XL.get(), "anointment_quick_draw");
        reuseTexture(BMItems.ANOINTMENT_SILK_TOUCH_L.get(), "anointment_silk_touch");
        reuseTexture(BMItems.ANOINTMENT_SILK_TOUCH_XL.get(), "anointment_silk_touch");
        reuseTexture(BMItems.ANOINTMENT_FORTUNE_L.get(), "anointment_fortune");
        reuseTexture(BMItems.ANOINTMENT_FORTUNE_XL.get(), "anointment_fortune");
        reuseTexture(BMItems.ANOINTMENT_SMELTING_L.get(), "anointment_smelting");
        reuseTexture(BMItems.ANOINTMENT_SMELTING_XL.get(), "anointment_smelting");
        reuseTexture(BMItems.ANOINTMENT_VOIDING_L.get(), "anointment_voiding");
        reuseTexture(BMItems.ANOINTMENT_VOIDING_XL.get(), "anointment_voiding");
        reuseTexture(BMItems.ANOINTMENT_WEAPON_REPAIR_L.get(), "anointment_weapon_repair");
        reuseTexture(BMItems.ANOINTMENT_WEAPON_REPAIR_XL.get(), "anointment_weapon_repair");
        basicItem(BMItems.LIVING_PLATE.get());
        basicItem(BMItems.UPGRADE_TOME.get());
        basicItem(BMItems.SIGIL_HOLDING.get());
        basicItem(BMItems.THROWING_DAGGER.get());
        basicItem(BMItems.THROWING_DAGGER_SYRINGE.get());
        basicItem(BMItems.SOUL_SNARE.get());
        // Debug-only tool (see ItemDungeonTester's javadoc) - 1.20.1 never gave it dedicated art
        // either, so this reuses the dungeon key icon as a placeholder (same "no upstream art exists"
        // reuse convention as reagent_binding below).
        reuseTexture(BMItems.DUNGEON_TESTER.get(), "simplekey");
        // The Sentient Armour Gem needs a second, orthogonal dimension (activated/deactivated) on
        // top of the plain per-Will-type variant every other WILL_ITEMS entry gets, so it's built
        // separately below rather than through this generic loop.
        BMItems.WILL_ITEMS.getEntries().stream()
                .filter(item -> item.get() != BMItems.SENTIENT_ARMOUR_GEM.get())
                .forEach(item -> {
                    String path = item.getId().getPath();
                    ItemModelBuilder builder = getBuilder(path);
                    for (EnumWillType type : EnumWillType.values()) {
                        ModelFile modelFile = singleTexture(String.format("item/variant/%s_%s", path, type.getSerializedName()), mcLoc("item/handheld"), "layer0", modLoc(String.format("item/%s_%s", path, type.getSerializedName())));
                        builder.override().predicate(BloodMagic.TYPE_PROPERTY, type.ordinal()).model(modelFile).end();
                    }
                });
        createSentientArmourGemModel();

        ItemModelBuilder builder = getBuilder(BMItems.SACRIFICIAL_DAGGER.getId().getPath());
        ModelFile normalDagger = singleTexture("item/variant/sacrificial_dagger_normal", mcLoc("item/handheld"), "layer0", modLoc("item/sacrificial_dagger"));
        ModelFile chargedDagger = singleTexture("item/variant/sacrificial_dagger_charged", mcLoc("item/handheld"), "layer0", modLoc("item/sacrificial_dagger_charged"));
        builder.override().predicate(BloodMagic.INCENSE_PROPERTY, 0).model(normalDagger).end();
        builder.override().predicate(BloodMagic.INCENSE_PROPERTY, 1).model(chargedDagger).end();

        createSigilModels();
        createFlaskModels();

        // Item Routing "Filter" system items have no dedicated art yet, so - matching the
        // reagent_binding/DUNGEON_TESTER convention above - each reuses one of the existing Slate
        // icons as a placeholder.
        reuseTexture(BMItems.STANDARD_FILTER.get(), "slate_blank");
        reuseTexture(BMItems.TAG_FILTER.get(), "slate_imbued");
        reuseTexture(BMItems.MOD_FILTER.get(), "slate_demonic");
        reuseTexture(BMItems.ENCHANT_FILTER.get(), "slate_ethereal");
        reuseTexture(BMItems.COMPOSITE_FILTER.get(), "slate_reinforced");
    }

    // Alchemy Flask items - 3-layer generated models (tinted liquid, untinted outline, tinted
    // highlight) matching 1.20.1's registerMultiLayerItem setup; tint is supplied by FlaskColor.
    private void createFlaskModels() {
        createFlaskModel(BMItems.ALCHEMY_FLASK.getId().getPath(), "potionflask_outline", "potionflask_overlay");
        createFlaskModel(BMItems.ALCHEMY_FLASK_THROWABLE.getId().getPath(), "potionflask_outline_throwable", "potionflask_overlay_throwable");
        createFlaskModel(BMItems.ALCHEMY_FLASK_LINGERING.getId().getPath(), "potionflask_outline_lingering", "potionflask_overlay_lingering");
    }

    private void createFlaskModel(String path, String outlineTexture, String overlayTexture) {
        getBuilder(path)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("item/potionflask_underlay"))
                .texture("layer1", modLoc("item/" + outlineTexture))
                .texture("layer2", modLoc("item/" + overlayTexture));
    }

    private void createSigilModels() {
        createStandard(Sigils.DIVINATION);
        createStandard(Sigils.SEER);
        createStandard(Sigils.LAVA);
        createStandard(Sigils.WATER);
        createStandard(Sigils.VOID);
        createToggle(Sigils.MINER);
        createStandard(Sigils.AIR);
        createToggle(Sigils.ICE);
        createToggle(Sigils.GROWTH);
        createToggle(Sigils.MAGNETISM);
        createStandard(Sigils.BLOODLIGHT);
        createToggle(Sigils.SUPPRESSION);
        createStandard(Sigils.TELEPOSITION);
    }

    private void createStandard(ResourceKey<SigilEffect> key) {
        String path = key.location().getPath();
        getBuilder("sigil_" + path)
                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                .texture("layer0", modLoc("item/sigil_" + path));
    }

    private void createToggle(ResourceKey<SigilEffect> key) {
        String path = key.location().getPath();
        getBuilder("sigil_" + path)
                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                .texture("layer0", modLoc("item/sigil_" + path + "_deactivated"))
                .override().predicate(BMIdentifiers.ItemProperties.SIGIL_ACTIVE, 1)
                .model(singleTexture("item/sigil_" + path + "_activated", mcLoc("item/handheld"), "layer0", modLoc("item/sigil_" + path + "_activated"))).end();
    }

    // Sentient Armour Gem: 5 Will types x 2 activation states = 10 icon variants, one dimension more
    // than the plain per-type WILL_ITEMS loop above handles. Overrides are matched by scanning in
    // list order and keeping the last entry whose predicates are all satisfied (each predicate is an
    // ">=" threshold, not an exact match) - the same trick the per-type-only loop above already
    // relies on with its ascending ordinals, just nested two levels deep here so the combination of
    // (type, active) that's actually reached last in an ascending scan is always the exact match.
    private void createSentientArmourGemModel() {
        String path = BMItems.SENTIENT_ARMOUR_GEM.getId().getPath();
        ItemModelBuilder builder = getBuilder(path)
                .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                .texture("layer0", modLoc("item/" + path + "_default_deactivated"));

        for (EnumWillType type : EnumWillType.values()) {
            for (boolean active : new boolean[]{false, true}) {
                String suffix = type.getSerializedName() + "_" + (active ? "activated" : "deactivated");
                ModelFile modelFile = singleTexture("item/variant/" + path + "_" + suffix, mcLoc("item/handheld"), "layer0", modLoc("item/" + path + "_" + suffix));
                builder.override()
                        .predicate(BloodMagic.TYPE_PROPERTY, type.ordinal())
                        .predicate(BMIdentifiers.ItemProperties.SENTIENT_GEM_ACTIVE, active ? 1 : 0)
                        .model(modelFile)
                        .end();
            }
        }
    }
}

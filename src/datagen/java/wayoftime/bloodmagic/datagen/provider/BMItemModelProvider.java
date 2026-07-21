package wayoftime.bloodmagic.datagen.provider;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
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

import java.util.function.Supplier;

public class BMItemModelProvider extends ItemModelProvider {
    public BMItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, BloodMagic.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        // reagent_binding has no dedicated texture yet, so it's excluded from the generic loop and
        // instead reuses reagent_holding's texture as a placeholder below.
        BMItems.BASIC_ITEMS.getEntries().stream().map(Supplier::get)
                .filter(item -> item != BMItems.REAGENT_BINDING.get())
                .forEach(this::basicItem);
        getBuilder(BMItems.REAGENT_BINDING.getId().getPath())
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("item/reagent_holding"));
        basicItem(BMItems.LIVING_PLATE.get());
        basicItem(BMItems.UPGRADE_TOME.get());
        basicItem(BMItems.SIGIL_HOLDING.get());
        basicItem(BMItems.THROWING_DAGGER.get());
        basicItem(BMItems.SOUL_SNARE.get());
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

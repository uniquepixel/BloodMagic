package wayoftime.bloodmagic.datagen.provider;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import wayoftime.bloodmagic.BloodMagic;
import wayoftime.bloodmagic.api.BMTags;
import wayoftime.bloodmagic.api.altar.AltarTier;
import wayoftime.bloodmagic.api.ritual.EnumRuneType;
import wayoftime.bloodmagic.api.ritual.Ritual;
import wayoftime.bloodmagic.api.ritual.RitualComponent;
import wayoftime.bloodmagic.common.ritual.RitualRegistry;
import wayoftime.bloodmagic.datagen.content.AltarTiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Generates {@code modopedia:multiblock} resources (under {@code assets/bloodmagic/modopedia/multiblocks/}) for
 * every ritual and every Blood Altar tier, so the Modopedia guidebook can render a 3D ghost-block preview of each
 * structure - the 1.21.1 replacement for 1.20.1's {@code RegisterPatchouliMultiblocks}.
 * <p>
 * Unlike 1.20.1's Patchouli, Modopedia (this branch's Patchouli-alike book library, see {@code modopedia_version}
 * in gradle.properties) has no runtime Java {@code IMultiblock} registration API. Its multiblocks are plain
 * datapack JSON, loaded automatically by a client-only reload listener (Modopedia's
 * {@code book.loading.MultiblockLoader}) exactly like this mod's book categories/entries already are - no
 * bootstrap call is needed anywhere at mod init for these to show up in-game.
 * <p>
 * The JSON is generated here, rather than hand-written, so it always matches the <em>live</em> layout data:
 * <ul>
 *     <li>Ritual patterns are read straight from {@link Ritual#gatherComponents} (the same rune-offset data the
 *     real Master Ritual Stone validates against), so a ritual's layout changing in code and re-running
 *     {@code runData} automatically updates its book preview.</li>
 *     <li>Blood Altar tiers are read from the same {@link AltarTier} field values in {@link AltarTiers} used to
 *     generate the {@code altar_tier} datapack registry entries themselves (see {@link AltarTiers#tiers}), and
 *     the ring/pillar/capstone geometry below mirrors {@code wayoftime.bloodmagic.util.AltarUtil#getTier}
 *     exactly, so the preview always matches what the real altar tier-up check requires.</li>
 * </ul>
 */
public class BMMultiblockProvider implements DataProvider {

    private static final char ALTAR_CHAR = '0';
    private static final char RUNE_CHAR = 'R';
    private static final char PILLAR_CHAR = 'P';

    /** Ascending order, matching {@code AltarUtil.TIERS} (sorted by {@link AltarTier#tier()}). */
    private static final List<AltarTier> ALTAR_TIERS = List.of(
            AltarTiers.APPRENTICE, AltarTiers.MAGE, AltarTiers.MASTER, AltarTiers.ARCHMAGE, AltarTiers.TRANSCENDENT
    );

    private final PackOutput.PathProvider pathProvider;

    public BMMultiblockProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "modopedia/multiblocks");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Ritual ritual : RitualRegistry.all()) {
            futures.add(save(output, ritualId(ritual), buildRitualMultiblock(ritual)));
        }

        for (int tier = 1; tier <= 6; tier++) {
            futures.add(save(output, altarId(tier), buildAltarMultiblock(tier)));
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Blood Magic Modopedia Multiblocks";
    }

    private CompletableFuture<?> save(CachedOutput output, ResourceLocation id, JsonObject json) {
        return DataProvider.saveStable(output, json, pathProvider.json(id));
    }

    private static ResourceLocation ritualId(Ritual ritual) {
        return BloodMagic.rl("ritual/" + ritual.getId().getPath());
    }

    private static ResourceLocation altarId(int tier) {
        return BloodMagic.rl("altar/tier_" + tier);
    }

    // ---- Rituals ----

    private JsonObject buildRitualMultiblock(Ritual ritual) {
        List<RitualComponent> components = new ArrayList<>();
        ritual.gatherComponents(components::add);

        Map<BlockPos, Character> layout = new HashMap<>();
        Map<Character, JsonObject> key = new HashMap<>();

        layout.put(BlockPos.ZERO, ALTAR_CHAR);
        key.put(ALTAR_CHAR, simpleMatcher("bloodmagic:ritual_stone_master"));

        for (RitualComponent component : components) {
            char c = runeChar(component.getRuneType());
            layout.put(component.getOffset(), c);
            key.putIfAbsent(c, simpleMatcher("bloodmagic:" + runeBlockPath(component.getRuneType())));
        }

        return buildDenseMultiblock(layout, key);
    }

    private static char runeChar(EnumRuneType type) {
        return switch (type) {
            case BLANK -> 'B';
            case WATER -> 'W';
            case FIRE -> 'F';
            case EARTH -> 'E';
            case AIR -> 'A';
            case DUSK -> 'D';
            case DAWN -> 'd';
        };
    }

    private static String runeBlockPath(EnumRuneType type) {
        return "ritual_stone_" + type.getSerializedName();
    }

    // ---- Blood Altar ----

    private JsonObject buildAltarMultiblock(int tier) {
        Map<BlockPos, Character> layout = new HashMap<>();
        Map<Character, JsonObject> key = new HashMap<>();

        layout.put(BlockPos.ZERO, ALTAR_CHAR);
        key.put(ALTAR_CHAR, simpleMatcher("bloodmagic:blood_altar"));

        if (tier >= 2) {
            key.put(RUNE_CHAR, tagMatcher(BMTags.Blocks.RUNES));
            key.put(PILLAR_CHAR, tagMatcher(BMTags.Blocks.PILLARS));

            // Mirrors wayoftime.bloodmagic.util.AltarUtil#getTier, run forwards to build the full cumulative
            // structure for `tier` instead of backwards to validate one already placed in the world.
            BlockPos centerPos = BlockPos.ZERO.below();
            int distance = 0;

            for (AltarTier current : ALTAR_TIERS) {
                if (current.tier() > tier) {
                    break;
                }
                distance += current.distance();

                for (Direction side : Direction.Plane.HORIZONTAL) {
                    BlockPos centerRune = centerPos.relative(side, distance);
                    layout.put(centerRune, RUNE_CHAR);

                    for (int j = 0; j < current.sideRunes(); j++) {
                        layout.put(centerRune.relative(side.getClockWise(), 1 + j), RUNE_CHAR);
                        layout.put(centerRune.relative(side.getCounterClockWise(), 1 + j), RUNE_CHAR);
                    }

                    BlockPos bottomPillar = centerRune.relative(side.getClockWise(), current.sideRunes() + current.sideBlocks() + 1)
                            .above(1 + current.pillarOffset());
                    for (int j = 0; j < current.pillarHeight(); j++) {
                        layout.put(bottomPillar.above(j), PILLAR_CHAR);
                    }

                    placeCapstone(layout, key, current, bottomPillar.above(current.pillarHeight()));
                }

                centerPos = centerPos.below();
            }
        }

        return buildDenseMultiblock(layout, key);
    }

    private static void placeCapstone(Map<BlockPos, Character> layout, Map<Character, JsonObject> key, AltarTier current, BlockPos capPos) {
        boolean isTag = current.capstone().tag();
        ResourceLocation capId = current.capstone().id();

        if (!isTag && (capId.equals(ResourceLocation.withDefaultNamespace("air")) || capId.getPath().isEmpty())) {
            return; // No real requirement (e.g. Apprentice's "air" capstone) - nothing to render.
        }

        char capChar = Character.forDigit(current.tier(), 10);
        JsonObject matcher = isTag ? tagMatcher(TagKey.create(Registries.BLOCK, capId)) : simpleMatcher(capId.toString());
        key.putIfAbsent(capChar, matcher);
        layout.put(capPos, capChar);
    }

    // ---- Shared dense-multiblock JSON building ----

    private static JsonObject buildDenseMultiblock(Map<BlockPos, Character> layout, Map<Character, JsonObject> key) {
        int minX = 0, maxX = 0, minY = 0, maxY = 0, minZ = 0, maxZ = 0;
        for (BlockPos pos : layout.keySet()) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        int xSize = maxX - minX + 1;
        int ySize = maxY - minY + 1;
        int zSize = maxZ - minZ + 1;

        JsonArray pattern = new JsonArray();
        for (int y = 0; y < ySize; y++) {
            JsonArray layer = new JsonArray();
            for (int z = 0; z < zSize; z++) {
                StringBuilder row = new StringBuilder();
                for (int x = 0; x < xSize; x++) {
                    Character c = layout.get(new BlockPos(x + minX, y + minY, z + minZ));
                    row.append(c != null ? c : ' ');
                }
                layer.add(row.toString());
            }
            pattern.add(layer);
        }

        JsonObject keyJson = new JsonObject();
        key.forEach((c, matcher) -> keyJson.add(String.valueOf(c), matcher));

        JsonObject result = new JsonObject();
        result.addProperty("type", "modopedia:dense");
        result.add("pattern", pattern);
        result.add("key", keyJson);
        return result;
    }

    private static JsonObject simpleMatcher(String blockId) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "modopedia:simple");
        JsonArray states = new JsonArray();
        states.add(blockId);
        obj.add("states", states);
        return obj;
    }

    private static JsonObject tagMatcher(TagKey<Block> tag) {
        JsonObject obj = new JsonObject();
        obj.addProperty("type", "modopedia:tag");
        obj.addProperty("tag", tag.location().toString());
        return obj;
    }
}

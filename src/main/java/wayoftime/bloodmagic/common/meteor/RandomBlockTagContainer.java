package wayoftime.bloodmagic.common.meteor;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

/**
 * Full-fidelity port of 1.20.1's {@code RandomBlockTagContainer}: resolves to a block drawn from a
 * block tag, either a fixed index into the tag's contents (used by the original for e.g. "always the
 * first entry of this ore tag") or, when no index is given, a uniformly random member of the tag.
 * An empty/unresolved tag resolves to {@code null}, same as the original - {@link MeteorLayer} treats
 * that as "skip this weighted entry".
 */
public class RandomBlockTagContainer extends RandomBlockContainer {
    private final TagKey<Block> tag;
    private final int index;

    public RandomBlockTagContainer(TagKey<Block> tag) {
        this(tag, -1);
    }

    public RandomBlockTagContainer(TagKey<Block> tag, int index) {
        this.tag = tag;
        this.index = index;
    }

    @Override
    public Block getRandomBlock(RandomSource rand, Level world) {
        Optional<HolderSet.Named<Block>> holderSet = BuiltInRegistries.BLOCK.getTag(tag);
        if (holderSet.isEmpty() || holderSet.get().size() <= 0) {
            return null;
        }

        HolderSet.Named<Block> entries = holderSet.get();
        if (index >= 0 && index < entries.size()) {
            return entries.get(index).value();
        }

        Optional<Holder<Block>> randomEntry = entries.getRandomElement(rand);
        return randomEntry.map(Holder::value).orElse(null);
    }
}

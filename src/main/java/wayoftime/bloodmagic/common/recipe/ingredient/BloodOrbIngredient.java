package wayoftime.bloodmagic.common.recipe.ingredient;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import wayoftime.bloodmagic.common.datamap.BMDataMaps;
import wayoftime.bloodmagic.common.datamap.BloodOrb;

import java.util.stream.Stream;

public record BloodOrbIngredient(int orbTier) implements ICustomIngredient {
    public static final MapCodec<BloodOrbIngredient> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                    Codec.INT.fieldOf("orb_tier").forGetter(BloodOrbIngredient::orbTier)
            ).apply(builder, BloodOrbIngredient::new)
    );

    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, BloodOrbIngredient> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, BloodOrbIngredient::orbTier,
            BloodOrbIngredient::new
    );

    @Override
    public boolean test(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        BloodOrb orb = stack.getItemHolder().getData(BMDataMaps.BLOOD_ORB_STATS);
        return orb != null && orb.tier() >= orbTier;
    }

    @Override
    public Stream<ItemStack> getItems() {
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> {
                    BloodOrb orb = item.builtInRegistryHolder().getData(BMDataMaps.BLOOD_ORB_STATS);
                    return orb != null && orb.tier() >= orbTier;
                })
                .map(Item::getDefaultInstance);
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return BMIngredientTypes.BLOOD_ORB.get();
    }
}

package wayoftime.bloodmagic.common.potion;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wayoftime.bloodmagic.BloodMagic;

/**
 * All sixteen of the 1.20.1 branch's custom potion effects are ported. Soul Snare, Soft Fall,
 * Heavy Heart, Spectral Sight, Grounded, Obsidian Cloak, Bounce and Soul Fray are plain markers
 * with no tick logic of their own (mirroring 1.20.1's own {@code PotionBloodMagic} base class,
 * which most of these used directly) - their gameplay behavior, where any exists beyond the
 * marker itself, lives in whatever other (separately-owned) system grants/checks them: fall
 * damage/bounce in the Bounce Sigil, explosion resistance in Obsidian Cloak's consumer, x-ray-style
 * rendering for Spectral Sight, etc. Gravity and Hard Cloak additionally carry an attribute
 * modifier exactly like 1.20.1's did (gravity multiplier, armor toughness bonus - ported to this
 * version's {@code Attributes.GRAVITY}/{@code ResourceLocation}-keyed {@link AttributeModifier}
 * API from the old Forge {@code ForgeMod.ENTITY_GRAVITY}/UUID-keyed one). Fire Fuse, Suspended,
 * Flight, Passivity, Plant Leech and Sacrificial Lamb implement {@code applyEffectTick} directly,
 * matching how vanilla's own periodic effects (Poison, Wither, etc.) work.
 */
public class BMPotions {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, BloodMagic.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> SOUL_SNARE = MOB_EFFECTS.register("soul_snare",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x4B0082) {});

    public static final DeferredHolder<MobEffect, MobEffect> SOFT_FALL = MOB_EFFECTS.register("soft_fall",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xC2B280) {});

    public static final DeferredHolder<MobEffect, MobEffect> FIRE_FUSE = MOB_EFFECTS.register("fire_fuse", FireFuseMobEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> SUSPENDED = MOB_EFFECTS.register("suspended", SuspendedMobEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> FLIGHT = MOB_EFFECTS.register("flight", FlightMobEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> HEAVY_HEART = MOB_EFFECTS.register("heavy_heart",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x4A2C2A) {});

    public static final DeferredHolder<MobEffect, MobEffect> PASSIVITY = MOB_EFFECTS.register("passivity", PassivityMobEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> PLANT_LEECH = MOB_EFFECTS.register("plant_leech", PlantLeechMobEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> SACRIFICIAL_LAMB = MOB_EFFECTS.register("sacrificial_lamb", SacrificialLambMobEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> SPECTRAL_SIGHT = MOB_EFFECTS.register("spectral_sight",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x2FB813) {});

    public static final DeferredHolder<MobEffect, MobEffect> GRAVITY = MOB_EFFECTS.register("gravity",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0x800080) {}
                    .addAttributeModifier(Attributes.GRAVITY, BloodMagic.rl("effect_gravity"), 0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));

    public static final DeferredHolder<MobEffect, MobEffect> GROUNDED = MOB_EFFECTS.register("grounded",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xBA855B) {});

    public static final DeferredHolder<MobEffect, MobEffect> OBSIDIAN_CLOAK = MOB_EFFECTS.register("obsidian_cloak",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x3C1A8D) {});

    public static final DeferredHolder<MobEffect, MobEffect> HARD_CLOAK = MOB_EFFECTS.register("hard_cloak",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x3C1A8D) {}
                    .addAttributeModifier(Attributes.ARMOR_TOUGHNESS, BloodMagic.rl("effect_hard_cloak"), 3.0D, AttributeModifier.Operation.ADD_VALUE));

    public static final DeferredHolder<MobEffect, MobEffect> BOUNCE = MOB_EFFECTS.register("bounce",
            () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x57FF2E) {});

    public static final DeferredHolder<MobEffect, MobEffect> SOUL_FRAY = MOB_EFFECTS.register("soul_fray",
            () -> new MobEffect(MobEffectCategory.HARMFUL, 0xFFFFFFFF) {});

    public static void register(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }
}

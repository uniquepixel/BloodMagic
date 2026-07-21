package wayoftime.bloodmagic.common.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import wayoftime.bloodmagic.BloodMagic;

public class BMEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(net.minecraft.core.registries.Registries.ENTITY_TYPE, BloodMagic.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<ThrowingDaggerEntity>> THROWING_DAGGER = ENTITY_TYPES.register("throwing_dagger",
            () -> EntityType.Builder.<ThrowingDaggerEntity>of(ThrowingDaggerEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(BloodMagic.rl("throwing_dagger").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<EntityMeteor>> METEOR = ENTITY_TYPES.register("meteor",
            () -> EntityType.Builder.<EntityMeteor>of(EntityMeteor::new, MobCategory.MISC)
                    .sized(1F, 1F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(BloodMagic.rl("meteor").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<SoulSnareEntity>> SOUL_SNARE = ENTITY_TYPES.register("soul_snare",
            () -> EntityType.Builder.<SoulSnareEntity>of(SoulSnareEntity::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(BloodMagic.rl("soul_snare").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<PotionFlaskEntity>> POTION_FLASK = ENTITY_TYPES.register("potion_flask",
            () -> EntityType.Builder.<PotionFlaskEntity>of(PotionFlaskEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(BloodMagic.rl("potion_flask").toString()));

    public static void register(IEventBus modBus) {
        ENTITY_TYPES.register(modBus);
    }
}

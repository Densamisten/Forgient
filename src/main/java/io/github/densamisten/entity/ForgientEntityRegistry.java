package io.github.densamisten.entity;

import io.github.densamisten.Forgient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForgientEntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Forgient.MOD_ID);

    public static final RegistryObject<EntityType<ForgientEntity>> FORGIENT_ENTITY = ENTITIES.register("example_entity",
            () -> EntityType.Builder.<ForgientEntity>of(ForgientEntity::new, MobCategory.CREATURE)
                    .sized(1.0f, 1.0f)
                    .build(new ResourceLocation(Forgient.MOD_ID, "example_entity").toString())
    );


    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}

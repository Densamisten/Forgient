package io.github.densamisten.item;

import io.github.densamisten.Forgient;
import io.github.densamisten.block.ForgientBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ForgientCreativeModeTab {

    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Forgient.MOD_ID);
    public static final RegistryObject<CreativeModeTab> FORGIENT_TAB = CREATIVE_MODE_TABS.register("forgient",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.forgient"))
                    .icon(ForgientItems.FORGIENT_KEY_ITEM.get()::getDefaultInstance)
                    .withSearchBar()
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ForgientItems.EXAMPLE_BLOCK_ITEM.get());
                        pOutput.accept(ForgientItems.FORGIENT_KEY_ITEM.get());
                        //pOutput.accept(ForgientBlocks.FORGIENT_CRYPTO_BLOCK.get());
                    })

                    .build()
    );
                        public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }

}
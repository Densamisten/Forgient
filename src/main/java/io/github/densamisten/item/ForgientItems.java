package io.github.densamisten.item;

import io.github.densamisten.Forgient;
import io.github.densamisten.block.ForgientBlocks;
import io.github.densamisten.block.ForgientCryptoBlock;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class ForgientItems {
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "forgient" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Forgient.MOD_ID);


    // Creates a new BlockItem with the id "forgient:example_block", combining the namespace and path
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(ForgientBlocks.EXAMPLE_BLOCK.get(), new Item.Properties()));

    // Creates a new item with the id "forgient:forgient_key"
    public static final RegistryObject<Item> FORGIENT_KEY_ITEM = ITEMS.register("forgient_key", () -> new ForgientKey(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

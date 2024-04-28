package io.github.densamisten;

import com.mojang.logging.LogUtils;
/*import io.github.densamisten.arguments.ForgientArgumentType;
import io.github.densamisten.block.ForgientBlocks;
import io.github.densamisten.command.ImageCommand;
import io.github.densamisten.gui.ForgientMenus;
import io.github.densamisten.item.ForgientCreativeModeTab;
import io.github.densamisten.item.ForgientItems;

import io.github.densamisten.sound.ForgientSounds;*/
import io.github.densamisten.arguments.ForgientArgumentType;
import io.github.densamisten.block.ForgientBlocks;
import io.github.densamisten.command.ImageCommand;
import io.github.densamisten.entity.ForgientEntity;
import io.github.densamisten.entity.ForgientEntityRegistry;
import io.github.densamisten.gui.ForgientMenus;
import io.github.densamisten.item.ForgientCreativeModeTab;
import io.github.densamisten.item.ForgientItems;
import io.github.densamisten.sound.ForgientSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Forgient.MOD_ID)
public class Forgient
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "forgient";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    @OnlyIn(Dist.CLIENT)
    private static ResourceManager resourceManager;

    public Forgient()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ForgientArgumentType.register(modEventBus);

        // items
        ForgientItems.register(modEventBus);

        // blocks
        ForgientBlocks.register(modEventBus);

        // creative mode tab
        ForgientCreativeModeTab.register(modEventBus);
        ForgientMenus.register(modEventBus);

        // entities
        ForgientEntityRegistry.register(modEventBus);

        // sounds
        ForgientSounds.register(modEventBus);

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the loadComplete method for modloading
        modEventBus.addListener(this::loadComplete);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        //ForgientArgumentType.register(modEventBus);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

     /**
     * Get mod resource manager
     **/

    @OnlyIn(Dist.CLIENT)
    public static ResourceManager getResourceManager() {
        return resourceManager;
    }
    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        ServerLevelAccessor.DIRECTIONS.clone();
        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        try {
            resourceManager = Minecraft.getInstance().getResourceManager();
        } catch(Exception e) {
            // Handle exception
        }

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }
    private void loadComplete(final FMLLoadCompleteEvent event) {
        ImageCommand.reload();
        LOGGER.info("Load completed!");
    }

}

package io.github.densamisten.event;

import io.github.densamisten.Forgient;
import io.github.densamisten.client.ClientHooks;
import io.github.densamisten.client.Keybindings;
import io.github.densamisten.client.ProjectileManager;
import io.github.densamisten.command.*;

import io.github.densamisten.command.vanilla.SoundCommand;
import io.github.densamisten.command.vanilla.TeletransportCommand;
import io.github.densamisten.command.vanilla.VerboseSayCommand;
import io.github.densamisten.network.PacketHandler;
import io.github.densamisten.network.SKeyPressSpawnEntityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Forgient.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgientEvents {

    private static final Component EXAMPLE_KEY_PRESSED =
            Component.translatable("message." + Forgient.MOD_ID + ".example_key_pressed");

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (Keybindings.INSTANCE.exampleKey.consumeClick() && Minecraft.getInstance().player != null) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> ClientHooks::openExampleBlockScreen);
            String dimensionNamespace = String.valueOf(Minecraft.getInstance().player.level().dimension().location());
            BlockPos blockPos = Minecraft.getInstance().player.blockPosition();
            String biomeLocation = String.valueOf(Minecraft.getInstance().player.level().getBiome(blockPos).value());
            String biome4 = String.valueOf(Minecraft.getInstance().player.level().getBiome(blockPos).unwrapKey().get());
            System.out.println(biome4);
                // Check if the player's world is a server world
                Level level = Minecraft.getInstance().player.level();
                if (level instanceof ServerLevel serverLevel) {

                    // Spawn a WindCharge above the player
                    ProjectileManager.spawnWindChargeAbovePlayer(serverLevel, Minecraft.getInstance().player);
                };
        }

        if (Keybindings.INSTANCE.examplePacketKey.consumeClick() && Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.displayClientMessage(EXAMPLE_KEY_PRESSED, true);
            PacketHandler.sendToServer(new SKeyPressSpawnEntityPacket());
        }
    }

    /*
     * Register commands
     */
    @SubscribeEvent
    public static void registerCommandsEvent(RegisterCommandsEvent event) {
        new FireworkCommand(event.getDispatcher());
        new ImageCommand(event.getDispatcher());
        new ALORCommand(event.getDispatcher());
        new Base64ToImageCommand(event.getDispatcher());
        new FMLCommand(event.getDispatcher());
        new BibleCommand(event.getDispatcher());
        new TmdbCommand(event.getDispatcher());
        new MCFCommand(event.getDispatcher());
        new MyCommand(event.getDispatcher());
        new SoundCommand(event.getDispatcher());
        new UuidCommand(event.getDispatcher());
        new BookCommand(event.getDispatcher());
        new CopyRawDataCommand(event.getDispatcher());
        new Hex2ImgCommand(event.getDispatcher());
        new VerboseSayCommand(event.getDispatcher());
        new TeletransportCommand(event.getDispatcher());
        new PingCommand(event.getDispatcher());
    }

}

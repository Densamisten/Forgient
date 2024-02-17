package io.github.densamisten.event;

import io.github.densamisten.Forgient;
import io.github.densamisten.command.Base64ToImageCommand;
import io.github.densamisten.command.ImageCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Forgient.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgientEvents {

    /**
     * Register commands
     **/
    @SubscribeEvent
    public static void registerCommandsEvent(RegisterCommandsEvent event) {
        new ImageCommand(event.getDispatcher());
        new Base64ToImageCommand(event.getDispatcher());
    }

}

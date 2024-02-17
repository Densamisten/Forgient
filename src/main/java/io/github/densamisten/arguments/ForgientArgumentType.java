package io.github.densamisten.arguments;

import io.github.densamisten.Forgient;
import io.github.densamisten.util.DirectoryArgument;
import io.github.densamisten.util.PathArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForgientArgumentType {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, Forgient.MOD_ID);

    public static final RegistryObject<SingletonArgumentInfo<PathArgument>> PATH_ARGUMENT = ARGUMENT_TYPES.register(
            "pathargument",
            () -> ArgumentTypeInfos.registerByClass(PathArgument.class, SingletonArgumentInfo.contextFree(PathArgument::new))
    );

    public static final RegistryObject<SingletonArgumentInfo<DirectoryArgument>> DIRECTORY_ARGUMENT = ARGUMENT_TYPES.register(
            "directoryargument",
            () -> ArgumentTypeInfos.registerByClass(DirectoryArgument.class, SingletonArgumentInfo.contextFree(DirectoryArgument::new))
    );

    public static void register(IEventBus eventBus) {
        ARGUMENT_TYPES.register(eventBus);
    }
}

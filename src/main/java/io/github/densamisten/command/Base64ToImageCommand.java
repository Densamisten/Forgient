package io.github.densamisten.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public class Base64ToImageCommand {
    public Base64ToImageCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("base64toimage")
                .then(Commands.argument("base64", StringArgumentType.string())
                        .executes(context -> execute(context.getSource(), StringArgumentType.getString(context, "base64")))
                )
        );
    }

    private static int execute(CommandSourceStack source, String base64) {
        String base64String = base64;

        // Remove the prefix 'data:image/png;base64,' from the Base64 string if present
        if (base64String.startsWith("data:image/png;base64,")) {
            base64String = base64String.substring("data:image/png;base64,".length());
        }

        byte[] imageBytes = Base64.getDecoder().decode(base64String);

        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            File outputFile = new File("decoded_image.png");
            ImageIO.write(img, "png", outputFile);
            source.sendSuccess(() -> Component.literal("Image successfully decoded and saved as 'decoded_image.png'"), true);
        } catch (IOException e) {
            source.sendFailure(Component.literal("Failed to decode image: " + e.getMessage()));
        }

        return 1;
    }
}

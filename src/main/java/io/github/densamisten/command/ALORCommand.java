package io.github.densamisten.command;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ALORCommand {
    private static final String BASE_URL = "https://archlinux.org/packages/";

    // Method to fetch package details in JSON format
    static JsonObject fetchPackageDetails(String repository, String architecture, String packageName) throws IOException {
        String urlString = BASE_URL + repository + "/" + architecture + "/" + packageName + "/json/";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return new Gson().fromJson(response.toString(), JsonObject.class);
        } else {
            throw new IOException("Failed to fetch package details. Response code: " + responseCode);
        }
    }

    // Command registration method
    public ALORCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("alorsearch")
                        .then(Commands.argument("repository", StringArgumentType.word())
                                .then(Commands.argument("architecture", StringArgumentType.word())
                                        .then(Commands.argument("package", StringArgumentType.greedyString())
                                                .executes(ALORCommand::execute)))));
    }

    // Command execution method
    private static int execute(CommandContext<CommandSourceStack> context) {
        String repository = context.getArgument("repository", String.class);
        String architecture = context.getArgument("architecture", String.class);
        String packageName = context.getArgument("package", String.class);

        try {
            JsonObject packageDetails = fetchPackageDetails(repository, architecture, packageName);
            // Extract and display package information
            context.getSource().sendSuccess(() -> Component.literal("Package details for " + packageName + ":\n"
                    + "Architecture: " + packageDetails.get("arch").getAsString() + "\n"
                    + "Repository: " + packageDetails.get("repo").getAsString() + "\n"
                    + "Description: " + packageDetails.get("pkgdesc").getAsString() + "\n"
                    + "Upstream URL: " + packageDetails.get("url").getAsString() + "\n"
                    + "License(s): " + packageDetails.get("licenses").getAsJsonArray() + "\n"
                    + "Maintainers: " + packageDetails.get("maintainers").getAsJsonArray() + "\n"
                    + "Package Size: " + formatSize(packageDetails.get("compressed_size").getAsLong()) + " MB" + "\n"
                    + "Installed Size: " + formatSize(packageDetails.get("installed_size").getAsLong()) + " MB" + "\n"
                    + "Last Packager: " + packageDetails.get("packager").getAsString() + "\n"
                    + "Build Date: " + packageDetails.get("build_date").getAsString() + "\n"
                    + "Last Updated: " + packageDetails.get("last_update").getAsString()), false);
        } catch (IOException e) {
            context.getSource().sendFailure(Component.literal("Failed to fetch package details: " + e.getMessage()));
        }

        return 1;
    }
    // Method to format size from bytes to MB
    private static String formatSize(long bytes) {
        double mb = (double) bytes / (1024 * 1024);
        return String.format("%.2f", mb);
    }
}

package io.github.densamisten.command;

import com.google.gson.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NyuushuCommand {
    // URLs for AUR API

    private static final String SEARCH_API_URL = "https://aur.archlinux.org/rpc/v5/search/";
    // Query exact name in AUR
    private static final String QUERY_INFO_AUR_URL = "https://aur.archlinux.org/rpc/v5/info?arg[]=";

    private static final String QUERY_PKG_ORWI_URL = "https://archlinux.org/packages/search/json/?q=";
    // Base URL
    private static final String BASE_URL = "https://archlinux.org/packages/";

    public NyuushuCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Register the commands
        dispatcher.register(
                Commands.literal("nyuushu")
                        .then(
                                Commands.literal("aur")
                                        .then(
                                                Commands.literal("search")
                                                        .then(
                                                                Commands.argument("query", StringArgumentType.string())
                                                                        .executes(context -> queryAur(context, StringArgumentType.getString(context, "query")))
                                                        )
                                        )
                                        .then(
                                                Commands.literal("info")
                                                        .then(
                                                                Commands.argument("pkg", StringArgumentType.string())
                                                                        .executes(context -> aurPkgInfo(context, StringArgumentType.getString(context, "pkg")))
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("orwi")
                                        .then(
                                                Commands.literal("search")
                                                        .then(
                                                                Commands.argument("query", StringArgumentType.string())
                                                                        .executes(context -> queryOrwi(context, StringArgumentType.getString(context, "query")))
                                                        )
                                        )
                                        .then(
                                                Commands.literal("info")
                                                        .then(
                                                                Commands.argument("repository", StringArgumentType.word())
                                                                        .then(
                                                                                Commands.argument("architecture", StringArgumentType.word())
                                                                                        .then(
                                                                                                Commands.argument("package", StringArgumentType.greedyString())
                                                                                                        .executes(NyuushuCommand::execute)
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
        );
    }





    // Method to search for packages in ORWI
    private int queryOrwi(CommandContext<CommandSourceStack> context, String query) {
        try {
            String response = sendGetRequest(QUERY_PKG_ORWI_URL + query);
            // Parse the JSON response
            JsonObject jsonObject = parseResponse(response);

            // Extract and display package names
            if (jsonObject.has("results")) {
                for (JsonElement element : jsonObject.getAsJsonArray("results")) {
                    if (element.isJsonObject()) {
                        JsonObject resultObject = element.getAsJsonObject();
                        String packageName = resultObject.get("pkgname").getAsString();
                        String packageDescription = resultObject.get("pkgdesc").getAsString();
                        String packageRepository = resultObject.get("repo").getAsString();
                        String packageVersion = resultObject.get("pkgver").getAsString();
                        context.getSource().sendSuccess(() -> Component.literal(packageRepository + "/" + packageName + " " + packageVersion), false);
                        context.getSource().sendSuccess(() -> Component.literal(packageDescription).withStyle(ChatFormatting.GREEN), false);
                    }
                }
            }
            return 1;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
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

    // Method to search for packages in AUR
    private int queryAur(CommandContext<CommandSourceStack> context, String query) {
        context.getSource().sendSuccess(() -> Component.literal(query), false);
        try {
            String response = sendGetRequest(SEARCH_API_URL + query);
            // Parse the JSON response
            JsonObject jsonObject = parseResponse(response);

            // Extract and display package names
            if (jsonObject.has("results")) {
                for (JsonElement element : jsonObject.getAsJsonArray("results")) {
                    if (element.isJsonObject()) {
                        JsonObject resultObject = element.getAsJsonObject();
                        String packageName = resultObject.get("Name").getAsString();
                        String packageDescription = resultObject.get("Description").getAsString();
                        context.getSource().sendSuccess(() -> Component.literal(packageName).withStyle(ChatFormatting.WHITE), false);
                        context.getSource().sendSuccess(() -> Component.literal(packageDescription).withStyle(ChatFormatting.GREEN), false);
                    }
                }
            }
            return 1;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // Method to retrieve information about a specific package from AUR
    private int aurPkgInfo(CommandContext<CommandSourceStack> context, String pkg) {
        try {
            String response = sendGetRequest(QUERY_INFO_AUR_URL + pkg);
            // Parse the JSON response
            JsonObject jsonObject = parseResponse(response);

            // Extract and display package information
            if (jsonObject.has("results")) {
                JsonObject resultObject = jsonObject.getAsJsonArray("results").get(0).getAsJsonObject();
                String packageName = resultObject.get("Name").getAsString();
                String packageVersion = resultObject.get("Version").getAsString();
                String packageDescription = resultObject.get("Description").getAsString();
                String packageMaintainer = resultObject.get("Maintainer").getAsString();
                String packageLicense = resultObject.get("License").getAsString();
                context.getSource().sendSuccess(() -> Component.literal("Name: ").withStyle(ChatFormatting.BLUE).append(Component.literal(packageName).withStyle(ChatFormatting.GREEN)), false);
                context.getSource().sendSuccess(() -> Component.literal("Version: ").withStyle(ChatFormatting.BLUE).append(Component.literal(packageVersion).withStyle(ChatFormatting.BOLD)), false);
                context.getSource().sendSuccess(() -> Component.literal("Description: ").withStyle(ChatFormatting.BLUE).append(Component.literal(packageDescription).withStyle(ChatFormatting.BOLD)), false);
                context.getSource().sendSuccess(() -> Component.literal("License: ").withStyle(ChatFormatting.BLUE).append(Component.literal(packageLicense).withStyle(ChatFormatting.BOLD)), false);
                context.getSource().sendSuccess(() -> Component.literal("Maintainer: ").withStyle(ChatFormatting.BLUE).append(Component.literal(packageMaintainer).withStyle(ChatFormatting.BOLD)), false);
            }
            return 1;
        } catch (IOException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    // Method to send GET request and retrieve response
    private String sendGetRequest(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }

    // Method to parse JSON response
    private JsonObject parseResponse(String response) {
        JsonElement jsonResponse = JsonParser.parseString(response);
        return jsonResponse.getAsJsonObject();
    }

    // Method to fetch package details in JSON format
    private static JsonObject fetchPackageDetails(String repository, String architecture, String packageName) throws IOException {
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
}
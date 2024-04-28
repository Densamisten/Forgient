package io.github.densamisten.command;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.io.IOUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MapiCommand {
    private static final Gson GSON = new Gson();

    public MapiCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mapi")
                .then(Commands.literal("blockedservers")
                        .executes(MapiCommand::executeBlockedServers))
                .then(Commands.literal("user2uuid")
                        .then(Commands.argument("username", StringArgumentType.string())
                                .executes(MapiCommand::executeUserToUUID))
                )
                .then(Commands.literal("uuid2user")
                        .then(Commands.argument("uuid", StringArgumentType.string())
                                .executes(MapiCommand::executeUUIDToUser))
                )
        );
    }
    private static int executeBlockedServers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            String blockedServersUrl = "https://sessionserver.mojang.com/blockedservers";
            List<String> blockedServers = IOUtils.readLines(new URL(blockedServersUrl).openStream(), StandardCharsets.UTF_8);
            context.getSource().sendSuccess(() -> Component.literal("Blocked Servers:"), false);
            for (String server : blockedServers) {
                context.getSource().sendFailure(Component.literal(server));
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to retrieve blocked servers: " + e.getMessage()));
        }
        return 1;
    }

    private static int executeUserToUUID(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String username = StringArgumentType.getString(context, "username");
        try {
            String endpointUrl1 = "https://api.mojang.com/users/profiles/minecraft/" + username;
            String endpointUrl2 = "https://api.mojang.com/user/profile/agent/minecraft/name/" + username;

            String uuidJson = getUUIDJson(endpointUrl1);
            if (uuidJson == null) {
                uuidJson = getUUIDJson(endpointUrl2);
            }

            if (uuidJson != null) {
                JsonObject jsonObject = GSON.fromJson(uuidJson, JsonObject.class);
                String name = jsonObject.get("name").getAsString();
                String id = jsonObject.get("id").getAsString();
                MutableComponent text = Component.literal("Username:\n" + name + ", UUID:\n" + id);
                text.setStyle(text.getStyle()
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy UUID")))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id)));

                context.getSource().sendSuccess(() -> Component.literal("Username: " + name + ", UUID: " + id).withStyle(ChatFormatting.AQUA).withStyle((style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, id)))), false);
                context.getSource().sendSuccess(() -> Component.literal(String.valueOf(text)), false);
            } else {
                context.getSource().sendFailure(Component.literal("No UUID found for username: " + username));
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to retrieve UUID for username " + username + ": " + e.getMessage()));
        }
        return 1;
    }
    private static int executeUUIDToUser(CommandContext<CommandSourceStack> context) {
        String uuid = StringArgumentType.getString(context, "uuid");
        try {
            String endpointUrl = "https://api.mojang.com/user/profile/" + uuid;
            String json = makeGetRequest(endpointUrl);

            if (json != null) {
                String username = GSON.fromJson(json, JsonObject.class).get("name").getAsString();
                context.getSource().sendSuccess(() -> Component.literal("UUID: " + uuid + ", Username: " + username), false);
            } else {
                context.getSource().sendFailure(Component.literal("No username found for UUID: " + uuid));
            }
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to retrieve username for UUID " + uuid + ": " + e.getMessage()));
        }
        return 1;
    }

    private static String getUUIDJson(String endpointUrl) throws Exception {
        URL url = new URL(endpointUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
        } else if (responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
            return null; // No UUID found
        } else {
            throw new RuntimeException("Failed to retrieve UUID. Response code: " + responseCode);
        }
    }
    private static String makeGetRequest(String endpointUrl) throws Exception {
        URL url = new URL(endpointUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
        } else {
            return null;
        }
    }
}
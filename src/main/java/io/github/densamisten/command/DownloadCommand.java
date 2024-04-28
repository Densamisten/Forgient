package io.github.densamisten.command;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.server.command.EnumArgument;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Usage: /aur search <field> <query> Where <field> is the search field and <query> is the search query
 * AurCommand. Search packages within Arch User Repository
 * License: Free for all use.
 **/

public class DownloadCommand {

    public static class AurFieldEnum {

        public enum Field {
            NAME("name"),
            NAME_DESC("name-desc"),
            MAINTAINER("maintainer"),
            DEPENDS("depends"),
            MAKEDEPENDS("makedepends"),
            OPTDEPENDS("optdepends"),
            CHECKDEPENDS("checkdepends");

            private final String fieldName;

            Field(String fieldName) {
                this.fieldName = fieldName;
            }

            public String getFieldName() {
                return fieldName;
            }

            private static final Map<String, Field> BY_FIELD_NAMES = new HashMap<>();

            static {
                for (Field field : values()) {
                    BY_FIELD_NAMES.put(field.getFieldName(), field);
                }
            }

            public static Field getByFieldName(String fieldName) {
                return BY_FIELD_NAMES.get(fieldName);
            }

            public static EnumArgument<Field> enumArgument() {
                return EnumArgument.enumArgument(Field.class);
            }
        }
    }

    public DownloadCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("download")
                .then(Commands.literal("search")
                        .then(Commands.argument("field", AurFieldEnum.Field.enumArgument())
                                .then(Commands.argument("query", StringArgumentType.string())
                                        .executes(context -> searchAur(context, context.getArgument("field", AurFieldEnum.Field.class), StringArgumentType.getString(context, "query")))))));
    }

    private static int searchAur(CommandContext<CommandSourceStack> context, AurFieldEnum.Field field, String query) {
        try {
            String API_URL = "https://aur.archlinux.org/rpc/v5/search/" + query + "?=" + field;
            String response = sendGetRequest(API_URL);

            // Parse the JSON response
            JsonObject jsonResponse = new Gson().fromJson(response, JsonObject.class);
            JsonArray resultsArray = jsonResponse.getAsJsonArray("results");

            // Extract and print the value based on the selected field
            for (JsonElement element : resultsArray) {
                String fieldValue = getString(field, element);

                // Send the extracted value as a message to the command source
                context.getSource().sendSuccess(() -> Component.literal(fieldValue), false);
            }

            return 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getString(AurFieldEnum.Field field, JsonElement element) {
        JsonObject result = element.getAsJsonObject();
        switch (field) {
            case NAME:
                return result.has("Name") ? result.get("Name").getAsString() : "Name field not found";
            case NAME_DESC:
                if (result.has("Name") && result.has("Description")) {
                    return result.get("Name").getAsString() + " - " + result.get("Description").getAsString();
                } else {
                    return "Name or Description field not found";
                }
            case MAINTAINER:
                return result.has("Maintainer") ? result.get("Maintainer").getAsString() : "Maintainer field not found";
            case DEPENDS:
                return result.has("Depends") ? result.get("Depends").getAsString() : "Depends field not found";
            case MAKEDEPENDS:
                return result.has("MakeDepends") ? result.get("MakeDepends").getAsString() : "MakeDepends field not found";
            case OPTDEPENDS:
                return result.has("OptDepends") ? result.get("OptDepends").getAsString() : "OptDepends field not found";
            case CHECKDEPENDS:
                return result.has("CheckDepends") ? result.get("CheckDepends").getAsString() : "CheckDepends field not found";
            default:
                return "Unknown field";
        }
    }

    private static String sendGetRequest(String url) throws IOException {
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
}
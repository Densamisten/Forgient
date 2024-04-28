package io.github.densamisten.command;

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
import java.util.HashMap;
import java.util.Map;

public class TmdbCommand {

    public TmdbCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tmdb").then(Commands.literal("search").then(Commands.literal("movie")
                .then(Commands.argument("query", StringArgumentType.string())
                        .then(Commands.argument("rating", ContentRatingEnum.enumArgument())
                                .then(Commands.argument("language", LanguageEnum.enumArgument())
                                        .executes(context -> execute(context, StringArgumentType.getString(context, "query"),
                                                context.getArgument("rating", ContentRatingEnum.class),
                                                context.getArgument("language", LanguageEnum.class)))))))));
    }

    private int execute(CommandContext<CommandSourceStack> context, String query, ContentRatingEnum rating,
            LanguageEnum language) {
        String lang = language.languageCode + "-" + language.countryCode;
        context.getSource().sendSuccess(() -> Component.literal("Search: " + query + language + rating), false);
        sendRequest(query, lang, rating);
        return 1; // Return success
    }

    private void sendRequest(String query, String language, ContentRatingEnum rating) {
        try {
            HttpURLConnection conn = getHttpURLConnection(query, language, rating);
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close(); // Print or process the response as needed
                System.out.println(response);
            } else {
                System.out.println("GET request failed: HTTP error code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static HttpURLConnection getHttpURLConnection(String query, String language, ContentRatingEnum rating)
            throws IOException {
        URL url = new URL("https://api.themoviedb.org/3/search/movie?query=" + query + "&include_adult="
                + rating.isAdultContent() + "&language=" + language + "&page=1");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestProperty("Authorization",
                "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI3M2ZjYTE2ODNhMTQwMWZlMjQ3ZTc4Y2RjNWU0NzlmOSIsInN1YiI6IjY1Yzc4NzUyYjZjZmYxMDE4NWE1ODIwOSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.y4OGSPFYUZd42RzDJa9uk7btWe3cAjCJBgnZVNhq5mc");
        return conn;
    }

    enum LanguageEnum {
        // ISO-639-1-ISO-3166-1 codes
        english("en", "US"), // English
        french("fr", "FR"), // French
        spanish("es", "ES"), // Spanish
        portuguese("pt", "PT"), // Portuguese
        german("de", "DE"), // German
        italian("it", "IT"), // Italian
        chinese("zh", "CN"); // Chinese;

        private final String languageCode;
        private final String countryCode;
        private static final Map<String, LanguageEnum> BY_CODES = new HashMap<>();

        static {
            for (LanguageEnum languageEnum : values()) {
                BY_CODES.put(languageEnum.languageCode + "-" + languageEnum.countryCode, languageEnum);
            }
        }

        LanguageEnum(String languageCode, String countryCode) {
            this.languageCode = languageCode;
            this.countryCode = countryCode;
        }

        public static LanguageEnum getEnum(String code) {
            return BY_CODES.get(code);
        }

        public static EnumArgument<LanguageEnum> enumArgument() {
            return EnumArgument.enumArgument(LanguageEnum.class);
        }
    }

    enum ContentRatingEnum {
        adult(true),
        youth(false);

        private final boolean contentRatingBoolean;

        ContentRatingEnum(boolean contentRatingBoolean) {
            this.contentRatingBoolean = contentRatingBoolean;
        }

        public boolean isAdultContent() {
            return contentRatingBoolean;
        }

        public static EnumArgument<ContentRatingEnum> enumArgument() {
            return EnumArgument.enumArgument(ContentRatingEnum.class);
        }
    }
}

package io.github.densamisten.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BibleCommand {

    public BibleCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("seisho")
                        .then(
                                Commands.argument("chapter", StringArgumentType.word())
                                        .suggests(this::suggestChapters)
                                        .then(
                                                Commands.argument("verse", StringArgumentType.word())
                                                        .suggests(this::suggestVerses)
                                                        .executes(this::searchBibleApi) // Register the command execution handler
                                        )
                        )
        );
    }

    private CompletableFuture<Suggestions> suggestChapters(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase(); // Convert input to lowercase for case-insensitive comparison
        List<String> suggestions = Arrays.stream(MinecraftVersion.values())
                .map(MinecraftVersion::getBookChapter)
                .filter(bookChapter -> bookChapter.toLowerCase().startsWith(input))
                .toList();
        suggestions.forEach(builder::suggest);
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestVerses(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase(); // Convert input to lowercase for case-insensitive comparison
        MinecraftVersion minecraftVersion = MinecraftVersion.getByVersionName(StringArgumentType.getString(context, "verse"));
        if (minecraftVersion != null) {
            List<String> suggestions = Arrays.stream(minecraftVersion.getBookVerses())
                    .filter(verse -> verse.toLowerCase().startsWith(input))
                    .toList();
            suggestions.forEach(builder::suggest);
        }
        return builder.buildFuture();
    }

    private int searchBibleApi(CommandContext<CommandSourceStack> context) {
        String chapter = StringArgumentType.getString(context, "chapter");
        String verse = StringArgumentType.getString(context, "verse");
        // Construct the URL based on the provided chapter and verse
        String url = "https://bible-api.com/" + chapter + "%20" + verse;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);

            // Read the content from the response
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonContent.append(line);
            }

            // Parse the JSON content into a JSON object
            JsonObject jsonObject = JsonParser.parseString(jsonContent.toString()).getAsJsonObject();

            // Print the JSON object
            context.getSource().sendSuccess(() -> Component.literal(String.valueOf(jsonObject)), true);
            return 1;
        } catch (Exception e) {
            // Handle exceptions (e.g., network errors) and inform the player about the failure
            context.getSource().sendFailure(Component.literal("Failed to download Bible verse: " + e.getMessage()));
            return 0;
        }
    }


    public enum MinecraftVersion {
        GENESIS("Genesis"),
        EXODUS("Exodus"),
        LEVITICUS("Leviticus"),
        NUMBERS("Numbers"),
        DEUTERONOMY("Deuteronomy"),
        JOSHUA("Joshua"),
        JUDGES("Judges"),
        RUTH("Ruth"),
        FIRST_SAMUEL("1Samuel"),
        SECOND_SAMUEL("2Samuel"),
        FIRST_KINGS("1Kings"),
        SECOND_KINGS("2Kings"),
        FIRST_CHRONICLES("1Chronicles"),
        SECOND_CHRONICLES("2Chronicles"),
        EZRA("Ezra"),
        NEHEMIAH("Nehemiah"),
        ESTHER("Esther"),
        JOB("Job"),
        PSALMS("Psalms"),
        ECCLESIASTES("Ecclesiastes"),
        SONG_OF_SOLOMON("Song of Solomon"),
        ISAIAH("Isaiah"),
        JEREMIAH("Jeremiah"),
        LAMENTATIONS("Lamentations"),
        EZEKIEL("Ezekiel"),
        DANIEL("Hosea"),
        JOEL("Joel"),
        AMOS("Amos"),
        OBADIAH("Obadiah"),
        JONAH("Jonah"),
        MICAH("Micah"),
        NAHUM("Nahum"),
        HABAKKUK("Habakkuk"),
        ZEPHANIAH("Zephaniah"),
        HAGGAI("Haggai"),
        ZECHARIAH("Zechariah"),
        MALACHI("Malachi"),
        MATTHEW("Matthew"),
        MARK("Mark"),
        LUKE("Luke"),
        JOHN("John"),
        ACTS("Acts"),
        ROMANS("Romans"),
        FIRST_CORINTHIANS("1Corinthians"),
        SECOND_CORINTHIANS("2Corinthians"),
        GALATIANS("Galatians"),
        EPHESIANS("Ephesians"),
        PHILIPPIANS("Philippians"),
        COLOSSIANS("Colossians"),
        FIRST_THESSALONIANS("1 Thessalonians"),
        SECOND_THESSALONIANS("2 Thessalonians"),
        FIRST_TIMOTHY("1Timothy"),
        SECOND_TIMOTHY("2Timothy"),
        TITUS("Titus"),
        PHILEMON("Philemon"),
        HEBREWS("Hebrews"),
        JAMES("James"),
        FIRST_PETER("1Peter"),
        SECOND_PETER("2Peter"),
        FIRST_JOHN("1John"),
        SECOND_JOHN("2John"),
        THIRD_JOHN("3John"),
        JUDE("Jude"),
        REVELATION("Revelation");

        private final String bookChapter;
        private final String[] bookVerses;

        MinecraftVersion(String bookChapter, String... bookVerses) {
            this.bookChapter = bookChapter;
            this.bookVerses = bookVerses;
        }

        public String getBookChapter() {
            return bookChapter;
        }

        public String[] getBookVerses() {
            return bookVerses;
        }

        public static MinecraftVersion getByVersionName(String bookChapter) {
            for (MinecraftVersion chapter : values()) {
                if (chapter.getBookChapter().equalsIgnoreCase(bookChapter)) {
                    return chapter;
                }
            }
            return null;
        }
    }
}
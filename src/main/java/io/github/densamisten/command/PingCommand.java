package io.github.densamisten.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;


/*
* /ping 127.0.0.1 9050 amiusingtor.net
 */
public class PingCommand {

    public PingCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("ping")
                .then(Commands.argument("proxy", StringArgumentType.word())
                        .then(Commands.argument("port", IntegerArgumentType.integer())
                                .then(Commands.argument("ip", StringArgumentType.word())
                                        .executes(context -> execute(context.getSource(),
                                                StringArgumentType.getString(context, "proxy"),
                                                IntegerArgumentType.getInteger(context, "port"),
                                                StringArgumentType.getString(context, "ip"))
                                        )
                                )
                        )
                )
        );
    }

    private static int execute(CommandSourceStack source, String proxyAddress, int proxyPort, String urlString) {
        try {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyAddress, proxyPort));
            URI uri = new URI("http://" + urlString);
            source.sendSuccess(() -> Component.literal(createText("Started socks request! URL: " + urlString)), true);
            long startTime = System.currentTimeMillis();

            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection(proxy);
            connection.setConnectTimeout(10000); // 10 seconds connection timeout
            connection.setReadTimeout(10000); // 10 seconds read timeout

            int responseCode = connection.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;
            boolean status = (responseCode == HttpURLConnection.HTTP_OK);

            source.sendSuccess(() -> Component.literal(createText("Response Time: " + responseTime + " ms")), false);
            source.sendSuccess(() -> Component.literal(createText("Load Time: " + responseTime + " ms")), false);
            source.sendSuccess(() -> Component.literal(createText("Status: " + status)), true);
            source.sendSuccess(() -> Component.literal(createText("Loaded: " + status)), true);

            // Read and print the response content
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder responseContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            reader.close();
            source.sendSuccess(() -> Component.literal(createText("Response Content: " + responseContent)), true);

            connection.disconnect();
        } catch (IOException | URISyntaxException e) {
            source.sendFailure(Component.literal(createText("Host unreachable error")));
            e.printStackTrace();
        }
        return 1;
    }

    private static String createText(String message) {
        return (message);
    }
}

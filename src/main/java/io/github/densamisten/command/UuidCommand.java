package io.github.densamisten.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.UUID;

public class UuidCommand {

    public enum Namespace {
        /* Name string is a fully-qualified domain name */
        NAMESPACE_DNS("6ba7b810-9dad-11d1-80b4-00c04fd430c8"),
        /* Name string is a URL */
        NAMESPACE_URL("6ba7b811-9dad-11d1-80b4-00c04fd430c8"),
        /* Name string is an ISO OID */
        NAMESPACE_OID("6ba7b812-9dad-11d1-80b4-00c04fd430c8"),
        /* Name string is an X.500 DN (in DER or a text output format) */
        NAMESPACE_X500("6ba7b814-9dad-11d1-80b4-00c04fd430c8");
        private final String uuidString;

        Namespace(String uuidString) {
            this.uuidString = uuidString;
        }

        public String getUUIDString() {
            return uuidString;
        }
    }

    public enum Variant {
        STANDARD,
        MICROSOFT

    }

    public UuidCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("uuid")
                        .then(Commands.literal("generate")
                                .then(Commands.literal("v1")
                                        .executes(UuidCommand::generateUuidV1))
                                .then(Commands.literal("v2")
                                        .executes(UuidCommand::generateUuidV2))
                                .then(Commands.literal("v4")
                                        .executes(UuidCommand::generateUuidV4)))
                        .then(Commands.literal("hash")
                                .then(Commands.literal("v3")
                                        .then(Commands.argument("namespace", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    // Provide repository values as suggestions
                                                    for (Namespace namespace : Namespace.values()) {
                                                        builder.suggest(namespace.getUUIDString());
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("string", StringArgumentType.word())
                                                        .executes(context -> executeHash(context, 3)))))
                                .then(Commands.literal("v5")
                                        .then(Commands.argument("namespace", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    // Provide repository values as suggestions
                                                    for (Namespace namespace : Namespace.values()) {
                                                        builder.suggest(namespace.getUUIDString());
                                                    }
                                                    return builder.buildFuture();
                                                })
                                                .then(Commands.argument("string", StringArgumentType.word())
                                                        .executes(context -> executeHash(context, 5))))))
                        .then(Commands.literal("decode")
                                .then(Commands.argument("uuid", StringArgumentType.word())
                                        .executes(UuidCommand::decodeUuid)))
        );
    }

    private static int generateUuidV1(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        UUID uuid = generateType1UUID(); // Generate UUID version 1
        Component uuidText = Component.literal(uuid.toString()).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString())));
        source.sendSuccess(() -> uuidText, false);
        return 1;
    }

    private static UUID generateType1UUID() {
        long most64SigBits = get64MostSignificantBitsForVersion1();
        long least64SigBits = get64LeastSignificantBitsForVersion1();
        return new UUID(most64SigBits, least64SigBits);
    }

    private static long get64LeastSignificantBitsForVersion1() {
        Random random = new Random();
        long random63BitLong = random.nextLong() & 4611686018427387903L;
        long variant3BitFlag = -9223372036854775808L;
        return random63BitLong | variant3BitFlag;
    }

    private static long get64MostSignificantBitsForVersion1() {
        long currentTimeMillis = System.currentTimeMillis();
        long timeLow = (currentTimeMillis & 4294967295L) << 32;
        long timeMid = ((currentTimeMillis >> 32) & 65535L) << 16;
        long version = 1L << 12;
        long timeHi = (currentTimeMillis >> 48) & 4095L;
        return timeLow | timeMid | version | timeHi;
    }
    private static int generateUuidV2(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        UUID uuid = generateUuidV2();
        Component uuidText = Component.literal(uuid.toString()).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString())));
        source.sendSuccess(() -> uuidText, false);
        return 1;
    }

    private static UUID generateUuidV2() {
        Random random = new Random();
        long mostSignificantBits = random.nextLong() & 0xFFFFFFFFFFFF0FFFL | 0x0000000000002000L; // Set the version to 2
        long leastSignificantBits = random.nextLong();
        return new UUID(mostSignificantBits, leastSignificantBits);
    }
    private static int generateUuidV4(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        UUID uuid = UUID.randomUUID();
        Component uuidText = Component.literal(uuid.toString()).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, uuid.toString())));
        source.sendSuccess(() -> uuidText, false);
        return 1;
    }

    private static int decodeUuid(CommandContext<CommandSourceStack> context) {
        String uuidString = StringArgumentType.getString(context, "uuid");
        try {
            UUID uuid = UUID.fromString(uuidString);
            String variantInfo = getVariantInfo(uuid);
            String versionInfo = getVersionInfo(uuid);
            String algorithmInfo = getAlgorithmInfo(uuid);
            context.getSource().sendSuccess(() -> Component.literal("Unhashed UUID: " + uuid + versionInfo + algorithmInfo + variantInfo), true);
            return 1;
        } catch (IllegalArgumentException e) {
            context.getSource().sendFailure(Component.literal("Invalid UUID format."));
            return 0;
        }
    }

    private static String getVersionInfo(UUID uuid) {
        int version = (uuid.version() & 0xF);
        return " (Version " + version + ")";
    }

    private static String getVariantInfo(UUID uuid) {
        byte[] bytes = toBytes(uuid);
        int variantBits = (bytes[8] & 0xFF) >> 6;  // Extract the 17th hex digit (bytes[8]) and shift right by 6 to get the variant bits
        String variant = switch (variantBits) {
            case 0, 1 -> "Reserved (future use)";
            case 2 -> "RFC 4122 (DCE)";
            case 3 -> "Microsoft GUID";
            default -> "Unknown";
        };
        return " (Variant: " + variant + ")";
    }

    private static String getAlgorithmInfo(UUID uuid) {
        byte[] bytes = toBytes(uuid);
        if (uuid.version() == 3 && bytes[6] >> 4 == 3) {
            return " (MD5)";
        } else if (uuid.version() == 5 && bytes[6] >> 4 == 5) {
            return " (SHA-1)";
        } else {
            return " (Random data)";
        }
    }

    private static byte[] toBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }
    private static int executeHash(CommandContext<CommandSourceStack> context, int version) {
        String name = StringArgumentType.getString(context, "string");
        UUID uuid = generateHashUuid(name, version);
        Component uuidText = Component.literal(uuid.toString());
        context.getSource().sendSuccess(() -> uuidText, false);
        return 1;
    }
    private static UUID generateHashUuid(String name, int version) {
        MessageDigest hasher;
        try {
            hasher = MessageDigest.getInstance(version == 3 ? "MD5" : "SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not supported.");
        }

        byte[] hashBytes = hasher.digest(name.getBytes(StandardCharsets.UTF_8));

        // Set version
        hashBytes[6] &= 0x0f;  // Clear version
        hashBytes[6] |= (byte) ((version << 4) & 0xf0);  // Set version

        // Set variant
        hashBytes[8] &= 0x3f;  // Clear variant
        hashBytes[8] |= (byte) 0x80;  // Set to variant 1

        // Create UUID
        return bytesToUuid(hashBytes);
    }

    private static UUID bytesToUuid(byte[] hashBytes) {
        long msb = 0;
        long lsb = 0;
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (hashBytes[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (hashBytes[i] & 0xff);
        return new UUID(msb, lsb);
    }
}
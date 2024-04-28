package io.github.densamisten.command;

import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

public class BookCommand {

    private static final ClipboardManager clipboardManager = new ClipboardManager();

    public BookCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("book")
                        .then(Commands.literal("write")
                                .then(Commands.literal("nbt")
                                        .executes(BookCommand::writeNbt))
                                .then(Commands.literal("text")
                                        .executes(BookCommand::writeText)))
                        .then(Commands.literal("read")
                                .then(Commands.literal("nbt")
                                        .executes(BookCommand::readNbt))
                                .then(Commands.literal("text")
                                        .executes(BookCommand::readText)))
                        .then(Commands.literal("copy")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> copyBook(context, EntityArgument.getPlayer(context, "target"))))));
    }

    private static int writeText(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        ItemStack heldItem = player != null ? player.getMainHandItem() : null;

        // Check if the held item is a writable book
        if (heldItem.getItem() == Items.WRITABLE_BOOK) {
            String clipboardText = clipboardManager.getClipboard(GLFW.glfwGetCurrentContext(), GLFWErrorCallback.createPrint(System.err));

            // Check if clipboard has text
            if (clipboardText.isEmpty()) {
                context.getSource().sendFailure(Component.literal("Clipboard is empty!"));
                return 0;
            }

            ListTag pages = new ListTag();
            StringBuilder currentPage = new StringBuilder();

            // Iterate over clipboard contents and write them to book pages
            for (int i = 0; i < clipboardText.length(); i++) {
                // Append character to current page
                currentPage.append(clipboardText.charAt(i));

                // Check if the current page exceeds the character limit or if all clipboard content has been processed
                if (currentPage.length() >= 200 || i == clipboardText.length() - 1) {
                    // Add current page to the list of pages
                    pages.add(StringTag.valueOf(currentPage.toString()));

                    // Reset current page
                    currentPage = new StringBuilder();
                }
            }

            // Add pages to the book
            writeTextToBook(heldItem, pages);

            context.getSource().sendSuccess(() -> Component.literal("Clipboard contents written to the book!"), false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("You need to hold a writable book in your hand!"));
            return 0;
        }
    }


    private static void writeTextToBook(ItemStack book, ListTag pages) {
        CompoundTag bookTag = book.getOrCreateTag();
        bookTag.put("pages", pages);
    }


    private static int writeNbt(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        ItemStack heldItem = player.getMainHandItem();

        // Check if the held item is a writable book
        if (heldItem.getItem() == Items.WRITABLE_BOOK) {
            String clipboardText = clipboardManager.getClipboard(GLFW.glfwGetCurrentContext(), GLFWErrorCallback.createPrint(System.err));

            // Check if clipboard has text
            if (clipboardText.isEmpty()) {
                context.getSource().sendFailure(Component.literal("Clipboard is empty!"));
                return 0;
            }

            // Add clipboard text as raw data to the book
            CompoundTag bookTag = heldItem.getOrCreateTag();
            bookTag.putString("raw_data", clipboardText);

            context.getSource().sendSuccess(() -> Component.literal("Clipboard contents written to the book!"), false);
            return 1;
        } else {
            context.getSource().sendFailure(Component.literal("You need to hold a writable book in your hand!"));
            return 0;
        }
    }

    private static int readNbt(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() == Items.WRITABLE_BOOK || heldItem.getItem() == Items.WRITTEN_BOOK) {
            CompoundTag bookTag = heldItem.getTag();
            if (bookTag != null && bookTag.contains("raw_data")) {
                String rawData = bookTag.getString("raw_data");
                clipboardManager.setClipboard(GLFW.glfwGetCurrentContext(), rawData);
                context.getSource().sendSuccess(() -> Component.literal("Clipboard contents read from the book!"), false);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("The book does not contain raw data!"));
                return 0;
            }
        } else {
            context.getSource().sendFailure(Component.literal("You need to hold a written book in your hand!"));
            return 0;
        }
    }

    private static int readText(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        ItemStack heldItem = player.getMainHandItem();

        if (heldItem.getItem() == Items.WRITABLE_BOOK || heldItem.getItem() == Items.WRITTEN_BOOK) {
            CompoundTag bookTag = heldItem.getTag();
            if (bookTag != null && bookTag.contains("pages")) {
                ListTag pagesTag = bookTag.getList("pages", 8); // Assuming it's a ListTag of strings
                StringBuilder textBuilder = new StringBuilder();
                for (int i = 0; i < pagesTag.size(); i++) {
                    textBuilder.append(pagesTag.getString(i)).append("\n");
                }
                String text = textBuilder.toString().trim();
                clipboardManager.setClipboard(GLFW.glfwGetCurrentContext(), text);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("The book is empty!"));
                return 0;
            }
        } else {
            context.getSource().sendFailure(Component.literal("You need to hold a written book in your hand!"));
            return 0;
        }
    }


    private static int copyBook(CommandContext<CommandSourceStack> context, ServerPlayer targetPlayer) throws CommandSyntaxException {
        CommandSourceStack sourceStack = context.getSource();
        ServerPlayer sourcePlayer = sourceStack.getPlayerOrException();

        // Check if the source player is holding a book
        ItemStack heldItem = sourcePlayer.getMainHandItem();
        if (!(heldItem.getItem() instanceof WritableBookItem || heldItem.getItem() instanceof WrittenBookItem)) {
            sourceStack.sendFailure(Component.literal("You must be holding a written or writable book to copy."));
            return 0;
        }

        // Copy the book to the target player's inventory
        ItemStack copiedBook = heldItem.copy();
        copiedBook.setCount(1); // Ensure only one copy is added
        // If the book is written, get its title and print it
        if (heldItem.getItem() instanceof WrittenBookItem) {
            CompoundTag bookTag = heldItem.getTag();
            if (bookTag != null && bookTag.contains("title")) {
                String title = bookTag.getString("title");
                sourceStack.sendSuccess(() -> Component.literal("Copied book \"" + title + "\" to " + targetPlayer.getName().getString() + "'s inventory."), true);
                targetPlayer.displayClientMessage(Component.literal("You received a copied book \"" + title + "\" from " + sourcePlayer.getName().getString() + "."), false);
            }
        }
        Inventory targetInventory = targetPlayer.getInventory();
        targetInventory.add(copiedBook);

        return 1;
    }


}
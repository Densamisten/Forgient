package io.github.densamisten.gui;

import io.github.densamisten.Forgient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.NetworkContext;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ForgientMenus extends AbstractContainerMenu {
    // Create a Deferred Register to hold "Forgient Menu" which will all be registered under the "forgient" namespace
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Forgient.MOD_ID);

    public static final RegistryObject<MenuType<ForgientMenus>> FORGIENT_MENU = MENUS.register("forgient_menu", () ->
            new MenuType<>(ForgientMenus::new, null)
    );

    public ForgientMenus(int containerId, Inventory playerInv) {
        super(FORGIENT_MENU.get(), containerId);
        // Initialize your menu here
    }

    public ForgientMenus(int containerId, Inventory playerInv, FriendlyByteBuf extraData) {
        super(FORGIENT_MENU.get(), containerId);
        // Initialize your menu with extra data here
    }

    @Override
    public boolean stillValid(Player player) {
        // Your stillValid logic here
        return true; // Return true for the sake of example
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        // Your quickMoveStack logic here
        return ItemStack.EMPTY; // Return empty stack for the sake of example
    }
    public static void openMenu(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
           serverPlayer.openMenu(new MenuProvider() {

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInv, Player player) {
                    return new ForgientMenus(windowId, playerInv);
                }

                @Override
                public Component getDisplayName() {
                    return Component.literal("Your Menu Title");
                }
            });
        }
    }

    public static void register(IEventBus eventBus) {
        // Register your menus here if necessary
        MENUS.register(eventBus);
    }
}

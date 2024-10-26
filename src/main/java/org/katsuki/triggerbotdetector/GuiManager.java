package org.katsuki.triggerbotdetector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;
import java.util.stream.Collectors;

public class GuiManager {
    private final TriggerBotDetector plugin;
    private final PunishmentManager punishmentManager;

    public GuiManager(TriggerBotDetector plugin, PunishmentManager punishmentManager) {
        this.plugin = plugin;
        this.punishmentManager = punishmentManager;
    }

    public void openSuspiciousPlayersGUI(Player admin) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_GRAY + plugin.getConfig().getString("messages.gui_title", "Suspicious Players"));

        plugin.getPlayerStats().forEach((uuid, stats) -> {
            if (stats.isSuspicious(150, 5, 50.0, 0.80)) {
                ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD, 1);
                ItemMeta meta = playerItem.getItemMeta();
                meta.setDisplayName(ChatColor.RED + Bukkit.getOfflinePlayer(uuid).getName());
                meta.setLore(plugin.getPlayerStats().entrySet().stream()
                        .map(entry -> ChatColor.GRAY + "Player: " + entry.getKey() + " Stats: " + entry.getValue().getLastInteractionTime())
                        .collect(Collectors.toList()));
                playerItem.setItemMeta(meta);
                gui.addItem(playerItem);
            }
        });

        admin.openInventory(gui);
    }

    public void handleInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equalsIgnoreCase(plugin.getConfig().getString("messages.gui_title", "Suspicious Players"))) {
            event.setCancelled(true);  // Prevent normal actions

            if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null) {
                Player admin = (Player) event.getWhoClicked();
                String playerName = ChatColor.stripColor(event.getCurrentItem().getItemMeta().getDisplayName());
                Player target = Bukkit.getPlayer(playerName);

                if (target != null) {
                    punishmentManager.kickPlayer(target);
                    admin.sendMessage("§aYou have kicked " + playerName + ".");
                }
            }
        }
    }
}
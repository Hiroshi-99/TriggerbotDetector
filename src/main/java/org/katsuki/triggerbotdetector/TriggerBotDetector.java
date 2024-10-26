package org.katsuki.triggerbotdetector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TriggerBotDetector extends JavaPlugin implements Listener {
    private final Map<UUID, PlayerStats> playerStats = new HashMap<>();
    private ConfigManager configManager;
    private PunishmentManager punishmentManager;
    private GuiManager guiManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        punishmentManager = new PunishmentManager(this, configManager);
        guiManager = new GuiManager(this, punishmentManager);
        scheduleDataCleanup();
        getCommand("triggerbot").setExecutor(new CommandManager(this, configManager, guiManager));

        if (configManager.isEnabled) {
            Bukkit.getServer().getPluginManager().registerEvents(this, this);
            sendStartupMessage();
        } else {
            getLogger().info("TriggerBotDetector is disabled in the configuration.");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("TriggerBotDetector has been disabled.");
    }

    public Map<UUID, PlayerStats> getPlayerStats() {
        return playerStats;
    }

    private void sendStartupMessage() {
        String[] startupMessage = {
                ChatColor.DARK_GRAY + "--------------------------------------------",
                ChatColor.DARK_AQUA + "         TriggerBotDetector Plugin v1.1",
                ChatColor.DARK_AQUA + "      Developed by Katsuki",
                ChatColor.DARK_GRAY + "--------------------------------------------",
                ChatColor.GREEN + "Status: " + (configManager.isEnabled ? ChatColor.GOLD + "Enabled" : ChatColor.RED + "Disabled"),
                ChatColor.DARK_GRAY + "--------------------------------------------"
        };

        for (String line : startupMessage) {
            getServer().getConsoleSender().sendMessage(line);
        }
    }

    private void scheduleDataCleanup() {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            long currentTime = System.currentTimeMillis();
            playerStats.entrySet().removeIf(entry ->
                    (currentTime - entry.getValue().getLastInteractionTime()) > 10 * 60 * 1000);  // Remove old data after 10 minutes
        }, 6000L, 6000L);  // Run every 5 minutes (6000 ticks)
    }
}
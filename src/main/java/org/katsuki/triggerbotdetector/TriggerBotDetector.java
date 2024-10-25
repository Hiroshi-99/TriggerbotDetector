// File: src/main/java/org/katsuki/triggerbotdetector/TriggerBotDetector.java
package org.katsuki.triggerbotdetector;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TriggerBotDetector extends JavaPlugin implements Listener {

    private final Map<UUID, PlayerStats> playerStats = new HashMap<>();
    private int maxAverageAttackInterval;
    private int minAttackCount;
    private double maxStdDeviation;
    private double tolerancePercentage;
    private boolean warnPlayer;
    private boolean kickPlayer;
    private boolean isEnabled;
    private String warningMessage;
    private String kickMessage;

    @Override
    public void onEnable() {
        // Load configuration
        saveDefaultConfig();
        loadConfiguration();

        // Print a beautiful startup message
        sendStartupMessage();

        if (isEnabled) {
            Bukkit.getServer().getPluginManager().registerEvents(this, this);
            getLogger().info("TriggerBotDetector has been enabled.");
        } else {
            getLogger().info("TriggerBotDetector is disabled in the configuration.");
        }
    }

    private void loadConfiguration() {
        FileConfiguration config = getConfig();
        this.maxAverageAttackInterval = config.getInt("settings.sensitivity.max_average_attack_interval", 150);
        this.minAttackCount = config.getInt("settings.sensitivity.min_attack_count", 5);
        this.maxStdDeviation = config.getDouble("settings.sensitivity.max_std_deviation", 50.0);
        this.tolerancePercentage = config.getDouble("settings.sensitivity.tolerance_percentage", 0.80);
        this.warnPlayer = config.getBoolean("settings.actions.warn_player", true);
        this.kickPlayer = config.getBoolean("settings.actions.kick_player", false);
        this.isEnabled = config.getBoolean("settings.enabled", true);
        this.warningMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.warning", "&c[TriggerBotDetector] Unusual attack patterns detected! Please play fair."));
        this.kickMessage = ChatColor.translateAlternateColorCodes('&', config.getString("messages.kick", "&c[TriggerBotDetector] You have been kicked for suspicious activity."));
    }

    @Override
    public void onDisable() {
        getLogger().info("TriggerBotDetector has been disabled.");
    }

    // Method to send a beautiful startup message to the console
    private void sendStartupMessage() {
        String[] startupMessage = {
                ChatColor.DARK_GRAY + "--------------------------------------------",
                ChatColor.DARK_AQUA + "         TriggerBotDetector Plugin v1.1",
                ChatColor.DARK_AQUA + "      Developed by Katsuki",
                ChatColor.DARK_GRAY + "--------------------------------------------",
                ChatColor.GREEN + "Status: " + (isEnabled ? ChatColor.GOLD + "Enabled" : ChatColor.RED + "Disabled"),
                ChatColor.DARK_GRAY + "--------------------------------------------"
        };

        for (String line : startupMessage) {
            getServer().getConsoleSender().sendMessage(line);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("triggerbot")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("triggerbot.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }

                // Reload the configuration
                reloadConfig();
                loadConfiguration();
                sender.sendMessage(ChatColor.GREEN + "TriggerBotDetector configuration reloaded successfully.");
                return true;
            }

            sender.sendMessage(ChatColor.RED + "Invalid usage. Try /triggerbot reload.");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        playerStats.putIfAbsent(playerId, new PlayerStats());
        PlayerStats stats = playerStats.get(playerId);
        stats.recordInteraction();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled || !(event.getDamager() instanceof Player)) return;

        Player player = (Player) event.getDamager();
        UUID playerId = player.getUniqueId();

        playerStats.putIfAbsent(playerId, new PlayerStats());
        PlayerStats stats = playerStats.get(playerId);
        stats.recordAttack(event.getEntity().getLocation());

        if (stats.isSuspicious(maxAverageAttackInterval, minAttackCount, maxStdDeviation, tolerancePercentage)) {
            if (warnPlayer) {
                warnPlayer(player);
            }
            if (kickPlayer) {
                kickPlayer(player);
            }
            getLogger().warning(player.getName() + " might be using a trigger bot!");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        playerStats.putIfAbsent(playerId, new PlayerStats());
        PlayerStats stats = playerStats.get(playerId);
        stats.recordMovement(event.getTo());
    }

    private void warnPlayer(Player player) {
        player.sendMessage(warningMessage);
    }

    private void kickPlayer(Player player) {
        player.kickPlayer(kickMessage);
        getLogger().warning(player.getName() + " has been kicked for using a suspected trigger bot.");
    }

    private static class PlayerStats {
        private int interactionCount;
        private int attackCount;
        private long lastInteractionTime;
        private long lastAttackTime;
        private long totalInteractionInterval;
        private long totalAttackInterval;
        private List<Long> attackIntervals = new ArrayList<>();
        private org.bukkit.Location lastMovementLocation;

        public void recordInteraction() {
            long currentTime = System.currentTimeMillis();
            if (lastInteractionTime != 0) {
                totalInteractionInterval += (currentTime - lastInteractionTime);
            }
            lastInteractionTime = currentTime;
            interactionCount++;
        }

        public void recordAttack(org.bukkit.Location attackLocation) {
            long currentTime = System.currentTimeMillis();
            if (lastAttackTime != 0) {
                long interval = currentTime - lastAttackTime;
                attackIntervals.add(interval);
                totalAttackInterval += interval;
            }
            lastAttackTime = currentTime;
            attackCount++;
        }

        public void recordMovement(org.bukkit.Location newLocation) {
            // Update player's last movement location for context-based analysis
            lastMovementLocation = newLocation;
        }

        public boolean isSuspicious(int maxInterval, int minCount, double maxStdDeviation, double tolerancePercentage) {
            if (attackCount >= minCount) {
                long averageAttackInterval = totalAttackInterval / attackCount;

                // Calculate standard deviation to identify if the clicks are consistent or random
                double variance = 0.0;
                for (long interval : attackIntervals) {
                    variance += Math.pow(interval - averageAttackInterval, 2);
                }
                variance /= attackIntervals.size();
                double stdDeviation = Math.sqrt(variance);

                // If the average interval is below the threshold and the consistency is high, it's suspicious
                if (averageAttackInterval < maxInterval && stdDeviation < maxStdDeviation) {
                    // Allow some tolerance percentage for occasional fast clicks
                    int consistentFastClicks = (int) attackIntervals.stream().filter(i -> i < maxInterval).count();
                    double consistencyRate = (double) consistentFastClicks / attackCount;
                    return consistencyRate > tolerancePercentage;
                }
            }
            return false;
        }
    }
}
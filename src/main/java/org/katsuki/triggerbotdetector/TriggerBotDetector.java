// File: src/main/java/org/katsuki/triggerbotdetector/TriggerBotDetector.java
package org.katsuki.triggerbotdetector;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TriggerBotDetector extends JavaPlugin implements Listener {

    private final Map<UUID, PlayerStats> playerStats = new HashMap<>();
    private int maxAverageAttackInterval;
    private int minAttackCount;
    private boolean warnPlayer;
    private boolean kickPlayer;
    private boolean isEnabled;

    @Override
    public void onEnable() {
        // Load configuration
        saveDefaultConfig();
        loadConfiguration();

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
        this.warnPlayer = config.getBoolean("settings.actions.warn_player", true);
        this.kickPlayer = config.getBoolean("settings.actions.kick_player", false);
        this.isEnabled = config.getBoolean("settings.enabled", true);
    }

    @Override
    public void onDisable() {
        getLogger().info("TriggerBotDetector has been disabled.");
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

        if (stats.isSuspicious(maxAverageAttackInterval, minAttackCount)) {
            if (warnPlayer) {
                warnPlayer(player);
            }
            if (kickPlayer) {
                player.kickPlayer("You have been kicked for suspicious activity.");
                getLogger().warning(player.getName() + " has been kicked for using a suspected trigger bot.");
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
        player.sendMessage("§cWarning: Unusual attack patterns detected! Play fair.");
    }

    private static class PlayerStats {
        private int interactionCount;
        private int attackCount;
        private long lastInteractionTime;
        private long lastAttackTime;
        private long totalInteractionInterval;
        private long totalAttackInterval;

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
                totalAttackInterval += (currentTime - lastAttackTime);
            }
            lastAttackTime = currentTime;
            attackCount++;
        }

        public void recordMovement(org.bukkit.Location newLocation) {
            // Movement logic can be used to further analyze suspicious activity
        }

        public boolean isSuspicious(int maxInterval, int minCount) {
            if (attackCount >= minCount) {
                long averageAttackInterval = totalAttackInterval / attackCount;
                return averageAttackInterval < maxInterval;
            }
            return false;
        }
    }
}
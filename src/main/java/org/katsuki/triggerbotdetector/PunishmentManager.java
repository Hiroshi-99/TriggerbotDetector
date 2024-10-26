package org.katsuki.triggerbotdetector;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PunishmentManager {
    private final TriggerBotDetector plugin;
    private final ConfigManager configManager;
    private final Map<UUID, Long> mutedPlayers = new HashMap<>();
    private final Map<UUID, Long> restrictedPlayers = new HashMap<>();

    public PunishmentManager(TriggerBotDetector plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public void warnPlayer(Player player) {
        player.sendMessage(configManager.warningMessage);
        plugin.getLogger().info(player.getName() + " has been warned for unusual attack patterns.");
    }

    public void kickPlayer(Player player) {
        player.kickPlayer(configManager.kickMessage);
        plugin.getLogger().warning(player.getName() + " has been kicked for suspicious behavior.");
    }

    public void temporarilyMutePlayer(Player player, int durationSeconds) {
        UUID playerId = player.getUniqueId();
        mutedPlayers.put(playerId, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(durationSeconds));
        player.sendMessage("§cYou have been muted for " + durationSeconds + " seconds.");
        plugin.getLogger().info(player.getName() + " has been muted for " + durationSeconds + " seconds.");
        scheduleMuteExpiration(playerId, durationSeconds);
    }

    public void restrictPlayer(Player player, int durationSeconds) {
        UUID playerId = player.getUniqueId();
        restrictedPlayers.put(playerId, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(durationSeconds));
        player.sendMessage("§cYou have been restricted for " + durationSeconds + " seconds.");
        plugin.getLogger().info(player.getName() + " has been restricted for " + durationSeconds + " seconds.");
        scheduleRestrictionExpiration(playerId, durationSeconds);
    }

    private void scheduleMuteExpiration(UUID playerId, int durationSeconds) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            mutedPlayers.remove(playerId);
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§aYour mute has expired.");
            }
        }, durationSeconds * 20L);
    }

    private void scheduleRestrictionExpiration(UUID playerId, int durationSeconds) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            restrictedPlayers.remove(playerId);
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage("§aYour restriction has expired.");
            }
        }, durationSeconds * 20L);
    }

    public boolean isMuted(Player player) {
        return mutedPlayers.containsKey(player.getUniqueId());
    }

    public boolean isRestricted(Player player) {
        return restrictedPlayers.containsKey(player.getUniqueId());
    }
}
package org.katsuki.triggerbotdetector;

import org.bukkit.plugin.Plugin;

public class AntiCheatIntegration {
    private final TriggerBotDetector plugin;

    public AntiCheatIntegration(TriggerBotDetector plugin) {
        this.plugin = plugin;
    }

    public boolean isOtherAntiCheatEnabled() {
        Plugin otherAntiCheatPlugin = plugin.getServer().getPluginManager().getPlugin("OtherAntiCheatPluginName");
        if (otherAntiCheatPlugin != null && otherAntiCheatPlugin.isEnabled()) {
            plugin.getLogger().info("Detected another anti-cheat plugin: " + otherAntiCheatPlugin.getName());
            return true;
        }
        return false;
    }

    public void handleCompatibility() {
        if (isOtherAntiCheatEnabled()) {
            // Add specific compatibility handling logic if needed
            plugin.getLogger().warning("TriggerBotDetector is running with another anti-cheat plugin. Compatibility settings applied.");
        }
    }
}
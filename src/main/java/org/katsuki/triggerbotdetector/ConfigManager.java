package org.katsuki.triggerbotdetector;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {
    public boolean warnPlayer;
    public boolean kickPlayer;
    public boolean isEnabled;
    public String warningMessage;
    public String kickMessage;
    public String guiTitle;

    private final FileConfiguration config;
    private final JavaPlugin plugin;
    private FileConfiguration messagesConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        loadConfiguration();
    }

    public void loadConfiguration() {
        String language = config.getString("settings.language", "en");
        loadLanguageFile(language);

        this.warnPlayer = config.getBoolean("actions.warn_player", true);
        this.kickPlayer = config.getBoolean("actions.kick_player", false);
        this.isEnabled = config.getBoolean("enabled", true);

        // Load messages from the messages config file
        this.warningMessage = getFormattedMessage("warning", "&c[TriggerBotDetector] Unusual attack patterns detected! Please play fair.");
        this.kickMessage = getFormattedMessage("kick", "&c[TriggerBotDetector] You have been kicked for suspicious activity.");
        this.guiTitle = getFormattedMessage("gui_title", "&aSuspicious Players");
    }

    private void loadLanguageFile(String language) {
        File langFile = new File(plugin.getDataFolder(), "messages_" + language + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("messages_" + language + ".yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    private String getFormattedMessage(String key, String defaultValue) {
        String message = messagesConfig.getString(key, defaultValue);
        if (message == null) {
            plugin.getLogger().warning("Message key '" + key + "' is missing in the language file. Using default value.");
            return ChatColor.translateAlternateColorCodes('&', defaultValue);
        }
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
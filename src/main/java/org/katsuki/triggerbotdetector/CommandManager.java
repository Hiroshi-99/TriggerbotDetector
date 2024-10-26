package org.katsuki.triggerbotdetector;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager implements CommandExecutor {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final GuiManager guiManager;

    public CommandManager(JavaPlugin plugin, ConfigManager configManager, GuiManager guiManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("triggerbot")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("triggerbot.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }

                plugin.reloadConfig();
                configManager.loadConfiguration();
                sender.sendMessage(ChatColor.GREEN + "TriggerBotDetector configuration reloaded successfully.");
                return true;
            }

            if (args.length > 0 && args[0].equalsIgnoreCase("gui")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                    return true;
                }

                Player player = (Player) sender;
                if (!player.hasPermission("triggerbot.gui")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }

                guiManager.openSuspiciousPlayersGUI(player);
                return true;
            }

            sender.sendMessage(ChatColor.RED + "Invalid usage. Try /triggerbot reload or /triggerbot gui.");
            return true;
        }
        return false;
    }
}
package com.cookievcban.commands;

import com.cookievcban.CookieVCBan;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class VCUnbanCommand implements BasicCommand {
    private final CookieVCBan plugin;
    private final MiniMessage miniMessage;

    public VCUnbanCommand(CookieVCBan plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();

        if (!sender.hasPermission("cookievcban.unban")) {
            String message = plugin.getConfig().getString("messages.no-permission", 
                "<red>You don't have permission to use this command.</red>");
            sender.sendMessage(miniMessage.deserialize(message));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /vcunban <player></red>"));
            return;
        }

        String playerName = args[0];
        
        // Try to get the player (online or offline)
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        OfflinePlayer targetPlayer = onlinePlayer != null ? onlinePlayer : Bukkit.getOfflinePlayer(playerName);

        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline()) {
            String message = plugin.getConfig().getString("messages.player-not-found", 
                "<red>Player not found.</red>");
            sender.sendMessage(miniMessage.deserialize(message));
            return;
        }

        if (plugin.getBanManager().unbanPlayer(targetPlayer.getUniqueId())) {
            String message = plugin.getConfig().getString("messages.unban-success", 
                "<green>Successfully unbanned <player> from voice chat.</green>");
            message = message.replace("<player>", targetPlayer.getName());
            sender.sendMessage(miniMessage.deserialize(message));
        } else {
            String message = plugin.getConfig().getString("messages.not-banned", 
                "<red><player> is not banned from voice chat.</red>");
            message = message.replace("<player>", targetPlayer.getName());
            sender.sendMessage(miniMessage.deserialize(message));
        }
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            String partial = args[0].toLowerCase();
            
            // Suggest banned players
            for (UUID uuid : plugin.getBanManager().getBannedPlayers()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                String name = player.getName();
                if (name != null && name.toLowerCase().startsWith(partial)) {
                    suggestions.add(name);
                }
            }
            
            return suggestions;
        }
        
        return List.of();
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender) {
        return sender.hasPermission("cookievcban.unban");
    }
}

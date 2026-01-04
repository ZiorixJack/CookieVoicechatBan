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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VCBanCommand implements BasicCommand {
    private final CookieVCBan plugin;
    private final MiniMessage miniMessage;
    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([dhms])");

    public VCBanCommand(CookieVCBan plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();

        if (!sender.hasPermission("cookievcban.ban")) {
            String message = plugin.getConfig().getString("messages.no-permission", 
                "<red>You don't have permission to use this command.</red>");
            sender.sendMessage(miniMessage.deserialize(message));
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(miniMessage.deserialize("<red>Usage: /vcban <player> [time|inf]</red>"));
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

        // Parse time duration
        long durationMillis = 0; // 0 = permanent
        String timeString = "permanently";
        
        if (args.length >= 2) {
            String timeArg = args[1].toLowerCase();
            if (!timeArg.equals("inf") && !timeArg.equals("infinite")) {
                Long parsed = parseTime(timeArg);
                if (parsed == null) {
                    String message = plugin.getConfig().getString("messages.invalid-time",
                        "<red>Invalid time format. Use format like: 1d, 12h, 30m, or 'inf' for permanent.</red>");
                    sender.sendMessage(miniMessage.deserialize(message));
                    return;
                }
                durationMillis = parsed;
                timeString = formatTime(durationMillis);
            }
        }

        if (plugin.getBanManager().banPlayer(targetPlayer.getUniqueId(), durationMillis)) {
            String messageKey = durationMillis == 0 ? "messages.ban-success-permanent" : "messages.ban-success";
            String message = plugin.getConfig().getString(messageKey, 
                "<green>Successfully banned <player> from voice chat.</green>");
            message = message.replace("<player>", targetPlayer.getName()).replace("<time>", timeString);
            sender.sendMessage(miniMessage.deserialize(message));
            
            // Notify the player if they're online using action bar
            if (onlinePlayer != null && plugin.getConfig().getBoolean("settings.notify-on-connect", true)) {
                String messageKeyNotif = durationMillis == 0 ? "messages.banned-notification" : "messages.banned-notification-temporary";
                String bannedMessage = plugin.getConfig().getString(messageKeyNotif,
                    "<red>You are banned from using voice chat.</red>");
                bannedMessage = bannedMessage.replace("<time>", timeString);
                onlinePlayer.sendActionBar(miniMessage.deserialize(bannedMessage));
            }
        } else {
            String message = plugin.getConfig().getString("messages.already-banned", 
                "<red><player> is already banned from voice chat.</red>");
            message = message.replace("<player>", targetPlayer.getName());
            sender.sendMessage(miniMessage.deserialize(message));
        }
    }
    
    private Long parseTime(String time) {
        Matcher matcher = TIME_PATTERN.matcher(time.toLowerCase());
        long totalMillis = 0;
        
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            switch (unit) {
                case "d": totalMillis += TimeUnit.DAYS.toMillis(value); break;
                case "h": totalMillis += TimeUnit.HOURS.toMillis(value); break;
                case "m": totalMillis += TimeUnit.MINUTES.toMillis(value); break;
                case "s": totalMillis += TimeUnit.SECONDS.toMillis(value); break;
            }
        }
        
        return totalMillis > 0 ? totalMillis : null;
    }
    
    private String formatTime(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m");
        
        return sb.toString().trim();
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            String partial = args[0].toLowerCase();
            
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partial)) {
                    suggestions.add(player.getName());
                }
            }
            
            return suggestions;
        } else if (args.length == 2) {
            return List.of("inf", "1h", "12h", "1d", "7d", "30d", "30m", "2h");
        }
        
        return List.of();
    }

    @Override
    public boolean canUse(@NotNull CommandSender sender) {
        return sender.hasPermission("cookievcban.ban");
    }
}

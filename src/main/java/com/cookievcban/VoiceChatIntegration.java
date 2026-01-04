package com.cookievcban;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class VoiceChatIntegration implements VoicechatPlugin {
    private final CookieVCBan plugin;
    private VoicechatApi voicechatApi;
    private VoicechatServerApi serverApi;
    private final MiniMessage miniMessage;

    public VoiceChatIntegration(CookieVCBan plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public String getPluginId() {
        return "cookievcban";
    }

    @Override
    public void initialize(VoicechatApi api) {
        this.voicechatApi = api;
        plugin.getLogger().info("Successfully hooked into SimpleVoiceChat API");
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
    }

    private void onServerStarted(VoicechatServerStartedEvent event) {
        this.serverApi = event.getVoicechat();
        plugin.getLogger().info("VoiceChat server started");
    }

    private void onMicrophonePacket(MicrophonePacketEvent event) {
        // Check if the sender is banned
        UUID playerUuid = event.getSenderConnection().getPlayer().getUuid();
        if (plugin.getBanManager().isBanned(playerUuid)) {
            // Cancel the packet so it won't be broadcast
            event.cancel();
            
            // Optionally notify the player using action bar
            if (plugin.getConfig().getBoolean("settings.notify-on-connect", true)) {
                Player player = Bukkit.getPlayer(playerUuid);
                if (player != null) {
                    long timeRemaining = plugin.getBanManager().getTimeRemaining(playerUuid);
                    String messageKey;
                    String message;
                    
                    if (timeRemaining == 0) {
                        // Permanent ban
                        messageKey = "messages.banned-notification";
                        message = plugin.getConfig().getString(messageKey,
                            "<red>You are banned from using voice chat.</red>");
                    } else {
                        // Temporary ban
                        messageKey = "messages.banned-notification-temporary";
                        message = plugin.getConfig().getString(messageKey,
                            "<red>You are banned from using voice chat for <time>.</red>");
                        message = message.replace("<time>", formatTime(timeRemaining));
                    }
                    
                    player.sendActionBar(miniMessage.deserialize(message));
                }
            }
        }
    }
    
    private String formatTime(long millis) {
        long days = millis / (24 * 60 * 60 * 1000);
        long hours = (millis / (60 * 60 * 1000)) % 24;
        long minutes = (millis / (60 * 1000)) % 60;
        
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d ");
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0) sb.append(minutes).append("m");
        
        return sb.toString().trim();
    }

    public void registerPlugin() {
        BukkitVoicechatService service = plugin.getServer().getServicesManager().load(BukkitVoicechatService.class);
        if (service != null) {
            service.registerPlugin(this);
            plugin.getLogger().info("Successfully registered VoiceChat plugin");
        } else {
            plugin.getLogger().warning("SimpleVoiceChat service not found! Make sure SimpleVoiceChat is installed.");
        }
    }
}

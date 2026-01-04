package com.cookievcban;

import com.cookievcban.commands.VCBanCommand;
import com.cookievcban.commands.UnVCBanCommand;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class CookieVCBan extends JavaPlugin {
    private BanManager banManager;
    private VoiceChatIntegration voiceChatIntegration;

    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize ban manager
        banManager = new BanManager(this);
        
        // Initialize VoiceChat integration
        voiceChatIntegration = new VoiceChatIntegration(this);
        
        // Register VoiceChat plugin
        getServer().getScheduler().runTask(this, () -> {
            voiceChatIntegration.registerPlugin();
        });
        
        // Register commands using Paper's Command API
        registerCommands();
        
        getLogger().info("CookieVCBan has been enabled!");
    }

    @Override
    public void onDisable() {
        // Save banned players on shutdown
        if (banManager != null) {
            banManager.saveBannedPlayers();
        }
        
        getLogger().info("CookieVCBan has been disabled!");
    }

    private void registerCommands() {
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register("vcban", "Ban a player from voice chat", new VCBanCommand(this));
            commands.register("unvcban", "Unban a player from voice chat", new UnVCBanCommand(this));
        });
    }

    public BanManager getBanManager() {
        return banManager;
    }

    public VoiceChatIntegration getVoiceChatIntegration() {
        return voiceChatIntegration;
    }
}

package com.cookievcban;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BanManager {
    private final CookieVCBan plugin;
    private final Map<UUID, Long> bannedPlayers; // UUID -> expiration timestamp (0 = permanent)
    private File dataFile;
    private FileConfiguration dataConfig;

    public BanManager(CookieVCBan plugin) {
        this.plugin = plugin;
        this.bannedPlayers = new HashMap<>();
        loadBannedPlayers();
    }

    private void loadBannedPlayers() {
        if (!plugin.getConfig().getBoolean("settings.save-to-file", true)) {
            return;
        }

        dataFile = new File(plugin.getDataFolder(), "banned-players.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create banned-players.yml: " + e.getMessage());
                return;
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        if (dataConfig.contains("banned-players")) {
            var section = dataConfig.getConfigurationSection("banned-players");
            if (section != null) {
                for (String uuidString : section.getKeys(false)) {
                    try {
                        UUID uuid = UUID.fromString(uuidString);
                        long expiration = section.getLong(uuidString, 0L);
                        bannedPlayers.put(uuid, expiration);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID in banned-players.yml: " + uuidString);
                    }
                }
            }
        }

        // Remove expired bans
        cleanupExpiredBans();
        plugin.getLogger().info("Loaded " + bannedPlayers.size() + " banned player(s)");
    }

    public void saveBannedPlayers() {
        if (!plugin.getConfig().getBoolean("settings.save-to-file", true)) {
            return;
        }

        if (dataConfig == null || dataFile == null) {
            return;
        }

        dataConfig.set("banned-players", null);
        for (Map.Entry<UUID, Long> entry : bannedPlayers.entrySet()) {
            dataConfig.set("banned-players." + entry.getKey().toString(), entry.getValue());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save banned-players.yml: " + e.getMessage());
        }
    }

    public void cleanupExpiredBans() {
        long now = System.currentTimeMillis();
        bannedPlayers.entrySet().removeIf(entry -> entry.getValue() != 0 && entry.getValue() < now);
    }

    public boolean banPlayer(UUID uuid, long durationMillis) {
        cleanupExpiredBans();
        if (isBanned(uuid)) {
            return false;
        }
        long expiration = durationMillis == 0 ? 0 : System.currentTimeMillis() + durationMillis;
        bannedPlayers.put(uuid, expiration);
        saveBannedPlayers();
        return true;
    }

    public boolean unbanPlayer(UUID uuid) {
        if (!bannedPlayers.containsKey(uuid)) {
            return false;
        }
        bannedPlayers.remove(uuid);
        saveBannedPlayers();
        return true;
    }

    public boolean isBanned(UUID uuid) {
        cleanupExpiredBans();
        return bannedPlayers.containsKey(uuid);
    }

    public long getTimeRemaining(UUID uuid) {
        Long expiration = bannedPlayers.get(uuid);
        if (expiration == null) return -1;
        if (expiration == 0) return 0; // Permanent
        long remaining = expiration - System.currentTimeMillis();
        return remaining > 0 ? remaining : -1;
    }

    public Set<UUID> getBannedPlayers() {
        cleanupExpiredBans();
        return bannedPlayers.keySet();
    }
}

# CookieVCBan

A Minecraft Paper 1.21.5 plugin that adds voice chat ban functionality with SimpleVoiceChat integration.

## Features

- Ban and unban players from using voice chat
- Persistent ban storage
- Customizable messages using MiniMessage format
- Permission-based access control
- SimpleVoiceChat API integration
- Paper Command API for modern command handling

## Commands

- `/vcban <player>` - Ban a player from using voice chat
  - Permission: `cookievcban.ban`
- `/vcunban <player>` - Unban a player from voice chat
  - Permission: `cookievcban.unban`

## Configuration

Edit `config.yml` to customize messages and settings:

```yaml
messages:
  ban-success: "<green>Successfully banned <player> from voice chat.</green>"
  unban-success: "<green>Successfully unbanned <player> from voice chat.</green>"
  already-banned: "<red><player> is already banned from voice chat.</red>"
  not-banned: "<red><player> is not banned from voice chat.</red>"
  player-not-found: "<red>Player not found.</red>"
  banned-notification: "<red>You are banned from using voice chat.</red>"
  no-permission: "<red>You don't have permission to use this command.</red>"

settings:
  notify-on-connect: true
  save-to-file: true
```

## Building

```bash
./gradlew build
```

The compiled plugin jar will be in `build/libs/`.

## Dependencies

- Paper 1.21.5
- SimpleVoiceChat

## Installation

1. Install Paper 1.21.5 server
2. Install SimpleVoiceChat plugin
3. Place CookieVCBan jar in the plugins folder
4. Restart the server
5. Configure the plugin in `plugins/CookieVCBan/config.yml`

## License

MIT License

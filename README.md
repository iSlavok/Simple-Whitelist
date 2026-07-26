# Simple Whitelist

This is a Fabric mod for Minecraft 1.21 that provides a simple whitelist system based on usernames.

## Features

- **Whitelist based on usernames:** The whitelist system is based on usernames, not UUIDs. Names are case-sensitive — `Steve` and `steve` are treated as different players.
- **Database storage:** You can choose between storing the whitelist data in a JSON file or a MySQL database.
- **ModMenu screen (client, 1.21+):** With [ModMenu](https://modrinth.com/mod/modmenu) installed, a minimal config screen lets you toggle the whitelist — handy when you open a singleplayer world to LAN.

## Server plugin (Bukkit/Spigot/Paper/Purpur/Folia)

The same whitelist is also available as a **server plugin** — one jar for Spigot, Paper,
Purpur and Folia (Folia-supported). It reuses the mod's JSON/MySQL storage and the same
case-sensitive, username-based rules, and needs no client mod.

- **Config:** `plugins/SimpleWhitelist/config.json` — same schema as the mod (see below).
- **Commands:** `/simplewhitelist add|remove|list|on|off` with aliases `swl`, `swh`,
  `swhitelist`; permission `simplewhitelist.command` (default: op — grant it via any
  permissions plugin, e.g. LuckPerms).
- **Supported versions:** Minecraft **1.18 → 26.2** (Java 17+ servers) — one jar, thanks
  to the stable Bukkit API.
- **Build locally:** `./gradlew -p plugin build` → `plugin/build/libs/*.jar`.
- **Try it on a real server:** `./gradlew -p plugin runServer` (override the version with
  `-Prun_mc=1.21.8`).

The plugin lives in a standalone Gradle build under `plugin/` and publishes to the same
Modrinth project as the mod. Client-only features (the ModMenu config screen) are mod-only
and have no plugin equivalent.

## Dependencies

Required:

- **[Fabric API](https://modrinth.com/mod/fabric-api)**
- **[Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)**

Optional:

- **A [fabric-permissions-api](https://github.com/lucko/fabric-permissions-api) provider** (e.g. **[LuckPerms](https://modrinth.com/mod/luckperms)**) for permission-based command access. Without it, commands fall back to vanilla operator level 4.

## Configuration

The default configuration file is `config\SimpleWhitelist\config.json`:

```json
{
    "whitelist": true,
    "databaseType": "json",
    "mysqlUrl": "jdbc:mysql://<username>:<password>@<host>:<port>/<database>",
    "jsonFileName": "whitelist.json"
}
```

**Configuration options:**

- `whitelist`: Enable or disable the whitelist.
- `databaseType`: The type of database to use. Can be either `json` or `mysql`.
- `mysqlUrl`: The connection string for the MySQL database.
- `jsonFileName`: The name of the JSON file to store the whitelist data.

## Commands

- **`simplewhitelist list`:** List all players on the whitelist.
- **`simplewhitelist add <player>`:** Add a player to the whitelist.
- **`simplewhitelist remove <player>`:** Remove a player from the whitelist.
- **`simplewhitelist on`:** Enable the whitelist.
- **`simplewhitelist off`:** Disable the whitelist.

**`simplewhitelist`aliases:** `swl`, `swh`, `swhitelist`

## Permissions

- **`simplewhitelist.command`:** Allows players to use the whitelist commands.

## Installation

1. Download the latest release of Simple Whitelist from [here](https://modrinth.com/mod/simple-whitelist).
2. Place the `.jar` file in your `mods` folder.



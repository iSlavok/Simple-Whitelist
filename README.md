# Simple Whitelist

This is a Fabric mod for Minecraft 1.21 that provides a simple whitelist system based on usernames.

## Features

- **Whitelist based on usernames:** The whitelist system is based on usernames, not UUIDs.
- **Database storage:** You can choose between storing the whitelist data in a JSON file or a MySQL database.

## Dependencies

This mod requires the following dependencies:

- **[Fabric API](https://modrinth.com/mod/fabric-api)**
- **[Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)**
- **[LuckPerms](https://modrinth.com/mod/luckperms)**

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



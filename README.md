# MinecraftPlaytime

This is a Spigot Plugin for limiting play time for server users.
Users have a limited play time (except they have the permission `playtime.infinite`.
After this time, the player is kicked from the server. 
The playtime resets automatically each day.
The administrator has also the possibility to temporarily add or remove play time
from the player.

There are warnings at 10min, 5min, 1min, 30sec before the end of the play time and a countdown beginning at 15s before the player is kicked.

## Dependencies

- [Vault](https://www.spigotmc.org/resources/vault.34315/)
- [TitleAPI](https://www.spigotmc.org/resources/titleapi-1-8-1-14-2.1325/) (optional) for showing nice titles

## Commands
- `playtime` 
  - Shows time left to play today
- `playtime <playername>` 
  - Shows time left to play today for another player (
  - needs permission `playtime.others`
- `playtime reload` 
  - Reloads the plugin configuration 
  - needs permission `playtime.admin`
- `playtime <playername> add <value>`
  - adds play time to the players config for today
  - `<value>` is a number followed by `h` for hours or `m` for minutes, e.g. `1h` or `30m`
  - needs permission `playtime.admin`
- `playtime <playername> removes <value>`
  - removes play time from the players config for today
  - `<value>` is a number followed by `h` for hours or `m` for minutes, e.g. `1h` or `30m`
  - needs permission `playtime.admin`
- `playtime <playername> resets`
  - resets the play time for the player to its initial value
  - `<value>` is a number followed by `h` for hours or `m` for minutes, e.g. `1h` or `30m`
  - needs permission `playtime.admin`

## Permissions
- `playtime.others`
  - Enables the ability to see the left over play time of other players
- `playtime.infinite`
  - Users with this permission can play as long as they want
- `playtime.premium`
  - It is possible to set a different play time in the `config.yml` for a premium group.
- `playtime.admin`
  - Needed for the set/remove/reset commands
  - implies `playtime.others` and `playtime.infinite`

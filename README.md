# Breach

## Config

Most fields in the following config are optional, and are shown here with their default values.
The config is commented here, but note that comments are not permitted in the actual config.

```json5
{
  // Used by plasmid, not breach. Must be exactly "breach:breach". Required.
  "type": "breach:breach",
  "map": {
    // Map to use. Required.
    "id": "breach:village",
    // The world will be set to this time, as if the /time command was run. 6000 = noon and 18000 = midnight.
    "time_of_day": 6000
  },
  // The first team to win this many rounds will win the whole game.
  "score_needed_to_win": 3,
  "attacker_kits": {
    // Individual kit ids (possibly beyond those specified in the categories below) to add to the list of available kits
    // and move to the front of the list.
    // This can probably stay as an empty list, since the "categories" option is the primary way by which kits are made
    // available.
    "kits": [],
    // All kits which match any of the categories in this list will be available.
    // It is recommended to leave this alone.
    "categories": ["attacker"]
  },
  "defender_kits": {
    // Like "attacker_kits.kits", but for defenders.
    "kits": [],
    // Like "attacker_kits.categories", but for defenders.
    "categories": ["defender"]
  },
  "teams": {
    // If true, players can join teams even when the team is full.
    "remove_team_restrictions": false,
    // If false, the items in the waiting lobby that let you select a team will only be available before the first round
    // of the game starts; it won't be available between rounds.
    "allow_team_changes_after_first_round": false,
    // If true, the team that is attacking first will be selected randomly.
    // If false, the red team will always attack first.
    "randomize_first_attacking_team": true,
    // If true, each team will alternate between attacking and defending.
    // If false, the same team will be attacking every game.
    "swap_roles_after_each_round": true,
    // If false, players will not be given helmets when they receive their team-colored leather armor.
    "give_helmets": true
  },
  "gameplay": {
    // If true, players will always be at max hunger, but their saturation will not contribute to their regeneration.
    // Players will still regenerate (assuming "disable_natural_regeneration" is false), but at a slow rate.
    "disable_hunger": true,
    // If true, players cannot regain health through hunger, as if the gamerule naturalRegeneration was set to false.
    "disable_natural_regeneration": true,
    // If true, blocks will not drop items, as if the gamerule doTileDrops was set to false.
    "disable_tile_drops": true,
    // If true, fire will not spread or disappear, as if the gamerule doFireTick was set to false.
    "disable_fire_tick": true,
    // If true, arrows will instantly kill their targets.
    // It is recommended to leave this enabled, as it is one of the core characteristics of the game.
    "arrows_instant_kill": true
  },
  "times": {
    // Length of the "Prep" phase, in seconds
    "prep_length_in_seconds": 30,
    // Length of the round after the prep phase ends.
    // Total round length = prep_length_in_seconds + round_length_in_seconds.
    "round_length_in_seconds": 180,
    // Number of seconds to wait before starting the game if the threshold of players has not yet been reached.
    "lobby_ready_seconds": 30,
    // Number of seconds to wait before starting the game if the threshold has been reached or exceeded.
    "lobby_full_seconds": 10,
    // Seconds after each round before sending players to the next waiting lobby or transitioning into the
    // "game over" phase in the case of the last round.
    "seconds_after_round_end_before_next": 10,
    // Seconds that players should be in the "game over" phase after the last round before the game closes.
    "seconds_after_game_end_before_closure": 10
  },
  // Number of targets that the attackers have to break to win the round.
  "number_of_targets": 2,
  // If true, then a glowing box (a block display) will be placed around active targets.
  "outline_targets": true,
  // If true, displays the number of players left on each team as a title message whenever a player dies.
  "remaining_players_popup": true
}
```

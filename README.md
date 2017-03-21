


# Godot GoogleService

google service for godot (android)

> Login

> Logout

> achievements

> leaderboard

# google play service API
```
var google = Globals.get_singleton("GooglePlay");
google.init(get_instance_id());

# Google play Login
google.login();

# Google play Logout
google.logout();

# Google play achievements
google.unlock_achievement("achievementID"); // unlock achievement;
google.increse_achievement("achievementID", int(n)); // increse achievements by step.
google.show_achievements(); // show achievements;

# Google play Leaderboards
google.submit_leaderboard(int(score), "leaderboardID"); // submit score to leaderboard
google.show_leaderboard("leaderboardID"); // show leaderboard
google.show_leaderboards(); // show all available leaderboard

```

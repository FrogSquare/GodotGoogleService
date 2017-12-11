


# Godot GoogleService

google service for godot (android)

> Login

> Logout

> achievements

> leaderboard

# Enabling GooglePlayService
Edit engine.cfg and add
```
[android]
modules="org/godotengine/godot/GooglePlay"
```

# GDScript - getting module singleton and initializing;
```
var google = Globals.get_singleton("GooglePlay");

func _ready():
	if OS.get_name() == "Android":
		google.init(get_instance_id());

func _receive_message(from, key, data):
	if from == "GooglePlay":
		print("Key: ", key, " Data: ", data)

```

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

# Log
```
adb -d logcat godot:V GoogleService:V DEBUG:V AndroidRuntime:V ValidateServiceOp:V *:S
```

And if you are using [GodotFirebase](http://github.com/FrogSquare/GodotFireBase) add these, `GodotSQL:V FireBase:V` to the command

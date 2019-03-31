# Godot GoogleService

GodotGoogleService is a google play games integration for godot android;

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://github.com/FrogSquare/GodotFireBase)
[![GodotEngine](https://img.shields.io/badge/Godot_Engine-2.X%20/%203.X-blue.svg)](https://github.com/godotengine/godot)
[![LICENCE](https://img.shields.io/badge/License-Apache_V2-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![PATREON](https://img.shields.io/badge/Patreon-support-yellow.svg)](https://www.patreon.com/bePatron?u=5130479)

# Cloning
```
cd ${GODOT_ROOT}/modules/
git clone https://github.com/FrogSquare/GodotGoogleAds GodotGoogleAds
git clone https://github.com/FrogSquare/GodotSql GodotSql
```
 and you must configure your module by editing `${GODOT_ROOT}/modules/GodotGoogleAds/config.py`

# Depends on

> Godot game engine: `git clone https://github.com/godotengine/godot`

# Available Features

> Login

> Logout

> achievements

> leaderboard

# Build/Compile module

* Edit file modules/GodotGoogleService/config.py at line 2
```
p_app_id = "com.your.appid"     # config.py L:2
```

* Replay `com.your.appid` with you android application id.

# Initialize GodotGoogleService

Edit engine.cfg and add
```
[android]
modules="org/godotengine/godot/GooglePlay"
```

# GDScript - getting module singleton and initializing;

### On 2.X

```
var google = Globals.get_singleton("GooglePlay");
```

### On 3.X (latest from git)

```
var google = Engine.get_singleton("GooglePlay");
```

And initialize GodotGoogleService with script instance id

```
func _ready():
	if OS.get_name() == "Android":
		google.init(get_instance_ID()) # use get_instance_id () for Godot 3.X

func _receive_message(from, key, data):
	if from == "GooglePlay":
		print("Key: ", key, " Data: ", data)

```

# google play service Signin / Signout
```
google.login()
google.logout()
```

# Google play achievements
```
google.unlock_achievement("achievementID") # unlock achievement;
google.increase_achievement("achievementID", int(n)) # increse achievements by step.
google.show_achievements() # show achievements;
```

# Google play Leaderboards
```
google.submit_leaderboard(int(score), "leaderboardID") # submit score to leaderboard
google.show_leaderboard("leaderboardID") # show leaderboard
google.show_leaderboards() # show all available leaderboard
```

# Additional
```
google.get_version_code() # get package version code (Helper)
```

# Log Event
```
adb -d logcat godot:V GoogleService:V FrogSquare:V SignInIntentService:V SignInIntentService:V SignInActivity:V DEBUG:V AndroidRuntime:V ValidateServiceOp:V *:S
```

And if you are using [GodotFirebase](http://github.com/FrogSquare/GodotFireBase) add these, `GodotSQL:V FireBase:V` to the command


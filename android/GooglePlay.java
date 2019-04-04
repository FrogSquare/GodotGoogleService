
package org.godotengine.godot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.IntentSender.SendIntentException;
import android.util.Log;
import android.view.View;
import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONException;

public class GooglePlay extends Godot.SingletonBase {

	 public static Godot.SingletonBase initialize (Activity p_activity) {
		return new GooglePlay(p_activity);
	}

	public GooglePlay(Activity p_activity) {
		registerClass ("GooglePlay", new String[] {
			"init", "set_debug", "login", "logout", "unlock_achievement",
			"increase_achievement", "show_achievements",
			"submit_leaderboard", "show_leaderboard", "show_leaderboards",
			"get_version_code"
		});

		activity = p_activity;
		if (playService == null) {
			synchronized(PlayService.class) {
				playService = new PlayService(p_activity);
			}
		}
	}

	public int get_version_code(final int instanceID) {
		try {
			final PackageInfo pInfo =
			activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);

			return pInfo.versionCode;
		} catch (NameNotFoundException e) { }

		return 0;
	}


	public void init(final int instanceID) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				playService.init(instanceID);
			}
		});
	}

    public void set_debug(final boolean p_value) {
        Utils.set_debug(TAG, p_value);
    }

	public void initWithDict(final Dictionary dict, final int instanceID) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				playService.initAdvanced(dict, instanceID);
			}
		});
	}

	public void login() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				playService.connect();
			}
		});
	}

	public void logout() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				playService.disconnect();
			}
		});
	}

	public boolean isConnected() {
		return playService.isConnected();
	}

	public void unlock_achievement(final String id) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				playService.achievement_unlock(id);
			}
		});
	}

	public void increase_achievement(final String id, final int steps) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				playService.achievement_increment(id, steps);
			}
		});
	}

	public void show_achievements() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				playService.achievement_show_list();
			}
		});
	}

	public void submit_leaderboard(final int score, final String l_id) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				playService.leaderboard_submit(l_id, score);
			}
		});
	}

	public void show_leaderboard(final String l_id) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				playService.leaderboard_show(l_id);
			}
		});
	}

	public void show_leaderboards() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				playService.leaderboard_show_list();
			}
		});
	}

	protected void onMainActivityResult (int requestCode, int resultCode, Intent data) {
		Utils.d(TAG, "onActivityResult: reqCode=" + requestCode + ", resCode=" + resultCode);

		playService.onActivityResult(requestCode, resultCode, data);
	}

	protected void onMainPause () {
		playService.onPause();
	}

	protected void onMainResume () {
		playService.onResume();
	}

	protected void onMainDestroy () {
		playService.onStop();
	}

	private Context context;
	private Activity activity;
	private PlayService playService = null;

    static final String TAG = "godot";
}

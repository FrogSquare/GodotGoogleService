
package org.godotengine.godot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONException;

import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.games.Games;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class GooglePlay extends Godot.SingletonBase
	implements ConnectionCallbacks, OnConnectionFailedListener {

	static public Godot.SingletonBase initialize (Activity p_activity) {
		return new GooglePlay(p_activity);
	}

	public GooglePlay(Activity p_activity) {
		registerClass ("GooglePlay", new String[] {
			"init", "login", "logout", "unlock_achievement",
			"increse_achievement", "show_achievements",
			"submit_leaderboard", "show_leaderboard", "show_leaderboards"
		});

		activity = p_activity;
	}

	public void init(final int instanceID) {
		// script_id = instanceID;

		activity.runOnUiThread(new Runnable() {
			public void run() {
				initGoogleService();
			}
		});
	}

	public void login() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				connect();
			}
		});
	}

	public void logout() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				disconnect();
			}
		});
	}

	public void unlock_achievement(final String id) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				achievement_unlock(id);
			}
		});
	}

	public void increse_achievement(final String id, final int steps) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				achievement_increment(id, steps);
			}
		});
	}

	public void show_achievements() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				achievement_show_list();
			}
		});
	}

	public void submit_leaderboard(final int score, final String l_id) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				leaderboard_submit(l_id, score);
			}
		});
	}

	public void show_leaderboard(final String l_id) {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				leaderboard_show(l_id);
			}
		});
	}

	public void show_leaderboards() {
		activity.runOnUiThread(new Runnable() {
			public void run() {
				leaderboard_show_list();
			}
		});
	}

	private void initGoogleService() {
		mGoogleApiClient = new GoogleApiClient.Builder(activity)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(Games.API).addScope(Games.SCOPE_GAMES)
		.addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

		Log.d(TAG, "Google initialized.");

		onStart();
	}

	public GoogleApiClient getApiClient() {
		if (mGoogleApiClient == null) { return null; }
		return mGoogleApiClient;
	}

	public boolean isConnected() {
		return isGooglePlayConnected;
	}

	public void connect() {
		if (mGoogleApiClient == null) {
			//
			return;
		}

		if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
			mGoogleApiClient.connect();
			isRequestingSignIn = true;
			//
		} else { /** **/ }
	}

	public void disconnect() {
		if(mGoogleApiClient.isConnected()) {
			Games.signOut(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			isGooglePlayConnected = false;

			//
		}

		// 
	}

	public void succeedSignIn() {
		Log.d(TAG, "Google signed in.");

		isResolvingConnectionFailure = false;
		isGooglePlayConnected = true;
		isRequestingSignIn = false;

		// callScriptFunc(PLUGIN_GOOGLE, "login", "true");
	}

	public void achievement_unlock(final String achievement_id) {
		connect();

		if (isGooglePlayConnected) {
			// KeyValueStorage.setValue(achievement_id, "true");
			Games.Achievements.unlock(mGoogleApiClient, achievement_id);
			Log.i(TAG, "PlayGameServices: achievement_unlock");
		} else { Log.w(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void achievement_increment(final String achievement_id, final int amount) {
		connect();

		if (isGooglePlayConnected) {
			Games.Achievements.increment(mGoogleApiClient, achievement_id, amount);
			Log.i(TAG, "PlayGameServices: achievement_incresed");
		} else { Log.i(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void achievement_show_list() {
		connect();

		if (isGooglePlayConnected) {
			activity.startActivityForResult(
			Games.Achievements.getAchievementsIntent(mGoogleApiClient),
			REQUEST_ACHIEVEMENTS);
		} else { Log.i(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void leaderboard_submit(String id, int score) {
		connect();

		if (isGooglePlayConnected) {
			Games.Leaderboards.submitScore(mGoogleApiClient, id, score);
			Log.i(TAG, "PlayGameServices: leaderboard_submit, " + score);
		} else { Log.i(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void leaderboard_show(final String l_id) {
		connect();

		if (isGooglePlayConnected) {
			activity.startActivityForResult(
			Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
			l_id), REQUEST_LEADERBOARD);
		} else { Log.i(TAG, "PlayGameServices: Google not login calling connect"); }
	}

	public void leaderboard_show_list() {
		connect();

		if (isGooglePlayConnected) {
			activity.startActivityForResult(
			Games.Leaderboards.getAllLeaderboardsIntent(mGoogleApiClient),
			REQUEST_LEADERBOARD);
		} else { Log.i(TAG, "PlayGameServices: Google calling connect"); }
	}

	@Override
	public void onConnected(Bundle m_bundle) {
		//

		if (m_bundle != null) {

		}

		if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
			final Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
			String personName = currentPerson.getDisplayName();

			// 
		}

		succeedSignIn();
	}

	@Override
	public void onConnectionSuspended(int m_cause) {
		Log.i(TAG, "ConnectionSuspended: "+String.valueOf(m_cause));
	}

	@Override
	public void onConnectionFailed(ConnectionResult m_result) {
		Log.d(TAG, "google Connection failed.");

		if (isResolvingConnectionFailure) { return; }
		if(!isIntentInProgress && m_result.hasResolution()) {
			try {
				isIntentInProgress = true;
				activity.startIntentSenderForResult(
				m_result.getResolution().getIntentSender(),
				RC_SIGN_IN, null, 0, 0, 0);
			} catch (SendIntentException ex) {
				isIntentInProgress = false;
				connect();
			}

			isResolvingConnectionFailure = true;
			Log.d(TAG, "google Connection Resolving.");
		}
	}

	protected void onMainActivityResult (int requestCode, int resultCode, Intent data) {
		if (requestCode == RC_SIGN_IN) {
			isIntentInProgress = false;
			if (!mGoogleApiClient.isConnecting()) { mGoogleApiClient.connect(); }
		}
	}

	protected void onStart() {
		if (mGoogleApiClient.isConnected()) {
			succeedSignIn();
			Log.i(TAG, "client was already connected");
		} else {
			Log.i(TAG, "connecting on start.");
			connect();
		}
	}

	protected void onMainPause () {

	}

	protected void onMainResume () {
//		mFirebaseAnalytics.setCurrentScreen(activity, "Main", currentScreen);
	}

	protected void onMainDestroy () {
		if (mGoogleApiClient.isConnected()) { mGoogleApiClient.disconnect(); }

		isGooglePlayConnected = false;
		activity = null;
	}

	private static Context context;
	private static Activity activity;

	private static int script_id;

	private static final int RC_SIGN_IN = 9001;

	private static final int REQUEST_ACHIEVEMENTS = 9002;
	private static final int REQUEST_LEADERBOARD = 1002;
	private static final String TAG = "GoogleService";

	private Boolean isRequestingSignIn = false;
	private Boolean isIntentInProgress = false;
	private Boolean isGooglePlayConnected = false;
	private Boolean isResolvingConnectionFailure = false;

	private static GoogleApiClient mGoogleApiClient;
}

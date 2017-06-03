
package org.godotengine.godot;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.util.Log;
import android.view.View;
import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONException;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class PlayService
	implements ConnectionCallbacks, OnConnectionFailedListener {

	public static PlayService getInstance (Activity p_activity) {
		if (mInstance == null) {
			mInstance = new PlayService(p_activity);
		}

		return mInstance;
	}

	public PlayService(Activity p_activity) {
		activity = p_activity;
	}

	public void init (final int instanceID) {
		script_id = instanceID;
		GUtils.setScriptInstance(script_id);

		if (GUtils.checkGooglePlayService(activity)) {
			Log.d(TAG, "Play Service Available.");
		}

		mGoogleApiClient = new GoogleApiClient.Builder(activity)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.addApi(Games.API).addScope(Games.SCOPE_GAMES)
		.addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
		.addApi(AppInvite.API)
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
			Log.d(TAG, "GoogleApiClient not initialized");
			return;
		}

		if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
			mGoogleApiClient.connect();
			isRequestingSignIn = true;
			Log.d(TAG, "Connecting to google play service");
		} else { /** **/ }
	}

	public void disconnect() {
		if(mGoogleApiClient.isConnected()) {
			Games.signOut(mGoogleApiClient);
			mGoogleApiClient.disconnect();
			isGooglePlayConnected = false;

			GUtils.callScriptFunc("login", "false");
			Log.d(TAG, "Google play service disconnected.");
		}
	}

	public void succeedSignIn() {
		Log.d(TAG, "Google signed in.");

		isResolvingConnectionFailure = false;
		isGooglePlayConnected = true;
		isRequestingSignIn = false;

		GUtils.callScriptFunc("login", "true");
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
		Log.d(TAG, "Connected to google play service");

		if (m_bundle != null) {

		}

		if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
			final Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
			String personName = currentPerson.getDisplayName();

			Log.d(TAG, "Signed in as: " + personName);
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
				GUtils.GOOGLE_SIGN_IN_REQUEST, null, 0, 0, 0);
			} catch (SendIntentException ex) {
				isIntentInProgress = false;
				connect();
			}

			isResolvingConnectionFailure = true;
			Log.d(TAG, "google Connection Resolving.");
		}
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GUtils.GOOGLE_SIGN_IN_REQUEST) {
			isIntentInProgress = false;
			if (!mGoogleApiClient.isConnecting()) { mGoogleApiClient.connect(); }
		}
	}

	public void onStart() {
		if (mGoogleApiClient.isConnected()) {
			succeedSignIn();
			Log.i(TAG, "client was already connected");
		} else {
			Log.i(TAG, "connecting on start.");
			connect();
		}

		boolean autoLaunchDeepLink = true;

		AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, activity, autoLaunchDeepLink)
		.setResultCallback(new ResultCallback<AppInviteInvitationResult>() {

			@Override
			public void onResult(AppInviteInvitationResult result) {
				Log.d(TAG, "getInvitation: onResult:" + result.getStatus());

				if (result.getStatus().isSuccess()) {
					// Extract information from the intent
					Intent intent = result.getInvitationIntent();
					String deepLink = AppInviteReferral.getDeepLink(intent);
					String iId = AppInviteReferral.getInvitationId(intent);

					// autoLaunchDeepLink = true we don't have to do anything
					// here, but we could set that to false and manually choose
					// an Activity to launch to handle the deep link here.
					// ...

					/**
					JSONObject data = new JSONObject();

					try {
						data.put("deepLink", deepLink);
						data.put("invitationID", iId);
					} catch (JSONException e) {
						Log.d(TAG, "JSONException: " + e.toString());
					}

					GUtils.callSctionFunc("data", data.toString());
					**/
				}
			}
		});
	}

	public void onPause() {

	}

	public void onResume() {

	}

	public void onStop() {
		if (mGoogleApiClient.isConnected()) { mGoogleApiClient.disconnect(); }

		isGooglePlayConnected = false;
		activity = null;
	}

	private static Activity activity = null;
	private static Context context = null;
	private static PlayService mInstance = null;

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

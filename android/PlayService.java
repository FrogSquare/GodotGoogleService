
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
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONException;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.games.Games;

public class PlayService {

	public static PlayService getInstance (Activity p_activity) {
		if (mInstance == null) {
			synchronized (PlayService.class) {
				mInstance = new PlayService(p_activity);
			}
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

		GoogleSignInOptions gso =
		new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
		.requestEmail()
		.requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
		.requestScopes(new Scope(Scopes.PROFILE))
		.build();

		mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);

		Log.d(TAG, "Google initialized.");
		onStart();
	}

	public boolean isConnected() {
		return isGooglePlayConnected;
	}

	public void connect() {
		if (mGoogleSignInClient == null) {
			Log.d(TAG, "GoogleSignInClient not initialized");
			return;
		}

		Intent signInIntent = mGoogleSignInClient.getSignInIntent();
		activity.startActivityForResult(signInIntent, GUtils.GOOGLE_SIGN_IN_REQUEST);
	}

	public void disconnect() {
		mGoogleSignInClient.signOut()
		.addOnCompleteListener(activity, new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				Log.d(TAG, "Google signed out.");
				GUtils.callScriptFunc("login", "false");
			}
		});
	}

	public void succeedSignIn() {
		Log.d(TAG, "Google signed in.");
		GUtils.callScriptFunc("login", "true");
	}

	public void achievement_unlock(final String achievement_id) {
		connect();

		mAccount = GoogleSignIn.getLastSignedInAccount(activity);

		if (mAccount != null) {
			// KeyValueStorage.setValue(achievement_id, "true");
			Games.getAchievementsClient(activity, mAccount).unlock(achievement_id);

			Log.i(TAG, "PlayGameServices: achievement_unlock");
		} else { Log.w(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void achievement_increment(final String achievement_id, final int amount) {
		connect();

		mAccount = GoogleSignIn.getLastSignedInAccount(activity);

		if (mAccount != null) {
			Games.getAchievementsClient(activity, mAccount)
			.increment(achievement_id, amount);

			Log.i(TAG, "PlayGameServices: achievement_incresed");
		} else { Log.i(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void achievement_show_list() {
		connect();

		mAccount = GoogleSignIn.getLastSignedInAccount(activity);

		if (mAccount != null) {
			Games.getAchievementsClient(activity, mAccount)
			.getAchievementsIntent()
			.addOnSuccessListener(new OnSuccessListener<Intent>() {
				@Override
				public void onSuccess(Intent intent) {
					activity.startActivityForResult(intent, REQUEST_ACHIEVEMENTS);
				}
			});

		} else { Log.i(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void leaderboard_submit(String id, int score) {
		connect();

		mAccount = GoogleSignIn.getLastSignedInAccount(activity);

		if (mAccount != null) {
			Games.getLeaderboardsClient(activity, mAccount).submitScore(id, score);

			Log.i(TAG, "PlayGameServices: leaderboard_submit, " + score);
		} else { Log.i(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void leaderboard_show(final String l_id) {
		connect();

		mAccount = GoogleSignIn.getLastSignedInAccount(activity);

		if (mAccount != null) {
			Games.getLeaderboardsClient(activity, mAccount)
			.getLeaderboardIntent(l_id)
			.addOnSuccessListener(new OnSuccessListener<Intent>() {
				@Override
				public void onSuccess (Intent intent) {
					activity.startActivityForResult(intent, REQUEST_LEADERBOARD);
				}
			});

		} else { Log.i(TAG, "PlayGameServices: Google not login calling connect"); }
	}

	public void leaderboard_show_list() {
		connect();

		mAccount = GoogleSignIn.getLastSignedInAccount(activity);

		if (mAccount != null) {
			Games.getLeaderboardsClient(activity, mAccount)
			.getAllLeaderboardsIntent()
			.addOnSuccessListener(new OnSuccessListener<Intent>() {
				@Override
				public void onSuccess (Intent intent) {
					activity.startActivityForResult(intent, REQUEST_LEADERBOARD);
				}
			});
		
		} else { Log.i(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GUtils.GOOGLE_SIGN_IN_REQUEST) {
			Task<GoogleSignInAccount> task =
			GoogleSignIn.getSignedInAccountFromIntent(data);

			handleSignInResult(task);
		}
	}

	private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
		try {
			mAccount = completedTask.getResult(ApiException.class);
			succeedSignIn();
		} catch (ApiException e) {
			Log.w(TAG, "signInResult:failed code="
			+ e.getStatusCode() + ", Message: " + e.getStatusMessage());
		}
	}

	public void onStart() {
		mAccount = GoogleSignIn.getLastSignedInAccount(activity);

		if (mAccount != null &&
		GoogleSignIn.hasPermissions(mAccount, new Scope(Scopes.DRIVE_APPFOLDER))) {
			Log.d(TAG, "Google already connected to an account");
			succeedSignIn();
		} else {
			Log.d(TAG, "Google not connected");
			connect();
		}


		boolean autoLaunchDeepLink = true;
		/**
		// Check for App Invite invitations and launch deep-link activity if possible.
		// Requires that an Activity is registered in AndroidManifest.xml to handle
		// deep-link URLs.

		FirebaseDynamicLinks.getInstance().getDynamicLink(getIntent())
		.addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                @Override
                public void onSuccess(PendingDynamicLinkData data) {
                    if (data == null) {
                        Log.d(TAG, "getInvitation: no data");
                        return;
                    }

                    // Get the deep link
                    Uri deepLink = data.getLink();

                    // Extract invite
                    FirebaseAppInvite invite = FirebaseAppInvite.getInvitation(data);
                    if (invite != null) {
                        String invitationId = invite.getInvitationId();
                    }

                    // Handle the deep link
                    // ...
                }
		}).addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "getDynamicLink:onFailure", e);
                }
		});
		**/
	}

	public void onPause() {

	}

	public void onResume() {

	}

	public void onStop() {
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

	private Boolean isGooglePlayConnected = false;

	private GoogleSignInClient mGoogleSignInClient;
	private GoogleSignInAccount mAccount;
}

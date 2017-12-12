
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.games.AchievementsClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.tasks.Task;

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
		new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
		.requestScopes(new Scope(Scopes.GAMES))
		.requestEmail()
		.build();

		mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);

		Log.d(TAG, "Google::Initialized");
		onStart();
	}

	public boolean isConnected() {
		mAccount = GoogleSignIn.getLastSignedInAccount(activity);
		return mAccount != null;
	}

	public void connect() {
		if (mGoogleSignInClient == null) {
			Log.d(TAG, "GoogleSignInClient not initialized");
			return;
		}

		if (isConnected()) {
			Log.d(TAG, "Google service is already connected");
			return;
		}

		Intent signInIntent = mGoogleSignInClient.getSignInIntent();
		activity.startActivityForResult(signInIntent, GOOGLE_SIGN_IN_REQUEST);
	}

	public void disconnect() {
		mGoogleSignInClient.signOut()
		.addOnCompleteListener(activity, new OnCompleteListener<Void>() {
			@Override
			public void onComplete(@NonNull Task<Void> task) {
				Log.d(TAG, "Google signed out.");

				mAchievementsClient = null;
				mLeaderboardsClient = null;
				mPlayersClient = null;

				GUtils.callScriptFunc("login", "false");
			}
		});
	}

	public void succeedSignIn() {
		Log.d(TAG, "Google signed in.");

		mAchievementsClient = Games.getAchievementsClient(activity, mAccount);
		mLeaderboardsClient = Games.getLeaderboardsClient(activity, mAccount);
		mPlayersClient = Games.getPlayersClient(activity, mAccount);

		GUtils.callScriptFunc("login", "true");

		mPlayersClient.getCurrentPlayer()
		.addOnCompleteListener(new OnCompleteListener<Player>() {
			@Override
			public void onComplete(@NonNull Task<Player> task) {
				String displayName = "UserName";

				if (task.isSuccessful()) {
					displayName = task.getResult().getDisplayName();
				} else {
					Exception e = task.getException();
				}

				GUtils.callScriptFunc("user", displayName);
                    }
		});
	}

	public void achievement_unlock(final String achievement_id) {
		connect();

		if (isConnected()) {
			// KeyValueStorage.setValue(achievement_id, "true");
			mAchievementsClient.unlock(achievement_id);

			Log.i(TAG, "PlayGameServices: achievement_unlock");
		} else { Log.w(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void achievement_increment(final String achievement_id, final int amount) {
		connect();

		if (isConnected()) {
			mAchievementsClient.increment(achievement_id, amount);

			Log.i(TAG, "PlayGameServices: achievement_incresed");
		} else { Log.i(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void achievement_show_list() {
		connect();

		if (isConnected()) {
			mAchievementsClient.getAchievementsIntent()
			.addOnSuccessListener(new OnSuccessListener<Intent>() {
				@Override
				public void onSuccess(Intent intent) {
					activity.startActivityForResult(intent, REQUEST_ACHIEVEMENTS);
				}
			})
			.addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					Log.d(TAG, "Showing::Loaderboard::Failed:: " + e.toString());
				}
			});

		} else { Log.i(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void leaderboard_submit(String id, int score) {
		connect();

		if (isConnected()) {
			mLeaderboardsClient.submitScore(id, score);

			Log.i(TAG, "PlayGameServices: leaderboard_submit, " + score);
		} else { Log.i(TAG, "PlayGameServices: Google calling connect"); }
	}

	public void leaderboard_show(final String l_id) {
		connect();

		if (isConnected()) {
			mLeaderboardsClient.getLeaderboardIntent(l_id)
			.addOnSuccessListener(new OnSuccessListener<Intent>() {
				@Override
				public void onSuccess (Intent intent) {
					Log.d(TAG, "Showing::Loaderboard::" + l_id);
					activity.startActivityForResult(intent, REQUEST_LEADERBOARD);
				}
			})
			.addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					Log.d(TAG, "Showing::Loaderboard::Failed:: " + e.toString());
				}
			});

		} else { Log.i(TAG, "PlayGameServices: Google not connected calling connect"); }
	}

	public void leaderboard_show_list() {
		connect();

		if (isConnected()) {
			mLeaderboardsClient.getAllLeaderboardsIntent()
			.addOnSuccessListener(new OnSuccessListener<Intent>() {
				@Override
				public void onSuccess (Intent intent) {
					Log.d(TAG, "Showing::Loaderboard::List");
					activity.startActivityForResult(intent, REQUEST_LEADERBOARD);
				}
			})
			.addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception e) {
					Log.d(TAG, "Showing::Loaderboard::Failed:: " + e.toString());
				}
			});

		} else { Log.i(TAG, "PlayGameServices: Google not connected calling connect"); }
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GOOGLE_SIGN_IN_REQUEST) {
			GoogleSignInResult result =
			Auth.GoogleSignInApi.getSignInResultFromIntent(data);

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
			Log.w(TAG, "SignInResult::Failed code="
			+ e.getStatusCode() + ", Message: " + e.getStatusMessage());
		}
	}

	private void signInSilently() {
		if (isConnected()) { return; }

		GoogleSignInClient signInClient = GoogleSignIn.getClient(activity,
		GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

		signInClient.silentSignIn().addOnCompleteListener(activity,
		new OnCompleteListener<GoogleSignInAccount>() {
			@Override
			public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
				if (task.isSuccessful()) {
					// The signed in account is stored in the task's result.
					try {
						mAccount = task.getResult(ApiException.class);
						succeedSignIn();
					} catch (ApiException e) {
						Log.w(TAG, "SignInResult::Failed code="
						+ e.getStatusCode() + ", Message: "
						+ e.getStatusMessage());
					}
				} else {
					// Player will need to sign-in explicitly using via UI
					Log.d(TAG, "Silent::Login::Failed");
				}
			}
		});
	}

	public void onStart() {
		mAccount = GoogleSignIn.getLastSignedInAccount(activity);

		if (mAccount != null) {
			Log.d(TAG, "Google already connected to an account");
			succeedSignIn();
		} else {
			Log.d(TAG, "Google not connected");
			connect();
			//signInSilently();
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
		signInSilently();
	}

	public void onStop() {
		activity = null;
	}

	private static Activity activity = null;
	private static Context context = null;
	private static PlayService mInstance = null;

	private static int script_id;

	private static final int GOOGLE_SIGN_IN_REQUEST	= 9001;
	private static final int REQUEST_ACHIEVEMENTS = 9002;
	private static final int REQUEST_LEADERBOARD = 9003;

	private GoogleSignInClient mGoogleSignInClient;

	private GoogleSignInAccount mAccount;
	private AchievementsClient mAchievementsClient;
	private LeaderboardsClient mLeaderboardsClient;
	private PlayersClient mPlayersClient;

	private static final String TAG = "GoogleService";
}

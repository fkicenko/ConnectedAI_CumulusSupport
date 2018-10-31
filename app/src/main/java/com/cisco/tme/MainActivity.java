package com.cisco.tme;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cisco.tme.REST.RestClient;
import com.cisco.tme.fragment.CallFragment;
import com.cisco.tme.fragment.ChatFragment;
import com.cisco.tme.fragment.HomeFragment;
import com.cisco.tme.fragment.NotificationsFragment;
import com.cisco.tme.fragment.SettingsFragment;
import com.cisco.tme.nl.language.AccessTokenLoader;
import com.cisco.tme.nl.language.ApiFragment;
import com.cisco.tme.nl.language.model.EntityInfo;
import com.cisco.tme.nl.language.model.SentimentInfo;
import com.cisco.tme.nl.language.model.TokenInfo;
import com.cisco.tme.other.CircleTransform;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.JsonElement;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import ai.api.android.AIConfiguration;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;
import ai.api.ui.AIDialog;
import ai.api.util.BluetoothController;
import cz.msebera.android.httpclient.Header;

/**
 * @author Frank Kicenko
 *
 */
public class MainActivity extends AppCompatActivity implements AIDialog.AIDialogListener, ChatFragment.OnFragmentInteractionListener, ApiFragment.Callback {

	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String ALL = "- Sender -";
	private static final String TEMP_FILE = "smsxp.txt";
	
	private final SimpleDateFormat sdf = new SimpleDateFormat();
	private final Date dt = new Date();

	private int criteriaAddr;
	private String criteriaStartDt;
	private String criteriaEndDt;
	private String criteriaMsg;
	private int activitiesCount;
	private boolean inprogress;
	private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
	// API.AI (DialogFlow)
	private BluetoothControllerImpl bluetoothController;
	private SettingsManager settingsManager;
	// Google Sentiment APIs
	private static final int API_ENTITIES = 0;
	private static final int API_SENTIMENT = 1;
	private static final int API_SYNTAX = 2;
	private static final String FRAGMENT_API = "api";
	private static final int LOADER_ACCESS_TOKEN = 1;
	// urls to load navigation header background image
	// and profile image (Michael Littlefoot)
	private static final String urlNavHeaderBg = "https://api.androidhive.info/images/nav-menu-header-bg.jpg";
	private static final String urlProfileImg = "http://capricorn.ucplanning.com/michael_littlefoot.png";
	// index to identify current nav menu item
	public static int navItemIndex = 0;
	// tags used to attach the fragments
	private static final String TAG_HOME = "home";
	private static final String TAG_CHAT = "chat";
	private static final String TAG_CALL = "call";
	private static final String TAG_NOTIFICATIONS = "notifications";
	private static final String TAG_SETTINGS = "settings";
	public static String CURRENT_TAG = TAG_HOME;

	private NavigationView navigationView;
	private DrawerLayout drawer;
	private View navHeader;
	private ImageView imgNavHeaderBg, imgProfile;
	private TextView txtName, txtWebsite;
	private Toolbar toolbar;
	private FloatingActionButton fab;
	// DialogFlow
	private AIDialog aiDialog;
	private static final long PAUSE_CALLBACK_DELAY = 500;
	private static final int REQUEST_AUDIO_PERMISSIONS_ID = 33;
	// Google Pay
	private PaymentsClient mPaymentsClient;

	// toolbar titles respected to selected nav menu item
	private String[] activityTitles;

	// flag to load home fragment when user presses back key
	private boolean shouldLoadHomeFragOnBackPress = true;
	private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Google Pay
		mPaymentsClient =
			Wallet.getPaymentsClient(
				this,
				new Wallet.WalletOptions.Builder()
						.setEnvironment(WalletConstants.ENVIRONMENT_TEST)
						.build());
		// Start Main Application
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);

        // Set up retained fragment (Google Analysis - ApiFragment)
		final FragmentManager fm = getSupportFragmentManager();

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

       	mHandler = new Handler();

		drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		navigationView = (NavigationView) findViewById(R.id.nav_view);
		fab = (FloatingActionButton) findViewById(R.id.fab);

		// Navigation view header
		navHeader = navigationView.getHeaderView(0);
		txtName = (TextView) navHeader.findViewById(R.id.name);
		txtWebsite = (TextView) navHeader.findViewById(R.id.website);
		imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
		imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

		// load toolbar titles from string resources
		activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

		// Initialize BlueTooth
		bluetoothController = new BluetoothControllerImpl(this);
		settingsManager = new SettingsManager(this);

		// Get permissions for using SMS
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
			getPermissionToReadSMS();
		}
		// Check permissions for Audio
		checkAudioRecordPermission();
		// Create our Fab
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Launch a Voice / Video Call", Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		});
		// Check if we have Natural Language set
		if(settingsManager.isUseVoice()) {
			// Initialize our TTS
			TTS.init(getApplicationContext());

			// Initialize DialogFlow for Speech to Text
			final AIConfiguration config = new AIConfiguration(Config.ACCESS_TOKEN,
					AIConfiguration.SupportedLanguages.English,
					AIConfiguration.RecognitionEngine.System);

			/*aiDialog = new AIDialog(this, config, R.layout.aidialog);
			aiDialog.setResultsListener(this);*/
		}

		// load nav menu header data
		loadNavHeader();

		// initializing navigation menu
		setUpNavigationView();

       	// Start Login
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);

		// Start Home Screen fragment
		loadHomeFragment();
		// Get the latest Token from Firebase. Need this for notifications.
		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		System.out.println("*** TOKEN *** :" + refreshedToken);
		// Initiate REST call to Context Service to obtain any Alerts from Context Service
		/*RestClient rc = new RestClient();
		rc.get(Config.REST_URL, null, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// If the response is JSONObject instead of expected JSONArray
				System.out.println("Received JSONObject: " + response);
				CreateAlert(response);
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {
				// Do something with the response
				System.out.println("Received JSONArray: " + timeline);
				CreateAlert(null);
			}
		});*/
		// Prepare the Google Sentiment API
		if (getApiFragment() == null) {
			fm.beginTransaction().add(new ApiFragment(), FRAGMENT_API).commit();
		}
		prepareApi();
    }
    public void CreateAlert(JSONObject obj)
	{
		View v = getWindow().getDecorView().findViewById(android.R.id.content);
		Resources.Theme greenTheme = new ContextThemeWrapper(v.getContext(), R.style.AppTheme_Green).getTheme();
		TypedArray a = greenTheme.obtainStyledAttributes(new int[] {R.attr.colorAccent});
		int accentColor = a.getColor(0, 0);
		a.recycle();
		final JSONObject objAlert = obj;

		Snackbar snackbar = Snackbar.make(v, R.string.cumulus_alert_header, Snackbar.LENGTH_LONG)
				.setAction("View Alert", new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						new AlertDialog.Builder(MainActivity.this)
								.setTitle("Cumulus Alert")
								.setMessage(objAlert.toString())
								.setCancelable(false)
								.setPositiveButton("Get Help Now", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Bundle bundle = new Bundle();
										String myMessage = "Stackoverflow is cool!";
										bundle.putString("message", myMessage );
										Fragment fragInfo = new ChatFragment();
										fragInfo.setArguments(bundle);

										FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
										fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,	android.R.anim.fade_out);
										String CHAT_TAG = ChatFragment.class.getName();
										fragmentTransaction.replace(R.id.fragment_container, fragInfo, CHAT_TAG);
										fragmentTransaction.commitAllowingStateLoss();
									}
								}).show();
					}
				});
				snackbar.setActionTextColor(accentColor);
				snackbar.show();
	}
	protected void checkAudioRecordPermission() {
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.RECORD_AUDIO)
				!= PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.RECORD_AUDIO)) {

				// Show an explanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.

			} else {
				// No explanation needed, we can request the permission.

				ActivityCompat.requestPermissions(this,
						new String[]{Manifest.permission.RECORD_AUDIO},
						REQUEST_AUDIO_PERMISSIONS_ID);

			}
		}
	}
	/***
	 * Load navigation menu header information
	 * like background image, profile image
	 * name, website, notifications action view (dot)
	 */
	private void loadNavHeader() {
		// name, website
		txtName.setText("Michael Littlefoot");
		txtWebsite.setText("michael.littlefoot@cc.com");

		// loading header background image
		Glide.with(this).load(urlNavHeaderBg)
				.crossFade()
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.into(imgNavHeaderBg);

		// Loading profile image
		Glide.with(this).load(urlProfileImg)
				.crossFade()
				.thumbnail(0.5f)
				.bitmapTransform(new CircleTransform(this))
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.into(imgProfile);

		// showing dot next to notifications label
		navigationView.getMenu().getItem(3).setActionView(R.layout.menu_dot);
	}
	private void selectNavMenu() {
		navigationView.getMenu().getItem(navItemIndex).setChecked(true);
	}

	private void setUpNavigationView() {
		//Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
		navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

			// This method will trigger on item Click of navigation menu
			@Override
			public boolean onNavigationItemSelected(MenuItem menuItem) {

				//Check to see which item was being clicked and perform appropriate action
				switch (menuItem.getItemId()) {
					//Replacing the main content with ContentFragment Which is our Inbox View;
					case R.id.nav_home:
						navItemIndex = 0;
						CURRENT_TAG = TAG_HOME;
						break;
					case R.id.nav_chat:
						navItemIndex = 1;
						CURRENT_TAG = TAG_CHAT;
						break;
					case R.id.nav_call:
						navItemIndex = 2;
						CURRENT_TAG = TAG_CALL;
						break;
					case R.id.nav_notifications:
						navItemIndex = 3;
						CURRENT_TAG = TAG_NOTIFICATIONS;
						break;
					case R.id.nav_settings:
						navItemIndex = 4;
						CURRENT_TAG = TAG_SETTINGS;
						break;
					case R.id.nav_about_us:
						// launch new intent instead of loading fragment
						startActivity(new Intent(MainActivity.this, AboutUsActivity.class));
						drawer.closeDrawers();
						return true;
					case R.id.nav_privacy_policy:
						// launch new intent instead of loading fragment
						startActivity(new Intent(MainActivity.this, PrivacyPolicyActivity.class));
						drawer.closeDrawers();
						return true;
					default:
						navItemIndex = 0;
				}

				//Checking if the item is in checked state or not, if not make it in checked state
				if (menuItem.isChecked()) {
					menuItem.setChecked(false);
				} else {
					menuItem.setChecked(true);
				}
				menuItem.setChecked(true);
				loadHomeFragment();

				return true;
			}
		});

		ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

			@Override
			public void onDrawerClosed(View drawerView) {
				// Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
				super.onDrawerClosed(drawerView);
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				// Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
				super.onDrawerOpened(drawerView);
			}
		};

		//Setting the actionbarToggle to drawer layout
		drawer.setDrawerListener(actionBarDrawerToggle);

		//calling sync state is necessary or else your hamburger icon wont show up
		actionBarDrawerToggle.syncState();
	}
	/***
	 * Returns respected fragment that user
	 * selected from navigation menu
	 */
	private void loadHomeFragment() {
		// selecting appropriate nav menu item
		selectNavMenu();

		// set toolbar title
		setToolbarTitle();

		// if user select the current navigation menu again, don't do anything
		// just close the navigation drawer
		if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
			drawer.closeDrawers();
			// show or hide the fab button
			toggleFab();
			return;
		}

		// Sometimes, when fragment has huge data, screen seems hanging
		// when switching between navigation menus
		// So using runnable, the fragment is loaded with cross fade effect
		// This effect can be seen in GMail app
		Runnable mPendingRunnable = new Runnable() {
			@Override
			public void run() {
				// update the main content by replacing fragments
				Fragment fragment = getHomeFragment();
				FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
				fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,	android.R.anim.fade_out);
				fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
				fragmentTransaction.commitAllowingStateLoss();
			}
		};

		// If mPendingRunnable is not null, then add to the message queue
		if (mPendingRunnable != null) {
			mHandler.post(mPendingRunnable);
		}

		// show or hide the fab button
		toggleFab();

		//Closing drawer on item click
		drawer.closeDrawers();

		// refresh toolbar menu
		invalidateOptionsMenu();
	}

	private void setToolbarTitle() {
		getSupportActionBar().setTitle(activityTitles[navItemIndex]);
	}

	// show or hide the fab
	private void toggleFab() {
		if (navItemIndex == 0)
			fab.show();
		else
			fab.hide();
	}

	private Fragment getHomeFragment() {
		switch (navItemIndex) {
			case 0:
				// home
				HomeFragment homeFragment = new HomeFragment();
				return homeFragment;
			case 1:
				// Chat
				ChatFragment chatFragment = new ChatFragment();
				return chatFragment;
			case 2:
				// Call fragment
				CallFragment callFragment = new CallFragment();
				return callFragment;
			case 3:
				// notifications fragment
				NotificationsFragment notificationsFragment = new NotificationsFragment();
				return notificationsFragment;
			case 4:
				// settings fragment
				SettingsFragment settingsFragment = new SettingsFragment();
				return settingsFragment;
			default:
				return new HomeFragment();
		}
	}
	// Setting up the Google Sentiment APIs
	private ApiFragment getApiFragment() {
		return (ApiFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_API);
	}

	private void prepareApi() {
		// Initiate token refresh
		getSupportLoaderManager().initLoader(LOADER_ACCESS_TOKEN, null,
				new LoaderManager.LoaderCallbacks<String>() {
					@Override
					public Loader<String> onCreateLoader(int id, Bundle args) {
						return new AccessTokenLoader(MainActivity.this);
					}

					@Override
					public void onLoadFinished(Loader<String> loader, String token) {
						getApiFragment().setAccessToken(token);
					}

					@Override
					public void onLoaderReset(Loader<String> loader) {
					}
				});
	}
	// End of setting Google APIs
	public BluetoothController getBluetoothController() {
		return bluetoothController;
	}

	public SettingsManager getSettingsManager() {
		return settingsManager;
	}

	public void getPermissionToReadSMS() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
				!= PackageManager.PERMISSION_GRANTED) {
			if (shouldShowRequestPermissionRationale(
					Manifest.permission.READ_SMS)) {
				Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
			}
			requestPermissions(new String[]{Manifest.permission.READ_SMS},
					READ_SMS_PERMISSIONS_REQUEST);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("criteriaAddr", criteriaAddr);
		outState.putString("criteriaStartDt", criteriaStartDt);
		outState.putString("criteriaEndDt", criteriaEndDt);
		outState.putString("criteriaMsg", criteriaMsg);
		outState.putBoolean("inprogress", inprogress);
	}	

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		criteriaAddr = state.getInt("criteriaAddr");
		criteriaStartDt = state.getString("criteriaStartDt");
		criteriaEndDt = state.getString("criteriaEndDt");
		criteriaMsg = state.getString("criteriaMsg");
		inprogress = state.getBoolean("inprogress");
	}    

	@Override
	protected void onResume() {
		if (aiDialog != null) {
			aiDialog.resume();
		}
		super.onResume();

	}	
	

	@Override
	protected void onDestroy() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File f = new File(Environment.getExternalStorageDirectory(), TEMP_FILE);
			f.delete();
			//unregisterReceiver(myReceiver);
			System.out.println("*** Main Activity->OnDestroy ***");
		}
		
		super.onDestroy();
	}

	@Override
	public void onFragmentInteraction(Uri uri) {

	}
	public class BluetoothControllerImpl extends BluetoothController {

		public BluetoothControllerImpl(Context context) {
			super(context);
		}

		@Override
		public void onHeadsetDisconnected() {
			Log.d(TAG, "Bluetooth headset disconnected");
		}

		@Override
		public void onHeadsetConnected() {
			Log.d(TAG, "Bluetooth headset connected");

			if (isInForeground() && settingsManager.isUseBluetooth()
					&& !bluetoothController.isOnHeadsetSco()) {
				bluetoothController.start();
			}
		}

		@Override
		public void onScoAudioDisconnected() {
			Log.d(TAG, "Bluetooth sco audio finished");
			bluetoothController.stop();

			if (isInForeground() && settingsManager.isUseBluetooth()) {
				bluetoothController.start();
			}
		}

		@Override
		public void onScoAudioConnected() {
			Log.d(TAG, "Bluetooth sco audio started");
		}

	}
	private boolean isInForeground() {
		return activitiesCount > 0;
	}

	// Next sections are overrides for DialogFlow

	@Override
	public void onResult(final AIResponse response) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "onResult");

				//resultTextView.setText(gson.toJson(response));

				Log.i(TAG, "Received success response");

				// this is example how to get different parts of result object
				final Status status = response.getStatus();
				Log.i(TAG, "Status code: " + status.getCode());
				Log.i(TAG, "Status type: " + status.getErrorType());

				final Result result = response.getResult();
				Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

				Log.i(TAG, "Action: " + result.getAction());
				final String speech = result.getFulfillment().getSpeech();
				Log.i(TAG, "Speech: " + speech);
				TTS.speak(speech);

				final Metadata metadata = result.getMetadata();
				if (metadata != null) {
					Log.i(TAG, "Intent id: " + metadata.getIntentId());
					Log.i(TAG, "Intent name: " + metadata.getIntentName());
				}

				final HashMap<String, JsonElement> params = result.getParameters();
				if (params != null && !params.isEmpty()) {
					Log.i(TAG, "Parameters: ");
					for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
						Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
					}
				}
			}

		});
	}

	@Override
	public void onError(final AIError error) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//resultTextView.setText(error.toString());
			}
		});
	}

	@Override
	public void onCancelled() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//resultTextView.setText("");
			}
		});
	}
	public void buttonListenOnClick(final View view) {
		aiDialog.showAndListen();
	}

	// Google Sentiment overrides from ApiFragment
	@Override
	public void onEntitiesReady(EntityInfo[] entities) {
		/*if (mViewPager.getCurrentItem() == API_ENTITIES) {
			showResults();
		}
		mAdapter.setEntities(entities);*/
	}

	@Override
	public void onSentimentReady(SentimentInfo sentiment) {
		/*if (mViewPager.getCurrentItem() == API_SENTIMENT) {
			showResults();
		}
		mAdapter.setSentiment(sentiment);*/
		int i = 1;
	}

	@Override
	public void onSyntaxReady(TokenInfo[] tokens) {
		/*if (mViewPager.getCurrentItem() == API_SYNTAX) {
			showResults();
		}
		mAdapter.setTokens(tokens);*/
	}
	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	public interface OnFragmentInteractionListener {
		// TODO: Update argument type and name
		void onFragmentInteraction(Uri uri);
	}

}
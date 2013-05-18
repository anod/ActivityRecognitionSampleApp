package com.anod.activitytest;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

public class MainActivity extends Activity implements GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
	private static final String KEY_STATUS = "key_status";
	private ActivityRecognitionClient mActivityRecognitionClient;
	private TextView mStatusTextView;
	private StringBuilder mText;
	
		
	private ServiceMessageReceiver mMessageReciever = new ServiceMessageReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(MESSAGE_INTENT)) {
				ActivityRecognitionResult result = (ActivityRecognitionResult)intent.getExtras().get(ActivityRecognitionResult.EXTRA_ACTIVITY_RESULT);
				DetectedActivity probActivity = result.getMostProbableActivity();
				append("Detected activity: ["+String.format("%03d", probActivity.getConfidence())+"] "+renderActivityType(probActivity.getType()));
			}
		}
	};

	private String renderActivityType(int type) {
		if (type == DetectedActivity.IN_VEHICLE) {
			return "IN_VEHICLE";
		}
		if (type == DetectedActivity.ON_BICYCLE) {
			return "ON_BICYCLE";
		}
		if (type == DetectedActivity.ON_FOOT) {
			return "ON_FOOT";
		}
		if (type == DetectedActivity.STILL) {
			return "STILL (NOT MOOVING)";
		}
		if (type == DetectedActivity.TILTING) {
			return "TILTING";
		}
		return "UNKNOWN";
	}
	
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		mStatusTextView = (TextView)findViewById(R.id.statusText);

		if (savedInstanceState != null) {
			String savedText = savedInstanceState.getString(KEY_STATUS);
			mText = new StringBuilder(savedText);
		} else {
			mText = new StringBuilder("");
		}
		mStatusTextView.setText(mText.toString());
	}

	@Override
	protected void onSaveInstanceState (Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(KEY_STATUS, mText.toString());
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mMessageReciever);
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (status  !=  ConnectionResult.SUCCESS) {
			updateStatusError(status);
			return;
		}
		IntentFilter filter = new IntentFilter(ServiceMessageReceiver.MESSAGE_INTENT);
		registerReceiver(mMessageReciever, filter);
		// Connect to the ActivityRecognitionService
		mActivityRecognitionClient = new ActivityRecognitionClient(this, this, this);
		mActivityRecognitionClient.connect();
	}

	private void updateStatusError(int status) {
		String text = "";
		if (status == ConnectionResult.SERVICE_MISSING) {
			text = "Error: ConnectionResult.SERVICE_MISSING";
		} else if (status == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED){
			text = "Error: ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED";
		} else if (status == ConnectionResult.SERVICE_DISABLED){
			text = "Error: ConnectionResult.SERVICE_DISABLED";
		} else if (status == ConnectionResult.SERVICE_INVALID) {
			text = "Error: ConnectionResult.SERVICE_INVALID";
		} else {
			text = "Error: code " + status;
		}
		mStatusTextView.setText(text);
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		append("Connection Failed, result: "+result.toString());
	}

	private void append(String text) {
		mText.append(text);
		mText.append("\n");
		mStatusTextView.setText(mText);
	}
	
	@Override
	// Called when a connection to the ActivityRecognitionService has been established.
	public void onConnected(Bundle connectionHint) {
		append("Connected.");
		Intent intent = new Intent(this, MyIntentService.class);
		PendingIntent callbackIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mActivityRecognitionClient.requestActivityUpdates(30000, callbackIntent);

	}

	@Override
	public void onDisconnected() {
		append("Disconnected.");
	}

}

package cookie;

import java.util.List;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.cookie.R;

import cookie.ecourse.network.NetWorkUtil;

import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements
		LocationListener, TextToSpeech.OnInitListener {

	private String TAG = MainActivity.class.getSimpleName();
	/** Called when the activity is first created. */
	private boolean getService = false; // �O�_�w�}�ҩw��A��
	private int mInterval = 10000; // GPS�w�춡�j�ɶ�
	private Handler mHandler;
	private TextToSpeech mTts;
	private LocationManager mLocationManager;
	private String bestProvider;
	private String prevMsg = "";

	TextView txtLongitude;
	TextView txtLatitude;
	TextView txtAddress;
	Button btn;
	Boolean isPlay = false;

	Runnable mStatusChecker = new Runnable() {

		@Override
		public void run() {
			// update gps information
			Location location = getLastBestLocation();
			updateLocation(location);
			// Toast.makeText(MainActivity.this, "gps update!",
			// Toast.LENGTH_SHORT)
			// .show();
			Log.d(TAG, "gps update!");

			// send location to server
			RequestData reqData = new RequestData();
			reqData.location = location;
			// TODO velocity remain for future work
			reqData.velocity = 50;
			new GetInfoTask(reqData).execute();
			mHandler.postDelayed(mStatusChecker, mInterval);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Initialize text-to-speech. This is an asynchronous operation.
		// The OnInitListener (second argument) is called after initialization
		// completes.
		mTts = new TextToSpeech(this, this // TextToSpeech.OnInitListener
		);

		txtLongitude = (TextView) findViewById(R.id.longitude);
		txtLatitude = (TextView) findViewById(R.id.latitude);
		txtAddress = (TextView) findViewById(R.id.address);
		btn = (Button) findViewById(R.id.btn);

		mHandler = new Handler();
		checkLocationProvider(); // �ˬd�w��A��

		btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// RequestData reqData = new RequestData();
				// reqData.location = getLastBestLocation();
				// reqData.velocity = 50;
				// new GetInfoTask(reqData).execute();
				if (isPlay) {
					// stop gps track
					isPlay = false;
					btn.setBackgroundResource(R.drawable.play);
					stopGPSTrack();
				} else {
					// start gps track
					isPlay = true;
					btn.setBackgroundResource(R.drawable.stop);
					startGPSTrack();
				}
			}
		});
	}

	private void checkLocationProvider() {
		// ���o�t�Ωw��A��
		LocationManager status = (LocationManager) (this
				.getSystemService(Context.LOCATION_SERVICE));
		if (status.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| status.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			// �p�GGPS�κ����w��}�ҡA��s��m
			getService = true; // �T�{�}�ҩw��A��
			locationServiceInitial();
		} else {
			Toast.makeText(this, "�ж}�ҩw��A��", Toast.LENGTH_LONG).show();
			startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)); // �}�ҳ]�w����
		}
	}

	private void locationServiceInitial() {
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE); // ���o�t�Ωw��A��
		Criteria criteria = new Criteria(); // ��T���Ѫ̿���з�
		bestProvider = mLocationManager.getBestProvider(criteria, true); // ��ܺ�ǫ׳̰������Ѫ�

		Log.d(TAG, "locationServiceInitial....updateLocation()");
		Location location = getLastBestLocation();
		updateLocation(location);
	}

	/**
	 * @return the last know best location
	 */
	private Location getLastBestLocation() {
		Location locationGPS = mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location locationNet = mLocationManager
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		long GPSLocationTime = 0;
		if (null != locationGPS) {
			GPSLocationTime = locationGPS.getTime();
		}

		long NetLocationTime = 0;

		if (null != locationNet) {
			NetLocationTime = locationNet.getTime();
		}

		if (0 < GPSLocationTime - NetLocationTime) {
			return locationGPS;
		} else {
			return locationNet;
		}
	}

	private void updateLocation(Location location) { // �N�w���T��ܦb�e����
		if (location != null) {

			Double longitude = location.getLongitude(); // ���o�g��
			Double latitude = location.getLatitude(); // ���o�n��

			txtLongitude.setText("�g��: " + String.valueOf(longitude));
			txtLatitude.setText("�n��: " + String.valueOf(latitude));
			txtAddress.setText("��m: " + getAddressByLocation(location));
		} else {
			Toast.makeText(this, "�L�k�w��y��", Toast.LENGTH_LONG).show();
		}
	}

	private class RequestData {
		Location location;
		int velocity;
	}

	// ���o���p
	private class GetInfoTask extends AsyncTask<String, Void, String> {

		JSONObject params = new JSONObject();
		String url = "http://192.168.101.39:8080/cookie/servlet";

		// String url = "http://192.168.100.91:8080/cookie/servlet";

		public GetInfoTask(RequestData reqData) {
			try {
				params.put("longitude", reqData.location.getLongitude());
				params.put("latitude", reqData.location.getLatitude());
				params.put("velocity", reqData.velocity);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected String doInBackground(String... urls) {
			return NetWorkUtil.POST(url, this.params.toString());
		}

		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "onPostExecute:" + result);

			if (!result.equals(prevMsg)) {
				Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT)
						.show();
				Log.d(TAG, "say result:" + result);
				if(result != "")
					sayInfo(result);
				prevMsg = result;
			} else {
				Log.d(TAG, "Message is the same.");
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getService) {
			mLocationManager
					.requestLocationUpdates(bestProvider, 1000, 1, this);
			// �A�ȴ��Ѫ̡B��s�W�v60000�@��=1�����B�̵u�Z���B�a�I���ܮɩI�s����
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (getService) {
			mLocationManager.removeUpdates(this); // ���}�����ɰ����s
			stopGPSTrack();
		}
	}

	@Override
	protected void onRestart() { // �q�䥦�������^��
		super.onRestart();
		checkLocationProvider();
	}

	@Override
	public void onDestroy() {
		// Don't forget to shutdown!
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}

		super.onDestroy();
	}

	@Override
	public void onLocationChanged(Location location) { // ��a�I���ܮ�
		updateLocation(location);
	}

	@Override
	public void onProviderDisabled(String arg0) { // ��GPS�κ����w��\��������
	}

	@Override
	public void onProviderEnabled(String arg0) { // ��GPS�κ����w��\��}��
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) { // �w�쪬�A����
	}

	public String getAddressByLocation(Location location) {
		String returnAddress = "";
		try {
			if (location != null) {
				Double longitude = location.getLongitude(); // ���o�g��
				Double latitude = location.getLatitude(); // ���o�n��

				// �إ�Geocoder����: Android 8 �H�W�Һþ������|����
				Geocoder gc = new Geocoder(this, Locale.TRADITIONAL_CHINESE); // �a��:�x�W
				// �۸g�n�ר��o�a�}
				List<Address> lstAddress = gc.getFromLocation(latitude,
						longitude, 1);

				// if (!Geocoder.isPresent()){ //Since: API Level 9
				// returnAddress = "Sorry! Geocoder service not Present.";
				// }
				returnAddress = lstAddress.get(0).getAddressLine(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnAddress;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// init text to speech
	@Override
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		if (status == TextToSpeech.SUCCESS) {
			// Set preferred language to US english.
			// Note that a language may not be available, and the result will
			// indicate this.
			int result = mTts.setLanguage(Locale.CHINA);
			// Try this someday for some interesting results.
			// int result mTts.setLanguage(Locale.FRANCE);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Lanuage data is missing or the language is not supported.
				Log.e(TAG, "Language is not available." + result);
			} else {
				// Check the documentation for other possible result codes.
				// For example, the language may be available for the locale,
				// but not for the specified country and variant.

				// The TTS engine has been successfully initialized.
				// Allow the user to press the button for the app to speak
				// again.
			}
		} else {
			// Initialization failed.
			Log.e(TAG, "Could not initialize TextToSpeech.");
		}

	}

	private void startGPSTrack() {
		Log.d(TAG, "startGPSTrack()");
		Toast.makeText(MainActivity.this, "Start track GPS!",
				Toast.LENGTH_SHORT).show();
		mStatusChecker.run();
	}

	private void stopGPSTrack() {
		Log.d(TAG, "stopGPSTrack()");
		Toast.makeText(MainActivity.this, "Stop track GPS!", Toast.LENGTH_SHORT)
				.show();
		mHandler.removeCallbacks(mStatusChecker);
	}

	private void sayInfo(String info) {
		mTts.speak(info, TextToSpeech.QUEUE_FLUSH, // Drop all pending entries
													// in the playback queue.
				null);
	}
}

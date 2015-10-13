package cookie.ecourse.network;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

public class AbstractPostTask extends AsyncTask<Void, Void, String> {
	
	protected String baseUrl = "http://140.116.72.162:8080/CourseHelper/";
	protected ProgressDialog dialog;
	
	protected String url;
	
	
	public AbstractPostTask(Context context) {
		dialog = new ProgressDialog(context);
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.dialog.setMessage("Loading...");
		this.dialog.show();
	}

	@Override
	protected String doInBackground(Void... arg0) {
		return null;
	}
	
	@Override
	protected void onPostExecute(String result) {
		super.onPostExecute(result);
		this.dialog.dismiss();
	}
}

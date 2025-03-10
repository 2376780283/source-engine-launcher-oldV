package zzh.source.hl2;

import android.content.*;
import java.io.*;
import java.net.*;
import zzh.source.hl2.R;
import android.os.AsyncTask;
import java.net.URL;
import java.net.URLConnection;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import android.util.Log;
import zzh.source.hl2.UpdateService;

public class UpdateSystem extends AsyncTask<String, Integer, String> {
	private static final String git_url = "https://raw.githubusercontent.com/2376780283/update_source-engine";
	private static final String app = "srceng-zzh-fix.apk";

	String deploy_branch, last_commit;
	Context mContext;

	public UpdateSystem( Context context )
	{
		mContext = context; // save application context
		deploy_branch = context.getResources().getString(R.string.srceng_app_name);
		last_commit = context.getResources().getString(R.string.srceng_update);
	}

	private static String toString(InputStream inputStream)
	{
        try {
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String inputLine;
			StringBuilder stringBuilder = new StringBuilder();
			while ((inputLine = bufferedReader.readLine()) != null) {
				stringBuilder.append(inputLine);
			}
			return stringBuilder.toString();
        }
		catch(Exception e) {
			e.printStackTrace();
		}

		return "";
	}

	@Override
	protected String doInBackground(String... params) {
		URL urlObject;
		URLConnection urlConnection;

		try {
			urlObject = new URL(git_url+"/"+deploy_branch+"/version");
			urlConnection = urlObject.openConnection();
			return toString(urlConnection.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	protected void onPostExecute(String result) {
		if( result != null && !result.equals("") && !last_commit.equals(result) ) {
			Intent notif = new Intent(mContext, UpdateService.class);
			notif.putExtra("update_url", git_url+"/"+deploy_branch+"/"+app);
			mContext.startService(notif);
		}
	}
}

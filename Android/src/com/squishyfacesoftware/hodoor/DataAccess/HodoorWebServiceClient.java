package com.squishyfacesoftware.hodoor.DataAccess;

import android.content.Context;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.SyncHttpClient;
import org.apache.http.cookie.Cookie;

import java.util.Date;
import java.util.List;

public class HodoorWebServiceClient
{
	public final AsyncHttpClient client;
	public final SyncHttpClient syncClient;
	public final PersistentCookieStore mCookieStore;
	private final Context mContext;
	static private HodoorWebServiceClient ywscInstance;
	public static final String URL_PREFS_KEY = "baseUrl";
	public static final String AUTH_COOKIE_NAME = "authCookie";

	public static HodoorWebServiceClient getInstance(Context context){
		if(ywscInstance == null) ywscInstance = new HodoorWebServiceClient(context);
		return ywscInstance;
	}

	private HodoorWebServiceClient(Context context)
	{
		client = new AsyncHttpClient();
		client.setTimeout(6000);

		syncClient = new SyncHttpClient();
		syncClient.setTimeout(6000);

		mCookieStore = new PersistentCookieStore(context);
		client.setCookieStore(mCookieStore);
		syncClient.setCookieStore(mCookieStore);

		mContext = context;
	}

	public void postNoParams(String url, AsyncHttpResponseHandler responseHandler) {
		String fullUrl = getFullUrl(url);
		client.post(fullUrl,  responseHandler);
	}

	public void postNoParamsSync(String url, AsyncHttpResponseHandler responseHandler) {
		String fullUrl = getFullUrl(url);
		syncClient.post(fullUrl,  responseHandler);
	}

	public void get(String url, AsyncHttpResponseHandler responseHandler){
		String fullUrl = getFullUrl(url);
		client.get(fullUrl,  responseHandler);
	}

	public void authenticate(){
		List<Cookie> cookies  = mCookieStore.getCookies();
		if(cookies.size() > 0){
			Cookie authCookie = cookies.get(0);
			String[] values = authCookie.getValue().split(":");
			client.setBasicAuth(values[0], values[1]);
			syncClient.setBasicAuth(values[0], values[1]);
		}
	}

	public void setAuthCookie(final String login, final String pass){
		mCookieStore.clear();
		mCookieStore.addCookie(new Cookie()
		{
			public String loginpassword = login + ":" + pass;

			@Override
			public String getName()
			{
				return AUTH_COOKIE_NAME;
			}

			@Override
			public String getValue()
			{
				return loginpassword;
			}

			@Override
			public String getComment()
			{
				return null;
			}

			@Override
			public String getCommentURL()
			{
				return null;
			}

			@Override
			public Date getExpiryDate()
			{
				return null;
			}

			@Override
			public boolean isPersistent()
			{
				return true;
			}

			@Override
			public String getDomain()
			{
				return null;
			}

			@Override
			public String getPath()
			{
				return null;
			}

			@Override
			public int[] getPorts()
			{
				return new int[0];
			}

			@Override
			public boolean isSecure()
			{
				return false;
			}

			@Override
			public int getVersion()
			{
				return 0;
			}

			@Override
			public boolean isExpired(Date date)
			{
				return false;
			}
		});
	}

	public static String getBaseUrl(Context context)
	{
/*
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(!prefs.contains(URL_PREFS_KEY))
		{
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(URL_PREFS_KEY, context.getResources().getString(R.string.base_url));
			editor.commit();
		}
		return prefs.getString(URL_PREFS_KEY, "");
*/
		return "http://hodoor.squishyfacesoftware.com:8000";
	}

	private String getFullUrl(String relativeUrl) {
		String baseUrl = getBaseUrl(mContext);
		return baseUrl + relativeUrl;
	}

}

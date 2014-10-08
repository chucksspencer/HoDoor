package com.squishyfacesoftware.hodoor.DataAccess;

import android.content.Context;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import org.apache.http.cookie.Cookie;

import java.util.Date;
import java.util.List;

public class HodoorIntranetWebServiceClient
{
	public final AsyncHttpClient client;
	public final PersistentCookieStore mCookieStore;
	private final Context mContext;
	static private HodoorIntranetWebServiceClient ywscInstance;
	public static final String URL_PREFS_KEY = "baseUrl";
	public static final String AUTH_COOKIE_NAME = "authCookie";

	public static HodoorIntranetWebServiceClient getInstance(Context context){
		if(ywscInstance == null) ywscInstance = new HodoorIntranetWebServiceClient(context);
		return ywscInstance;
	}

	private HodoorIntranetWebServiceClient(Context context)
	{
		client = new AsyncHttpClient();
		client.setTimeout(6000);

		mCookieStore = new PersistentCookieStore(context);
		client.setCookieStore(mCookieStore);

		mContext = context;
	}

	public void postNoParams(String url, AsyncHttpResponseHandler responseHandler) {
		String fullUrl = getFullUrl(url);
		client.post(fullUrl,  responseHandler);
	}

	public void authenticate(){
		List<Cookie> cookies  = mCookieStore.getCookies();
		if(cookies.size() > 0){
			Cookie authCookie = cookies.get(0);
			String[] values = authCookie.getValue().split(":");
			client.setBasicAuth(values[0], values[1]);
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
		return "http://192.168.1.102:8100";
	}

	private String getFullUrl(String relativeUrl) {
		String baseUrl = getBaseUrl(mContext);
		return baseUrl + relativeUrl;
	}

}

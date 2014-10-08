package com.squishyfacesoftware.hodoor.DataAccess;

import android.content.Context;
import com.loopj.android.http.TextHttpResponseHandler;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.Arrays;

public class ImageUrlProvider
{
	protected TextHttpResponseHandler mResponseHandler;
	private final static String GET_CURRENT_IMAGE_URL = "/macros/getCurrentImage";
	private final static String GET_IMAGES_FOR_EVENT_URL = "/macros/getImagesForEvent/";

	public ImageUrlProvider(){
		mResponseHandler = new TextHttpResponseHandler(){
			@Override
			public void onFailure(int i, Header[] headers, String s, Throwable throwable)
			{
				ImageUrlProvider.this.onFailure(throwable.getMessage());
			}

			@Override
			public void onSuccess(int statusCode, Header[] headers, String responseBody)
			{
				if(responseBody.contains("[[")){
					String parsed = responseBody.replaceAll("\\[", "").replaceAll("\\]","").replaceAll("\"", "").replaceAll(" ", "");
					String[] strings = parsed.split(",");
					ArrayList<String> stringArray = new ArrayList<String>(Arrays.asList(strings));
					onImagesUrlRetrieved(stringArray);
				}
				else{
					onImageUrlRetrieved(responseBody);
				}
			}

		};
	}

	public void onImageUrlRetrieved(String url){

	}

	public void onImagesUrlRetrieved(ArrayList<String> urls){

	}

	public void onFailure(String error){

	}

	public void GetCurrentImage(Context context){
		HodoorWebServiceClient.getInstance(context).postNoParams(GET_CURRENT_IMAGE_URL, mResponseHandler);
	}

	public void GetImagesForEvent(Context context, String eventName){
		HodoorWebServiceClient.getInstance(context).postNoParams(GET_IMAGES_FOR_EVENT_URL + eventName, mResponseHandler);
	}
}

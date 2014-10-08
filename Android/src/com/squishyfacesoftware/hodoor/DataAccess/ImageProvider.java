package com.squishyfacesoftware.hodoor.DataAccess;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.loopj.android.http.BinaryHttpResponseHandler;
import org.apache.http.Header;

public class ImageProvider
{
	protected BinaryHttpResponseHandler mResponseHandler;
	private final static String IMAGE_URL_PREFIX = "/ImageCapture/";

	public ImageProvider(){
		mResponseHandler = new BinaryHttpResponseHandler (){

			@Override
			public void onSuccess(int i, Header[] headers, byte[] bytes)
			{
				Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
				onImageRetrieved(image);

			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error)
			{
				ImageProvider.this.onFailure(error.getMessage());
			}
		};
	}

	public void onImageRetrieved(Bitmap image){

	}

	public void onFailure(String error){

	}

	public void GetImage(Context context, String imageName){
		HodoorWebServiceClient.getInstance(context).get(IMAGE_URL_PREFIX + imageName, mResponseHandler);
	}
}

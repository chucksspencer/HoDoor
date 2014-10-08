package com.squishyfacesoftware.hodoor.Fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.loopj.android.image.SmartImageView;
import com.squishyfacesoftware.hodoor.DataAccess.ImageProvider;
import com.squishyfacesoftware.hodoor.R;

import java.io.InputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class ImageFragment extends Fragment
{

	String mImageUrl;
	ImageProvider mImageProvider;
	private ImageView mfragmentImageView;
	private Bitmap mImage;
	private Boolean mIsLoading;
	public static ImageFragment getInstance(String imageUrl)
	{
		ImageFragment instance = new ImageFragment();
		instance.mImageUrl = imageUrl;
		instance.mIsLoading = false;
		instance.mImage = null;
		return instance;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate(R.layout.imagefragment, container, false);
		mfragmentImageView = (ImageView)getActivity().findViewById(R.id.fragmentImage);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		setImage();
	}

	private void setImage(){
		if(mImage == null)
		{
			if(!mIsLoading){
				downloadImage();
			}
		}
		else
		{
			hideProgressIndicator();
			View view = getView();
			mfragmentImageView = (ImageView)view.findViewById(R.id.fragmentImage);
			mfragmentImageView.setImageBitmap(mImage);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
	}

	private void downloadImage(){
		mIsLoading = true;
		if(mImageProvider == null){
			mImageProvider = new ImageProvider(){
				@Override
				public void onImageRetrieved(Bitmap image)
				{
					super.onImageRetrieved(image);
					try{
						mIsLoading = false;
						mImage = image;
						hideProgressIndicator();
						ImageFragment.this.setImage();
					}
					catch(Exception e){
						Log.e("Hodoor", e.toString());
					}
				}

				@Override
				public void onFailure(String error)
				{
					super.onFailure(error);
				}
			};
		}

		showProgressIndicator();
		mImageProvider.GetImage(getActivity(), mImageUrl);
	}

	private void showProgressIndicator(){
		LinearLayout progressView = (LinearLayout) getActivity().findViewById(R.id.imageLoadProgress);
		progressView.setVisibility(View.VISIBLE);
	}

	private void hideProgressIndicator(){
		LinearLayout progressView = (LinearLayout) getActivity().findViewById(R.id.imageLoadProgress);
		progressView.setVisibility(View.GONE);
	}


}

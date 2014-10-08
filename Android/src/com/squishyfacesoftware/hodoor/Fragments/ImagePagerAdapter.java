package com.squishyfacesoftware.hodoor.Fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public class ImagePagerAdapter extends FragmentPagerAdapter
{
	List<String> mImageUrls;
	public ImagePagerAdapter(FragmentManager fm, List<String> imageUrls){
		super(fm);
		mImageUrls = imageUrls;
	}

	@Override
	public Fragment getItem(int position)
	{
		if(mImageUrls.size() >= position){
			return ImageFragment.getInstance(mImageUrls.get(position));
		}
		return null;
	}

	@Override
	public int getCount()
	{
		return mImageUrls.size();
	}
}

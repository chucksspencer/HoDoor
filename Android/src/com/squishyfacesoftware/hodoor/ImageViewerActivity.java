package com.squishyfacesoftware.hodoor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.view.WindowManager;
import com.squishyfacesoftware.hodoor.Fragments.ImagePagerAdapter;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.PageIndicator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageViewerActivity extends FragmentActivity
{
	ViewPager mPager;
	PageIndicator mIndicator;
	ImagePagerAdapter mAdapter;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//Remove title bar
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.imageviewer);

		Intent intent = getIntent();
		List<String> urlList = intent.getStringArrayListExtra("IMAGE_LOCATIONS");
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), urlList);

		mPager = (ViewPager)findViewById(R.id.imagepager);
		mPager.setAdapter(mAdapter);
		mPager.setOffscreenPageLimit(2);

		mIndicator = (CirclePageIndicator)findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
	}
}
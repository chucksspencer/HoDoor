package com.squishyfacesoftware.hodoor.Fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class HoDoorPagerAdapter extends FragmentPagerAdapter
{
	public HoDoorPagerAdapter(FragmentManager fm)
	{
		super(fm);
	}

	@Override
	public Fragment getItem(int position)
	{
		switch (position)
		{
			case 0:
				return OpenerFragment.getInstance();
			case 1:
				return StatusFragment.getInstance();
			case 2:
				return HistoryFragment.getInstance();
			default:
				return null;
		}
	}

	@Override
	public int getCount()
	{
		return 3;
	}
}


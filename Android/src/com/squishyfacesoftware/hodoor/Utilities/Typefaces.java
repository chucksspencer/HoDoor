package com.squishyfacesoftware.hodoor.Utilities;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.Hashtable;

public class Typefaces
{
	private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

	public static Typeface get(Context c, String name)
	{
		synchronized (cache)
		{
			if (!cache.containsKey(name))
			{
				Typeface t = null;
				try
				{
					AssetManager a = c.getApplicationContext().getAssets();
					t = Typeface.createFromAsset(
							a,
							String.format("fonts/%s.ttf", name)
					);
				} catch (Exception e)
				{
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
				cache.put(name, t);
			}
			return cache.get(name);
		}
	}
}
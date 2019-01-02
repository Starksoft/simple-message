package com.simplemessage;

import android.content.res.Resources;

@SuppressWarnings("WeakerAccess")
public class Util {

	private Util() {
		throw new UnsupportedOperationException();
	}

	public static int dpToPx(int dp) {
		return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
	}
}

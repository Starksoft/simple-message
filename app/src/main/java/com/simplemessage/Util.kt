package com.simplemessage

import android.content.res.Resources

object Util {

	@JvmStatic
	fun Int.dpToPx(): Int {
		return (this * Resources.getSystem().displayMetrics.density).toInt()
	}
}

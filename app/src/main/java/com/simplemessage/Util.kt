package com.simplemessage

import android.content.res.Resources
import android.view.View

object Util {

	@JvmStatic
	fun Int.dpToPx(): Int {
		return (this * Resources.getSystem().displayMetrics.density).toInt()
	}

	@JvmStatic
	fun View?.setVisibility(visible: Boolean) {
		this?.visibility = if (visible) {
			View.VISIBLE
		} else {
			View.GONE
		}
	}
}

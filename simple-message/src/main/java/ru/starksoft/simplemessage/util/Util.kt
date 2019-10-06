package ru.starksoft.simplemessage.util

import android.content.res.Resources
import android.view.View

fun Int.dpToPx(): Int {
	return (this * Resources.getSystem().displayMetrics.density).toInt()
}

fun View?.setVisibility(visible: Boolean) {
	this?.visibility = if (visible) {
		View.VISIBLE
	} else {
		View.GONE
	}
}

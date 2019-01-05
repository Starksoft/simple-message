package ru.starksoft.simplemessage.util

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

	@JvmStatic
	fun <T> checkNotNull(reference: T?): T {
		return if (reference == null) {
			throw NullPointerException()
		} else {
			reference
		}
	}

}

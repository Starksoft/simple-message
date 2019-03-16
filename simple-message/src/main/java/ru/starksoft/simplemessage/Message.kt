package ru.starksoft.simplemessage

import android.support.annotation.UiThread

internal interface Message {

	fun getCallback(): SimpleMessageManager.Callback

	fun getMessageData(): MessageData

	fun isDismissing(): Boolean

	fun setOnDismissListener(onDismissListener: OnDismissListener?)

	@UiThread
	fun show()

	@UiThread
	fun hide()

	fun hideSystemUi()

	fun showSystemUi()

	fun showMessageDelay(): Long

	interface OnDismissListener {

		fun onDismissed()
	}
}
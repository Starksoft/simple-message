package ru.starksoft.simplemessage

import android.os.Handler
import android.os.Looper
import android.support.annotation.UiThread
import android.util.Log

internal class SimpleMessageManager private constructor() {

	private val handler = Handler(Looper.getMainLooper())
	private var currentMessage: Message? = null

	@UiThread
	fun show(message: Message, duration: Int) {
		if (currentMessage == null) {
			showInternal(message, duration, message.showMessageDelay())

		} else {
			// TODO: 03/01/2019 Решить, нужно ли разрешать дубликаты сообщений
			val currentMessageSafe = currentMessage!!
			val currentMessageRecord = currentMessageSafe.getMessageData()
			val newMessageRecord = message.getMessageData()

			if (newMessageRecord == currentMessageRecord) {
				Log.d(TAG, "show: trying to show the same message.. skipping")

			} else {
				if (!currentMessageSafe.isDismissing()) {
					handler.removeCallbacksAndMessages(null)
					currentMessageSafe.setOnDismissListener(object : Message.OnDismissListener {
						override fun onDismissed() {
							currentMessageSafe.setOnDismissListener(null)
							showInternal(message, duration, 0)
						}
					})
					currentMessageSafe.getCallback().dismiss()
				} else {
					Log.d(TAG, "show: we are in dismissing state")
				}
			}
		}
	}

	fun hide() {
		currentMessage?.apply {
			getCallback().dismiss()
			showSystemUi()
			currentMessage = null
		}
	}

	fun hide(message: Message) {
		message.getCallback().dismiss()
		message.showSystemUi()
	}

	private fun showInternal(message: Message, duration: Int, messageDelay: Long) {
		currentMessage = message
		val currentMessageSafe = currentMessage!!
		currentMessageSafe.hideSystemUi()

		handler.removeCallbacksAndMessages(null)
		handler.postDelayed({
								currentMessageSafe.getCallback().apply {
									show()
									if (duration > 0) {
										handler.removeCallbacksAndMessages(null)
										handler.postDelayed({
																dismiss()
																currentMessageSafe.showSystemUi()
																currentMessage = null

															}, duration.toLong())
									}
								}
							}, messageDelay)
	}

	fun destroy() {
		handler.removeCallbacksAndMessages(null)
		currentMessage = null
	}

	interface Callback {

		fun show()

		fun dismiss()
	}

	companion object {

		private const val TAG = "SimpleMessageManager"

		@JvmStatic
		val instance: SimpleMessageManager by lazy { SimpleMessageManager() }
	}
}

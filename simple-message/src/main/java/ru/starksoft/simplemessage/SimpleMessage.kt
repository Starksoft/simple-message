package ru.starksoft.simplemessage

import android.app.Activity
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.annotation.ColorInt
import android.support.v7.app.AppCompatActivity
import android.util.Log

class SimpleMessage private constructor(activity: Activity, messageData: MessageData, messageType: MessageType) {

	private val activity: Activity = checkNotNull(activity)

	private val view: Message = getMessageImplByType(messageType, activity, messageData)

	init {
		observeLifecycle()
	}

	private fun observeLifecycle() {
		if (activity is AppCompatActivity) {
			activity.lifecycle.addObserver(object : LifecycleObserver {
				@OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
				fun destroy() {
					SimpleMessageManager.instance.destroy()
					Log.d(TAG, "destroy() called")
				}
			})
		}
	}

	fun show() {
		view.show()
	}

	fun hide() {
		view.hide()
	}

	private fun getMessageImplByType(messageType: MessageType, activity: Activity, messageData: MessageData): Message {
		return when (messageType) {
			MessageType.STATUS_BAR -> StatusBarMessageLayout(activity, messageData)

			MessageType.TOOL_BAR -> TODO("Not implemented")
		}
	}

	@Suppress("MemberVisibilityCanBePrivate")
	class Builder(private val activity: Activity, message: CharSequence, private val messageType: MessageType, template: MessageTemplate?) {

		private val messageData: MessageData

		init {
			messageData = getMessageRecordByTemplate(message, template)
			messageData.message = message
		}

		private fun getMessageRecordByTemplate(message: CharSequence, template: MessageTemplate?): MessageData {
			return when (template) {
				MessageTemplate.ERROR_PERSISTENT -> MessageData.createErrorPersistent(message)

				MessageTemplate.ERROR -> MessageData.createError(message)

				MessageTemplate.NORMAL_PROGRESS -> MessageData.createNormalWithProgress(message)

				MessageTemplate.NORMAL -> MessageData.createNormal(message)

				MessageTemplate.SUCCESS -> MessageData.createSuccess(message)

				else -> MessageData()
			}
		}

		fun textColor(@ColorInt color: Int): Builder {
			messageData.textColor = color
			return this
		}

		fun backgroundColor(@ColorInt color: Int): Builder {
			messageData.backgroundColor = color
			return this
		}

		fun progress(progress: Boolean): Builder {
			messageData.isProgress = progress
			return this
		}

		fun persistent(persistent: Boolean): Builder {
			messageData.isPersistent = persistent
			return this
		}

		fun build(): SimpleMessage {
			return SimpleMessage(activity, messageData, messageType)
		}

		fun show() {
			build().show()
		}
	}

	companion object {

		private const val TAG = "SimpleMessage"

		@JvmStatic
		fun create(activity: Activity, message: CharSequence): Builder {
			return create(activity, message, MessageType.STATUS_BAR, null)
		}

		@JvmStatic
		fun create(activity: Activity, message: CharSequence, template: MessageTemplate?): Builder {
			return create(activity, message, MessageType.STATUS_BAR, template)
		}

		@JvmStatic
		fun create(activity: Activity, message: CharSequence, messageType: MessageType, template: MessageTemplate?): Builder {
			return Builder(activity, message, messageType, template)
		}

	}
}
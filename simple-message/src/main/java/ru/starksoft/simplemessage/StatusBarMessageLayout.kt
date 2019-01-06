package ru.starksoft.simplemessage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import ru.starksoft.simplemessage.util.dpToPx
import ru.starksoft.simplemessage.util.setVisibility

@SuppressLint("ViewConstructor")
internal class StatusBarMessageLayout(context: Context, messageData: MessageData) : BaseMessageLayout(context, messageData) {
	private lateinit var messageTextView: TextView
	private lateinit var progressBar: ProgressBar

	override fun createView() {
		LayoutInflater.from(context).inflate(R.layout.message_statusbar, this)

		gravity = Gravity.CENTER
		setPadding(PADDING_LEFT_RIGHT, 0, PADDING_LEFT_RIGHT, 0)

		messageTextView = findViewById(R.id.text)
		progressBar = findViewById(R.id.progress)

		val backgroundColor = messageData.backgroundColor
		if (backgroundColor != 0) {
			setBackgroundColor(backgroundColor)
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				setBackgroundColor(window.statusBarColor)
			}
		}
		messageTextView.text = messageData.message

		val textColor = messageData.textColor
		messageTextView.setTextColor(textColor)

		val progress = messageData.isProgress
		progressBar.setVisibility(progress)

		if (progress) {
			progressBar.indeterminateDrawable.setColorFilter(textColor, PorterDuff.Mode.SRC_IN)
		}
	}

	override fun createAnimationCallback(): MessageAnimationCallback {
		return object : MessageAnimationCallback {
			override fun onShowAnimationStart() {
				animateContentIn(messageTextView, progressBar)
			}

			override fun onShowAnimationEnd() {

			}

			override fun onHideAnimationStart() {
				animateContentOut(messageTextView, progressBar)
			}

			override fun onHideAnimationEnd() {

			}
		}
	}

	companion object {
		val PADDING_LEFT_RIGHT = 8.dpToPx()
	}
}
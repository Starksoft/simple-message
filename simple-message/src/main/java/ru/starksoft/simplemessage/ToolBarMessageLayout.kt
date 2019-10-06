package ru.starksoft.simplemessage

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.os.Build
import android.support.annotation.Px
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import ru.starksoft.simplemessage.util.dpToPx
import ru.starksoft.simplemessage.util.setVisibility

@SuppressLint("ViewConstructor")
internal class ToolBarMessageLayout(context: Context, messageData: MessageData) : BaseMessageLayout(context, messageData) {

	private lateinit var messageTextView: TextView
	private lateinit var progressBar: ProgressBar
	private lateinit var toolbarBackground: View
	private lateinit var messageContent: LinearLayout

	override fun createView() {
		LayoutInflater.from(context).inflate(R.layout.message_toolbar, this)

		messageTextView = findViewById(R.id.text)
		progressBar = findViewById(R.id.progress)
		toolbarBackground = findViewById(R.id.toolbarBackground)
		messageContent = findViewById(R.id.messageContent)

		val statusBarHeight = statusBarHeight
		resizeView(toolbarBackground, MATCH_PARENT, statusBarHeight)

		messageContent.setPadding(messageContent.paddingLeft, statusBarHeight, messageContent.paddingRight, messageContent.paddingBottom)

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

	private fun resizeView(view: View, newWidth: Int, newHeight: Int) {
		try {
			view.layoutParams = view
				.layoutParams.javaClass.getDeclaredConstructor(Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
				.newInstance(newWidth, newHeight)

		} catch (e: Exception) {
			e.printStackTrace()
		}
	}

	override fun getViewHeight(): Int {
		return getToolbarHeight() + statusBarHeight
	}

	@Px
	private fun getToolbarHeight(): Int {
		val typedValue = TypedValue()

		return if (context.theme.resolveAttribute(R.attr.actionBarSize, typedValue, true)) {
			TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
		} else {
			56.dpToPx()
		}
	}

	override fun hideSystemUi() {
		// nop
	}

	override fun createAnimationCallback(): MessageAnimationCallback {
		return object : MessageAnimationCallback {
			override fun onShowAnimationStart() {
				animateContentIn(toolbarBackground, messageTextView, progressBar)
			}

			override fun onShowAnimationEnd() {
			}

			override fun onHideAnimationStart() {
				animateContentOut(toolbarBackground, messageTextView, progressBar)
			}

			override fun onHideAnimationEnd() {
			}
		}
	}

	override fun showMessageDelay(): Long {
		return 0
	}
}

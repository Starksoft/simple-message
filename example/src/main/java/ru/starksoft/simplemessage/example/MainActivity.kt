package ru.starksoft.simplemessage.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import ru.starksoft.simplemessage.MessageTemplate
import ru.starksoft.simplemessage.MessageType
import ru.starksoft.simplemessage.SimpleMessage

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		showMessageButton.setOnClickListener {
			SimpleMessage.create(this, "normal message", MessageTemplate.NORMAL).messageType(getMessageType()).show()
		}

		showMessageButton2.setOnClickListener {
			SimpleMessage.create(this, "Loading...", MessageTemplate.NORMAL_PROGRESS).messageType(getMessageType()).show()
		}

		showMessageButton3.setOnClickListener {
			SimpleMessage.create(this, "Success!", MessageTemplate.SUCCESS).messageType(getMessageType()).show()
		}

		showMessageButton4.setOnClickListener {
			SimpleMessage.create(this, "No network", MessageTemplate.ERROR).messageType(getMessageType()).show()
		}

		showMessageButton5.setOnClickListener {
			SimpleMessage.create(this, "No network persistent", MessageTemplate.ERROR_PERSISTENT).messageType(getMessageType()).show()
		}

		showMessageButton6.setOnClickListener {
			SimpleMessage
				.create(this, "Custom message")
				.textColor(-0x1)
				.backgroundColor(-0x27c3f3)
				.progress(true)
				.persistent(true)
				.messageType(getMessageType())
				.show()
		}

		showMessageButton7.setOnClickListener {
			SimpleMessage.hide()
		}
	}

	private fun getMessageType(): MessageType {
		return when (messageTypeRadioGroup.checkedRadioButtonId) {

			R.id.statusBarRadioButton -> MessageType.STATUS_BAR

			R.id.toolBarRadioButton -> MessageType.TOOL_BAR

			else -> throw UnsupportedOperationException()
		}
	}
}

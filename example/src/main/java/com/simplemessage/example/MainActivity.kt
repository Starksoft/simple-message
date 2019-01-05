package com.simplemessage.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.simplemessage.MessageTemplate
import com.simplemessage.SimpleMessage
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		showMessageButton.setOnClickListener {
			SimpleMessage.create(this, "normal message").show()
		}

		showMessageButton2.setOnClickListener {
			SimpleMessage.create(this, "Loading...", MessageTemplate.NORMAL_PROGRESS).show()
		}

		showMessageButton3.setOnClickListener {
			SimpleMessage.create(this, "Success!", MessageTemplate.SUCCESS).show()
		}

		showMessageButton4.setOnClickListener {
			SimpleMessage.create(this, "No network", MessageTemplate.ERROR).show()
		}

		showMessageButton5.setOnClickListener {
			SimpleMessage.create(this, "No network persistent", MessageTemplate.ERROR_PERSISTENT).show()
		}

		showMessageButton6.setOnClickListener {
			SimpleMessage
				.create(this, "Custom message")
				.textColor(-0x1)
				.backgroundColor(-0x27c3f3)
				.progress(true)
				.persistent(true)
				.show()
		}
	}
}
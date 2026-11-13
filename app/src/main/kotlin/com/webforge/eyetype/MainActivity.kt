package com.webforge.eyetype

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 36)
            setBackgroundColor(0xFF101713.toInt())
        }

        val title = TextView(this).apply {
            text = "EyeType"
            textSize = 34f
            setTextColor(0xFFF4FFF7.toInt())
            gravity = Gravity.CENTER
        }
        val subtitle = TextView(this).apply {
            text = "Your keyboard, controlled by your gaze."
            textSize = 16f
            setTextColor(0xFFB7C8BD.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 12, 0, 28)
        }
        val status = TextView(this).apply {
            text = "Foundation build · Touch keyboard available · Eye tracking is not connected yet"
            textSize = 14f
            setTextColor(0xFFE3EEE6.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        }
        val enable = Button(this).apply {
            text = "1 · Enable EyeType in Settings"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        }
        val choose = Button(this).apply {
            text = "2 · Choose EyeType Keyboard"
            setOnClickListener {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        }
        val test = Button(this).apply {
            text = "3 · Test keyboard"
            setOnClickListener {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(status)
        root.addView(enable)
        root.addView(choose)
        root.addView(test)
        setContentView(root)
    }
}

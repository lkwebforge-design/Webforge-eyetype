package com.webforge.eyetype.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout

class EyeTypeInputMethodService : InputMethodService() {
    override fun onCreateInputView(): View {
        return EyeTypeKeyboardView(this) { key -> handleKey(key) }
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
    }

    private fun handleKey(key: String) {
        val connection = currentInputConnection ?: return
        when (key) {
            "⌫" -> connection.deleteSurroundingText(1, 0)
            "SPACE" -> connection.commitText(" ", 1)
            "↵" -> connection.commitText("\n", 1)
            "123" -> { /* Number layout is added in the next keyboard milestone. */ }
            else -> connection.commitText(key, 1)
        }
    }
}

private class EyeTypeKeyboardView(
    service: InputMethodService,
    private val onKey: (String) -> Unit
) : LinearLayout(service) {
    init {
        orientation = VERTICAL
        setPadding(6, 8, 6, 8)
        setBackgroundColor(0xFF101713.toInt())

        val rows = listOf(
            listOf("Q","W","E","R","T","Y","U","I","O","P"),
            listOf("A","S","D","F","G","H","J","K","L"),
            listOf("Z","X","C","V","B","N","M","⌫"),
            listOf("123",",","SPACE",".","↵")
        )
        rows.forEach { row ->
            val grid = GridLayout(service).apply {
                columnCount = row.size
                useDefaultMargins = true
            }
            row.forEach { label ->
                val button = Button(service).apply {
                    text = label
                    isAllCaps = false
                    textSize = if (label.length > 1) 12f else 17f
                    setTextColor(0xFFF4FFF7.toInt())
                    setBackgroundColor(0xFF26372D.toInt())
                    setOnClickListener { onKey(label) }
                    contentDescription = "Type $label"
                }
                grid.addView(button, GridLayout.LayoutParams().apply {
                    width = 0
                    height = (if (label == "SPACE") 48 else 52)
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(2, 2, 2, 2)
                })
            }
            addView(grid, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        }
    }
}

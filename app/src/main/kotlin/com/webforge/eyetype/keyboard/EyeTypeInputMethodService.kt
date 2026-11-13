package com.webforge.eyetype.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout

class EyeTypeInputMethodService : InputMethodService() {
    override fun onCreateInputView(): View =
        EyeTypeKeyboardView(this) { key -> handleKey(key) }

    private fun handleKey(key: String) {
        val connection = currentInputConnection ?: return
        when (key) {
            "⌫" -> connection.deleteSurroundingText(1, 0)
            "SPACE" -> connection.commitText(" ", 1)
            "↵" -> {
                val actionId = currentInputEditorInfo?.actionId ?: 0
                if (actionId != 0) connection.performEditorAction(actionId)
                else connection.commitText("\n", 1)
            }
            else -> connection.commitText(key, 1)
        }
    }
}

private class EyeTypeKeyboardView(
    service: InputMethodService,
    private val onKey: (String) -> Unit
) : LinearLayout(service) {
    private var symbolsMode = false
    private var shiftMode = false

    private val letters = listOf(
        listOf("Q","W","E","R","T","Y","U","I","O","P"),
        listOf("A","S","D","F","G","H","J","K","L"),
        listOf("⇧","Z","X","C","V","B","N","M","⌫"),
        listOf("?123",",","SPACE",".","↵")
    )
    private val symbols = listOf(
        listOf("1","2","3","4","5","6","7","8","9","0"),
        listOf("@","#","$","%","&","-","+","(",")"),
        listOf("=","*","' ",":",";","!","?","~","⌫"),
        listOf("ABC",",","SPACE",".","↵")
    )

    init {
        orientation = VERTICAL
        setPadding(6, 8, 6, 8)
        setBackgroundColor(0xFF101713.toInt())
        render()
    }

    private fun render() {
        removeAllViews()
        val rows = if (symbolsMode) symbols else letters
        rows.forEach { row ->
            val grid = GridLayout(context).apply {
                columnCount = row.size
                useDefaultMargins = true
            }
            row.forEach { rawLabel ->
                val label = when {
                    rawLabel == "SPACE" -> "English"
                    rawLabel.length == 1 && rawLabel[0].isLetter() && !shiftMode -> rawLabel.lowercase()
                    else -> rawLabel
                }
                val button = Button(context).apply {
                    text = if (rawLabel == "SPACE") "English" else label
                    isAllCaps = false
                    textSize = if (rawLabel.length > 1) 11f else 17f
                    setTextColor(0xFFF4FFF7.toInt())
                    setBackgroundColor(
                        if (rawLabel in listOf("⌫", "↵", "⇧", "?123", "ABC")) 0xFF385344.toInt()
                        else 0xFF26372D.toInt()
                    )
                    contentDescription = when (rawLabel) {
                        "SPACE" -> "Space"
                        "⌫" -> "Backspace"
                        "↵" -> "Enter"
                        "⇧" -> "Shift"
                        "?123" -> "Numbers and symbols"
                        "ABC" -> "Letters"
                        else -> "Type $label"
                    }
                    setOnClickListener { handleKey(rawLabel) }
                }
                grid.addView(button, GridLayout.LayoutParams().apply {
                    width = 0
                    height = if (rawLabel == "SPACE") 48 else 52
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(2, 2, 2, 2)
                })
            }
            addView(grid, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
        }
    }

    private fun handleKey(key: String) {
        when (key) {
            "?123" -> { symbolsMode = true; render() }
            "ABC" -> { symbolsMode = false; render() }
            "⇧" -> { shiftMode = !shiftMode; render() }
            else -> {
                val output = if (!symbolsMode && shiftMode && key.length == 1 && key[0].isLetter()) key.uppercase() else key
                onKey(output)
                if (shiftMode && key.length == 1 && key[0].isLetter()) {
                    shiftMode = false
                    render()
                }
            }
        }
    }
}

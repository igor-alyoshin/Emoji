package io.wola.app

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.CallSuper
import androidx.appcompat.widget.AppCompatEditText
import com.vanniktech.emoji.EmojiManager

class EmojiEditText(context: Context, attrs: AttributeSet) : AppCompatEditText(context, attrs) {

    init {
        setOnClickListener { v: View? ->
            v?.let {
                if (!it.isFocused) {
                    requestFocus()
                } else {
                    clearFocus()
                    requestFocus()
                }
            }
        }
    }

    @CallSuper
    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        if (isInEditMode) return
        val fontMetrics = paint.fontMetrics
        val defaultEmojiSize = fontMetrics.descent - fontMetrics.ascent
        EmojiManager.getInstance().replaceWithImages(context, getText(), defaultEmojiSize)
    }
}
package io.wola.app

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.viewpager2.widget.ViewPager2
import com.mikepenz.fastadapter.FastAdapter
import com.wola.android.emoji.R
import com.wola.android.emoji.databinding.EmojiKeyboardBinding

class EmojiKeyboardView(context: Context, attrs: AttributeSet? = null) :
    ConstraintLayout(context, attrs) {

    private val binding = EmojiKeyboardBinding.inflate(LayoutInflater.from(context), this)

    private val emojiViewPager get() = binding.emojiViewPager
    private val emojiViewTab get() = binding.emojiViewTab

    private val tabHeight by lazy { resources.getDimensionPixelSize(R.dimen.emoji_header_height) }
    private val dividerHeight by lazy {
        resources.getDimensionPixelSize(R.dimen.emoji_divider_height)
    }

    val currentItem get() = emojiViewPager.currentItem

    var onPageChange: ((Int) -> Unit)? = null

    fun setupSizes(keyboardHeight: Int) {
        emojiViewPager.updateLayoutParams {
            height = keyboardHeight - tabHeight - dividerHeight
        }
    }

    fun increasePageLimitIfNeed() {
        emojiViewPager.offscreenPageLimit = emojiViewPager.adapter?.itemCount ?: 0
    }

    fun setupPages(adapter: FastAdapter<*>, position: Int) {
        emojiViewPager.adapter = adapter
        emojiViewTab.initWith(emojiViewPager)
        emojiViewPager.setCurrentItem(position, false)
        emojiViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                onPageChange?.invoke(position)
            }
        })
    }

    fun setOnBackspaceClick(onClick: () -> Unit) {
        emojiViewTab.setOnBackspaceClick(onClick)
    }
}
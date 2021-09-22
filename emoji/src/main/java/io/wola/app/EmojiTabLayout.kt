package io.wola.app

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mikepenz.fastadapter.FastAdapter
import com.vanniktech.emoji.listeners.RepeatListener
import com.wola.android.emoji.R
import java.util.concurrent.TimeUnit

class EmojiTabLayout(context: Context, attrs: AttributeSet) : TabLayout(context, attrs) {

    private var backspaceTab: Tab? = null
    private var onBackspaceClick: (() -> Unit)? = null

    fun setOnBackspaceClick(onClick: () -> Unit) {
        onBackspaceClick = onClick
    }

    fun initWith(viewPager: ViewPager2) {
        TabLayoutMediator(this, viewPager, false) { tab, position ->
            tab.setCustomView(R.layout.emoji_tab)
            val adapter = viewPager.adapter as FastAdapter<*>
            val iconResId =
                adapter.getItem(position)?.identifier?.toInt() ?: R.drawable.emoji_recent
            tab.setIcon(iconResId)
        }.attach()
        backspaceTab = newTab().apply {
            setCustomView(R.layout.emoji_tab)
            setIcon(R.drawable.emoji_backspace)
            addTab(this)
            view.setOnTouchListener(
                RepeatListener(INITIAL_INTERVAL, NORMAL_INTERVAL) {
                    onBackspaceClick?.invoke()
                }
            )
        }

    }

    override fun selectTab(tab: Tab?, updateIndicator: Boolean) {
        if (tab == backspaceTab) {
            onBackspaceClick?.invoke()
        } else {
            super.selectTab(tab, updateIndicator)
        }
    }

    companion object {
        private val INITIAL_INTERVAL = TimeUnit.SECONDS.toMillis(1) / 2
        private val NORMAL_INTERVAL = 50L
    }
}
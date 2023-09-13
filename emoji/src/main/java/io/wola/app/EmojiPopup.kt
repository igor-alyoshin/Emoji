package io.wola.app

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.res.Resources
import android.view.ViewPropertyAnimator
import android.widget.EditText
import androidx.core.view.WindowInsetsCompat
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.vanniktech.emoji.*
import com.vanniktech.emoji.Utils.backspace
import com.vanniktech.emoji.Utils.input
import com.vanniktech.emoji.emoji.Emoji
import com.vanniktech.emoji.emoji.EmojiCategory
import com.wola.android.emoji.R

class EmojiPopup(private val activity: Activity) {

    private val preferences =
        activity.applicationContext.getSharedPreferences("emoji", MODE_PRIVATE)
    private val recentEmoji = RecentEmojiManager(activity)
    private val variantEmoji = VariantEmojiManager(activity)
    private var variantPopup: EmojiVariantPopup? = null

    private val recentEmojisPageItem =
        EmojiPageItem(
            tabIconResId = R.drawable.emoji_recent,
            emojis = recentEmoji.recentEmojis.map { EmojiItem(it, variantEmoji) }
        )
    private var categories = listOf<EmojiCategory>()

    private val expanded get() = emojiKeyboardView.translationY == 0f && hideAnimator == null
    private val emojiWidth =
        activity.resources.getDimensionPixelSize(R.dimen.emoji_grid_view_column_width)

    private lateinit var editText: EditText
    private lateinit var emojiKeyboardView: EmojiKeyboardView

    private var hideAnimator: ViewPropertyAnimator? = null
    private var showAnimator: ViewPropertyAnimator? = null
    private var keyboardHeight = restoreKeyboardHeight()
    private var mode: KeyboardMode = KeyboardMode.Hidden
    private var postNotifyRecentEmojis = false
    private var bottomOffset = 0

    var onModeChanged: ((KeyboardMode) -> Unit)? = null
    var onBottomChanged: ((Int) -> Unit)? = null

    sealed class KeyboardMode {
        object Hidden : KeyboardMode()
        object Soft : KeyboardMode()
        object Emoji : KeyboardMode()
    }

    fun setupWith(editText: EditText, emojiKeyboardView: EmojiKeyboardView) {
        this.editText = editText
        this.emojiKeyboardView = emojiKeyboardView
        init()
    }

    fun onInsetsApplied(insets: WindowInsetsCompat) {
        bottomOffset = insets.getGlobalInsets().bottom
        val imeHeight = insets.getImeInsets().bottom
        val keyboardVisible = imeHeight > 0
        if (!keyboardVisible && mode == KeyboardMode.Soft) {
            setMode(KeyboardMode.Hidden)
        } else if (keyboardVisible) {
            keyboardHeight = imeHeight
            saveKeyboardHeight(imeHeight)
            emojiKeyboardView.setupSizes(imeHeight)
            setMode(KeyboardMode.Soft)
        }
        onBottomChanged?.invoke(getBottomOffset())
    }

    private fun getBottomOffset(): Int {
        return if (mode == KeyboardMode.Emoji) {
            bottomOffset + keyboardHeight
        } else {
            bottomOffset
        }
    }

    fun init() {
        emojiKeyboardView.alpha = 0f
        emojiKeyboardView.translationY = keyboardHeight.toFloat()
        categories = EmojiManager.getInstance().categories.toList()
        val categoryItems: List<GenericItem> = categories.map { category ->
            EmojiPageItem(category.icon, category.emojis.map { EmojiItem(it, variantEmoji) })
        }
        val numOfColumns = activity.resources.displayMetrics.widthPixels / emojiWidth
        val adapter = createFastAdapter(listOf(recentEmojisPageItem).plus(categoryItems)).apply {
            addOnCreateViewHolder<EmojiPageItem.ViewHolder> {
                setup(
                    numOfColumns = numOfColumns,
                    onEmojiClick = { item ->
                        editText.input(item.emoji)
                        updateRecentEmojis(this@apply, emojiKeyboardView, item.emoji)
                    },
                    onEmojiLongClick = { v, item ->
                        variantPopup?.show(v, item.emoji)
                    }
                )
            }
        }
        variantPopup =
            EmojiVariantPopup(emojiKeyboardView.rootView) { view, emoji ->
                editText.input(emoji)
                view.setEmoji(emoji)
                view.setImageDrawable(emoji.getDrawable(activity))
                variantEmoji.addVariant(emoji)
                updateRecentEmojis(adapter, emojiKeyboardView, emoji)
                variantPopup?.dismiss()
            }
        val initialPosition = if (recentEmoji.recentEmojis.isEmpty()) 1 else RECENT_EMOJIS_POSITION
        emojiKeyboardView.setupPages(adapter, initialPosition)
        emojiKeyboardView.setupSizes(keyboardHeight)
        emojiKeyboardView.setOnBackspaceClick {
            editText.backspace()
        }
        emojiKeyboardView.onPageChange = { position ->
            if (position == RECENT_EMOJIS_POSITION && postNotifyRecentEmojis) {
                postNotifyRecentEmojis = false
                adapter.notifyItemChanged(recentEmojisPageItem)
            }
        }
        editText.setOnClickListener {
            if (mode == KeyboardMode.Hidden) {
                setMode(KeyboardMode.Soft)
                editText.requestFocus()
                activity.showKeyboard(editText)
            }
        }
    }

    fun release() {
        onModeChanged = null
        onBottomChanged = null
        handlePopupClosed()
    }

    fun toggle() {
        if (expanded) {
            if (mode == KeyboardMode.Soft) {
                setMode(KeyboardMode.Emoji)
                activity.hideKeyboard(editText)
            } else {
                setMode(KeyboardMode.Soft)
                activity.showKeyboard(editText)
            }
        } else {
            setMode(KeyboardMode.Emoji)
            onBottomChanged?.invoke(getBottomOffset())
            activity.hideKeyboard(editText)
        }
    }

    fun handleBackPressed(): Boolean {
        return when(mode) {
            KeyboardMode.Emoji -> {
                setMode(KeyboardMode.Hidden)
                onBottomChanged?.invoke(getBottomOffset())
                true
            }
            KeyboardMode.Soft -> {
                setMode(KeyboardMode.Hidden)
                activity.hideKeyboard(editText)
                true
            }
            KeyboardMode.Hidden -> false
        }
    }

    private fun expand() {
        if (showAnimator == null) {
            hideAnimator?.cancel()
            hideAnimator = null
            val duration = if (keyboardHeight > emojiKeyboardView.measuredHeight) {
                ANIMATION_DURATION * (keyboardHeight - emojiKeyboardView.measuredHeight) / keyboardHeight
            } else {
                ANIMATION_DURATION * emojiKeyboardView.measuredHeight / keyboardHeight
            }
            showAnimator = emojiKeyboardView.animate().translationY(0f)
                .setDuration(duration)
                .withEndAction {
                    showAnimator = null
                    if (mode == KeyboardMode.Emoji) {
                        emojiKeyboardView.postInLifecycle {
                            emojiKeyboardView.increasePageLimitIfNeed()
                        }
                    }
                }
            showAnimator?.start()
        }
    }

    private fun collapse() {
        if (hideAnimator == null) {
            showAnimator?.cancel()
            showAnimator = null
            val duration = ANIMATION_DURATION * emojiKeyboardView.measuredHeight / keyboardHeight
            hideAnimator = emojiKeyboardView.animate().translationY(keyboardHeight.toFloat())
                .setDuration(duration)
                .withEndAction {
                    hideAnimator = null
                    handlePopupClosed()
                }
            hideAnimator?.start()
        }
    }

    private fun handlePopupClosed() {
        recentEmoji.persist()
        variantEmoji.persist()
        variantPopup?.dismiss()
    }

    private fun setMode(newMode: KeyboardMode) {
        if (mode != newMode) {
            mode = newMode
            onModeChanged?.invoke(newMode)
            when (newMode) {
                KeyboardMode.Emoji -> {
                    emojiKeyboardView.alpha = 1f
                    expand()
                }
                KeyboardMode.Soft -> {
                    emojiKeyboardView.postDelayedInLifecycle(100L) {
                        emojiKeyboardView.alpha = 0f
                    }
                    expand()
                }
                KeyboardMode.Hidden -> {
                    emojiKeyboardView.alpha = 0f
                    collapse()
                }
            }
        }
    }

    private fun restoreKeyboardHeight(): Int {
        return preferences.getInt(
            "emoji_keyboard_height",
            (Resources.getSystem().displayMetrics.heightPixels * DEFAULT_KEYBOARD_PART).toInt()
        )
    }

    private fun saveKeyboardHeight(height: Int) {
        preferences.edit().putInt(KEY_KEYBOARD_HEIGHT, height).apply()
    }

    private fun updateRecentEmojis(
        adapter: FastAdapter<*>,
        emojiKeyboardView: EmojiKeyboardView,
        emoji: Emoji
    ) {
        recentEmoji.addEmoji(emoji)
        recentEmojisPageItem.emojis = recentEmoji.recentEmojis.map { EmojiItem(it, variantEmoji) }
        if (emojiKeyboardView.currentItem > RECENT_EMOJIS_POSITION) {
            adapter.notifyItemChanged(recentEmojisPageItem)
        } else {
            postNotifyRecentEmojis = true
        }
    }

    private fun EditText.input(emoji: Emoji) {
        input(this, emoji)
    }

    private fun EditText.backspace() {
        backspace(this)
    }

    companion object {
        private const val KEY_KEYBOARD_HEIGHT = "emoji_keyboard_height"

        private const val DEFAULT_KEYBOARD_PART = 0.39f
        private const val ANIMATION_DURATION = 200L

        private const val RECENT_EMOJIS_POSITION = 0
    }
}
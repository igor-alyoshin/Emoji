package io.wola.app

import android.animation.Animator
import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.res.Resources
import android.widget.EditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.vanniktech.emoji.*
import com.vanniktech.emoji.Utils.backspace
import com.vanniktech.emoji.Utils.input
import com.vanniktech.emoji.emoji.Emoji
import com.vanniktech.emoji.emoji.EmojiCategory
import com.wola.android.emoji.R
import timber.log.Timber

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

    private val expanded
        get() = (emojiKeyboardView?.measuredHeight ?: 0) > 0 && hideAnimator == null
    private val emojiWidth =
        activity.resources.getDimensionPixelSize(R.dimen.emoji_grid_view_column_width)

    private var editText: EditText? = null
    private var emojiKeyboardView: EmojiKeyboardView? = null

    private var hideAnimator: Animator? = null
    private var showAnimator: Animator? = null
    private var keyboardHeight = restoreKeyboardHeight()
    private var mode: KeyboardMode = KeyboardMode.Hidden
    private var postNotifyRecentEmojis = false

    var onModeChanged: ((KeyboardMode) -> Unit)? = null
    var onAnimationComplete: (() -> Unit)? = null

    sealed class KeyboardMode {
        object Hidden : KeyboardMode()
        object Soft : KeyboardMode()
        object Emoji : KeyboardMode()
    }

    fun setupWith(editText: EditText, emojiKeyboardView: EmojiKeyboardView) {
        this.editText = editText
        this.emojiKeyboardView = emojiKeyboardView
        init(editText, emojiKeyboardView)
    }

    fun init(editText: EditText, emojiKeyboardView: EmojiKeyboardView) {
        emojiKeyboardView.maxHeight = 0
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

        activity.window?.let { window ->
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
                val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                val navigationBottom =
                    insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                val height = imeBottom - navigationBottom
                if (imeBottom == 0 && mode == KeyboardMode.Soft) {
                    setMode(KeyboardMode.Hidden)
                } else if (height > 0) {
                    keyboardHeight = height
                    saveKeyboardHeight(height)
                    emojiKeyboardView.setupSizes(height)
                    expand()
                    setMode(KeyboardMode.Soft)
                }
                val newInsets = if (imeBottom > 0) {
                    insets.removeGlobalBottomInsets(keyboardHeight)
                } else {
                    insets
                }
                ViewCompat.onApplyWindowInsets(v, newInsets)
            }
        } ?: Timber.w("Window not found")
    }

    fun release() {
        activity.window?.let { window ->
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView, null)
        }
        editText = null
        emojiKeyboardView = null
        onModeChanged = null
        handlePopupClosed()
    }

    fun toggle() {
        if (expanded) {
            if (mode == KeyboardMode.Soft) {
                setMode(KeyboardMode.Emoji)
            } else {
                setMode(KeyboardMode.Soft)
            }
        } else {
            setMode(KeyboardMode.Emoji)
        }
    }

    fun handleBackPressed(): Boolean {
        if (mode == KeyboardMode.Emoji) {
            setMode(KeyboardMode.Hidden)
            return true
        }
        return false
    }

    fun expand() = emojiKeyboardView?.let { view ->
        if (showAnimator == null) {
            hideAnimator?.cancel()
            hideAnimator = null
            val duration = if (keyboardHeight > view.measuredHeight) {
                ANIMATION_DURATION * (keyboardHeight - view.measuredHeight) / keyboardHeight
            } else {
                ANIMATION_DURATION * view.measuredHeight / keyboardHeight
            }
            showAnimator = view.animateHeight(keyboardHeight, duration, onEnd = {
                showAnimator = null
                editText?.requestFocus()
                if (mode == KeyboardMode.Emoji) {
                    view.postInLifecycle {
                        view.increasePageLimitIfNeed()
                    }
                }
                onAnimationComplete?.invoke()
            })
        }
    }

    fun collapse() = emojiKeyboardView?.let { view ->
        if (hideAnimator == null) {
            showAnimator?.cancel()
            showAnimator = null
            val duration = ANIMATION_DURATION * view.measuredHeight / keyboardHeight
            hideAnimator = view.animateHeight(0, duration, onEnd = {
                hideAnimator = null
                handlePopupClosed()
                onAnimationComplete?.invoke()
            })
        }
    }

    private fun handlePopupClosed() {
        recentEmoji.persist()
        variantEmoji.persist()
        variantPopup?.dismiss()
    }

    private fun setMode(newMode: KeyboardMode) {
        val oldMode = mode
        if (mode != newMode) {
            mode = newMode
            onModeChanged?.invoke(newMode)
        }
        when (mode) {
            KeyboardMode.Emoji -> {
                emojiKeyboardView?.alpha = 1f
                editText?.let {
                    activity.hideKeyboard(it)
                }
                expand()
            }
            KeyboardMode.Soft -> {
                emojiKeyboardView?.alpha = 1f
                editText?.let { activity.showKeyboard(it) }
            }
            KeyboardMode.Hidden -> {
                emojiKeyboardView?.alpha = if (oldMode == KeyboardMode.Soft) 0f else 1f
                if (expanded) collapse()
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
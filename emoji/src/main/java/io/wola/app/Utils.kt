package io.wola.app

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.IItemVHFactory
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.OnCreateViewHolderListener
import kotlinx.coroutines.*
import kotlin.math.max

fun WindowInsetsCompat.getGlobalInsets() = getInsets(globalMask)

fun WindowInsetsCompat.getImeInsets() = getInsets(WindowInsetsCompat.Type.ime())

fun View.postDelayedInLifecycle(
    durationMillis: Long,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    block: () -> Unit
): Job? = findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
    lifecycleOwner.lifecycle.coroutineScope.launch(dispatcher) {
        delay(durationMillis)
        block()
    }
}

fun FastAdapter<*>.notifyItemChanged(item: GenericItem) {
    val index = getPosition(item.identifier)
    if (index >= 0) notifyItemChanged(index)
}

fun View.postInLifecycle(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    block: () -> Unit
): Job? = findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
    lifecycleOwner.lifecycle.coroutineScope.launch(dispatcher) {
        block()
    }
}

inline fun <reified Holder> FastAdapter<GenericItem>.addOnCreateViewHolder(
    noinline action: (Holder.() -> Unit)?
) {
    val oldOnCreateViewHolderListener = onCreateViewHolderListener
    onCreateViewHolderListener = object : OnCreateViewHolderListener<GenericItem> {
        override fun onPostCreateViewHolder(
            fastAdapter: FastAdapter<GenericItem>,
            viewHolder: RecyclerView.ViewHolder,
            itemVHFactory: IItemVHFactory<*>
        ): RecyclerView.ViewHolder {
            val holder = oldOnCreateViewHolderListener.onPostCreateViewHolder(
                fastAdapter,
                viewHolder,
                itemVHFactory
            )
            if (holder is Holder) action?.invoke(holder)
            return holder
        }

        override fun onPreCreateViewHolder(
            fastAdapter: FastAdapter<GenericItem>,
            parent: ViewGroup,
            viewType: Int,
            itemVHFactory: IItemVHFactory<*>
        ): RecyclerView.ViewHolder {
            return oldOnCreateViewHolderListener.onPreCreateViewHolder(
                fastAdapter,
                parent,
                viewType,
                itemVHFactory
            )
        }
    }
}

fun <T : GenericItem> createFastAdapter(vararg adapters: ItemAdapter<T>): FastAdapter<T> {
    return FastAdapter.with(adapters.toList())
}

fun <T : GenericItem> createFastAdapter(items: List<T> = emptyList()): FastAdapter<T> {
    val itemAdapter = ItemAdapter<T>().apply { add(items) }
    return FastAdapter.with(itemAdapter)
}

fun <T : GenericItem> ItemAdapter<T>.setWithDiffUtil(items: List<T>) {
    val diffResult = FastAdapterDiffUtil.calculateDiff(this, items)
    FastAdapterDiffUtil[this] = diffResult
}

val ViewBinding.context get() = root.context

fun ConstraintLayout.animateHeight(
    targetHeight: Int,
    duration: Long,
    onStart: (() -> Unit) = {},
    onEnd: (() -> Unit) = {}
): Animator? {
    val currentHeight = measuredHeight
    if (measuredHeight == targetHeight) {
        updateLayoutParams { height = targetHeight }
        onEnd.invoke()
        return null
    }
    val animator = ValueAnimator.ofInt(currentHeight, targetHeight)
    animator.addUpdateListener {
        val v = it.animatedValue as Int
        updateLayoutParams { height = v }
        maxHeight = v
    }
    animator.doOnStart {
        onStart.invoke()
    }
    animator.doOnEnd {
        onEnd.invoke()
    }
    animator.duration = duration
    animator.start()
    return animator
}

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Context.showKeyboard(view: View, cursorToEnd: Boolean = true) {
    view.requestFocus()
    val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, 0)
    if (view is EditText && cursorToEnd) view.setSelection(view.text?.length ?: 0)
}

private val globalMask = WindowInsetsCompat.Type.ime() or WindowInsetsCompat.Type.systemBars() or
        WindowInsetsCompat.Type.systemGestures()
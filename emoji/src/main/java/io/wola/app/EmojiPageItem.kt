package io.wola.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.binding.BindingViewHolder
import com.vanniktech.emoji.EmojiImageView
import com.wola.android.emoji.R
import com.wola.android.emoji.databinding.EmojiPageItemBinding

data class EmojiPageItem(
    val tabIconResId: Int,
    var emojis: List<EmojiItem>
) : AbstractBindingItem<EmojiPageItemBinding>() {

    override val type = R.id.emoji_fast_adapter_page_item

    override var identifier = tabIconResId.toLong()

    override fun bindView(holder: BindingViewHolder<EmojiPageItemBinding>, payloads: List<Any>) {
        super.bindView(holder, payloads)
        if (holder is ViewHolder) holder.setEmojis(emojis)
    }

    override fun bindView(binding: EmojiPageItemBinding, payloads: List<Any>) =
        binding.run {
        }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?) =
        EmojiPageItemBinding.inflate(inflater, parent, false)

    override fun getViewHolder(viewBinding: EmojiPageItemBinding) = ViewHolder(viewBinding)

    class ViewHolder(binding: EmojiPageItemBinding) :
        BindingViewHolder<EmojiPageItemBinding>(binding) {

        private val emojisAdapter = ItemAdapter<GenericItem>()

        fun setup(
            numOfColumns: Int,
            onEmojiClick: (EmojiItem) -> Unit,
            onEmojiLongClick: (EmojiImageView, EmojiItem) -> Unit
        ) = binding.run {
            rvEmojis.isFocusableInTouchMode = false
            rvEmojis.isFocusable = false
            rvEmojis.layoutManager =
                GridLayoutManager(context, numOfColumns, GridLayoutManager.VERTICAL, false)
            rvEmojis.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            rvEmojis.addItemDecoration(SpacingDecoration(context, R.dimen.emoji_spacing))
            rvEmojis.adapter = createFastAdapter(emojisAdapter).apply {
                addOnCreateViewHolder<EmojiItem.ViewHolder> {
                    setup(emojisAdapter, onEmojiClick, onEmojiLongClick)
                }
            }
        }

        fun setEmojis(emojis: List<EmojiItem>) {
            emojisAdapter.setWithDiffUtil(emojis)
        }
    }
}
package io.wola.app

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.binding.BindingViewHolder
import com.vanniktech.emoji.EmojiImageView
import com.vanniktech.emoji.VariantEmoji
import com.vanniktech.emoji.emoji.Emoji
import com.wola.android.emoji.R
import com.wola.android.emoji.databinding.EmojiItemBinding

data class EmojiItem(val emoji: Emoji, val variantEmoji: VariantEmoji) : AbstractBindingItem<EmojiItemBinding>() {

    override val type = R.id.emoji_fast_adapter_item

    override var identifier = emoji.unicode.hashCode().toLong()

    override fun bindView(binding: EmojiItemBinding, payloads: List<Any>) =
        binding.run {
            val finalEmoji = variantEmoji.getVariant(emoji)
            emojiImageView.contentDescription = finalEmoji.unicode
            emojiImageView.setEmoji(finalEmoji)
            emojiImageView.setImageDrawable(finalEmoji.getDrawable(context))
        }

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?) =
        EmojiItemBinding.inflate(inflater, parent, false)

    override fun getViewHolder(viewBinding: EmojiItemBinding) = ViewHolder(viewBinding)

    class ViewHolder(binding: EmojiItemBinding) :
        BindingViewHolder<EmojiItemBinding>(binding) {

        fun setup(
            adapter: ItemAdapter<GenericItem>,
            onEmojiClick: (EmojiItem) -> Unit,
            onEmojiLongClick: (EmojiImageView, EmojiItem) -> Unit
        ) = binding.run {
            emojiImageView.setOnClickListener {
                val item = adapter.getAdapterItem(bindingAdapterPosition) as EmojiItem
                onEmojiClick.invoke(item)
            }
            emojiImageView.setOnLongClickListener {
                val item = adapter.getAdapterItem(bindingAdapterPosition) as EmojiItem
                onEmojiLongClick.invoke(emojiImageView, item)
                true
            }
        }
    }
}
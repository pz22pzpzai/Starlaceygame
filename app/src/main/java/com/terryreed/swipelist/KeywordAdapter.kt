package com.terryreed.swipelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/** Adapter displaying keywords for a given category. */
class KeywordAdapter(
    keywords: List<String>,
    private val onKeywordLongClick: (Int) -> Unit
) : RecyclerView.Adapter<KeywordAdapter.ViewHolder>() {

    private val keywords: MutableList<String> = keywords.toMutableList()
    private var highlightedPosition: Int = RecyclerView.NO_POSITION

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.textKeyword)
        val defaultTextColor: Int = textView.currentTextColor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_keyword, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = keywords[position]
        holder.itemView.setOnLongClickListener {
            onKeywordLongClick(holder.bindingAdapterPosition)
            true
        }
        holder.itemView.setOnClickListener {
            if (highlightedPosition == holder.bindingAdapterPosition) {
                clearHighlight()
            }
        }
        if (position == highlightedPosition) {
            holder.textView.setBackgroundResource(R.drawable.rounded_light_blue)
            holder.textView.setTextColor(
                ContextCompat.getColor(holder.textView.context, R.color.white)
            )
        } else {
            holder.textView.setBackgroundResource(0)
            holder.textView.setTextColor(holder.defaultTextColor)
        }
    }

    override fun getItemCount(): Int = keywords.size

    fun addItems(newKeywords: List<String>) {
        if (newKeywords.isEmpty()) return
        val start = keywords.size
        keywords.addAll(newKeywords)
        notifyItemRangeInserted(start, newKeywords.size)
    }

    fun removeItem(position: Int) {
        keywords.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, keywords.size - position)
    }

    fun getItem(position: Int): String = keywords[position]

    fun findIndex(query: String): Int =
        keywords.indexOfFirst { it.contains(query, ignoreCase = true) }

    fun highlightKeywordAt(position: Int) {
        val prev = highlightedPosition
        highlightedPosition = position
        if (prev != RecyclerView.NO_POSITION) {
            notifyItemChanged(prev)
        }
        notifyItemChanged(position)
    }

    private fun clearHighlight() {
        val prev = highlightedPosition
        highlightedPosition = RecyclerView.NO_POSITION
        if (prev != RecyclerView.NO_POSITION) {
            notifyItemChanged(prev)
        }
    }
}

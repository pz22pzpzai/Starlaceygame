package com.terryreed.swipelist

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * Adapter displaying grocery categories allowing drag-and-drop reordering.
 */
class CategoryOrderAdapter(
    categories: List<String>,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit,
    private val onCategoryClick: (String) -> Unit,
    private val onCategoryLongClick: (Int) -> Unit
) : RecyclerView.Adapter<CategoryOrderAdapter.ViewHolder>() {

    /**
     * Copy of the category list used purely for display purposes. Mutating this
     * list should not directly alter the underlying repository state; callers
     * are responsible for updating persistence as needed.
     */
    private val categories: MutableList<String> = categories.toMutableList()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numberView: TextView = itemView.findViewById(R.id.textCategoryNumber)
        val textView: TextView = itemView.findViewById(R.id.textCategory)
        val handleView: ImageView = itemView.findViewById(R.id.imageDragHandle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.numberView.text = "${position + 1}."
        holder.textView.text = categories[position]
        holder.itemView.setOnClickListener {
            onCategoryClick(categories[holder.bindingAdapterPosition])
        }
        holder.itemView.setOnLongClickListener {
            onCategoryLongClick(holder.bindingAdapterPosition)
            true
        }
        holder.handleView.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                onStartDrag(holder)
                // Consume the touch event so it isn't interpreted as a click or long
                // press on the underlying list item. This avoids accidental
                // selections while attempting to drag.
                true
            } else {
                false
            }
        }
    }

    override fun getItemCount(): Int = categories.size

    /** Replaces the displayed categories with [items] and refreshes the view. */
    fun updateItems(items: List<String>) {
        categories.clear()
        categories.addAll(items)
        notifyDataSetChanged()
    }

    fun getItem(position: Int): String = categories[position]

    fun moveItem(from: Int, to: Int) {
        val item = categories.removeAt(from)
        categories.add(to, item)
        notifyItemMoved(from, to)
        val start = minOf(from, to)
        val count = abs(from - to) + 1
        notifyItemRangeChanged(start, count)
    }

    fun addItem(name: String) {
        categories.add(name)
        notifyItemInserted(categories.size - 1)
    }

    fun removeItem(position: Int) {
        categories.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, categories.size - position)
    }
}


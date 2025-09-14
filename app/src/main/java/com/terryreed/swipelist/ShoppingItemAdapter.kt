package com.terryreed.swipelist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** Adapter for displaying shopping items. */
class ShoppingItemAdapter(
    initial: MutableList<ShoppingItem> = mutableListOf(),
    private val onItemLongClick: ((ShoppingItem) -> Unit)? = null
) :
    RecyclerView.Adapter<ShoppingItemAdapter.VH>() {

    private val items = initial

    fun addItem(item: ShoppingItem) {
        items.add(0, item)
        notifyItemInserted(0)
    }

    fun addItems(newItems: List<ShoppingItem>) {
        items.addAll(0, newItems)
        notifyItemRangeInserted(0, newItems.size)
    }

    /**
     * Adds [newItems] to the current list, then sorts all items according to the
     * provided [order] of [GroceryCategorizer.Category]s so that items are grouped
     * by category importance.
     */
    fun addItemsSorted(
        newItems: List<ShoppingItem>,
        order: List<GroceryCategorizer.Category>
    ) {
        items.addAll(newItems)
        val sorted = GroceryCategorizer.sortByCategory(items, order)
        items.clear()
        items.addAll(sorted)
        notifyDataSetChanged()
    }

    fun clearItems() {
        val size = items.size
        items.clear()
        notifyItemRangeRemoved(0, size)
    }

    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_item, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.title.text = item.name
        holder.subtitle.text = item.note
        holder.subtitle.visibility = if (item.note.isNotBlank()) View.VISIBLE else View.GONE
        holder.itemView.setOnLongClickListener {
            val currentItem = items[holder.bindingAdapterPosition]
            onItemLongClick?.invoke(currentItem)
            true
        }
    }

    override fun getItemCount(): Int = items.size

    fun getItems(): List<ShoppingItem> = items.toList()

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.txtTitle)
        val subtitle: TextView = itemView.findViewById(R.id.txtSubtitle)
    }
}

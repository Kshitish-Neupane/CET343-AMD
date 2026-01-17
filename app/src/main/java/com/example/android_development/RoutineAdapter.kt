package com.example.android_development

import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RoutineAdapter(
    private val items: MutableList<RoutineItem>,
    private val onEdit: (RoutineItem) -> Unit
) : RecyclerView.Adapter<RoutineAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val cb: CheckBox = v.findViewById(R.id.cbItem)
        val title: TextView = v.findViewById(R.id.tvWorkout)
        val equip: TextView = v.findViewById(R.id.tvEquipment)
        val reps: TextView = v.findViewById(R.id.tvReps)
        val sets: TextView = v.findViewById(R.id.tvSets)
        val image: ImageView = v.findViewById(R.id.ivImage)
        val edit: ImageView = v.findViewById(R.id.ivEdit)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_routine, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(h: VH, pos: Int) {
        val item = items[pos]

        // CHECKBOX
        h.cb.setOnCheckedChangeListener(null)
        h.cb.isChecked = item.checked
        h.cb.setOnCheckedChangeListener { _, checked ->
            item.checked = checked
        }

        // TEXT (MATCHES WEEKLY PLANNER FORMAT)
        h.title.text = item.title
        h.equip.text = "Equipment: ${item.equipment.ifBlank { "-" }}"
        h.reps.text = "Reps: ${item.reps.ifBlank { "-" }}"
        h.sets.text = "Sets: ${item.sets.ifBlank { "-" }}"

        // IMAGE
        if (!item.imageUri.isNullOrBlank()) {
            h.image.visibility = View.VISIBLE
            h.image.setImageURI(Uri.parse(item.imageUri))
        } else {
            h.image.visibility = View.GONE
        }

        // COMPLETED STATE (ORANGE)
        if (item.isCompleted) {
            h.title.setTextColor(Color.parseColor("#FF9800"))
        } else {
            h.title.setTextColor(Color.BLACK)
        }

        // EDIT
        h.edit.setOnClickListener { onEdit(item) }
    }

    // -------- PUBLIC HELPERS --------

    fun getItem(position: Int): RoutineItem = items[position]

    fun getSelected(): List<RoutineItem> =
        items.filter { it.checked }

    fun markComplete(position: Int) {
        items[position].isCompleted = true
        notifyItemChanged(position)
    }

    fun deleteAt(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    fun resetAll() {
        items.forEach {
            it.checked = false
            it.isCompleted = false
        }
        notifyDataSetChanged()
    }

    fun setItems(newItems: List<RoutineItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun addItem(item: RoutineItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun deleteSelected() {
        items.removeAll { it.checked }
        notifyDataSetChanged()
    }

    fun setRecommended(checked: Boolean) {
        items.forEach {
            if (it.recommended) it.checked = checked
        }
        notifyDataSetChanged()
    }
}

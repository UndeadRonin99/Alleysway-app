package com.example.alleysway

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

class SetAdapter(
    private val sets: MutableList<SetData>,
    private val onUpdateTotalWeight: () -> Unit
) : RecyclerView.Adapter<SetAdapter.SetViewHolder>() {

    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val setNumber: TextView = itemView.findViewById(R.id.setNumber)
        val repsEditText: TextInputEditText = itemView.findViewById(R.id.repsEditText)
        val weightEditText: TextInputEditText = itemView.findViewById(R.id.weightEditText)
        val deleteSetButton: ImageButton = itemView.findViewById(R.id.deleteSetButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.set_item, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val setData = sets[position]

        // Update set number display
        holder.setNumber.text = "Set ${position + 1}"

        // Set existing values for reps and weight
        holder.repsEditText.setText(setData.reps.toString())
        holder.weightEditText.setText(setData.weight.toString())

        // Update reps and weight when focus is lost
        holder.repsEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val repsText = holder.repsEditText.text.toString()
                setData.reps = repsText.toIntOrNull() ?: 0
                onUpdateTotalWeight()
            }
        }

        holder.weightEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val weightText = holder.weightEditText.text.toString()
                setData.weight = weightText.toDoubleOrNull() ?: 0.0
                onUpdateTotalWeight()
            }
        }

        // Delete set button logic
        holder.deleteSetButton.setOnClickListener {
            // Remove the set from list and notify adapter
            sets.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, sets.size) // Update the remaining item positions
            onUpdateTotalWeight() // Recalculate total weight
        }
    }

    override fun getItemCount(): Int = sets.size
}

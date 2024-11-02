package com.techtitans.alleysway

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

class SetAdapter(
    private val sets: MutableList<SetData>,
    private val onUpdateTotalWeight: (() -> Unit)? = null,
    private val isLogging: Boolean = true
) : RecyclerView.Adapter<SetAdapter.SetViewHolder>() {

    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Common Views
        val setNumber: TextView = itemView.findViewById(R.id.setNumber)

        // Logging mode Views
        val repsEditText: TextInputEditText? = itemView.findViewById(R.id.repsEditText)
        val weightEditText: TextInputEditText? = itemView.findViewById(R.id.weightEditText)
        val deleteSetButton: ImageButton? = itemView.findViewById(R.id.deleteSetButton)

        // Read-only mode Views
        val repsTextView: TextView? = itemView.findViewById(R.id.repsTextView)
        val weightTextView: TextView? = itemView.findViewById(R.id.weightTextView)

        private var repsTextWatcher: TextWatcher? = null
        private var weightTextWatcher: TextWatcher? = null

        fun bind(setData: SetData, position: Int) {
            setNumber.text = "Set ${position + 1}"

            if (isLogging) {
                // Remove existing TextWatchers to prevent multiple triggers
                repsEditText?.removeTextChangedListener(repsTextWatcher)
                weightEditText?.removeTextChangedListener(weightTextWatcher)

                // Set the text to current values
                repsEditText?.setText(setData.reps.takeIf { it != 0 }?.toString() ?: "")
                weightEditText?.setText(setData.weight.takeIf { it != 0.0 }?.toString() ?: "")

                // Initialize TextWatchers
                repsTextWatcher = object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        val reps = s?.toString()?.toIntOrNull() ?: 0
                        setData.reps = reps
                        onUpdateTotalWeight?.invoke()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                }

                weightTextWatcher = object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        val weight = s?.toString()?.toDoubleOrNull() ?: 0.0
                        setData.weight = weight
                        onUpdateTotalWeight?.invoke()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                }

                // Add TextWatchers
                repsEditText?.addTextChangedListener(repsTextWatcher)
                weightEditText?.addTextChangedListener(weightTextWatcher)

                // Handle delete set
                deleteSetButton?.setOnClickListener {
                    val currentPosition = adapterPosition
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        sets.removeAt(currentPosition)
                        notifyItemRemoved(currentPosition)
                        notifyItemRangeChanged(currentPosition, sets.size)
                        onUpdateTotalWeight?.invoke()
                    }
                }
            } else {
                // Read-only mode: Display the reps and weight
                repsTextView?.text = "Reps: ${setData.reps}"
                weightTextView?.text = "Weight: ${setData.weight} kg"

                // Hide the delete button in read-only mode
                deleteSetButton?.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        val layoutId = if (isLogging) R.layout.set_item else R.layout.item_set_read_only
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return SetViewHolder(view)
    }

    override fun onBindViewHolder(holder: SetViewHolder, position: Int) {
        val setData = sets[position]
        holder.bind(setData, position)
    }

    override fun getItemCount(): Int = sets.size
}

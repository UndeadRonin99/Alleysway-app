// Package declaration
package com.techtitans.alleysway

// Import statements
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

/**
 * RecyclerView Adapter for displaying and managing workout sets.
 *
 * @param sets List of SetData representing each set.
 * @param onUpdateTotalWeight Callback to update total weight when sets change.
 * @param isLogging Flag to determine if the adapter is in logging mode.
 */
class SetAdapter(
    private val sets: MutableList<SetData>,
    private val onUpdateTotalWeight: (() -> Unit)? = null,
    private val isLogging: Boolean = true
) : RecyclerView.Adapter<SetAdapter.SetViewHolder>() {

    /**
     * ViewHolder class for individual set items.
     */
    inner class SetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Common view
        val setNumber: TextView = itemView.findViewById(R.id.setNumber)

        // Logging mode views
        val repsEditText: TextInputEditText? = itemView.findViewById(R.id.repsEditText)
        val weightEditText: TextInputEditText? = itemView.findViewById(R.id.weightEditText)
        val deleteSetButton: ImageButton? = itemView.findViewById(R.id.deleteSetButton)

        // Read-only mode views
        val repsTextView: TextView? = itemView.findViewById(R.id.repsTextView)
        val weightTextView: TextView? = itemView.findViewById(R.id.weightTextView)

        // TextWatchers to monitor changes
        private var repsTextWatcher: TextWatcher? = null
        private var weightTextWatcher: TextWatcher? = null

        /**
         * Binds the SetData to the views based on the mode.
         *
         * @param setData Data for the current set.
         * @param position Position of the set in the list.
         */
        fun bind(setData: SetData, position: Int) {
            setNumber.text = "Set ${position + 1}"

            if (isLogging) {
                // Remove existing TextWatchers to prevent multiple triggers
                repsEditText?.removeTextChangedListener(repsTextWatcher)
                weightEditText?.removeTextChangedListener(weightTextWatcher)

                // Set current values
                repsEditText?.setText(if (setData.reps != 0) setData.reps.toString() else "")
                weightEditText?.setText(if (setData.weight != 0.0) setData.weight.toString() else "")

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

                // Handle delete set action
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
                // Display reps and weight in read-only mode
                repsTextView?.text = "Reps: ${setData.reps}"
                weightTextView?.text = "Weight: ${setData.weight} kg"
                // Hide delete button
                deleteSetButton?.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SetViewHolder {
        // Choose layout based on mode
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

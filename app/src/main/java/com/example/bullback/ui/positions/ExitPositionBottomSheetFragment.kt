package com.example.bullback.ui.positions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.example.bullback.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ExitPositionBottomSheetFragment(
    private val isExitAll: Boolean,
    private val onConfirm: () -> Unit
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_exit_position_bottom_sheet, container, false)

        val tvTitle = view.findViewById<TextView>(R.id.tvTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvMessage)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)

        if (isExitAll) {
            tvTitle.text = "Exit All Positions"
            tvMessage.text = "Are you sure want to square off all positions."
            btnConfirm.text = "Exit"
        } else {
            tvTitle.text = "Square off position"
            tvMessage.text = "Are you sure want to square off this positions."
            btnConfirm.text = "Square off"
        }

        btnCancel.setOnClickListener { dismiss() }
        btnConfirm.setOnClickListener {
            onConfirm()
            dismiss()
        }
        return view
    }
}

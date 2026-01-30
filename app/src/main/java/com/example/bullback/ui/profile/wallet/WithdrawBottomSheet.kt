package com.example.bullback.ui.profile.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.bullback.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class WithdrawBottomSheet : BottomSheetDialogFragment() {

    companion object {
        const val TAG = "WithdrawBottomSheet"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_withdraw_bottom_sheet, container, false)
    }
}

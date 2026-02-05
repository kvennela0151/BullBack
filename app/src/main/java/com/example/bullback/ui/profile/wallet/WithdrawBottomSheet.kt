package com.example.bullback.ui.profile.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.bullback.R
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.WalletApiService
import com.example.bullback.data.repository.WalletRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText

class WithdrawBottomSheet : BottomSheetDialogFragment() {

    private lateinit var viewModel: WalletViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = WalletRepository(RetrofitClient.createService(WalletApiService::class.java))
        viewModel = ViewModelProvider(requireActivity(), WalletViewModelFactory(repo))[WalletViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_withdraw_bottom_sheet, container, false)

        val amountEt = view.findViewById<TextInputEditText>(R.id.etAmount)
        val upiEt = view.findViewById<TextInputEditText>(R.id.etUpi)
        val nameEt = view.findViewById<TextInputEditText>(R.id.etName)
        val accEt = view.findViewById<TextInputEditText>(R.id.etAccNumber)
        val ifscEt = view.findViewById<TextInputEditText>(R.id.etIfsc)

        val submitBtn = view.findViewById<Button>(R.id.btnSubmit)
        val cancelBtn = view.findViewById<Button>(R.id.btnCancel)

        cancelBtn.setOnClickListener { dismiss() }

        submitBtn.setOnClickListener {
            viewModel.withdrawFunds(
                amount = amountEt.text.toString(),
                name = nameEt.text.toString(),
                acc = accEt.text.toString(),
                ifsc = ifscEt.text.toString(),
                upi = upiEt.text.toString().ifBlank { null }
            )
        }

        observeResult()

        return view
    }

    private fun observeResult() {
        viewModel.withdrawResult.observe(this) { result ->
            if (result == null) {
                Toast.makeText(requireContext(), "Withdrawal failed!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Withdrawal request submitted!", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }
    }
}


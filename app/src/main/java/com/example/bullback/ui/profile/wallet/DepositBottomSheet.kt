package com.example.bullback.ui.profile.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.bullback.data.remote.RetrofitClient
import com.example.bullback.data.remote.api.WalletApiService
import com.example.bullback.data.repository.WalletRepository
import com.example.bullback.databinding.FragmentDepositBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DepositBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentDepositBottomSheetBinding? = null
    private val binding get() = _binding!!

    // Provide factory to activityViewModels
    private val viewModel: WalletViewModel by activityViewModels {
        WalletViewModelFactory(
            WalletRepository(
                RetrofitClient.createService(WalletApiService::class.java)
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDepositBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadBankDetails()
        observeBankDetails()
        setupClicks()
    }

    private fun observeBankDetails() {
        viewModel.bankDetails.observe(viewLifecycleOwner) { details ->
            if (details != null) {
                // Display labels + nicely formatted amount
                binding.tvAccountName.text = "Account Name: ${details.accountName}"
                binding.tvAccountNumber.text = "Account Number: ${details.accountNumber}"
                binding.tvIfsc.text = "IFSC: ${details.ifscCode}"
                binding.tvUpiId.text = "UPI ID: ${details.upi}"

                // Format maximum UPI amount with commas and no decimal
                val maxUpi = String.format("%,d", details.amtAfterChangeUpi.toInt())
                binding.tvMaxUpiAmount.text = "Maximum UPI Amount: ₹$maxUpi"
            }
        }
    }

    private fun setupClicks() {
        binding.btnCancel.setOnClickListener { dismiss() }

        binding.btnSubmit.setOnClickListener {
            val amount = binding.etAmount.text.toString()
            if (amount.isBlank()) {
                Toast.makeText(requireContext(), "Enter amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(requireContext(), "Deposit request submitted", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

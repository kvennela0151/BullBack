package com.example.bullback.ui.profile.wallet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.bullback.databinding.FragmentDepositBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DepositBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentDepositBottomSheetBinding? = null
    private val binding get() = _binding!!

    //  Shared ViewModel with WalletFragment
    private val viewModel: WalletViewModel by activityViewModels()

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

        setupClicks()
    }



    private fun setupClicks() {

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            val amount = binding.etAmount.text.toString()

            if (amount.isBlank()) {
                Toast.makeText(requireContext(), "Enter amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Deposit API call later
            Toast.makeText(
                requireContext(),
                "Deposit request submitted",
                Toast.LENGTH_SHORT
            ).show()

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

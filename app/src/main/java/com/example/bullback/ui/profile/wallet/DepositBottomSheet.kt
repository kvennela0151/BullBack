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
import java.io.File


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns

import java.io.FileOutputStream

class DepositBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentDepositBottomSheetBinding? = null
    private val binding get() = _binding!!

    private var screenshotFile: File? = null
    private var selectedBankId: Int = 0

    // PICK FILE REQUEST
    private val PICK_IMAGE_REQUEST = 101

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
        observeDepositResult()
        setupClicks()
    }

    // ---------------------------------------------------------------------
    // BANK DETAILS
    // ---------------------------------------------------------------------
    private fun observeBankDetails() {
        viewModel.bankDetails.observe(viewLifecycleOwner) { details ->
            if (details != null) {

                selectedBankId = details.id  // IMPORTANT

                binding.tvAccountName.text = "Account Name: ${details.accountName}"
                binding.tvAccountNumber.text = "Account Number: ${details.accountNumber}"
                binding.tvIfsc.text = "IFSC: ${details.ifscCode}"
                binding.tvUpiId.text = "UPI ID: ${details.upi}"

                val maxUpi = String.format("%,d", details.amtAfterChangeUpi.toInt())
                binding.tvMaxUpiAmount.text = "Maximum UPI Amount: ₹$maxUpi"
            }
        }
    }

    // ---------------------------------------------------------------------
    // BUTTON CLICKS
    // ---------------------------------------------------------------------
    private fun setupClicks() {

        binding.btnCancel.setOnClickListener { dismiss() }

        // Upload screenshot click
        binding.layoutUpload.setOnClickListener {
            openFileChooser()
        }

        // SUBMIT
        binding.btnSubmit.setOnClickListener {
            val amount = binding.etAmount.text.toString().trim()

            if (amount.isEmpty()) {
                Toast.makeText(requireContext(), "Enter amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedBankId == 0) {
                Toast.makeText(requireContext(), "Bank account not loaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (screenshotFile == null) {
                Toast.makeText(requireContext(), "Please upload screenshot", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.depositFunds(amount, selectedBankId, screenshotFile)
        }
    }

    // ---------------------------------------------------------------------
    // OBSERVE DEPOSIT RESULT
    // ---------------------------------------------------------------------
    private fun observeDepositResult() {
        viewModel.depositResult.observe(viewLifecycleOwner) { response ->
            if (response?.status == true) {
                Toast.makeText(requireContext(), "Deposit submitted!", Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                Toast.makeText(requireContext(), "Deposit failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------------------------------------------------------------------
    // FILE PICKER
    // ---------------------------------------------------------------------
    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                screenshotFile = copyUriToFile(uri)
                binding.tvUploadHint.text = "Uploaded: ${screenshotFile?.name}"
            }
        }
    }

    // Convert URI → File
    private fun copyUriToFile(uri: Uri): File {
        val returnCursor = requireActivity().contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: 0
        returnCursor?.moveToFirst()
        val fileName = returnCursor?.getString(nameIndex) ?: "uploaded_file.jpg"
        returnCursor?.close()

        val file = File(requireContext().cacheDir, fileName)
        val inputStream = requireActivity().contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)

        inputStream?.close()
        outputStream.close()

        return file
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

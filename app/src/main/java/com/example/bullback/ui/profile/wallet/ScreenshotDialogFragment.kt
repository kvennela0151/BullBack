package com.example.bullback.ui.profile.wallet

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.bullback.databinding.FragmentScreenshotDialogBinding
import com.bumptech.glide.Glide

class ScreenshotDialogFragment : DialogFragment() {

    private var _binding: FragmentScreenshotDialogBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val ARG_URL = "url"

        fun newInstance(url: String?): ScreenshotDialogFragment {
            val dialog = ScreenshotDialogFragment()
            val bundle = Bundle()
            bundle.putString(ARG_URL, url)
            dialog.arguments = bundle
            return dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScreenshotDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString(ARG_URL)

        if (!url.isNullOrEmpty()) {
            Glide.with(this)
                .load(url)                // load remote URL // optional error image
                .into(binding.ivScreenshot)
        }

        // Close dialog on tap
        binding.ivScreenshot.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

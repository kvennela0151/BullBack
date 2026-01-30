package com.example.bullback.ui.profile.marginSettings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bullback.R
import com.example.bullback.databinding.FragmentMarginSettingsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MarginSettingsFragment : Fragment() {

    private var _binding: FragmentMarginSettingsBinding? = null
    private val binding get() = _binding!!

    private val marginViewModel: MarginViewModel by viewModels()
    private lateinit var adapter: MarginSettingsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarginSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupToolbar()

        // Load margin settings
        marginViewModel.loadMarginSettings()
    }

    private fun setupRecyclerView() {
        adapter = MarginSettingsAdapter()
        binding.rvMarginSettings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMarginSettings.adapter = adapter
    }

    private fun setupObservers() {
        lifecycleScope.launchWhenStarted {
            marginViewModel.marginSettings.collectLatest { list ->
                if (list.isNotEmpty()) {
                    adapter.updateList(list)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            marginViewModel.error.collectLatest { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupToolbar() {
        binding.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.where_am_i_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.where_am_i_app.adapter.UserAlertReportAdapter
import com.example.where_am_i_app.databinding.FragmentFeedBinding
import com.example.where_am_i_app.model.Model
import com.example.where_am_i_app.model.UserAlertReport

class FeedFragment : Fragment() {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding

    private val viewModel: UserAlertReportsViewModel by viewModels()
    private var adapter: UserAlertReportAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)

        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)
        binding?.progressBar?.visibility = View.VISIBLE

        adapter = UserAlertReportAdapter(null)
        adapter?.listener = object : OnUserAlertReportClickListener {
            override fun onEditClick(userAlertReport: UserAlertReport?) {
               Toast.makeText(requireContext(), "alert ${userAlertReport?.text}", Toast.LENGTH_SHORT).show()
            }

            override fun onDeleteClick(userAlertReport: UserAlertReport?) {
                viewModel.deleteUserAlertReport(userAlertReport) { success: Boolean ->
                    if (success) {
                        Toast.makeText(requireContext(), "Deleted successfully", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Delete failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding?.recyclerView?.adapter = adapter

        viewModel.userAlertReports.observe(viewLifecycleOwner) {
            adapter?.update(it)
            binding?.progressBar?.visibility = View.GONE
        }
        binding?.swipeToRefresh?.setOnRefreshListener {
            viewModel.refreshAllUserAlertReports()
        }
        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefresh?.isRefreshing = state == Model.LoadingState.LOADING
        }

        return _binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAllUserAlertReports()
    }
}
package com.example.where_am_i_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.where_am_i_app.adapter.AlertsAdapter
import com.example.where_am_i_app.databinding.FragmentAlertsBinding
import com.example.where_am_i_app.model.Model

class AlertsFragment : Fragment() {
    private var binding: FragmentAlertsBinding? = null

    private val viewModel: AlertsViewModel by viewModels()
    private var adapter: AlertsAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlertsBinding.inflate(inflater, container, false)

        binding?.recyclerView?.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        binding?.recyclerView?.layoutManager = layoutManager

        adapter = AlertsAdapter(viewModel.alerts.value)
        viewModel.alerts.observe(viewLifecycleOwner) {
            adapter?.notifyDataSetChanged()
            binding?.progressBar?.visibility = View.GONE
        }
        binding?.swipeToRefresh?.setOnRefreshListener {
            viewModel.refreshAlerts()
        }
        Model.shared.loadingState.observe(viewLifecycleOwner) { state ->
            binding?.swipeToRefresh?.isRefreshing = state == Model.LoadingState.LOADING
        }
        binding?.recyclerView?.adapter = adapter

        return binding?.root
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshAlerts()
    }
}
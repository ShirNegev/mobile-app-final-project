package com.example.where_am_i_app

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.where_am_i_app.adapter.AlertsAdapter
import com.example.where_am_i_app.databinding.FragmentAlertsBinding
import com.example.where_am_i_app.model.Alerts
import com.example.where_am_i_app.model.Model

class AlertsFragment : Fragment() {
    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding

    private val viewModel: AlertsViewModel by viewModels()
    private var adapter: AlertsAdapter? = AlertsAdapter(Alerts(emptyList()))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlertsBinding.inflate(inflater, container, false)

        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)

        binding?.recyclerView?.adapter = null
        binding?.recyclerView?.adapter = adapter

        viewModel.alerts.observe(viewLifecycleOwner) {
            Log.e("TAG", "adapter! ${adapter}")

            adapter?.update(it)
            adapter?.notifyDataSetChanged()
            binding?.progressBar?.visibility = View.GONE
        }
        binding?.swipeToRefresh?.setOnRefreshListener {
            viewModel.refreshAlerts()
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
        viewModel.refreshAlerts()
    }
}
package com.fossil.trackme.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fossil.trackme.R
import com.fossil.trackme.base.BaseActivity
import com.fossil.trackme.base.BasicSingleViewAdapter
import com.fossil.trackme.base.BindableAdapter
import com.fossil.trackme.base.RVClickListener
import com.fossil.trackme.data.models.TrackingSessionEntity
import com.fossil.trackme.data.viewmodel.HomeViewModel
import com.fossil.trackme.ui.adapters.TrackingSessionAdapter
import com.fossil.trackme.ui.viewholders.TrackingSessionViewHolder
import com.fossil.trackme.utils.UISchedule
import com.fossil.trackme.utils.getViewModel
import com.fossil.trackme.utils.observeOnce
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : BaseActivity() {
    private lateinit var viewModel: HomeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = getViewModel(HomeViewModel::class.java)
        setContentView(R.layout.activity_home)
        viewModel.getListTrackSessionEntity()
    }

    override fun initViews() {
        rvTrackingSession?.run {
            layoutManager = LinearLayoutManager(this@HomeActivity, RecyclerView.VERTICAL, false)
            adapter = TrackingSessionAdapter(
                R.layout.rv_item_session_layout,
                object : RVClickListener<TrackingSessionEntity> {
                    override fun onItemClick(pos: Int, data: TrackingSessionEntity?) {
                        super.onItemClick(pos, data)
                        //Handle click item
                    }
                })
        }

        btnRecord.setOnClickListener {
            startActivity(Intent(this,MapsActivity::class.java))
            finish()
        }
    }

    override fun observeData() {
        viewModel.listTrackingSession.observe(this, Observer {
            UISchedule.submitJob { pgListSession.visibility = View.GONE }
            it?.run {
                rvTrackingSession.post { (rvTrackingSession.adapter as TrackingSessionAdapter).let { rv ->
                    rv.setData(this)
                    rv.notifyDataSetChanged()
                }}
            }
        })
    }
}
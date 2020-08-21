package com.fossil.trackme.ui.viewholders

import android.view.View
import com.fossil.trackme.base.BaseViewHolder
import com.fossil.trackme.base.RVClickListener
import com.fossil.trackme.data.models.TrackingSessionEntity
import com.fossil.trackme.utils.UISchedule
import com.fossil.trackme.utils.secondToTimeString
import kotlinx.android.synthetic.main.rv_item_session_layout.view.*

class TrackingSessionViewHolder(itemview:View,clickListener: RVClickListener<TrackingSessionEntity>?): BaseViewHolder<TrackingSessionEntity>(itemview,clickListener) {
    override fun initViews() {
        itemView.setOnClickListener{
            itemClick()
        }
    }

    override fun bind() {
        data?.run {
            UISchedule.submitJob { bitMap?.run { itemView.ivTrackingSession.setImageBitmap(this) } }
            UISchedule.submitJob { itemView.tvDistanceSession.text = "${String.format("%.2f", (totalDistance / 1000))} Km" }
            UISchedule.submitJob { itemView.tvAvgSpeedSession.text = "${String.format("%.2f", (totalDistance / totalTime) * (18 / 5))} Km/h" }
            UISchedule.submitJob { itemView.tvTimeSession.text = secondToTimeString(totalTime) }
        }
    }
}
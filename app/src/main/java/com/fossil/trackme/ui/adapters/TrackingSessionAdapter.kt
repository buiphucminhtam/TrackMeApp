package com.fossil.trackme.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.fossil.trackme.base.BaseAdapter
import com.fossil.trackme.base.BaseViewHolder
import com.fossil.trackme.base.RVClickListener
import com.fossil.trackme.data.models.TrackingSessionEntity
import com.fossil.trackme.ui.viewholders.TrackingSessionViewHolder
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class TrackingSessionAdapter(
    private val layoutId: Int,
    private var clickListener: RVClickListener<TrackingSessionEntity>? = null
) : BaseAdapter<TrackingSessionEntity>(
    layoutId,
    TrackingSessionViewHolder::class,
    clickListener
) {
    /**
     * Auto make ViewHolder that extend [BaseViewHolder]
     * @return object of ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<TrackingSessionEntity> {
        return TrackingSessionViewHolder(
            LayoutInflater.from(parent.context).inflate(layoutId, parent, false), clickListener
        )
    }

    override fun onBindViewHolder(holder: BaseViewHolder<TrackingSessionEntity>, position: Int) {
        super.onBindViewHolder(holder, position)
    }

    override var diffCallback: DiffUtil.ItemCallback<TrackingSessionEntity> = object : DiffUtil.ItemCallback<TrackingSessionEntity>(){
        override fun areItemsTheSame(
            oldItem: TrackingSessionEntity,
            newItem: TrackingSessionEntity
        ): Boolean {
            return oldItem.id == newItem.id
        }


        override fun areContentsTheSame(
            oldItem: TrackingSessionEntity,
            newItem: TrackingSessionEntity
        ): Boolean {
            return oldItem == newItem
        }
    }
}
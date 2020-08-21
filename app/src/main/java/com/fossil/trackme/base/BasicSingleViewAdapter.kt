package com.fossil.trackme.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * Basic one type of itemConfigBox RecyclerView adapter
 */
open class BasicSingleViewAdapter<TYPE>(
        private val layoutId: Int,
        private val vhClass: KClass<out BaseViewHolder<TYPE>>,
        private var clickListener: RVClickListener<TYPE>? = null
) : BaseAdapter<TYPE>(layoutId,vhClass,clickListener) {
    /**
     * Auto make ViewHolder that extend [BaseViewHolder]
     * @return object of ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<TYPE> {
        val constructor = vhClass.primaryConstructor
        return constructor!!.call(LayoutInflater.from(parent.context).inflate(layoutId, parent, false),clickListener)
    }

    override var diffCallback: DiffUtil.ItemCallback<TYPE> = object : DiffUtil.ItemCallback<TYPE>(){
        override fun areItemsTheSame(oldItem: TYPE, newItem: TYPE): Boolean {
            return false
        }

        override fun areContentsTheSame(oldItem: TYPE, newItem: TYPE): Boolean {
            return false
        }
    }
}
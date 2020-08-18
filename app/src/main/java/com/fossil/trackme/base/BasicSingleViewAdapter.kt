package com.fossil.trackme.base

import android.view.LayoutInflater
import android.view.ViewGroup
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val constructor = vhClass.primaryConstructor
        return constructor!!.call(LayoutInflater.from(parent.context).inflate(layoutId, parent, false),clickListener)
    }
}
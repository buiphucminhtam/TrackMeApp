package com.fossil.trackme.data.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fossil.trackme.data.base.BaseAdapter
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * Basic one type of itemConfigBox RecyclerView adapter
 */
open class BasicSingleViewAdapter<TYPE>(
        private val layoutId: Int,
        private val vhClass: KClass<out BaseViewHolder<TYPE>>
) : BaseAdapter<TYPE>() {
    /**
     * Auto make ViewHolder that extend [BaseViewHolder]
     * @return object of ViewHolder
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val constructor = vhClass.primaryConstructor
        return constructor!!.call(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))
    }
}
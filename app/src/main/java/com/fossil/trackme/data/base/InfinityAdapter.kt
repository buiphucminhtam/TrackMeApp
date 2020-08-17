package com.fossil.trackme.data.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fossil.trackme.data.base.BaseAdapter
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class InfinityAdapter <TYPE>(
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

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is IViewHolder<*> && listData.isNotEmpty()) {
            (holder as IViewHolder<TYPE>).apply {
                val realPos = position%listData.size
                set(listData[realPos])
                bind()
            }
        }
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }
}
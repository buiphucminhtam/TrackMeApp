package com.fossil.trackme.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class ItemDecorationList(private val spaceSize: Int, private val orientation: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        parent.adapter?.let { adapter ->
            if (parent.getChildAdapterPosition(view) != (adapter.itemCount - 1)) {
                when (orientation) {
                    RecyclerView.HORIZONTAL -> outRect.right = spaceSize
                    RecyclerView.VERTICAL -> outRect.bottom = spaceSize
                }
            }
        }
    }
}
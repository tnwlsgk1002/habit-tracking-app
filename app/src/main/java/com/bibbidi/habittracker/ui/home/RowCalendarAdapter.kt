package com.bibbidi.habittracker.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bibbidi.habittracker.databinding.ItemRowCalendarBinding
import com.bibbidi.habittracker.ui.common.BaseViewHolder
import com.bibbidi.habittracker.ui.common.customview.DateView
import com.bibbidi.habittracker.ui.model.date.DateItem
import com.facebook.shimmer.ShimmerFrameLayout

class RowCalendarAdapter(private val onClick: (DateItem) -> (Unit)) :
    ListAdapter<Array<DateItem>, RowCalendarAdapter.DateItemViewHolder>(
        DateViewsDiffCallback
    ) {

    override fun onBindViewHolder(holder: DateItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateItemViewHolder {
        val binding =
            ItemRowCalendarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DateItemViewHolder(binding, onClick)
    }

    class DateItemViewHolder(
        binding: ItemRowCalendarBinding,
        private val onClick: (DateItem) -> Unit
    ) : BaseViewHolder<Array<DateItem>, ItemRowCalendarBinding>(binding) {

        private val dateViews: Array<DateView>
        private var clickedDateViewIndex: Int = 0
        private val shimmerLayout: ShimmerFrameLayout

        init {
            with(binding) {
                dateViews = arrayOf(
                    dateViewSun,
                    dateViewMon,
                    dateViewTue,
                    dateViewWed,
                    dateViewThu,
                    dateViewFri,
                    dateViewSat
                )

                shimmerLayout = shimmerFrameLayout.shimmerFrameLayout
            }
        }

        override fun bind(items: Array<DateItem>) {
            items.forEachIndexed { index, item ->
                dateViews[index].apply {
                    checked = item.checked
                    dayOfTheMonth = item.dayOfTheMonth
                    dayOfTheWeek = item.dayOfTheWeek
                    isToday = item.isToday

                    if (checked) {
                        clickedDateViewIndex = index
                    }

                    setOnClickListener {
                        if (!shimmerLayout.isVisible) {
                            dateViews[index].checked = true
                            dateViews[clickedDateViewIndex].checked = false
                            clickedDateViewIndex = index
                            onClick(item)
                        }
                    }
                }
            }
        }

        fun startShimmer() {
            dateViews.forEach { it.visibility = View.INVISIBLE }
            shimmerLayout.visibility = View.VISIBLE
            shimmerLayout.startShimmer()
        }

        fun stopShimmer() {
            dateViews.forEach { it.visibility = View.VISIBLE }
            shimmerLayout.visibility = View.INVISIBLE
            shimmerLayout.stopShimmer()
        }
    }

    object DateViewsDiffCallback : DiffUtil.ItemCallback<Array<DateItem>>() {
        override fun areItemsTheSame(oldItem: Array<DateItem>, newItem: Array<DateItem>): Boolean {
            val oldDateItems = oldItem.map { it.dayOfTheMonth }
            val newDateItems = newItem.map { it.dayOfTheMonth }
            return oldDateItems == newDateItems
        }

        override fun areContentsTheSame(
            oldItem: Array<DateItem>,
            newItem: Array<DateItem>
        ): Boolean {
            return oldItem.contentEquals(newItem)
        }
    }
}

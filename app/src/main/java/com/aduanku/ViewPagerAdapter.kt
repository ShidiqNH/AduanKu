package com.aduanku

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 4 // 4 fragments (Home, Notification, About, Profile)
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> AboutFragment()
            2 -> NotificationFragment()
            3 -> ProfileFragment()
            else -> throw IllegalStateException("Unexpected position $position")
        }
    }
}

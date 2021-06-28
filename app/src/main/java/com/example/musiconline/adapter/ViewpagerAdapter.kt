package com.example.musiconline.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.musiconline.ui.fragment.MusicDetailFragment
import com.example.musiconline.ui.fragment.RecommendFragment

class ViewpagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                MusicDetailFragment()
            }
            1 -> {
                RecommendFragment()
            }
            else -> {
                MusicDetailFragment()
            }
        }
    }

//    override fun getPageTitle(position: Int): CharSequence? {
//
//        when (position) {
//            0 -> {
//                return "Detail"
//            }
//            1 -> return "Recommend"
//        }
//        return super.getPageTitle(position)
//    }
}
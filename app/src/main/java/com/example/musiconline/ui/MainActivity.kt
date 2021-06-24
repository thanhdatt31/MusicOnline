package com.example.musiconline.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.musiconline.R
import com.example.musiconline.databinding.ActivityMainBinding
import com.example.musiconline.ui.fragment.FavoriteFragment
import com.example.musiconline.ui.fragment.HomeFragment
import com.example.musiconline.ui.fragment.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val bottomNavigation: BottomNavigationView = binding.navigationView
        bottomNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        val homeFragment = HomeFragment()
        openFragment(homeFragment)
    }

    private val mOnNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> {
                    val homeFragment = HomeFragment()
                    openFragment(homeFragment)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.favorite -> {
                    val searchFragment = SearchFragment()
                    openFragment(searchFragment)
                    return@OnNavigationItemSelectedListener true
                }

                R.id.search -> {
                    val favoriteFragment = FavoriteFragment()
                    openFragment(favoriteFragment)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    private fun openFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

}
package com.example.musiconline.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.musiconline.R
import com.example.musiconline.databinding.ActivityMainBinding
import com.example.musiconline.ui.fragment.FavoriteFragment
import com.example.musiconline.ui.fragment.HomeFragment
import com.example.musiconline.ui.fragment.OfflineMusicFragment
import com.example.musiconline.ui.fragment.SearchFragment
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.toolbar.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityMainBinding
    private var homeFragment = HomeFragment()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        displayScreen(0)
        supportFragmentManager.beginTransaction()
            .replace(R.id.relativelayout, homeFragment).commit()
        toolbar.title = "Music Online"
    }


    private fun displayScreen(i: Int) {
        when (i) {
            R.id.nav_home -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativelayout, HomeFragment()).commit()
                toolbar.title = "Music Online"
            }
            R.id.music_offline -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativelayout, OfflineMusicFragment()).commit()
                toolbar.title = "Music Offline"
            }
            R.id.favorite -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativelayout, FavoriteFragment()).commit()
                toolbar.title = "Music Favorite"
            }
            R.id.nav_aboutUs -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativelayout, SearchFragment()).commit()
                toolbar.title = "Search"
            }

            R.id.nav_rateMe -> {
                Toast.makeText(this, "Rate Me 5 Star", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        displayScreen(item.itemId)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


}
package com.example.musiconline.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
        requestPermission()
    }
    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        if (permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true && permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true) {
            initLayout()
        }
    }
    private fun requestPermission() {
        val permissionRead = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE

        )
        val permissionWrite = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (permissionRead == PackageManager.PERMISSION_GRANTED && permissionWrite == PackageManager.PERMISSION_GRANTED) {
            initLayout()
        } else {
            requestPermission.launch(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        }
    }

    private fun initLayout() {
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        toolbar.post {
            val d = ResourcesCompat.getDrawable(resources, R.drawable.ic_menu_black_24dp, null)
            toolbar.navigationIcon = d
        }
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        displayScreen(0)
        supportFragmentManager.beginTransaction()
            .replace(R.id.relativelayout, homeFragment).commit()
    }


    private fun displayScreen(i: Int) {
        when (i) {
            R.id.nav_home -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativelayout, HomeFragment()).commit()
            }
            R.id.music_offline -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativelayout, OfflineMusicFragment()).commit()
            }
            R.id.favorite -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativelayout, FavoriteFragment()).commit()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativelayout, SearchFragment()).addToBackStack("").commit()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.relativelayout, HomeFragment()).commit()
            } else {
                super.onBackPressed()
            }

        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        displayScreen(item.itemId)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


}
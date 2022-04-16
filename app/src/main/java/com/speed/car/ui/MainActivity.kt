package com.speed.car.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.speed.car.R
import com.speed.car.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val navController by lazy {
        Navigation.findNavController(this, R.id.fragmentContainerView)
    }
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
//        setupDrawerLayout()
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, binding.drawerLayout)
    }

    private fun setupDrawerLayout() {
        binding.navigationView.setupWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(this, navController, binding.drawerLayout)
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
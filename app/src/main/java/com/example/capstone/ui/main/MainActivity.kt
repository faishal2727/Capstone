package com.example.capstone.ui.main


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle

import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.capstone.R
import com.example.capstone.databinding.ActivityMainBinding
import com.example.capstone.ui.upload_event.UploadActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigationView.background = null
        binding.bottomNavigationView.menu.getItem(2).isEnabled = false

        buttonAddAction()

        val navController = findNavController(R.id.fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.mapsFragment,
                R.id.profileFragment,
                R.id.newsFragment2
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigationView.setupWithNavController(navController)

    }

    private fun buttonAddAction() {
        binding.add.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
}
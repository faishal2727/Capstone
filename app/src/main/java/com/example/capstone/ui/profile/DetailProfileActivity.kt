package com.example.capstone.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.capstone.data.Result
import com.example.capstone.databinding.ActivityDetailProfileBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.ui.edit_profille.EditProfilleActivity

class DetailProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailProfileBinding
    private lateinit var viewModelFactory: ViewModelFactory
    private val detailProfileViewModel: DetailProfileViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()
        setViewModel()
        getDataUser()
        toEditProfille()
        back()
    }

    private fun setActionBar() {
        supportActionBar?.hide()
    }

    private fun back() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setViewModel() {
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }

    private fun toEditProfille() {
        binding.tvEdit.setOnClickListener {
            startActivity(Intent(this, EditProfilleActivity::class.java))
        }
    }

    private fun getDataUser() {
        detailProfileViewModel.getDataUser.observe(this) {
            binding.apply {
                when (it) {
                    is Result.Loading -> {
                        showLoading(true)
                    }
                    is Result.Error -> {
                        showLoading(false)
                    }
                    is Result.Success -> {
                        Glide.with(binding.ivProfile)
                            .load(it.data.data.image_profile)
                            .circleCrop()
                            .into(ivProfile)
                        tvDetailName.text = it.data.data.name
                        tvDetailEmail.text = it.data.data.email
                        tvDetailAddress.text = it.data.data.address
                        tvDetailPhone.text = it.data.data.phone
                        showLoading(false)
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarProfille.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
package com.example.capstone.ui.auth.sign_up

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.data.Result
import com.example.capstone.databinding.ActivitySignupBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.ui.auth.login.LoginActivity

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var viewModelFactory: ViewModelFactory
    private val signUpViewModel: SignUpViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setViewModel()
        buttonRegister()
        setUi()

        binding.tvMasukDisini.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun setViewModel() {
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }

    private fun setUi() {
        supportActionBar?.hide()
    }

    private fun buttonRegister() {
        binding.btnSignUp.setOnClickListener {
            val name = binding.edtUsernameSignUp.text.toString().trim()
            val email = binding.edtEmailSignUp.text.toString().trim()
            val password = binding.edtPasswordSignUp.text.toString().trim()

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                processSignUp(name, email, password)
            } else {
                Toast.makeText(this, "Lengkapi Form", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun formSignUp() {
        binding.edtUsernameSignUp.text?.clear()
        binding.edtEmailSignUp.text?.clear()
        binding.edtPasswordSignUp.text?.clear()
    }

    private fun processSignUp(name: String, email: String, password: String) {
        signUpViewModel.register(name, email, password).observe(this) {
            if (it != null) {
                when (it) {
                    is Result.Loading -> {
                        showLoading(true)
                    }
                    is Result.Error -> {
                        showLoading(false)
                        Toast.makeText(this, "Ada yang tidak beres", Toast.LENGTH_SHORT).show()
                    }
                    is Result.Success -> {
                        formSignUp()
                        startActivity(Intent(this, LoginActivity::class.java))
                        Toast.makeText(this, "Berhasil Daftar Akun", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarSigUp.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}
package com.example.capstone.ui.auth.login


import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.capstone.R
import com.example.capstone.data.Result
import com.example.capstone.databinding.ActivityLoginBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.model.login_model.LoginResultModel
import com.example.capstone.model.login_model.ResponseLogin
import com.example.capstone.preference.PreferenceLogin
import com.example.capstone.ui.auth.sign_up.SignUpActivity
import com.example.capstone.ui.custom_view.MyAlertDialog
import com.example.capstone.ui.main.MainActivity

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModelFactory: ViewModelFactory
    private val loginViewModel: LoginViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setViewModel()
        buttonLogin()
        setActionBar()

        binding.tvDaftarDisini.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun setActionBar() {
        supportActionBar?.hide()
    }

    private fun showLoading(){
        binding.loading.visibility = View.VISIBLE
        binding.loading.playAnimation()
    }

    private fun stopLoading(){
        binding.loading.visibility = View.GONE
        binding.loading.cancelAnimation()
    }

    private fun setViewModel(){
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }

    private fun buttonLogin() {
        binding.btnLogin.setOnClickListener {
            val email = binding.edtUsernameLogIn.text.toString()
            val password = binding.edtPasswordLogin.text.toString()

            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
                processLogin(email, password)
            } else {
                Toast.makeText(this, "Lengkapi Form", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun processLogin(email: String, password:String){
        loginViewModel.authLogin(email, password).observe(this){ result->
            if (result != null){
                when(result){
                    is Result.Loading ->{
                        showLoading()
                    }
                    is Result.Error -> {
                        stopLoading()
                        Toast.makeText(this, "${result.error}", Toast.LENGTH_SHORT).show()
                        errorLogin()
                    }
                    is Result.Success -> {
                        stopLoading()
                        succesLogin(result.data)
                        Toast.makeText(
                            this,
                            "Selamat Datang ${result.data.data?.name}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun intentToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun succesLogin(responseLogin: ResponseLogin) {
        saveDataUser(responseLogin)
        intentToHome()
    }

    private fun errorLogin() {
        MyAlertDialog(this, R.string.error_message, R.drawable.ic_home)
    }

    private fun saveDataUser(responseLogin: ResponseLogin) {
        val preferenceLogin = PreferenceLogin(this)
        val resultLogin = responseLogin.data
        val loginResultModel = LoginResultModel(
            name = resultLogin?.name, id = resultLogin?.id.toString(), token = resultLogin?.token
        )
        preferenceLogin.setAuthLogin(loginResultModel)
    }
}
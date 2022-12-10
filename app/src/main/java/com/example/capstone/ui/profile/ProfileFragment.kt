package com.example.capstone.ui.profile

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.capstone.data.Result
import com.example.capstone.databinding.FragmentProfileBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.model.login_model.LoginResultModel
import com.example.capstone.preference.PreferenceLogin
import com.example.capstone.ui.list_join.ListJoinActivity
import com.example.capstone.ui.auth.login.LoginActivity
import com.example.capstone.ui.edit_profille.EditProfileAvatarActivity
import com.example.capstone.ui.list_post.ListPostActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModelFactory: ViewModelFactory
    private val detailProfileViewModel: DetailProfileViewModel by viewModels { viewModelFactory }


    private lateinit var preferenceLogin: PreferenceLogin
    private lateinit var loginResultModel: LoginResultModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentProfileBinding.inflate(inflater,container,false)
        val root: View = binding.root
        preferenceLogin = PreferenceLogin(root.context)
        loginResultModel = preferenceLogin.getUser()

        setViewModel()
        buttonLogout()
        setupUi()
        toDetailProfile()
        toListJoin()
        getDataUser()
        toEditAvatar()
        toListMyPost()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    private fun setupUi() {
        binding.tvUsername.text = loginResultModel.name
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        getDataUser()
    }

    private fun toListJoin(){
        binding.cardDaftarJoin.setOnClickListener {
            startActivity(Intent(activity, ListJoinActivity::class.java))
        }
    }

    private fun setViewModel(){
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }

    private fun buttonLogout(){
        binding.cardLogOut.setOnClickListener {
            val builder = context?.let { it1 -> AlertDialog.Builder(it1) }
            builder?.setTitle("Peringatan !!!")
            builder?.setMessage("Apakah Anda Yakin Ingin Keluar Aplikasi ?")
            builder?.setNegativeButton("Tidak") { _, _ ->
            }
            builder?.setPositiveButton("Iya") { _, _ ->
                preferenceLogin.deleteUser()
                startActivity(Intent(activity, LoginActivity::class.java))
            }
            val alert = builder?.create()
            alert?.show()

        }
    }


    private fun toEditAvatar(){
        binding.btnEditAvatar.setOnClickListener {
            startActivity(Intent(activity, EditProfileAvatarActivity::class.java))
        }
    }

    private fun getDataUser(){
        detailProfileViewModel.getDataUser.observe(viewLifecycleOwner){
            binding.apply {
                when(it) {
                    is Result.Loading -> {
                        showLoading(true)
                    }
                    is Result.Error ->{
                        showLoading(false)
                    }
                    is Result.Success -> {
                        Glide.with(binding.ivProfile)
                            .load(it.data.data.image_profile)
                            .circleCrop()
                            .into(ivProfile)
                        tvUsername.text = it.data.data.name
                        showLoading(false)
                    }
                }
            }
        }
    }

    private fun toDetailProfile(){
        binding.cardDetailProfille.setOnClickListener {
            startActivity(Intent(activity, DetailProfileActivity::class.java))
        }
    }

    private fun toListMyPost(){
        binding.cardDaftarPost.setOnClickListener {
            startActivity(Intent(activity,ListPostActivity::class.java))
        }
    }

    private fun showLoading(isLoading: Boolean){
        binding.progressBarProfile.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

}
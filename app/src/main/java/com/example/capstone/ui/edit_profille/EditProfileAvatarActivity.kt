package com.example.capstone.ui.edit_profille

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.example.capstone.R
import com.example.capstone.data.Result
import com.example.capstone.databinding.ActivityEditProfileAvatarBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.ui.custom_view.MyAlertDialog
import com.example.capstone.ui.main.MainActivity
import com.example.capstone.ui.profile.DetailProfileViewModel
import com.example.capstone.util.reduceFileImage
import com.example.capstone.util.uriToFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File

class EditProfileAvatarActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileAvatarBinding
    private var getFile: File? = null
    private lateinit var viewModelFactory: ViewModelFactory
    private val detailProfileViewModel: DetailProfileViewModel by viewModels { viewModelFactory }
    private val editProfileAvatarViewModel: EditProfileAvatarViewModel by viewModels { viewModelFactory }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this,
                    "Tidak Ada Response",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileAvatarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setPermission()
        setActionBar()
        setViewModel()
        getDataUser()
        btnEdit()
        btnGalery()
    }

    private fun setPermission() {
        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private fun setActionBar() {
        supportActionBar?.hide()
    }

    private fun setViewModel() {
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
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
                        Glide.with(binding.ivEditAvatar)
                            .load(it.data.data.image_profile)
                            .fitCenter()
                            .into(ivEditAvatar)
                        showLoading(false)
                    }
                }
            }
        }
    }

    private fun btnGalery() {
        binding.btnOpenGalery.setOnClickListener {
            startGalery()
        }
    }

    private fun btnEdit() {
        binding.btnUpdateAvatar.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan !!!")
            builder.setMessage("Upadte Avatar ?")
            builder.setNegativeButton("Tidak") { _, _ ->
            }
            builder.setPositiveButton("Iya") { _, _ ->
                updateProfileAvatar()
            }
            val alert = builder.create()
            alert.show()
        }
    }

    private fun startGalery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Pilih Gambar")
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToFile(selectedImg, this)
            getFile = myFile
            binding.ivEditAvatar.setImageURI(selectedImg)
        }
    }

    private fun updateProfileAvatar() {
        var file = reduceFileImage(getFile as File)
        val currentImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
            "image_profile",
            file.name,
            currentImageFile
        )
        editProfileAvatarViewModel.updateProfileAvatar(
            imageMultipart
        ).observe(this) {
            if (it != null) {
                when (it) {
                    is Result.Loading -> {
                        showLoading(true)
                    }
                    is Result.Error -> {
                        showLoading(false)
                        Log.d("eyoy", "${it.error}")
                        Toast.makeText(this, "${it.error}", Toast.LENGTH_SHORT).show()
                    }
                    is Result.Success -> {
                        showLoading(false)
                        successAlert()
                        setToastSucces()
                    }
                }
            }
        }
    }

    private fun setToastSucces() {
        val titleToast = "Sukses !!!"
        val messageToast = "Sukses Upload Event"
        MotionToast.createColorToast(
            this,
            titleToast,
            "$messageToast",
            MotionToastStyle.SUCCESS,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(this, R.font.poppins)
        )
    }

    private fun successAlert() {
        MyAlertDialog(
            this,
            R.string.sukses,
            R.drawable.success_alert,
            fun() {
                val moveActivity = Intent(this, MainActivity::class.java)
                moveActivity.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(moveActivity)
                finish()
            }
        ).show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarEditAvatar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}
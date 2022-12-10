package com.example.capstone.ui.detail_post

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.example.capstone.R
import com.example.capstone.data.Result
import com.example.capstone.databinding.ActivityDetailEditPostBinding
import com.example.capstone.databinding.ActivityEditEventBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.ui.custom_view.MyAlertDialog
import com.example.capstone.ui.detail_comment.DetailCommentActivity
import com.example.capstone.ui.detail_event.DetailEventActivity
import com.example.capstone.ui.detail_event.DetailEventViewModel
import com.example.capstone.ui.list_post.ListPostActivity
import com.example.capstone.ui.main.MainActivity
import com.example.capstone.ui.profile.DetailProfileViewModel
import com.example.capstone.ui.upload_event.UploadActivity
import com.example.capstone.util.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.util.*

class DetailEditPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailEditPostBinding
    private lateinit var viewModelFactory: ViewModelFactory
    private var getFile: File? = null
    private var suratFile: File? = null
    private var latLng: LatLng? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val detailEventViewModel: DetailEventViewModel by viewModels { viewModelFactory }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setActionBar()
        setViewModel()
        getDataEvent()
        back()
        btnCancel()
        submitEvent()
        btnGalery()
        startPdf()
        addLoc()
        binding.edtDateEventEdit.setOnClickListener {
            setCalender()
        }

    }

    private fun setActionBar() {
        supportActionBar?.hide()
    }

    private fun setViewModel() {
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }

    private fun back() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun btnCancel() {
        binding.btnCancelUpdateEvent.setOnClickListener {
            onBackPressed()
        }
    }

    private fun btnGalery() {
        binding.tvUpload.setOnClickListener {
            startGalery()
        }
    }

    private fun setCalender() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val moth = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(
            this, DatePickerDialog.OnDateSetListener { view, year, monthofYear, dayOfMonth ->
                val returnDate = "${monthofYear + 1} $dayOfMonth $year"
                val date = StringHelper.parseDate(
                    "dd MM yyyy",
                    "dd/MM/yy",
                    returnDate
                )
                binding.edtDateEventEdit.setText(
                    StringHelper.parseDate(
                        "dd/MM/yy",
                        "dd MM yyyy",
                        date
                    )
                )
                binding.edtDateEventEdit.error = null
            }, year, moth, day
        )
        dpd.show()
    }

    @SuppressLint("missingpermission")
    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    latLng = LatLng(it.latitude, it.longitude)
                    binding.tvLocationEdit.text = "${it.latitude} , ${it.longitude}"
                    Toast.makeText(this, "Berhasil Mendapatkan Lokasi", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal Mendapatkan Lokasi", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "izinkan lokasi", Toast.LENGTH_SHORT).show()
            requestPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)

        }
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                getLocation()
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

            binding.ivUpload.setImageURI(selectedImg)
        }
    }

    private fun addLoc() {
        binding?.addLoaction.setOnClickListener {
            getLocation()
        }
    }

    private fun startPdf() {
        binding.ivSK.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            val chooser = Intent.createChooser(intent, "Choose a File")
            launcherIntentPdf.launch(chooser)
        }
    }

    private val launcherIntentPdf = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri

            val myFile = uriToSurat(selectedImg, this)

            suratFile = myFile

            binding.ivSK.setImageURI(selectedImg)
        }
    }

    private fun getDataEvent() {
        val id = intent.getIntExtra(DetailPostActivity.EXTRA_ID_POST_DETAIL, 0)
        detailEventViewModel.getEventById(id).observe(this) {
            when (it) {
                is Result.Loading -> {
                }
                is Result.Error -> {
                    Log.d("Eyyoy", it.error)

                }
                is Result.Success -> {
                    Log.d("cekID", "${it.data.data.id}")
                    Log.d("cekName", it.data.data.name)
                    binding.edtNameEventEdit.setText(it.data.data.name)
                    binding.edtDateEventEdit.setText(it.data.data.date)
                    binding.edtAuthorEventEdit.setText(it.data.data.author)
                    binding.edtDescEventEdit.setText(it.data.data.deskripsi)
                    binding.edtContactEventEdit.setText(it.data.data.contact_person)
                    binding.edtEmailEventEdit.setText(it.data.data.email)
                    binding.edtLocationEventEdit.setText(it.data.data.location)
                    binding.tvLocationEdit.setText("${it.data.data.latitude}, ${it.data.data.longitude} ")
                    Glide.with(this@DetailEditPostActivity)
                        .load(it.data.data.image_poster)
                        .into(binding.ivUpload)
                    Glide.with(this@DetailEditPostActivity)
                        .load(it.data.data.image_surat)
                        .into(binding.ivSK)

                }
            }
        }
    }

    private fun submitEvent() {
        binding.btnPostEvent.setOnClickListener {
            val edtDesc = binding?.edtDescEventEdit?.text.toString()
            val edtName = binding?.edtNameEventEdit?.text.toString()
            val edtLoc = binding?.edtLocationEventEdit?.text.toString()
            val edtPhone = binding?.edtContactEventEdit?.text.toString()
            val edtAuthor = binding?.edtAuthorEventEdit?.text.toString()
            val edtEmail = binding?.edtEmailEventEdit?.text.toString()
            val edtDate = binding?.edtDateEventEdit?.text.toString()

            if (!TextUtils.isEmpty(edtDesc) && !TextUtils.isEmpty(edtName) && !TextUtils.isEmpty(
                    edtLoc
                ) && !TextUtils.isEmpty(edtPhone) && !TextUtils.isEmpty(edtPhone) && !TextUtils.isEmpty(
                    edtAuthor
                ) && !TextUtils.isEmpty(edtEmail) && !TextUtils.isEmpty(edtDate)
            ) {
                cobaUpdate()
            } else {
                MyAlertDialog(
                    this,
                    R.string.error_validation,
                    R.drawable.error_form
                ).show()
            }
        }
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
        binding.progressBarUpload.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun cobaUpdate() {
        if (getFile != null && suratFile != null && latLng != null) {
            val file = reduceFileImage(getFile as File)
            val surat = reduceFileSurat(suratFile as File)
            val id = intent.getIntExtra(DetailPostActivity.EXTRA_ID_POST_DETAIL, 0)

            val name = binding.edtNameEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val date = binding.edtDateEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val loc = binding.edtLocationEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val dekripsi = binding.edtDescEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val author = binding.edtAuthorEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val email = binding.edtEmailEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val contact = binding.edtContactEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())

            val currentImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "image_poster",
                file.name,
                currentImageFile
            )
            val currentPdf = surat.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val pdfMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "image_surat",
                surat.name,
                currentPdf
            )
            detailEventViewModel.updateEvent(
                id,
                name,
                date,
                imageMultipart,
                pdfMultipart,
                loc,
                dekripsi,
                lat = (latLng as LatLng).latitude,
                lon = (latLng as LatLng).longitude,
                author,
                email,
                contact
            ).observe(this) {
                if (it != null) {
                    showLoading(false)
                    setToastSucces()
                    startActivity(Intent(this, DetailPostActivity::class.java).also {
                        it.putExtra(DetailPostActivity.EXTRA_ID_POST_DETAIL, id)
                        finish()
                    })
                } else {
                    Toast.makeText(
                        this,
                        "Gagal Upload Story",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        } else if (getFile != null && suratFile != null) {
            val file = reduceFileImage(getFile as File)
            val surat = reduceFileSurat(suratFile as File)
            val id = intent.getIntExtra(DetailPostActivity.EXTRA_ID_POST_DETAIL, 0)

            val name = binding?.edtNameEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val date = binding?.edtDateEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val location = binding?.edtLocationEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val dekripsi = binding?.edtDescEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val author = binding?.edtAuthorEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val email = binding?.edtEmailEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val contact = binding?.edtContactEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())

            val currentImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "image_poster",
                file.name,
                currentImageFile
            )
            val currentPdf = surat.asRequestBody("application/pdf".toMediaTypeOrNull())
            val pdfMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "image_surat",
                surat.name,
                currentPdf
            )
            detailEventViewModel.updateEventWIthoutLocation(
                id,
                name,
                date,
                imageMultipart,
                pdfMultipart,
                location,
                dekripsi,
                author,
                email,
                contact
            )
                .observe(this) { result ->
                    when (result) {
                        is Result.Loading -> {
                            showLoading(true)
                        }
                        is Result.Error -> {
                            showLoading(false)
                            successAlert()
                            setToastSucces()
                        }
                        is Result.Success -> {
                            successAlert()
                            setToastSucces()
                        }
                    }

                }

        } else if (latLng != null) {
            val id = intent.getIntExtra(DetailPostActivity.EXTRA_ID_POST_DETAIL, 0)

            val name = binding.edtNameEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val date = binding.edtDateEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val loc = binding.edtLocationEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val dekripsi = binding.edtDescEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val author = binding.edtAuthorEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val email = binding.edtEmailEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val contact = binding.edtContactEventEdit.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            detailEventViewModel.updateEventWithouImage(
                id,
                name,
                date,
                loc,
                dekripsi,
                lat = (latLng as LatLng).latitude,
                lon = (latLng as LatLng).longitude,
                author,
                email,
                contact
            ).observe(this) {
                if (it != null) {
                    Log.d("cekData2", "$it")
                    setToastSucces()
                    finish()
                    startActivity(Intent(this, DetailPostActivity::class.java).also {
                        it.putExtra(DetailPostActivity.EXTRA_ID_POST_DETAIL, id)
                    })
                } else {
                    Toast.makeText(
                        this,
                        "Gagal Upload Story",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        } else {
            val id = intent.getIntExtra(DetailPostActivity.EXTRA_ID_POST_DETAIL, 0)

            val name = binding?.edtNameEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val date = binding?.edtDateEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val location = binding?.edtLocationEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val dekripsi = binding?.edtDescEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val author = binding?.edtAuthorEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val email = binding?.edtEmailEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            val contact = binding?.edtContactEventEdit?.text.toString()
                .toRequestBody("text/plain".toMediaTypeOrNull())
            detailEventViewModel.updateEventWIthoutImageLocation(
                id,
                name,
                date,
                location,
                dekripsi,
                author,
                email,
                contact
            )
                .observe(this) { result ->
                    when (result) {
                        is Result.Loading -> {
                            showLoading(true)
                        }
                        is Result.Error -> {
                            showLoading(false)
                            successAlert()
                            setToastSucces()
                        }
                        is Result.Success -> {
                            successAlert()
                            setToastSucces()
                        }
                    }
                }
        }
    }

    private fun setToastSucces() {
        val titleToast = "Sukses !!!"
        val messageToast = "Sukses Update Event"
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
}


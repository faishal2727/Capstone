package com.example.capstone.ui.upload_event

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.capstone.R
import com.example.capstone.data.Result
import com.example.capstone.databinding.ActivityUploadBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.ui.custom_view.MyAlertDialog
import com.example.capstone.ui.main.MainActivity
import com.example.capstone.util.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.io.File
import java.util.*

class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private var getFile: File? = null
    private var suratFile: File? = null
    private lateinit var viewModelFactory: ViewModelFactory
    private val uploadVieModel: UploadViewModel by viewModels { viewModelFactory }
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

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
                    "Tidak Mendapatkan Response",
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
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setViewModel()
        setActionBar()
        setPermission()
        getDate()
        btnGalery()
        btnPdf()
        addLoc()
        submitEvent()
        back()

    }

    private fun setViewModel() {
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }

    private fun setActionBar() {
        supportActionBar?.hide()
    }

    private fun back() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getDate() {
        binding.edtDateEvent.setOnClickListener {
            setCalender()
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
                    "mm DD yyyy",
                    "mm/DD/yy",
                    returnDate
                )
                binding.edtDateEvent.setText(StringHelper.parseDate("mm/DD/yy", "mm DD yyyy", date))
                binding.edtDateEvent.error = null
            }, year, moth, day
        )
        dpd.show()
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

    private fun getLoc() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                10
            )
            return
        }

        val loc = fusedLocationProviderClient.lastLocation
        loc.addOnSuccessListener {
            if (it != null) {
                val lat = it.latitude.toString()
                val lot = it.longitude.toString()
                binding.tvLocation.text = lat + "," + lot
                Toast.makeText(this, "Berhasil Mendapatkan Lokasi", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Gagal Mendapatkan Lokasi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addLoc() {
        binding.addLoaction.setOnClickListener {
            getLoc()
        }
    }

    private fun btnGalery() {
        binding.tvUpload.setOnClickListener {
            startGalery()
        }
    }

    private fun btnPdf() {
        binding.btnSK.setOnClickListener {
            startPdf()
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

    private fun startPdf() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a File")
        launcherIntentPdf.launch(chooser)
    }

    private val launcherIntentPdf = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri
            val myFile = uriToSurat(selectedImg, this)
            suratFile = myFile
            binding.tvUploadFile.text = "Berhasil upload !!"
        }
    }

    private fun submitEvent() {
        binding.btnPostEvent.setOnClickListener {
            val edtDesc = binding.edtDescEvent.text.toString()
            val edtName = binding.edtNameEvent.text.toString()
            val edtLoc = binding.edtLocationEvent.text.toString()
            val edtPhone = binding.edtContactEvent.text.toString()
            val edtAuthor = binding.edtAuthorEvent.text.toString()
            val edtEmail = binding.edtEmailEvent.text.toString()
            val edtDate = binding.edtDateEvent.text.toString()

            if (!TextUtils.isEmpty(edtDesc) && !TextUtils.isEmpty(edtName) && !TextUtils.isEmpty(
                    edtLoc
                ) && !TextUtils.isEmpty(edtPhone) && !TextUtils.isEmpty(edtPhone) && !TextUtils.isEmpty(
                    edtAuthor
                ) && !TextUtils.isEmpty(edtEmail) && !TextUtils.isEmpty(edtDate) && getFile != null && suratFile != null
            ) {
                uploadEvent()
            } else {
                MyAlertDialog(
                    this,
                    R.string.error_validation,
                    R.drawable.error_form
                ).show()
            }
        }
    }

    private fun uploadEvent() {
        val task = fusedLocationProviderClient.lastLocation
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 10
            )
            return
        }
        var lat: Double
        var lon: Double
        task.addOnSuccessListener { loc: Location? ->
            if (loc != null) {
                lat = loc.latitude
                lon = loc.longitude
                val file = reduceFileImage(getFile as File)
                val surat = reduceFileSurat(suratFile as File)

                val name = binding.edtNameEvent.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val date = binding.edtDateEvent.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val location = binding.edtLocationEvent.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val dekripsi = binding.edtDescEvent.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val author = binding.edtAuthorEvent.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val email = binding.edtEmailEvent.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val contact = binding.edtContactEvent.text.toString()
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
                uploadVieModel.uploadEvents(
                    name,
                    date,
                    imageMultipart,
                    pdfMultipart,
                    location,
                    dekripsi,
                    lat,
                    lon,
                    author,
                    email,
                    contact
                ).observe(this) { result ->
                    if (result != null) {
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
            } else {
                val file = reduceFileImage(getFile as File)
                val surat = reduceFileSurat(suratFile as File)

                val name = binding.edtNameEvent.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val date = binding.edtDateEvent.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val location = binding.edtLocationEvent.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val dekripsi = binding.edtDescEvent.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val author = binding.edtAuthorEvent.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val email = binding.edtEmailEvent.text.toString()
                    .toRequestBody("text/plain".toMediaTypeOrNull())
                val contact = binding.edtContactEvent.text.toString()
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
                uploadVieModel.uploadEventsWithoutLocation(
                    name, date, imageMultipart,
                    pdfMultipart, location, dekripsi, author, email, contact
                ).observe(this) { result ->

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

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }
}
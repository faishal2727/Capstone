package com.example.capstone.ui.daftar_event

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.capstone.R
import com.example.capstone.data.Result
import com.example.capstone.databinding.ActivityDaftarEventBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.ui.list_join.ListJoinActivity
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class DaftarEventActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDaftarEventBinding
    private lateinit var viewModelFactory: ViewModelFactory
    private val daftarEventViewModel: DaftarEventViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDaftarEventBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val i = intent.getIntExtra(EXTRA_ID_EVENT, 0)
        binding.edtIdEvent.setText(i.toString())
        setActionBar()
        setViewModel()
        buttonJoin()
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

    private fun buttonJoin() {
        binding.btnJOinEvent.setOnClickListener {

            val id_event = binding.edtIdEvent.text.toString().toInt()
            val nama = binding.edtNameUser.text.toString()
            val alamat = binding.edtAddressUser.text.toString()
            val phone = binding.edtContactUser.text.toString()
            if (id_event == null || nama.isEmpty() || alamat.isEmpty() || phone.isEmpty()) {
                setToastFormNotFully()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Peringatan !!!")
                builder.setMessage("Yakin Daftar Event ?")
                builder.setNegativeButton("Tidak") { _, _ ->
                }
                builder.setPositiveButton("Iya") { _, _ ->
                    joinEvent()
                    startActivity(Intent(this, ListJoinActivity::class.java))
                    setToastSuccessJoin()
                    finish()
                }
                val alert = builder.create()
                alert.show()
            }
        }
    }

    private fun joinEvent() {

        val id_event = binding.edtIdEvent.text.toString().toInt()
        val nama = binding.edtNameUser.text.toString()
        val alamat = binding.edtAddressUser.text.toString()
        val phone = binding.edtContactUser.text.toString()

        daftarEventViewModel.joinEvent(id_event, nama, alamat, phone).observe(this) {
            if (it != null) {
                when (it) {
                    is Result.Loading -> {
                        showLoading(true)
                    }
                    is Result.Success -> {
                        setToastSuccessJoin()
                    }
                    is Result.Error -> {
                        showLoading(false)
                        setToastFormNotFully()
                    }
                }
            }

        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarDaftar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setToastSuccessJoin() {
        val titleToast = "Sukses !!!"
        val messageToast = "Berhasil Join Event"
        MotionToast.createColorToast(
            this,
            titleToast,
            "$messageToast",
            MotionToastStyle.SUCCESS,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.SHORT_DURATION,
            ResourcesCompat.getFont(this, R.font.poppins)
        )
    }

    private fun setToastFormNotFully() {
        val titleToast = "Peringatan !!!"
        val messageToast = "Semua Form Wajib Di isi"
        MotionToast.createColorToast(
            this,
            titleToast,
            "$messageToast",
            MotionToastStyle.ERROR,
            MotionToast.GRAVITY_BOTTOM,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(this, R.font.poppins)
        )
    }

    companion object {
        const val EXTRA_ID_EVENT = "extra id"
    }
}
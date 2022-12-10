package com.example.capstone.ui.detail_post

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.example.capstone.R
import com.example.capstone.data.Result
import com.example.capstone.databinding.ActivityDetailPostBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.ui.detail_event.DetailEventViewModel
import com.example.capstone.ui.list_post.ListPostActivity
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle


class DetailPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPostBinding
    private lateinit var viewModelFactory: ViewModelFactory
    private val detailEventViewModel: DetailEventViewModel by viewModels { viewModelFactory }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActionBar()
        setViewModel()
        getDetailEvent()
        btnDelete()
        back()
        btnUpdate()
    }

    private fun setActionBar(){
        supportActionBar?.hide()
    }
    private fun back(){
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this, ListPostActivity::class.java).also {
                finish()
            })
        }
    }
    private fun setViewModel(){
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, ListPostActivity::class.java).also {
            finish()
        })
    }

    private fun getDetailEvent() {
        val id = intent.getIntExtra(EXTRA_ID_POST_DETAIL, 1)
        detailEventViewModel.getEventById(id).observe(this) {
            when (it) {
                is Result.Loading -> {
                }
                is Result.Error -> {
                    Log.d("Eyyoy", "${it.error}")

                }
                is Result.Success -> {
                    Toast.makeText(this, "${it.data.data.id}", Toast.LENGTH_SHORT).show()
                    binding?.apply {
                        Log.d("KAMU", "${it.data.data.id}")
                        tvNameEventDetail.text = it.data.data.name
                        tvDateEvent.text = it.data.data.date
                        tvDescEvent.text = it.data.data.deskripsi
                        tvDetailLoc.text = it.data.data.location
                        tvDetailContactHp.text = it.data.data.contact_person
                        tvDetailEmail.text = it.data.data.email
                        tvDetailAuthor.text = it.data.data.author
                        Glide.with(this@DetailPostActivity)
                            .load(it.data.data.image_poster)
                            .into(ivEvent)

                    }
                }
            }
        }
    }

    private fun btnDelete(){
        val id = intent.getIntExtra(EXTRA_ID_POST_DETAIL, 0)
        binding.btnDeleteEvent.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan !!!")
            builder.setMessage("Hapus Event ?")
            builder.setNegativeButton("Tidak") { _, _ ->
            }
            builder.setPositiveButton("Iya") { _, _ ->
                deleteEvent()
                setToastSuccessDeletePost()
                startActivity(Intent(this, ListPostActivity::class.java).also {
                    it.putExtra(EXTRA_ID_POST_DETAIL, id)
                })
                finish()
            }
            val alert = builder.create()
            alert.show()
        }
    }
    private fun btnUpdate(){
        val id = intent.getIntExtra(EXTRA_ID_POST_DETAIL, 0)
        binding.btnUpdateEvent.setOnClickListener {
            startActivity(Intent(this, DetailEditPostActivity::class.java).also {
                it.putExtra(EXTRA_ID_POST_DETAIL,id)
            })
        }
    }
    private fun deleteEvent(){
        val id = intent.getIntExtra(EXTRA_ID_POST_DETAIL, 0)
        detailEventViewModel.deleteMyPost(id).observe(this){
            if (it != null){
                when (it) {
                    is Result.Loading -> {
                        showLoading(true)
                    }
                    is Result.Error -> {
                        showLoading(false)
                        Log.d("HapusD", "${it.error}")
                    }
                    is Result.Success -> {
                        showLoading(false)
                        setToastSuccessDeletePost()
                        Log.d("hapusC", "${it.data.msg}")
                    }
                }
            }
        }
    }
    private fun showLoading(isLoading: Boolean) {
        binding.progressBarDetailPost.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun setToastSuccessDeletePost() {
        val titleToast = "Sukses !!!"
        val messageToast = "Berhasil Hapus Event"
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

    companion object{
        const val EXTRA_ID_POST_DETAIL = "extra_id_post"
    }
}
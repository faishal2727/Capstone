package com.example.capstone.ui.detail_comment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.example.capstone.R
import com.example.capstone.data.Result
import com.example.capstone.databinding.ActivityDetailCommentBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.ui.detail_event.DetailEventActivity
import com.example.capstone.ui.detail_event.DetailEventActivity.Companion.EXTRA_ID
import com.example.capstone.ui.profile.DetailProfileViewModel
import com.example.capstone.util.Constanta
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle
import java.util.*

class DetailCommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailCommentBinding
    private lateinit var viewModelFactory: ViewModelFactory
    private val detailCommentViewModel: DetailCommentViewModel by viewModels { viewModelFactory }
    private val detailProfileViewModel: DetailProfileViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setViewModel()
        getDetailComment()
        getDataUser()
        btnDeleteComment()
        btnUpdateComment()
        setActionBar()
        back()

    }

    private fun setViewModel() {
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }
    private fun setActionBar() {
        supportActionBar?.hide()
    }

    private fun back(){
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getDataUser() {
        val i = intent.getIntExtra(EXTRA_USER, 0)
        detailProfileViewModel.getDataUser.observe(this) {
            binding.apply {
                when (it) {
                    is Result.Success -> {
                        val b = it.data.data.id
                        if (i == b) {
                            binding.btnDeleteComment.visibility = View.VISIBLE
                            binding.btnUpdateComment.visibility = View.VISIBLE
                        } else {
                            binding.btnDeleteComment.visibility = View.GONE
                            binding.btnUpdateComment.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    fun getDetailComment() {
        val id = intent.getIntExtra(EXTRA_COMMENT, 0)
        detailCommentViewModel.getCommentById(id).observe(this) {
            when (it) {
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Error -> {
                    showLoading(false)
                }
                is Result.Success -> {
                    showLoading(false)
                    binding.apply {
                        tvCreated.text = Constanta.formatDate(it.data.data.createdAt, TimeZone.getDefault().id)
                        tvComment.text = it.data.data.commentar
                        tvUsernameComment.text = it.data.data.user.name
                        Glide.with(binding.ivAvatarUser)
                            .load(it.data.data.user.image_profile)
                            .circleCrop()
                            .into(ivAvatarUser)
                    }
                }
            }
        }
    }

    private fun btnDeleteComment() {
        val idEvent = intent.getIntExtra(EXTRA_EVENT, 1)
        binding.btnDeleteComment.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan !!!")
            builder.setMessage("Hapus Komentar mu ?")
            builder.setNegativeButton("Tidak") { _, _ ->
            }
            builder.setPositiveButton("Iya") { _, _ ->
                deleteComment()
                setToastSuccessDeleteComment()
                startActivity(Intent(this, DetailEventActivity::class.java).also {
                    it.putExtra(EXTRA_ID, idEvent)
                    finish()
                })
            }
            val alert = builder.create()
            alert.show()
        }
    }

    private fun deleteComment() {
        val id = intent.getIntExtra(EXTRA_COMMENT, 0)
        detailCommentViewModel.deleteComment(id).observe(this) {
            if (it != null) {
                when (it) {
                    is Result.Loading -> {
                        showLoading(true)
                    }
                    is Result.Error -> {
                        showLoading(false)
                    }
                    is Result.Success -> {
                        showLoading(false)
                        setToastSuccessDeleteComment()
                    }
                }
            }
        }
    }

    private fun btnUpdateComment() {
        val idComment = intent.getIntExtra(EXTRA_COMMENT, 0)
        binding.btnUpdateComment.setOnClickListener {
            detailCommentViewModel.getCommentById(idComment).observe(this) {
                when (it) {
                    is Result.Success -> {
                        val comment = it.data.data.commentar
                        val mDialogView =
                            LayoutInflater.from(this).inflate(R.layout.edit_layout, null)
                        val builder = AlertDialog.Builder(this)
                            .setView(mDialogView)
                        val mAlertDialog = builder.show()
                        val btnOk = mDialogView.findViewById<Button>(R.id.btnUpdateComment)
                        val edtComment =
                            mDialogView.findViewById<EditText>(R.id.edtCommentUpdateEvent)
                        val cancelUpdate = mDialogView.findViewById<Button>(R.id.btnCancelUpdateComment)
                        edtComment.setText("$comment")
                        btnOk.setOnClickListener {
                           val update = edtComment.text.toString()
                            detailCommentViewModel.updateComment(idComment,update).observe(this){
                                when(it){
                                    is Result.Error -> {
                                        Log.d("comentC", "${it.error}")
                                    }
                                    is Result.Success ->{
                                        Log.d("commentU", "${it.data.msg}")
                                    }
                                }
                            }
                            setToastSuccessUpdateComment()
                            startActivity(Intent(this,DetailCommentActivity::class.java).also {
                                it.putExtra(EXTRA_COMMENT, idComment)
                                finish()
                            })

                        }
                        cancelUpdate.setOnClickListener {
                            mAlertDialog.dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun setToastSuccessUpdateComment() {
        val titleToast = "Sukses !!!"
        val messageToast = "Berhasil Update Komentar"
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

    private fun setToastSuccessDeleteComment() {
        val titleToast = "Sukses !!!"
        val messageToast = "Berhasil Hapus Komentar"
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

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarDetailComment.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        const val EXTRA_COMMENT = "extra_comment"
        const val EXTRA_USER = "extra_user"
        const val EXTRA_EVENT = "extra_event"
    }
}
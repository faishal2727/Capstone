package com.example.capstone.ui.list_post

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.adapter.ListEventsAdapter
import com.example.capstone.data.Result
import com.example.capstone.databinding.ActivityListPostBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.model.event_model.Greevents
import com.example.capstone.ui.detail_post.DetailPostActivity
import com.example.capstone.ui.detail_post.DetailPostActivity.Companion.EXTRA_ID_POST_DETAIL
import com.example.capstone.ui.main.MainActivity

class ListPostActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListPostBinding
    private lateinit var listEventAdapter: ListEventsAdapter
    private lateinit var viewModelFactory: ViewModelFactory
    private val listPostViewModel: ListPostViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()
        setViewModel()
        getListMyPost()
        back()

    }

    private fun setActionBar() {
        supportActionBar?.hide()
    }

    private fun back() {
        binding.btnBack.setOnClickListener {
            startActivity(Intent(this@ListPostActivity, MainActivity::class.java).also {
                finish()
            })
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this@ListPostActivity, MainActivity::class.java).also {
            finish()
        })
    }

    private fun setViewModel() {
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }

    private fun getListMyPost() {
        listPostViewModel.getListMyPost().observe(this) {
            when (it) {
                is Result.Error -> {
                    Toast.makeText(this, "${it.error}", Toast.LENGTH_SHORT).show()
                }
                is Result.Success -> {
                    setRecyler(it.data.data)
                    Log.d("Horre", "${it.data.msg}")
                }
            }
        }
    }


    private fun setRecyler(listPost: ArrayList<Greevents>) {
        listEventAdapter = ListEventsAdapter(listPost)
        binding.rvListMyPost.apply {
            adapter = listEventAdapter
            layoutManager = LinearLayoutManager(context)
        }
        listEventAdapter.setOnItemClickCallback(object : ListEventsAdapter.OnItemClickCallback {
            override fun onItemClicked(data: Greevents) {
                startActivity(Intent(this@ListPostActivity, DetailPostActivity::class.java).also {
                    it.putExtra(EXTRA_ID_POST_DETAIL, data.id)
                })
            }
        })
    }
}
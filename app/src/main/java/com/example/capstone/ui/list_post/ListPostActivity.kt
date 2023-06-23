package com.example.capstone.ui.list_post

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
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
    private val handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()
        setViewModel()
        getListMyPost()
        back()
        binding.swipeRefreshLayout.setOnRefreshListener { getListMyPost() }

    }

    private fun setActionBar() {
        supportActionBar?.hide()
    }

    private fun back(){
        binding.btnBack.setOnClickListener {
            onBackPressed()
            finish()
        }
    }

    private fun startShimmer() {
        binding.loadingEvent.startShimmer()
    }

    private fun stopShimmer() {
        binding.loadingEvent.stopShimmer()
        binding.loadingEvent.visibility = View.GONE
    }

    private fun setViewModel() {
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }

    private fun getListMyPost(){
        listPostViewModel.getListMyPost().observe(this){
            when(it){
                is Result.Loading -> {
                    startShimmer()
                }
                is Result.Error ->{
                    startShimmer()
                    handler.postDelayed({
                        stopShimmer()
                        binding.failureLoad.visibility = View.VISIBLE
                        binding.failureLoad.playAnimation()
                    },2500)
                        Toast.makeText(this, "${it.error}", Toast.LENGTH_SHORT).show()
                }
                is Result.Success ->{
                    stopShimmer()
                    setRecyler(it.data.data)
                    binding.failureLoad.visibility = View.GONE
                    binding.failureLoad.cancelAnimation()
                    binding.swipeRefreshLayout.isRefreshing = false
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
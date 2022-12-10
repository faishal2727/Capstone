package com.example.capstone.ui.list_join


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.adapter.JoinEventAdapter
import com.example.capstone.data.Result
import com.example.capstone.databinding.ActivityDetailJoinBinding
import com.example.capstone.databinding.ActivityListJoinBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.model.DetailJoin
import com.example.capstone.ui.detail_join.DetailJoinActivity
import com.example.capstone.ui.detail_join.DetailJoinActivity.Companion.EXTRA_DATA


class ListJoinActivity : AppCompatActivity() {
    private lateinit var binding: ActivityListJoinBinding
    private lateinit var listJoinEventAdapter: JoinEventAdapter
    private lateinit var viewModelFactory: ViewModelFactory
    private val listJoinViewModel: ListJoinViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setActionBar()
        setViewModel()
        getList()
        back()
    }

    private fun setActionBar(){
        supportActionBar?.hide()
    }
    private fun back(){
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setViewModel(){
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }

    private fun getList(){
        listJoinViewModel.listJoinEvent().observe(this){
            when(it){
                is Result.Loading -> {
                    showLoading(true)
                }
                is Result.Error ->{
                    showLoading(false)
                }
                is Result.Success ->{
                    setRecyler(it.data.data)
                    showLoading(false)
                }
            }
        }
    }

    private fun setRecyler(listJoin: ArrayList<DetailJoin>){
        listJoinEventAdapter = JoinEventAdapter(listJoin)
        binding.rvListJoin.apply {
            adapter = listJoinEventAdapter
            layoutManager = LinearLayoutManager(context)
        }
        listJoinEventAdapter.setOnItemClickCallback(object : JoinEventAdapter.OnItemClickCallBack{
            override fun onItemClicked(data: DetailJoin) {
                startActivity(Intent(this@ListJoinActivity, DetailJoinActivity::class.java).also {
                    it.putExtra(EXTRA_DATA, data)
                })
            }
        })
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBarListJoin.visibility = if (isLoading) View.VISIBLE else View.GONE
    }


}
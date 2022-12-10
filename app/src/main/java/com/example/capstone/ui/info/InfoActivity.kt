package com.example.capstone.ui.info

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.capstone.databinding.ActivityInfoBinding
import com.example.capstone.model.info_model.InfoModel

class InfoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getDetailInfo()
        setActionBar()
        back()

    }

    private fun setActionBar(){
        supportActionBar?.hide()
    }

    private fun back(){
        binding.ivback.setOnClickListener {
            onBackPressed()
        }
    }

    private fun getDetailInfo(){
        val getData = intent.getParcelableExtra<InfoModel>(EXTRA_INFO) as InfoModel
        binding.apply {
            tvArtikel.text = getData.artikel.trimEnd()
            tvTitle.text = getData.title.trimIndent()
            ivArtikel.setImageResource(getData.image)
        }
    }

    companion object{
        const val EXTRA_INFO = "extra_info"
    }
}
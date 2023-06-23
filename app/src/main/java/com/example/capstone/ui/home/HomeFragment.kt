package com.example.capstone.ui.home

import android.content.Intent
import android.nfc.NfcAdapter.EXTRA_ID
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.capstone.adapter.InfoAdapter
import com.example.capstone.adapter.ListEventsAdapter
import com.example.capstone.data.Result
import com.example.capstone.databinding.FragmentHomeBinding
import com.example.capstone.factory.ViewModelFactory
import com.example.capstone.model.event_model.Greevents
import com.example.capstone.model.info_model.InfoModel
import com.example.capstone.model.login_model.LoginResultModel
import com.example.capstone.preference.PreferenceLogin
import com.example.capstone.ui.detail_event.DetailEventActivity


class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val list = ArrayList<InfoModel>()
    private val handler = Handler()
    private lateinit var listEventsAdapter: ListEventsAdapter
    private lateinit var preferenceLogin: PreferenceLogin
    private lateinit var loginResultModel: LoginResultModel
    private lateinit var viewModelFactory: ViewModelFactory
    private val homeViewModel: HomeViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        preferenceLogin = PreferenceLogin(binding.root.context)
        loginResultModel = preferenceLogin.getUser()

        setViewModel()
        getUsernameUser()
        list.addAll(listUser)
        binding.swipeRefreshLayout.setOnRefreshListener { getData() }
        search()
        btnSearch()
        return root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun btnSearch(){
        binding.btnSearch.setOnClickListener {
            search()
        }
    }

    private fun startShimmer() {
        binding.loadingEvent.startShimmer()
    }

    private fun stopShimmer() {
        binding.loadingEvent.stopShimmer()
        binding.loadingEvent.visibility = View.GONE
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    private val listUser: ArrayList<InfoModel>
        get() {

            val dataName = resources.getStringArray(com.example.capstone.R.array.title)
            val dataUsername = resources.getStringArray(com.example.capstone.R.array.artikel)
            val dataAvatar = resources.obtainTypedArray(com.example.capstone.R.array.avatar)

            val listGithub = ArrayList<InfoModel>()

            for (i in dataName.indices) {
                val git = InfoModel(
                    dataName[i],
                    dataUsername[i],
                    dataAvatar.getResourceId(i, 1)
                )
                listGithub.add(git)
            }
            return listGithub
        }

//    private fun showRecylerView() {
//        val infoAdapter = InfoAdapter(list)
//        binding.rvNews.adapter = infoAdapter
//        binding.rvNews.layoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//        infoAdapter.setOnItemClickCallback(object : InfoAdapter.OnItemClickCallback {
//            override fun onItemClicked(data: InfoModel) {
//                startActivity(Intent(activity, InfoActivity::class.java).also {
//                    it.putExtra(EXTRA_INFO, data)
//                })
//            }
//        })
//    }

    private fun getUsernameUser() {
        binding.tvUsername.text = loginResultModel.name
    }

    private fun setViewModel() {
        viewModelFactory = ViewModelFactory.getInstnce(binding.root.context)
    }

    private fun getData() {
        homeViewModel.getAllEvents().observe(viewLifecycleOwner) {
            when (it) {
                is Result.Loading -> {
                    startShimmer()
                }
                is Result.Error -> {

                    startShimmer()
                    handler.postDelayed({
                        stopShimmer()
                        binding.failureLoad.visibility = View.VISIBLE
                        binding.failureLoad.playAnimation()
                    }, 2500)
                }
                is Result.Success -> {
                    binding.failureLoad.visibility = View.GONE
                    binding.failureLoad.cancelAnimation()
                    binding.swipeRefreshLayout.isRefreshing = false
                    setRecylerView(it.data.data)
                    stopShimmer()
                }
            }
        }
    }

    private fun setRecylerView(listEvent: ArrayList<Greevents>) {
        listEventsAdapter = ListEventsAdapter(listEvent)
        binding.rvEvents.apply {
            adapter = listEventsAdapter
            layoutManager = LinearLayoutManager(context)
        }
        listEventsAdapter.setOnItemClickCallback(object : ListEventsAdapter.OnItemClickCallback {
            override fun onItemClicked(data: Greevents) {
                startActivity(Intent(activity, DetailEventActivity::class.java).also {
                    it.putExtra(EXTRA_ID, data.id)
                    it.putExtra(DetailEventActivity.EXTRA_POSTER, data.image_poster)
                    it.putExtra(DetailEventActivity.EXTRA_SURAT, data.image_surat)
                })
            }
        })
    }

    private fun search() {
        binding.searcView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                startShimmer()
                if (query != null) {
                    homeViewModel.searchEvent(query).observe(viewLifecycleOwner) {
                        when (it) {
                            is Result.Loading -> {
                                startShimmer()
                            }
                            is Result.Error -> {
                                Toast.makeText(activity, "Terjadi Kesalahan", Toast.LENGTH_SHORT)
                                    .show()
                                binding.failureLoad.visibility = View.VISIBLE
                                binding.failureLoad.playAnimation()
//                                val includeView = binding.falilureLoads.root
//                                includeView.visibility = View.VISIBLE
//                               showLoading()
                                stopShimmer()
                            }
                            is Result.Success -> {
                                stopShimmer()
                                binding.failureLoad.visibility = View.GONE
                                binding.failureLoad.playAnimation()
//                                stopLoading()
                                Toast.makeText(activity, "Sukses Mencari Event", Toast.LENGTH_SHORT)
                                    .show()
                                setRecylerView(it.data.data)
                                if (it.data.data.isEmpty()) {
                                    binding.failureLoad.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                    stopShimmer()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                homeViewModel.searchEvent(newText.toString())
                return true
            }

        })
    }

//    private fun showLoading(isLoading: Boolean) {
//        binding.progressBarHome.visibility = if (isLoading) View.VISIBLE else View.GONE
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.week1_0627.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.week1_0627.databinding.FragmentDashboardBinding

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.week1_0627.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class DashboardFragment : Fragment() {

    private lateinit var recyclerViewFolders: RecyclerView
    private lateinit var recyclerViewImages: RecyclerView
    private lateinit var folderAdapter: FolderAdapter
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var favoriteImages: MutableList<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        recyclerViewFolders = view.findViewById(R.id.recycler_view_folders)
        recyclerViewFolders.layoutManager = GridLayoutManager(context, 2) // 폴더를 그리드 형식으로 표시

        recyclerViewImages = view.findViewById(R.id.recycler_view_images)
        recyclerViewImages.layoutManager = GridLayoutManager(context, 3) // 이미지를 그리드 형식으로 표시

        favoriteImages = loadFavoriteImages()
        loadFolders()

        return view
    }

    private fun loadFolders() {
        val folders = getFoldersFromAssets(context)
        folderAdapter = FolderAdapter(requireContext(), folders) { folderName ->
            loadImages(folderName)
        }
        recyclerViewFolders.adapter = folderAdapter
    }

    private fun loadImages(folderName: String) {
        val images = getImagesFromFolder(context, folderName)
        imageAdapter = ImageAdapter(images) { imagePath ->
            toggleFavorite(imagePath)
        }
        recyclerViewImages.adapter = imageAdapter
    }

    private fun getFoldersFromAssets(context: Context?): List<String> {
        val assetManager = context?.assets
        val allFiles = assetManager?.list("") ?: return emptyList()

        // 특정 폴더들 제외하기 (예: images, webkit, geoid_map)
        val excludedFolders = listOf("images", "webkit", "geoid_map")
        val folderList = mutableListOf<String>()

        for (file in allFiles) {
            val isFolder = assetManager.list(file)?.isNotEmpty() == true
            if (isFolder && file !in excludedFolders) {
                folderList.add(file)
            }
        }
        return folderList
    }

    private fun getImagesFromFolder(context: Context?, folderName: String): List<String> {
        val assetManager = context?.assets
        return assetManager?.list(folderName)?.map { "$folderName/$it" } ?: emptyList()
    }

    private fun loadFavoriteImages(): MutableList<String> {
        val sharedPreferences = context?.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val json = sharedPreferences?.getString("favorite_images", "[]")
        val type = object : TypeToken<MutableList<String>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun saveFavoriteImages() {
        val sharedPreferences = context?.getSharedPreferences("favorites", Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        val json = Gson().toJson(favoriteImages)
        editor?.putString("favorite_images", json)
        editor?.apply()
    }

    private fun toggleFavorite(imagePath: String) {
        if (favoriteImages.contains(imagePath)) {
            favoriteImages.remove(imagePath)
        } else {
            favoriteImages.add(imagePath)
        }
        saveFavoriteImages()
    }
}
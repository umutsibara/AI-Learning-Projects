package com.umutsibara.chess_app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.umutsibara.chess_app.databinding.FragmentFenBinding
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class FenFragment : Fragment() {

    private var _binding: FragmentFenBinding? = null
    private val binding get() = _binding!!

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { imageUri ->
                binding.imageViewBoard.setImageURI(imageUri)
                // Görsel seçildi, şimdi sunucuya gönderelim.
                uploadImageToServer(imageUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonGorselSec.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryLauncher.launch(intent)
        }
    }

    private fun uploadImageToServer(imageUri: Uri) {
        // Ağ isteğini Coroutine ile arka planda yapıyoruz.
        lifecycleScope.launch {
            try {
                // TextView'ı temizle ve yükleniyor mesajı göster
                binding.textViewFen.text = "FEN üretiliyor..."

                // 1. Uri'dan dosya verisini oku
                val inputStream = requireContext().contentResolver.openInputStream(imageUri)
                val fileBytes = inputStream?.readBytes()
                inputStream?.close()

                if (fileBytes == null) {
                    binding.textViewFen.text = "Hata: Görsel okunamadı."
                    return@launch
                }

                // 2. Retrofit için MultipartBody.Part oluştur
                val requestFile = fileBytes.toRequestBody(
                    requireContext().contentResolver.getType(imageUri)?.toMediaTypeOrNull()
                )
                val body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile)

                // 3. API isteğini yap
                val response = RetrofitClient.api.uploadImage(body)

                // 4. Gelen cevabı işle
                if (response.isSuccessful) {
                    val fenResponse = response.body()
                    fenResponse?.fen?.let {
                        binding.textViewFen.text = it
                    }
                    fenResponse?.error?.let {
                        binding.textViewFen.text = "Sunucu Hatası: $it"
                    }
                } else {
                    binding.textViewFen.text = "Hata: ${response.code()} - ${response.message()}"
                }

            } catch (e: Exception) {
                // İnternet yoksa veya sunucuya ulaşılamazsa bu blok çalışır.
                binding.textViewFen.text = "Bağlantı Hatası: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
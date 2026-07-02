package com.promobile.cipur

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.databinding.ActivityTiketKeluhanBinding

class TiketKeluhanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTiketKeluhanBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTiketKeluhanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnKirimTiket.setOnClickListener {
            val idPlg = binding.etIdPelanggan.text.toString().trim()
            val kategori = binding.etKategori.text.toString().trim()
            val keluhan = binding.etKeluhan.text.toString().trim()

            if (idPlg.isEmpty() || kategori.isEmpty() || keluhan.isEmpty()) {
                Toast.makeText(this, "Informasi tidak lengkap, mohon lengkapi data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            buatTiketOtomatis(idPlg, kategori, keluhan)
        }
    }

    private fun buatTiketOtomatis(idPlg: String, kategori: String, keluhan: String) {
        val noTiket = "CMP-${System.currentTimeMillis().toString().takeLast(5)}"
        val tiketData = hashMapOf(
            "idPelanggan" to idPlg,
            "kategori" to kategori,
            "keluhan" to keluhan,
            "status" to "Open"
        )

        db.collection("database_tiket").document(noTiket)
            .set(tiketData)
            .addOnSuccessListener {
                Toast.makeText(this, "Tiket Dibuat: $noTiket", Toast.LENGTH_LONG).show()
                finish()
            }
    }
}
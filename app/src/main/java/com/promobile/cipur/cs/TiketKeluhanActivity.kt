package com.promobile.cipur.cs

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.R
import com.promobile.cipur.databinding.CsActivityTiketBinding

class TiketKeluhanActivity : AppCompatActivity() {

    private lateinit var binding: CsActivityTiketBinding
    private val db = FirebaseFirestore.getInstance()

    private val listPelangganDisplay = mutableListOf<String>()
    private var selectedIdPelanggan = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = CsActivityTiketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadDataPelanggan()

        binding.etPelanggan.setOnItemClickListener { parent, _, position, _ ->
            val textTerpilih = parent.getItemAtPosition(position).toString()

            selectedIdPelanggan = textTerpilih.substringBefore(" -")
        }

        binding.btnKirimTiket.setOnClickListener {
            val inputText = binding.etPelanggan.text.toString()
            val kategori = binding.etKategori.text.toString().trim()
            val keluhan = binding.etKeluhan.text.toString().trim()

            if (selectedIdPelanggan.isEmpty() || !inputText.contains(selectedIdPelanggan)) {
                Toast.makeText(this, "Silakan pilih nama pelanggan dari daftar saran (dropdown)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (kategori.isEmpty() || keluhan.isEmpty()) {
                Toast.makeText(this, "Kategori dan detail keluhan tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            buatTiketOtomatis(selectedIdPelanggan, kategori, keluhan)
        }
    }

    private fun loadDataPelanggan() {
        db.collection("database_pelanggan").get()
            .addOnSuccessListener { documents ->
                listPelangganDisplay.clear()

                for (doc in documents) {
                    val nama = doc.getString("nama") ?: "Tanpa Nama"
                    val id = doc.id
                    listPelangganDisplay.add("$id - $nama")
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    listPelangganDisplay
                )
                binding.etPelanggan.setAdapter(adapter)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat daftar pelanggan", Toast.LENGTH_SHORT).show()
            }
    }

    private fun buatTiketOtomatis(idPlg: String, kategori: String, keluhan: String) {
        binding.btnKirimTiket.isEnabled = false

        val noTiket = "CMP-${System.currentTimeMillis().toString().takeLast(5)}"
        val tiketData = hashMapOf(
            "idPelanggan" to idPlg,
            "kategori" to kategori,
            "keluhan" to keluhan,
            "status" to "Open",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("database_tiket").document(noTiket)
            .set(tiketData)
            .addOnSuccessListener {
                Toast.makeText(this, "Sukses! Tiket $noTiket berhasil dibuat.", Toast.LENGTH_LONG).show()
                finish() 
            }
            .addOnFailureListener {
                binding.btnKirimTiket.isEnabled = true
                Toast.makeText(this, "Gagal membuat tiket, periksa koneksi", Toast.LENGTH_SHORT).show()
            }
    }
}
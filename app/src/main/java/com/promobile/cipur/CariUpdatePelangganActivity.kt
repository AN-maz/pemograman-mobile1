package com.promobile.cipur

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.databinding.ActivityCariUpdatePelangganBinding

class CariUpdatePelangganActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCariUpdatePelangganBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentIdPelanggan = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCariUpdatePelangganBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnCari.setOnClickListener {
            val idCari = binding.etSearchId.text.toString().trim()
            if (idCari.isNotEmpty()) {
                cariDataPelanggan(idCari)
            }
        }

        binding.btnUpdate.setOnClickListener {
            simpanPembaruanData()
        }
    }

    private fun cariDataPelanggan(idPelanggan: String) {
        db.collection("database_pelanggan").document(idPelanggan).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    currentIdPelanggan = idPelanggan
                    binding.layoutData.visibility = View.VISIBLE
                    binding.etNama.setText(document.getString("nama"))
                    binding.etWa.setText(document.getString("noWhatsapp"))
                    binding.etAlamat.setText(document.getString("alamat"))
                    Toast.makeText(this, "Data ditemukan", Toast.LENGTH_SHORT).show()
                } else {
                    binding.layoutData.visibility = View.GONE
                    Toast.makeText(this, "Data kosong / Pelanggan tidak ditemukan", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun simpanPembaruanData() {
        val nama = binding.etNama.text.toString().trim()
        val wa = binding.etWa.text.toString().trim()

        if (nama.isEmpty() || wa.isEmpty()) {
            Toast.makeText(this, "Format data tidak sesuai, lengkapi field", Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = mapOf(
            "nama" to nama,
            "noWhatsapp" to wa,
            "alamat" to binding.etAlamat.text.toString().trim()
        )

        db.collection("database_pelanggan").document(currentIdPelanggan)
            .update(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "Pembaruan profil sukses", Toast.LENGTH_SHORT).show()
            }
    }
}
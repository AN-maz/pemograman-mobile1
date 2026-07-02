package com.promobile.cipur

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.databinding.ActivityMintaBarangBinding

class MintaBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMintaBarangBinding
    private val db = FirebaseFirestore.getInstance()
    private val BATAS_STANDAR = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMintaBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.etJumlah.addTextChangedListener { text ->
            val jumlah = text.toString().toIntOrNull() ?: 0
            if (jumlah > BATAS_STANDAR) {
                binding.tilJustifikasi.visibility = View.VISIBLE
            } else {
                binding.tilJustifikasi.visibility = View.GONE
            }
        }

        binding.btnKirimPermintaan.setOnClickListener {
            val namaBarang = binding.etNamaBarang.text.toString().trim()
            val jumlahStr = binding.etJumlah.text.toString().trim()
            val jumlah = jumlahStr.toIntOrNull() ?: 0
            val justifikasi = binding.etJustifikasi.text.toString().trim()

            if (namaBarang.isEmpty() || jumlahStr.isEmpty()) {
                Toast.makeText(this, "Semua kolom wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi Blok Alt: Meminta Justifikasi Tambahan
            if (jumlah > BATAS_STANDAR && justifikasi.isEmpty()) {
                Toast.makeText(this, "Jumlah melebihi batas standar, mohon isi alasan justifikasi!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            simpanKeAntreanLogistik(namaBarang, jumlah, justifikasi)
        }
    }

    private fun simpanKeAntreanLogistik(barang: String, qty: Int, alasan: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnKirimPermintaan.isEnabled = false

        val nomorManifes = "MNF-${System.currentTimeMillis().toString().takeLast(6)}"

        val dataPermintaan = hashMapOf(
            "nomorManifes" to nomorManifes,
            "namaMaterial" to barang,
            "jumlah" to qty,
            "justifikasi" to if (qty > BATAS_STANDAR) alasan else "Normal",
            "statusAntrean" to "Menunggu Persetujuan Logistik"
        )

        db.collection("antrean_logistik").document(nomorManifes)
            .set(dataPermintaan)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnKirimPermintaan.isEnabled = true
                Toast.makeText(this, "Sukses! Nomor Manifes Berhasil Dibuat: $nomorManifes", Toast.LENGTH_LONG).show()
                finish() // Kembali ke dashboard
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnKirimPermintaan.isEnabled = true
                Toast.makeText(this, "Gagal mengirim data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
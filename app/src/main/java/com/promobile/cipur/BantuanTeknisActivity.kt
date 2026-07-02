package com.promobile.cipur

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.databinding.ActivityBantuanTeknisBinding

class BantuanTeknisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBantuanTeknisBinding
    private val db = FirebaseFirestore.getInstance()
    private var currentIdPelanggan = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBantuanTeknisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnCekKoneksi.setOnClickListener {
            val idPlg = binding.etIdPelanggan.text.toString().trim()
            if (idPlg.isNotEmpty()) {
                cekKonektivitas(idPlg)
            }
        }

        binding.btnRestart.setOnClickListener {
            simulasiRestart()
        }

        binding.btnTiketTeknisi.setOnClickListener {
            buatTiketFisik()
        }
    }

    private fun cekKonektivitas(idPelanggan: String) {
        currentIdPelanggan = idPelanggan

        // Simulasi Cek Koneksi berdasar statusLayanan di Database
        db.collection("database_pelanggan").document(idPelanggan).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val status = document.getString("statusLayanan")
                    if (status == "Aktif") {
                        binding.tvStatus.text = "Status Koneksi: Perangkat Online (Merespons)"
                        binding.btnRestart.visibility = View.VISIBLE
                        binding.btnTiketTeknisi.visibility = View.GONE
                    } else {
                        binding.tvStatus.text = "Status Koneksi: Terputus / Gagal Komunikasi"
                        binding.btnRestart.visibility = View.GONE
                        binding.btnTiketTeknisi.visibility = View.VISIBLE
                    }
                }
            }
    }

    private fun simulasiRestart() {
        binding.tvStatus.text = "Status Koneksi: Sedang Restart..."
        binding.btnRestart.isEnabled = false

        Handler(Looper.getMainLooper()).postDelayed({
            // Simulasi hasil acak setelah restart (Berhasil / Gagal)
            val isNormal = Math.random() > 0.5

            if (isNormal) {
                binding.tvStatus.text = "Status Koneksi: Laporan Selesai, Normal"
                Toast.makeText(this, "Laporan berhasil ditutup, log disimpan", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                binding.tvStatus.text = "Status Koneksi: Masih Bermasalah"
                binding.btnTiketTeknisi.visibility = View.VISIBLE
                binding.btnRestart.isEnabled = true
            }
        }, 2000)
    }

    private fun buatTiketFisik() {
        val noTiket = "FSC-${System.currentTimeMillis().toString().takeLast(4)}"
        val dataTiket = hashMapOf(
            "idPelanggan" to currentIdPelanggan,
            "statusPekerjaan" to "Open",
            "keterangan" to "Gangguan Fisik Jaringan"
        )

        db.collection("database_operasional").document(noTiket).set(dataTiket)
            .addOnSuccessListener {
                Toast.makeText(this, "Tiket fisik dikirim ke Teknisi: $noTiket", Toast.LENGTH_LONG).show()
                finish()
            }
    }
}
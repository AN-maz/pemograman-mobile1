package com.promobile.cipur

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.databinding.ActivityInstalasiBinding

class InstalasiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInstalasiBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityInstalasiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnSelesaiInstalasi.setOnClickListener {
            val nomorTiket = binding.etNoTiket.text.toString().trim()
            val laporanInstalasi = binding.etLaporan.text.toString().trim()

            if (nomorTiket.isEmpty() || laporanInstalasi.length < 10) {
                Toast.makeText(this, "Peringatan: Data Laporan Instalasi Kurang Lengkap (Minimal 10 Karakter)!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            prosesPenyelesaianTiket(nomorTiket, laporanInstalasi)
        }
    }

    private fun prosesPenyelesaianTiket(tiketId: String, laporan: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSelesaiInstalasi.isEnabled = false

        val updateDataOperasional = hashMapOf(
            "statusPekerjaan" to "Selesai",
            "laporanTeknisi" to laporan,
            "antreanAktivasiNoc" to true
        )

        db.collection("database_operasional").document(tiketId)
            .set(updateDataOperasional)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSelesaiInstalasi.isEnabled = true
                Toast.makeText(this, "Sukses! Tiket $tiketId berhasil ditutup dan diteruskan ke NOC.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnSelesaiInstalasi.isEnabled = true
                Toast.makeText(this, "Gagal memperbarui status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
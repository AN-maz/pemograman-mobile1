package com.promobile.cipur

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.databinding.ActivityMonitoringLogistikBinding

class MonitoringLogistikActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonitoringLogistikBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMonitoringLogistikBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        muatDataPermintaanTeknisi()
        muatDataStatusFinance()
        muatDataStokGudang()
    }

    private fun muatDataPermintaanTeknisi() {
        db.collection("antrean_logistik")
            .whereEqualTo("statusAntrean", "Menunggu Persetujuan Logistik")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.tvListPermintaan.text = "✅ Tidak ada antrean permintaan Teknisi."
                    return@addOnSuccessListener
                }

                val sb = java.lang.StringBuilder()
                for (doc in documents) {
                    val manifes = doc.getString("nomorManifes") ?: "-"
                    val material = doc.getString("namaMaterial") ?: "-"
                    val jumlah = doc.getLong("jumlah") ?: 0
                    sb.append("• [$manifes] $material (Qty: $jumlah)\n")
                }
                binding.tvListPermintaan.text = sb.toString().trim()
            }
            .addOnFailureListener {
                binding.tvListPermintaan.text = "Gagal memuat data."
            }
    }

    private fun muatDataStatusFinance() {
        db.collection("pengajuan_finance")
            .whereIn("statusApproval", listOf("Ditolak", "Butuh Revisi"))
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.tvListFinance.text = "✅ Tidak ada PR yang ditolak atau direvisi."
                    return@addOnSuccessListener
                }

                val sb = java.lang.StringBuilder()
                for (doc in documents) {
                    val barang = doc.getString("rincianBarang") ?: "-"
                    val status = doc.getString("statusApproval") ?: "-"
                    sb.append("• $barang ➔ Status: $status\n")
                }
                binding.tvListFinance.text = sb.toString().trim()
            }
            .addOnFailureListener {
                binding.tvListFinance.text = "Gagal memuat data."
            }
    }

    private fun muatDataStokGudang() {
        db.collection("database_inventaris")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    binding.tvListStok.text = "Data inventaris kosong."
                    return@addOnSuccessListener
                }

                val sb = java.lang.StringBuilder()
                for (doc in documents) {
                    val namaBarang = doc.id
                    val stok = doc.getLong("stok") ?: 0

                    // Beri tanda jika stok menipis
                    if (stok < 5) {
                        sb.append("• $namaBarang : $stok (⚠️ Menipis)\n")
                    } else {
                        sb.append("• $namaBarang : $stok\n")
                    }
                }
                binding.tvListStok.text = sb.toString().trim()
            }
            .addOnFailureListener {
                binding.tvListStok.text = "Gagal memuat data."
            }
    }
}
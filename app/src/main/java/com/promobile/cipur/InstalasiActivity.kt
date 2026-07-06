package com.promobile.cipur

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
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
    private val listTiketOpen = mutableListOf<String>()

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

        loadTiketTerbuka()

        binding.btnSelesaiInstalasi.setOnClickListener {
            val nomorTiket = binding.etNoTiket.text.toString().trim()
            val laporanInstalasi = binding.etLaporan.text.toString().trim()

            // Exceptional Flow 3E: Data belum lengkap
            if (nomorTiket.isEmpty() || laporanInstalasi.length < 10) {
                Toast.makeText(this, "Data belum lengkap! Laporan minimal 10 karakter.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            prosesPenyelesaianTiket(nomorTiket, laporanInstalasi)
        }
    }

    private fun loadTiketTerbuka() {
        // Hanya menarik tiket yang masih terbuka (Open)
        db.collection("database_operasional")
            .whereEqualTo("statusPekerjaan", "Open")
            .get()
            .addOnSuccessListener { documents ->
                listTiketOpen.clear()
                for (doc in documents) {
                    listTiketOpen.add(doc.id)
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listTiketOpen)
                binding.etNoTiket.setAdapter(adapter)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat daftar tiket", Toast.LENGTH_SHORT).show()
            }
    }

    private fun prosesPenyelesaianTiket(tiketId: String, laporan: String) {
        binding.btnSelesaiInstalasi.isEnabled = false

        val updateDataOperasional = hashMapOf(
            "statusPekerjaan" to "Selesai",
            "laporanTeknisi" to laporan,
            "antreanAktivasiNoc" to true // Meneruskan ke NOC (UC_OP08 Trigger)
        )

        db.collection("database_operasional").document(tiketId)
            .update(updateDataOperasional as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Sukses! Instalasi Selesai, diteruskan ke NOC.", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                binding.btnSelesaiInstalasi.isEnabled = true
                Toast.makeText(this, "Gagal memperbarui status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
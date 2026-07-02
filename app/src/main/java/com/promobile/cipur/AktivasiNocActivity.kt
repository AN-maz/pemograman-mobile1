package com.promobile.cipur

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.databinding.ActivityAktivasiNocBinding
import java.net.URLEncoder

class AktivasiNocActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAktivasiNocBinding
    private val db = FirebaseFirestore.getInstance()
    private val listDataTiket = mutableListOf<Pair<String, String>>()
    private var selectedTiketId: String? = null
    private var selectedIdPelanggan: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAktivasiNocBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadDataTiket()

        binding.etTiket.setOnItemClickListener { _, _, position, _ ->
            selectedTiketId = listDataTiket[position].first
            selectedIdPelanggan = listDataTiket[position].second
        }

        binding.btnAktivasi.setOnClickListener {
            if (selectedTiketId != null && selectedIdPelanggan != null) {
                prosesAktivasiServer(selectedTiketId!!, selectedIdPelanggan!!)
            } else {
                Toast.makeText(this, "Pilih tiket terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadDataTiket() {
        db.collection("database_operasional")
            .whereEqualTo("antreanAktivasiNoc", true)
            .get()
            .addOnSuccessListener { documents ->
                listDataTiket.clear()
                val listId = mutableListOf<String>()

                for (document in documents) {
                    val idPelanggan = document.getString("idPelanggan") ?: ""
                    listDataTiket.add(Pair(document.id, idPelanggan))
                    listId.add(document.id)
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listId)
                binding.etTiket.setAdapter(adapter)
            }
    }

    private fun prosesAktivasiServer(tiketId: String, idPelanggan: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnAktivasi.isEnabled = false

        Handler(Looper.getMainLooper()).postDelayed({
            updateDatabasePelanggan(tiketId, idPelanggan)
        }, 1500)
    }

    private fun updateDatabasePelanggan(tiketId: String, idPelanggan: String) {
        db.collection("database_pelanggan").document(idPelanggan)
            .update("statusLayanan", "Aktif")
            .addOnSuccessListener {
                db.collection("database_operasional").document(tiketId)
                    .update("antreanAktivasiNoc", false)

                Toast.makeText(this, "Aktivasi Sukses! Membuka WhatsApp...", Toast.LENGTH_SHORT).show()
                kirimNotifikasiWA(idPelanggan)
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnAktivasi.isEnabled = true
                Toast.makeText(this, "Gagal memperbarui database", Toast.LENGTH_SHORT).show()
            }
    }

    private fun kirimNotifikasiWA(idPelanggan: String) {
        db.collection("database_pelanggan").document(idPelanggan).get()
            .addOnSuccessListener { document ->
                binding.progressBar.visibility = View.GONE
                binding.btnAktivasi.isEnabled = true

                if (document != null && document.exists()) {
                    val nama = document.getString("nama") ?: ""
                    var noWa = document.getString("noWhatsapp") ?: ""

                    if (noWa.startsWith("0")) {
                        noWa = "62" + noWa.substring(1)
                    }

                    val pesan = "Halo Bapak/Ibu $nama, layanan internet Anda dengan ID Pelanggan $idPelanggan telah aktif. Silakan mencoba koneksi Anda. Terima kasih."

                    try {
                        val url = "https://wa.me/$noWa?text=" + URLEncoder.encode(pesan, "UTF-8")
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(url)
                        startActivity(intent)

                        val logData = hashMapOf(
                            "idPelanggan" to idPelanggan,
                            "noWa" to noWa,
                            "status" to "Berhasil Membuka WA",
                            "timestamp" to System.currentTimeMillis()
                        )

                        db.collection("log_notifikasi").add(logData)
                        finish()

                    } catch (e: Exception) {
                        Toast.makeText(this, "Gagal membuka WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnAktivasi.isEnabled = true
                Toast.makeText(this, "Gagal mengambil data kontak pelanggan", Toast.LENGTH_SHORT).show()
            }
    }
}
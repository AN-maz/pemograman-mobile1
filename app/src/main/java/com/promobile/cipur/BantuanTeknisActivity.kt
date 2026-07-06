package com.promobile.cipur

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
import com.promobile.cipur.databinding.ActivityBantuanTeknisBinding

class BantuanTeknisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBantuanTeknisBinding
    private val db = FirebaseFirestore.getInstance()

    private val listPelangganDisplay = mutableListOf<String>()
    private var selectedIdPelanggan = ""

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

        loadDataPelanggan()

        binding.etPelanggan.setOnItemClickListener { parent, _, position, _ ->
            val textTerpilih = parent.getItemAtPosition(position).toString()
            selectedIdPelanggan = textTerpilih.substringBefore(" -")
            resetTampilan()
        }

        binding.btnCekKoneksi.setOnClickListener {
            val inputText = binding.etPelanggan.text.toString()
            if (selectedIdPelanggan.isEmpty() || !inputText.contains(selectedIdPelanggan)) {
                Toast.makeText(this, "Silakan pilih nama pelanggan dari daftar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            jalankanDiagnosisAwal()
        }

        binding.btnRestart.setOnClickListener {
            jalankanPemulihanJarakJauh()
        }

        binding.btnTutupLaporan.setOnClickListener {
            tutupLaporanNormal()
        }

        binding.btnTiketTeknisi.setOnClickListener {
            buatTiketGangguanFisik()
        }
    }

    private fun loadDataPelanggan() {
        db.collection("database_pelanggan").get()
            .addOnSuccessListener { documents ->
                listPelangganDisplay.clear()
                for (doc in documents) {
                    val nama = doc.getString("nama") ?: "Tanpa Nama"
                    listPelangganDisplay.add("${doc.id} - $nama")
                }
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listPelangganDisplay)
                binding.etPelanggan.setAdapter(adapter)
            }
    }

    private fun resetTampilan() {
        binding.layoutStatus.visibility = View.GONE
        binding.btnRestart.visibility = View.GONE
        binding.btnTutupLaporan.visibility = View.GONE
        binding.btnTiketTeknisi.visibility = View.GONE
    }

    private fun jalankanDiagnosisAwal() {
        binding.btnCekKoneksi.isEnabled = false
        binding.layoutStatus.visibility = View.VISIBLE
        binding.tvStatus.text = "Sedang menghubungi perangkat..."
        binding.tvStatus.setTextColor(android.graphics.Color.GRAY)

        Handler(Looper.getMainLooper()).postDelayed({
            binding.btnCekKoneksi.isEnabled = true

            // Simulasi Probabilitas Kasus
            val peluang = Math.random()

            if (peluang < 0.2) {
                // Exceptional Flow 3E: Gagal Komunikasi
                binding.tvStatus.text = "Error: Sistem gagal berkomunikasi dengan perangkat (Infrastruktur Terputus)"
                binding.tvStatus.setTextColor(android.graphics.Color.RED)

                binding.btnRestart.visibility = View.GONE
                binding.btnTiketTeknisi.visibility = View.VISIBLE
            } else {
                // Normal Flow: Perangkat Merespons
                binding.tvStatus.text = "Perangkat Online, namun terdeteksi penurunan kualitas (Redaman Tinggi)"
                binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#FF9800")) // Orange

                binding.btnRestart.visibility = View.VISIBLE
                binding.btnTiketTeknisi.visibility = View.GONE
            }
        }, 1500)
    }

    private fun jalankanPemulihanJarakJauh() {
        binding.btnRestart.isEnabled = false
        binding.tvStatus.text = "Sistem mengeksekusi instruksi pemulihan jarak jauh (Restarting...)"
        binding.tvStatus.setTextColor(android.graphics.Color.GRAY)

        Handler(Looper.getMainLooper()).postDelayed({
            val berhasilPulih = Math.random() > 0.4 // 60% peluang sukses

            if (berhasilPulih) {
                // Skenario 5: Layanan kembali normal
                binding.tvStatus.text = "Status: Layanan Kembali Normal / Stabil"
                binding.tvStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50")) // Hijau

                binding.btnRestart.visibility = View.GONE
                binding.btnTutupLaporan.visibility = View.VISIBLE
            } else {
                // Alternate Flow 4A: Masih gangguan
                binding.tvStatus.text = "Status: Pemulihan gagal, gangguan masih terjadi"
                binding.tvStatus.setTextColor(android.graphics.Color.RED)

                binding.btnRestart.visibility = View.GONE
                binding.btnTiketTeknisi.visibility = View.VISIBLE
            }
        }, 2500)
    }

    private fun tutupLaporanNormal() {
        val logData = hashMapOf(
            "idPelanggan" to selectedIdPelanggan,
            "aktivitas" to "Diagnosis & Restart Jarak Jauh Sukses",
            "timestamp" to System.currentTimeMillis()
        )

        // Skenario 6: Mencatat log operasional
        db.collection("log_operasional").add(logData)
            .addOnSuccessListener {
                Toast.makeText(this, "Laporan ditutup. Log operasional tersimpan.", Toast.LENGTH_LONG).show()
                finish()
            }
    }

    private fun buatTiketGangguanFisik() {
        val noTiket = "FSC-${System.currentTimeMillis().toString().takeLast(4)}"
        val dataTiket = hashMapOf(
            "idPelanggan" to selectedIdPelanggan,
            "statusPekerjaan" to "Open",
            "keterangan" to "Membutuhkan Pengecekan Fisik Lapangan",
            "sumber" to "Eskalasi CS Support",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("database_operasional").document(noTiket).set(dataTiket)
            .addOnSuccessListener {
                Toast.makeText(this, "Tiket diteruskan ke Teknisi: $noTiket", Toast.LENGTH_LONG).show()
                finish()
            }
    }
}
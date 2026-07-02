package com.promobile.cipur

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.databinding.ActivityCekOdpBinding

class CekOdpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCekOdpBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityCekOdpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()

        binding.btnCekPort.setOnClickListener {
            val kodeOdp = binding.etKodeOdp.text.toString().trim()
            val nomorPort = binding.etNomorPort.text.toString().trim()

            if (kodeOdp.isEmpty() || nomorPort.isEmpty()) {
                Toast.makeText(this, "Mohon isi Kode ODP and Nomor Port!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            cekKetersediaanPort(kodeOdp, nomorPort)
        }
    }

    private fun cekKetersediaanPort(kodeOdp: String, nomorPort: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnCekPort.isEnabled = false
        val namaFieldPort = "port_$nomorPort"

        db.collection("data_odp").document(kodeOdp).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val statusPort = document.getString(namaFieldPort)
                    if (statusPort == "Tersedia") {
                        alokasikanPort(kodeOdp, namaFieldPort)
                    } else {
                        binding.progressBar.visibility = View.GONE
                        binding.btnCekPort.isEnabled = true
                        Toast.makeText(this, "Gagal: Port $nomorPort berstatus '$statusPort'.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    binding.progressBar.visibility = View.GONE
                    binding.btnCekPort.isEnabled = true
                    Toast.makeText(this, "Kesalahan: Kode ODP tidak ditemukan!", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.btnCekPort.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun alokasikanPort(kodeOdp: String, namaFieldPort: String) {
        db.collection("data_odp").document(kodeOdp)
            .update(namaFieldPort, "Dialokasikan")
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.btnCekPort.isEnabled = true
                Toast.makeText(this, "Validasi ODP Berhasil! Port telah dialokasikan.", Toast.LENGTH_LONG).show()
                binding.etKodeOdp.text?.clear()
                binding.etNomorPort.text?.clear()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnCekPort.isEnabled = true
                Toast.makeText(this, "Gagal mengalokasikan port.", Toast.LENGTH_SHORT).show()
            }
    }
}
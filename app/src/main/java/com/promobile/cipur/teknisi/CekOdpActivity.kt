package com.promobile.cipur.teknisi

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.databinding.TeknisiActivityCekOdpBinding

class CekOdpActivity : AppCompatActivity() {

    private lateinit var binding: TeknisiActivityCekOdpBinding
    private val db = FirebaseFirestore.getInstance()
    private val TOTAL_PORT = 8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TeknisiActivityCekOdpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCekPort.setOnClickListener {
            val kodeOdp = binding.etKodeOdp.text.toString().trim().uppercase()
            val nomorPort = binding.etNomorPort.text.toString().trim()

            if (kodeOdp.isEmpty()) {
                binding.etKodeOdp.error = "Kode ODP wajib diisi!"
                return@setOnClickListener
            }

            prosesValidasiOdp(kodeOdp, nomorPort)
        }
    }

    private fun prosesValidasiOdp(kodeOdp: String, nomorPort: String) {
        // Tampilkan loading overlay
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.btnCekPort.isEnabled = false

        val odpRef = db.collection("database_odp").document(kodeOdp)

        odpRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // ODP sudah ada di database — baca data port
                    val dataPorts = mutableMapOf<String, String>()
                    for (i in 1..TOTAL_PORT) {
                        dataPorts["port$i"] = document.getString("port$i") ?: "KOSONG"
                    }

                    if (nomorPort.isNotEmpty()) {
                        // Teknisi ingin mengalokasikan port
                        alokasiPort(kodeOdp, nomorPort, dataPorts)
                    } else {
                        // Hanya tampilkan status ODP
                        tampilkanRingkasan(kodeOdp, dataPorts, false)
                        binding.loadingOverlay.visibility = View.GONE
                        binding.btnCekPort.isEnabled = true
                    }
                } else {
                    // ODP baru / belum terdaftar — buat document baru dengan 8 port KOSONG
                    buatOdpBaru(kodeOdp, nomorPort)
                }
            }
            .addOnFailureListener { e ->
                binding.loadingOverlay.visibility = View.GONE
                binding.btnCekPort.isEnabled = true
                Toast.makeText(this, "Gagal mengakses database: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun buatOdpBaru(kodeOdp: String, nomorPort: String) {
        val dataOdpBaru = hashMapOf<String, Any>()
        for (i in 1..TOTAL_PORT) {
            dataOdpBaru["port$i"] = "KOSONG"
        }
        dataOdpBaru["kapasitas"] = TOTAL_PORT

        db.collection("database_odp").document(kodeOdp)
            .set(dataOdpBaru)
            .addOnSuccessListener {
                Toast.makeText(this, "ODP Baru '$kodeOdp' berhasil didaftarkan ke database!", Toast.LENGTH_LONG).show()

                val dataPorts = mutableMapOf<String, String>()
                for (i in 1..TOTAL_PORT) {
                    dataPorts["port$i"] = "KOSONG"
                }

                if (nomorPort.isNotEmpty()) {
                    // Langsung alokasikan port jika teknisi mengisinya
                    alokasiPort(kodeOdp, nomorPort, dataPorts)
                } else {
                    tampilkanRingkasan(kodeOdp, dataPorts, true)
                    binding.loadingOverlay.visibility = View.GONE
                    binding.btnCekPort.isEnabled = true
                }
            }
            .addOnFailureListener { e ->
                binding.loadingOverlay.visibility = View.GONE
                binding.btnCekPort.isEnabled = true
                Toast.makeText(this, "Gagal mendaftarkan ODP baru: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun alokasiPort(kodeOdp: String, nomorPort: String, dataPorts: MutableMap<String, String>) {
        val portNumber = nomorPort.toIntOrNull()

        if (portNumber == null || portNumber < 1 || portNumber > TOTAL_PORT) {
            binding.loadingOverlay.visibility = View.GONE
            binding.btnCekPort.isEnabled = true
            Toast.makeText(this, "Nomor Port harus antara 1 - $TOTAL_PORT", Toast.LENGTH_SHORT).show()
            return
        }

        val portKey = "port$portNumber"
        val statusPort = dataPorts[portKey] ?: "KOSONG"

        if (statusPort == "TERPAKAI") {
            binding.loadingOverlay.visibility = View.GONE
            binding.btnCekPort.isEnabled = true
            Toast.makeText(this, "Port $portNumber sudah TERPAKAI! Pilih port lain.", Toast.LENGTH_LONG).show()
            tampilkanRingkasan(kodeOdp, dataPorts, false)
            return
        }

        // Update port menjadi TERPAKAI di Firestore
        db.collection("database_odp").document(kodeOdp)
            .update(portKey, "TERPAKAI")
            .addOnSuccessListener {
                dataPorts[portKey] = "TERPAKAI"
                tampilkanRingkasan(kodeOdp, dataPorts, false)
                binding.loadingOverlay.visibility = View.GONE
                binding.btnCekPort.isEnabled = true
                Toast.makeText(
                    this,
                    "Sukses mengalokasikan Port $portNumber pada $kodeOdp — Tersimpan ke database!",
                    Toast.LENGTH_LONG
                ).show()
            }
            .addOnFailureListener { e ->
                binding.loadingOverlay.visibility = View.GONE
                binding.btnCekPort.isEnabled = true
                Toast.makeText(this, "Gagal mengalokasikan port: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun tampilkanRingkasan(kodeOdp: String, dataPorts: Map<String, String>, isBaruDibuat: Boolean) {
        val portKosong = mutableListOf<Int>()
        val portTerpakai = mutableListOf<Int>()

        for (i in 1..TOTAL_PORT) {
            val status = dataPorts["port$i"] ?: "KOSONG"
            if (status == "TERPAKAI") {
                portTerpakai.add(i)
            } else {
                portKosong.add(i)
            }
        }

        val label = if (isBaruDibuat) "ODP Baru (Baru Didaftarkan)" else "ODP Terdaftar"

        val sb = StringBuilder()
        sb.append("$label (Kapasitas: $TOTAL_PORT Port)\n")

        if (portKosong.isNotEmpty()) {
            sb.append("• Port ${portKosong.joinToString(", ")} ➔ 🟢 KOSONG\n")
        }
        if (portTerpakai.isNotEmpty()) {
            sb.append("• Port ${portTerpakai.joinToString(", ")} ➔ 🔴 TERPAKAI\n")
        }

        sb.append("\n")
        when {
            portKosong.isEmpty() -> sb.append("⚠️ Kesimpulan: ODP FULL, semua port terpakai.")
            portTerpakai.isEmpty() -> sb.append("✅ Kesimpulan: Infrastruktur bersih, bebas pilih port.")
            portKosong.size == 1 -> sb.append("⚠️ Kesimpulan: Tersisa 1 port (Port ${portKosong[0]}). Hampir penuh!")
            else -> sb.append("✅ Kesimpulan: ${portKosong.size} port tersedia untuk instalasi.")
        }

        binding.tvOdpSummary.text = sb.toString()
    }
}
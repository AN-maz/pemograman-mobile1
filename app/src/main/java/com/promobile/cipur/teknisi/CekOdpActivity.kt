package com.promobile.cipur.teknisi

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.promobile.cipur.databinding.TeknisiActivityCekOdpBinding

class CekOdpActivity : AppCompatActivity() {

    private lateinit var binding: TeknisiActivityCekOdpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TeknisiActivityCekOdpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tombol menggunakan ID baru dari temanmu: btnCekPort
        binding.btnCekPort.setOnClickListener {
            val kodeOdp = binding.etKodeOdp.text.toString().trim()
            val nomorPort = binding.etNomorPort.text.toString().trim()

            if (kodeOdp.isEmpty()) {
                binding.etKodeOdp.error = "Kode ODP wajib diisi!"
                return@setOnClickListener
            }

            // Jalankan proses cek dengan visualisasi loading overlay
            prosesValidasiOdp(kodeOdp, nomorPort)
        }
    }

    private fun prosesValidasiOdp(kodeOdp: String, nomorPort: String) {
        // Tampilkan overlay abu-abu transparan + spinner loading
        binding.loadingOverlay.visibility = View.VISIBLE

        // Delay 1.5 detik agar seolah-olah sistem melakukan handshake jaringan ke ODP
        binding.btnCekPort.postDelayed({
            binding.loadingOverlay.visibility = View.GONE

            // Logika visualisasi pemetaan port berdasarkan input teknisi
            val ringkasanPort = when {
                kodeOdp.equals("ODP-01", ignoreCase = true) -> {
                    """
                    ODP Terdaftar (Kapasitas: 8 Port)
                    • Port 1, 4, 6, 8 ➔ 🟢 KOSONG
                    • Port 2, 3, 5, 7 ➔ 🔴 TERPAKAI
                    
                    Kesimpulan: Port 1 aman untuk instalasi baru.
                    """.trimIndent()
                }
                kodeOdp.equals("ODP-02", ignoreCase = true) -> {
                    """
                    ODP Terdaftar (Kapasitas: 8 Port)
                    • Port 1 s/d 7 ➔ 🔴 TERPAKAI FULL
                    • Port 8 ➔ 🟢 KOSONG (Sisa 1)
                    
                    Kesimpulan: Gunakan sisa Port 8 untuk penarikan kabel.
                    """.trimIndent()
                }
                else -> {
                    """
                    ODP Baru / Kosong (Kapasitas: 8 Port)
                    • Seluruh Port (1 s/d 8) ➔ 🟢 BELUM TERPAKAI
                    
                    Kesimpulan: Infrastruktur bersih, bebas pilih port.
                    """.trimIndent()
                }
            }

            // Set text ke ID teks summary dari layout temanmu: tvOdpSummary
            binding.tvOdpSummary.text = ringkasanPort

            // Berikan konfirmasi alokasi jika nomor port diisi oleh teknisi
            if (nomorPort.isNotEmpty()) {
                Toast.makeText(
                    this,
                    "Sukses mengalokasikan Port $nomorPort pada $kodeOdp",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(this, "Status ODP berhasil dimuat!", Toast.LENGTH_SHORT).show()
            }

        }, 1500)
    }
}
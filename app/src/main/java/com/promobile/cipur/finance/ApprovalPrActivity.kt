package com.promobile.cipur.finance

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.R
import com.promobile.cipur.databinding.FinanceActivityApprovalPrBinding

class ApprovalPrActivity : AppCompatActivity() {

    private lateinit var binding: FinanceActivityApprovalPrBinding
    private val db = FirebaseFirestore.getInstance()
    private val listDataPr = mutableListOf<Pair<String, Map<String, Any>>>()
    private var selectedDocId: String? = null
    private var selectedData: Map<String, Any>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = FinanceActivityApprovalPrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadDataPr()

        binding.etPr.setOnItemClickListener { _, _, position, _ ->
            val dataTerpilih = listDataPr[position]
            selectedDocId = dataTerpilih.first
            selectedData = dataTerpilih.second
            binding.etNamaBarang.setText(dataTerpilih.second["rincianBarang"].toString())
            binding.etQty.setText(dataTerpilih.second["jumlah"].toString())
        }

        binding.btnSetuju.setOnClickListener { updateStatus("Disetujui") }
        binding.btnTolak.setOnClickListener { updateStatus("Ditolak") }
        binding.btnRevisi.setOnClickListener { updateStatus("Butuh Revisi") }
    }

    private fun loadDataPr() {
        db.collection("pengajuan_finance")
            .whereEqualTo("statusApproval", "Menunggu Persetujuan Finance")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(this, "Gagal memuat data PR", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                listDataPr.clear()
                val listBarang = mutableListOf<String>()

                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        if (document.data != null) {
                            listDataPr.add(Pair(document.id, document.data!!))
                            val namaBarang = document.getString("rincianBarang") ?: ""
                            val jumlah = document.getLong("jumlah") ?: 0
                            listBarang.add("$namaBarang (Qty: $jumlah)")
                        }
                    }
                }

                val adapter = ArrayAdapter(this@ApprovalPrActivity, android.R.layout.simple_dropdown_item_1line, listBarang)
                binding.etPr.setAdapter(adapter)

                // Jika data kosong, beri tahu pengguna dan bersihkan form
                if (listBarang.isEmpty()) {
                    binding.etPr.setText("")
                    binding.etNamaBarang.setText("")
                    binding.etQty.setText("")
                    selectedDocId = null
                    selectedData = null
                }
            }
    }

    private fun updateStatus(status: String) {
        if (selectedDocId == null || selectedData == null) {
            Toast.makeText(this, "Pilih pengajuan terlebih dahulu!", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable semua tombol saat proses
        setButtonsEnabled(false)

        db.collection("pengajuan_finance").document(selectedDocId!!)
            .update("statusApproval", status)
            .addOnSuccessListener {
                if (status == "Disetujui") {
                    // Salin data ke antrean_logistik agar bisa dikirim oleh Logistik
                    simpanKeAntreanLogistik(selectedData!!)
                } else {
                    Toast.makeText(this, "Status diubah menjadi: $status", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                setButtonsEnabled(true)
                Toast.makeText(this, "Gagal mengubah status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun simpanKeAntreanLogistik(dataPr: Map<String, Any>) {
        val nomorManifes = "MNF-FIN-${System.currentTimeMillis().toString().takeLast(6)}"

        val namaBarang = dataPr["rincianBarang"]?.toString() ?: "-"
        val jumlah = (dataPr["jumlah"] as? Long)?.toInt()
            ?: dataPr["jumlah"].toString().toIntOrNull() ?: 0

        val dataAntrean = hashMapOf(
            "nomorManifes" to nomorManifes,
            "namaMaterial" to namaBarang,
            "jumlah" to jumlah,
            "statusAntrean" to "Disetujui Finance",
            "sumberPengajuan" to "Purchase Request (Finance)",
            "tanggalPersetujuan" to System.currentTimeMillis()
        )

        db.collection("antrean_logistik").document(nomorManifes)
            .set(dataAntrean)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "PR Disetujui & diteruskan ke Logistik!\nManifes: $nomorManifes",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .addOnFailureListener { e ->
                setButtonsEnabled(true)
                Toast.makeText(
                    this,
                    "Status disetujui tapi gagal meneruskan ke Logistik: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        binding.btnSetuju.isEnabled = enabled
        binding.btnTolak.isEnabled = enabled
        binding.btnRevisi.isEnabled = enabled
    }
}
package com.promobile.cipur.logistik

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.databinding.LogistikActivityBeliBinding

class BeliBarangActivity : AppCompatActivity() {

    private lateinit var binding: LogistikActivityBeliBinding
    private val db = FirebaseFirestore.getInstance()
    // Menyimpan data antrean beserta document ID Firestore-nya
    private val listDataAntrean = mutableListOf<Pair<String, Map<String, Any>>>()
    private var selectedIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LogistikActivityBeliBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadDataAntrean()

        // Saat item dipilih dari combobox, auto-fill nama barang dan qty
        binding.etBarangPr.setOnItemClickListener { _, _, position, _ ->
            selectedIndex = position
            val dataTerpilih = listDataAntrean[position]
            binding.etNamaBarang.setText(dataTerpilih.second["namaMaterial"].toString())
            binding.etQtyPr.setText(dataTerpilih.second["jumlah"].toString())
        }

        binding.btnKirimPr.setOnClickListener {
            val namaBarang = binding.etNamaBarang.text.toString().trim()
            val qty = binding.etQtyPr.text.toString().trim()

            // Validasi Input
            if (selectedIndex < 0 || selectedIndex >= listDataAntrean.size) {
                Toast.makeText(this, "Pilih barang terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (namaBarang.isEmpty() || qty.isEmpty()) {
                Toast.makeText(this, "Data barang belum lengkap!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            kirimKeFinance(namaBarang, qty.toIntOrNull() ?: 0)
        }
    }

    private fun loadDataAntrean() {
        // Ambil data dari antrean_logistik: barang yang belum ada di database / menunggu approval
        db.collection("antrean_logistik")
            .whereIn("statusAntrean", listOf(
                "Menunggu Persetujuan Logistik",
                "Disetujui Finance"
            ))
            .get()
            .addOnSuccessListener { documents ->
                listDataAntrean.clear()
                val listNamaBarang = mutableListOf<String>()

                for (document in documents) {
                    val data = document.data
                    val namaMaterial = data["namaMaterial"]?.toString() ?: "-"
                    val jumlah = data["jumlah"] ?: 0
                    val manifes = data["nomorManifes"]?.toString() ?: document.id

                    listDataAntrean.add(Pair(document.id, data))
                    listNamaBarang.add("$manifes — $namaMaterial (Qty: $jumlah)")
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    listNamaBarang
                )
                binding.etBarangPr.setAdapter(adapter)

                if (listNamaBarang.isEmpty()) {
                    Toast.makeText(this, "Tidak ada antrean barang untuk diajukan PR", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data antrean", Toast.LENGTH_SHORT).show()
            }
    }

    private fun kirimKeFinance(namaBarang: String, qty: Int) {
        binding.btnKirimPr.isEnabled = false
        binding.btnKirimPr.text = "Mengirim Data..."

        val dataPr = hashMapOf(
            "rincianBarang" to namaBarang,
            "jumlah" to qty,
            "statusApproval" to "Menunggu Persetujuan Finance",
            "tanggalPengajuan" to System.currentTimeMillis()
        )

        db.collection("pengajuan_finance")
            .add(dataPr)
            .addOnSuccessListener { docRef ->
                binding.btnKirimPr.isEnabled = true
                binding.btnKirimPr.text = "Kirim Ke Finance"

                // Update status di antrean_logistik agar tidak muncul lagi di combobox PR Logistik
                if (selectedIndex >= 0 && selectedIndex < listDataAntrean.size) {
                    val antreanId = listDataAntrean[selectedIndex].first
                    db.collection("antrean_logistik").document(antreanId)
                        .update("statusAntrean", "Diproses ke Finance")
                }

                Toast.makeText(
                    this,
                    "PR $namaBarang (Qty: $qty) berhasil diajukan ke Finance!\nID: ${docRef.id}",
                    Toast.LENGTH_LONG
                ).show()

                finish()
            }
            .addOnFailureListener { e ->
                binding.btnKirimPr.isEnabled = true
                binding.btnKirimPr.text = "Kirim Ke Finance"
                Toast.makeText(this, "Gagal mengirim PR: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
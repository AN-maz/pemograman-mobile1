package com.promobile.cipur

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.databinding.ActivitySiapBarangBinding

class SiapBarangActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySiapBarangBinding
    private val db = FirebaseFirestore.getInstance()
    private val listDataManifes = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySiapBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadDataAntrean()

        binding.etManifes.setOnItemClickListener { _, _, position, _ ->
            val dataTerpilih = listDataManifes[position]
            binding.etNamaBarang.setText(dataTerpilih["namaMaterial"].toString())
            binding.etQty.setText(dataTerpilih["jumlah"].toString())
        }

        binding.btnProsesPenyerahan.setOnClickListener {
            val manifes = binding.etManifes.text.toString().trim()
            val namaBarang = binding.etNamaBarang.text.toString().trim()
            val qty = binding.etQty.text.toString().toIntOrNull() ?: 0

            if (manifes.isEmpty() || namaBarang.isEmpty() || qty <= 0) {
                Toast.makeText(this, "Data tidak lengkap atau belum dipilih", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prosesStokBarang(manifes, namaBarang, qty)
        }
    }

    private fun loadDataAntrean() {
        db.collection("antrean_logistik")
            .whereEqualTo("statusAntrean", "Menunggu Persetujuan Logistik")
            .get()
            .addOnSuccessListener { documents ->
                listDataManifes.clear()
                val nomorManifesList = mutableListOf<String>()

                for (document in documents) {
                    val data = document.data
                    listDataManifes.add(data)
                    data["nomorManifes"]?.let { nomorManifesList.add(it.toString()) }
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nomorManifesList)
                binding.etManifes.setAdapter(adapter)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal menarik data antrean", Toast.LENGTH_SHORT).show()
            }
    }

    private fun prosesStokBarang(manifes: String, barang: String, qtyKeluar: Int) {
        binding.btnProsesPenyerahan.isEnabled = false

        db.collection("database_inventaris").document(barang).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val stokSaatIni = document.getLong("stok")?.toInt() ?: 0

                    if (stokSaatIni < qtyKeluar) {
                        Toast.makeText(this, "Peringatan: Stok $barang tersisa $stokSaatIni, tidak mencukupi!", Toast.LENGTH_LONG).show()
                        binding.btnProsesPenyerahan.isEnabled = true
                    } else {
                        val stokTerbaru = stokSaatIni - qtyKeluar
                        updateStokDatabase(manifes, barang, stokTerbaru)
                    }
                } else {
                    Toast.makeText(this, "Error: $barang tidak terdaftar di database inventaris", Toast.LENGTH_LONG).show()
                    binding.btnProsesPenyerahan.isEnabled = true
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Kesalahan sistem/database", Toast.LENGTH_SHORT).show()
                binding.btnProsesPenyerahan.isEnabled = true
            }
    }

    private fun updateStokDatabase(manifes: String, barang: String, stokTerbaru: Int) {
        db.collection("database_inventaris").document(barang)
            .update("stok", stokTerbaru)
            .addOnSuccessListener {
                db.collection("antrean_logistik").document(manifes)
                    .update("statusAntrean", "Selesai Diserahkan")

                Toast.makeText(this, "Penyerahan sukses & Stok Terupdate!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Rollback: Gagal memperbarui stok", Toast.LENGTH_SHORT).show()
                binding.btnProsesPenyerahan.isEnabled = true
            }
    }
}
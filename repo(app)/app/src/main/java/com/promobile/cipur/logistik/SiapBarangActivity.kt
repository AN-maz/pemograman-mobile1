package com.promobile.cipur.logistik

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.R
import com.promobile.cipur.databinding.LogistikActivitySiapBarangBinding

class SiapBarangActivity : AppCompatActivity() {

    private lateinit var binding: LogistikActivitySiapBarangBinding
    private val db = FirebaseFirestore.getInstance()

    private val listDataInventaris = mutableListOf<Pair<String, Long>>()
    private var selectedIndex: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = LogistikActivitySiapBarangBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadDataInventaris()

        binding.etManifes.setOnItemClickListener { _, _, position, _ ->
            selectedIndex = position
            val dataTerpilih = listDataInventaris[position]
            binding.etNamaBarang.setText(dataTerpilih.first)
            binding.etQty.setText(dataTerpilih.second.toString())
        }

        binding.btnProsesPenyerahan.setOnClickListener {
            val namaBarang = binding.etNamaBarang.text.toString().trim()
            val qty = binding.etQty.text.toString().toIntOrNull() ?: 0

            if (selectedIndex < 0 || selectedIndex >= listDataInventaris.size) {
                Toast.makeText(this, "Pilih barang terlebih dahulu dari daftar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (namaBarang.isEmpty() || qty <= 0) {
                Toast.makeText(this, "Data tidak lengkap atau belum dipilih", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prosesStokBarang(namaBarang, qty)
        }
    }

    private fun loadDataInventaris() {
        // Ambil data dari database_inventaris: barang yang sudah tersimpan di database
        db.collection("database_inventaris")
            .get()
            .addOnSuccessListener { documents ->
                listDataInventaris.clear()
                val listNamaBarang = mutableListOf<String>()

                for (document in documents) {
                    val namaBarang = document.id
                    val stok = document.getLong("stok") ?: 0

                    listDataInventaris.add(Pair(namaBarang, stok))
                    listNamaBarang.add("$namaBarang (Stok: $stok)")
                }

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    listNamaBarang
                )
                binding.etManifes.setAdapter(adapter)

                if (listNamaBarang.isEmpty()) {
                    Toast.makeText(this, "Database inventaris kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data inventaris", Toast.LENGTH_SHORT).show()
            }
    }

    private fun prosesStokBarang(barang: String, qtyKeluar: Int) {
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
                        updateStokDatabase(barang, stokTerbaru)
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

    private fun updateStokDatabase(barang: String, stokTerbaru: Int) {
        db.collection("database_inventaris").document(barang)
            .update("stok", stokTerbaru)
            .addOnSuccessListener {
                Toast.makeText(this, "Penyerahan sukses & Stok Terupdate!", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Rollback: Gagal memperbarui stok", Toast.LENGTH_SHORT).show()
                binding.btnProsesPenyerahan.isEnabled = true
            }
    }
}


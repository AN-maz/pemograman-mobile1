package com.promobile.cipur.cs

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.R
import com.promobile.cipur.databinding.CsActivityCariUpdateBinding

class CariUpdatePelangganActivity : AppCompatActivity() {

    private lateinit var binding: CsActivityCariUpdateBinding
    private val db = FirebaseFirestore.getInstance()

    private val masterListPelanggan = mutableListOf<Pelanggan>()
    private val displayListPelanggan = mutableListOf<Pelanggan>() 
    private lateinit var adapter: PelangganAdapter

    private var currentIdPelanggan = ""
    private var isFormatting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = CsActivityCariUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.etSearch.filters = arrayOf(InputFilter.AllCaps())
        adapter = PelangganAdapter(displayListPelanggan) { pelangganTerpilih ->
            tampilkanFormUpdate(pelangganTerpilih)
        }
        binding.rvPelanggan.layoutManager = LinearLayoutManager(this)
        binding.rvPelanggan.adapter = adapter

        loadSemuaDataPelanggan()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isFormatting) return

                val teks = s.toString()

                if (teks.isNotEmpty() && teks[0].isDigit()) {
                    isFormatting = true
                    val teksOtomatis = "PLG-$teks"
                    binding.etSearch.setText(teksOtomatis)
                    binding.etSearch.setSelection(teksOtomatis.length)
                    isFormatting = false
                    filterData(teksOtomatis)
                } else {
                    filterData(teks)
                }
            }
        })

        binding.btnUpdate.setOnClickListener {
            simpanPembaruanData()
        }
    }

    private fun loadSemuaDataPelanggan() {
        db.collection("database_pelanggan").get()
            .addOnSuccessListener { documents ->
                masterListPelanggan.clear()
                displayListPelanggan.clear()

                for (doc in documents) {
                    val pelanggan = Pelanggan(
                        id = doc.id,
                        nama = doc.getString("nama") ?: "",
                        noWhatsapp = doc.getString("noWhatsapp") ?: "",
                        alamat = doc.getString("alamat") ?: ""
                    )
                    masterListPelanggan.add(pelanggan)
                }

                displayListPelanggan.addAll(masterListPelanggan)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data pelanggan", Toast.LENGTH_SHORT).show()
            }
    }
    private fun filterData(query: String) {
        displayListPelanggan.clear()

        if (query.isEmpty()) {
            displayListPelanggan.addAll(masterListPelanggan)
            binding.layoutData.visibility = View.GONE
        } else {
            val q = query.uppercase()
            for (pelanggan in masterListPelanggan) {
                if (pelanggan.id.uppercase().contains(q) || pelanggan.nama.uppercase().contains(q)) {
                    displayListPelanggan.add(pelanggan)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun tampilkanFormUpdate(pelanggan: Pelanggan) {
        binding.layoutData.visibility = View.VISIBLE
        currentIdPelanggan = pelanggan.id

        binding.etNama.setText(pelanggan.nama)
        binding.etWa.setText(pelanggan.noWhatsapp)
        binding.etAlamat.setText(pelanggan.alamat)
    }

    private fun simpanPembaruanData() {
        val nama = binding.etNama.text.toString().trim()
        val wa = binding.etWa.text.toString().trim()

        if (nama.isEmpty() || wa.isEmpty()) {
            Toast.makeText(this, "Lengkapi format nama dan nomor WA", Toast.LENGTH_SHORT).show()
            return
        }

        val updateData = mapOf(
            "nama" to nama,
            "noWhatsapp" to wa,
            "alamat" to binding.etAlamat.text.toString().trim()
        )

        db.collection("database_pelanggan").document(currentIdPelanggan)
            .update(updateData)
            .addOnSuccessListener {
                Toast.makeText(this, "Pembaruan profil sukses!", Toast.LENGTH_SHORT).show()
                binding.layoutData.visibility = View.GONE
                binding.etSearch.text?.clear()
                loadSemuaDataPelanggan()
            }
    }
}
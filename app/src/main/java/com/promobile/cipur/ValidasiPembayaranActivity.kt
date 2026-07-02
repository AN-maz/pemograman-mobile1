package com.promobile.cipur

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.databinding.ActivityValidasiPembayaranBinding

class ValidasiPembayaranActivity : AppCompatActivity() {

    private lateinit var binding: ActivityValidasiPembayaranBinding
    private val db = FirebaseFirestore.getInstance()
    private val listDataTagihan = mutableListOf<Pair<String, Map<String, Any>>>()
    private var selectedTagihanId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityValidasiPembayaranBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadDataTagihan()

        binding.etTagihan.setOnItemClickListener { _, _, position, _ ->
            val dataTerpilih = listDataTagihan[position]
            selectedTagihanId = dataTerpilih.first
            binding.etPelanggan.setText(dataTerpilih.second["pelanggan"].toString())
            binding.etNominal.setText(dataTerpilih.second["nominal"].toString())
        }

        binding.btnValidasi.setOnClickListener { prosesValidasi("Lunas") }
        binding.btnTolak.setOnClickListener { prosesValidasi("Belum Valid") }
    }

    private fun loadDataTagihan() {
        db.collection("database_keuangan")
            .whereEqualTo("statusPembayaran", "Menunggu Validasi")
            .get()
            .addOnSuccessListener { documents ->
                listDataTagihan.clear()
                val listId = mutableListOf<String>()

                for (document in documents) {
                    listDataTagihan.add(Pair(document.id, document.data))
                    listId.add(document.id)
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listId)
                binding.etTagihan.setAdapter(adapter)
            }
    }

    private fun prosesValidasi(status: String) {
        if (selectedTagihanId == null) return

        db.collection("database_keuangan").document(selectedTagihanId!!)
            .update("statusPembayaran", status)
            .addOnSuccessListener {
                Toast.makeText(this, "Validasi Berhasil: $status", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
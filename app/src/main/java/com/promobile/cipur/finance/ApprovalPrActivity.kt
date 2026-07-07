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
            binding.etQty.setText(dataTerpilih.second["jumlah"].toString())
        }

        binding.btnSetuju.setOnClickListener { updateStatus("Disetujui") }
        binding.btnTolak.setOnClickListener { updateStatus("Ditolak") }
        binding.btnRevisi.setOnClickListener { updateStatus("Butuh Revisi") }
    }

    private fun loadDataPr() {
        db.collection("pengajuan_finance")
            .whereEqualTo("statusApproval", "Menunggu Persetujuan Finance")
            .get()
            .addOnSuccessListener { documents ->
                listDataPr.clear()
                val listBarang = mutableListOf<String>()

                for (document in documents) {
                    listDataPr.add(Pair(document.id, document.data))
                    listBarang.add(document.getString("rincianBarang") ?: "")
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listBarang)
                binding.etPr.setAdapter(adapter)
            }
    }

    private fun updateStatus(status: String) {
        if (selectedDocId == null) return

        db.collection("pengajuan_finance").document(selectedDocId!!)
            .update("statusApproval", status)
            .addOnSuccessListener {
                Toast.makeText(this, "Status diubah menjadi: $status", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
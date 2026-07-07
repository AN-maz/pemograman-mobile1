package com.promobile.cipur.logistik

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.R
import com.promobile.cipur.databinding.LogistikActivityBeliBinding

class BeliBarangActivity : AppCompatActivity() {

    private lateinit var binding: LogistikActivityBeliBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = LogistikActivityBeliBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnKirimPr.setOnClickListener {
            val barang = binding.etBarangPr.text.toString().trim()
            val qty = binding.etQtyPr.text.toString().toIntOrNull() ?: 0

            if (barang.isEmpty() || qty <= 0) {
                Toast.makeText(this, "Mohon lengkapi informasi PR!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            kirimKeFinance(barang, qty)
        }
    }

    private fun kirimKeFinance(barang: String, qty: Int) {
        binding.btnKirimPr.isEnabled = false

        val dataPr = hashMapOf(
            "rincianBarang" to barang,
            "jumlah" to qty,
            "statusApproval" to "Menunggu Persetujuan Finance",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("pengajuan_finance").add(dataPr)
            .addOnSuccessListener {
                Toast.makeText(this, "PR Berhasil dikirim ke Finance", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal mengirim PR", Toast.LENGTH_SHORT).show()
                binding.btnKirimPr.isEnabled = true
            }
    }
}
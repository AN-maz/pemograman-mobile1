package com.promobile.cipur.finance

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.promobile.cipur.R
import com.promobile.cipur.databinding.FinanceActivityMainBinding

class FinanceActivity : AppCompatActivity() {

    private lateinit var binding: FinanceActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = FinanceActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnMenuValidasi.setOnClickListener {
            startActivity(Intent(this, ValidasiPembayaranActivity::class.java))
        }

        binding.btnMenuApproval.setOnClickListener {
            startActivity(Intent(this, ApprovalPrActivity::class.java))
        }
    }
}
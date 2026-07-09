package com.promobile.cipur.teknisi

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.promobile.cipur.teknisi.CekOdpActivity
import com.promobile.cipur.teknisi.InstalasiActivity
import com.promobile.cipur.teknisi.MintaBarangActivity
import com.promobile.cipur.R
import com.promobile.cipur.databinding.TeknisiActivityMainBinding

class TeknisiActivity : AppCompatActivity() {

    private lateinit var binding: TeknisiActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = TeknisiActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnMenuOdp.setOnClickListener {
            startActivity(Intent(this, CekOdpActivity::class.java))
        }
        binding.btnMenuMintaBarang.setOnClickListener {
            startActivity(Intent(this, MintaBarangActivity::class.java))
        }
        binding.btnMenuInstalasi.setOnClickListener {
            startActivity(Intent(this, InstalasiActivity::class.java))
        }
    }
}
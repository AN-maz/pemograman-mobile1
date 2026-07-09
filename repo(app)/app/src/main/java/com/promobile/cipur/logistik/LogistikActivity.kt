package com.promobile.cipur.logistik

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.promobile.cipur.R
import com.promobile.cipur.databinding.LogistikActivityMainBinding

class LogistikActivity : AppCompatActivity() {

    private lateinit var binding: LogistikActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = LogistikActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnMenuMonitoring.setOnClickListener {
            startActivity(Intent(this, MonitoringLogistikActivity::class.java))
        }

        binding.btnMenuSiapBarang.setOnClickListener {
            startActivity(Intent(this, SiapBarangActivity::class.java))
        }

        binding.btnMenuBeliBarang.setOnClickListener {
            startActivity(Intent(this, BeliBarangActivity::class.java))
        }
    }
}
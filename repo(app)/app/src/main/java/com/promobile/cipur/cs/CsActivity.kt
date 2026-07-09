package com.promobile.cipur.cs

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.promobile.cipur.cs.BantuanTeknisActivity
import com.promobile.cipur.cs.CariUpdatePelangganActivity
import com.promobile.cipur.R
import com.promobile.cipur.cs.TiketKeluhanActivity
import com.promobile.cipur.databinding.CsActivityMainBinding

class CsActivity : AppCompatActivity() {
    private lateinit var binding: CsActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = CsActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnMenuCariUpdate.setOnClickListener {
            startActivity(Intent(this, CariUpdatePelangganActivity::class.java))
        }
        binding.btnMenuKeluhan.setOnClickListener {
            startActivity(Intent(this, TiketKeluhanActivity::class.java))
        }
        binding.btnMenuBantuan.setOnClickListener {
            startActivity(Intent(this, BantuanTeknisActivity::class.java))
        }
    }
}
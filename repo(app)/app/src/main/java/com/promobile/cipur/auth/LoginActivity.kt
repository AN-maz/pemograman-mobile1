package com.promobile.cipur.auth

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import com.google.firebase.firestore.FirebaseFirestore
import com.promobile.cipur.cs.CsActivity
import com.promobile.cipur.databinding.AuthActivityLoginBinding
import com.promobile.cipur.finance.FinanceActivity
import com.promobile.cipur.logistik.LogistikActivity
import com.promobile.cipur.noc.NocActivity
import com.promobile.cipur.teknisi.TeknisiActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: AuthActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = AuthActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        binding.btnLogin.setOnClickListener {
            prosesLoginPegawai()
        }
    }

    private fun prosesLoginPegawai() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email tidak boleh kosong"
            return
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            return
        } else {
            binding.tilPassword.error = null
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE
                binding.btnLogin.isEnabled = true

                if (task.isSuccessful) {
                    Toast.makeText(this, "Autentikasi Berhasil!", Toast.LENGTH_SHORT).show()

                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        binding.progressBar.visibility = View.VISIBLE
                        val db = FirebaseFirestore.getInstance()
                        db.collection("pegawai").document(userId).get()
                            .addOnSuccessListener { document ->
                                binding.progressBar.visibility = View.GONE

                                if (document != null && document.exists()) {
                                    val divisi = document.getString("divisi")
                                    val intent = when (divisi) {
                                        "Teknisi" -> Intent(this, TeknisiActivity::class.java)
                                        "Logistik" -> Intent(this, LogistikActivity::class.java)
                                        "Finance" -> Intent(this, FinanceActivity::class.java)
                                        "NOC" -> Intent(this, NocActivity::class.java)
                                        "Customer Service" -> Intent(this, CsActivity::class.java)
                                        else -> null
                                    }

                                    if (intent != null) {
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Divisi tidak dikenali!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this, "Data pegawai tidak ditemukan di database!", Toast.LENGTH_LONG).show()
                                }
                            }
                            .addOnFailureListener { exception ->
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this, "Gagal mengambil data: ${exception.message}", Toast.LENGTH_LONG).show()
                            }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Login Gagal: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}

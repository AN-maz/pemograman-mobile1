package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup View Binding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Instance Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Set up listener untuk tombol login
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
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Autentikasi Berhasil!", Toast.LENGTH_SHORT).show()

                        // 1. Ambil UID pengguna yang sedang login
                        val userId = auth.currentUser?.uid

                        if (userId != null) {
                            // Tampilkan loading kembali saat mengambil data dari database
                            binding.progressBar.visibility = View.VISIBLE

                            // 2. Akses Cloud Firestore ke koleksi "Pegawai" berdasarkan ID dokumen (UID)
                            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            db.collection("Pegawai").document(userId).get()
                                .addOnSuccessListener { document ->
                                    binding.progressBar.visibility = View.GONE

                                    if (document != null && document.exists()) {
                                        // 3. Ambil string data dari field "divisi"
                                        val divisi = document.getString("divisi")

                                        // 4. Logika Routing menggunakan "when" sesuai Class Diagram
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
                                            finish() // Menutup LoginActivity agar tidak bisa di-back kembali
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
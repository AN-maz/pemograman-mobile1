package com.promobile.cipur.cs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.promobile.cipur.R

data class Pelanggan(
    val id: String,
    val nama: String,
    val noWhatsapp: String,
    val alamat: String
)

class PelangganAdapter(
    private val listPelanggan: List<Pelanggan>,
    private val onClick: (Pelanggan) -> Unit
) : RecyclerView.Adapter<PelangganAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvId: TextView = view.findViewById(R.id.tvItemId)
        val tvNama: TextView = view.findViewById(R.id.tvItemNama)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cs_item_pelanggan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pelanggan = listPelanggan[position]
        holder.tvId.text = pelanggan.id
        holder.tvNama.text = pelanggan.nama

        holder.itemView.setOnClickListener {
            onClick(pelanggan)
        }
    }

    override fun getItemCount() = listPelanggan.size
}
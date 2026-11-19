package com.example.listcontact3

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.listcontact3.databinding.ItemContactBinding

class ContactAdapter(
    private var contacts: List<Contact>,
    private val onItemClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        val fullName = "${contact.nom} ${contact.prenom}".trim()
        holder.binding.tvContactName.text = fullName
        holder.binding.tvContactPhone.text = contact.telephone

        if (!contact.photoUri.isNullOrEmptyUri()) {
            val uri = Uri.parse(contact.photoUri)
            holder.binding.ivContactPhoto.load(uri) {
                placeholder(android.R.drawable.ic_menu_camera)
                error(android.R.drawable.ic_menu_camera)
            }
        } else {
            holder.binding.ivContactPhoto.setImageResource(android.R.drawable.ic_menu_camera)
        }

        holder.itemView.setOnClickListener {
            onItemClick(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun updateData(newContacts: List<Contact>) {
        this.contacts = newContacts
        notifyDataSetChanged()
    }

    private fun String?.isNullOrEmptyUri(): Boolean {
        return this.isNullOrBlank()
    }
}

package com.example.listcontact3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactsViewModel : ViewModel() {

    private val _contactsList = mutableListOf<Contact>()
    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> = _contacts

    init {
        loadDemoContacts()
    }

    private fun loadDemoContacts() {
        _contactsList.addAll(
            listOf(
                Contact(
                    nom = "Dupont",
                    prenom = "Jean",
                    telephone = "0612345678",
                    email = "jean.dupont@email.com",
                    latitude = 48.8566,
                    longitude = 2.3522
                ),
                Contact(
                    nom = "Curie",
                    prenom = "Marie",
                    telephone = "0698765432",
                    email = "marie.curie@email.com",
                    latitude = 45.7640,
                    longitude = 4.8357
                ),
                Contact(
                    nom = "Pesquet",
                    prenom = "Thomas",
                    telephone = "0611223344",
                    email = "thomas.pesquet@email.com",
                    latitude = 43.6047,
                    longitude = 1.4442
                ),
                Contact(
                    nom = "Martin",
                    prenom = "Sophie",
                    telephone = "0655443322",
                    email = "sophie.martin@email.com",
                    latitude = null,
                    longitude = null
                )
            )
        )
        _contacts.value = ArrayList(_contactsList)
    }

    fun getContactById(id: String): Contact? {
        return _contactsList.find { it.id == id }
    }

    fun saveContact(contact: Contact) {
        val index = _contactsList.indexOfFirst { it.id == contact.id }
        if (index >= 0) {
            _contactsList[index] = contact
        } else {
            _contactsList.add(contact)
        }
        _contacts.value = ArrayList(_contactsList)
    }

    fun deleteContact(id: String) {
        _contactsList.removeAll { it.id == id }
        _contacts.value = ArrayList(_contactsList)
    }
}

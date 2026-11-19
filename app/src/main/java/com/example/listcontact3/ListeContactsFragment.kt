package com.example.listcontact3

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.listcontact3.databinding.FragmentListeContactsBinding

class ListeContactsFragment : Fragment() {

    private var _binding: FragmentListeContactsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ContactsViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter

    private var allContacts: List<Contact> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListeContactsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchFilter()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = ContactAdapter(emptyList()) { contact ->
            openDetailFragment(contact.id)
        }
        binding.rvContacts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvContacts.adapter = adapter
    }

    private fun setupSearchFilter() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterContacts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFab() {
        binding.fabAddContact.setOnClickListener {
            openDetailFragment(null)
        }
    }

    private fun observeViewModel() {
        viewModel.contacts.observe(viewLifecycleOwner) { contactsList ->
            allContacts = contactsList
            filterContacts(binding.etSearch.text.toString())
        }
    }

    private fun filterContacts(query: String) {
        val trimmedQuery = query.trim().lowercase()
        val filteredList = if (trimmedQuery.isEmpty()) {
            allContacts
        } else {
            allContacts.filter { contact ->
                contact.nom.lowercase().contains(trimmedQuery) ||
                        contact.prenom.lowercase().contains(trimmedQuery) ||
                        contact.telephone.lowercase().contains(trimmedQuery)
            }
        }

        adapter.updateData(filteredList)

        if (filteredList.isEmpty()) {
            binding.rvContacts.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvContacts.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    private fun openDetailFragment(contactId: String?) {
        parentFragmentManager.commit {
            replace(R.id.fragment_container, DetailContactFragment.newInstance(contactId))
            addToBackStack(null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

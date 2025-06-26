package com.example.cakeorderingapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.cakeorderingapp.data.CakeFlavour
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FlavourViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _flavours = MutableStateFlow<List<CakeFlavour>>(emptyList())
    val flavours: StateFlow<List<CakeFlavour>> = _flavours
    internal val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun addFlavour(flavour: CakeFlavour) {
        val newFlavour = flavour.copy(id = db.collection("flavours").document().id)
        db.collection("flavours").document(newFlavour.id).set(newFlavour)
            .addOnSuccessListener {
                fetchFlavours()
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to add flavour: ${e.message}"
            }
    }

    fun fetchFlavours() {
        db.collection("flavours").get()
            .addOnSuccessListener { result ->
                _flavours.value = result.documents.mapNotNull { doc ->
                    doc.toObject(CakeFlavour::class.java)?.copy(id = doc.id)
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to fetch flavours: ${e.message}"
            }
    }
}

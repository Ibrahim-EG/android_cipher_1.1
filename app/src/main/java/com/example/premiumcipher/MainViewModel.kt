package com.example.premiumcipher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    var unlocked by mutableStateOf(false)
        private set

    private var passphrase: String? = null

    fun unlock(rawPassphrase: String) {
        passphrase = CryptoEngine.normalizePassphrase(rawPassphrase)
        unlocked = true
    }

    fun lock() {
        passphrase = null
        unlocked = false
    }

    fun <T> withPassphrase(block: (String) -> T): T {
        val p = passphrase ?: throw CryptoException("Session is locked.")
        return block(p)
    }
}

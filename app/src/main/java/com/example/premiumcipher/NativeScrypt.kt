package com.example.premiumcipher

object NativeScrypt {
    init {
        System.loadLibrary("premiumscrypt")
    }

    external fun scryptNative(
        pass: ByteArray,
        salt: ByteArray,
        N: Long,
        r: Int,
        p: Int,
        dkLen: Int
    ): ByteArray?
}

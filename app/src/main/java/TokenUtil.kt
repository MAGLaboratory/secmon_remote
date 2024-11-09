package com.MAGLab.secmon_remote

import android.util.Base64
import android.util.Log
import java.nio.ByteBuffer
import java.util.zip.CRC32

class TokenUtil {
    // this is adapted from the code on the maglab repository
    // https://github.com/MAGLaboratory/token_generator/blob/zero/decode.py
    companion object {
        const val START = "magld_"
        const val MINCTLEN = 2
        const val B64CRCLEN = 6
        fun extractToken(wholeToken: String): ByteArray {
            // length verification
            require(wholeToken.length >= START.length + MINCTLEN + B64CRCLEN) { "Invalid token length" }

            // header verification
            require(
                wholeToken.substring(0, START.length).uppercase() == START.uppercase()
            ) { "Invalid token header" }

            val centralToken = wholeToken.substring(START.length, wholeToken.length - B64CRCLEN)
            val centralTokenBytes =
                Base64.decode(centralToken.toByteArray(Charsets.UTF_8), Base64.NO_PADDING)
            return centralTokenBytes
        }
        fun checkToken(wholeToken: String): ByteArray {
            val centralTokenBytes: ByteArray
            try
            {
                val cNUMBYTESCRC32 = 4
                centralTokenBytes = extractToken(wholeToken)
                val endChecksum = wholeToken.substring(wholeToken.length - B64CRCLEN)

                val crc32 = CRC32()
                crc32.reset()
                crc32.update(centralTokenBytes)
                // by default, the crc32 function outputs a "Long" which is actually 8 bytes instead of the standard 4
                // we take the last 4 bytes because that is where the data is encoded...  this is
                // reversed because this part is encoded as little-endian (instead of the big-endian that base64 defaults to)
                val checksumBytes =
                    ByteBuffer.allocate(Long.SIZE_BYTES).putLong(crc32.value).array().takeLast(cNUMBYTESCRC32)
                        .reversed().toByteArray()
                val calcChecksum = Base64.encodeToString(checksumBytes, Base64.NO_PADDING).trimEnd()

                require(calcChecksum == endChecksum) { "Checksum Incorrect" }
            } catch (e: IllegalArgumentException)
            {
                Log.i("TokenDecode", e.message.toString())
                return byteArrayOf()
            }
            return centralTokenBytes
        }
    }
}
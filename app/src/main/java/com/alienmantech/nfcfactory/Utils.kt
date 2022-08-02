package com.alienmantech.nfcfactory

import android.content.Intent
import android.nfc.*
import android.nfc.tech.Ndef
import android.util.Log
import com.alienmantech.nfcfactory.models.CustomNfcTag
import com.alienmantech.nfcfactory.models.NfcTag
import com.google.gson.Gson
import org.json.JSONException
import java.io.IOException
import java.lang.NumberFormatException
import java.nio.charset.Charset

class Utils {
    companion object {
        private const val TAG = "NFC Creator"

        private const val appPackage = "com.alienmantech.maroonnova"
        private const val mimeType = "application/vnd.at-equipcheck+json"

        fun readNfcTag(intent: Intent): NfcTag {
            val nfcTag = NfcTag()

            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                nfcTag.id = tag.id.toHexString()
            }

            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMessages != null) {
                val messages = arrayOfNulls<NdefMessage>(rawMessages.size)
                for (i in rawMessages.indices) {

                    val nfcMessage = NfcTag.Message()

                    messages[i] = rawMessages[i] as NdefMessage

                    messages[i]?.records?.let { records ->
                        for (record in records) {
                            val nfcRecord = NfcTag.Message.Record()

                            record.toMimeType()?.let { mime ->
                                nfcRecord.mime = mime
                            }

                            record.payload?.let { payload ->
                                nfcRecord.payload = String(payload, Charset.forName("US-ASCII"))
                            }

                            nfcMessage.addRecord(nfcRecord)
                        }
                    }

                    nfcTag.addMessage(nfcMessage)
                }
            }

            return nfcTag
        }

        fun writeNfcTag(intent: Intent, id: String): Boolean {
            // get the tag we just scanned
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return false
            // create the main record
            val mainRecord = createMainRecord(id) ?: return false
            // create the record that launches our app on the store if they don't have it
            val aarRecord = createAarRecord()
            // put together as a message
            val msg = NdefMessage(arrayOf(mainRecord, aarRecord))
            // write and return result
            return write(tag, msg)
        }

        private fun createMainRecord(id: String): NdefRecord? {
            val tag = CustomNfcTag(
                version = 1,
                id = id
            )

            val jTag = Gson().toJson(tag)

            return try {
                NdefRecord.createMime(
                    mimeType,
                    jTag.toByteArray(Charset.forName("US-ASCII"))
                )
            } catch (e: JSONException) {
                null
            }
        }

        private fun createAarRecord(): NdefRecord? {
            return NdefRecord.createApplicationRecord(appPackage)
        }

        /**
         * Does the real write to the tag.
         *
         * @param tag Tag - The tag
         * @param message NdefMessage - The message to write
         * @return boolean - Success or fail
         */
        private fun write(tag: Tag, message: NdefMessage): Boolean {
            val ndef = Ndef.get(tag)
            try {
                ndef.connect()
                if (ndef.maxSize < message.byteArrayLength) {
                    return false
                }
                if (!ndef.isWritable) {
                    return false
                }
                ndef.writeNdefMessage(message)
            } catch (e: IOException) {
                Log.e(TAG, "IOException while closing ndef...", e)
            } catch (e: FormatException) {
                Log.e(TAG, "IOException while closing ndef...", e)
            } finally {
                try {
                    ndef.close()
                } catch (e: IOException) {
                    Log.e(TAG, "IOException while closing ndef...", e)
                }
            }
            return true
        }

        fun splitBarcode(barcode: String): Triple<String, Int, String> {
            var prefixIndex = barcode.length
            var suffixIndex = barcode.length

            for (i in barcode.indices) {
                if (barcode[i].isDigit()) {
                    prefixIndex = i
                    break
                }
            }

            val startIndex = if (prefixIndex == -1) 0 else prefixIndex
            for (i in startIndex until barcode.length) {
                if (!barcode[i].isDigit()) {
                    suffixIndex = i
                    break
                }
            }

            var prefix = ""
            if (prefixIndex >= 0) {
                prefix = barcode.substring(0, prefixIndex)
            }

            var number = 0
            try {
                if (prefixIndex >= 0 && suffixIndex >= 0) {
                    number = barcode.substring(prefixIndex, suffixIndex).toInt()
                } else if (prefixIndex >= 0) {
                    number = barcode.substring(prefixIndex).toInt()
                } else if (suffixIndex >= 0) {
                    number = barcode.substring(0, prefixIndex).toInt()
                }
            } catch (e: NumberFormatException) {
                number = 0
            }

            var suffix = ""
            if (suffixIndex >= 0) {
                suffix = barcode.substring(suffixIndex)
            }

            return Triple(prefix, number, suffix)
        }

        private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
    }
}
package com.alienmantech.nfcfactory

import android.content.Intent
import android.nfc.*
import android.nfc.tech.Ndef
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset

class Utils {
    companion object {
        private const val TAG = "NFC Creator"

        private const val appPackage = "com.alienmantech.maroonnova"
        private const val mimeType = "application/vnd.at-equipcheck+json"

        fun readNfcTag(intent: Intent): String {
            val output = java.lang.StringBuilder()
            // get raw tag id
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            output.append("Tag ID: ")
            if (tag != null) {
                output.append(tag.id.toHexString())
            } else {
                output.append("N/A")
            }
            output.append("\n\n")

            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            if (rawMessages != null) {
                val messages = arrayOfNulls<NdefMessage>(rawMessages.size)
                for (i in rawMessages.indices) {
                    messages[i] = rawMessages[i] as NdefMessage

                    // start of message
                    output.append("Message ")
                    output.append((i + 1).toString())
                    output.append(" of ")
                    output.append(rawMessages.size.toString())
                    output.append(": \n")
                    output.append("---------------------------------")
                    output.append("\n")
                    val records = messages[i]!!.records
                    for (r in records.indices) {
                        val record = records[r]
                        output.append("  Record ")
                        output.append((r + 1).toString())
                        output.append(" of ")
                        output.append(records.size.toString())
                        output.append(": \n")

                        // mime type
                        output.append("  MIME: ")
                        val mime = record.toMimeType()
                        if (mime == null) {
                            output.append("NULL")
                        } else {
                            output.append(mime)
                        }
                        output.append("\n")

                        // payload
                        output.append("  Payload: ")
                        val payload = record.payload
                        if (payload == null) {
                            output.append("NULL")
                        } else {
                            output.append(String(payload, Charset.forName("US-ASCII")))
                        }
                        output.append("\n\n")
                    }
                }
            }

            return output.toString()
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
            return try {
                val jTag = JSONObject()
                jTag.put("v", 1)
                jTag.put("id", id)
                NdefRecord
                    .createMime(
                        mimeType,
                        jTag.toString().toByteArray(Charset.forName("US-ASCII"))
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

        private fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
    }
}
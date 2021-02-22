package com.alienmantech.nfcfactory

import android.content.Intent
import android.nfc.*
import android.nfc.tech.Ndef
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.Charset
import kotlin.experimental.and

class Utils {
    companion object {
        val TAG = "NFC Creator"

        val appPackage = "com.alienmantech.maroonnova"
        val mimeType = "application/vnd.at-equipcheck+json"

        fun readNfcTag(intent: Intent): String {
            val output = java.lang.StringBuilder()
            if (intent != null) { // && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
                val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

                // get raw tag data
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)

                if (tag != null) {
                    val id = tag.id
                    val rawTagId = bytesToHexString(id)
                }

                val rawTagId = bytesToHexString(tag!!.id)
                output.append("Tag ID: ")
                output.append(rawTagId)
                output.append("\n\n")
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
            }

            return output.toString()
        }

        fun writeNfcTag(intent: Intent?, id: String) {
            if (intent != null) {
                // get the tag we just scanned
                val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                var prefixSeperatorIndex = -1

                // remove any prefix separatore
//            if (id != null && id.contains("-")) {
//                prefixSeperatorIndex = id.indexOf("-")
//                id = id.replace("-", "")
//            }

                // create the main record
                val mainRecord = createMainRecord(id!!)
                if (mainRecord == null) {
//                Toast.makeText(getApplicationContext(), "Invalid main record.", Toast.LENGTH_SHORT).show()
                    return
                }

                // create the record that launches our app on the store if they don't have it
                val aarRecord = createAarRecord()

                // put together as a message
                val msg = NdefMessage(arrayOf(mainRecord, aarRecord))
                if (write(tag!!, msg)) {
//                Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show()

                    // try and convert id to a number and increase by one
                    try {
                        // remopve prefix
                        if (id != null) {
                            var number = id.substring(prefixSeperatorIndex).toInt().toLong()
                            number++
                            if (prefixSeperatorIndex >= 0) {
                                val prefix = id.substring(0, prefixSeperatorIndex)
//                            idEt.setText("$prefix-$number")
                            } else {
//                            idEt.setText(number.toString())
                            }
                        }
                    } catch (e: Exception) {
//                    Toast.makeText(
//                        getApplicationContext(),
//                        "Failed to increase count of the edit text number.",
//                        Toast.LENGTH_SHORT
//                    ).show()
                    }
                }
            }
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

        private val hexArray = "0123456789ABCDEF".toCharArray()
        private fun bytesToHexString(bytes: ByteArray): String {
//            val hexChars = CharArray(bytes.size * 2)
//            for (j in bytes.indices) {
//                val v = (bytes[j] and 0xFF.toByte()).toInt()
//
//                hexChars[j * 2] = hexArray[v ushr 4]
//                hexChars[j * 2 + 1] = hexArray[v and 0x0F]
//            }
//            return String(hexChars)

            return bytes.toHexString()
        }

        fun ByteArray.toHexString() = joinToString("") { "%02x".format(it) }
    }
}
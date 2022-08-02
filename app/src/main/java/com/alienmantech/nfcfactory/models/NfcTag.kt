package com.alienmantech.nfcfactory.models

import com.google.gson.Gson

class NfcTag {
    var id: String = ""

    var messages = mutableListOf<Message>()
        private set

    fun addMessage(message: Message) {
        messages.add(message)
    }

    class Message {
        var records = mutableListOf<Record>()
            private set

        fun addRecord(record: Record) {
            records.add(record)
        }

        class Record {
            var mime: String = ""
            var payload: String = ""

            val customTag: CustomNfcTag?
                get() {
                    return if (payload.isNotEmpty() && payload.contains("v")) {
                        Gson().fromJson(payload, CustomNfcTag::class.java)
                    } else {
                        null
                    }
                }
        }
    }

    fun getTagBarcode(): String? {
        messages.let { messages ->
            for (message in messages) {
                for (record in message.records) {
                    if (record.customTag != null) {
                        return record.customTag?.id
                    }
                }
            }
        }
        return null
    }

    fun print(): String {
        val output = java.lang.StringBuilder()

        output.append("Tag ID: ")
        if (id.isNotEmpty()) {
            output.append(id)
        } else {
            output.append("N/A")
        }
        output.append("\n\n")

        for (i in messages.indices) {
            // start of message
            output.append("Message ")
            output.append((i + 1).toString())
            output.append(" of ")
            output.append(messages.size.toString())
            output.append(": \n")
            output.append("---------------------------------")
            output.append("\n")
            val records = messages[i].records
            for (r in records.indices) {
                val record = records[r]
                output.append("  Record ")
                output.append((r + 1).toString())
                output.append(" of ")
                output.append(records.size.toString())
                output.append(": \n")

                // mime type
                output.append("  MIME: ")
                val mime = record.mime
                if (mime.isEmpty()) {
                    output.append("NULL")
                } else {
                    output.append(mime)
                }
                output.append("\n")

                // payload
                output.append("  Payload: ")
                val payload = record.payload
                if (payload.isEmpty()) {
                    output.append("NULL")
                } else {
                    output.append(payload)
                }
                output.append("\n\n")
            }
        }

        return output.toString()
    }
}



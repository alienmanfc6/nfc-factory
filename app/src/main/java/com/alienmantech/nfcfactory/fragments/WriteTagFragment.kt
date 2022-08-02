package com.alienmantech.nfcfactory.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import com.alienmantech.nfcfactory.R
import com.alienmantech.nfcfactory.Utils
import com.alienmantech.nfcfactory.viewmodels.WriteTagViewModel
import java.lang.NumberFormatException

private const val ARG_PARAM1 = "param1"

class WriteTagFragment : BaseTagFragment() {
    private var param1: String? = null

    private lateinit var prefixEditText: EditText
    private lateinit var numberEditText: EditText
    private lateinit var suffixEditText: EditText
    private lateinit var increaseButton: Button
    private lateinit var decreaseButton: Button

    private lateinit var viewModel: WriteTagViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }

        viewModel = ViewModelProvider(this).get(WriteTagViewModel::class.java).apply {
            init()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_write_tag, container, false)
        prefixEditText = root.findViewById(R.id.prefix_edit_text)
        numberEditText = root.findViewById(R.id.number_edit_text)
        suffixEditText = root.findViewById(R.id.suffix_edit_text)
        increaseButton = root.findViewById(R.id.increase_number_button)
        decreaseButton = root.findViewById(R.id.decrease_number_button)

        increaseButton.setOnClickListener {
            increaseNumber(1)
        }

        decreaseButton.setOnClickListener {
            increaseNumber(-1)
        }

        return root
    }

    override fun processTag(intent: Intent) {
        val input = getBarcodeInput()
        if (input.isNotEmpty()) {
            val result = Utils.writeNfcTag(intent, input)
            if (result) {
                increaseNumber(1)
            }
        }
    }

    override fun processBarcodeRead(format: Int, barcode: String) {
        super.processBarcodeRead(format, barcode)

        val result = Utils.splitBarcode(barcode)
        prefixEditText.setText(result.first)
        numberEditText.setText(result.second.toString())
        suffixEditText.setText(result.third)
    }

    private fun getBarcodeInput(): String {
        return prefixEditText.text.toString() + numberEditText.text.toString() + suffixEditText.text.toString()
    }

    private fun increaseNumber(increaseBy: Int) {
        try {
            var number = Integer.parseInt(numberEditText.text.toString())
            number += increaseBy
            numberEditText.setText(number.toString())
        } catch (e: NumberFormatException) {

        }
    }

    companion object {

        fun newInstance(param1: String) =
                WriteTagFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                    }
                }
    }
}
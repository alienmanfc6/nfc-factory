package com.alienmantech.nfcfactory.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.alienmantech.nfcfactory.R
import com.alienmantech.nfcfactory.Utils
import com.alienmantech.nfcfactory.viewmodels.WriteTagViewModel

private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [WriteTagFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WriteTagFragment : BaseTagFragment() {
    private var param1: String? = null

    private lateinit var prefixEditText: EditText
    private lateinit var numberEditText: EditText
    private lateinit var suffixEditText: EditText
    private lateinit var mViewModel: WriteTagViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }

        mViewModel = ViewModelProvider(this).get(WriteTagViewModel::class.java).apply {
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
        var number = Integer.parseInt(numberEditText.text.toString())
        number += increaseBy
        numberEditText.setText(number.toString())
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment WriteTagFragment.
         */
        @JvmStatic
        fun newInstance(param1: String) =
                WriteTagFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                    }
                }
    }
}
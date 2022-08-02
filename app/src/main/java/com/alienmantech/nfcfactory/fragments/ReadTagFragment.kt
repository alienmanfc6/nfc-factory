package com.alienmantech.nfcfactory.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.alienmantech.nfcfactory.R
import com.alienmantech.nfcfactory.Utils
import com.alienmantech.nfcfactory.viewmodels.ReadTagViewModel

class ReadTagFragment : BaseTagFragment() {

    private lateinit var outputTextView: TextView
    private lateinit var writeNextButton: Button

    private lateinit var viewModel: ReadTagViewModel
    var callback: ReadTagFragmentCallbacks? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_read_tag, container, false)
        outputTextView = root.findViewById(R.id.read_output)
        writeNextButton = root.findViewById(R.id.write_next_button)

        writeNextButton.setOnClickListener {
            callback?.setWriteBarcode(viewModel.tag?.getTagBarcode())
        }

        return root
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this).get(ReadTagViewModel::class.java)

        viewModel.output.observe(this) {
            outputTextView.text = it
        }

        viewModel.writeNextButtonEnabled.observe(this) {
            writeNextButton.isEnabled = it
        }
    }

    override fun processTag(intent: Intent) {
        viewModel.tag = Utils.readNfcTag(intent)
    }

    interface ReadTagFragmentCallbacks {
        fun setWriteBarcode(barcode: String?)
    }
}
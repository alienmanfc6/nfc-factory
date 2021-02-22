package com.alienmantech.nfcfactory.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private lateinit var inputEditText: TextView
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
        inputEditText = root.findViewById(R.id.id_edit_text)

        return root
    }

    override fun processTag(intent: Intent) {
        val input = getTextInput()
        if (input.isNotEmpty()) {
            Utils.writeNfcTag(intent, input)
        }
    }

    private fun getTextInput(): String {
        return inputEditText.text.toString()
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
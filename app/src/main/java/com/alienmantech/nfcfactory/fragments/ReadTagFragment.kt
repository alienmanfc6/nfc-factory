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
import com.alienmantech.nfcfactory.viewmodels.ReadTagViewModel

private const val ARG_PARAM1 = "param1"

/**
 * A simple [Fragment] subclass.
 * Use the [ReadTagFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReadTagFragment : BaseTagFragment() {
    private var param1: String? = null

    private lateinit var mViewModel: ReadTagViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }

        mViewModel = ViewModelProvider(this).get(ReadTagViewModel::class.java).apply {
            init()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_read_tag, container, false)
        val outputTextView: TextView = root.findViewById(R.id.read_output)
        mViewModel.output.observe(this, {
            outputTextView.text = it
        })
        return root
    }

    override fun processTag(intent: Intent) {
        mViewModel.updateOutput(Utils.readNfcTag(intent))
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment ReadTagFragment.
         */
        @JvmStatic
        fun newInstance(param1: String) =
            ReadTagFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}
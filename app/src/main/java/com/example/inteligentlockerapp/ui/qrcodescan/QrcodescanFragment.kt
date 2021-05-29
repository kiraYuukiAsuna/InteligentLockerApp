package com.example.inteligentlockerapp.ui.qrcodescan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.example.inteligentlockerapp.MainActivity
import com.example.inteligentlockerapp.R
import com.example.inteligentlockerapp.databinding.FragmentQrcodescanBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class QrcodescanFragment : Fragment() {

    private lateinit var qrcodescanViewModel: QrcodescanViewModel
    private var _binding: FragmentQrcodescanBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        qrcodescanViewModel =
            ViewModelProvider(this).get(QrcodescanViewModel::class.java)

        _binding = FragmentQrcodescanBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private lateinit var codeScanner: CodeScanner

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val scannerView = view.findViewById<CodeScannerView>(R.id.scanner_view)
        val mainActivity: MainActivity = activity as MainActivity
        codeScanner = CodeScanner(mainActivity, scannerView)
        codeScanner.decodeCallback = DecodeCallback {
            GlobalScope.launch {
                mainActivity.unlockRequest(it.text)
            }
            mainActivity.runOnUiThread {
                Toast.makeText(activity, it.text, Toast.LENGTH_SHORT).show()
            }
        }
        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

}
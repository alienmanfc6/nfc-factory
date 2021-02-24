package com.alienmantech.nfcfactory

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.alienmantech.nfcfactory.adapters.SectionsPagerAdapter
import com.alienmantech.nfcfactory.barcodereader.BarcodeCaptureActivity
import com.alienmantech.nfcfactory.fragments.BaseTagFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    private val RC_BARCODE_SCANNER = 8674

    var mAdapter: NfcAdapter? = null
    private lateinit var mViewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        mViewPager = findViewById(R.id.view_pager)
        mViewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(mViewPager)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener { view ->
            val i = Intent(view.context, BarcodeCaptureActivity::class.java)
            startActivityForResult(i, RC_BARCODE_SCANNER)
        }

        // start nfc stuff
        initNfc()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent == null) return

        val fragment = supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.view_pager.toString() + ":" + mViewPager.currentItem)
        if (fragment != null) {
            (fragment as BaseTagFragment).processTag(intent)
        }
    }
    
    override fun onResume() {
        super.onResume()

        try {
            val nfcIntent = Intent(this, javaClass)
            nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0)
            val intentFiltersArray = arrayOf<IntentFilter>()
            val techList = arrayOf(
                arrayOf(
                    Ndef::class.java.name
                ), arrayOf(NdefFormatable::class.java.name)
            )
            val nfcAdpt = NfcAdapter.getDefaultAdapter(this)
            nfcAdpt.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList)
        } catch (e: Exception) {
        }
    }

    override fun onPause() {
        super.onPause()

        if (mAdapter != null) {
            mAdapter!!.disableForegroundDispatch(this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_BARCODE_SCANNER) {
            val scannedBarcode = data?.getStringExtra(BarcodeCaptureActivity.RETURN_BARCODE).toString()
            if (scannedBarcode.isNotEmpty()) {

            }
        }
    }

    private fun initNfc() {
        mAdapter = NfcAdapter.getDefaultAdapter(this)
        if (mAdapter == null) {
            Toast.makeText(applicationContext, "NFC not supported.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!mAdapter!!.isEnabled) {
            Toast.makeText(applicationContext, "NFC not enabled.", Toast.LENGTH_SHORT).show()
            return
        }
    }
}
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
import com.alienmantech.nfcfactory.fragments.ReadTagFragment
import com.alienmantech.nfcfactory.fragments.WriteTagFragment
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {
    companion object {
        private const val RC_BARCODE_SCANNER = 8674
    }

    private var adapter: NfcAdapter? = null
    private lateinit var viewPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        viewPager = findViewById(R.id.view_pager)
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                initFragmentCallbacks()
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })

        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
        val fab: FloatingActionButton = findViewById(R.id.fab)

        fab.setOnClickListener { view ->
            val i = Intent(view.context, BarcodeCaptureActivity::class.java)
            startActivityForResult(i, RC_BARCODE_SCANNER)
        }

        initNfc()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent == null) return

        val fragment =
            supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.view_pager.toString() + ":" + viewPager.currentItem)
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

        adapter?.disableForegroundDispatch(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_BARCODE_SCANNER) {
            val format = -1 //TODO: get the format
            val barcode = data?.getStringExtra(BarcodeCaptureActivity.RETURN_BARCODE).toString()
            if (barcode.isNotEmpty()) {
                val fragment =
                    supportFragmentManager.findFragmentByTag("android:switcher:" + R.id.view_pager.toString() + ":" + viewPager.currentItem)
                if (fragment != null) {
                    (fragment as BaseTagFragment).processBarcodeRead(format, barcode)
                }
            }
        }
    }

    private fun initNfc() {
        adapter = NfcAdapter.getDefaultAdapter(this)
        if (adapter == null) {
            Toast.makeText(applicationContext, "NFC not supported.", Toast.LENGTH_SHORT).show()
            return
        }
        if (adapter?.isEnabled == false) {
            Toast.makeText(applicationContext, "NFC not enabled.", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private fun initFragmentCallbacks() {
        for (fragment in supportFragmentManager.fragments) {
            (fragment as? ReadTagFragment)?.callback =
                object : ReadTagFragment.ReadTagFragmentCallbacks {
                    override fun setWriteBarcode(barcode: String?) {
                        setWriteFragmentBarcode(barcode)
                        moveToPage(1)
                    }
                }
        }
    }

    private fun moveToPage(position: Int) {
        viewPager.currentItem = position
    }

    fun setWriteFragmentBarcode(barcode: String?) {
        if (barcode != null) {
            for (fragment in supportFragmentManager.fragments) {
                (fragment as? WriteTagFragment)?.setBarcode(barcode)
            }
        }
    }
}
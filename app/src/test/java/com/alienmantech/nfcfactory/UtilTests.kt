package com.alienmantech.nfcfactory

import org.junit.Assert
import org.junit.Test

class UtilTests {
    @Test
    fun testBarcodeSplit() {
        // no prefix or suffix
        Assert.assertEquals(
            Triple("", 164564, ""),
            Utils.splitBarcode("164564"))

        // prefix with no suffix
        Assert.assertEquals(
            Triple("E", 164564, ""),
            Utils.splitBarcode("E164564"))

        // prefix with no suffix
        Assert.assertEquals(
            Triple("", 164564, "X"),
            Utils.splitBarcode("164564X"))

        // prefix and suffix
        Assert.assertEquals(
            Triple("E", 164564, "X"),
            Utils.splitBarcode("E164564X"))

        // longer prefix
        Assert.assertEquals(
            Triple("ETF", 164564, ""),
            Utils.splitBarcode("ETF164564"))

        // longer suffix
        Assert.assertEquals(
            Triple("", 164564, "XTG"),
            Utils.splitBarcode("164564XTG"))

        // no number
        Assert.assertEquals(
            Triple("ETF", 0, ""),
            Utils.splitBarcode("ETF"))

        // short number
        Assert.assertEquals(
            Triple("E", 1, ""),
            Utils.splitBarcode("E1"))

        // two sets of numbers
        Assert.assertEquals(
            Triple("E", 1645, "X489"),
            Utils.splitBarcode("E1645X489"))
    }
}
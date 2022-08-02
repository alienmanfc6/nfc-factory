package com.alienmantech.nfcfactory.models

import com.google.gson.annotations.SerializedName

data class CustomNfcTag (
    @SerializedName("v")
    val version: Int = 1,

    @SerializedName("id")
    val id: String = "",
)
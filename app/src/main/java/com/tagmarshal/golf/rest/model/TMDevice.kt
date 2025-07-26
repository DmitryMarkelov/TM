package com.tagmarshal.golf.rest.model

import java.io.Serializable


data class TMDevice(val type: String, val tag: String, val coos: List<Double>): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TMDevice

        if (type != other.type) return false
        if (tag != other.tag) return false
        if (coos != other.coos) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + tag.hashCode()
        result = 31 * result + coos.hashCode()
        return result
    }
}

object DeviceType{
    const val appClassic = "app_classic"
    const val classic = "classic"
    const val drinks = "drinks"
    const val superintendent = "superintendent"
    const val appCart = "app_cart"
    const val marshal = "marshal"
    const val cart = "cart"
}
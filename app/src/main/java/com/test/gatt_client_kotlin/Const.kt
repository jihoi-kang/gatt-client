package com.test.gatt_client_kotlin

import java.util.*

object Const {

    object Action {
        const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
        const val ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
        const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
        const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"
    }

    object Profile {
        val TIME_SERVICE: UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb")
        val CURRENT_TIME: UUID = UUID.fromString("00002a2b-0000-1000-8000-00805f9b34fb")
        val WRITE_CHARACTERISTIC: UUID = UUID.fromString("00002a2c-0000-1000-8000-00805f9b34fb")
        val LOCAL_TIME_INFO: UUID = UUID.fromString("00002a0f-0000-1000-8000-00805f9b34fb")
        val CLIENT_CONFIG: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    }

}
package com.test.gatt_client_kotlin.service

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.test.gatt_client_kotlin.Const

@SuppressLint("MissingPermission")
class BluetoothLeService : Service() {

    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private val binder: IBinder by lazy {
        LocalBinder()
    }

    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothDeviceAddress: String? = null

    private var currentTimeCharacteristic: BluetoothGattCharacteristic? = null

    private val gattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Log.d("jay", "onConnectionStateChange passed")
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = Const.Action.ACTION_GATT_CONNECTED
                sendBroadcast(Intent(intentAction))
                Log.d("jay", "Connected to GATT server.")
                // Attempts to discover services after successful connection.
                Log.d(
                    "jay",
                    "Attempting to start service discovery: ${bluetoothGatt?.discoverServices()}"
                )
                Log.d("jay", "gatt.services size: ${gatt.services.size}")
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = Const.Action.ACTION_GATT_DISCONNECTED
                Log.d("jay", "Disconnected from GATT server.")
                sendBroadcast(Intent(intentAction))
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Log.d("jay", "onServicesDiscovered passed")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendBroadcast(Intent(Const.Action.ACTION_GATT_SERVICES_DISCOVERED))

                Log.d("jay", "gatt.services.size: ${gatt.services.size}")

                gatt.services.forEach {
                    Log.d("jay", "services: ${it.uuid}")
                    if (it.uuid == Const.Profile.TIME_SERVICE) {
                        it.characteristics.forEach { characteristic ->
                            Log.d("jay", "characteristic.uuid: ${characteristic.uuid}")
                            gatt.readCharacteristic(characteristic)
                            gatt.writeCharacteristic(characteristic)
                            if (characteristic.uuid == Const.Profile.CURRENT_TIME) {
                                Log.d("jay", "passed")
                                currentTimeCharacteristic = characteristic
                            }
                        }
                    }
                }
            } else {
                Log.d("jay", "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Log.d("jay", "onCharacteristicRead: " + characteristic.value)
            val data = String(characteristic.value)
            Log.d("jay", "data: $data")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendBroadcast(Intent(Const.Action.ACTION_DATA_AVAILABLE))
                // todo: 코드들이 더 있는데 필요 없을것 같아서 일단 생략
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            Log.d("jay", "onCharacteristicChanged: " + characteristic.value)
            sendBroadcast(Intent(Const.Action.ACTION_DATA_AVAILABLE))
            // todo: 코드들이 더 있는데 필요 없을것 같아서 일단 생략
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    fun connect(address: String): Boolean {
        Log.d("jay", "connect")
        if (address == bluetoothDeviceAddress && bluetoothGatt != null) {
            Log.d("jay", "Trying to use an existing bluetoothGatt for connection.")
            return bluetoothGatt?.connect() == true
        }

        val device = bluetoothAdapter.getRemoteDevice(address)
        device?.let {
            bluetoothGatt = it.connectGatt(this, false, gattCallback)
            Log.d("jay", "Trying to create a new connection")
            bluetoothDeviceAddress = address
            return true
        } ?: run {
            return false
        }
    }

    fun disconnect() {
        Log.d("jay", "disconnect")
        bluetoothGatt?.disconnect()
    }

    fun close() {
        Log.d("jay", "close")
        bluetoothGatt?.close()
        bluetoothGatt = null
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        Log.d("jay", "readCharacteristic: ${characteristic.value}")
        bluetoothGatt?.readCharacteristic(characteristic)
    }

    fun setCharacteristicNotification(
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean,
    ) {
        Log.d("jay", "setCharacteristicNotification: " + characteristic.value)
        bluetoothGatt?.setCharacteristicNotification(characteristic, enabled)
    }

    fun write() {
        if (bluetoothGatt != null && currentTimeCharacteristic != null) {
            Log.d("jay", "write passed!!!!")
            currentTimeCharacteristic!!.setValue("Hello world")
            currentTimeCharacteristic!!.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            bluetoothGatt!!.writeCharacteristic(currentTimeCharacteristic)
        }
    }

    inner class LocalBinder : Binder() {
        val service: BluetoothLeService
            get() = this@BluetoothLeService
    }

}
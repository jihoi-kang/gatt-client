package com.test.gatt_client_kotlin

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.test.gatt_client_kotlin.databinding.ActivityMainBinding
import com.test.gatt_client_kotlin.service.BluetoothLeService

@SuppressLint("MissingPermission")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private var bluetoothLeService: BluetoothLeService? = null

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        bluetoothManager.adapter
    }

    private val bluetoothLeScanner: BluetoothLeScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val bluetoothListAdapter by lazy {
        BluetoothListAdapter {
            Log.d("jay", "clicked: ${it.name}")
            connect(it)
        }
    }

    private val handler by lazy {
        Handler(mainLooper)
    }
    private var scanning: Boolean = false

    private var device: BluetoothDevice? = null

    private val scanCallback: ScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (result.device.name == null) return
            Log.d("jay", "result: " + result.device.name)
            bluetoothListAdapter.updateItem(result.device)
        }
    }

    // Code to manage Service lifecycle.
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            Log.d("jay", "onServiceConnected passed")
            bluetoothLeService = (service as BluetoothLeService.LocalBinder).service
            // Automatically connects to the device upon successful start-up initialization.
            device?.let {
                bluetoothLeService?.connect(it.address)
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothLeService = null
        }
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Const.Action.ACTION_GATT_CONNECTED -> {
                    binding.tvStatus.text = "Connected!"
                }
                Const.Action.ACTION_GATT_DISCONNECTED -> {
                    binding.tvStatus.text = "DisConnected!"
                }
                Const.Action.ACTION_GATT_SERVICES_DISCOVERED -> {
                    // Show all the supported services and characteristics on the user interface.
                    binding.tvDeviceInfo.text = "${device?.name} | ${device?.address}"
                }
                Const.Action.ACTION_DATA_AVAILABLE -> {
                    val data = intent.getStringExtra(Const.Action.EXTRA_DATA)
                    binding.tvChat.text = "${binding.tvChat.text}\n$data"
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.btnScan.setOnClickListener { scan() }
        binding.rvBluetoothList.adapter = bluetoothListAdapter
        binding.btnSend.setOnClickListener { bluetoothLeService?.write() }
    }

    override fun onResume() {
        super.onResume()

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
    }

    override fun onPause() {
        unregisterReceiver(gattUpdateReceiver)

        super.onPause()
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        bluetoothLeService = null

        super.onDestroy()
    }

    private fun scan() {
        if (scanning) return

        bluetoothListAdapter.clear()
        scanning = true
        bluetoothLeScanner.startScan(scanCallback)

        handler.postDelayed({
            scanning = false
            bluetoothLeScanner.stopScan(scanCallback)
        }, 5000L)
    }

    private fun connect(device: BluetoothDevice) {
        this.device = device

        val intent = Intent(this, BluetoothLeService::class.java)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Const.Action.ACTION_GATT_CONNECTED)
        intentFilter.addAction(Const.Action.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(Const.Action.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(Const.Action.ACTION_DATA_AVAILABLE)
        return intentFilter
    }

}
package com.test.gatt_client_kotlin

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.test.gatt_client_kotlin.databinding.ItemBluetoothBinding

@SuppressLint("MissingPermission")
class BluetoothListAdapter(
    private val listener: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<BluetoothListViewHolder>() {

    private val deviceItems = mutableListOf<BluetoothDevice>()

    fun updateItem(deviceItem: BluetoothDevice) {
        deviceItems.forEach {
            if (deviceItem.name == it.name) return
        }
        deviceItems.add(deviceItem)
        notifyDataSetChanged()
    }

    fun clear() {
        this.deviceItems.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BluetoothListViewHolder {
        val binding = DataBindingUtil.inflate<ItemBluetoothBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_bluetooth,
            parent,
            false
        )

        return BluetoothListViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: BluetoothListViewHolder, position: Int) {
        holder.bind(deviceItems[position])
    }

    override fun getItemCount(): Int = deviceItems.size

}
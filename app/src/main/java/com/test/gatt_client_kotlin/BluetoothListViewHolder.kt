package com.test.gatt_client_kotlin

import android.bluetooth.BluetoothDevice
import androidx.recyclerview.widget.RecyclerView
import com.test.gatt_client_kotlin.databinding.ItemBluetoothBinding

class BluetoothListViewHolder(
    private val binding: ItemBluetoothBinding,
    private val listener: (BluetoothDevice) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: BluetoothDevice) {
        binding.root.setOnClickListener {
            listener(item)
        }
        binding.item = item
        binding.executePendingBindings()
    }

}
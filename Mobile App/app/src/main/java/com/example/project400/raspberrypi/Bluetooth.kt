package com.example.project400.raspberrypi

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import java.util.*

class Bluetooth(private val context: Context, private val listener: SensorDataListener) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothGatt: BluetoothGatt? = null
    private val handler = Handler(Looper.getMainLooper())

    private val raspberryPiMacAddress = "D8:3A:DD:9F:48:09"
    private val serviceUuid = UUID.fromString("00000001-710e-4a5b-8d75-3e5b444bc3cf")
    private val tempUuid = UUID.fromString("00000002-710e-4a5b-8d75-3e5b444bc3cf")
    private val hrUuid = UUID.fromString("00000003-710e-4a5b-8d75-3e5b444bc3cf")
    private val spo2Uuid = UUID.fromString("00000004-710e-4a5b-8d75-3e5b444bc3cf")
    private val humidityUuid = UUID.fromString("00000005-710e-4a5b-8d75-3e5b444bc3cf")

    interface SensorDataListener {
        fun onSensorDataUpdated(temp: String, hr: String, spo2: String, humidity: String)
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.d("Bluetooth", "Connected to device, discovering services...")
                handler.postDelayed({
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return@postDelayed
                    gatt.discoverServices()
                }, 1500)
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.e("Bluetooth", "Disconnected from device.")
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("Bluetooth", "Service discovery failed with status: $status")
                return
            }

            Log.d("Bluetooth", "Services discovered:")
            gatt.services.forEach { service ->
                Log.d("Bluetooth", "Service UUID: ${service.uuid}")
                service.characteristics.forEach { char ->
                    Log.d("Bluetooth", "  Characteristic UUID: ${char.uuid}")
                }
            }

            bluetoothGatt = gatt
            readAllSensorData()
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            Log.d("Bluetooth", "Characteristic ${characteristic.uuid} read with status $status")

            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e("Bluetooth", "Failed to read ${characteristic.uuid}")
                return
            }

            val value = characteristic.getStringValue(0)
            Log.d("Bluetooth", "Value read from ${characteristic.uuid}: $value")

            // Update sensor values based on UUID
            when (characteristic.uuid) {
                tempUuid -> {
                    sensorValues["temp"] = value
                    Log.d("Bluetooth", "Temperature value updated: $value")
                }
                hrUuid -> {
                    sensorValues["hr"] = value
                    Log.d("Bluetooth", "Heart Rate value updated: $value")
                }
                spo2Uuid -> {
                    sensorValues["spo2"] = value
                    Log.d("Bluetooth", "SpO2 value updated: $value")
                }
                humidityUuid -> {
                    sensorValues["humidity"] = value
                    Log.d("Bluetooth", "Humidity value updated: $value")
                }
            }

            // Ensure all sensor values are updated before triggering the callback
            if (sensorValues.size == 4) {
                // Update UI with the latest data
                handler.post {
                    listener.onSensorDataUpdated(
                        sensorValues["temp"] ?: "-",
                        sensorValues["hr"] ?: "-",
                        sensorValues["spo2"] ?: "-",
                        sensorValues["humidity"] ?: "-"
                    )
                    // Clear the sensor values after updating the UI
                    sensorValues.clear()
                }
            }
        }
    }

    private val sensorValues = mutableMapOf<String, String>()

    fun connectToPairedDevice() {
        bluetoothAdapter?.bondedDevices?.forEach { device ->
            if (device.address.equals(raspberryPiMacAddress, ignoreCase = true)) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return
                bluetoothGatt = device.connectGatt(context, false, gattCallback)
                return
            }
        }
        Log.e("Bluetooth", "Raspberry Pi device not found in paired devices.")
    }

    fun readAllSensorData() {
        val gatt = bluetoothGatt ?: return
        val service = gatt.getService(serviceUuid)
        if (service == null) {
            Log.e("Bluetooth", "Service $serviceUuid not found!")
            return
        }

        // Read the characteristics with slight delay or asynchronously
        listOf(tempUuid, hrUuid, spo2Uuid, humidityUuid).forEachIndexed { index, uuid ->
            val characteristic = service.getCharacteristic(uuid)
            if (characteristic == null) {
                Log.e("Bluetooth", "Characteristic $uuid not found!")
            } else if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_READ == 0) {
                Log.e("Bluetooth", "Characteristic $uuid is not readable!")
            } else {
                Log.d("Bluetooth", "Reading characteristic: $uuid")

                // Add slight delay between reads or handle async
                handler.postDelayed({
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        gatt.readCharacteristic(characteristic)
                    }
                }, (index * 500).toLong()) // Delay 500 ms between reads, adjust as needed
            }
        }
    }


    fun disconnect() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}

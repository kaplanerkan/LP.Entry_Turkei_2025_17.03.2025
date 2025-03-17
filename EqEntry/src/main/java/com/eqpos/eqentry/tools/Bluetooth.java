package com.eqpos.eqentry.tools;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.Set;

/**
 * Created by dursu on 28.11.2018.
 */

public class Bluetooth {


    public static void connectToPrinter(String printerAddress) {
        try {
            Variables.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Log.v(Variables.TAG, "Coming incoming address " + printerAddress);
            Variables.mBluetoothDevice = Variables.mBluetoothAdapter.getRemoteDevice(printerAddress);

            Variables.mBluetoothSocket = Variables.mBluetoothDevice.createRfcommSocketToServiceRecord(Variables.applicationUUID);
            Variables.mBluetoothAdapter.cancelDiscovery();
            Variables.mBluetoothSocket.connect();
        } catch (IOException eConnectException) {
            Log.d(Variables.TAG, "CouldNotConnectToSocket", eConnectException);
            closeSocket(Variables.mBluetoothSocket);
            return;
        }

    }

    public static void ListPairedDevices() {
        Set<BluetoothDevice> mPairedDevices = Variables.mBluetoothAdapter
                .getBondedDevices();
        if (mPairedDevices.size() > 0) {
            for (BluetoothDevice mDevice : mPairedDevices) {
                Log.v(Variables.TAG, "PairedDevices: " + mDevice.getName() + "  "
                        + mDevice.getAddress());
            }
        }
    }

    public static void closeSocket(BluetoothSocket nOpenSocket) {
        try {
            nOpenSocket.close();
            Log.d(Variables.TAG, "SocketClosed");
        } catch (IOException ex) {
            Log.d(Variables.TAG, "CouldNotCloseSocket");
        }
    }
}

package net.ballmerlabs.scatterbrain.network.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.util.Log;

import net.ballmerlabs.scatterbrain.MainTrunk;

import java.io.IOException;
import java.io.InterruptedIOException;

/**
 * Listens for incoming bluetooth connections
 * without paring/security
 */
public class ScatterAcceptThread extends Thread {
    private BluetoothServerSocket mmServerSocket = null;
    private MainTrunk trunk;
    public ScatterAcceptThread(MainTrunk trunk, BluetoothAdapter adapter) {
        this.trunk = trunk;
        BluetoothServerSocket tmp = null;
        try {
            tmp = adapter.listenUsingInsecureRfcommWithServiceRecord(
                    trunk.blman.NAME, trunk.blman.UID);
        } catch (IOException e) {
            Log.e(trunk.blman.TAG, "IOException when starting bluetooth listener");
        }

        mmServerSocket = tmp;
    }

    @Override
    public void run() {
        Log.v(trunk.blman.TAG,"Accepted a connection" );
        BluetoothSocket socket = null;
        while (true) {
            try {
                socket = mmServerSocket.accept();
                Thread.sleep(1000);
                socket.close();
            } catch (IOException e) {
                break;
            }
            catch(InterruptedException e) {

            }

        }
    }


    public void cancel() {
        try {
            mmServerSocket.close();
        } catch (IOException e) {


        }
    }
}

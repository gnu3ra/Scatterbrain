package net.ballmerlabs.scatterbrain.network.wifidirect;

import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;

/**
 * Created by user on 5/25/16.
 */
public class DirectConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener {
    HashMap<WifiP2pDevice, WifiP2pConfig> connectedList;
    public DirectConnectionInfoListener(HashMap<WifiP2pDevice, WifiP2pConfig> connectedList) {
        this.connectedList = connectedList;
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        InetAddress address = info.groupOwnerAddress;
        if(info.groupFormed && info.isGroupOwner) {
            //start server and make others connect
        }
        else if(info.groupFormed) {
            //connect to group leader
        }
    }
}

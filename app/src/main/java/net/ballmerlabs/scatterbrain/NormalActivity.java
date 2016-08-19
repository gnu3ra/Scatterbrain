package net.ballmerlabs.scatterbrain;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.network.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.GlobalNet;
import net.ballmerlabs.scatterbrain.network.NetTrunk;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

public class NormalActivity extends AppCompatActivity {

    private EditText MsgBox;
    private ListView messageTimeline;
    private ArrayAdapter<String> Messages;
    private Button sendButton;
    private GlobalNet globnet;
    private DeviceProfile profile;
    private TextView peersView;
    private ScatterRoutingService service;
    private boolean scatterBound = false;
    private ScatterRoutingService mService;
    public static boolean active = false;
    public final String TAG = "MessagingActivity";

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ScatterRoutingService.ScatterBinder binder =
                    (ScatterRoutingService.ScatterBinder) service;
            mService = binder.getService();

            mService.registerOnDeviceConnectedCallback(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView senpai_notice = (TextView) findViewById(R.id.notice_text);
                            if(senpai_notice != null) {
                                senpai_notice.setVisibility(View.VISIBLE);
                                senpai_notice.setText("Senpai NOTICED YOU! \n and the packet was not corrupt");
                            }
                        }
                    });
                }
            });

            mService.registerMessageArrayAdapter(Messages);

            Log.v(TAG, "Bound to routing service");
            scatterBound = true;



        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e(TAG, "Disconnected from routing service");
            scatterBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);

        messageTimeline = (ListView) this.findViewById(R.id.timeline);

        MsgBox = (EditText) this.findViewById(R.id.editText);
        Messages = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        messageTimeline.setAdapter(Messages);

        //messagebox handeling
        sendButton = (Button) this.findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList();
            }
        });

        Intent bindIntent = new Intent(this, ScatterRoutingService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);


    }

    //adds a message to the list and clears the input field
    private void updateList() {
        BlockDataPacket bd = new BlockDataPacket(MsgBox.getText().toString().getBytes(), true,
                mService.getTrunk().profile,mService.luid);
        BlockDataPacket out = new BlockDataPacket(bd.contents);
        Messages.add(new String(out.body));
       // BlockDataPacket bd = new BlockDataPacket(MsgBox.getText().toString().getBytes(), true,profile);

        if(scatterBound) {
            Log.v(TAG, "Updating list");
            mService.getBluetoothManager().sendMessageToBroadcast(
                    MsgBox.getText().toString().getBytes(),true);
        }

        MsgBox.setText("");

    }


    @Override
    protected void onStart() {
        super.onStart();
        NormalActivity.active = true;

    }

    @Override
    protected void onStop() {
        super.onStop();
        NormalActivity.active = false;
    }

    @Override
    protected void onResume() {
        // trunk.globnet.startWifiDirectLoopThread();
        super.onResume();
    }

    @Override
    protected void onPause() {
        //trunk.trunk.globnet.stopWifiDirectLoopThread();
        super.onPause();
    }

    public void addMessage(String message) {
        Messages.add(message);
    }
}

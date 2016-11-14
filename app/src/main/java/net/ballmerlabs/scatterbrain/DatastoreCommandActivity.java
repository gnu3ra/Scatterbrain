package net.ballmerlabs.scatterbrain;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.datastore.LeDataStore;
import net.ballmerlabs.scatterbrain.network.ScatterRoutingService;

public class DatastoreCommandActivity extends AppCompatActivity {
    private ScatterRoutingService mService;
    private String TAG = "DatastoreCommand";
    private TextView dbDisplay;
    private LeDataStore ds;
    private boolean scatterBound;
    private boolean dbConnected;
    private Button refresh_button;
    private TextView dbTextView;
    private Button clearButton;
    private Button trimButton;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ScatterRoutingService.ScatterBinder binder =
                    (ScatterRoutingService.ScatterBinder) service;
            mService = binder.getService();

            //mService.getBluetoothManager().startDiscoverLoopThread();
            scatterBound = true;

            ds = mService.dataStore;

            dbConnected = mService.dataStore.connected;
            if (dbConnected) {
                dbDisplay.setText("CONNECTED");
                dbDisplay.setTextColor(Color.GREEN);
                dbConnected = true;
            }
            else {
                dbDisplay.setText("DISCONNECTED");
                dbDisplay.setTextColor(Color.RED);
                dbConnected = false;
            }



        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            scatterBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datastore_command);

        dbDisplay = (TextView) findViewById(R.id.dboverviewtext);
        if(!dbConnected) {
            dbDisplay.setText("DISCONNECTED");
            dbDisplay.setTextColor(Color.RED);
        }

        final Activity main = this;




        refresh_button = (Button) findViewById(R.id.refreshdb_button);
        dbTextView = (TextView) findViewById(R.id.db_textview2);
        clearButton = (Button) findViewById(R.id.clear_button);
        trimButton = (Button) findViewById(R.id.button_trim);


        refresh_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbConnected) {
                    ds.enqueueMessage("Testfefefefeef", "contentsfefef", 5, "goobyfefefefef", "sexy data" , "quantum fruit", "ternary gender", "flagsfrgrgrrfref", "sigfefefefefefefefef", 3);
                    dbTextView.setText(ds.getMessages().toString());
                }
                else {
                    dbTextView.setText("No connection to datastore. Please try again.");
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbConnected) {
                    ds.flushDb();
                }
            }
        });

        trimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dbConnected) {
                    ds.trimDatastore(100);
                }
            }
        });

        dbTextView.setText("");

    }
}

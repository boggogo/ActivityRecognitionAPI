package georgikoemdzhiev.acrectest;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{


    private Button mStartBtn;
    private Button mStopBtn;
    private TextView mActivityType;
    private TextView mConfidenceLevel;

    private static final int REQUEST_CODE = 0;
    private static final long UPDATE_INTERVAL = 3000;
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.INTENT_FILTER));

        mClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mClient.connect();


        mStartBtn = (Button)findViewById(R.id.startBtn);
        mStopBtn = (Button)findViewById(R.id.stopBtn);
        mActivityType = (TextView)findViewById(R.id.activityType);
        mConfidenceLevel = (TextView)findViewById(R.id.confidence);


        mStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.disconnect();
                LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mBroadcastReceiver);
                Toast.makeText(MainActivity.this,"Stopped",Toast.LENGTH_SHORT).show();
            }
        });

        mStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClient.connect();
                LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(mBroadcastReceiver,
                        new IntentFilter(Constants.INTENT_FILTER));

                Toast.makeText(MainActivity.this,"Started",Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        logThis("OnConnected");
        Intent intent = new Intent(this,ActivityRecognizedService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mClient,UPDATE_INTERVAL, pendingIntent);
    }

    @Override
    public void onConnectionSuspended(int i) {
        logThis("onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        logThis("onConnectionFailed");
    }

    private void logThis(String s){
        Log.d(MainActivity.class.getSimpleName(),s);
    }


    @Override
    protected void onDestroy() {
        mClient.disconnect();
        LocalBroadcastManager.getInstance(MainActivity.this).unregisterReceiver(mBroadcastReceiver);
        Toast.makeText(MainActivity.this,"Activity Recognition Stopped!",Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(Constants.MESSAGE_KEY);
            mActivityType.setText(message);
            mConfidenceLevel.setText("" + intent.getIntExtra(Constants.CONFIDENCE_KEY,0));
            logThis("Got message: " + message);
        }
    };
}

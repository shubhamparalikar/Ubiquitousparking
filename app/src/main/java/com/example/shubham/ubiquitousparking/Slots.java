package com.example.shubham.ubiquitousparking;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.MainThread;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static com.example.shubham.ubiquitousparking.R.mipmap.occupied;

public class Slots extends AppCompatActivity {
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private String mConnectedDeviceName = null;
    BluetoothSocket btSocket = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    BluetoothAdapter myBluetoothadpt = null;
    //ConnectedThread manageConnectedSocket;
    BluetoothDevice device=null;
    // private AcceptThread mSecureAcceptThread;
    private static final String NAME_SECURE = "HC-05";

    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    /**
     * Member fields
     */
    private StringBuffer mOutStringBuffer;
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
   // BluetoothChatService mChatService=null;
   ConnectThread ct;
    ConnectedThread manageConnectedSocket;
    String str="";

    ImageButton ib1,ib2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slots);
        myBluetoothadpt = BluetoothAdapter.getDefaultAdapter();
ib1=(ImageButton)findViewById(R.id.imageButton11);
        ib2=(ImageButton)findViewById(R.id.ib1);

        // Initialize the buffer for outgoing messages
        //mOutStringBuffer = new StringBuffer("");
        device = myBluetoothadpt.getRemoteDevice("98:D3:32:70:90:B8");
        //ConnectThread ct = new ConnectThread(device);
        //ct.start();
        // mSecureAcceptThread = new AcceptThread(true);
        // mSecureAcceptThread.start();
        String address = "98:D3:32:70:90:B8";
        // Get the BluetoothDevice object
        Log.d("here", "connectDevice "+address);
        BluetoothDevice device = myBluetoothadpt.getRemoteDevice(address);
        // Attempt to connect to the device

        ct = new ConnectThread(device);
        ct.start();



    }

    @Override
    protected void onStop() {
        super.onStop();
        ct.cancel();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflt=this.getMenuInflater();
        inflt.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
if(item.getItemId()==R.id.refresh){
    String s="r";
    manageConnectedSocket.write(s.getBytes());

}
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(str.length()==0){
            Toast.makeText(this, " PARKING SLOTS ARE AVAILABLE", Toast.LENGTH_SHORT).show();
            ib2.setBackgroundColor(Color.GREEN);

            ib2.setImageResource(R.mipmap.free);
            ib1.setBackgroundColor(Color.GREEN);

            ib1.setImageResource(R.mipmap.free);
        }
        if(str.contains("1")){
            ib1.setBackgroundColor(Color.RED);

            ib1.setImageResource(R.mipmap.occupied);

        }

        if(str.contains("2")){
            ib2.setBackgroundColor(Color.RED);

            ib2.setImageResource(R.mipmap.occupied);

        }
str="";
        return super.onOptionsItemSelected(item);

    }



    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) { }
            mmSocket = tmp;
            btSocket=mmSocket;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            myBluetoothadpt.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();

            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            manageConnectedSocket=new ConnectedThread(btSocket);
            manageConnectedSocket.start();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;


        }

        public void run() {
             // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            for (int i=0;i<2 ; i++) {
                try {
                    byte[] buffer = new byte[1];
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    str += new String(buffer, "UTF-8");
                    // Send the obtained bytes to the UI Activity
                    Log.d("shubham", "received: "+str);


                } catch (IOException e) {
                    Log.e("", "disconnected", e);

                    // Start the service over to restart listening mode

                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                Log.d("track", "in write" + bytes.toString());

                // mmOutStream.flush();
                mmOutStream.write(bytes);
                mmOutStream.flush();

            } catch (IOException e) {
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                //Toast.makeText(Hall.this,"you are not connected!!",Toast.LENGTH_SHORT).show();}
            }
        }

    }


}

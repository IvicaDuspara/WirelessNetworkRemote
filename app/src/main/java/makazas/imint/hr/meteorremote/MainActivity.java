package makazas.imint.hr.meteorremote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


/**
 * Main activity which allows user to enter IPv4 address and a port of a server
 * which is broadcasting. An attempt of a connection will be done once user
 * enters IP address and port.
 *
 * @author Ivica Duspara
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Used as a key for {@link Bundle}
     */
    public static final String IP_ADDRESS="IP";

    /**
     * Used as a key for {@link Bundle}
     */
    public static final String PORT="port";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(l -> attemptConnection());

    }

    /**
     * Launches {@link ListedSongsActivity} if user has entered both IP address and port number.
     * Note that this method does not check validity of entered data only if data is entered.<br>
     * If user did not enter any data, a {@code Toast} will be shown as a warning.<br>
     *
     * For validation of IP Address and port number, please see {@link ListedSongsActivity#startSocket() startSocket()}
     *
     */
    private void attemptConnection() {
        String macAddress ="";
        try {
            macAddress = getMACAddres();
        }catch(SocketException ex) {
            ex.printStackTrace();
        }
        EditText address = findViewById(R.id.ipEditText);
        EditText port = findViewById(R.id.portEditText);
        if(address.getText().toString().equals("")) {
            Toast.makeText(this,"Invalid IP address",Toast.LENGTH_LONG).show();
            return;
        }
        if(port.getText().toString().equals("")) {
            Toast.makeText(this,"Invalid port", Toast.LENGTH_LONG).show();
            return;
        }
        if(macAddress==null) {
            Toast.makeText(this,"No MAC address", Toast.LENGTH_LONG).show();
        }
        Intent listIntent = new Intent(MainActivity.this, ListedSongsActivity.class);
        Bundle b = new Bundle();
        b.putString(IP_ADDRESS,address.getText().toString());
        b.putInt(PORT,Integer.valueOf(port.getText().toString()));
        b.putString("MAC",macAddress);
        listIntent.putExtras(b);
        startActivity(listIntent);
    }

    private String getMACAddres() throws SocketException{
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while(en.hasMoreElements()){
            NetworkInterface ni = en.nextElement();
            byte[] polje = ni.getHardwareAddress();
            if(polje != null && ni.toString().contains("192.168")) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < polje.length; i++) {
                    sb.append(String.format("%02X%s", polje[i], (i < polje.length - 1) ? "-" : ""));
                }
                return sb.toString();
            }
        }
        return null;
    }
}

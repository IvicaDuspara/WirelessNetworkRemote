package makazas.imint.hr.meteorremote;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import makazas.imint.hr.meteorremote.util.SharedPrefsUtil;


/**
 * Main activity which allows user to enter IPv4 address and a port of a server
 * which is broadcasting. An attempt of a connection will be done once user
 * enters IP address and port and clicks connect.
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
    public static final String PORT = "port";

    /**
     * Used as a key for {@link Bundle}
     */
    public static final String MAC = "MAC_ADDRESS";

    /**
     * Edit text field for ip address
     */
    private EditText ipEditText;

    /**
     * Edit text field for port number.
     */
    private EditText portEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button connectButton = findViewById(R.id.connectButton);
        ipEditText = findViewById(R.id.ipEditText);
        portEditText = findViewById(R.id.portEditText);
        connectButton.setOnClickListener(l -> attemptConnection());
        initializeEditTextFromSharedPreferences();

    }

    /**
     * Launches {@link ListedSongsActivity} if user has entered both IP address and port number.
     * If user hasn't entered IP address or port number or if MAC address could not be obtained
     * {@link ListedSongsActivity} won't be launched.<br>
     *
     * This method will not check if a socket can be created. For that please see {@link ListedSongsActivity#startSocket()}
     *
     */
    private void attemptConnection() {
        String address = ipEditText.getText().toString();
        String port = portEditText.getText().toString();
        String macAddress;
        try {
            macAddress = getMACAddres();
        }catch(SocketException ex) {
            Toast.makeText(this,"Could not get MAC address",Toast.LENGTH_LONG).show();
            return;
        }
        if(address.isEmpty()) {
            ipEditText.setError("Invalid IP address");
            return;
        }
        if(port.isEmpty()) {
            portEditText.setError("Invalid port");
            return;
        }
        if(macAddress == null) {
            Toast.makeText(this,"No MAC address", Toast.LENGTH_LONG).show();
            return;
        }
        saveInputToSharedPreferences();
        Intent listIntent = new Intent(MainActivity.this, ListedSongsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(IP_ADDRESS,address);
        bundle.putInt(PORT,Integer.valueOf(port));
        bundle.putString(MAC,macAddress);
        listIntent.putExtras(bundle);
        startActivity(listIntent);
    }


    /**
     * Returns a {@code String} representing MAC address of a client of {@code null} if no MAC address was found.
     * This method will search for MAC address bound on a network interface of a device which uses wireless network
     * and which is connected on wireless network currently.
     *
     *
     * @return String representing MAC address of a client or {@code null} if no MAC address was found
     *
     * @throws SocketException if an I/O error occurs while getting {@link NetworkInterface}
     */
    private String getMACAddres() throws SocketException{
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while(en.hasMoreElements()){
            NetworkInterface ni = en.nextElement();
            byte[] bytes = ni.getHardwareAddress();
            if(bytes != null && ni.toString().contains("192.168")) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < bytes.length; i++) {
                    sb.append(String.format("%02X%s", bytes[i], (i < bytes.length - 1) ? "-" : ""));
                }
                return sb.toString();
            }
        }
        return null;
    }


    /**
     * Initializes {@link #ipEditText} and {@link #portEditText} with appropriate text.
     * This text will be equal to last entered values in respective fields when application was launched
     */
    private void initializeEditTextFromSharedPreferences() {
        ipEditText.setText(SharedPrefsUtil.get(this,IP_ADDRESS,""));
        portEditText.setText(SharedPrefsUtil.get(this,PORT,""));
    }

    /**
     * Saves values of {@link #ipEditText} and {@link #portEditText} to shared preferences.
     */
    private void saveInputToSharedPreferences() {
        SharedPrefsUtil.save(this,IP_ADDRESS,ipEditText.getText().toString());
        SharedPrefsUtil.save(this,PORT,portEditText.getText().toString());
    }
}

package makazas.imint.hr.meteorremote.ui;

import android.content.Intent;
import android.os.NetworkOnMainThreadException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import makazas.imint.hr.meteorremote.util.Constants;
import makazas.imint.hr.meteorremote.R;
import makazas.imint.hr.meteorremote.ui.songslist.SongsListActivity;
import makazas.imint.hr.meteorremote.util.NetworkUtil;
import makazas.imint.hr.meteorremote.util.SharedPrefsUtil;

/**
 * Main activity which allows user to enter IPv4 address and a port of a server
 * which is broadcasting. An attempt of a connection will be done once user
 * enters IP address and port.
 *
 * @author Ivica Duspara
 * @version 1.0
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.et_main_ipaddress)
    EditText etIpAddress;

    @BindView(R.id.et_main_port)
    EditText etPort;

    @BindView(R.id.btn_main_connect)
    Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initializeEditTextsFromSharedPrefs();

        btnConnect.setOnClickListener(l -> attemptConnection());
    }

    /**
     * Launches {@link SongsListActivity} if user has entered both IP address and port number.
     * Note that this method does not check validity of entered data only if data is entered.<br>
     * If user did not enter any data, a {@code Toast} will be shown as a warning.<br>
     * <p>
     * For validation of IP Address and port number, please see {@link SongsListActivity#startSocket() startSocket()}
     */
    private void attemptConnection() {
        String ipAddress = etIpAddress.getText().toString();
        String port = etPort.getText().toString();

        if (!NetworkUtil.isValidIpAddress(ipAddress)) {
            etIpAddress.setError(getStringResource(R.string.string_ipaddress_error));
            return;
        }
        if (!NetworkUtil.isValidPort(port)) {
            etPort.setError(getStringResource(R.string.string_port_error));
            return;
        }
        saveInputToSharedPrefs();

        Intent listIntent = new Intent(MainActivity.this, SongsListActivity.class);
        Bundle b = new Bundle();
        b.putString(Constants.IP_ADDRESS, etIpAddress.getText().toString());
        b.putInt(Constants.PORT, Integer.valueOf(etPort.getText().toString()));
        listIntent.putExtras(b);

        startActivity(listIntent);
    }

    private void saveInputToSharedPrefs(){
        SharedPrefsUtil.save(this, Constants.IP_ADDRESS, etIpAddress.getText().toString());
        SharedPrefsUtil.save(this, Constants.PORT, etPort.getText().toString());
    }

    private void initializeEditTextsFromSharedPrefs() {
        etIpAddress.setText(SharedPrefsUtil.get(this, Constants.IP_ADDRESS, ""));
        etPort.setText(SharedPrefsUtil.get(this, Constants.PORT, ""));
    }

    private String getStringResource(int resId){
        return getResources().getString(resId);
    }
}

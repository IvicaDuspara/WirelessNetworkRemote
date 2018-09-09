package makazas.imint.hr.meteorremote.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import makazas.imint.hr.meteorremote.networking.SocketSingleton;
import makazas.imint.hr.meteorremote.util.Constants;
import makazas.imint.hr.meteorremote.R;
import makazas.imint.hr.meteorremote.ui.songslist.SongsListActivity;
import makazas.imint.hr.meteorremote.util.NetworkUtil;
import makazas.imint.hr.meteorremote.util.SharedPrefsUtil;
import makazas.imint.hr.meteorremote.util.ToastUtil;

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
    }

    @OnClick(R.id.btn_main_connect)
    public void attemptConnection() {
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

        tryInitializingSocket(ipAddress, port);
    }

    private void saveInputToSharedPrefs() {
        SharedPrefsUtil.save(this, Constants.IP_ADDRESS, etIpAddress.getText().toString());
        SharedPrefsUtil.save(this, Constants.PORT, etPort.getText().toString());
    }

    private void initializeEditTextsFromSharedPrefs() {
        etIpAddress.setText(SharedPrefsUtil.get(this, Constants.IP_ADDRESS, ""));
        etPort.setText(SharedPrefsUtil.get(this, Constants.PORT, ""));
    }

    private String getStringResource(int resId) {
        return getResources().getString(resId);
    }

    @SuppressLint("StaticFieldLeak")
    private void tryInitializingSocket(String ipAddress, String port){
        new AsyncTask<String, String, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                if(SocketSingleton.getInstance().initializeSocket(ipAddress, port)){
                    startActivity(new Intent(MainActivity.this, SongsListActivity.class));
                    saveInputToSharedPrefs();
                } else {
                    runOnUiThread(
                            () -> ToastUtil.showShortToastWithMessage(MainActivity.this, getStringResource(R.string.string_cannotconnect))
                    );
                }
                return null;
            }
        }.execute(ipAddress, port);
    }
}

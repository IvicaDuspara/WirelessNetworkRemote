package makazas.imint.hr.meteorremote.ui.songslist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import makazas.imint.hr.meteorremote.util.Constants;
import makazas.imint.hr.meteorremote.R;
import makazas.imint.hr.meteorremote.presentation.SongsListPresenter;
import makazas.imint.hr.meteorremote.ui.MainActivity;
import makazas.imint.hr.meteorremote.util.ToastUtil;


public class SongsListActivity extends AppCompatActivity implements SongsListContract.View {

    private SongsListContract.Presenter presenter;

    @BindView(R.id.rv_songslist_songs)
    RecyclerView rvSongs;

    @BindView(R.id.tv_songslist_nowplaying)
    TextView tvNowPlayingSong;

    @BindView(R.id.tv_songslist_queuedsong)
    TextView tvQueuedSong;

    @BindView(R.id.tv_songslist_queuedposition)
    TextView tvQueuedSongPosition;

    private SongsListAdapter songsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_list);
        ButterKnife.bind(this);

        presenter = new SongsListPresenter(this);

        songsAdapter = new SongsListAdapter(getSongClickListener());
        initRecyclerView();

        startSocket();
    }

    /**
     * Connects {@link SongsListPresenter#clientSocket clientSocket} to a server. Server's IP address and port are passed as a {@code Bundle} in a intent,
     * bundled in {@link MainActivity MainActivity}.<br>
     * <p>
     * If this method is called without a {@code Bundle} or this activity is started without a {@code Intent} a message will be displayed<br><br>
     * <p>
     * <b>Note:</b> this method does not check validity of IPv4 address or validity of a port. That job is done when constructing a {@link java.net.SocketAddress SocketAddress} object.
     * See {@link SongsListPresenter#initSongsFromServer(String, String)}.
     */
    private void startSocket() {
        if (getIntent() != null && getIntent().getExtras() != null) {
            String ipAddress = getIntent().getStringExtra(Constants.IP_ADDRESS);
            int portNumber = getIntent().getIntExtra(Constants.PORT, 0);

            presenter.initSongsFromServer(ipAddress, Integer.toString(portNumber));
        }
    }

    private SongClickListener getSongClickListener() {
        return new SongClickListener() {
            @Override
            public void onClick(String songName) {
                presenter.sendSongToServer(songName);
            }
        };
    }

    @Override
    public void updateListWithSongs(List<String> songs) {
        runOnUiThread(() -> songsAdapter.updateSongs(songs));
    }

    @Override
    public void setNowPlayingSong(String songName) {
        runOnUiThread(() -> tvNowPlayingSong.setText(songName));
    }

    @Override
    public void setQueuedSong(String songName) {
        runOnUiThread(() -> tvQueuedSong.setText(songName));
    }

    @Override
    public void setQueuedSongPosition(int position) {
        String toDisplay = String.format(
                Locale.getDefault(),
                "%s(%d):", getResources().getString(R.string.string_queued), position
        );
        runOnUiThread(() -> tvQueuedSongPosition.setText(toDisplay));
    }

    @Override
    public void showSuccessfulQueuedSongToast(String songName) {
        runOnUiThread(() -> ToastUtil.showShortToastWithMessage(
                this,
                String.format(Locale.getDefault(), "%s %s", songName, getStringResource(R.string.string_queued_confirmation_toast))
        ));
    }

    @Override
    public void clearQueuedSongView() {
        runOnUiThread(() -> tvQueuedSong.setText(null));
        runOnUiThread(() -> tvQueuedSongPosition.setText(null));
    }

    private void initRecyclerView() {
        rvSongs.setHasFixedSize(true);
        rvSongs.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        rvSongs.setAdapter(songsAdapter);
    }

    private void testViews(){
        List<String> test = new ArrayList<>();
        test.add("Sinan Sakic - Trezan mi je napravio dijete");
        test.add("Black Eyed Peas - Boom Boom Pow");
        test.add("BONES - AsTheAncientHawaiiansUsedToSay");
        updateListWithSongs(test);

        setNowPlayingSong("Ivica Duspara - Sto i jedan dokaz da je zemlja ravna");
        setQueuedSongPosition(7);
        setQueuedSong("Marko Duspara - Zasto je moj brat u krivu");
    }

    private String getStringResource(int resId){
        return getResources().getString(resId);
    }

    // TODO: 27-Aug-18 actually figure out what to do on back pressed
    /*@Override
    public void onBackPressed() {
        if (clientSocketWriter != null) {
            try {
                clientSocketWriter.write(Constants.CLIENT_CLOSED);
                clientSocketWriter.newLine();
                clientSocketWriter.flush();
                clientSocketWriter.close();
            } catch (IOException ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }*/
}
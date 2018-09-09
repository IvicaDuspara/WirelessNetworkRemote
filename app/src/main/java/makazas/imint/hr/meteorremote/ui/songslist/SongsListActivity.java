package makazas.imint.hr.meteorremote.ui.songslist;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
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
import makazas.imint.hr.meteorremote.util.StringFormattingUtil;
import makazas.imint.hr.meteorremote.util.ToastUtil;

// TODO: 08-Sep-18 add progress bar while loading songs
// TODO: 08-Sep-18 disconnect from server on back-pressed

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

    @Override
    protected void onDestroy() {
        Log.d(Constants.LOG_TAG, "in ondestroy");
        presenter.disconnectFromServer();
        super.onDestroy();
    }

    private void startSocket() {
       /* if (getIntent() != null && getIntent().getExtras() != null) {
            String ipAddress = getIntent().getStringExtra(Constants.IP_ADDRESS);
            int portNumber = getIntent().getIntExtra(Constants.PORT, 0);

            presenter.connectToServer(ipAddress, Integer.toString(portNumber));
        }*/
       presenter.connectToServer();
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
        SpannableString str;
        if(songName == null || songName.isEmpty()){
            //nothing is playing so the view can display nothing.
            str = new SpannableString("");
        } else {
            //using spannable string allows bolding one part of the string and not the other.
            String nowPlayingLabel = getStringResource(R.string.string_nowplaying);
            str = new SpannableString(nowPlayingLabel + ": " + songName);
            str.setSpan(new StyleSpan(Typeface.BOLD), 0, nowPlayingLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        runOnUiThread(() -> tvNowPlayingSong.setText(str));
    }

    @Override
    public void setQueuedSong(String songName) {
        runOnUiThread(() -> tvQueuedSong.setText(songName));
    }

    @Override
    public void setQueuedSongPosition(int position) {
        String toDisplay = String.format(
                Locale.getDefault(),
                "%s(%s):", getResources().getString(R.string.string_queued), StringFormattingUtil.attachOrdinalSuffix(position + 1)
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

        DividerItemDecoration decor = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        decor.setDrawable(getDrawable(R.drawable.listitem_divider));
        rvSongs.addItemDecoration(decor);

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
}
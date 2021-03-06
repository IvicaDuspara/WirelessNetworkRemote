package makazas.imint.hr.meteorremote.ui.songslist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import makazas.imint.hr.meteorremote.util.Constants;
import makazas.imint.hr.meteorremote.R;
import makazas.imint.hr.meteorremote.presentation.SongsListPresenter;
import makazas.imint.hr.meteorremote.networking.NetworkUtil;
import makazas.imint.hr.meteorremote.util.StringFormattingUtil;
import makazas.imint.hr.meteorremote.util.ToastUtil;

public class SongsListActivity extends AppCompatActivity implements SongsListContract.View {

    @BindView(R.id.rv_songslist_songs)
    RecyclerView rvSongs;

    @BindView(R.id.tv_songslist_nowplaying)
    TextView tvNowPlayingSong;

    @BindView(R.id.tv_songslist_queuedsong)
    TextView tvQueuedSong;

    @BindView(R.id.tv_songslist_queuedposition)
    TextView tvQueuedSongPosition;

    @BindView(R.id.pb_songslist_loadingsongs)
    ProgressBar pbLoadingSongs;

    private boolean isSongsListInitialized;

    private SongsListContract.Presenter presenter;

    private SongsListAdapter songsAdapter;

    private BroadcastReceiver networkChangedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_list);
        ButterKnife.bind(this);
        Log.d(Constants.LOG_TAG, "in oncreate");

        presenter = new SongsListPresenter(this);

        //display the progress bar to let the user know his songs list is being loaded.
        pbLoadingSongs.setVisibility(View.VISIBLE);
        isSongsListInitialized = false;

        networkChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(!NetworkUtil.isDeviceConnectedToWifi(context)){
                    //if network changed and the device is no longer connected to WiFi,
                    //finish the activity(calls onDestroy and shuts down the listening thread,
                    //since we can't disconnect from the server with no internet access.)

                    finish();
                }
            }
        };
        registerReceiver(networkChangedReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        //construct the songs list adapter and make it send a song to server each time a song is clicked.
        songsAdapter = new SongsListAdapter(songName -> presenter.sendSongToServer(songName));

        //now that the adapter is constructed, initialize our recycler view.
        initRecyclerView();

        //finally, connect to server to receive server responses and display our data.
        presenter.connectToServer();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        initSearchView(menu.findItem(R.id.menu_search));
        return true;
    }

    @Override
    //this method is called each time the menu gets reconstructed.
    public boolean onPrepareOptionsMenu(Menu menu) {
        setMenuItemEnabled(menu.findItem(R.id.menu_search), isSongsListInitialized);
        return super.onPrepareOptionsMenu(menu);
    }

    private void initSearchView(MenuItem searchViewItem){
        //define menu item specific behavior
        searchViewItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {
                //when overriding these methods, we must return true so the item expands.
                //(interface segregation principle, google?)
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                presenter.displayAllSongs();
                return true;
            }
        });

        //define searchview specific attributes
        SearchView searchView = (SearchView) searchViewItem.getActionView();
        searchView.setQueryHint(getStringResource(R.string.string_searchhint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //returning true means the overridden listener handled the text submit.
                //if it returned false, the searchview would execute its default behavior, which
                //is sending a new intent that starts a searchable activity. this way, no activity gets
                //started because submitting a search query is done dynamically.

                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                presenter.displaySongsThatMatchQuery(query);
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.d(Constants.LOG_TAG, "in ondestroy");
        if(networkChangedReceiver != null){
            unregisterReceiver(networkChangedReceiver);
        }

        if(NetworkUtil.isDeviceConnectedToWifi(this)){
            //cant notify server we disconnected if there's no internet.

            presenter.disconnectFromServer();
        } else {
            //if no internet, then just shut down the listener thread and exit.

            presenter.setListenerThreadRunning(false);
        }

        super.onDestroy();
    }

    @Override
    public void updateListWithSongs(List<String> songs) {
        runOnUiThread(() -> songsAdapter.updateSongs(songs));

        //Sometimes the menu gets created after receiving the songs list which is why I can't just
        //enable and disable the SearchView. Rather, I have to invalidate the whole menu and reconstruct
        //it once the songs list is received. This is because our SearchView is part of the menu.
        if(!isSongsListInitialized){
            isSongsListInitialized = true;
            runOnUiThread(() -> invalidateOptionsMenu());
            runOnUiThread(() -> pbLoadingSongs.setVisibility(View.INVISIBLE));

            Log.d(Constants.LOG_TAG, "invalidated options menu");
        }
    }

    @Override
    public void setNowPlayingSong(String songName) {
        SpannableString str;
        if(songName == null || songName.isEmpty()){
            //nothing is playing so the view displays nothing
            str = new SpannableString("");
        } else {
            //using spannable string allows bolding one part of the string and not the other.
            String nowPlayingLabel = getStringResource(R.string.string_nowplaying);
            str = new SpannableString(nowPlayingLabel + ": " + songName);
            str.setSpan(new StyleSpan(Typeface.BOLD), 0, nowPlayingLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        runOnUiThread(() ->tvNowPlayingSong.setText(str));
    }

    @Override
    public void setQueuedSong(String songName) {
        runOnUiThread(() -> tvQueuedSong.setText(songName));
    }

    @Override
    public void setQueuedSongPosition(int position) {
        String toDisplay = String.format(
                Locale.getDefault(),
                "%s(%s): ", getResources().getString(R.string.string_queued), StringFormattingUtil.attachOrdinalSuffix(position + 1)
        );
        runOnUiThread(() -> tvQueuedSongPosition.setText(toDisplay));
    }

    @Override
    public void clearQueuedSongView() {
        runOnUiThread(() -> tvQueuedSong.setText(null));
        runOnUiThread(() -> tvQueuedSongPosition.setText(null));
    }

    @Override
    public void showSuccessfulQueuedSongToast(String songName) {
        runOnUiThread(() -> ToastUtil.showShortToastWithMessage(
                this,
                String.format(Locale.getDefault(), "%s %s", songName, getStringResource(R.string.string_queued_confirmation_toast))
        ));
    }

    @Override
    public void showAlreadyQueuedSongToast(String songName) {
        ToastUtil.showShortToastWithMessage(
                this,
                String.format(Locale.getDefault(), "%s \"%s\"", getStringResource(R.string.string_alreadyqueued), songName)
        );
    }

    @Override
    public AssetManager getAssetManager() {
        return getAssets();
    }

    private void initRecyclerView() {
        rvSongs.setHasFixedSize(true);

        DividerItemDecoration decor = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        decor.setDrawable(getDrawable(R.drawable.listitem_divider));
        rvSongs.addItemDecoration(decor);

        rvSongs.setLayoutManager(new LinearLayoutManager(this));

        rvSongs.setAdapter(songsAdapter);
    }

    private String getStringResource(int resId){
        return getResources().getString(resId);
    }

    private void setMenuItemEnabled(MenuItem item, boolean isEnabled){
        item.setEnabled(isEnabled);
        item.getIcon().setAlpha(isEnabled ? 255 : 130);
    }
}
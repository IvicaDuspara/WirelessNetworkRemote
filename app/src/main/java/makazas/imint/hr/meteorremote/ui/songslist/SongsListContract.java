package makazas.imint.hr.meteorremote.ui.songslist;

import android.content.res.AssetManager;

import java.util.List;

public interface SongsListContract {

    interface View {
        void updateListWithSongs(List<String> songs);
        void setNowPlayingSong(String songName);
        void setQueuedSong(String songName);
        void setQueuedSongPosition(int position);
        void clearQueuedSongView();

        void showSuccessfulQueuedSongToast(String songName);
        void showAlreadyQueuedSongToast(String songName);

        AssetManager getAssetManager();
    }

    interface Presenter {
        void connectToServer();
        void sendSongToServer(String songName);
        void disconnectFromServer();

        void displaySongsThatMatchQuery(String query);
        void displayAllSongs();

        void setListenerThreadRunning(boolean isRunning);

        void handleSongsListResponse(List<String> songs);
        void handleQueuedSongsListResponse(List<String> queuedSongs);
        void handleNowPlayingResponse(List<String> responseBody);
        void handleEnqueuedResponse(List<String> responseBody);
        void handleMyQueuedSongResponse(List<String> responseBody);
        void handleMoveUpResponse(List<String> responseBody);
    }
}

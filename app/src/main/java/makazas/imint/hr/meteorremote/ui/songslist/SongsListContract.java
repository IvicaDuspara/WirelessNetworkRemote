package makazas.imint.hr.meteorremote.ui.songslist;

import java.util.List;

public interface SongsListContract {

    interface View {
        void updateListWithSongs(List<String> songs);
        void setNowPlayingSong(String songName);
        void setQueuedSong(String songName);
        void setQueuedSongPosition(int position);

        void showSuccessfulQueuedSongToast(String songName);

        void clearQueuedSongView();
    }

    interface Presenter {
        void initSongsFromServer(String ipAddress, String portNumber);
        void sendSongToServer(String songName);
    }
}

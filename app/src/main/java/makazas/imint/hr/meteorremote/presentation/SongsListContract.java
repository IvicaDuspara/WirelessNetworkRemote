package makazas.imint.hr.meteorremote.presentation;

import java.util.List;

public interface SongsListContract {

    interface View {
        void updateListWithSongs(List<String> songs);
    }

    interface Presenter {
        void initSongsFromServer(String ipAddress, String portNumber);
        void sendSongToServer(String songName);
    }
}

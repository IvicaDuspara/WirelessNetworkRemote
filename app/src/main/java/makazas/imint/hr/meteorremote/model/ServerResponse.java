package makazas.imint.hr.meteorremote.model;

import java.util.ArrayList;
import java.util.List;

import makazas.imint.hr.meteorremote.util.Constants;

public class ServerResponse {

    private ServerCode serverCode;
    private String queuedSongName;
    private String nowPlayingSong;
    private int positionInQueue;
    private List<String> queuedSongs;

    public ServerResponse(String response){
        queuedSongs = new ArrayList<>();
        parseResponse(response);
    }

    private void parseResponse(String response) {
        String[] information = response.split(Constants.SERVER_RESPONSE_SEPARATOR);

        serverCode = ServerCode.valueOf(information[0]);

        switch(serverCode){
            case SERVER_QUEUE_LIST:
                for(int i = 1; i < information.length; i++){
                    queuedSongs.add(information[i]);
                }
                break;
            case SERVER_MY_QUEUED_SONG:
                nowPlayingSong = information[1];
                queuedSongName = information[2];
                positionInQueue = Integer.parseInt(information[3]);
                break;
            case SERVER_ENQUEUED:
                queuedSongName = information[1];
                positionInQueue = Integer.parseInt(information[2]);
                break;
        }
    }

    public ServerCode getServerCode() {
        return serverCode;
    }

    public String getQueuedSongName() {
        return queuedSongName;
    }

    public String getNowPlayingSong() {
        return nowPlayingSong;
    }

    public int getPositionInQueue() {
        return positionInQueue;
    }

    public List<String> getQueuedSongs() {
        return queuedSongs;
    }
}

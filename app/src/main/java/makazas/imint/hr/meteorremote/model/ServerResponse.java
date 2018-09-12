package makazas.imint.hr.meteorremote.model;

import java.util.ArrayList;
import java.util.List;

import makazas.imint.hr.meteorremote.util.Constants;

public class ServerResponse {

    private ServerCode serverCode;
    private String queuedSong;
    private String nowPlayingSong;
    private int positionInQueue;
    private List<String> queuedSongs;
    private List<String> allSongs;

    public ServerResponse(String response){
        queuedSongs = new ArrayList<>();
        allSongs = new ArrayList<>();
        parseResponse(response);
    }

    private void parseResponse(String response) {
        String[] information = response.split(Constants.SERVER_RESPONSE_SEPARATOR);

        serverCode = ServerCode.valueOf(information[0]);

        switch(serverCode){
            case SERVER_QUEUE_LIST:
                addAllInformationLinesToList(queuedSongs, information, 1);
                break;
            case SERVER_MY_QUEUED_SONG:
                queuedSong = information[1];
                positionInQueue = Integer.parseInt(information[2]);
                break;
            case SERVER_ENQUEUED:
                queuedSong = information[1];
                positionInQueue = Integer.parseInt(information[2]);
                break;
            case SERVER_NOW_PLAYING:
                // TODO: 12-Sep-18 remove this when main app is updated to not send nowplaying if there is nothing playing
                nowPlayingSong = information.length == 1 ? null : information[1];
                break;
            case SERVER_SONG_LIST:
                addAllInformationLinesToList(allSongs, information, 1);
                break;
        }
    }

    private void addAllInformationLinesToList(List<String> list, String[] information, int startIndex){
        for(int i = startIndex; i < information.length; i++){
            list.add(information[i]);
        }
    }

    public ServerCode getServerCode() {
        return serverCode;
    }

    public String getQueuedSong() {
        return queuedSong;
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

    public List<String> getAllSongs() {
        return allSongs;
    }
}

package makazas.imint.hr.meteorremote.model;

import java.util.ArrayList;
import java.util.List;

import makazas.imint.hr.meteorremote.util.Constants;

/**
 * Parses various server responses and provides getter methods to access the results. <br>
 *
 * <br>If a server response doesn't contain a {@link String} field, the getter returns <code>null</code>.
 * <br>If a server response doesn't contain an {@link Integer} field, the getter returns <code>-2</code>.
 * <br>If a server response doesn't contain a {@link List} field, the getter returns an empty {@link ArrayList}.<br>
 *
 * <br>However, if the server response SHOULD contain a {@link String} field, but there is nothing there,
 * the getter returns an empty string. Integer and list getters return the values given above.
 */
public class ServerResponse {

    private ServerCode serverCode;

    private String queuedSong = null;
    private String nowPlayingSong = null;
    private int positionInQueue = -2;

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
                addAllArrayLinesToList(queuedSongs, information, 1);
                break;
            case SERVER_MY_QUEUED_SONG:
                setNowPlayingSong(information[1]);
                setQueuedSong(information[2]);
                setPositionInQueue(information[3]);
                break;
            case SERVER_ENQUEUED:
                setQueuedSong(information[1]);
                setPositionInQueue(information[2]);
                break;
            case SERVER_NOW_PLAYING:
                setNowPlayingSong(information[1]);
                break;
            case SERVER_SONG_LIST:
                addAllArrayLinesToList(allSongs, information, 1);
                break;
        }
    }

    private void addAllArrayLinesToList(List<String> list, String[] array, int startIndex){
        for(int i = startIndex; i < array.length; i++){
            list.add(array[i]);
        }
    }

    private void setQueuedSong(String queuedSong) {
        this.queuedSong = queuedSong != null ? queuedSong : "";
    }

    private void setNowPlayingSong(String nowPlayingSong) {
        this.nowPlayingSong = nowPlayingSong != null ? nowPlayingSong : "";
    }

    private void setPositionInQueue(String positionInQueue) {
        int pos;
        try{
            pos = Integer.parseInt(positionInQueue);
        } catch(NumberFormatException e){
            pos = -2;
        }
        this.positionInQueue = pos;
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

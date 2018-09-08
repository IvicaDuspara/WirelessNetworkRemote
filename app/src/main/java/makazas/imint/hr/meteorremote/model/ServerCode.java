package makazas.imint.hr.meteorremote.model;

/**
 * Various codes that the server sends. Each {@link ServerCode} is followed by the body of the
 * response, which varies depending on the code.<br>
 *
 * <br>After the body a {@link ServerCode#SERVER_BROADCAST_ENDED} always follows to denote end of current response.
 */
public enum ServerCode {
    /**
     * Sent at the end of each server broadcast, denoting end of response.
     */
    SERVER_BROADCAST_ENDED,

    /**
     * Sent when the server queue moves up, a.k.a when the next song plays.
     */
    SERVER_MOVE_UP,

    /**
     * Sent when a song was enqueued or swapped.
     * After this code, data is sent in this order(separated by newline):<br>
     *
     * <br> name of queued or swapped song.
     * <br> current position of queued or swapped song.
     */
    SERVER_ENQUEUED,

    /**
     * Sent when the server sends its current list of queued songs.
     * After this code, data is sent in this order(separated by newline):<br>
     *
     * <br> list of queued songs, each separated by a newline.
     */
    SERVER_QUEUE_LIST,

    /**
     * Sent by the server upon client connection if he was previously connected and his song
     * is still in queue.
     * After this code, data is sent in this order(separated by newline):<br>
     *
     * <br> name of clients queued song.
     */
    SERVER_MY_QUEUED_SONG,

    //sent when someone manually plays a song on the server
    /**
     * Sent when the server sends a client the song that is currently playing.
     * After this code, data is sent in this order(separated by newline):<br>
     *
     * <br> name of now playing song.
     */
    SERVER_NOW_PLAYING,

    //sent before the whole server available song list.
    /**
     * First thing the server sends to client after client connects.
     * After this code, data is sent in this order(separated by newline):<br>
     *
     * <br> list of all available songs on the server, each separated by newline.
     */
    SERVER_SONG_LIST
}

package makazas.imint.hr.meteorremote;

public enum Codes {
    /*Signal to server that client has disconnected*/
    CLIENT_DISCONNECT,

    /*Signal to server that client is sending mac address*/
    CLIENT_MAC_ADDRESS,

    /*Signal to server that client is sending a song request*/
    CLIENT_QUEUE,

    /*Signal to server that  a song should be played immediately*/
    censored,

    /*Signals an end of data broadcast*/
    SERVER_BROADCAST_ENDED,

    /*Signals that client's song has successfully been enqueued*/
    SERVER_ENQUEUED,

    /*Signals that clients should update their's queues*/
    SERVER_MOVE_UP,

    /*Signals to a client that client has a queued song*/
    SERVER_MY_QUEUED_SONG,

    /*Signals sending of currently playing song*/
    SERVER_NOW_PLAYING,

    /*Signals sending of queued songs*/
    SERVER_QUEUE_LIST,

    /*Signals sending of loaded songs*/
    SERVER_SONG_LIST
}

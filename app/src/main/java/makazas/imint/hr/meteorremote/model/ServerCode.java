package makazas.imint.hr.meteorremote.model;

public enum ServerCode {
    //sent on end of each broadcast
    SERVER_BROADCAST_ENDED,

    //sent after queue moves up
    SERVER_MOVE_UP,

    //sent when user swaps or queues song. After code comes: queued song, newline, current position
    SERVER_ENQUEUED,

    //sent after songs list. After code: queued song Ends with SERVER_BROADCAST_ENDED
    SERVER_QUEUE_LIST,

    //sent if my queued song already exists. After code: now playing, queued, queuepos
    SERVER_MY_QUEUED_SONG
}

package makazas.imint.hr.meteorremote.model;

/**
 * Various codes the client sends to the server.
 */
public enum ClientCode {
    /**
     * Sent to server when the client wants to disconnect.
     */
    CLIENT_DISCONNECT,

    /**
     * Sent to server when the client wants to enqueue or swap his song.
     */
    CLIENT_QUEUE,

    /**
     * Sent to server when the client sends his mac address.
     */
    CLIENT_MAC_ADDRESS
}

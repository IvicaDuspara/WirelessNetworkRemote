package makazas.imint.hr.meteorremote.presentation;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import makazas.imint.hr.meteorremote.factory.ResponseFactory;
import makazas.imint.hr.meteorremote.networking.SocketSingleton;
import makazas.imint.hr.meteorremote.multithreading.ServerResponseChangedObserver;
import makazas.imint.hr.meteorremote.multithreading.ServerResponseListenerThread;
import makazas.imint.hr.meteorremote.serverresponse.IResponse;
import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;
import makazas.imint.hr.meteorremote.util.Constants;
import makazas.imint.hr.meteorremote.networking.NetworkUtil;

public class SongsListPresenter implements SongsListContract.Presenter, ServerResponseChangedObserver {

    private SongsListContract.View view;

    private Socket clientSocket;
    private BufferedWriter clientSocketWriter;
    private BufferedReader clientSocketReader;

    private ServerResponseListenerThread listenerThread;
    private ResponseFactory responseFactory;

    private int clientSongIndex;
    private String clientQueuedSong;
    private LinkedList<String> allQueuedSongs;
    private List<String> allSongs;

    public SongsListPresenter(SongsListContract.View view) {
        this.view = view;
        this.allQueuedSongs = new LinkedList<>();
        this.allSongs = new ArrayList<>();
        this.clientSongIndex = -1;
        this.responseFactory = new ResponseFactory(this, view.getAssetManager());
    }

    private void sendMacAddressToServer() throws IOException {
        clientSocketWriter.write(Constants.CLIENT_MAC_ADDRESS_CODE);
        clientSocketWriter.newLine();
        clientSocketWriter.write(NetworkUtil.getMacAddress());
        clientSocketWriter.newLine();
        clientSocketWriter.flush();
    }

    private void startListeningForServerResponses() {
        listenerThread = new ServerResponseListenerThread();
        listenerThread.setBufferedReader(clientSocketReader);
        listenerThread.setDaemon(true);
        listenerThread.attach(this);
        listenerThread.start();
    }

    private void setQueuedSongIfNotExists(String songName) {
        if (clientQueuedSong == null) {
            allQueuedSongs.addLast(songName);
            clientQueuedSong = songName;
            clientSongIndex = allQueuedSongs.indexOf(clientQueuedSong);
        }
    }

    @Override
    public void displaySongsThatMatchQuery(String query) {
        Pattern searchPattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);

        List<String> matchingSongs = new ArrayList<>();

        for (String song : allSongs) {
            if (searchPattern.matcher(song).find()) {
                matchingSongs.add(song);
            }
        }

        view.updateListWithSongs(matchingSongs);
    }

    @Override
    public void displayAllSongs() {
        view.updateListWithSongs(allSongs);
    }

    @Override
    public void setListenerThreadRunning(boolean isRunning) {
        listenerThread.setRunning(isRunning);
    }

    @Override
    public void update(String responseString) {
        IResponse response = responseFactory.createResponse(responseString);
        response.executeStrategy(this);
    }

    @Override
    public void handleSongsListResponse(String[] songs) {
        allSongs = Arrays.asList(songs);
        view.updateListWithSongs(allSongs);
    }

    @Override
    public void handleQueuedSongsListResponse(String[] queuedSongs) {
        allQueuedSongs.addAll(Arrays.asList(queuedSongs));
    }

    public void handleNowPlayingResponse(String[] responseBody) {
        String nowPlaying = null;
        if(responseBody.length != 0){
            // TODO: 12-Sep-18 remove this when main app is updated to not send nowplaying if there is nothing playing
            // ^ todo left from last year, figure it out
            nowPlaying = responseBody[0];
        }

        view.setNowPlayingSong(nowPlaying);
    }

    @Override
    public void handleMyQueuedSongResponse(String[] responseBody) {
        clientQueuedSong = responseBody[0];
        clientSongIndex = Integer.parseInt(responseBody[1]);

        view.setQueuedSong(clientQueuedSong);
        view.setQueuedSongPosition(clientSongIndex);
    }

    @Override
    public void handleEnqueuedResponse(String[] responseBody) {
        String queuedSong = responseBody[0];
        int posInQueue = Integer.parseInt(responseBody[1]);

        if (posInQueue == clientSongIndex) {
            //if the currently enqueued song's position equals clients queued song index,
            //this means he swapped his song.

            clientQueuedSong = queuedSong;
            allQueuedSongs.set(clientSongIndex, queuedSong);

            view.showSuccessfulQueuedSongToast(clientQueuedSong);
            view.setQueuedSong(clientQueuedSong);
            view.setQueuedSongPosition(clientSongIndex);

        } else if (posInQueue == allQueuedSongs.size()) {
            //if the currently enqueued song's position is greater than the size of all queued songs
            //this means ANOTHER client enqueued a song. therefore we update our list of queued songs.

            allQueuedSongs.addLast(queuedSong);

        } else {
            //if the currently enqueued song's position isn't greater than the size of all queued songs
            //and its index isn't equal to this client's song position, another user swapped his song.

            allQueuedSongs.set(posInQueue, queuedSong);
        }
    }

    @Override
    public void handleMoveUpResponse(String[] responseBody) {
        if (clientSongIndex == -1) {
            //if the client hasn't queued anything yet, his queued song index is -1,
            //denoting the song doesn't exist in the list of queued songs.

            return;
        }

        //if it's not -1, this means he has a queued song.
        //when the queue list moves up, his queued song index reduces by one.
        clientSongIndex--;

        if (clientSongIndex == -1) {
            //if his song index is now -1, this means the song has reached the top of the
            //queue and is now playing.

            view.setNowPlayingSong(clientQueuedSong);
            view.clearQueuedSongView();

            allQueuedSongs.removeFirst();
            clientQueuedSong = null;
            clientSongIndex = -1;

        } else {
            //if his song index isn't -1, we just update the song's position.

            view.setQueuedSongPosition(clientSongIndex);
        }
    }

    @Override
    public void connectToServer() {
        new ConnectToServerTask(this).execute();
    }

    @Override
    public void sendSongToServer(String song) {
        if(song.equals(clientQueuedSong)){
            view.showAlreadyQueuedSongToast(clientQueuedSong);
            return;
        }

        setQueuedSongIfNotExists(song);
        new SendSongToServerTask(this).execute(song);
    }

    @Override
    public void disconnectFromServer() {
        new DisconnectFromServerTask(this).execute();
    }

    private static class ConnectToServerTask extends AsyncTask<String, String, Void> {

        private WeakReference<SongsListPresenter> presenterReference;

        ConnectToServerTask(SongsListPresenter presenter) {
            presenterReference = new WeakReference<>(presenter);
        }

        @Override
        protected Void doInBackground(String... strings) {
            SongsListPresenter presenter = presenterReference.get();

            if (presenter == null) return null;

            try {
                presenter.clientSocket = SocketSingleton.getInstance().getSocket();
                presenter.clientSocketReader = new BufferedReader(new InputStreamReader(presenter.clientSocket.getInputStream(), Charset.forName("UTF-8")));
                presenter.clientSocketWriter = new BufferedWriter(new OutputStreamWriter(presenter.clientSocket.getOutputStream(), Charset.forName("UTF-8")));

                presenter.sendMacAddressToServer();
                presenter.startListeningForServerResponses();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(Constants.LOG_TAG, "Connect to server task stopped");
            return null;
        }
    }

    private static class SendSongToServerTask extends AsyncTask<String, String, Void> {
        private WeakReference<SongsListPresenter> presenterReference;

        SendSongToServerTask(SongsListPresenter presenter) {
            presenterReference = new WeakReference<>(presenter);
        }

        @Override
        protected Void doInBackground(String... strings) {
            SongsListPresenter presenter = presenterReference.get();

            if (presenter == null) return null;

            try {
                presenter.clientSocketWriter.write(Constants.CLIENT_QUEUE_CODE);
                presenter.clientSocketWriter.newLine();
                presenter.clientSocketWriter.write(NetworkUtil.getMacAddress());
                presenter.clientSocketWriter.newLine();
                presenter.clientSocketWriter.write(strings[0]);
                presenter.clientSocketWriter.newLine();
                presenter.clientSocketWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Log.d(Constants.LOG_TAG, "Send song to server task stopped");
            return null;
        }
    }

    private static class DisconnectFromServerTask extends AsyncTask<String, String, Void> {

        private WeakReference<SongsListPresenter> presenterReference;

        DisconnectFromServerTask(SongsListPresenter presenter) {
            presenterReference = new WeakReference<>(presenter);
        }

        @Override
        protected Void doInBackground(String... strings) {
            SongsListPresenter presenter = presenterReference.get();

            if (presenter == null) return null;

            if (presenter.clientSocketWriter != null) {
                try {
                    presenter.clientSocketWriter.write(Constants.CLIENT_DISCONNECT_CODE);
                    presenter.clientSocketWriter.newLine();
                    presenter.clientSocketWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (presenter.clientSocket != null) {
                try {
                    SocketSingleton.getInstance().closeSocket();
                    presenter.clientSocketReader.close();
                    presenter.clientSocketWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if(presenter.listenerThread != null) {
                presenter.setListenerThreadRunning(false);
            }

            NetworkUtil.logIfClosed(presenter.clientSocket, presenter.clientSocketReader, presenter.clientSocketWriter);
            Log.d(Constants.LOG_TAG, "Disconnect from server task stopped");

            return null;
        }
    }
}


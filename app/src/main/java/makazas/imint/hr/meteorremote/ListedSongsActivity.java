package makazas.imint.hr.meteorremote;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;


import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;

import java.net.Socket;


import java.util.ArrayList;
import java.util.List;


import makazas.imint.hr.meteorremote.listeners.ClickListenerImplementation;
import makazas.imint.hr.meteorremote.makazas.imint.hr.meteorremote.jobs.ListeningJob;
import makazas.imint.hr.meteorremote.songslist.SimpleRecyclerAdapter;


public class ListedSongsActivity extends AppCompatActivity {


    /**
     * Reference to a {@code RecyclerView} used in layout
     */
    private RecyclerView mRecyclerView;

    /**
     * Adapter used in {@link #mRecyclerView}
     */
    private RecyclerView.Adapter mAdapter;


    /**
     * List in which song names are loaded
     */
    private List<String> listOfSongs;

    private List<String> queueOfSongs;

    private String currentlyPlaying;


    /**
     * Socket on which client connects.
     */
    private Socket clientSocket;

    /**
     * Wrapper writer for {@code clientSocket}'s {@code outputStream}
     */
    private BufferedWriter clientSocketWriter;

    /**
     * Wrapper reader for {@code clientSocket}'s {@code inputStream}
     */
    private BufferedReader clientSocketReader;

    /**
     * Saved MACAddress for this client
     */
    private String MACAddress;

    private ClickListenerImplementation cli;


    /**
     * ListeningThread
     */
    private ListeningJob listeningJob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listed_songs);

        listOfSongs = new ArrayList<>();
        queueOfSongs = new ArrayList<>();
        mRecyclerView =  findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        DividerItemDecoration decor = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        decor.setDrawable(getDrawable(R.drawable.listitem_divider));
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.addItemDecoration(decor);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SimpleRecyclerAdapter(listOfSongs);
        mRecyclerView.setAdapter(mAdapter);
        listeningJob = new ListeningJob(queueOfSongs,this);
        startSocket();
        cli = new ClickListenerImplementation(listOfSongs,MACAddress);
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(),mRecyclerView,cli));



    }


    /**
     * Connects {@link #clientSocket clientSocket} to a server. Server's IP address and port are passed as a {@code Bundle} in a intent,
     * bundled in {@link MainActivity MainActivity}.<br>
     *
     * If this method is called without a {@code Bundle} or this activity is started without a {@code Intent} a message will be displayed<br><br>
     *
     * <b>Note:</b> this method does not check validity of IPv4 address or validity of a port. That job is done when constructing a {@link java.net.SocketAddress SocketAddress} object.
     */
    private void startSocket() {
        if(getIntent() != null && getIntent().getExtras() != null) {
            String ipaddr = getIntent().getStringExtra(MainActivity.IP_ADDRESS);
            int portNumber = getIntent().getIntExtra(MainActivity.PORT,0);
            MACAddress = getIntent().getStringExtra(MainActivity.MAC);
            InitializationJob job = new InitializationJob(ipaddr,portNumber);
            Thread thread = new Thread(job);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @Override
    public void onBackPressed() {
        if(clientSocket != null) {
            try {
                clientSocketWriter.write(Codes.CLIENT_DISCONNECT.toString());
                clientSocketWriter.newLine();
                clientSocketWriter.flush();
                clientSocket.close();
            } catch (IOException ex) {
                Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        listeningJob.setRunning(false);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(clientSocket != null) {
            try {
                clientSocketWriter.write(Codes.CLIENT_DISCONNECT.toString());
                clientSocketWriter.newLine();
                clientSocketWriter.flush();
                clientSocket.close();
            } catch (IOException ex) {
                Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        listeningJob.setRunning(false);
    }


    private void startListeningJob() {
        listeningJob.setClientSocketReader(clientSocketReader);
        Thread t = new Thread(listeningJob);
        t.setDaemon(true);
        t.start();
    }
    /**
     * Models an initialization job.<br>
     * When a client connects to a server, server will send following things to client:
     * <ul>
     *      <li>List of all songs</li>
     *      <li>List of queued songs</li>
     *      <li>Currently playing song</li>
     *      <li>Client's queued song if it exists</li>
     * </ul>
     * This job reads above listed information and stores it into appropriate fields.
     *
     *
     * @author Ivica Duspara
     * @version  1.0
     */
    private class InitializationJob implements Runnable {
        private String ipaddr;
        private int portNumber;


        InitializationJob(String ipaddr, int portNumber) {
            this.ipaddr = ipaddr;
            this.portNumber = portNumber;

        }

        /**
         * Populates {@code populated} from {@link #clientSocketReader} until {@link Codes#SERVER_BROADCAST_ENDED} is encountered.
         * First {@code String} read is ignored.<br><br>
         *
         * <b>NOTE: </b> this implementation ignores first string - enum command -  and handles reading of strings based on {@code populated}.
         * These commands are sent from server because actual application for users requires commands for functioning. This version, however, is not
         * a release version hence the tweaks
         *
         * @param populated List which will be populated
         *
         * @throws IOException if an I/O error occurs while reading lines
         */
        private void populateListOfSongs(List<String> populated) throws  IOException {
            clientSocketReader.readLine();
            String str;
            while(!(str = clientSocketReader.readLine()).equals(Codes.SERVER_BROADCAST_ENDED.toString())) {
                populated.add(str);
            }
        }


        /**
         * Sets {@code currentPlaying}
         *
         * @throws IOException if an I/O error occurs while reading lines
         */
        private void setCurrentlyPlayingSong() throws IOException{
            clientSocketReader.readLine();
            currentlyPlaying = clientSocketReader.readLine();
            clientSocketReader.readLine();
        }


        /**
         * Writes MAC address of this client to server.
         *
         * @throws IOException if an I/O error occurs while writing lines
         */
        private void writeMACAddress() throws IOException{
            clientSocketWriter.write(Codes.CLIENT_MAC_ADDRESS.toString());
            clientSocketWriter.newLine();
            clientSocketWriter.write(MACAddress);
            clientSocketWriter.newLine();
            clientSocketWriter.flush();
        }



        @Override
        public void run() {
            try {
                clientSocket = new Socket(InetAddress.getByName(ipaddr),portNumber);
                clientSocketReader = new BufferedReader(new InputStreamReader(ListedSongsActivity.this.clientSocket.getInputStream()));
                clientSocketWriter = new BufferedWriter(new OutputStreamWriter(ListedSongsActivity.this.clientSocket.getOutputStream()));
                cli.setClientSocketReader(clientSocketReader);
                cli.setClientSocketWriter(clientSocketWriter);
                populateListOfSongs(listOfSongs);
                populateListOfSongs(queueOfSongs);
                setCurrentlyPlayingSong();
                writeMACAddress();
                startListeningJob();
                runOnUiThread(() -> {
                    mAdapter.notifyDataSetChanged();
                    TextView nowPlayingText = findViewById(R.id.nowPlayingText);
                    nowPlayingText.setText(currentlyPlaying);

                });
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }
}
package makazas.imint.hr.meteorremote;

import android.icu.util.LocaleData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;

import java.net.Socket;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;


public class ListedSongsActivity extends AppCompatActivity {

    private static final String CLIENT_CLOSED = "CLIENT:DISCONNECT";
    /**
     * Reference to a {@code RecyclerView} used in layout
     */
    private RecyclerView mRecyclerView;

    /**
     * Adapter used in {@link #mRecyclerView}
     */
    private RecyclerView.Adapter mAdapter;

    /**
     * LayoutManager used in {@link #mRecyclerView}
     */
    private RecyclerView.LayoutManager mLayoutManager;

    /**
     * List in which song names are loaded
     */
    private List<String> recyclerList;

    /**
     * Socket on which client connects.
     */
    private Socket clientSocket;

    /**
     * Wrapper writer for <code>clientSocket</code>'s <code>outputStream</code>
     */
    private BufferedWriter clientSocketWriter;

    private String MACAddress;

    /**
     * Adapter class used for {@code RecyclerView}.<br>
     *
     */
    private static class SimpleRecyclerAdapter extends RecyclerView.Adapter<SimpleRecyclerAdapter.ViewHolder>{
        /**
         * {@code List} which is used to populate {@code RecyclerView}
         */
        private List<String> recyclerList;

        /**
         * Sub class of {@link RecyclerView.ViewHolder} used for holding layout items of {@code RecyclerView}.<br>
         * Since {@link SimpleRecyclerAdapter} shows only one widget - a {@link TextView}, this {@code ViewHolder} will
         * only have a reference to one {@code TextView} widget.
         */
        public  class ViewHolder extends RecyclerView.ViewHolder{

            /**
             * {@code TextView} used for displaying a title of a song
             */
            public TextView title;

            /**
             * Constructs a new {@code ViewHolder} item with given parameter.
             *
             * @param itemView
             *        {@code View} which holds a {@code TextView} which will be used for displaying song titles.
             *
             * @throws  NullPointerException
             *          if <code>itemView</code> is <code>null</code>
             */
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.song);
            }

        }


        /**
         * Constructs a new {@code SimpleRecyclerAdapter} with given parameter.
         *
         * @param recyclerList
         *        {@code List} which provides displayed data in {@link RecyclerView}
         *
         * @throws NullPointerException
         *         if <code>recyclerList</code> is <code>null</code>
         */
        SimpleRecyclerAdapter(List<String> recyclerList) {
            Objects.requireNonNull(recyclerList);
            this.recyclerList = recyclerList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View tv =  LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.songs,viewGroup,false);
            return new ViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
            viewHolder.title.setText(recyclerList.get(i));
        }

        @Override
        public int getItemCount() {
            return recyclerList.size();
        }


    }

    /**
     * Task used for communicating with a server.
     * Lists available songs to user.
     */
    private class NetworkTask extends AsyncTask<String,String,Void> {

        /**
         * Signals an end of a broadcast
         */
        private static final String BROADCAST_END = "SERVER_BROADCAST_ENDED";

        /**
         * Connects to a server, reads song names and updates {@link RecyclerView RecyclerView}.<br>
         * Data from server is read as {@code Strings} until {@link #BROADCAST_END} is read. Each read
         * {@code String} is added to {@link #recyclerList} of this {@link ListedSongsActivity <code>ListedSongsActivity</code>}.<br><br>
         *
         * This method will throw an exception if any of following occurs:
         * <ul>
         *     <li>Invalid IPv4/IPv6 address or invalid port number</li>
         *     <li>I/O error occurs while creating a socket or while creating {@link java.io.InputStream <code>InputStream</code>} of a socket</li>
         *     <li>Socket is closed or not connected</li>
         * </ul>
         *
         * @param strings
         *        An array with two elements:
         *        <ol>
         *            <li>IPv4 address</li>
         *            <li>Port number</li>
         *        </ol>
         * @return  <code>null</code>
         *          Since progress of a task is not tracked, null is returned
         *
         * @throws UnknownHostException
         *         if provided IPv4/IPv6 address or port number in <code>strings</code> aren't not valid
         *
         * @throws  IOException
         *          if an I/O error occurs while creating a socket or while creating socket's input stream or while reading data.
         *
         */
        @Override
        protected Void doInBackground(String... strings) {
            int port = Integer.parseInt(strings[1]);
            try {
                ListedSongsActivity.this.clientSocket = new Socket(InetAddress.getByName(strings[0]),port);
                BufferedReader br = new BufferedReader(new InputStreamReader(ListedSongsActivity.this.clientSocket.getInputStream()));
                ListedSongsActivity.this.clientSocketWriter = new BufferedWriter(new OutputStreamWriter(ListedSongsActivity.this.clientSocket.getOutputStream()));
                String str;
                while(!(str = br.readLine()).equals(BROADCAST_END)) {
                    ListedSongsActivity.this.recyclerList.add(str);
                    Log.d("KREE",str);
                }
                ListedSongsActivity.this.runOnUiThread(() -> ListedSongsActivity.this.mAdapter.notifyDataSetChanged());

            } catch (IOException e) {
                e.printStackTrace();

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listed_songs);
        recyclerList = new ArrayList<>();

        mRecyclerView =  findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SimpleRecyclerAdapter(recyclerList);
        mRecyclerView.setAdapter(mAdapter);
        startSocket();
        Log.d("KREE","Ich bin hier");

        //mAdapter.notifyDataSetChanged();
        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String s = recyclerList.get(position);
                try {
                    clientSocketWriter.write(getString(R.string.admin));
                    clientSocketWriter.newLine();
                    clientSocketWriter.write(s);
                    clientSocketWriter.newLine();
                    clientSocketWriter.flush();
                }catch(IOException ex) {
                    TextView tv = findViewById(R.id.LSAdispaly);
                    tv.setText(ex.getMessage());
                }

            }

            @Override
            public void onLongClick(View view, int position) {
                String s = recyclerList.get(position);
                try {
                    clientSocketWriter.write(getString(R.string.client));
                    clientSocketWriter.newLine();
                    clientSocketWriter.write(MACAddress);
                    clientSocketWriter.newLine();
                    clientSocketWriter.write(s);
                    clientSocketWriter.newLine();
                    clientSocketWriter.flush();
                }catch(IOException ex) {
                    TextView tv = findViewById(R.id.LSAdispaly);
                    tv.setText(ex.getMessage());
                }
            }
        }));
        Button b = findViewById(R.id.returnButton);
        b.setOnClickListener(l -> {
            mAdapter.notifyDataSetChanged();
        });
        Log.d("KREE","gotovsan");

    }


    /**
     * Connects {@link #clientSocket clientSocket} to a server. Server's IP address and port are passed as a {@code Bundle} in a intent,
     * bundled in {@link MainActivity MainActivity}.<br>
     *
     * If this method is called without a {@code Bundle} or this activity is started without a {@code Intent} a message will be displayed<br><br>
     *
     * <b>Note:</b> this method does not check validity of IPv4 address or validity of a port. That job is done when constructing a {@link java.net.SocketAddress SocketAddress} object.
     * See {@link NetworkTask NetworkTask}.
     */
    private void startSocket() {
        if(getIntent() != null && getIntent().getExtras() != null) {
            String ipaddr = getIntent().getStringExtra(MainActivity.IP_ADDRESS);
            int portNumber = getIntent().getIntExtra(MainActivity.PORT,0);
            MACAddress = getIntent().getStringExtra("MAC");
            NetworkTask nt = new NetworkTask();

                nt.execute(ipaddr,Integer.toString(portNumber));

        }
        else {
            TextView tv = findViewById(R.id.LSAdispaly);
            tv.setText(R.string.bundleerror);
        }
    }

    @Override
    public void onBackPressed() {
        if(clientSocketWriter != null) {
            try {
                clientSocketWriter.write(CLIENT_CLOSED);
                clientSocketWriter.newLine();
                clientSocketWriter.flush();
                clientSocketWriter.close();
            }catch(IOException ex) {
                Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        if(clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException ex) {
                Toast.makeText(this,ex.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }
}
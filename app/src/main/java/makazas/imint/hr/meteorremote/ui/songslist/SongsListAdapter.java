package makazas.imint.hr.meteorremote.ui.songslist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import makazas.imint.hr.meteorremote.R;
import makazas.imint.hr.meteorremote.SongClickListener;

/**
 * Adapter class used for {@code RecyclerView}.<br>
 *
 */
public class SongsListAdapter extends RecyclerView.Adapter<SongsListAdapter.SongViewHolder>{
    /**
     * {@code List} which is used to populate {@code RecyclerView}
     */
    private List<String> songs;

    private SongClickListener songClickListener;

    /**
     * Constructs a new {@code SongsListAdapter} with given parameter.
     *
     * @throws NullPointerException
     *         if <code>listitem_song</code> is <code>null</code>
     */
    public SongsListAdapter(SongClickListener listener) {
        this.songs = new ArrayList<>();
        this.songClickListener = listener;
    }

    public void updateSongs(List<String> songs){
        this.songs = songs;
        notifyDataSetChanged();
    }

    public String getSong(int index){
        return songs.get(index);
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View tv = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.listitem_song,viewGroup,false);
        return new SongViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder viewHolder, int i) {
        viewHolder.tvTitle.setText(songs.get(i));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    /**
     * Sub class of {@link RecyclerView.ViewHolder} used for holding layout items of {@code RecyclerView}.<br>
     * Since {@link SongsListAdapter} shows only one widget - a {@link TextView}, this {@code SongViewHolder} will
     * only have a reference to one {@code TextView} widget.
     */
    public class SongViewHolder extends RecyclerView.ViewHolder {

        /**
         * {@code TextView} used for displaying a tvTitle of a song
         */
        @BindView(R.id.song)
        public TextView tvTitle;

        /**
         * Constructs a new {@code SongViewHolder} item with given parameter.
         *
         * @param itemView
         *        {@code View} which holds a {@code TextView} which will be used for displaying song titles.
         *
         * @throws  NullPointerException
         *          if <code>itemView</code> is <code>null</code>
         */
        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick
        public void onSongClick(){
            songClickListener.onClick(getSong(getAdapterPosition()));
        }
    }
}

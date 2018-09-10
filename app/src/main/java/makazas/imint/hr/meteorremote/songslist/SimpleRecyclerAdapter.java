package makazas.imint.hr.meteorremote.songslist;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;
import makazas.imint.hr.meteorremote.R;

/**
 *
 */
public class SimpleRecyclerAdapter extends RecyclerView.Adapter<SimpleRecyclerAdapter.ViewHolder> {
    /**
     * {@code List} which is used to populate {@code RecyclerView}
     */
    private List<String> songs;

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
         * @param itemView {@code View} which holds a {@code TextView} which will be used for displaying song titles.
         *
         * @throws  NullPointerException if {@code itemView} is {@code null}
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.song);
        }

    }


    /**
     * Constructs a new {@code SimpleRecyclerAdapter} with given parameter.
     *
     * @param songs {@code List} which provides displayed data in {@link RecyclerView}
     *
     * @throws NullPointerException if {@code songs} is {@code null}
     */
    public SimpleRecyclerAdapter(List<String> songs) {
        Objects.requireNonNull(songs);
        this.songs = songs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View tv =  LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.songs,viewGroup,false);
        return new ViewHolder(tv);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.title.setText(songs.get(i));
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

}

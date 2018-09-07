package makazas.imint.hr.meteorremote.ui.songslist;

import android.view.View;

/**
 * Interface which models a click listener for {@link android.support.v7.widget.RecyclerView RecyclerView}.<br>
 *
 */
public interface SongClickListener {

    /**
     * Called when a click is performed on a item of {@code RecyclerView)
     *
     * @param position
     *        position of an item which was clicked.
     */
    void onClick(String songName);
}

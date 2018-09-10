package makazas.imint.hr.meteorremote.listeners;

import android.view.View;

/**
 * Interface which models a click listener for {@link android.support.v7.widget.RecyclerView RecyclerView}.<br>
 *
 */
public interface ClickListener {

    /**
     * Called when a click is performed on a item of {@code RecyclerView}
     *
     * @param view {@code RecyclerView} on which a click is performed
     *
     * @param position of an item which was clicked.
     */
    void onClick(View view, int position);

    /**
     * Called when a long click is performed on a item of {@code RecyclerView}
     *
     * @param view {@code RecyclerView} on which a long click is performed
     *
     * @param position of an item which was long clicked.
     */
    void onLongClick(View view, int position);
}

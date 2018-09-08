package makazas.imint.hr.meteorremote.multithreading;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;

import makazas.imint.hr.meteorremote.model.ServerResponse;

/**
 * An observable thread that passes each server response to all of its observers.
 */
public abstract class ObservableThread extends Thread {

    private List<ServerResponseChangedObserver> observers;

    public ObservableThread(){
        observers = new ArrayList<>();
    }

    /**
     * Attaches {@code observer} to this thread.
     *
     * @param   observer    observes server responses and gets updated on changes.
     */
    public void attach(ServerResponseChangedObserver observer){
        observers.add(observer);
    }

    /**
     * Detaches {@code observer} from this thread.
     *
     * @param   observer    currently attached observer to be detached.
     *                      If no such observer is attached, nothing happens.
     */
    public void detach(ServerResponseChangedObserver observer){
        observers.remove(observer);
    }

    /**
     * Notifies all currently attached observers of received server response.
     *
     * @param   serverResponse  received server response to be sent to all observers.
     */
    public void notifyObserversOfChange(ServerResponse serverResponse){
        for(ServerResponseChangedObserver o: observers){
            o.update(serverResponse);
        }
    }
}

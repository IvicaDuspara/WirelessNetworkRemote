package makazas.imint.hr.meteorremote;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;

import makazas.imint.hr.meteorremote.model.ServerResponse;

/**
 * An observable thread that passes each server response to all of its observers.
 */
public abstract class ObservableServerResponseListenerThread extends Thread {

    private List<ServerResponseChangedObserver> observers;

    public ObservableServerResponseListenerThread(){
        observers = new ArrayList<>();
    }

    /**
     * Attaches {@code observer} to this thread.
     *
     * @param observer Observes server responses and gets updated on changes.
     */
    public void attach(ServerResponseChangedObserver observer){
        observers.add(observer);
    }

    /**
     * Detaches {@code observer} from this thread.
     *
     * @param observer Currently attached observer to be detached.
     *                 If no such observer is attached, nothing happens.
     */
    public void detach(ServerResponseChangedObserver observer){
        observers.remove(observer);
    }

    /**
     * Notifies all currently attached observers of received server response.
     *
     * @param serverResponse Received server response to be sent to all observers.
     */
    public void notifyObserversOfChange(ServerResponse serverResponse){
        for(ServerResponseChangedObserver o: observers){
            o.update(serverResponse);
        }
    }
}

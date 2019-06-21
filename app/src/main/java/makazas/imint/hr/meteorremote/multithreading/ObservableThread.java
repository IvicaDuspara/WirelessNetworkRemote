package makazas.imint.hr.meteorremote.multithreading;

import java.lang.Thread;
import java.util.ArrayList;
import java.util.List;


public abstract class ObservableThread extends Thread {

    private List<ServerResponseChangedObserver> observers;

    public ObservableThread(){
        observers = new ArrayList<>();
    }

    public void attach(ServerResponseChangedObserver observer){
        observers.add(observer);
    }

    public void detach(ServerResponseChangedObserver observer){
        observers.remove(observer);
    }

    public void notifyObserversOfChange(List<String> serverResponse){
        for(ServerResponseChangedObserver o: observers){
            o.update(serverResponse);
        }
    }
}

package makazas.imint.hr.meteorremote.multithreading;

import java.util.List;

public interface ServerResponseChangedObserver {
    void update(List<String> serverResponse);
}

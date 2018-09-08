package makazas.imint.hr.meteorremote.multithreading;

import makazas.imint.hr.meteorremote.model.ServerResponse;

public interface ServerResponseChangedObserver {
    void update(ServerResponse serverResponse);
}

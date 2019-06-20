package makazas.imint.hr.meteorremote.serverresponse;

import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;

public interface IResponse {
    void executeStrategy(SongsListContract.Presenter presenter);
}

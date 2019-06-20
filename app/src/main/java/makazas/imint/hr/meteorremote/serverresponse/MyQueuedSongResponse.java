package makazas.imint.hr.meteorremote.serverresponse;

import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;

public class MyQueuedSongResponse implements IResponse {

    private String[] responseBody;

    public MyQueuedSongResponse(String[] responseBody){
        this.responseBody = responseBody;
    }

    @Override
    public void executeStrategy(SongsListContract.Presenter presenter) {
        presenter.handleMyQueuedSongResponse(responseBody);
    }
}

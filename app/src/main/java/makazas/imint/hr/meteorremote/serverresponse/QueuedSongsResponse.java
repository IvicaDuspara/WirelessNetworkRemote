package makazas.imint.hr.meteorremote.serverresponse;

import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;

public class QueuedSongsResponse implements IResponse {

    private String[] responseBody;

    public QueuedSongsResponse(String[] responseBody){
        this.responseBody = responseBody;
    }

    @Override
    public void executeStrategy(SongsListContract.Presenter presenter) {
        presenter.handleQueuedSongsListResponse(responseBody);
    }
}

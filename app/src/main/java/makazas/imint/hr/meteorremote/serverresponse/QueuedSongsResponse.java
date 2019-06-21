package makazas.imint.hr.meteorremote.serverresponse;

import java.util.List;

import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;

public class QueuedSongsResponse implements IResponse {

    private List<String> responseBody;

    public QueuedSongsResponse(List<String> responseBody){
        this.responseBody = responseBody;
    }

    @Override
    public void executeStrategy(SongsListContract.Presenter presenter) {
        presenter.handleQueuedSongsListResponse(responseBody);
    }
}

package makazas.imint.hr.meteorremote.serverresponse;

import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;

public class NowPlayingResponse implements IResponse {

    private String[] responseBody;

    public NowPlayingResponse(String[] responseBody){
        this.responseBody = responseBody;
    }

    @Override
    public void executeStrategy(SongsListContract.Presenter presenter) {
        presenter.handleNowPlayingResponse(responseBody);
    }
}

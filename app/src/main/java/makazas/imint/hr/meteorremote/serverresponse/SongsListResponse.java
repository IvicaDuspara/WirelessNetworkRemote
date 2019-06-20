package makazas.imint.hr.meteorremote.serverresponse;

import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;

public class SongsListResponse implements IResponse {
    private String[] responseBody;

    public SongsListResponse(String[] responseBody){
        this.responseBody = responseBody;
    }

    @Override
    public void executeStrategy(SongsListContract.Presenter presenter) {
        presenter.handleSongsListResponse(responseBody);
    }
}

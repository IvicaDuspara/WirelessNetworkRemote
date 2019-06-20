package makazas.imint.hr.meteorremote.serverresponse;

import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;

public class MoveUpResponse implements IResponse {

    private String[] responseBody;

    public MoveUpResponse(String[] responseBody){
        this.responseBody = responseBody;
    }

    @Override
    public void executeStrategy(SongsListContract.Presenter presenter) {
        presenter.handleMoveUpResponse(responseBody);
    }
}

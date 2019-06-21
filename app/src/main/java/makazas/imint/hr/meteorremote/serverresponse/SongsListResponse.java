package makazas.imint.hr.meteorremote.serverresponse;

import android.util.Log;

import java.util.List;

import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;
import makazas.imint.hr.meteorremote.util.Constants;

public class SongsListResponse implements IResponse {
    private List<String> responseBody;

    public SongsListResponse(List<String> responseBody){
        this.responseBody = responseBody;
    }

    @Override
    public void executeStrategy(SongsListContract.Presenter presenter) {
        presenter.handleSongsListResponse(responseBody);
    }
}

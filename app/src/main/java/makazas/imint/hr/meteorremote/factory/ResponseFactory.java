package makazas.imint.hr.meteorremote.factory;

import android.content.res.AssetManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import makazas.imint.hr.meteorremote.serverresponse.IResponse;
import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;
import makazas.imint.hr.meteorremote.util.Constants;
import makazas.imint.hr.meteorremote.util.FileUtil;

public class ResponseFactory {
    private static final String RESPONSES_PATH = "makazas.imint.hr.meteorremote.";
    private static final String CODE_CLASS_MAP_PATH = "code-to-class.txt";
    private static final String PAIR_DELIMITER = "\n";
    private static final String CODE_CLASS_DELIMITER = ",";

    private SongsListContract.Presenter presenter;
    private AssetManager assetManager;
    // server code is key, class name of server code is value
    private HashMap<String, String> codeToClassName;

    public ResponseFactory(SongsListContract.Presenter presenter, AssetManager assetManager){
        this.presenter = presenter;
        this.assetManager = assetManager;
        loadCodeToClassMap();
    }

    private void loadCodeToClassMap() {
        String lines = "";

        try {
            lines = FileUtil.readFile(assetManager.open(CODE_CLASS_MAP_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] pairs = lines.split(PAIR_DELIMITER);

        for(String p : pairs){
            String[] names = p.split(CODE_CLASS_DELIMITER);
            codeToClassName.put(names[0], names[1]);
        }
    }

    public IResponse createResponse(String responseString){
        String[] responseLines = responseString.split(Constants.SERVER_RESPONSE_SEPARATOR);

        String responseCode = responseLines[0];
        String[] responseBody = Arrays.copyOfRange(responseLines, 1, responseLines.length);

        IResponse result = null;

        // jesus christ
        try {
            Class<IResponse> responseClass = (Class<IResponse>) Class.forName(RESPONSES_PATH + codeToClassName.get(responseCode));
            result = responseClass.getDeclaredConstructor(String[].class).newInstance(responseBody);
        } catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

}

package makazas.imint.hr.meteorremote.factory;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import makazas.imint.hr.meteorremote.serverresponse.IResponse;
import makazas.imint.hr.meteorremote.ui.songslist.SongsListContract;
import makazas.imint.hr.meteorremote.util.Constants;
import makazas.imint.hr.meteorremote.util.FileUtil;

public class ResponseFactory {
    private static final String RESPONSES_PATH = "makazas.imint.hr.meteorremote.serverresponse.";
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
        this.codeToClassName = new HashMap<>();
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

    public IResponse createResponse(List<String> response){
        String responseCode = response.get(0);
        List<String> responseBody = response.subList(1, response.size());

        IResponse result = null;

        // jesus christ
        try {
            Class<IResponse> responseClass = (Class<IResponse>) Class.forName(RESPONSES_PATH + codeToClassName.get(responseCode));
            result = responseClass.getDeclaredConstructor(List.class).newInstance(responseBody);
        } catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }

}

package ai.apdigital.ipcamera.data;

import com.google.gson.Gson;

public class SdkInitParams {

    public String appKey;
    public String accessToken;
    public int serverAreaId;
    public String openApiServer;
    public String openAuthApiServer;
    public String cameraName;
    public String serial;
    public String verifyCode;
    public boolean usingGlobalSDK;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static SdkInitParams createBy(ServerAreasEnum serverArea){
        SdkInitParams sdkInitParams = new SdkInitParams();
        if (serverArea != null){
            sdkInitParams.appKey = serverArea.defaultOpenAuthAppKey;
            sdkInitParams.serverAreaId = serverArea.id;
            sdkInitParams.openApiServer = serverArea.openApiServer;
            sdkInitParams.openAuthApiServer = serverArea.openAuthApiServer;
            sdkInitParams.usingGlobalSDK = serverArea.usingGlobalSDK;
        }
        return sdkInitParams;
    }

}

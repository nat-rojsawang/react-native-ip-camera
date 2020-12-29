package ai.apdigital.ipcamera.ui.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.text.TextUtils;

import com.videogo.exception.InnerException;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.util.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class EZUtils {

    private final static String TAG = EZUtils.class.getSimpleName();

    public static void saveCapturePicture(String filePath, Bitmap bitmap) throws InnerException {
        if (TextUtils.isEmpty(filePath)){
            LogUtil.d("EZUtils","saveCapturePictrue file is null");
            return;
        }
        File filepath = new File(filePath);
        File parent = filepath.getParentFile();
        if (parent == null || !parent.exists() || parent.isFile()) {
            parent.mkdirs();
        }
        FileOutputStream out = null;
        try {
            // 保存原图

            if (!TextUtils.isEmpty(filePath)) {
                out = new FileOutputStream(filepath);
                bitmap.compress(CompressFormat.JPEG, 100, out);
                //out.write(tempBuf, 0, size);
                out.flush();
                out.close();
                out = null;
            }


        } catch (FileNotFoundException e) {
//            throw new InnerException(e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IOException e) {
//            throw new InnerException(e.getLocalizedMessage());
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public static EZCameraInfo getCameraInfoFromDevice(EZDeviceInfo deviceInfo,int camera_index) {
        if (deviceInfo == null) {
            return null;
        }
        if (deviceInfo.getCameraNum() > 0 && deviceInfo.getCameraInfoList() != null && deviceInfo.getCameraInfoList().size() > camera_index) {
            return deviceInfo.getCameraInfoList().get(camera_index);
        }
        return null;
    }

}

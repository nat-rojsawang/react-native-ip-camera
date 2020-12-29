package ai.apdigital.ipcamera

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class IPCameraModule(private val reactContext: ReactApplicationContext)
    : ReactContextBaseJavaModule(reactContext) {

    companion object {
        val EXTRA_SERIAL = "serial"
        val EXTRA_VERIFY_CODE = "verify_code"
        val EXTRA_ACCESS_TOKEN = "access_token"
        val EXTRA_APP_KEY = "app_key"
        val EXTRA_SERVER_URL = "server-url"
        val EXTRA_AUTH_SERVER_URL = "auth-server-url"
        val EXTRA_CAMERA_NAME = "camera-name"
        var RN_CALLBACK: Callback? = null
    }

    override fun getName(): String {
        return "IPCamera"
    }

    override fun canOverrideExistingModule(): Boolean {
        return true
    }

    @ReactMethod
    fun openIPCamera(serial: String, verifyCode: String, accessToken: String, appKey: String, authServerURL: String, serverURL: String, cameraName: String, callback: Callback) {
        val intent = Intent(reactContext, HomeActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            putExtra(EXTRA_SERIAL, serial)
            putExtra(EXTRA_VERIFY_CODE, verifyCode)
            putExtra(EXTRA_ACCESS_TOKEN, accessToken)
            putExtra(EXTRA_APP_KEY, appKey)
            putExtra(EXTRA_SERVER_URL, serverURL)
            putExtra(EXTRA_AUTH_SERVER_URL, authServerURL)

            putExtra(EXTRA_CAMERA_NAME, cameraName)
        }
        RN_CALLBACK = callback
        reactContext.startActivity(intent)
    }
}

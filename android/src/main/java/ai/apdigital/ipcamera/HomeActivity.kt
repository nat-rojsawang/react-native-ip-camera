package ai.apdigital.ipcamera

import ai.apdigital.ipcamera.data.SdkInitParams
import ai.apdigital.ipcamera.data.SdkInitTool
import ai.apdigital.ipcamera.data.SpTool
import ai.apdigital.ipcamera.data.ValueKeys
import ai.apdigital.ipcamera.ui.realplay.EZRealPlayActivity
import ai.apdigital.ipcamera.ui.util.DataManager
import ai.apdigital.ipcamera.ui.util.EZUtils
import android.Manifest
import android.content.BroadcastReceiver
import android.content.Intent
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.videogo.constant.IntentConsts
import com.videogo.errorlayer.ErrorInfo
import com.videogo.exception.BaseException
import com.videogo.openapi.bean.EZCameraInfo
import com.videogo.openapi.bean.EZDeviceInfo
import com.videogo.util.LogUtil

class HomeActivity : RootActivity() {
    private val TAG = HomeActivity::class.java.simpleName

    companion object {
        private val LOAD_MY_DEVICE = 0
        private val mLoadType = LOAD_MY_DEVICE
        var mInitParams: SdkInitParams = SdkInitParams()
    }

    var cameraName: String? = null
    var serial: String? = null
    var authServerUrl: String? = null
    var serverUrl: String? = null
    var verifyCode: String? = null
    var appKey: String? = null
    var accessToken: String? = null
    var btnBack: AppCompatImageView?=null
    var tvTitleName: AppCompatTextView?=null
    var errorDesc: AppCompatTextView?=null
    var boxError:RelativeLayout?=null
    var page_container:RelativeLayout?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        showLoginAnim(true)
        cameraName = intent?.getStringExtra("camera-name")
        serial = intent?.getStringExtra("serial")
        authServerUrl = intent?.getStringExtra("auth-server-url")
        serverUrl = intent?.getStringExtra("server-url")
        verifyCode = intent?.getStringExtra("verify_code")
        appKey = intent?.getStringExtra("app_key")
        accessToken = intent?.getStringExtra("access_token")
        /*cameraName = "POPY"
        serial = "779952598"
        authServerUrl = "https://isgpopenauth.ezvizlife.com"
        verifyCode = "OCMRPH"
        serverUrl = "https://isgpopen.ezvizlife.com"
        appKey = "e34d2f451c7043a98661e058ecf00369"
        accessToken = "at.afb4b0cbcpnwrv4o6nz7cfis7x11rwoy-2vd6vcclzs-0p7c0ha-orj7zvmt4"*/
        btnBack = findViewById(R.id.btnBack)
        tvTitleName= findViewById(R.id.tvTitleName)
        errorDesc= findViewById(R.id.errorDesc)
        boxError = findViewById(R.id.boxError)
        page_container  = findViewById(R.id.page_container)
        btnBack?.setOnClickListener { finish() }

        val tf: Typeface = Typeface.createFromAsset(assets,  getString(R.string.font))
        tvTitleName?.typeface = tf
        errorDesc?.typeface = tf
        tvTitleName?.text = cameraName

        if (appKey != null || accessToken != null || verifyCode != null && serial != null && cameraName != null && authServerUrl != null && serverUrl != null) {
            try {
                Dexter.withContext(this)
                        .withPermissions(
                                Manifest.permission.CAMERA,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_MEDIA_LOCATION,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                                Manifest.permission.VIBRATE
                        ).withListener(object : MultiplePermissionsListener {
                            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                                SpTool.init(application)
                                mInitParams = SdkInitParams()
                                mInitParams.appKey = appKey
                                mInitParams.accessToken = accessToken
                                mInitParams.verifyCode = verifyCode
                                mInitParams.openApiServer = serverUrl
                                mInitParams.openAuthApiServer = authServerUrl
                                mInitParams.usingGlobalSDK = true
                                mInitParams.serial = serial
                                mInitParams.cameraName = cameraName

                                SdkInitTool.initSdk(application, mInitParams)
                                Thread {
                                    if (checkAppKeyAndAccessToken()) {
                                        mInitParams.accessToken = null
                                        SpTool.storeValue(ValueKeys.SDK_INIT_PARAMS, mInitParams.toString())
                                        jumpToCameraListActivity()
                                    } else {
                                        showError()
                                    }
                                    showLoginAnim(false)
                                }.start()
                            }

                            override fun onPermissionRationaleShouldBeShown(p0: MutableList<com.karumi.dexter.listener.PermissionRequest>?, p1: PermissionToken?) {

                            }
                        }).check()
            } catch (ignore: Exception) {
                showLoginAnim(false)
                showError()
            }
        } else {
            showLoginAnim(false)
            showError()
        }
    }

    private var mLoginAnimVg: ViewGroup? = null
    private var isShowLoginAnim = false

    private fun showLoginAnim(show: Boolean) {
        if (mLoginAnimVg == null) {
            mLoginAnimVg = findViewById<View>(R.id.vg_login_anim) as ViewGroup
        }
        if (mLoginAnimVg == null) {
            return
        }
        runOnUiThread {
            isShowLoginAnim = show
            if (show) {
                mLoginAnimVg?.visibility = View.VISIBLE
            } else {
                mLoginAnimVg?.visibility = View.INVISIBLE
            }
        }
    }

    private fun checkAppKeyAndAccessToken(): Boolean {
        var isValid = false
        try {
            getOpenSDK().getDeviceList(0, 20)
            isValid = true
        } catch (e: BaseException) {
            e.printStackTrace()
            Log.e(TAG, "Error code is " + e.errorCode)
            val errCode = e.errorCode
            val errMsg: String
            errMsg = when (errCode) {
                400031 -> applicationContext.getString(R.string.can_not_connect_to_device)
                else -> applicationContext.getString(R.string.can_not_connect_to_device)
            }
            toast(errMsg)
        }
        return isValid
    }

    private fun jumpToCameraListActivity() {
        GetCamerasInfoListTask(this@HomeActivity, boxError,page_container, serial ?: "", verifyCode
                ?: "", cameraName
                ?: "").execute()
    }

    private class GetCamerasInfoListTask(private var activity: HomeActivity?, private var boxError: RelativeLayout?,private var page_container: RelativeLayout?, private var serial: String, private var verifyCode: String, private var cameraName: String) : AsyncTask<Void?, Void?, List<EZDeviceInfo?>?>() {
        private var mErrorCode = 0

        override fun onPostExecute(result: List<EZDeviceInfo?>?) {
            super.onPostExecute(result)
            var cameraInfo: EZCameraInfo? = null
            var deviceInfo: EZDeviceInfo? = null
            result?.forEach {
                if (it?.deviceSerial == serial) {

                    DataManager.getInstance().setDeviceSerialVerifyCode(it.deviceSerial, verifyCode)
                    cameraInfo = EZUtils.getCameraInfoFromDevice(it, 0) ?: return
                    deviceInfo = it
                    return@forEach
                }
            }

            if (cameraInfo != null && deviceInfo != null) {
                val intent = Intent(activity, EZRealPlayActivity::class.java)
                intent.putExtra(IntentConsts.EXTRA_CAMERA_INFO, cameraInfo)
                intent.putExtra(IntentConsts.EXTRA_DEVICE_INFO, deviceInfo)
                intent.putExtra("CameraName", cameraName)
                activity?.startActivity(intent)
                activity?.finish()
            } else {
                page_container?.visibility = View.VISIBLE
                boxError?.visibility = View.VISIBLE
            }
        }

        override fun doInBackground(vararg params: Void?): List<EZDeviceInfo?>? {
            return try {
                if (mLoadType == LOAD_MY_DEVICE) {
                    getOpenSDK().getDeviceList(0, 20)
                } else {
                    getOpenSDK().getSharedDeviceList(0, 20)
                }
            } catch (e: BaseException) {
                val errorInfo = e.getObject() as ErrorInfo
                mErrorCode = errorInfo.errorCode
                LogUtil.e(TAG, errorInfo.toString())
                null
            }
        }
    }

    fun showError(){

        page_container?.visibility = View.VISIBLE
        boxError?.visibility = View.VISIBLE
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterLoginResultReceiver()
    }

    override fun onBackPressed() {
        if (isShowLoginAnim) {
            showLoginAnim(false)
        } else {
            super.onBackPressed()
        }
    }

    private var mLoginResultReceiver: BroadcastReceiver? = null
    private fun unregisterLoginResultReceiver() {
        if (mLoginResultReceiver != null) {
            unregisterReceiver(mLoginResultReceiver)
            mLoginResultReceiver = null
            Log.i(TAG, "unregistered login result receiver")
        }
    }
}
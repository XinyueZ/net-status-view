package library.view.lib

import android.annotation.TargetApi
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.*
import android.content.Intent
import android.content.IntentFilter
import android.content.res.TypedArray
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.*
import android.os.Build
import android.provider.Settings.Global.AIRPLANE_MODE_ON
import android.provider.Settings.Global.getInt
import android.support.annotation.ArrayRes
import android.support.annotation.ColorInt
import android.support.annotation.DimenRes
import android.support.annotation.StringRes
import android.support.v4.content.res.ResourcesCompat
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.*
import android.util.AttributeSet
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import library.view.lib.NetStatusView.Companion.NET_2G
import library.view.lib.NetStatusView.Companion.NET_3G
import library.view.lib.NetStatusView.Companion.NET_4G
import library.view.lib.NetStatusView.Companion.NET_UNKNOWN
import library.view.lib.NetStatusView.Companion.NET_WIFI
import library.view.lib.NetStatusView.Companion.UNKNOWN_STRENGTH

open class NetStatusView : LinearLayout {
    internal var strengthLevelCount = 4
    internal var netStrengthLevelResIds: TypedArray? = null
        set(value) {
            field = value
            strengthLevelCount = value?.length() ?: 4
        }
    @ColorInt
    internal var labelColor: Int = Color.BLACK
    @DimenRes
    internal var labelSizeResId: Int =
        DEFAULT_LABEL_SIZE
    private var networkStatus: NetworkStatus? = null

    private val networkReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                updateNetworkStatus()
            }
        }
    }

    companion object {
        private const val DEFAULT_LABEL_SIZE = -1
        internal var NET_WIFI = "Wifi"
        internal var NET_2G = "2G"
        internal var NET_3G = "3G"
        internal var NET_4G = "4G"
        internal var NET_UNKNOWN = "--"
        internal val UNKNOWN_STRENGTH by lazy { -9 }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Suppress("unused")
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet? = null) {
        with(LayoutInflater.from(context)) {
            inflate(R.layout.net_status_view, this@NetStatusView, true)
        }
        if (attrs != null) {
            with(context.theme.obtainStyledAttributes(attrs, R.styleable.nsv, 0, 0)) {
                setUp(
                    getResourceId(R.styleable.nsv_strengthLevelResIds, 0),
                    ResourcesCompat.getColor(
                        resources,
                        getResourceId(
                            R.styleable.nsv_textStatusColor, 0
                        ), context.theme
                    ),
                    getResourceId(R.styleable.nsv_textStatusSize, DEFAULT_LABEL_SIZE),
                    getResourceId(R.styleable.nsv_textWifi, 0),
                    getResourceId(R.styleable.nsv_text2G, 0),
                    getResourceId(R.styleable.nsv_text3G, 0),
                    getResourceId(R.styleable.nsv_text4G, 0),
                    getResourceId(R.styleable.nsv_textUnknown, 0)
                )
                recycle()
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        with(findViewById<TextView>(R.id.net_type_tv)) {
            if (labelSizeResId > 0) {
                setTextSize(COMPLEX_UNIT_PX, resources.getDimension(labelSizeResId))
            }
            setTextColor(labelColor)
        }

        updateNetworkStatus()
        setUpBroadcastReceiver()
    }

    override fun onDetachedFromWindow() {
        tearDownBroadcastReceiver()
        super.onDetachedFromWindow()
    }

    private fun setUpBroadcastReceiver() {
        IntentFilter().run {
            addAction(NETWORK_STATE_CHANGED_ACTION)
            addAction(WIFI_STATE_CHANGED_ACTION)
            addAction(CONNECTIVITY_ACTION)
            context.registerReceiver(networkReceiver, this)
        }
    }

    private fun tearDownBroadcastReceiver() {
        context.unregisterReceiver(networkReceiver)
    }

    private fun updateNetworkStatus() {
        with(context) {
            with(
                (getSystemService(TELEPHONY_SERVICE) as TelephonyManager)
                    .getNetStatus(applicationContext as Application, this@NetStatusView)
            ) {
                networkStatus = this
                findViewById<TextView>(R.id.net_type_tv).text = type

                with(findViewById<ImageView>(R.id.net_strength_level_iv)) {
                    contentDescription = type
                    netStrengthLevelResIds?.run {
                        if (strength in 0..(strengthLevelCount - 1)) {
                            visibility = VISIBLE
                            setImageResource(getResourceId(strength, -1))
                            return
                        }
                    }
                    visibility = GONE
                }
            }
        }
    }
}

fun NetStatusView.setUp(
    @ArrayRes strengthLevelResIds: Int,
    @ColorInt labelColor: Int,
    @DimenRes labelSizeResId: Int,
    @StringRes labelWifi: Int,
    @StringRes label2G: Int,
    @StringRes label3G: Int,
    @StringRes label4G: Int,
    @StringRes labelUnknown: Int
) {
    with(context) {
        if (labelWifi != 0) NET_WIFI = getString(labelWifi)
        if (label2G != 0) NET_2G = getString(label2G)
        if (label3G != 0) NET_3G = getString(label3G)
        if (label4G != 0) NET_4G = getString(label4G)
        if (labelUnknown != 0) NET_UNKNOWN = getString(labelUnknown)
        if (strengthLevelResIds != 0) netStrengthLevelResIds =
                resources.obtainTypedArray(strengthLevelResIds)
    }
    this.labelSizeResId = labelSizeResId
    this.labelColor = labelColor
}

private fun Context.isAirplaneModeOn() =
    getInt(contentResolver, AIRPLANE_MODE_ON, 0) != 0

internal data class NetworkStatus(val type: String, val strength: Int)

private fun TelephonyManager.getNetStatus(
    context: Application,
    netStatusView: NetStatusView
): NetworkStatus {
    // Ask for network type.
    val type =
        if (context.isAirplaneModeOn()) {
            NET_UNKNOWN
        } else {
            (context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).activeNetworkInfo?.run {
                when {
                    isNotConnected() -> NET_UNKNOWN
                    type == TYPE_WIFI -> NET_WIFI
                    else -> when (networkType) {
                        NETWORK_TYPE_GPRS,
                        NETWORK_TYPE_EDGE,
                        NETWORK_TYPE_CDMA,
                        NETWORK_TYPE_1xRTT,
                        NETWORK_TYPE_IDEN -> {
                            NET_2G
                        }
                        NETWORK_TYPE_UMTS,
                        NETWORK_TYPE_EVDO_0,
                        NETWORK_TYPE_EVDO_A,
                        NETWORK_TYPE_HSDPA,
                        NETWORK_TYPE_HSUPA,
                        NETWORK_TYPE_HSPA,
                        NETWORK_TYPE_EVDO_B,
                        NETWORK_TYPE_EHRPD,
                        NETWORK_TYPE_HSPAP -> {
                            NET_3G
                        }
                        NETWORK_TYPE_LTE -> {
                            NET_4G
                        }
                        else -> NET_UNKNOWN
                    }
                }
            } ?: kotlin.run {
                NET_UNKNOWN
            }
        }
    // Ask for strength.
    val strengthLevel: Int = when (type) {
        NET_WIFI -> {
            (context.getSystemService(WIFI_SERVICE) as WifiManager).run {
                calculateSignalLevel(
                    connectionInfo.rssi,
                    netStatusView.strengthLevelCount
                )
            }
        }
        NET_UNKNOWN -> {
            UNKNOWN_STRENGTH
        }
        else -> { // Mobile data are others.

            /**
             * FIXME: Because of permission of location we ignore status of cell-network currently, using "full".
             *
             * val cellinfogsm = allCellInfo[0] as CellInfoGsm
             * val cellSignalStrengthGsm = cellinfogsm.cellSignalStrength
             * cellSignalStrengthGsm.level
             */
            netStatusView.netStrengthLevelResIds?.let { it.length() - 1 }
                    ?: kotlin.run { UNKNOWN_STRENGTH }
        }
    }

    return NetworkStatus(
        type,
        strengthLevel
    )
}

private fun NetworkInfo.isNotConnected() =
    !isConnected || ((type != ConnectivityManager.TYPE_WIFI) && (type != ConnectivityManager.TYPE_MOBILE))

private fun NetworkInfo.hasConnection() = !isNotConnected()
package library.view.lib

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.TELEPHONY_SERVICE
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.content.res.TypedArray
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION
import android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION
import android.net.wifi.WifiManager.calculateSignalLevel
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.os.Build.VERSION_CODES.M
import android.provider.Settings.Global.AIRPLANE_MODE_ON
import android.provider.Settings.Global.getInt
import android.telephony.PhoneStateListener
import android.telephony.PhoneStateListener.LISTEN_DATA_ACTIVITY
import android.telephony.PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
import android.telephony.PhoneStateListener.LISTEN_NONE
import android.telephony.PhoneStateListener.LISTEN_SERVICE_STATE
import android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT
import android.telephony.TelephonyManager.NETWORK_TYPE_CDMA
import android.telephony.TelephonyManager.NETWORK_TYPE_EDGE
import android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD
import android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0
import android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A
import android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B
import android.telephony.TelephonyManager.NETWORK_TYPE_GPRS
import android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA
import android.telephony.TelephonyManager.NETWORK_TYPE_HSPA
import android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP
import android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA
import android.telephony.TelephonyManager.NETWORK_TYPE_IDEN
import android.telephony.TelephonyManager.NETWORK_TYPE_LTE
import android.telephony.TelephonyManager.NETWORK_TYPE_UMTS
import android.util.AttributeSet
import android.util.TypedValue.COMPLEX_UNIT_PX
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.IntRange
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import library.view.lib.NetStatusView.Companion.NET_2G
import library.view.lib.NetStatusView.Companion.NET_3G
import library.view.lib.NetStatusView.Companion.NET_4G
import library.view.lib.NetStatusView.Companion.NET_UNKNOWN
import library.view.lib.NetStatusView.Companion.NET_WIFI
import library.view.lib.NetStatusView.Companion.UNKNOWN_STRENGTH
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.jvm.isAccessible

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

    private val phoneStateEvents by lazy { LISTEN_SERVICE_STATE or LISTEN_SIGNAL_STRENGTHS or LISTEN_DATA_CONNECTION_STATE or LISTEN_DATA_ACTIVITY }
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
            super.onDataConnectionStateChanged(state, networkType)
            updateNetworkStatus()
        }

        override fun onSignalStrengthsChanged(signalStrength: SignalStrength?) {
            super.onSignalStrengthsChanged(signalStrength)
            updateNetworkStatus(signalStrength)
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

    @TargetApi(LOLLIPOP)
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
        setUpListeners()
    }

    override fun onDetachedFromWindow() {
        tearDownListeners()
        super.onDetachedFromWindow()
    }

    private fun setUpListeners() {
        with(context) {
            IntentFilter().run {
                addAction(CONNECTIVITY_ACTION)
                addAction(WIFI_STATE_CHANGED_ACTION)
                addAction(NETWORK_STATE_CHANGED_ACTION)
                context.registerReceiver(networkReceiver, this)
            }

            (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).listen(
                phoneStateListener, phoneStateEvents
            )
        }
    }

    private fun tearDownListeners() {
        with(context) {
            unregisterReceiver(networkReceiver)
            (getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).listen(
                phoneStateListener, LISTEN_NONE
            )
        }
    }

    private fun updateNetworkStatus(cellSignalStrength: SignalStrength? = null) {
        with(context) {
            with(
                (getSystemService(TELEPHONY_SERVICE) as TelephonyManager)
                    .getNetStatus(
                        applicationContext as Application,
                        cellSignalStrength,
                        this@NetStatusView
                    )
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

@SuppressLint("Recycle")
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
    cellSignalStrength: SignalStrength? = null,
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
            with(netStatusView) {
                netStrengthLevelResIds?.let {
                    cellSignalStrength?.getCellStrengthLevel(it.length() - 1, PRE_M)
                } ?: kotlin.run { UNKNOWN_STRENGTH }
            }
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

private val PRE_M by lazy { SDK_INT < M }
internal const val STRENGTH_NONE_OR_UNKNOWN = 0

/**
 * Calculates the network cells strength level where its
 * maximum value is limited to [max] and its lowest
 * value is [STRENGTH_NONE_OR_UNKNOWN].
 *
 * @param isPreM True if Current API Level is Lollipop or below.
 */
internal fun SignalStrength?.getCellStrengthLevel(
    @IntRange(
        from = 0,
        to = 3
    ) max: Int = 3,
    isPreM: Boolean = PRE_M
): Int =
    when {
        this == null -> 0
        isPreM -> getLevelPreM()
        else -> if (SDK_INT >= M) {
            level
        } else {
            getLevelPreM()
        }
    }.run {
        if (this >= max) max else this
    }

internal fun SignalStrength.getLevelPreM(): Int =
    if (isGsm) {
        var level = callLevelApi("getLteLevel")
        if (level == STRENGTH_NONE_OR_UNKNOWN) {
            level = callLevelApi("getTdScdmaLevel")
            if (level == STRENGTH_NONE_OR_UNKNOWN) {
                level = callLevelApi("getGsmLevel")
            }
        }
        level
    } else {
        val evdoLevel = callLevelApi("getEvdoLevel")
        val cdmaLevel = callLevelApi("getCdmaLevel")
        when {
            evdoLevel == STRENGTH_NONE_OR_UNKNOWN -> cdmaLevel
            cdmaLevel == STRENGTH_NONE_OR_UNKNOWN -> evdoLevel
            cdmaLevel < evdoLevel -> cdmaLevel
            else -> evdoLevel
        }
    }

private fun SignalStrength.callLevelApi(methodName: String): Int {
    try {
        val function = this::class.memberFunctions.find { it.name == methodName }
        function?.let {
            it.isAccessible = true
            val ret = it.call(this@callLevelApi) as? Int
            ret?.let {
                return it
            }
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
    return STRENGTH_NONE_OR_UNKNOWN
}
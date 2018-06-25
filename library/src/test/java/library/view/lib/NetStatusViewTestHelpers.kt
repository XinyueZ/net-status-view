@file:Suppress("PropertyName")

package library.view.lib

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Context.TELEPHONY_SERVICE
import android.content.Context.WIFI_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat.JPEG
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.net.ConnectivityManager.TYPE_MOBILE
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.graphics.drawable.DrawableCompat
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.view.View
import io.kotlintest.properties.Gen
import org.junit.Assert.assertNotNull
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowNetworkInfo.newInstance
import org.robolectric.shadows.ShadowSettings.setAirplaneMode
import org.robolectric.util.ReflectionHelpers
import java.io.ByteArrayOutputStream
import java.util.Arrays

internal const val INTI_VAL = 0
internal const val MIN_RSSI = -100
internal const val MAX_RSSI = -55

internal val FULL_LIST_OF_NET_TYPES by lazy {
    listOf(
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSPAP,
        TelephonyManager.NETWORK_TYPE_LTE,
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN
    )
}

internal val FULL_LIST_OF_NET_2G by lazy {
    listOf(
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN
    )
}

internal val FULL_LIST_OF_NET_3G by lazy {
    listOf(
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSPAP
    )
}

internal val FULL_LIST_OF_NET_4G by lazy {
    listOf(
        TelephonyManager.NETWORK_TYPE_LTE
    )
}

internal val FULL_LIST_OF_NET_UNKNOWN by lazy {
    listOf(
        Gen.negativeIntegers().generate()
    )
}

class TestNetStatusViewActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_net_status_view)
    }
}

fun <T : View> Activity.applyView(@IdRes id: Int, block: T.() -> Unit = {}): T =
    findViewById<T>(id).apply {
        assertNotNull(this)
        block(this)
    }

fun context(): Application = RuntimeEnvironment.application

fun Context.withNetworkTest(
    useWifi: Boolean = true,
    isUsable: Boolean = true,
    airplane: Boolean = false,
    listOfNetwork: List<Int>,
    netStrength: Int = 0,
    assertBlock: () -> Unit
) {

    (getSystemService(WIFI_SERVICE) as WifiManager).let { wifiMgr ->
        (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).let { telMgr ->
            (getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager).let { cnnMgr ->
                with(wifiMgr) {
                    shadowOf(this).isWifiEnabled = useWifi
                    shadowOf(shadowOf(this).connectionInfo).setRssi(netStrength)
                }
                setAirplaneMode(airplane)

                val type =
                    if (useWifi) TYPE_WIFI else TYPE_MOBILE

                shadowOf(cnnMgr).activeNetworkInfo = newInstance(
                    NetworkInfo.DetailedState.CONNECTED,
                    type,
                    type,
                    isUsable,
                    isUsable
                )

                if (!useWifi) {
                    shadowOf(telMgr).setNetworkType(
                        Gen.oneOf(
                            listOfNetwork
                        ).generate()
                    )

                    ReflectionHelpers.callConstructor(SignalStrength::class.java).apply {
                        ShadowSignalStrength.shadowOf(this).setLevel(netStrength)
                        with(shadowOf(telMgr)) {
                            @Suppress("DEPRECATION")
                            if (eventFlags != PhoneStateListener.LISTEN_NONE)
                                listener.onSignalStrengthsChanged(this@apply)
                        }
                    }
                } else {
                    sendBroadcast(Intent(CONNECTIVITY_ACTION))
                }

                assertBlock()
            }
        }
    }
}

fun <T : Drawable> T.bytesEqualTo(t: T?) = toBitmap().bytesEqualTo(t?.toBitmap(), true)

fun Bitmap.bytesEqualTo(otherBitmap: Bitmap?, shouldRecycle: Boolean = false) =
    otherBitmap?.let { other ->
        if (width == other.width && height == other.height) {
            val res = toBytes().contentEquals(other.toBytes())
            if (shouldRecycle) {
                doRecycle().also { otherBitmap.doRecycle() }
            }
            res
        } else false
    } ?: kotlin.run { false }

fun Bitmap.pixelsEqualTo(otherBitmap: Bitmap?, shouldRecycle: Boolean = false) =
    otherBitmap?.let { other ->
        if (width == other.width && height == other.height) {
            val res = Arrays.equals(toPixels(), other.toPixels())
            if (shouldRecycle) {
                doRecycle().also { otherBitmap.doRecycle() }
            }
            res
        } else false
    } ?: kotlin.run { false }

fun Bitmap.doRecycle() {
    if (!isRecycled) recycle()
}

fun <T : Drawable> T.toBitmap(): Bitmap {
    if (this is BitmapDrawable) return bitmap
    val drawable: Drawable = (DrawableCompat.wrap(this)).mutate()
    val bitmap = createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun Bitmap.toBytes(): ByteArray = ByteArrayOutputStream().use { stream ->
    compress(JPEG, 100, stream)
    stream.toByteArray()
}

fun Bitmap.toPixels() =
    IntArray(width * height).apply { getPixels(this, 0, width, 0, 0, width, height) }
package library.view.lib

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.calculateSignalLevel
import android.support.annotation.ArrayRes
import android.support.annotation.DimenRes
import io.kotlintest.properties.Gen
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.robolectric.util.ReflectionHelpers.callInstanceMethod
import org.robolectric.util.ReflectionHelpers.getField

abstract class AbstractNetStatusViewUnitTest {
    protected lateinit var netWifi: String
    protected lateinit var net2g: String
    protected lateinit var net3g: String
    protected lateinit var net4g: String
    protected lateinit var netUnknown: String
    @ArrayRes protected var netStrengthLevelRes: Int = INTI_VAL
    protected var labelColor: Int = INTI_VAL
    @DimenRes protected var labelSizeResId: Int = INTI_VAL
    protected lateinit var netStatusView: NetStatusView

    @Before
    fun init() {
        netStatusView = NetStatusView(context()).apply {
            config(this)
            callOnAttachedToWindow()
        }
    }

    abstract fun config(netStatusView: NetStatusView)

    @Test
    fun testLabelColorAndSizeInitialized() {
        with(netStatusView) {
            assertEquals(
                labelColor,
                this@AbstractNetStatusViewUnitTest.labelColor
            )
            assertEquals(
                labelSizeResId,
                this@AbstractNetStatusViewUnitTest.labelSizeResId
            )
        }
    }

    @Test
    fun testWifi() {
        with(context()) {
            with(netStatusView) {
                val rssi = Gen.choose(MIN_RSSI, MAX_RSSI).generate()
                val wifiStrength =
                    (context.getSystemService(Context.WIFI_SERVICE) as WifiManager).run {
                        calculateSignalLevel(rssi, 4)
                    }
                withNetworkTest(
                    true,
                    true,
                    false,
                    emptyList(),
                    rssi
                ) {
                    // net's type
                    assertEquals(
                        netWifi,
                        getNetworkStatus().type
                    )
                    // net's strength
                    assertEquals(
                        wifiStrength,
                        getNetworkStatus().strength
                    )
                }
            }
        }
    }

    @Test
    fun testUnknownNetwork() {
        with(context()) {
            with(netStatusView) {
                withNetworkTest(
                    false,
                    true,
                    false,
                    FULL_LIST_OF_NET_UNKNOWN
                ) {
                    // net's type
                    assertEquals(
                        netUnknown,
                        getNetworkStatus().type
                    )
                }
            }
        }
    }

    @Test
    fun testNetworkUnusable() {
        with(context()) {
            with(netStatusView) {
                withNetworkTest(
                    Gen.bool().generate(),
                    false,
                    Gen.bool().generate(),
                    FULL_LIST_OF_NET_TYPES
                ) {
                    // net's type
                    assertEquals(
                        netUnknown,
                        getNetworkStatus().type
                    )
                    // net's strength
                    assertEquals(
                        true,
                        getNetworkStatus().strength < 0
                    )
                }
            }
        }
    }

    @Test
    fun testAirplaneMode() {
        with(context()) {
            with(netStatusView) {
                withNetworkTest(
                    Gen.bool().generate(),
                    Gen.bool().generate(),
                    true,
                    FULL_LIST_OF_NET_TYPES
                ) {
                    // net's type
                    assertEquals(
                        netUnknown,
                        getNetworkStatus().type
                    )
                    // net's strength
                    assertEquals(
                        true,
                        getNetworkStatus().strength < 0
                    )
                }
            }
        }
    }

    @Test
    fun shouldNotChangeWhenDetached() {
        with(context()) {
            with(netStatusView) {
                callOnDetachedFromWindow()

                withNetworkTest(
                    Gen.bool().generate(),
                    Gen.bool().generate(),
                    Gen.bool().generate(),
                    FULL_LIST_OF_NET_TYPES
                ) {
                    // net's type
                    assertEquals(
                        netUnknown,
                        getNetworkStatus().type
                    )
                    // net's strength
                    assertEquals(
                        true,
                        getNetworkStatus().strength < 0
                    )
                }
            }
        }
    }

    private fun NetStatusView.callOnAttachedToWindow() {
        callInstanceMethod<Unit>(
            this,
            "onAttachedToWindow"
        )
    }

    private fun NetStatusView.callOnDetachedFromWindow() {
        callInstanceMethod<Unit>(
            this,
            "onDetachedFromWindow"
        )
    }

    internal fun NetStatusView.getNetworkStatus(): NetworkStatus =
        getField<NetworkStatus>(this, "networkStatus")
}
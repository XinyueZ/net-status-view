package library.view.lib

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.calculateSignalLevel
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ArrayRes
import androidx.annotation.DimenRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import io.kotlintest.properties.Gen
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [(ShadowSignalStrength::class)])
class NetStatusViewUiTest {
    private lateinit var netWifi: String
    private lateinit var net2g: String
    private lateinit var net3g: String
    private lateinit var net4g: String
    private lateinit var netUnknown: String
    @ArrayRes
    private var netStrengthLevelRes: Int = INTI_VAL
    private var labelColor: Int = INTI_VAL
    @DimenRes
    private var labelSizeResId: Int = INTI_VAL
    private lateinit var activityCtrl: ActivityController<TestNetStatusViewActivity>
    private val activity: TestNetStatusViewActivity
        get() = activityCtrl.get()
    private lateinit var netStatusView: NetStatusView

    @Before
    fun init() {
        activityCtrl = Robolectric.buildActivity(TestNetStatusViewActivity::class.java).setup()
        netStatusView = initUi()
    }

    private fun initUi(): NetStatusView = activity.applyView(R.id.network_status_banner) {
        netStrengthLevelRes = R.array.ic_net_strength_levels
        with(this@NetStatusViewUiTest) {
            labelColor = ResourcesCompat.getColor(
                resources,
                R.color.ns_view_text_color,
                null
            )
            labelSizeResId = R.dimen.ns_view_text_size
        }
        with(context) {
            netWifi = getString(R.string.net_status_wifi)
            net2g = getString(R.string.net_status_2g)
            net3g = getString(R.string.net_status_3g)
            net4g = getString(R.string.net_status_4g)
            netUnknown = getString(R.string.net_status_unknown)
        }
    }

    @Test
    fun testPropertiesEqual() {
        with(netStatusView) {
            assertEquals(this.labelSizeResId, this@NetStatusViewUiTest.labelSizeResId)
            assertEquals(this.labelColor, this@NetStatusViewUiTest.labelColor)

            val typArr = resources.obtainTypedArray(this@NetStatusViewUiTest.netStrengthLevelRes)
            var i = 0
            val cnt = typArr.length()
            while (i < cnt) {
                assertEquals(
                    typArr.getResourceId(i, 0),
                    this.netStrengthLevelResIds?.getResourceId(i, 0)
                )
                i++
            }
        }
    }

    @Test
    fun testWifi() {
        with(netStatusView) {
            context.run {
                /**
                 * [NetStatusView] updates with different networks.
                 */
                // wifi-on(cell-off) airplane-off, usable.
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
                    // indicator
                    if (netStrengthLevelRes > 0)
                        assertEquals(
                            true,
                            findViewById<ImageView>(R.id.net_strength_level_iv).sameResource(wifiStrength)
                        )
                    // the UI to show the indicator.
                    assertEquals(
                        if (netStrengthLevelRes > 0) VISIBLE else GONE,
                        findViewById<ImageView>(R.id.net_strength_level_iv).visibility
                    )
                    // label on UI
                    assertEquals(
                        netWifi,
                        findViewById<TextView>(R.id.net_type_tv).text.toString()
                    )
                }
            }
        }
    }

    @Test
    fun test2G() {
        val cellStrength = Gen.choose(0, 3).generate()
        with(netStatusView) {
            context.run {
                // wifi-off(cell-on 2g), airplane-off, usable.
                withNetworkTest(
                    false,
                    true,
                    false,
                    FULL_LIST_OF_NET_2G,
                    cellStrength
                ) {
                    // the UI to show the indicator.
                    assertEquals(
                        if (netStrengthLevelRes > 0) VISIBLE else GONE,
                        findViewById<ImageView>(R.id.net_strength_level_iv).visibility
                    )
                    assertEquals(
                        true,
                        findViewById<ImageView>(R.id.net_strength_level_iv).sameResource(cellStrength)
                    )

                    // label on UI
                    assertEquals(
                        net2g,
                        findViewById<TextView>(R.id.net_type_tv).text.toString()
                    )
                }
            }
        }
    }

    @Test
    fun test3G() {
        with(netStatusView) {
            val cellStrength = Gen.choose(0, 3).generate()
            context.run {
                // wifi-off(cell-on 3g), airplane-off, usable.
                withNetworkTest(
                    false,
                    true,
                    false,
                    FULL_LIST_OF_NET_3G,
                    cellStrength
                ) {
                    // the UI to show the indicator.
                    assertEquals(
                        if (netStrengthLevelRes > 0) VISIBLE else GONE,
                        findViewById<ImageView>(R.id.net_strength_level_iv).visibility
                    )
                    assertEquals(
                        true,
                        findViewById<ImageView>(R.id.net_strength_level_iv).sameResource(cellStrength)
                    )

                    // label on UI
                    assertEquals(
                        net3g,
                        findViewById<TextView>(R.id.net_type_tv).text.toString()
                    )
                }
            }
        }
    }

    @Test
    fun test4G() {
        with(netStatusView) {
            val cellStrength = Gen.choose(0, 3).generate()
            context.run {
                // wifi-off(cell-on 4g), airplane-off, usable.
                withNetworkTest(
                    false,
                    true,
                    false,
                    FULL_LIST_OF_NET_4G,
                    cellStrength
                ) {
                    // the UI to show the indicator.
                    assertEquals(
                        if (netStrengthLevelRes > 0) VISIBLE else GONE,
                        findViewById<ImageView>(R.id.net_strength_level_iv).visibility
                    )
                    assertEquals(
                        true,
                        findViewById<ImageView>(R.id.net_strength_level_iv).sameResource(cellStrength)
                    )

                    // label on UI
                    assertEquals(
                        net4g,
                        findViewById<TextView>(R.id.net_type_tv).text.toString()
                    )
                }
            }
        }
    }

    @Test
    fun testUnknownNetwork() {
        with(netStatusView) {
            context.run {
                withNetworkTest(
                    false,
                    true,
                    false,
                    FULL_LIST_OF_NET_UNKNOWN
                ) {
                    // the UI to show the indicator.
                    assertEquals(
                        GONE,
                        findViewById<ImageView>(R.id.net_strength_level_iv).visibility
                    )
                    // label on UI
                    assertEquals(
                        netUnknown,
                        findViewById<TextView>(R.id.net_type_tv).text.toString()
                    )
                }
            }
        }
    }

    @Test
    fun testNetworkUnusable() {
        with(netStatusView) {
            context.run {
                withNetworkTest(
                    Gen.bool().generate(),
                    false,
                    Gen.bool().generate(),
                    FULL_LIST_OF_NET_TYPES
                ) {
                    // label on UI
                    assertEquals(
                        netUnknown,
                        findViewById<TextView>(R.id.net_type_tv).text.toString()
                    )
                    // the UI to show the indicator.
                    assertEquals(
                        GONE,
                        findViewById<ImageView>(R.id.net_strength_level_iv).visibility
                    )
                }
            }
        }
    }

    @Test
    fun testAirplaneMode() {
        with(netStatusView) {
            context.run {
                withNetworkTest(
                    Gen.bool().generate(),
                    Gen.bool().generate(),
                    true,
                    FULL_LIST_OF_NET_TYPES
                ) {
                    // label on UI
                    assertEquals(
                        netUnknown,
                        findViewById<TextView>(R.id.net_type_tv).text.toString()
                    )
                    // the UI to show the indicator.
                    assertEquals(
                        GONE,
                        findViewById<ImageView>(R.id.net_strength_level_iv).visibility
                    )
                }
            }
        }
    }

    private fun ImageView.sameResource(index: Int) = drawable
        .bytesEqualTo(
            AppCompatResources.getDrawable(
                context,
                (resources.obtainTypedArray(netStrengthLevelRes)).getResourceId(
                    index,
                    -1
                )
            )
        )
}
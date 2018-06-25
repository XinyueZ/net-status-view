package library.view.lib

import io.kotlintest.properties.Gen
import library.view.lib.NetStatusView.Companion.NET_2G
import library.view.lib.NetStatusView.Companion.NET_3G
import library.view.lib.NetStatusView.Companion.NET_4G
import library.view.lib.NetStatusView.Companion.NET_UNKNOWN
import library.view.lib.NetStatusView.Companion.NET_WIFI
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers.getStaticField

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [(ShadowSignalStrength::class)])
class NetStatusViewDefaultUnitTest : AbstractNetStatusViewUnitTest() {
    override fun config(netStatusView: NetStatusView) {
        with(netStatusView) {
            netWifi = NET_WIFI
            net2g = NET_2G
            net3g = NET_3G
            net4g = NET_4G
            netUnknown = NET_UNKNOWN

            this@NetStatusViewDefaultUnitTest.labelColor = labelColor
            this@NetStatusViewDefaultUnitTest.labelSizeResId =
                    getStaticField(NetStatusView::class.java, "DEFAULT_LABEL_SIZE")
        }
    }

    @Test
    fun testStrengthLevelResShouldBeNull() {
        with(context()) {
            with(netStatusView) {
                assertNull(netStrengthLevelResIds)
            }
        }
    }

    @Test
    fun test2G_HaveNotNetworkTypeAndStrength() {
        val cellStrength = Gen.choose(0, 3).generate()
        with(context()) {
            with(netStatusView) {
                withNetworkTest(
                    false,
                    true,
                    false,
                    FULL_LIST_OF_NET_2G,
                    cellStrength
                ) {
                    // net's type
                    assertEquals(
                        net2g,
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
    fun test3G_HaveNotNetworkTypeAndStrength() {
        val cellStrength = Gen.choose(0, 3).generate()
        with(context()) {
            with(netStatusView) {
                withNetworkTest(
                    false,
                    true,
                    false,
                    FULL_LIST_OF_NET_3G,
                    cellStrength
                ) {
                    // net's type
                    assertEquals(
                        net3g,
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
    fun test4G_HaveNotNetworkTypeAndStrength() {
        val cellStrength = Gen.choose(0, 3).generate()
        with(context()) {
            with(netStatusView) {
                withNetworkTest(
                    false,
                    true,
                    false,
                    FULL_LIST_OF_NET_4G,
                    cellStrength
                ) {
                    // net's type
                    assertEquals(
                        net4g,
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
}
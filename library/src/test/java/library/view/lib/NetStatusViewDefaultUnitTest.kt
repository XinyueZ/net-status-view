package library.view.lib

import library.view.lib.NetStatusView.Companion.NET_2G
import library.view.lib.NetStatusView.Companion.NET_3G
import library.view.lib.NetStatusView.Companion.NET_4G
import library.view.lib.NetStatusView.Companion.NET_UNKNOWN
import library.view.lib.NetStatusView.Companion.NET_WIFI
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.util.ReflectionHelpers.getStaticField

@RunWith(RobolectricTestRunner::class)
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
}
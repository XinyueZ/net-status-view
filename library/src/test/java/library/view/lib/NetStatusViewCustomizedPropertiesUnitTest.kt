package library.view.lib

import android.support.v4.content.res.ResourcesCompat
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [(ShadowSignalStrength::class)])
class NetStatusViewCustomizedPropertiesUnitTest : AbstractNetStatusViewUnitTest() {
    override fun config(netStatusView: NetStatusView) {
        with(context()) {
            with(netStatusView) {
                setUp(
                    R.array.ic_net_strength_levels.apply { netStrengthLevelRes = this },
                    ResourcesCompat.getColor(
                        resources,
                        R.color.ns_view_text_color,
                        null
                    ).apply { this@NetStatusViewCustomizedPropertiesUnitTest.labelColor = this },
                    R.dimen.ns_view_text_size.apply {
                        this@NetStatusViewCustomizedPropertiesUnitTest.labelSizeResId = this
                    },
                    R.string.net_status_wifi.apply { netWifi = getString(this) },
                    R.string.net_status_2g.apply { net2g = getString(this) },
                    R.string.net_status_3g.apply { net3g = getString(this) },
                    R.string.net_status_4g.apply { net4g = getString(this) },
                    R.string.net_status_unknown.apply { netUnknown = getString(this) }
                )
            }
        }
    }

    @Test
    fun testStrengthLevelResShouldNotBeNull() {
        with(context()) {
            with(netStatusView) {
                assertNotNull(netStrengthLevelResIds)
            }
        }
    }
}
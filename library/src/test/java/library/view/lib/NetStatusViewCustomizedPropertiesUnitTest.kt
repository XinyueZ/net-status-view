package library.view.lib

import android.support.v4.content.res.ResourcesCompat
import io.kotlintest.properties.Gen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [(ShadowSignalStrength::class)], sdk = [27])
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

    @Test
    fun test2G_HaveNetworkTypeAndStrength() {
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
                        cellStrength,
                        getNetworkStatus().strength
                    )
                }

            }
        }
    }

    @Test
    fun test3G_HaveNetworkTypeAndStrength() {
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
                        cellStrength,
                        getNetworkStatus().strength
                    )
                }
            }
        }
    }

    @Test
    fun test4G_HaveNetworkTypeAndStrength() {
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
                        cellStrength,
                        getNetworkStatus().strength
                    )
                }
            }
        }
    }
}
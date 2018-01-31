package library.view.lib

import android.telephony.SignalStrength
import io.kotlintest.properties.Gen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [(ShadowSignalStrength::class)])
class NetStatusViewFunctionUnitTest {
    private lateinit var strength: SignalStrength
    private lateinit var shadowSignalStrength: ShadowSignalStrength

    @Before
    fun init() {
        strength = ReflectionHelpers.callConstructor(SignalStrength::class.java).apply {
            shadowSignalStrength = ShadowSignalStrength.shadowOf(this)
        }
    }

    @Test
    fun testIsGsmGet_LTE_Level_PreM() {
        shadowSignalStrength.apply {
            setIsGsm(true)
            setEvdoLevel(Gen.negativeIntegers().generate())
            setCdmaLevel(Gen.negativeIntegers().generate())

            Gen.positiveIntegers().generate().run {
                setLtelevel(this)
                setTdScdmaLevel(Gen.negativeIntegers().generate())
                setGsmLevel(Gen.negativeIntegers().generate())
                assertEquals(strength.getLevelPreM(), this)
            }
        }
    }

    @Test
    fun testIsGsmGet_TDS_CDMA_Level_PreM() {
        shadowSignalStrength.apply {
            setIsGsm(true)
            setEvdoLevel(Gen.negativeIntegers().generate())
            setCdmaLevel(Gen.negativeIntegers().generate())

            Gen.positiveIntegers().generate().run {
                setLtelevel(STRENGTH_NONE_OR_UNKNOWN)
                setTdScdmaLevel(this)
                setGsmLevel(Gen.negativeIntegers().generate())
                assertEquals(strength.getLevelPreM(), this)
            }
        }
    }

    @Test
    fun testIsGsmGet_GSM_Level_PreM() {
        shadowSignalStrength.apply {
            setIsGsm(true)
            setEvdoLevel(Gen.negativeIntegers().generate())
            setCdmaLevel(Gen.negativeIntegers().generate())

            Gen.positiveIntegers().generate().run {
                setLtelevel(STRENGTH_NONE_OR_UNKNOWN)
                setTdScdmaLevel(STRENGTH_NONE_OR_UNKNOWN)
                setGsmLevel(this)
                assertEquals(strength.getLevelPreM(), this)
            }
        }
    }

    @Test
    fun testIsNotGsmGet_CDMA_Level_Only_PreM() {
        shadowSignalStrength.apply {
            setIsGsm(false)
            setLtelevel(Gen.negativeIntegers().generate())
            setTdScdmaLevel(Gen.negativeIntegers().generate())
            setGsmLevel(Gen.negativeIntegers().generate())

            val cdma = Gen.choose(0, Int.MAX_VALUE - 1).generate()
            setCdmaLevel(cdma)
            val evdo = cdma + 1
            setEvdoLevel(evdo)
            assertEquals(strength.getLevelPreM(), cdma)
        }
    }

    @Test
    fun testIsNotGsmGet_EVDO_Level_Only_PreM() {
        shadowSignalStrength.apply {
            setIsGsm(false)
            setLtelevel(Gen.negativeIntegers().generate())
            setTdScdmaLevel(Gen.negativeIntegers().generate())
            setGsmLevel(Gen.negativeIntegers().generate())

            val evdo = Gen.choose(0, Int.MAX_VALUE - 1).generate()
            setEvdoLevel(evdo)
            val cdma = evdo + 1
            setCdmaLevel(cdma)
            assertEquals(strength.getLevelPreM(), evdo)
        }
    }

    @Test
    fun testIsNotGsmGet_CDMA_Level_PreM() {
        shadowSignalStrength.apply {
            setIsGsm(false)
            setLtelevel(Gen.negativeIntegers().generate())
            setTdScdmaLevel(Gen.negativeIntegers().generate())
            setGsmLevel(Gen.negativeIntegers().generate())

            Gen.positiveIntegers().generate().run {
                setEvdoLevel(STRENGTH_NONE_OR_UNKNOWN)
                setCdmaLevel(this)
                assertEquals(strength.getLevelPreM(), this)
            }
        }
    }

    @Test
    fun testIsNotGsmGet_EVDO_Level_PreM() {
        shadowSignalStrength.apply {
            setIsGsm(false)
            setLtelevel(Gen.negativeIntegers().generate())
            setTdScdmaLevel(Gen.negativeIntegers().generate())
            setGsmLevel(Gen.negativeIntegers().generate())

            Gen.positiveIntegers().generate().run {
                setCdmaLevel(STRENGTH_NONE_OR_UNKNOWN)
                setEvdoLevel(this)
                assertEquals(strength.getLevelPreM(), this)
            }
        }
    }

    @Test
    fun testGetCellStrengthNullAndGetZero() {
        val st: SignalStrength? = null
        Gen.positiveIntegers().generate().run {
            assertEquals(0, st.getCellStrengthLevel(this, Gen.bool().generate()))
        }
    }

    @Test
    fun testGetCellStrengthTooLargeLevel_ThenMaxOnly() {
        shadowSignalStrength.apply {
            setIsGsm(Gen.bool().generate())

            val level = Gen.choose(0, 4).generate()
            val lte = Gen.choose(0, 4).generate()
            val tdscdma = Gen.choose(0, 4).generate()
            val gsm = Gen.choose(0, 4).generate()
            val evdo = Gen.choose(0, 4).generate()
            val cdma = Gen.choose(0, 4).generate()
            val max = level + lte + tdscdma + gsm + evdo + cdma + 1

            setLevel(level + max + 1)
            setLtelevel(lte + max + 1)
            setTdScdmaLevel(tdscdma + max + 1)
            setGsmLevel(gsm + max + 1)
            setEvdoLevel(evdo + max + 1)
            setCdmaLevel(cdma + max + 1)

            assertEquals(max, strength.getCellStrengthLevel(max, Gen.bool().generate()))
        }
    }

    @Test
    fun testNotCall_GetLevel_But_Compat_PreM() {
        shadowSignalStrength.apply {
            Gen.positiveIntegers().generate().run {
                setLevel(0)
                setLtelevel(this)
                setTdScdmaLevel(this)
                setGsmLevel(this)
                setEvdoLevel(this)
                setCdmaLevel(this)
                assertEquals(this, strength.getCellStrengthLevel(this, true))
                assertNotEquals(0, strength.getCellStrengthLevel(this, true))
            }
        }
    }

    @Test
    fun testCall_GetLevel_AfterM() {
        shadowSignalStrength.apply {
            Gen.positiveIntegers().generate().run {
                setLevel(this)
                setLtelevel(0)
                setTdScdmaLevel(0)
                setGsmLevel(0)
                setEvdoLevel(0)
                setCdmaLevel(0)
                assertEquals(this, strength.getCellStrengthLevel(this, false))
                assertNotEquals(0, strength.getCellStrengthLevel(this, false))
            }
        }
    }
}
package library.view.lib

import android.telephony.SignalStrength
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadow.api.Shadow

@Implements(SignalStrength::class)
class ShadowSignalStrength {
    private var shadowLevel = 0
    private var shadowLteLevel = 0
    private var shadowTdScdmaLevel = 0
    private var shadowGsmLevel = 0
    private var shadowCdmaLevel = 0
    private var shadowEvdoLevel = 0
    private var shadowIsGsm = false

    fun setIsGsm(isGsm: Boolean) {
        shadowIsGsm = isGsm
    }

    fun setLevel(level: Int) {
        shadowLevel = level
    }

    fun setLtelevel(level: Int) {
        shadowLteLevel = level
    }

    fun setTdScdmaLevel(level: Int) {
        shadowTdScdmaLevel = level
    }

    fun setGsmLevel(level: Int) {
        shadowGsmLevel = level
    }

    fun setCdmaLevel(level: Int) {
        shadowCdmaLevel = level
    }

    fun setEvdoLevel(level: Int) {
        shadowEvdoLevel = level
    }

    @Implementation
    fun getLevel() = shadowLevel

    @Implementation
    fun getLteLevel() = shadowLteLevel

    @Implementation
    fun getTdScdmaLevel() = shadowTdScdmaLevel

    @Implementation
    fun getGsmLevel() = shadowGsmLevel

    @Implementation
    fun getCdmaLevel() = shadowCdmaLevel

    @Implementation
    fun getEvdoLevel() = shadowEvdoLevel

    @Implementation
    fun isGsm() = shadowIsGsm

    companion object {
        fun shadowOf(real: SignalStrength): ShadowSignalStrength =
            Shadow.extract<ShadowSignalStrength>(real)
    }
}
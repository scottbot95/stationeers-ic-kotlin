import com.github.scottbot95.stationeers.ic.devices.DaylightSensor
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.dsl.block
import com.github.scottbot95.stationeers.ic.dsl.comment
import com.github.scottbot95.stationeers.ic.dsl.compile
import com.github.scottbot95.stationeers.ic.dsl.define
import com.github.scottbot95.stationeers.ic.dsl.device
import com.github.scottbot95.stationeers.ic.dsl.divide
import com.github.scottbot95.stationeers.ic.dsl.forever
import com.github.scottbot95.stationeers.ic.dsl.of
import com.github.scottbot95.stationeers.ic.dsl.readDevice
import com.github.scottbot95.stationeers.ic.dsl.script
import com.github.scottbot95.stationeers.ic.dsl.subtract
import com.github.scottbot95.stationeers.ic.dsl.writeBatchDevices
import com.github.scottbot95.stationeers.ic.dsl.writeDevice

const val SOLAR_PANEL_HASH = "-2045627372"

val solarControl = script {
    val horizOffset by define(0.0) // Orientation offset. See https://stationeers-wiki.com/Solar_Logic_Circuits_Guide
    val vertScale by define(1.5)
    val vertOffset by define(50.0)

    val horizSensor by device(::DaylightSensor)
    val vertSensor by device(::DaylightSensor)

    comment("Configure daylight sensors")
    writeDevice(horizSensor.Mode, ScriptValue.of(1.0)) // these might need flipped according to wiki
    writeDevice(vertSensor.Mode, ScriptValue.of(2.0))

    forever {
        comment("Adjust horizontal position")
        readDevice(horizSensor.SolarAngle).use {
            subtract(it, horizOffset, it)
            writeBatchDevices(ScriptValue.of(SOLAR_PANEL_HASH), ScriptValue.of("Horizontal"), it)
        }


        comment("Adjust vertical position")
        readDevice(vertSensor.SolarAngle).use {
            divide(it, it, vertScale)
            subtract(it, vertOffset, it)
            writeBatchDevices(ScriptValue.of(SOLAR_PANEL_HASH), ScriptValue.of("Vertical"), it)
        }
    }
}


fun main() {
    val compiledScript = solarControl.compile()

    println(compiledScript)
}

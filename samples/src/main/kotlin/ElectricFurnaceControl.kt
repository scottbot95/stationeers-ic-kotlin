import com.github.scottbot95.stationeers.ic.devices.AdvFurnace
import com.github.scottbot95.stationeers.ic.devices.LogicMemory
import com.github.scottbot95.stationeers.ic.devices.On
import com.github.scottbot95.stationeers.ic.devices.Open
import com.github.scottbot95.stationeers.ic.devices.Pressure
import com.github.scottbot95.stationeers.ic.devices.Setting
import com.github.scottbot95.stationeers.ic.devices.Switch
import com.github.scottbot95.stationeers.ic.dsl.ScriptValue
import com.github.scottbot95.stationeers.ic.dsl.branch
import com.github.scottbot95.stationeers.ic.dsl.comment
import com.github.scottbot95.stationeers.ic.dsl.define
import com.github.scottbot95.stationeers.ic.dsl.device
import com.github.scottbot95.stationeers.ic.dsl.forever
import com.github.scottbot95.stationeers.ic.dsl.of
import com.github.scottbot95.stationeers.ic.dsl.readDevice
import com.github.scottbot95.stationeers.ic.dsl.register
import com.github.scottbot95.stationeers.ic.dsl.script
import com.github.scottbot95.stationeers.ic.dsl.subtract
import com.github.scottbot95.stationeers.ic.dsl.writeDevice
import com.github.scottbot95.stationeers.ic.util.EqualToZero
import com.github.scottbot95.stationeers.ic.util.GreaterThanEqualTo

val electricFurnaceControl = script {
    val furnace by device(::AdvFurnace)
    val powerSwitch by device(::Switch)
    val openSwitch by device(::Switch)
    val tempInput by device(::LogicMemory)
    val presInput by device(::LogicMemory)

    val targetTemp by register
    val targetPres by register
    val outputRate by register

    val negThreshold by define(-200)
    val posThreshold by define(200)
    val outputInc by define(1)

    // TODO this should go inside the loop probably?
    val presDiff by register

    forever("loop") {
        comment("Power Check")
        readDevice(powerSwitch.Setting).use {
            writeDevice(furnace.On, it)
            branch(EqualToZero(it), loopStart)
        }

        comment("Output Check")
        readDevice(openSwitch.Setting).use {
            writeDevice(furnace.Open, it)
        }

        comment("Load Targets")
        readDevice(targetTemp, tempInput.Setting)
        readDevice(targetPres, presInput.Setting)

        comment("Reset state")
        writeDevice(furnace.SettingInput, ScriptValue.of(0))
        writeDevice(furnace.SettingOutput, ScriptValue.of(0))

        comment("Pressure Check")
        readDevice(furnace.Pressure).use {
            subtract(presDiff, it, targetPres)
        }
        branch(GreaterThanEqualTo(presDiff, negThreshold), ScriptValue.of(3))
    }
}

import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.ICScriptBuilder
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.instructions.Device.BatchSave
import com.github.scottbot95.stationeers.ic.instructions.Device.Load
import com.github.scottbot95.stationeers.ic.instructions.Flow.Jump
import com.github.scottbot95.stationeers.ic.instructions.Math.Divide
import com.github.scottbot95.stationeers.ic.instructions.Math.Subtract
import com.github.scottbot95.stationeers.ic.instructions.Misc.Yield
import com.github.scottbot95.stationeers.ic.util.asFloatRegister
import com.github.scottbot95.stationeers.ic.util.asIntRegister
import com.github.scottbot95.stationeers.ic.util.toScriptValue

fun ICScriptBuilder.simpleSolarTracking() {
    val daySensor = Device.D0.toScriptValue()
    val solarPanel = Device.D1.toScriptValue()
    val panelHash = Register.R0.asIntRegister()
    val vertAngle = Register.R1.asFloatRegister()
    val panelAngle = Register.R2.asFloatRegister()
    val vertDiff = Register.R3.asFloatRegister()

    appendEntry(Load(panelHash, solarPanel, "PrefabHash".toScriptValue()))
    appendEntry(Load(vertAngle, daySensor, "Vertical".toScriptValue()))
    appendEntry(Subtract(vertDiff, vertAngle, 15.toScriptValue()))
    appendEntry(Divide(panelAngle, vertDiff, 1.5f.toScriptValue()))
    appendEntry(BatchSave(panelHash, "Vertical".toScriptValue(), panelAngle))
    appendEntry(Yield)
    appendEntry(Jump(0.toScriptValue()))
}

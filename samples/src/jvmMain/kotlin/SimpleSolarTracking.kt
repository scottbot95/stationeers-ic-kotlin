import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.ICScriptBuilder
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.appendInstruction
import com.github.scottbot95.stationeers.ic.instructions.Branch.Jump
import com.github.scottbot95.stationeers.ic.instructions.Device.BatchSave
import com.github.scottbot95.stationeers.ic.instructions.Device.Load
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

    appendInstruction(Load(panelHash, solarPanel, "PrefabHash".toScriptValue()))
    appendInstruction(Load(vertAngle, daySensor, "Vertical".toScriptValue()))
    appendInstruction(Subtract(vertDiff, vertAngle, 15.toScriptValue()))
    appendInstruction(Divide(panelAngle, vertDiff, 1.5f.toScriptValue()))
    appendInstruction(BatchSave(panelHash, "Vertical".toScriptValue(), panelAngle))
    appendInstruction(Yield)
    appendInstruction(Jump(0.toScriptValue()))
}

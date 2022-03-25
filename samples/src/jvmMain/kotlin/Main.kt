import com.github.scottbot95.stationeers.ic.Device
import com.github.scottbot95.stationeers.ic.DeviceLiteral
import com.github.scottbot95.stationeers.ic.FloatLiteral
import com.github.scottbot95.stationeers.ic.FloatRegister
import com.github.scottbot95.stationeers.ic.ICScriptBuilder
import com.github.scottbot95.stationeers.ic.IntLiteral
import com.github.scottbot95.stationeers.ic.IntRegister
import com.github.scottbot95.stationeers.ic.Register
import com.github.scottbot95.stationeers.ic.StringValue
import com.github.scottbot95.stationeers.ic.appendInstruction
import com.github.scottbot95.stationeers.ic.instructions.Branch.Jump
import com.github.scottbot95.stationeers.ic.instructions.Device.BatchSave
import com.github.scottbot95.stationeers.ic.instructions.Device.Load
import com.github.scottbot95.stationeers.ic.instructions.Math.Divide
import com.github.scottbot95.stationeers.ic.instructions.Math.Subtract
import com.github.scottbot95.stationeers.ic.instructions.Misc.Yield

fun ICScriptBuilder.simpleSolarTracking() {
    val daySensor = DeviceLiteral(Device.D0)
    val solarPanel = DeviceLiteral(Device.D1)
    val panelHash = IntRegister(Register.R0)
    val vertAngle = FloatRegister(Register.R1)
    val panelAngle = FloatRegister(Register.R2)
    val vertDiff = FloatRegister(Register.R3)

    appendInstruction(Load(panelHash, solarPanel, StringValue("PrefabHash")))
    appendInstruction(Load(vertAngle, daySensor, StringValue("Vertical")))
    appendInstruction(Subtract(vertDiff, vertAngle, IntLiteral(15)))
    appendInstruction(Divide(panelAngle, vertDiff, FloatLiteral(1.5f)))
    appendInstruction(BatchSave(panelHash, StringValue("Vertical"), panelAngle))
    appendInstruction(Yield)
    appendInstruction(Jump(IntLiteral(0)))
}

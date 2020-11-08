# stationeers-ts-ic

## Core Principals

- Script type contains raw operations (not necessarily 1:1 with MIPS)
- Device/register types contain more abstract tasks

## Examples

**script.kt**
```kotlin
val myScript = script {
    val lightSwitch: LogicSwitch by device(Devices.D0) // name inferred by delegate
    val light: Light by device(Devices.D1, "Light") // specify alias explicitly (null for none)
    val loopCount by register
    
    comment("Loop forever")
    forever("loop") {
        // Will implicitly create a temp register to store the value
        val switchSetting = readDevice(lightSwitch, "Setting")
        writeDevice(light, "Setting", switchSetting)
        inc(loopCount)
    }
    
    +"# You can add text directly as well"
}

val results = myScript.compile {
    /* TODO configure compile options */
}

/* Do something with the results directly */

myScript.export {
    /* TODO configure export options */
}
```

**script.out**
```
alias lightSwitch d0
alias Light d1
alias loopCount r0
# Loop forever
loop:
yield
l r15 lightSwitch Setting
s Light Setting r15
add loopCount loopCount 1
j loop
# You can add text directly as well
```

With `minify:true` option:
**script.min.out**
```
l r15 d0 Setting
s d1 Setting r15
add r0 r0 1
jr -3
```

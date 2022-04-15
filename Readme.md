# stationeers-ts-ic

## Core Principals

- Kotlin code is a high-level abstraction that gets compiled to MIPS
- High-level code gets optimized and optionally minified (aliases/labels stripped)
- Scripts should be able to be compiled more than once with different options each time

## Examples

**script.kt**

```kotlin
val myScript = script {
    val lightSwitch: LogicSwitch by device(Devices.D0) // name inferred by delegate
    val light: Light by device(Devices.D1, "Light") // specify alias explicitly (null for none)
    val loopCount by int()

    comment("Loop forever")
    forever("loop") {
        // Will implicitly create a temp register to store the value
        val switchSetting = readDevice(lightSwitch, "Setting")
        writeDevice(light, "On", switchSetting)
        loopCount++
    }

    +"# You can add text directly as well"
}

val compiledScript = myScript.compile {
    minify = true
}

/* Do something with the results directly */

println(compiledScript)
```

**script.out**
```
alias lightSwitch d0
alias Light d1
alias loopCount r0
# Loop forever
loop:
yield
l r11 lightSwitch Setting
s Light On r11
add loopCount loopCount 1
j loop
# You can add text directly as well
```

With `minify:true` option:

**script.min.out**
```
l r15 d0 Setting
s d1 On r15
add r0 r0 1
j 0
# You can add text directly as well
```

package online.slavok.whitelist

import net.minecraft.text.MutableText
import net.minecraft.text.Text
//? if <1.19 {
/*import net.minecraft.text.LiteralText*/
//?}

/**
 * Build a literal text component.
 *
 * Text.literal was added in 1.19 and replaced the LiteralText constructor used
 * in 1.18 and earlier. Branch once here so call sites stay version-agnostic.
 */
fun literalText(content: String): MutableText {
    //? if >=1.19 {
    return Text.literal(content)
    //?} else {
    /*return LiteralText(content)*/
    //?}
}

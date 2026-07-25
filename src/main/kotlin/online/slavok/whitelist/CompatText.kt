package online.slavok.whitelist

//? if >=1.22 {
/*import net.minecraft.network.chat.Component as Text
import net.minecraft.network.chat.MutableComponent as MutableText
import net.minecraft.ChatFormatting as Formatting*/
//?} else {
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
//?}
//? if <1.19 {
/*import net.minecraft.text.LiteralText*/
//?}

/**
 * Build a literal text component.
 *
 * Names differ across versions: Text.literal (1.19+), the LiteralText constructor
 * (<=1.18), and Component.literal (26+, unobfuscated). Branch once here so call
 * sites stay version-agnostic.
 */
fun literalText(content: String): MutableText {
    //? if >=1.19 {
    return Text.literal(content)
    //?} else {
    /*return LiteralText(content)*/
    //?}
}

/** Apply a color/format. Renamed formatted -> withStyle in 26 (unobfuscated). */
fun MutableText.colored(formatting: Formatting): MutableText {
    //? if >=1.22 {
    /*return withStyle(formatting)*/
    //?} else {
    return formatted(formatting)
    //?}
}

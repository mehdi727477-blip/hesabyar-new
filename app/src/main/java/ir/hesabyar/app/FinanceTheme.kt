package ir.hesabyar.app

import android.content.Context
import android.graphics.Color

/** Shared, user-selectable accent themes. */
object FinanceTheme {
    private const val PREFS = "hesabyar_preferences"
    private const val KEY_THEME = "accent_theme"
    data class Palette(val title: String, val primary: Int, val dark: Int, val tint: Int)
    private val palettes = linkedMapOf(
        "blue" to Palette("آبی اقیانوسی", Color.rgb(37, 99, 235), Color.rgb(25, 70, 160), Color.rgb(232, 241, 255)),
        "green" to Palette("سبز زمردی", Color.rgb(5, 150, 105), Color.rgb(4, 105, 76), Color.rgb(226, 248, 240)),
        "purple" to Palette("بنفش", Color.rgb(124, 58, 237), Color.rgb(91, 33, 182), Color.rgb(242, 235, 255)),
        "orange" to Palette("کهربایی", Color.rgb(217, 119, 6), Color.rgb(146, 64, 14), Color.rgb(255, 244, 224))
    )
    fun palette(context: Context): Palette = palettes[context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_THEME, "blue")] ?: palettes.getValue("blue")
    fun save(context: Context, key: String) { if (key in palettes) context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_THEME, key).apply() }
    fun choices(): Map<String, String> = palettes.mapValues { it.value.title }
    fun primary(context: Context) = palette(context).primary
    fun dark(context: Context) = palette(context).dark
    fun tint(context: Context) = palette(context).tint
}

package ir.hesabyar.app

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

class TransactionsActivity : Activity() {
    private lateinit var db: FinanceDb
    private lateinit var list: LinearLayout
    private lateinit var search: EditText
    private var filter = "همه"
    private val ink = Color.rgb(20, 32, 55); private val muted = Color.rgb(111, 126, 148)
    private val bg = Color.rgb(245, 248, 252); private val blue get() = FinanceTheme.primary(this)
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); db = FinanceDb(this); render() }
    override fun onResume() { super.onResume(); if (::list.isInitialized) refresh() }
    private fun render() {
        window.statusBarColor = bg; window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility =  View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg); layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL; setPadding(dp(16), dp(16), dp(16), dp(10)) }
        root.addView(label("تراکنش‌ها", 25, ink, true))
        root.addView(label("جست‌وجو و بررسی همه ثبت‌های مالی", 13, muted, false).apply { setPadding(0, dp(4), 0, dp(12) ) })
        search = EditText(this).apply { hint = "جست‌وجو در شرح، حساب یا طرف حساب"; textSize = 14f; setSingleLine(true); setPadding(dp(12), 0, dp(12), 0); background = shape(Color.WHITE, 14); addTextChangedListener(SimpleTextWatcher { refresh() }) }
        root.addView(search, LinearLayout.LayoutParams(-1, dp(50)).apply { bottomMargin = dp(10) })
        val filters = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL }
        listOf("همه", "واریز", "پرداخت", "انتقال").forEach { name ->
            val b = Button(this).apply { text = name; textSize = 11f; isAllCaps = false; setPadding(dp(3), 0, dp(3), 0); setOnClickListener { filter = name; refresh() } }
            filters.addView(b, LinearLayout.LayoutParams(0, dp(42), 1f).apply { marginEnd = dp(3) })
        }
        root.addView(filters)
        val scroll = ScrollView(this)
        list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL }
        scroll.addView(list); root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root); refresh()
    }
    private fun refresh() {
        if (!::list.isInitialized) return
        list.removeAllViews(); val q = if (::search.isInitialized) search.text.toString().trim() else ""
        val rows = db.all().filter { t ->
            val typeOk = when(filter) { "واریز" -> t.kind == "واریز"; "پرداخت" -> t.kind == "پرداخت/برداشت"; "انتقال" -> t.kind == "انتقال بین حساب‌ها"; else -> true }
            typeOk && (q.isBlank() || listOf(t.reason, t.account, t.toBank, t.person, t.method, t.jalaliDate, t.company).any { it.contains(q, true) } || t.amount.toString().contains(q))
        }
        if (rows.isEmpty()) { list.addView(label("تراکنشی مطابق جست‌وجو پیدا نشد.", 14, muted, false).apply { gravity = Gravity.CENTER; setPadding(0, dp(35), 0, dp(35)) }); return }
        rows.forEach { t ->
            val transfer = t.kind == "انتقال بین حساب‌ها"; val deposit = t.kind == "واریز"
            val c = if (transfer) Color.rgb(83, 105, 145) else if (deposit) Color.rgb(18, 151, 111) else Color.rgb(218, 78, 91)
            val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)); background = shape(Color.WHITE, 15); layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL }
            val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
            top.addView(label(t.reason.ifBlank { t.kind }, 14, ink, true), LinearLayout.LayoutParams(0, -2, 1f))
            top.addView(label("${if (transfer) "⇄" else if (deposit) "+" else "−"}${money(t.amount)} تومان", 13, c, true))
            card.addView(top)
            card.addView(label("${t.account}${if (t.toBank.isNotBlank()) "  ←  ${t.toBank}" else ""}", 12, muted, false).apply { setPadding(0, dp(5), 0, dp(2)) })
            card.addView(label(listOf(t.method, t.person, t.company.takeIf { it.isNotBlank() }?.let { "شرکت: $it" }, t.jalaliDate).filterNotNull().filter { it.isNotBlank() }.joinToString("  •  "), 10, muted, false))
            card.setOnLongClickListener { AlertDialog.Builder(this).setTitle("حذف تراکنش").setMessage("این ثبت مالی حذف شود؟ این کار مانده‌ها و گزارش‌ها را تغییر می‌دهد.").setNegativeButton("لغو", null).setPositiveButton("حذف") { _, _ -> db.deleteTransaction(t.id); refresh() }.show(); true }
            list.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
        }
    }
    private fun label(s:String, size:Int, color:Int, bold:Boolean) = TextView(this).apply { text=s; textSize=size.toFloat(); setTextColor(color); if(bold) typeface=Typeface.DEFAULT_BOLD }
    private fun shape(c:Int,r:Int)=GradientDrawable().apply { setColor(c); cornerRadius=dp(r).toFloat() }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun money(v:Long)=String.format(java.util.Locale("fa","IR"),"%,d",v)
}

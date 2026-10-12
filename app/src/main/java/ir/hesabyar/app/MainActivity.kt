package ir.hesabyar.app

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var db: FinanceDb
    private val ink = Color.rgb(20, 32, 55)
    private val muted = Color.rgb(111, 126, 148)
    private val bg = Color.rgb(245, 248, 252)
    private val green = Color.rgb(15, 153, 111)
    private val red = Color.rgb(218, 78, 91)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 30) window.setDecorFitsSystemWindows(true)
        window.statusBarColor = bg
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        window.decorView.layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
        db = FinanceDb(this)
    }

    override fun onResume() { super.onResume(); if (::db.isInitialized) render() }

    private fun render() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
        }
        val scroll = ScrollView(this).apply { clipToPadding = false; setPadding(dp(18), dp(18), dp(18), dp(22)) }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL }

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(2), dp(4), dp(2), dp(20)) }
        val brandIcon = TextView(this).apply {
            text = "ح"
            textSize = 25f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            background = shape(Color.rgb(25, 65, 118), 17)
        }
        header.addView(brandIcon, LinearLayout.LayoutParams(dp(54), dp(54)).apply { marginStart = 12 })
        val brandTexts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_VERTICAL }
        brandTexts.addView(text("حساب‌یار", 23, ink, true))
        brandTexts.addView(text("مدیریت مالی شخصی", 13, muted, false).apply { setPadding(0, dp(3), 0, 0) })
        header.addView(brandTexts, LinearLayout.LayoutParams(0, -2, 1f))
        header.addView(TextView(this).apply {
            text = "●  خصوصی"
            textSize = 11f; setTextColor(Color.rgb(19, 130, 103)); gravity = Gravity.CENTER
            setPadding(dp(10), dp(7), dp(10), dp(7)); background = shape(Color.rgb(224, 248, 241), 30)
        })
        content.addView(header)

        val (income, expense) = db.totals()
        val net = income - expense
        val balanceCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(22), dp(21), dp(22), dp(22))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(FinanceTheme.dark(this), FinanceTheme.primary(this))).apply { cornerRadius = dp(24).toFloat() }
            elevation = dp(4).toFloat()
        }
        val balanceHeader = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        balanceHeader.addView(text("خلاصه وضعیت مالی", 14, Color.rgb(214, 229, 245), false), LinearLayout.LayoutParams(0, -2, 1f))
        balanceHeader.addView(TextView(this).apply { text = "● این دستگاه"; textSize = 11f; setTextColor(Color.rgb(175, 239, 224)) })
        balanceCard.addView(balanceHeader)
        balanceCard.addView(text("ماندهٔ خالص ثبت‌شده", 13, Color.rgb(194, 215, 237), false).apply { setPadding(0, dp(21), 0, dp(4)) })
        balanceCard.addView(text("${money(net)} تومان", 27, Color.WHITE, true))
        val divider = View(this).apply { setBackgroundColor(Color.argb(60, 255, 255, 255)) }
        balanceCard.addView(divider, LinearLayout.LayoutParams(-1, dp(1)).apply { topMargin = dp(18); bottomMargin = dp(14) })
        balanceCard.addView(text("بر اساس مجموع واریزها و پرداخت‌های ثبت‌شده", 11, Color.rgb(203, 222, 240), false))
        content.addView(balanceCard, params(bottom = 16))

        val stats = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL }
        stats.addView(statCard("کل واریزی‌ها", income, green, "↙"), LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(6) })
        stats.addView(statCard("کل پرداخت‌ها", expense, red, "↗"), LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(6) })
        content.addView(stats, params(bottom = 20))

        val add = Button(this).apply {
            text = "＋    ثبت تراکنش جدید"; textSize = 16f; isAllCaps = false
            setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(FinanceTheme.dark(this), FinanceTheme.primary(this))).apply { cornerRadius = dp(16).toFloat() }
            elevation = dp(2).toFloat()
            setOnClickListener { startActivity(Intent(this@MainActivity, AddTransactionActivity::class.java)) }
        }
        content.addView(add, params(dp(58), 10))
        val quickActions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL }
        fun quickAction(label: String, initialKind: String, tint: Int) {
            val button = Button(this).apply {
                text = label; textSize = 12f; isAllCaps = false; setTextColor(FinanceTheme.primary(this@MainActivity))
                background = shape(tint, 14, Color.rgb(222, 231, 242))
                setOnClickListener { startActivity(Intent(this@MainActivity, AddTransactionActivity::class.java).putExtra("initial_kind", initialKind)) }
            }
            quickActions.addView(button, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginEnd = dp(4); marginStart = dp(4) })
        }
        quickAction("＋ ثبت واریز", "واریز", FinanceTheme.tint(this))
        quickAction("− ثبت هزینه", "پرداخت/برداشت", Color.rgb(255, 239, 240))
        quickActions.addView(Button(this).apply { text = "▥ گزارش"; textSize = 12f; isAllCaps = false; setTextColor(FinanceTheme.primary(this@MainActivity)); background = shape(Color.WHITE, 14, Color.rgb(222,231,242)); setOnClickListener { startActivity(Intent(this@MainActivity, ReportsActivity::class.java)) } }, LinearLayout.LayoutParams(0, dp(48), 1f).apply { marginStart = dp(4); marginEnd = dp(4) })
        content.addView(quickActions, params(bottom = 14))
        val overlay = Button(this).apply {
            text = "◉   ثبت سریع روی برنامه‌های دیگر"; textSize = 14f; isAllCaps = false
            setTextColor(FinanceTheme.primary(this)); background = shape(Color.WHITE, 16, Color.rgb(220, 230, 241))
            setOnClickListener {
                if (android.provider.Settings.canDrawOverlays(this@MainActivity)) startService(Intent(this@MainActivity, QuickEntryOverlayService::class.java))
                else startActivity(Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName")))
            }
        }
        content.addView(overlay, params(dp(52), 24))

        val recentHeader = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(2), 0, dp(2), dp(12)) }
        recentHeader.addView(text("تراکنش‌های اخیر", 19, ink, true), LinearLayout.LayoutParams(0, -2, 1f))
        recentHeader.addView(text("آخرین ثبت‌ها", 11, muted, false))
        content.addView(recentHeader)
        val items = db.all().take(20)
        if (items.isEmpty()) {
            val empty = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(22), dp(28), dp(22), dp(28)); background = shape(Color.WHITE, 20, Color.rgb(231, 237, 245)) }
            empty.addView(TextView(this).apply { text = "＋"; textSize = 27f; gravity = Gravity.CENTER; setTextColor(Color.rgb(33, 116, 171)); background = shape(Color.rgb(231, 244, 252), 40) }, LinearLayout.LayoutParams(dp(56), dp(56)))
            empty.addView(text("هنوز تراکنشی ثبت نکرده‌ای", 16, ink, true).apply { gravity = Gravity.CENTER; setPadding(0, dp(12), 0, dp(5)) })
            empty.addView(text("برای شروع، اولین واریز یا پرداختت را ثبت کن.", 12, muted, false).apply { gravity = Gravity.CENTER })
            content.addView(empty)
        } else items.forEach { item ->
            val isDeposit = item.kind == "واریز"
            val isTransfer = item.kind == "انتقال بین حساب‌ها"
            val transactionColor = when { isTransfer -> Color.rgb(78, 103, 143); isDeposit -> green; else -> red }
            val transactionTint = when { isTransfer -> Color.rgb(235, 241, 250); isDeposit -> Color.rgb(226, 248, 240); else -> Color.rgb(255, 236, 238) }
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(14), dp(14), dp(14)); background = shape(Color.WHITE, 18, Color.rgb(232, 238, 246)) }
            val icon = TextView(this).apply {
                text = when { isTransfer -> "⇄"; isDeposit -> "↓"; else -> "↑" }; textSize = 23f; gravity = Gravity.CENTER
                setTextColor(transactionColor); background = shape(transactionTint, 15)
            }
            row.addView(icon, LinearLayout.LayoutParams(dp(46), dp(46)).apply { marginStart = dp(12) })
            val detailsBox = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
            detailsBox.addView(text(item.reason.ifBlank { item.kind }, 14, ink, true))
            val accountLine = listOf(if (isTransfer) "${item.account} ← ${item.toBank}" else item.account, item.method, item.company.takeIf { it.isNotBlank() }?.let { "شرکت: $it" }).filterNotNull().filter { it.isNotBlank() }.joinToString("  •  ")
            detailsBox.addView(text(accountLine, 11, muted, false).apply { setPadding(0, dp(4), 0, 0) })
            detailsBox.addView(text(item.jalaliDate, 10, muted, false).apply { setPadding(0, dp(3), 0, 0) })
            row.addView(detailsBox, LinearLayout.LayoutParams(0, -2, 1f))
            val amountText = TextView(this).apply {
                text = if (isTransfer) "${money(item.amount)}\nتومان" else "${if (isDeposit) "+" else "−"}${money(item.amount)}\nتومان"
                textSize = 12f; gravity = Gravity.END; setTextColor(transactionColor); typeface = Typeface.DEFAULT_BOLD
            }
            row.addView(amountText)
            content.addView(row, params(bottom = 8))
        }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(7), dp(8), dp(7))
            setBackgroundColor(Color.WHITE)
        }
        fun navButton(label: String, target: Class<*>, selected: Boolean = false) {
            val b = TextView(this).apply {
                text = label
                textSize = 11f
                gravity = Gravity.CENTER
                setTextColor(if (selected) FinanceTheme.primary(this) else muted)
                setPadding(dp(4), dp(8), dp(4), dp(8))
                background = if (selected) shape(FinanceTheme.tint(this), 12) else shape(Color.WHITE, 12)
                setOnClickListener { if (!selected) startActivity(Intent(this@MainActivity, target)) }
            }
            nav.addView(b, LinearLayout.LayoutParams(0, dp(44), 1f))
        }
        navButton("⌂\nخانه", MainActivity::class.java, true)
        navButton("☷\nتراکنش‌ها", TransactionsActivity::class.java)
        navButton("▥\nگزارش‌ها", ReportsActivity::class.java)
        navButton("⚙\nتنظیمات", SettingsActivity::class.java)
        root.addView(nav, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
    }

    private fun statCard(title: String, value: Long, color: Int, symbol: String): LinearLayout {
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)); background = shape(Color.WHITE, 19, Color.rgb(231, 237, 245)); elevation = dp(1).toFloat() }
        card.addView(LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            addView(text(title, 12, muted, false), LinearLayout.LayoutParams(0, -2, 1f))
            addView(TextView(this@MainActivity).apply { text = symbol; textSize = 16f; gravity = Gravity.CENTER; setTextColor(color); background = shape(Color.rgb(244, 247, 251), 10) }, LinearLayout.LayoutParams(dp(30), dp(30)))
        })
        card.addView(text("${money(value)}", 17, ink, true).apply { setPadding(0, dp(12), 0, dp(3)) })
        card.addView(text("تومان", 11, muted, false))
        return card
    }

    private fun text(value: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        text = value; textSize = size.toFloat(); setTextColor(color); gravity = Gravity.RIGHT
        if (bold) setTypeface(typeface, Typeface.BOLD)
    }
    private fun shape(color: Int, radius: Int, stroke: Int? = null) = GradientDrawable().apply {
        setColor(color); cornerRadius = dp(radius).toFloat()
        if (stroke != null) setStroke(dp(1), stroke)
    }
    private fun params(height: Int = -2, bottom: Int = 0) = LinearLayout.LayoutParams(-1, height).apply { bottomMargin = dp(bottom) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
    private fun money(value: Long) = String.format(java.util.Locale("fa", "IR"), "%,d", value)
}

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

class AddTransactionActivity : Activity() {
    private lateinit var db: FinanceDb
    private var kind = "پرداخت/برداشت"
    private var method = ""
    private lateinit var amount: EditText
    private lateinit var account: EditText
    private lateinit var toBank: EditText
    private lateinit var person: EditText
    private lateinit var reason: EditText
    private lateinit var company: EditText
    private lateinit var dateLabel: TextView
    private lateinit var internalTransfer: CheckBox
    private val blue get() = FinanceTheme.primary(this)
    private val ink = Color.rgb(20, 32, 55)
    private val muted = Color.rgb(111, 126, 148)
    private val bg = Color.rgb(245, 248, 252)
    private val green = Color.rgb(18, 151, 111)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FinanceDb(this)
        kind = intent.getStringExtra("initial_kind") ?: "پرداخت/برداشت"
        window.decorView.layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
        if (android.os.Build.VERSION.SDK_INT >= 30) window.setDecorFitsSystemWindows(true)
        window.statusBarColor = bg
        window.navigationBarColor = Color.WHITE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val outer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg); layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL }
        val scroll = ScrollView(this).apply { clipToPadding = false; setPadding(dp(18), dp(16), dp(18), dp(18)) }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL }
        content.addView(TextView(this).apply { text = "ثبت تراکنش"; textSize = 25f; setTextColor(ink); typeface = Typeface.DEFAULT_BOLD })
        content.addView(TextView(this).apply { text = "جزئیات تراکنش مالی‌ات را وارد کن"; textSize = 13f; setTextColor(muted); setPadding(0, dp(4), 0, dp(16)) })

        val dateCard = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(13), dp(14), dp(13)); background = shape(Color.WHITE, 15, Color.rgb(229, 236, 245)) }
        dateCard.addView(TextView(this).apply { text = "◷"; textSize = 21f; setTextColor(blue) }, LinearLayout.LayoutParams(dp(34), -2))
        dateLabel = TextView(this).apply { text = "تاریخ و ساعت شمسی: ${Jalali.now()}"; textSize = 12f; setTextColor(muted) }
        dateCard.addView(dateLabel)
        content.addView(dateCard, lp(bottom = 16))

        content.addView(sectionTitle("نوع تراکنش"), lp(bottom = 7))
        val types = RadioGroup(this).apply { orientation = RadioGroup.HORIZONTAL; gravity = Gravity.CENTER; setPadding(dp(5), dp(5), dp(5), dp(5)); background = shape(Color.WHITE, 16, Color.rgb(226, 234, 244)) }
        val payment = RadioButton(this).apply { text = "پرداخت / برداشت"; isChecked = kind != "واریز"; id = 101; textSize = 14f; buttonTintList = android.content.res.ColorStateList.valueOf(blue); setTextColor(ink); setPadding(dp(8), dp(8), dp(8), dp(8)) }
        val deposit = RadioButton(this).apply { text = "واریز"; isChecked = kind == "واریز"; id = 102; textSize = 14f; buttonTintList = android.content.res.ColorStateList.valueOf(green); setTextColor(ink); setPadding(dp(8), dp(8), dp(8), dp(8)) }
        types.addView(payment, RadioGroup.LayoutParams(0, -2, 1f)); types.addView(deposit, RadioGroup.LayoutParams(0, -2, 1f))
        types.setOnCheckedChangeListener { _, checked ->
            kind = if (checked == 102) "واریز" else "پرداخت/برداشت"
            account.hint = if (kind == "واریز") "بانک / حساب دریافت‌کننده (اختیاری)" else "نام حساب یا بانک مبدأ (اجباری)"
            if (kind == "واریز") internalTransfer.isChecked = false
            internalTransfer.isEnabled = kind != "واریز"
            internalTransfer.alpha = if (kind == "واریز") 0.5f else 1f
        }
        content.addView(types, lp(bottom = 7))
        account.hint = if (kind == "واریز") "بانک / حساب دریافت‌کننده (اختیاری)" else "نام حساب یا بانک مبدأ"
        internalTransfer = CheckBox(this).apply {
            text = "انتقال بین حساب‌های خودم (درآمد/هزینه محسوب نشود)"
            textSize = 12f
            setTextColor(ink)
            buttonTintList = android.content.res.ColorStateList.valueOf(blue)
            setPadding(dp(4), dp(4), dp(4), dp(8))
            setOnCheckedChangeListener { _, checked ->
                toBank.isEnabled = true
                if (checked) toBank.hint = "حساب مقصد الزامی است" else toBank.hint = "بانک یا حساب مقصد (در صورت نیاز)"
            }
        }
        content.addView(internalTransfer, lp(bottom = 12))

        val form = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(15), dp(16), dp(16)); background = shape(Color.WHITE, 20, Color.rgb(230, 236, 245)) }
        form.addView(sectionTitle("اطلاعات اصلی"), lp(bottom = 3))
        amount = field(form, "مبلغ (تومان) *", "مثلاً ۲۵۰۰۰۰", true)
        account = field(form, "بانک / حساب مبدأ (برای واریز اختیاری)", "نام حساب یا بانک مبدأ", false)
        form.addView(sectionTitle("روش تراکنش (اجباری) *").apply { setPadding(0, dp(12), 0, dp(5)) })
        val methods = RadioGroup(this).apply { orientation = RadioGroup.VERTICAL }
        listOf("کارتخوان (POS)", "کارت به کارت", "نقدی").forEachIndexed { i, value ->
            val option = RadioButton(this).apply {
                text = value; id = 200 + i; textSize = 14f; setTextColor(ink)
                buttonTintList = android.content.res.ColorStateList.valueOf(blue)
                setPadding(dp(6), dp(7), dp(6), dp(7))
                setOnClickListener { method = value }
            }
            methods.addView(option)
        }
        form.addView(methods)
        form.addView(View(this).apply { setBackgroundColor(Color.rgb(237, 241, 247)) }, LinearLayout.LayoutParams(-1, dp(1)).apply { topMargin = dp(10); bottomMargin = dp(5) })
        form.addView(sectionTitle("اطلاعات تکمیلی").apply { setPadding(0, dp(7), 0, 0) })
        toBank = field(form, "به بانک:", "بانک یا حساب مقصد (در صورت انتقال)", false)
        person = field(form, "به نام:", "نام شخص یا طرف حساب", false)
        reason = field(form, "بابت:", "مثلاً حقوق، خرید روزانه، قبض", false)
        company = field(form, "شرکت / محل کار:", "مثلاً نام شرکت پرداخت‌کننده حقوق", false)
        content.addView(form)
        scroll.addView(content)
        outer.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val buttons = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(dp(18), dp(11), dp(18), dp(12)); background = shape(Color.WHITE, 0).also { it.setStroke(dp(1), Color.rgb(233, 238, 245)) } }
        buttons.addView(Button(this).apply {
            text = "برگشت"; textSize = 14f; isAllCaps = false; setTextColor(ink)
            background = shape(Color.rgb(242, 246, 250), 15)
            setOnClickListener { finish() }
        }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginEnd = dp(6) })
        buttons.addView(Button(this).apply {
            text = "✓   ذخیره تراکنش"; textSize = 14f; isAllCaps = false; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(FinanceTheme.dark(this@AddTransactionActivity), FinanceTheme.primary(this@AddTransactionActivity))).apply { cornerRadius = dp(15).toFloat() }
            setOnClickListener { save() }
        }, LinearLayout.LayoutParams(0, dp(52), 1f).apply { marginStart = dp(6) })
        outer.addView(buttons)
        setContentView(outer)
    }

    private fun field(parent: LinearLayout, title: String, hint: String, numeric: Boolean): EditText {
        parent.addView(TextView(this).apply { text = title; textSize = 13f; setTextColor(ink); typeface = Typeface.DEFAULT_BOLD; setPadding(0, dp(12), 0, dp(5)) })
        val edit = EditText(this).apply {
            this.hint = hint; textSize = 14f; setSingleLine(true); setTextColor(ink); setHintTextColor(Color.rgb(155, 166, 181))
            setPadding(dp(13), dp(5), dp(13), dp(5)); layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
            background = shape(Color.rgb(248, 250, 253), 12, Color.rgb(226, 234, 244))
            if (numeric) inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        parent.addView(edit, LinearLayout.LayoutParams(-1, dp(50)))
        return edit
    }

    private fun sectionTitle(value: String) = TextView(this).apply { text = value; textSize = 15f; setTextColor(ink); typeface = Typeface.DEFAULT_BOLD }
    private fun shape(color: Int, radius: Int, stroke: Int? = null) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat(); if (stroke != null) setStroke(dp(1), stroke) }
    private fun lp(height: Int = -2, bottom: Int = 0) = LinearLayout.LayoutParams(-1, height).apply { bottomMargin = dp(bottom) }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun save() {
        val amountText = amount.text.toString().trim()
            .replace(",", "").replace("٬", "").replace(" ", "")
            .map { ch ->
                when (ch) {
                    in '۰'..'۹' -> ('0'.code + (ch.code - '۰'.code)).toChar()
                    in '٠'..'٩' -> ('0'.code + (ch.code - '٠'.code)).toChar()
                    else -> ch
                }
            }.joinToString("")
        val amountValue = amountText.toLongOrNull()
        if (amountValue == null || amountValue <= 0L) {
            alert("مبلغ معتبر و بزرگ‌تر از صفر وارد کن.")
            return
        }
        val source = account.text.toString().trim()
        if (source.isEmpty() && kind != "واریز") {
            alert("برای پرداخت، نام بانک یا حساب مبدأ را وارد کن.")
            return
        }
        if (method.isBlank()) {
            alert("حتماً یکی از روش‌های تراکنش را انتخاب کن.")
            return
        }
        val destination = toBank.text.toString().trim()
        val effectiveKind = if (kind == "پرداخت/برداشت" && internalTransfer.isChecked) "انتقال بین حساب‌ها" else kind
        if (effectiveKind == "انتقال بین حساب‌ها") {
            if (destination.isBlank()) {
                alert("برای انتقال داخلی، حساب مقصد را وارد کن.")
                return
            }
            if (destination.equals(source, ignoreCase = true)) {
                alert("حساب مبدأ و مقصد نمی‌توانند یکسان باشند.")
                return
            }
        }
        try {
            db.insert(
                TransactionItem(
                    id = 0,
                    kind = effectiveKind,
                    amount = amountValue,
                    account = source,
                    method = method,
                    toBank = destination,
                    person = person.text.toString().trim(),
                    reason = reason.text.toString().trim(),
                    timestamp = System.currentTimeMillis(),
                    jalaliDate = Jalali.now(),
                    company = company.text.toString().trim()
                )
            )
            Toast.makeText(this, "تراکنش ذخیره شد", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            alert("ذخیره تراکنش انجام نشد. دوباره تلاش کن.")
        }
    }
    private fun alert(message: String) = AlertDialog.Builder(this).setMessage(message).setPositiveButton("باشه", null).show()
}

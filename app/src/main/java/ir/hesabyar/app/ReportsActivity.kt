package ir.hesabyar.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.Calendar

class ReportsActivity : Activity() {
    private lateinit var db: FinanceDb
    private var period = "همه دوره‌ها"
    private val ink=Color.rgb(20,32,55); private val muted=Color.rgb(111,126,148); private val bg=Color.rgb(245,248,252)
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);db=FinanceDb(this);render()}
    override fun onResume(){super.onResume();if(::db.isInitialized)render()}
    private fun render(){
        window.statusBarColor=bg;window.navigationBarColor=Color.WHITE
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(18),dp(18),dp(18));setBackgroundColor(bg);layoutDirection=ViewGroup.LAYOUT_DIRECTION_RTL}
        root.addView(txt("گزارش‌های مالی",25,ink,true));root.addView(txt("فیلتر بازه، درآمد، هزینه و محل پرداخت حقوق",12,muted,false).apply{setPadding(0,dp(5),0,dp(12))})
        val spinner=Spinner(this)
        val periods=listOf("همه دوره‌ها","امروز","این هفته","این ماه","۳۰ روز اخیر")
        spinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,periods)
        spinner.setSelection(periods.indexOf(period).coerceAtLeast(0))
        spinner.onItemSelectedListener=object:android.widget.AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent:android.widget.AdapterView<*>?){}
            override fun onItemSelected(parent:android.widget.AdapterView<*>?,view:View?,position:Int,id:Long){val next=periods[position];if(next!=period){period=next;render()}}
        }
        root.addView(spinner,LinearLayout.LayoutParams(-1,dp(48)).apply{bottomMargin=dp(12)})
        val all=filterRows(db.all());val income=all.filter{it.kind=="واریز"}.sumOf{it.amount};val expense=all.filter{it.kind=="پرداخت/برداشت"}.sumOf{it.amount};val transfers=all.filter{it.kind=="انتقال بین حساب‌ها"}.sumOf{it.amount}
        fun card(title:String,value:Long,color:Int){val c=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(14),dp(16),dp(14));background=shape(Color.WHITE,17)};c.addView(txt(title,13,muted,false));c.addView(txt("${money(value)} تومان",21,color,true).apply{setPadding(0,dp(6),0,0)});root.addView(c,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(9)})}
        card("مجموع درآمد ثبت‌شده",income,Color.rgb(18,151,111));card("مجموع هزینه ثبت‌شده",expense,Color.rgb(218,78,91));card("خالص درآمد منهای هزینه",income-expense,FinanceTheme.primary(this));card("مجموع انتقال داخلی (خارج از درآمد/هزینه)",transfers,Color.rgb(83,105,145))
        root.addView(txt("واریزی‌ها بر اساس شرکت / محل کار",17,ink,true).apply{setPadding(0,dp(12),0,dp(8))})
        val byCompany=all.filter{it.kind=="واریز"}.groupBy{it.company.ifBlank{"شرکت ثبت نشده"}}.mapValues{e->e.value.sumOf{it.amount}}.toList().sortedByDescending{it.second}.take(10)
        if(byCompany.isEmpty())root.addView(txt("پس از ثبت واریزی‌ها، نام شرکت و جمع واریزی‌های آن نمایش داده می‌شود.",12,muted,false)) else byCompany.forEach{(name,value)->reportRow(root,name,value,Color.rgb(18,151,111),income)}
        root.addView(txt("هزینه‌ها بر اساس شرح «بابت»",17,ink,true).apply{setPadding(0,dp(12),0,dp(8))})
        val byReason=all.filter{it.kind=="پرداخت/برداشت"}.groupBy{it.reason.ifBlank{"بدون شرح"}}.mapValues{e->e.value.sumOf{it.amount}}.toList().sortedByDescending{it.second}.take(12)
        if(byReason.isEmpty())root.addView(txt("برای این بازه هزینه‌ای ثبت نشده است.",13,muted,false)) else byReason.forEach{(name,value)->reportRow(root,name,value,Color.rgb(218,78,91),expense)}
        root.addView(txt("انتقال بین حساب‌های خودت در محاسبه درآمد و هزینه وارد نمی‌شود. گزارش‌ها فقط از تراکنش‌های ثبت‌شده روی این دستگاه محاسبه می‌شوند.",11,muted,false).apply{setPadding(0,dp(14),0,0)})
        setContentView(ScrollView(this).apply{addView(root)})
    }
    private fun filterRows(rows:List<TransactionItem>):List<TransactionItem>{
        val now=System.currentTimeMillis();val cal=Calendar.getInstance();cal.timeInMillis=now
        return when(period){
            "امروز" -> rows.filter{it.jalaliDate.take(10)==Jalali.now().take(10)}
            "این هفته" -> { val c=Calendar.getInstance(); c.timeInMillis=now; val daysSinceSaturday=c.get(Calendar.DAY_OF_WEEK)%7; c.add(Calendar.DAY_OF_YEAR,-daysSinceSaturday); c.set(Calendar.HOUR_OF_DAY,0);c.set(Calendar.MINUTE,0);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0); val start=c.timeInMillis; rows.filter{it.timestamp>=start} }
            "این ماه" -> { val prefix=Jalali.now().take(7); rows.filter{it.jalaliDate.startsWith(prefix)} }
            "۳۰ روز اخیر" -> rows.filter{it.timestamp>=now-30L*24*60*60*1000}
            else -> rows
        }
    }
    private fun reportRow(root:LinearLayout,name:String,value:Long,color:Int,totalValue:Long){
        val row=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(10),dp(12),dp(10));background=shape(Color.WHITE,13)}
        val h=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=android.view.Gravity.CENTER_VERTICAL};h.addView(txt(name,13,ink,true),LinearLayout.LayoutParams(0,-2,1f));h.addView(txt("${money(value)} تومان",12,color,true));row.addView(h)
        val width=(resources.displayMetrics.widthPixels-dp(60)).coerceAtLeast(dp(100));val ratio=if(totalValue<=0)0.0 else value.toDouble()/totalValue.toDouble();val bar=View(this).apply{background=GradientDrawable().apply{setColor(color);cornerRadius=dp(4).toFloat()}}
        val track=FrameLayout(this).apply{setBackgroundColor(Color.rgb(239,242,247));addView(bar,FrameLayout.LayoutParams((width*ratio).toInt().coerceIn(if(value>0)dp(3) else 0,width),dp(6)))}
        row.addView(track,LinearLayout.LayoutParams(-1,dp(6)).apply{topMargin=dp(9)});root.addView(row,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(7)})
    }
    private fun txt(s:String,size:Int,color:Int,bold:Boolean)=TextView(this).apply{text=s;textSize=size.toFloat();setTextColor(color);if(bold)typeface=Typeface.DEFAULT_BOLD}
    private fun shape(c:Int,r:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt();private fun money(v:Long)=String.format(java.util.Locale("fa","IR"),"%,d",v)
}

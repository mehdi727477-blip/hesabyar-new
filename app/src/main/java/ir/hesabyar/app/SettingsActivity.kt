package ir.hesabyar.app

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.ViewGroup
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject

class SettingsActivity : Activity() {
    private lateinit var db:FinanceDb
    private val ink=Color.rgb(20,32,55);private val muted=Color.rgb(111,126,148);private val bg=Color.rgb(245,248,252)
    private val createBackup=401;private val openBackup=402
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);db=FinanceDb(this);render()}
    private fun render(){window.statusBarColor=bg;window.navigationBarColor=Color.WHITE
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(18),dp(18),dp(18));setBackgroundColor(bg);layoutDirection=ViewGroup.LAYOUT_DIRECTION_RTL}
        root.addView(txt("تنظیمات حساب‌یار",25,ink,true));root.addView(txt("حریم خصوصی، داده‌ها و ابزارهای برنامه",13,muted,false).apply{setPadding(0,dp(5),0,dp(16))})
        section(root,"اطلاعات برنامه","حساب‌یار نسخه ۰٫۷٫۰\nذخیره‌سازی محلی روی همین دستگاه؛ برای ثبت و مشاهده تراکنش‌ها اینترنت لازم نیست.")
        action(root,"🎨  رنگ و ظاهر برنامه","انتخاب رنگ اصلی منوها، دکمه‌ها و عناصر برجسته؛ تغییر بلافاصله در بخش‌های برنامه اعمال می‌شود") {
            val keys = FinanceTheme.choices().keys.toList()
            val labels = keys.map { FinanceTheme.choices()[it] ?: it }.toTypedArray()
            val current = keys.indexOfFirst { it == getSharedPreferences("hesabyar_preferences", MODE_PRIVATE).getString("accent_theme", "blue") }.coerceAtLeast(0)
            AlertDialog.Builder(this).setTitle("انتخاب تم رنگی").setSingleChoiceItems(labels, current) { dialog, which ->
                FinanceTheme.save(this, keys[which]); dialog.dismiss(); Toast.makeText(this, "تم تغییر کرد؛ به صفحه موردنظر برگرد یا آن را دوباره باز کن.", Toast.LENGTH_SHORT).show(); render()
            }.setNegativeButton("بستن", null).show()
        }
        action(root,"⬇  تهیه نسخه پشتیبان","خروجی JSON از تراکنش‌ها برای نگهداری امن یا انتقال به دستگاه دیگر"){val i=Intent(Intent.ACTION_CREATE_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/json";putExtra(Intent.EXTRA_TITLE,"hesabyar-backup.json")};startActivityForResult(i,createBackup)}
        action(root,"⬆  بازیابی نسخه پشتیبان","داده‌های موجود جایگزین داده‌های فعلی می‌شوند؛ قبل از ادامه نسخه پشتیبان بگیر"){AlertDialog.Builder(this).setTitle("بازیابی اطلاعات").setMessage("بازیابی، تراکنش‌های فعلی را با فایل انتخابی جایگزین می‌کند. ادامه می‌دهی؟").setNegativeButton("لغو",null).setPositiveButton("انتخاب فایل"){_,_->val i=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/json"};startActivityForResult(i,openBackup)}.show()}
        action(root,"حساب‌ها و مانده‌ها","نمایش حساب‌هایی که در تراکنش‌ها استفاده شده‌اند"){val all=db.all();val names=(all.map{it.account}+all.map{it.toBank}).filter{it.isNotBlank()}.distinct().sorted();val msg=if(names.isEmpty())"هنوز حسابی ثبت نشده است." else names.joinToString("\n"){ "$it: ${money(db.accountBalance(it))} تومان"};AlertDialog.Builder(this).setTitle("حساب‌های شناسایی‌شده").setMessage(msg).setPositiveButton("باشه",null).show()}
        action(root,"حریم خصوصی","داده‌های مالی در نسخه فعلی در پایگاه داده محلی برنامه ذخیره می‌شوند. حذف برنامه یا پاک‌کردن داده‌های آن ممکن است اطلاعات را از بین ببرد؛ نسخه پشتیبان نگه دار."){AlertDialog.Builder(this).setTitle("حریم خصوصی و نگهداری داده").setMessage("حساب‌یار در این نسخه برای ثبت تراکنش به حساب کاربری یا اتصال اینترنت نیاز ندارد. فایل پشتیبان شامل اطلاعات مالی است؛ آن را در محل امن نگهداری کن.").setPositiveButton("متوجه شدم",null).show()}
        action(root,"حذف همه تراکنش‌ها","این عملیات برگشت‌پذیر نیست"){AlertDialog.Builder(this).setTitle("حذف همه اطلاعات؟").setMessage("تمام تراکنش‌های ذخیره‌شده حذف می‌شوند. ابتدا نسخه پشتیبان بگیر.").setNegativeButton("لغو",null).setPositiveButton("حذف همه"){_,_->db.replaceAll(emptyList());Toast.makeText(this,"همه تراکنش‌ها حذف شدند",Toast.LENGTH_SHORT).show()}.show()}
        root.addView(txt("قابلیت‌هایی مانند بودجه‌بندی، دسته‌بندی قابل ویرایش، رمز برنامه، اعلان اقساط و همگام‌سازی امن هنوز در برنامه کامل نشده‌اند.",11,muted,false).apply{setPadding(0,dp(12),0,0)})
        setContentView(ScrollView(this).apply{addView(root)})
    }
    private fun action(root:LinearLayout,title:String,subtitle:String,onClick:()->Unit){val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(15),dp(14),dp(15),dp(14));background=shape(Color.WHITE,16);setOnClickListener{onClick()}};card.addView(txt(title,15,ink,true));card.addView(txt(subtitle,11,muted,false).apply{setPadding(0,dp(5),0,0)});root.addView(card,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(9)})}
    private fun section(root:LinearLayout,title:String,body:String){val card=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(15),dp(14),dp(15),dp(14));background=shape(Color.WHITE,16)};card.addView(txt(title,14,ink,true));card.addView(txt(body,12,muted,false).apply{setPadding(0,dp(6),0,0)});root.addView(card,LinearLayout.LayoutParams(-1,-2).apply{bottomMargin=dp(10)})}
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=RESULT_OK)return;val uri=data?.data?:return;try{if(requestCode==createBackup)exportBackup(uri) else if(requestCode==openBackup)importBackup(uri)}catch(e:Exception){AlertDialog.Builder(this).setMessage("عملیات انجام نشد: فایل نامعتبر یا غیرقابل دسترسی است.").setPositiveButton("باشه",null).show()}}
    private fun exportBackup(uri:Uri){val arr=JSONArray();db.all().forEach{t->arr.put(JSONObject().apply{put("kind",t.kind);put("amount",t.amount);put("account",t.account);put("method",t.method);put("toBank",t.toBank);put("person",t.person);put("reason",t.reason);put("timestamp",t.timestamp);put("jalaliDate",t.jalaliDate);put("company",t.company)})};contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use{it.write(JSONObject().put("app","HesabYar").put("version",1).put("transactions",arr).toString(2))}?:error("stream");Toast.makeText(this,"نسخه پشتیبان ذخیره شد",Toast.LENGTH_LONG).show()}
    private fun importBackup(uri:Uri){val text=contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use{it.readText()}?:error("stream");val root=JSONObject(text);val arr=root.getJSONArray("transactions");val rows=mutableListOf<TransactionItem>();for(i in 0 until arr.length()){val o=arr.getJSONObject(i);rows+=TransactionItem(0,o.getString("kind"),o.getLong("amount"),o.getString("account"),o.optString("method"),o.optString("toBank"),o.optString("person"),o.optString("reason"),o.getLong("timestamp"),o.optString("jalaliDate"),o.optString("company"))};db.replaceAll(rows);Toast.makeText(this,"${rows.size} تراکنش بازیابی شد",Toast.LENGTH_LONG).show()}
    private fun txt(s:String,size:Int,color:Int,bold:Boolean)=TextView(this).apply{text=s;textSize=size.toFloat();setTextColor(color);if(bold)typeface=Typeface.DEFAULT_BOLD}
    private fun shape(c:Int,r:Int)=GradientDrawable().apply{setColor(c);cornerRadius=dp(r).toFloat()};private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt();private fun money(v:Long)=String.format(java.util.Locale("fa","IR"),"%,d",v)
}

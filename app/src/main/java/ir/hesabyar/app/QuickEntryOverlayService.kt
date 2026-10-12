package ir.hesabyar.app

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color
import android.view.ViewGroup

class QuickEntryOverlayService : Service() {
    private var view: LinearLayout? = null
    private var manager: WindowManager? = null
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!android.provider.Settings.canDrawOverlays(this)) { stopSelf(); return START_NOT_STICKY }
        if (view != null) return START_NOT_STICKY
        manager = getSystemService(WINDOW_SERVICE) as WindowManager
        val panel = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(24), dp(20), dp(24), dp(20)); setBackgroundColor(Color.WHITE); elevation = dp(12).toFloat(); layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL }
        panel.addView(TextView(this).apply { text = "حساب‌یار | ثبت سریع"; textSize = 19f; setTextColor(Color.rgb(15,23,42)) })
        panel.addView(TextView(this).apply { text = "برای ثبت سریع، فرم کامل را باز کن."; textSize = 14f; setTextColor(Color.DKGRAY); setPadding(0,dp(12),0,dp(12)) })
        panel.addView(Button(this).apply { text = "باز کردن فرم تراکنش"; setOnClickListener {
            val i = Intent(this@QuickEntryOverlayService, AddTransactionActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i); stopOverlay()
        } })
        panel.addView(Button(this).apply { text = "بستن"; setOnClickListener { stopOverlay() } })
        val type = if (android.os.Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams((resources.displayMetrics.widthPixels * 0.88).toInt(), WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.CENTER }
        view = panel
        manager?.addView(panel, params)
        return START_NOT_STICKY
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun removeOverlay() {
        val oldView = view
        view = null
        if (oldView != null) {
            try { manager?.removeView(oldView) } catch (_: IllegalArgumentException) { }
        }
    }

    private fun stopOverlay() {
        removeOverlay()
        stopSelf()
    }

    override fun onDestroy() {
        removeOverlay()
        manager = null
        super.onDestroy()
    }
}

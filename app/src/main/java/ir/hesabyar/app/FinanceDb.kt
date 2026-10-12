package ir.hesabyar.app

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** One locally stored financial transaction. Amounts are always stored in Toman as positive values. */
data class TransactionItem(
    val id: Long,
    val kind: String,
    val amount: Long,
    val account: String,
    val method: String,
    val toBank: String,
    val person: String,
    val reason: String,
    val timestamp: Long,
    val jalaliDate: String,
    val company: String = ""
)

class FinanceDb(context: Context) : SQLiteOpenHelper(context, "hesabyar.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                kind TEXT NOT NULL,
                amount INTEGER NOT NULL CHECK(amount > 0),
                account TEXT NOT NULL,
                method TEXT NOT NULL,
                to_bank TEXT NOT NULL DEFAULT '',
                person TEXT NOT NULL DEFAULT '',
                reason TEXT NOT NULL DEFAULT '',
                timestamp INTEGER NOT NULL,
                jalali_date TEXT NOT NULL,
                company TEXT NOT NULL DEFAULT ''
            )""".trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE transactions ADD COLUMN company TEXT NOT NULL DEFAULT ''")
        }
    }

    fun insert(item: TransactionItem): Long {
        val values = ContentValues().apply {
            put("kind", item.kind)
            put("amount", item.amount)
            put("account", item.account)
            put("method", item.method)
            put("to_bank", item.toBank)
            put("person", item.person)
            put("reason", item.reason)
            put("timestamp", item.timestamp)
            put("jalali_date", item.jalaliDate)
            put("company", item.company)
        }
        return writableDatabase.insertOrThrow("transactions", null, values)
    }

    fun all(): List<TransactionItem> {
        val result = mutableListOf<TransactionItem>()
        readableDatabase.rawQuery(
            "SELECT id, kind, amount, account, method, to_bank, person, reason, timestamp, jalali_date, company FROM transactions ORDER BY timestamp DESC, id DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                result += TransactionItem(
                    id = c.getLong(0),
                    kind = c.getString(1),
                    amount = c.getLong(2),
                    account = c.getString(3),
                    method = c.getString(4),
                    toBank = c.getString(5) ?: "",
                    person = c.getString(6) ?: "",
                    reason = c.getString(7) ?: "",
                    timestamp = c.getLong(8),
                    jalaliDate = c.getString(9),
                    company = c.getString(10) ?: ""
                )
            }
        }
        return result
    }

    fun deleteTransaction(id: Long) { writableDatabase.delete("transactions", "id = ?", arrayOf(id.toString())) }

    fun replaceAll(items: List<TransactionItem>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete("transactions", null, null)
            items.forEach { item ->
                val values = ContentValues().apply {
                    put("kind", item.kind); put("amount", item.amount); put("account", item.account)
                    put("method", item.method); put("to_bank", item.toBank); put("person", item.person)
                    put("reason", item.reason); put("timestamp", item.timestamp); put("jalali_date", item.jalaliDate); put("company", item.company)
                }
                db.insertOrThrow("transactions", null, values)
            }
            db.setTransactionSuccessful()
        } finally { db.endTransaction() }
    }

    /** Returns the balance of a named account based on explicitly typed transactions. */
    fun accountBalance(account: String): Long {
        var balance = 0L
        readableDatabase.rawQuery(
            "SELECT kind, amount, account, to_bank FROM transactions",
            null
        ).use { c ->
            while (c.moveToNext()) {
                val kind = c.getString(0)
                val amount = c.getLong(1)
                val source = c.getString(2)
                val destination = c.getString(3) ?: ""
                when (kind) {
                    "واریز" -> if (source == account) balance += amount
                    "پرداخت/برداشت" -> if (source == account) balance -= amount
                    "انتقال بین حساب‌ها" -> {
                        if (source == account) balance -= amount
                        if (destination == account) balance += amount
                    }
                }
            }
        }
        return balance
    }

    /** Internal transfers are deliberately excluded from income and expense totals. */
    fun totals(): Pair<Long, Long> {
        var income = 0L
        var expense = 0L
        readableDatabase.rawQuery("SELECT kind, amount FROM transactions", null).use { c ->
            while (c.moveToNext()) {
                val kind = c.getString(0)
                val amount = c.getLong(1)
                when (kind) {
                    "واریز" -> income += amount
                    "پرداخت/برداشت" -> expense += amount
                    // "انتقال بین حساب‌ها" changes account balances but not income/expenses.
                }
            }
        }
        return income to expense
    }
}

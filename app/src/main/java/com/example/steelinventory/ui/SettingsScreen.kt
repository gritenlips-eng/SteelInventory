package com.example.steelinventory.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.steelinventory.data.AppDatabase
import com.example.steelinventory.data.ChannelSpec
import com.example.steelinventory.data.InventoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** رمز ثابت برنامه برای ورود به این صفحه و رمزنگاری فایل بک‌آپ */
private const val BACKUP_PASSWORD = "9382735491Mas"

private const val BACKUP_VERSION = 3
private val FILE_MAGIC = "STEELBK1".toByteArray(Charsets.US_ASCII)
private const val SALT_LEN = 16
private const val IV_LEN = 16
private const val PBKDF2_ITERATIONS = 20_000

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { AppDatabase.getDatabase(context) }

    var unlocked by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }

    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    /** true = داده‌های فعلی پاک شود، false = ادغام با داده‌های فعلی */
    var wipeBeforeRestore by remember { mutableStateOf(true) }
    var confirmRestore by remember { mutableStateOf(false) }

    fun report(text: String, error: Boolean) {
        message = text
        isError = error
    }

    // ---------- انتخاب مسیر ذخیره فایل بک‌آپ ----------
    val createFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        busy = true
        message = null
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val json = buildBackupJson(db)
                    val encrypted = encryptBytes(json.toByteArray(Charsets.UTF_8), BACKUP_PASSWORD)
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(encrypted)
                        out.flush()
                    } ?: throw IllegalStateException("امکان نوشتن در فایل انتخاب‌شده نبود")
                    encrypted.size
                }
            }
            busy = false
            result.fold(
                onSuccess = { size -> report("پشتیبان با موفقیت ساخته شد (${size / 1024 + 1} کیلوبایت).", false) },
                onFailure = { e -> report("خطا در ساخت پشتیبان: ${e.message}", true) }
            )
        }
    }

    // ---------- انتخاب فایل بک‌آپ برای بازیابی ----------
    val openFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        busy = true
        message = null
        scope.launch {
            val result = runCatching {
                withContext(Dispatchers.IO) {
                    val raw = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: throw IllegalStateException("امکان خواندن فایل انتخاب‌شده نبود")
                    val json = String(decryptBytes(raw, BACKUP_PASSWORD), Charsets.UTF_8)
                    applyBackupJson(db, json, wipeBeforeRestore)
                }
            }
            busy = false
            result.fold(
                onSuccess = { counts ->
                    report("بازیابی انجام شد: ${counts.first} قلم موجودی و ${counts.second} مشخصات ناودانی.", false)
                },
                onFailure = { e -> report("خطا در بازیابی: ${e.message}", true) }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات و پشتیبان‌گیری") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            if (!unlocked) {
                // ------------------ مرحله ورود رمز ------------------
                Text(
                    "برای دسترسی به پشتیبان‌گیری، رمز را وارد کنید.",
                    style = MaterialTheme.typography.bodyLarge
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        message = null
                    },
                    label = { Text("رمز") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        if (passwordInput == BACKUP_PASSWORD) {
                            unlocked = true
                            passwordInput = ""
                            report("رمز تأیید شد.", false)
                        } else {
                            report("رمز اشتباه است.", true)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("ورود")
                }

            } else {
                // ------------------ بک‌آپ ------------------
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("تهیه پشتیبان", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "همه اقلام موجودی و مشخصات ناودانی در یک فایل رمزنگاری‌شده ذخیره می‌شود. " +
                                "مسیر ذخیره را خودت انتخاب می‌کنی (مثلاً Download یا Google Drive).",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Button(
                            onClick = { createFileLauncher.launch(defaultBackupFileName()) },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ساخت فایل پشتیبان")
                        }
                    }
                }

                // ------------------ بازیابی ------------------
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("بازیابی از پشتیبان", style = MaterialTheme.typography.titleMedium)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("پاک کردن داده‌های فعلی")
                                Text(
                                    if (wipeBeforeRestore)
                                        "همه داده‌های فعلی حذف و فقط محتوای فایل نوشته می‌شود."
                                    else
                                        "داده‌های فایل به داده‌های فعلی اضافه می‌شود (تکراری‌ها جایگزین می‌شوند).",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Switch(
                                checked = wipeBeforeRestore,
                                onCheckedChange = { wipeBeforeRestore = it },
                                enabled = !busy
                            )
                        }

                        OutlinedButton(
                            onClick = { confirmRestore = true },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("انتخاب فایل و بازیابی")
                        }
                    }
                }

                Divider()

                OutlinedButton(
                    onClick = {
                        unlocked = false
                        message = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("قفل کردن مجدد این صفحه")
                }
            }

            if (busy) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator()
                    Spacer(Modifier.fillMaxWidth(0.05f))
                    Text("در حال انجام عملیات...")
                }
            }

            message?.let { text ->
                Text(
                    text = text,
                    color = if (isError) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text("تأیید بازیابی") },
            text = {
                Text(
                    if (wipeBeforeRestore)
                        "همه داده‌های فعلی برنامه حذف می‌شود و جای آن محتوای فایل پشتیبان نوشته می‌شود. این کار برگشت‌پذیر نیست. ادامه می‌دهی؟"
                    else
                        "محتوای فایل پشتیبان به داده‌های فعلی اضافه می‌شود. رکوردهای تکراری (کارخانه + نوع + سایز) با نسخه فایل جایگزین می‌شوند. ادامه می‌دهی؟"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmRestore = false
                    openFileLauncher.launch(arrayOf("*/*"))
                }) { Text("ادامه") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestore = false }) { Text("انصراف") }
            }
        )
    }
}

// ---------------------------------------------------------------------------
// ساخت و خواندن JSON
// ---------------------------------------------------------------------------

private suspend fun buildBackupJson(db: AppDatabase): String {
    val items = db.inventoryDao().getAllItemsOnce()
    val specs = db.channelSpecDao().getAllOnce()

    val itemsArray = JSONArray()
    items.forEach { item ->
        itemsArray.put(
            JSONObject().apply {
                put("productType", item.productType)
                put("size", item.size)
                put("declaredWeight", item.declaredWeight)
                put("factoryName", item.factoryName)
                put("bundleCount", item.bundleCount)
                put("weightPerPieceMin", item.weightPerPieceMin)
                put("weightPerPieceMax", item.weightPerPieceMax)
                put("bundleWeight", item.bundleWeight)
                put("receiptDate", item.receiptDate)
            }
        )
    }

    val specsArray = JSONArray()
    specs.forEach { spec ->
        specsArray.put(
            JSONObject().apply {
                put("size", spec.size)
                put("declaredWeight", spec.declaredWeight)
                put("factoryName", spec.factoryName)
                put("wingWidth", spec.wingWidth)
                put("webThickness", spec.webThickness)
            }
        )
    }

    return JSONObject().apply {
        put("version", BACKUP_VERSION)
        put("createdAt", System.currentTimeMillis())
        put("inventory_items", itemsArray)
        put("channel_specs", specsArray)
    }.toString()
}

/** خروجی: تعداد اقلام موجودی و تعداد مشخصات ناودانی بازیابی‌شده */
private suspend fun applyBackupJson(
    db: AppDatabase,
    json: String,
    wipeFirst: Boolean
): Pair<Int, Int> {

    val root = JSONObject(json)
    val itemsArray = root.optJSONArray("inventory_items") ?: JSONArray()
    val specsArray = root.optJSONArray("channel_specs") ?: JSONArray()

    val items = ArrayList<InventoryItem>(itemsArray.length())
    for (i in 0 until itemsArray.length()) {
        val o = itemsArray.getJSONObject(i)
        items.add(
            InventoryItem(
                id = 0,
                productType = o.getString("productType"),
                size = o.getString("size"),
                declaredWeight = o.getDouble("declaredWeight"),
                factoryName = o.getString("factoryName"),
                bundleCount = o.getInt("bundleCount"),
                weightPerPieceMin = o.getDouble("weightPerPieceMin"),
                weightPerPieceMax = o.getDouble("weightPerPieceMax"),
                bundleWeight = o.getDouble("bundleWeight"),
                receiptDate = o.getString("receiptDate")
            )
        )
    }

    val specs = ArrayList<ChannelSpec>(specsArray.length())
    for (i in 0 until specsArray.length()) {
        val o = specsArray.getJSONObject(i)
        specs.add(
            ChannelSpec(
                id = 0,
                size = o.getString("size"),
                declaredWeight = o.getDouble("declaredWeight"),
                factoryName = o.getString("factoryName"),
                wingWidth = o.getDouble("wingWidth"),
                webThickness = o.getDouble("webThickness")
            )
        )
    }

    if (wipeFirst) {
        db.inventoryDao().deleteAllItems()
        db.channelSpecDao().deleteAll()
    }

    if (items.isNotEmpty()) db.inventoryDao().insertAll(items)
    if (specs.isNotEmpty()) db.channelSpecDao().insertAll(specs)

    return items.size to specs.size
}

// ---------------------------------------------------------------------------
// رمزنگاری: AES/CBC + کلید ساخته‌شده از رمز با PBKDF2
// ساختار فایل: MAGIC(8) + SALT(16) + IV(16) + داده رمزشده
// ---------------------------------------------------------------------------

private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
    val spec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, 256)
    val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
    return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
}

private fun encryptBytes(plain: ByteArray, password: String): ByteArray {
    val random = SecureRandom()
    val salt = ByteArray(SALT_LEN).also { random.nextBytes(it) }
    val iv = ByteArray(IV_LEN).also { random.nextBytes(it) }

    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, deriveKey(password, salt), IvParameterSpec(iv))
    val encrypted = cipher.doFinal(plain)

    return FILE_MAGIC + salt + iv + encrypted
}

private fun decryptBytes(raw: ByteArray, password: String): ByteArray {
    val headerSize = FILE_MAGIC.size + SALT_LEN + IV_LEN
    if (raw.size <= headerSize) {
        throw IllegalArgumentException("فایل انتخاب‌شده یک پشتیبان معتبر نیست")
    }
    val magic = raw.copyOfRange(0, FILE_MAGIC.size)
    if (!magic.contentEquals(FILE_MAGIC)) {
        throw IllegalArgumentException("فایل انتخاب‌شده پشتیبان این برنامه نیست")
    }

    val salt = raw.copyOfRange(FILE_MAGIC.size, FILE_MAGIC.size + SALT_LEN)
    val iv = raw.copyOfRange(FILE_MAGIC.size + SALT_LEN, headerSize)
    val body = raw.copyOfRange(headerSize, raw.size)

    return try {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(password, salt), IvParameterSpec(iv))
        cipher.doFinal(body)
    } catch (e: Exception) {
        throw IllegalArgumentException("رمز فایل اشتباه است یا فایل آسیب دیده")
    }
}

private fun defaultBackupFileName(): String {
    val stamp = SimpleDateFormat("yyyyMMdd-HHmm", Locale.US).format(Date())
    return "steel-backup-$stamp.steelbak"
}

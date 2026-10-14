package com.hassantechno.traceo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF0B1020)
private val SurfaceColor = Color(0xFF151D31)
private val Accent = Color(0xFF7C9CFF)
private val Muted = Color(0xFF9AA7C2)
private val TextMain = Color(0xFFF4F7FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { TraceoApp() }
    }
}

private enum class Category { IRANIAN, INTERNATIONAL }
private data class Service(
    val name: String,
    val emoji: String,
    val category: Category,
    val description: String,
    val openUrl: (String) -> String?
)

private val services = listOf(
    Service("بله", "🟢", Category.IRANIAN, "بررسی خودکار عمومی در دسترس نیست") { null },
    Service("روبیکا", "🟣", Category.IRANIAN, "بررسی خودکار عمومی در دسترس نیست") { null },
    Service("ایتا", "🟠", Category.IRANIAN, "بررسی خودکار عمومی در دسترس نیست") { null },
    Service("سروش پلاس", "🔵", Category.IRANIAN, "بررسی خودکار عمومی در دسترس نیست") { null },
    Service("شاد", "🎓", Category.IRANIAN, "بررسی خودکار عمومی در دسترس نیست") { null },
    Service("آی‌گپ", "💬", Category.IRANIAN, "بررسی خودکار عمومی در دسترس نیست") { null },
    Service("WhatsApp", "🟢", Category.INTERNATIONAL, "بازکردن لینک، وجود حساب را تأیید نمی‌کند") { n -> "https://wa.me/" + n.removePrefix("+") },
    Service("Telegram", "🔹", Category.INTERNATIONAL, "بازکردن تلگرام؛ تابع تنظیمات حریم خصوصی") { n -> "tg://resolve?phone=" + n.removePrefix("+") },
    Service("Signal", "🔒", Category.INTERNATIONAL, "بررسی خودکار عمومی در دسترس نیست") { null },
    Service("Viber", "🟣", Category.INTERNATIONAL, "لینک مستقیم ممکن است روی بعضی دستگاه‌ها کار نکند") { n -> "viber://chat?number=" + Uri.encode(n) },
    Service("LINE", "🟩", Category.INTERNATIONAL, "بررسی خودکار عمومی در دسترس نیست") { null },
    Service("WeChat", "🟢", Category.INTERNATIONAL, "بررسی خودکار عمومی در دسترس نیست") { null },
    Service("Instagram", "📸", Category.INTERNATIONAL, "از روی شماره تلفن قابل تأیید عمومی نیست") { null },
    Service("Messenger", "🔵", Category.INTERNATIONAL, "از روی شماره تلفن قابل تأیید عمومی نیست") { null },
    Service("Snapchat", "👻", Category.INTERNATIONAL, "بررسی خودکار عمومی در دسترس نیست") { null },
    Service("Discord", "🎮", Category.INTERNATIONAL, "بررسی خودکار عمومی در دسترس نیست") { null }
)

private fun normalizeIranPhone(raw: String): String? {
    val digits = raw.filter(Char::isDigit)
    val national = when {
        digits.startsWith("0098") -> digits.drop(4)
        digits.startsWith("98") -> digits.drop(2)
        digits.startsWith("0") -> digits.drop(1)
        else -> digits
    }
    if (national.length != 10 || !national.startsWith("9")) return null
    return "+98$national"
}

@Composable
private fun TraceoApp() {
    var phone by remember { mutableStateOf("") }
    var normalized by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var searched by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    MaterialTheme(colorScheme = darkColorScheme(
        background = Background, surface = SurfaceColor, primary = Accent,
        onBackground = TextMain, onSurface = TextMain
    )) {
        Surface(Modifier.fillMaxSize(), color = Background) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 28.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(48.dp).background(Accent, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            Text("T", color = Background, fontWeight = FontWeight.Black, fontSize = 28.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("TRACEO", color = TextMain, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                            Text("Phone service navigator", color = Muted, fontSize = 12.sp)
                        }
                    }
                }
                item {
                    Column(
                        Modifier.fillMaxWidth().background(SurfaceColor, RoundedCornerShape(24.dp)).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("جست‌وجوی شماره", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("شماره ایرانی را وارد کن تا سرویس‌ها و لینک‌های بازکردن آن‌ها را ببینی.", color = Muted, fontSize = 13.sp)
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it; error = null },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("شماره موبایل") },
                            placeholder = { Text("09123456789") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = { Text("+98", color = Accent, modifier = Modifier.padding(start = 12.dp), fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(16.dp),
                            isError = error != null
                        )
                        if (error != null) Text(error!!, color = Color(0xFFFF8585), fontSize = 12.sp)
                        Button(
                            onClick = {
                                val result = normalizeIranPhone(phone)
                                if (result == null) {
                                    error = "شماره معتبر نیست؛ شماره موبایل ایران را با 09 یا +989 وارد کن."
                                    searched = false
                                    normalized = null
                                } else {
                                    normalized = result
                                    searched = true
                                    error = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text("نمایش سرویس‌ها", fontWeight = FontWeight.Bold) }
                    }
                }
                if (searched && normalized != null) {
                    item {
                        Column(Modifier.fillMaxWidth().background(Color(0xFF17283A), RoundedCornerShape(16.dp)).padding(14.dp)) {
                            Text("شماره استانداردشده", color = Muted, fontSize = 12.sp)
                            Text(normalized!!, color = TextMain, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(6.dp))
                            Text("وجود حساب فقط با روش رسمی و معتبر قابل تأیید است. این نسخه وضعیت حساب را حدس نمی‌زند و شماره‌ای به سرور Traceo نمی‌فرستد.", color = Muted, fontSize = 12.sp)
                        }
                    }
                    item { SectionTitle("پیام‌رسان‌های ایرانی") }
                    items(services.filter { it.category == Category.IRANIAN }) { service ->
                        ServiceCard(service, normalized!!) { url -> openUrl(context, url) }
                    }
                    item { SectionTitle("سرویس‌های بین‌المللی") }
                    items(services.filter { it.category == Category.INTERNATIONAL }) { service ->
                        ServiceCard(service, normalized!!) { url -> openUrl(context, url) }
                    }
                } else {
                    item {
                        Column(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("◈", color = Accent, fontSize = 48.sp)
                            Text("شماره را وارد کن تا شروع کنیم", color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                            Text("حریم خصوصی مهم است؛ هیچ شماره‌ای به سرور ارسال نمی‌شود.", color = Muted, textAlign = TextAlign.Center, fontSize = 12.sp)
                        }
                    }
                }
                item {
                    Text("TRACEO • وضعیت حساب‌ها بدون روش معتبر نامشخص می‌ماند.", color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun ServiceCard(service: Service, phone: String, onOpen: (String) -> Unit) {
    val url = service.openUrl(phone)
    Row(
        modifier = Modifier.fillMaxWidth().background(SurfaceColor, RoundedCornerShape(18.dp)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(42.dp).background(Color(0xFF202B45), CircleShape), contentAlignment = Alignment.Center) {
            Text(service.emoji, fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(service.name, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(service.description, color = Muted, fontSize = 11.sp, lineHeight = 15.sp)
            Text("وضعیت حساب: نامشخص", color = Color(0xFFFFD28A), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
        if (url != null) {
            TextButton(onClick = { onOpen(url) }) { Text("بازکردن", color = Accent, fontWeight = FontWeight.Bold) }
        }
    }
}

private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (_: Exception) {
        // Some apps may not be installed or may not handle the deep link.
    }
}

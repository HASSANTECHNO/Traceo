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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
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
        setContent { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { TraceoApp() } }
    }
}

private enum class Category { IRANIAN, INTERNATIONAL }
private data class Service(
    val name: String,
    val emoji: String,
    val category: Category,
    val description: String,
    val packageName: String,
    val openUrl: (String) -> String? = { null }
)

private val services = listOf(
    Service("بله", "🟢", Category.IRANIAN, "بازکردن برنامه؛ جست‌وجوی شماره داخل خود پیام‌رسان", "ir.nasim"),
    Service("روبیکا", "🟣", Category.IRANIAN, "بازکردن برنامه؛ جست‌وجوی شماره داخل خود پیام‌رسان", "ir.resaneh1.iptv"),
    Service("ایتا", "🟠", Category.IRANIAN, "بازکردن برنامه؛ جست‌وجوی شماره داخل خود پیام‌رسان", "ir.eitaa.messenger"),
    Service("سروش پلاس", "🔵", Category.IRANIAN, "بازکردن برنامه؛ جست‌وجوی شماره داخل خود پیام‌رسان", "mobi.mmdt.ottplus"),
    Service("شاد", "🎓", Category.IRANIAN, "بازکردن برنامه؛ امکان پیدا کردن افراد تابع امکانات شاد است", "ir.medu.shad"),
    Service("آی‌گپ", "💬", Category.IRANIAN, "بازکردن برنامه؛ جست‌وجوی شماره داخل خود پیام‌رسان", "mobi.igap"),
    Service("WhatsApp", "🟢", Category.INTERNATIONAL, "بازکردن گفت‌وگو با شماره؛ وجود حساب را تضمین نمی‌کند", "com.whatsapp") { n -> "https://wa.me/" + n.removePrefix("+") },
    Service("Telegram", "🔹", Category.INTERNATIONAL, "بازکردن تلگرام با شماره؛ ممکن است حریم خصوصی مانع شود", "org.telegram.messenger") { n -> "tg://resolve?phone=" + n.removePrefix("+") },
    Service("Signal", "🔒", Category.INTERNATIONAL, "بازکردن برنامه؛ پیوند شماره ممکن است در دسترس نباشد", "org.thoughtcrime.securesms") { n -> "https://signal.me/#p/" + Uri.encode(n) },
    Service("Viber", "🟣", Category.INTERNATIONAL, "بازکردن گفت‌وگو با شماره در صورت پشتیبانی نسخه نصب‌شده", "com.viber.voip") { n -> "viber://chat?number=" + Uri.encode(n) },
    Service("LINE", "🟩", Category.INTERNATIONAL, "بازکردن برنامه؛ جست‌وجوی شماره تابع تنظیمات LINE است", "jp.naver.line.android"),
    Service("WeChat", "🟢", Category.INTERNATIONAL, "بازکردن برنامه؛ جست‌وجوی شماره داخل WeChat", "com.tencent.mm"),
    Service("Instagram", "📸", Category.INTERNATIONAL, "بازکردن برنامه؛ پیدا کردن حساب با شماره تضمین‌شده نیست", "com.instagram.android"),
    Service("Messenger", "🔵", Category.INTERNATIONAL, "بازکردن برنامه؛ جست‌وجوی مخاطب تابع تنظیمات Meta است", "com.facebook.orca"),
    Service("Snapchat", "👻", Category.INTERNATIONAL, "بازکردن برنامه؛ یافتن حساب با شماره تضمین‌شده نیست", "com.snapchat.android"),
    Service("Discord", "🎮", Category.INTERNATIONAL, "بازکردن برنامه؛ جست‌وجوی شماره تلفن پشتیبانی عمومی ندارد", "com.discord")
)

internal fun normalizeIranPhone(raw: String): String? {
    // Convert Persian/Arabic-Indic numerals to ASCII and ignore separators.
    val digits = raw.mapNotNull { it.digitToIntOrNull()?.digitToChar() }.joinToString("")
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
                            Text("راهنمای پیام‌رسان‌ها", color = Muted, fontSize = 12.sp)
                        }
                    }
                }
                item {
                    Column(
                        Modifier.fillMaxWidth().background(SurfaceColor, RoundedCornerShape(24.dp)).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("جست‌وجوی شماره", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("شماره موبایل را وارد کن و هر پیام‌رسان را مستقیم باز کن.", color = Muted, fontSize = 13.sp)
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
                        ServiceCard(service, normalized!!) { selected -> openService(context, selected, normalized!!) }
                    }
                    item { SectionTitle("سرویس‌های بین‌المللی") }
                    items(services.filter { it.category == Category.INTERNATIONAL }) { service ->
                        ServiceCard(service, normalized!!) { selected -> openService(context, selected, normalized!!) }
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
private fun ServiceCard(service: Service, phone: String, onOpen: (Service) -> Unit) {
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
        TextButton(onClick = { onOpen(service) }) { Text("بازکردن", color = Accent, fontWeight = FontWeight.Bold) }
    }
}

private fun openService(context: Context, service: Service, phone: String) {
    val deepLink = service.openUrl(phone)
    if (deepLink != null) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)))
            return
        } catch (_: Exception) {
            // Fall back to opening the installed app.
        }
    }

    val launchIntent = context.packageManager.getLaunchIntentForPackage(service.packageName)
    if (launchIntent != null) {
        try {
            context.startActivity(launchIntent)
            return
        } catch (_: Exception) {
            // Fall through to the user-facing message below.
        }
    }

    android.widget.Toast.makeText(
        context,
        "برنامه ${service.name} نصب نیست یا قابل بازشدن نیست.",
        android.widget.Toast.LENGTH_LONG
    ).show()
}

package com.hassantechno.traceo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF07111F)
private val SurfaceColor = Color(0xFF111E2D)
private val Accent = Color(0xFF079CFF)
private val Cyan = Color(0xFF00D4FF)
private val Muted = Color(0xFFA5B7CC)
private val TextMain = Color(0xFFF5F8FF)
private val Green = Color(0xFF00C66A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { TraceoApp() } }
    }
}

private enum class Category { IRANIAN, INTERNATIONAL }
private data class Service(
    val name: String, val iconRes: Int, val category: Category, val description: String,
    val packageName: String, val accent: Color = Accent, val openUrl: (String) -> String? = { null }
)

private val services = listOf(
    Service("بله", R.drawable.ic_bale, Category.IRANIAN, "جست‌وجوی مخاطب در پیام‌رسان", "ir.nasim", Color(0xFF16A34A)),
    Service("روبیکا", R.drawable.ic_rubika, Category.IRANIAN, "جست‌وجوی مخاطب در پیام‌رسان", "ir.resaneh1.iptv", Color(0xFF9333EA)),
    Service("ایتا", R.drawable.ic_eitaa, Category.IRANIAN, "جست‌وجوی مخاطب در پیام‌رسان", "ir.eitaa.messenger", Color(0xFFF97316)),
    Service("سروش پلاس", R.drawable.ic_soroush, Category.IRANIAN, "جست‌وجوی مخاطب در پیام‌رسان", "mobi.mmdt.ottplus", Color(0xFF2563EB)),
    Service("شاد", R.drawable.ic_shad, Category.IRANIAN, "جست‌وجوی مخاطب در برنامه", "ir.medu.shad", Color(0xFF0EA5E9)),
    Service("آی‌گپ", R.drawable.ic_igap, Category.IRANIAN, "جست‌وجوی مخاطب در پیام‌رسان", "mobi.igap", Color(0xFF14B8A6)),
    Service("واتساپ", R.drawable.ic_whatsapp, Category.INTERNATIONAL, "بازکردن گفت‌وگو با شماره", "com.whatsapp", Green) { n -> "https://wa.me/" + n.removePrefix("+") },
    Service("تلگرام", R.drawable.ic_telegram, Category.INTERNATIONAL, "بازکردن تلگرام با شماره", "org.telegram.messenger", Color(0xFF24A1DE)) { n -> "tg://resolve?phone=" + n.removePrefix("+") },
    Service("سیگنال", R.drawable.ic_signal, Category.INTERNATIONAL, "بازکردن برنامه سیگنال", "org.thoughtcrime.securesms", Color(0xFF3A76F0)) { n -> "https://signal.me/#p/" + Uri.encode(n) },
    Service("وایبر", R.drawable.ic_viber, Category.INTERNATIONAL, "بازکردن گفت‌وگو با شماره", "com.viber.voip", Color(0xFF7360F2)) { n -> "viber://chat?number=" + Uri.encode(n) },
    Service("لاین", R.drawable.ic_line, Category.INTERNATIONAL, "جست‌وجوی مخاطب در LINE", "jp.naver.line.android", Color(0xFF06C755)),
    Service("وی‌چت", R.drawable.ic_wechat, Category.INTERNATIONAL, "جست‌وجوی مخاطب در WeChat", "com.tencent.mm", Color(0xFF07C160)),
    Service("اینستاگرام", R.drawable.ic_instagram, Category.INTERNATIONAL, "جست‌وجوی حساب در اینستاگرام", "com.instagram.android", Color(0xFFE4405F)),
    Service("مسنجر", R.drawable.ic_messenger, Category.INTERNATIONAL, "جست‌وجوی مخاطب در Messenger", "com.facebook.orca", Color(0xFF0866FF)),
    Service("اسنپ‌چت", R.drawable.ic_snapchat, Category.INTERNATIONAL, "جست‌وجوی مخاطب در Snapchat", "com.snapchat.android", Color(0xFFFFD600)),
    Service("دیسکورد", R.drawable.ic_discord, Category.INTERNATIONAL, "بازکردن برنامه Discord", "com.discord", Color(0xFF5865F2))
)

internal fun normalizeIranPhone(raw: String): String? {
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
    var selected by remember { mutableStateOf<Service?>(null) }
    var tab by remember { mutableIntStateOf(0) }
    val context = androidx.compose.ui.platform.LocalContext.current
    MaterialTheme(colorScheme = darkColorScheme(background = Background, surface = SurfaceColor, primary = Accent, onBackground = TextMain, onSurface = TextMain)) {
        Surface(Modifier.fillMaxSize(), color = Background) {
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF10243A), Background, Color(0xFF060D17))))) {
                if (selected != null) {
                    ServiceDetail(selected!!, normalized, { selected = null }) { openService(context, selected!!, normalized) }
                } else if (tab == 2) {
                    AboutScreen()
                } else if (tab == 1) {
                    ResultsScreen(normalized, onBack = { tab = 0 }) { selected = it }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        LazyColumn(Modifier.weight(1f).fillMaxWidth(), contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 20.dp, bottom = 18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(54.dp).background(Color(0xFF0C263B), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                                        Image(painterResource(R.drawable.ic_traceo_foreground), "لوگوی Traceo", Modifier.size(48.dp))
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text("Traceo", color = TextMain, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
                                        Text("جست‌وجوی شماره در پیام‌رسان‌ها", color = Muted, fontSize = 12.sp)
                                    }
                                    Text("↻", color = TextMain, fontSize = 25.sp, modifier = Modifier.clickable { phone = ""; normalized = null; error = null }.padding(8.dp))
                                }
                            }
                            item {
                                Column(Modifier.fillMaxWidth().background(SurfaceColor.copy(alpha = .97f), RoundedCornerShape(24.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("شماره موبایل", color = TextMain, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text("شماره را وارد کن و پیام‌رسان موردنظرت را باز کن.", color = Muted, fontSize = 12.sp)
                                    OutlinedTextField(value = phone, onValueChange = { phone = it; error = null }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("شماره موبایل ایران") }, placeholder = { Text("09123456789") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), leadingIcon = { Text("+98", color = Cyan, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 10.dp)) }, shape = RoundedCornerShape(15.dp), isError = error != null, colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Cyan, unfocusedBorderColor = Color(0xFF31506A), focusedLabelColor = Cyan, cursorColor = Cyan))
                                    if (error != null) Text(error!!, color = Color(0xFFFF8585), fontSize = 12.sp)
                                    Button(onClick = {
                                        val result = normalizeIranPhone(phone)
                                        if (result == null) { error = "شماره معتبر نیست؛ شماره را با 09 یا +989 وارد کن."; normalized = null }
                                        else { normalized = result; error = null; tab = 1 }
                                    }, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(15.dp), colors = ButtonDefaults.buttonColors(containerColor = Accent)) {
                                        Text("بررسی در پیام‌رسان‌ها  ⌕", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                }
                            }
                            item { SectionTitle("پیام‌رسان‌های پرکاربرد", "انتخاب کن و ادامه بده") }
                            item {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf(services[6], services[7], services[8], services[9]).forEach { service ->
                                        PopularServiceCard(service, Modifier.weight(1f)) { selected = service }
                                    }
                                }
                            }
                            item { SectionTitle("همه پیام‌رسان‌ها", "ایرانی و بین‌المللی") }
                            items(services.chunked(2)) { pair ->
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    pair.forEach { service -> ServiceTile(service, Modifier.weight(1f)) { selected = service } }
                                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                            item { Text("Traceo فقط پیام‌رسان را باز می‌کند؛ وجود حساب به تنظیمات حریم خصوصی و امکانات همان سرویس بستگی دارد.", color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) }
                        }
                        BottomNavigationBar(tab) { tab = it }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, color = TextMain, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Muted, fontSize = 11.sp)
    }
}

@Composable
private fun PopularServiceCard(service: Service, modifier: Modifier, onClick: () -> Unit) {
    Column(modifier.background(SurfaceColor, RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(vertical = 14.dp, horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(44.dp).background(Color(0xFF0A1726), CircleShape), contentAlignment = Alignment.Center) { Image(painterResource(service.iconRes), service.name, Modifier.size(34.dp)) }
        Text(service.name, color = TextMain, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, textAlign = TextAlign.Center, maxLines = 1)
        Text("بازکردن  ›", color = service.accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ServiceTile(service: Service, modifier: Modifier, onClick: () -> Unit) {
    Row(modifier.background(SurfaceColor, RoundedCornerShape(17.dp)).clickable(onClick = onClick).padding(horizontal = 10.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Image(painterResource(service.iconRes), service.name, Modifier.size(34.dp).background(Color(0xFF0A1726), RoundedCornerShape(10.dp)).padding(3.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(service.name, color = TextMain, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text("بازکردن", color = Muted, fontSize = 10.sp)
        }
        Text("‹", color = Muted, fontSize = 22.sp)
    }
}

@Composable
private fun ResultsScreen(phone: String?, onBack: () -> Unit, onSelect: (Service) -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp)) {
        Text("‹ بازگشت", color = Cyan, modifier = Modifier.clickable(onClick = onBack).padding(8.dp))
        Text("نتایج جست‌وجو", color = TextMain, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(if (phone == null) "هنوز شماره‌ای وارد نشده است." else "شماره: $phone", color = Muted, fontSize = 13.sp)
        Spacer(Modifier.height(14.dp))
        Text("برای بررسی، پیام‌رسان را باز کن؛ Traceo وجود حساب را مستقلاً تأیید نمی‌کند.", color = Muted, fontSize = 12.sp)
        Spacer(Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(services) { service -> ServiceTile(service, Modifier.fillMaxWidth()) { onSelect(service) } }
        }
    }
}

@Composable
private fun ServiceDetail(service: Service, phone: String?, onBack: () -> Unit, onOpen: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("‹ بازگشت", color = Cyan, modifier = Modifier.clickable(onClick = onBack).padding(8.dp))
            Spacer(Modifier.weight(1f))
            Text("جزئیات پیام‌رسان", color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(54.dp))
        Box(Modifier.size(112.dp).background(Color(0xFF11283A), CircleShape), contentAlignment = Alignment.Center) { Image(painterResource(service.iconRes), service.name, Modifier.size(76.dp)) }
        Spacer(Modifier.height(22.dp))
        Text(service.name, color = TextMain, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(12.dp))
        Text(service.description, color = Muted, textAlign = TextAlign.Center, fontSize = 14.sp)
        Spacer(Modifier.height(22.dp))
        Column(Modifier.fillMaxWidth().background(SurfaceColor, RoundedCornerShape(18.dp)).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("وضعیت حساب", color = Muted, fontSize = 12.sp)
            Text("از داخل برنامه قابل بررسی است", color = TextMain, fontWeight = FontWeight.Bold)
            Text(if (phone == null) "برای شروع، ابتدا شماره را در صفحه اصلی وارد کن." else "شماره انتخاب‌شده: $phone", color = Muted, fontSize = 12.sp)
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onOpen, modifier = Modifier.fillMaxWidth().height(54.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = service.accent)) {
            Text("بازکردن " + service.name, color = if (service.name == "اسنپ‌چت") Color.Black else Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text("Traceo وجود حساب را تأیید نمی‌کند و شماره را به سرور ارسال نمی‌کند.", color = Muted, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun BottomNavigationBar(tab: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color(0xFF081321)).padding(horizontal = 20.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.CenterVertically) {
        BottomNavItem("⌂", "خانه", tab == 0) { onSelect(0) }
        BottomNavItem("◷", "نتایج", tab == 1) { onSelect(1) }
        BottomNavItem("ⓘ", "درباره", tab == 2) { onSelect(2) }
    }
}

@Composable
private fun BottomNavItem(symbol: String, label: String, active: Boolean, onClick: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(symbol, color = if (active) Cyan else Muted, fontSize = 23.sp)
        Text(label, color = if (active) Cyan else Muted, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
private fun AboutScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Image(painterResource(R.drawable.ic_traceo_foreground), "Traceo", Modifier.size(88.dp))
        Spacer(Modifier.height(18.dp))
        Text("درباره Traceo", color = TextMain, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Text("یک راهنمای ساده برای بازکردن پیام‌رسان‌ها و جست‌وجوی مخاطب. Traceo هیچ شماره‌ای را به سرور ارسال نمی‌کند و نمی‌تواند وجود حساب را مستقلاً تأیید کند.", color = Muted, textAlign = TextAlign.Center, fontSize = 14.sp)
    }
}

private fun openService(context: Context, service: Service, phone: String?) {
    val deepLink = phone?.let { service.openUrl(it) }
    if (deepLink != null) {
        try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(deepLink))); return } catch (_: Exception) { }
    }
    val launchIntent = context.packageManager.getLaunchIntentForPackage(service.packageName)
    if (launchIntent != null) {
        try { context.startActivity(launchIntent); return } catch (_: Exception) { }
    }
    Toast.makeText(context, "برنامه " + service.name + " نصب نیست یا قابل بازشدن نیست.", Toast.LENGTH_LONG).show()
}

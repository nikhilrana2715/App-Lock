package com.applock.protectedapps.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.protectedapps.R
import kotlinx.coroutines.delay

data class AppBrandInfo(
    val id: String,
    val name: String,
    val iconRes: Int
)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appsList = remember {
        listOf(
            AppBrandInfo("whatsapp", "WhatsApp", R.drawable.ic_app_whatsapp),
            AppBrandInfo("instagram", "Instagram", R.drawable.ic_app_instagram),
            AppBrandInfo("facebook", "Facebook", R.drawable.ic_app_facebook),
            AppBrandInfo("snapchat", "Snapchat", R.drawable.ic_app_snapchat),
            AppBrandInfo("gallery", "Gallery", R.drawable.ic_app_gallery),
            AppBrandInfo("photos", "Photos", R.drawable.ic_app_photos),
            AppBrandInfo("gmail", "Gmail", R.drawable.ic_app_gmail),
            AppBrandInfo("camera", "Camera", R.drawable.ic_app_camera),
            AppBrandInfo("amazon", "Amazon", R.drawable.ic_app_amazon),
            AppBrandInfo("flipkart", "Flipkart", R.drawable.ic_app_flipkart),
            AppBrandInfo("myntra", "Myntra", R.drawable.ic_app_myntra),
            AppBrandInfo("youtube", "YouTube", R.drawable.ic_app_youtube),
            AppBrandInfo("spotify", "Spotify", R.drawable.ic_app_spotify),
            AppBrandInfo("netflix", "Netflix", R.drawable.ic_app_netflix),
            AppBrandInfo("maps", "Maps", R.drawable.ic_app_maps),
            AppBrandInfo("telegram", "Telegram", R.drawable.ic_app_telegram),
            AppBrandInfo("phonepe", "PhonePe", R.drawable.ic_app_phonepe),
            AppBrandInfo("swiggy", "Swiggy", R.drawable.ic_app_swiggy),
            AppBrandInfo("zomato", "Zomato", R.drawable.ic_app_zomato),
            AppBrandInfo("settings", "Settings", R.drawable.ic_app_settings)
        )
    }

    var lockedCount by remember { mutableIntStateOf(0) }
    var isUnlocked by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "NeonRotate")
    val neonRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "NeonRotation"
    )

    val scale by animateFloatAsState(
        targetValue = if (isUnlocked) 1.2f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "SplashScale"
    )

    val smileRotation by animateFloatAsState(
        targetValue = if (isUnlocked) 360f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "SmileRotation"
    )

    // 6 Seconds total timing script
    LaunchedEffect(Unit) {
        for (i in 1..20) {
            delay(180)
            lockedCount = i
        }
        delay(400)
        isUnlocked = true
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC)) // Clean Light Porcelain Theme
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Title Area with full vertical breathing room
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
            ) {
                Text(
                    text = "App Lock",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Privacy Protected 😊",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )
            }

            // 20 Real Official App Icons Grid
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(appsList) { index, appInfo ->
                        val isThisAppLocked = index < lockedCount
                        val chipAlpha by animateFloatAsState(
                            targetValue = if (isThisAppLocked) 1.0f else 0.55f,
                            animationSpec = tween(durationMillis = 300),
                            label = "ChipAlpha"
                        )
                        val chipScale by animateFloatAsState(
                            targetValue = if (isThisAppLocked) 1.05f else 0.95f,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "ChipScale"
                        )

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = if (isThisAppLocked) 4.dp else 1.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isThisAppLocked) Color(0xFF4F46E5).copy(alpha = 0.4f) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier
                                .scale(chipScale)
                                .alpha(chipAlpha)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Image(
                                        painter = painterResource(id = appInfo.iconRes),
                                        contentDescription = appInfo.name,
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    )

                                    if (isThisAppLocked) {
                                        Box(
                                            modifier = Modifier
                                                .offset(x = 4.dp, y = (-4).dp)
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Locked Badge",
                                                modifier = Modifier.size(11.dp),
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = appInfo.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // Animated Lock & Smile Emblem Footer
            Box(
                modifier = Modifier
                    .padding(bottom = 12.dp, top = 6.dp)
                    .size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                // Neon Rotating Outer Border
                Canvas(modifier = Modifier.fillMaxSize().rotate(neonRotation)) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                Color(0xFF4F46E5),
                                Color(0xFF7C3AED),
                                Color(0xFFEC4899),
                                Color(0xFF06B6D4),
                                Color(0xFF4F46E5)
                            )
                        ),
                        style = Stroke(width = 4.dp.toPx())
                    )
                }

                // Inner Container
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = if (isUnlocked) listOf(Color(0xFF10B981), Color(0xFF059669))
                                else listOf(Color(0xFF4F46E5), Color(0xFF4338CA))
                            )
                        )
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.rotate(smileRotation)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockOpen,
                                contentDescription = "Unlocked Icon",
                                modifier = Modifier.size(42.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SentimentVerySatisfied,
                                contentDescription = "Smiling Icon",
                                modifier = Modifier.size(32.dp),
                                tint = Color(0xFFFDE047)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked Icon",
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

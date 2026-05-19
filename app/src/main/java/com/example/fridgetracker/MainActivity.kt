package com.example.fridgetracker

import android.Manifest
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

// Data Models
data class KitchenItem(
    val quantity: Double = 0.0,
    val minQuantity: Double = 1.0,
    val unit: String = "",
    val category: String = "",
    val expiryDate: Long? = null,
    val addedDate: Long = System.currentTimeMillis()
)

data class KitchenNote(
    val id: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class PredefinedItem(
    val key: String,
    val unit: String,
    val category: String,
    val nameRes: Int
)

val commonKitchenItems = listOf(
    PredefinedItem("milk", "l", "dairy", R.string.item_milk),
    PredefinedItem("eggs", "pcs", "dairy", R.string.item_eggs),
    PredefinedItem("cheese", "kg", "dairy", R.string.item_cheese),
    PredefinedItem("yogurt", "pcs", "dairy", R.string.item_yogurt),
    PredefinedItem("butter", "kg", "dairy", R.string.item_butter),
    PredefinedItem("bread", "pcs", "bakery", R.string.item_bread),
    PredefinedItem("apples", "kg", "fruits", R.string.item_apples),
    PredefinedItem("bananas", "kg", "fruits", R.string.item_bananas),
    PredefinedItem("oranges", "kg", "fruits", R.string.item_oranges),
    PredefinedItem("potatoes", "kg", "vegetables", R.string.item_potatoes),
    PredefinedItem("onions", "kg", "vegetables", R.string.item_onions),
    PredefinedItem("tomatoes", "kg", "vegetables", R.string.item_tomatoes),
    PredefinedItem("garlic", "pcs", "vegetables", R.string.item_garlic),
    PredefinedItem("ginger", "g", "vegetables", R.string.item_ginger),
    PredefinedItem("carrots", "kg", "vegetables", R.string.item_carrots),
    PredefinedItem("cucumber", "kg", "vegetables", R.string.item_cucumber),
    PredefinedItem("lettuce", "pcs", "vegetables", R.string.item_lettuce),
    PredefinedItem("green_pepper", "pcs", "vegetables", R.string.item_green_pepper),
    PredefinedItem("red_pepper", "pcs", "vegetables", R.string.item_red_pepper),
    PredefinedItem("yellow_pepper", "pcs", "vegetables", R.string.item_yellow_pepper),
    PredefinedItem("chicken", "kg", "chicken", R.string.item_chicken),
    PredefinedItem("beef", "kg", "meat", R.string.item_beef),
    PredefinedItem("fish", "kg", "meat", R.string.item_fish),
    PredefinedItem("rice", "kg", "pantry", R.string.item_rice),
    PredefinedItem("pasta", "packet", "pantry", R.string.item_pasta),
    PredefinedItem("flour", "kg", "pantry", R.string.item_flour),
    PredefinedItem("sugar", "kg", "pantry", R.string.item_sugar),
    PredefinedItem("coffee", "g", "pantry", R.string.item_coffee),
    PredefinedItem("tea", "g", "pantry", R.string.item_tea),
    PredefinedItem("salt", "kg", "pantry", R.string.item_salt),
    PredefinedItem("oil", "l", "pantry", R.string.item_oil),
    PredefinedItem("water", "l", "pantry", R.string.item_water),
    PredefinedItem("juice", "l", "pantry", R.string.item_juice),
    PredefinedItem("honey", "jar", "pantry", R.string.item_honey),
    PredefinedItem("jam", "jar", "pantry", R.string.item_jam),
    PredefinedItem("powdered_milk", "bag", "dairy", R.string.item_powdered_milk),
    PredefinedItem("spaghetti", "packet", "pantry", R.string.item_spaghetti),
    PredefinedItem("ditalini", "packet", "pantry", R.string.item_ditalini),
    PredefinedItem("penne", "packet", "pantry", R.string.item_penne),
    PredefinedItem("vermicelli", "packet", "pantry", R.string.item_vermicelli),
    PredefinedItem("orzo", "packet", "pantry", R.string.item_orzo),
    PredefinedItem("yellow_lentils", "kg", "pantry", R.string.item_yellow_lentils),
    PredefinedItem("oats", "packet", "pantry", R.string.item_oats),
    PredefinedItem("kidney_beans", "kg", "pantry", R.string.item_kidney_beans),
    PredefinedItem("various_fruits", "kg", "fruits", R.string.item_various_fruits),
    PredefinedItem("various_vegetables", "kg", "vegetables", R.string.item_various_vegetables),
    PredefinedItem("cheddar_cheese", "kg", "dairy", R.string.item_cheddar_cheese),
    PredefinedItem("romano_cheese", "kg", "dairy", R.string.item_romano_cheese),
    PredefinedItem("white_cheese", "kg", "dairy", R.string.item_white_cheese),
    PredefinedItem("ghee", "kg", "pantry", R.string.item_ghee),
    PredefinedItem("tomato_paste", "jar", "pantry", R.string.item_tomato_paste),
    PredefinedItem("basmati_rice", "kg", "pantry", R.string.item_basmati_rice),
    PredefinedItem("tuna", "can", "pantry", R.string.item_tuna),
    PredefinedItem("ketchup", "bottle", "pantry", R.string.item_ketchup),
    PredefinedItem("mayonnaise", "jar", "pantry", R.string.item_mayonnaise),
    PredefinedItem("baking_powder", "pcs", "pantry", R.string.item_baking_powder),
    PredefinedItem("bean", "kg", "pantry", R.string.item_bean),
    PredefinedItem("corn_flour", "kg", "pantry", R.string.item_corn_flour),
    PredefinedItem("dates", "kg", "fruits", R.string.item_dates),
    PredefinedItem("elbow_pasta", "packet", "pantry", R.string.item_elbow_pasta),
    PredefinedItem("nescafe", "g", "pantry", R.string.item_nescafe),
    PredefinedItem("vanilla_powder", "pcs", "pantry", R.string.item_vanilla_powder),
    PredefinedItem("yeast", "pcs", "pantry", R.string.item_yeast),
    PredefinedItem("molokhia", "bag", "frozen", R.string.item_molokhia),
    PredefinedItem("peas_with_carrots", "bag", "frozen", R.string.item_peas_with_carrots),
    PredefinedItem("peas", "bag", "frozen", R.string.item_peas),
    PredefinedItem("taro", "bag", "frozen", R.string.item_taro),
    PredefinedItem("green_beans", "bag", "frozen", R.string.item_green_beans),
    PredefinedItem("okra", "bag", "frozen", R.string.item_okra),
    PredefinedItem("spinach", "bag", "frozen", R.string.item_spinach),
    PredefinedItem("tomato_juice", "bottle", "pantry", R.string.item_tomato_juice),
    PredefinedItem("falafel", "pcs", "frozen", R.string.item_falafel),
    PredefinedItem("pumpkin", "kg", "vegetables", R.string.item_pumpkin),
    PredefinedItem("pastrami", "kg", "meat", R.string.item_pastrami),
    PredefinedItem("meat_with_vegetables", "kg", "meat", R.string.item_meat_with_vegetables),
    PredefinedItem("entrecote", "kg", "meat", R.string.item_entrecote),
    PredefinedItem("minced_meat", "kg", "meat", R.string.item_minced_meat),
    PredefinedItem("burger", "pcs", "meat", R.string.item_burger),
    PredefinedItem("kofta", "kg", "meat", R.string.item_kofta),
    PredefinedItem("rice_kofta", "kg", "meat", R.string.item_rice_kofta),
    PredefinedItem("liver", "kg", "meat", R.string.item_liver),
    PredefinedItem("chicken_shawarma", "kg", "chicken", R.string.item_chicken_shawarma),
    PredefinedItem("beef_shawarma", "kg", "meat", R.string.item_beef_shawarma),
    PredefinedItem("sausage", "kg", "meat", R.string.item_sausage),
    PredefinedItem("lamb", "kg", "meat", R.string.item_lamb),
    PredefinedItem("beef_steak", "kg", "meat", R.string.item_beef_steak),
    PredefinedItem("farm_frites", "bag", "frozen", R.string.item_farm_frites),
    PredefinedItem("bread_crumbs", "bag", "pantry", R.string.item_bread_crumbs),
    PredefinedItem("vinegar", "bottle", "pantry", R.string.item_vinegar),
    PredefinedItem("corn_oil", "l", "pantry", R.string.item_corn_oil),
    PredefinedItem("olive_oil", "l", "pantry", R.string.item_olive_oil),
    PredefinedItem("apple_cider_vinegar", "bottle", "pantry", R.string.item_apple_cider_vinegar),
    PredefinedItem("anise", "packet", "pantry", R.string.item_anise),
    PredefinedItem("caraway", "packet", "pantry", R.string.item_caraway),
    PredefinedItem("fennel", "packet", "pantry", R.string.item_fennel),
    PredefinedItem("cloves", "g", "pantry", R.string.item_cloves),
    PredefinedItem("nesquik", "can", "pantry", R.string.item_nesquik),
    PredefinedItem("black_pepper", "g", "pantry", R.string.item_black_pepper),
    PredefinedItem("cumin", "g", "pantry", R.string.item_cumin),
    PredefinedItem("coriander", "g", "pantry", R.string.item_coriander),
    PredefinedItem("garlic_powder", "g", "pantry", R.string.item_garlic_powder),
    PredefinedItem("onion_powder", "g", "pantry", R.string.item_onion_powder),

    // Spice Items
    PredefinedItem("paprika", "g", "spices", R.string.item_paprika),
    PredefinedItem("turmeric", "g", "spices", R.string.item_turmeric),
    PredefinedItem("cinnamon", "g", "spices", R.string.item_cinnamon),
    PredefinedItem("ginger_powder", "g", "spices", R.string.item_ginger_powder),
    PredefinedItem("chili_powder", "g", "spices", R.string.item_chili_powder),
    PredefinedItem("cumin_powder", "g", "spices", R.string.item_cumin_powder),
    PredefinedItem("coriander_powder", "g", "spices", R.string.item_coriander_powder),
    PredefinedItem("black_pepper_powder", "g", "spices", R.string.item_black_pepper_powder),
    PredefinedItem("nutmeg", "g", "spices", R.string.item_nutmeg),
    PredefinedItem("cloves_powder", "g", "spices", R.string.item_cloves_powder),
    PredefinedItem("cardamom", "g", "spices", R.string.item_cardamom),
    PredefinedItem("laurel_leaf", "g", "spices", R.string.item_laurel_leaf),
    PredefinedItem("seven_spices", "g", "spices", R.string.item_seven_spices),
    PredefinedItem("meat_spices", "g", "spices", R.string.item_meat_spices),
    PredefinedItem("chicken_spices", "g", "spices", R.string.item_chicken_spices),
    PredefinedItem("sumac", "g", "spices", R.string.item_sumac),
    PredefinedItem("thyme", "g", "spices", R.string.item_thyme),

    PredefinedItem("dish_soap", "bottle", "cleaners", R.string.item_dish_soap),
    PredefinedItem("all_purpose_cleaner", "bottle", "cleaners", R.string.item_all_purpose_cleaner),
    PredefinedItem("glass_cleaner", "bottle", "cleaners", R.string.item_glass_cleaner),
    PredefinedItem("floor_cleaner", "bottle", "cleaners", R.string.item_floor_cleaner),
    PredefinedItem("laundry_detergent", "bottle", "cleaners", R.string.item_laundry_detergent),
    PredefinedItem("bleach", "bottle", "cleaners", R.string.item_bleach),
    PredefinedItem("sponges", "packet", "cleaners", R.string.item_sponges),
    PredefinedItem("fabric_softener", "bottle", "cleaners", R.string.item_fabric_softener),
    PredefinedItem("dishwasher_tablets", "packet", "cleaners", R.string.item_dishwasher_tablets),
    PredefinedItem("toilet_cleaner", "bottle", "cleaners", R.string.item_toilet_cleaner),
    PredefinedItem("degreaser", "bottle", "cleaners", R.string.item_degreaser),
    PredefinedItem("disinfectant_wipes", "packet", "cleaners", R.string.item_disinfectant_wipes),
    PredefinedItem("aluminum_foil", "roll", "cleaners", R.string.item_aluminum_foil),
    PredefinedItem("parchment_paper", "roll", "cleaners", R.string.item_parchment_paper),
    PredefinedItem("storage_bags", "packet", "cleaners", R.string.item_storage_bags),
    PredefinedItem("soap", "pcs", "cleaners", R.string.item_soap),
    PredefinedItem("dishwasher_detergent", "bottle", "cleaners", R.string.item_dishwasher_detergent),
    PredefinedItem("wood_cleaner", "bottle", "cleaners", R.string.item_wood_cleaner),
    PredefinedItem("carpet_cleaner", "bottle", "cleaners", R.string.item_carpet_cleaner),
    PredefinedItem("soft_scouring_pad", "pcs", "cleaners", R.string.item_soft_scouring_pad),
    PredefinedItem("chicken_panee", "kg", "chicken", R.string.item_chicken_panee)
)

val baseCategories = listOf("fruits", "vegetables", "dairy", "meat", "chicken", "bakery", "frozen", "pantry", "cleaners", "spices")

fun getEnglishItemName(key: String): String {
    return key.replace("_", " ").replaceFirstChar { it.uppercase() }
}

fun getLocalizedItemNameWithArabic(context: Context, key: String, arabicContext: Context? = null): String {
    val englishName = getEnglishItemName(key)
    val predefined = commonKitchenItems.find { it.key == key }
    if (predefined != null && predefined.nameRes != -1) {
        val effectiveArabicContext = arabicContext ?: run {
            val configuration = Configuration(context.resources.configuration)
            configuration.setLocale(Locale.forLanguageTag("ar"))
            context.createConfigurationContext(configuration)
        }
        val arabicName = try { effectiveArabicContext.getString(predefined.nameRes) } catch (_: Exception) { "" }
        return if (arabicName.isNotEmpty()) "$englishName ($arabicName)" else englishName
    }
    return englishName
}

fun getLocalizedUnit(context: Context, unit: String): String {
    val resId = context.resources.getIdentifier("unit_$unit", "string", context.packageName)
    return if (resId != 0) context.getString(resId) else unit
}

fun getLocalizedCategory(context: Context, category: String): String {
    if (category.equals("All", ignoreCase = true)) return "All (الكل)"
    val resId = context.resources.getIdentifier("cat_$category", "string", context.packageName)
    return if (resId != 0) context.getString(resId) else category.replaceFirstChar { it.uppercase() }
}

fun getCategoryIcon(category: String): String {
    return when(category.lowercase()) {
        "fruits" -> "🍎"
        "vegetables" -> "🥦"
        "dairy" -> "🥛"
        "meat" -> "🥩"
        "chicken" -> "🍗"
        "bakery" -> "🍞"
        "frozen" -> "❄️"
        "cleaners" -> "🧼"
        "spices" -> "🧂"
        "pantry" -> "🥫"
        else -> "📦"
    }
}

// Firebase connection checker
fun checkFirebaseConnection(context: Context): Boolean {
    return try {
        val runtime = Runtime.getRuntime()
        val process = runtime.exec(arrayOf("/system/bin/ping", "-c", "1", "8.8.8.8"))
        val exitCode = process.waitFor()
        exitCode == 0
    } catch (e: Exception) {
        Log.w("NetworkCheck", "Could not verify internet", e)
        true // Assume online if we can't check
    }
}

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flameScale"
    )
    
    val panBob by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "panBob"
    )

    LaunchedEffect(Unit) {
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)))),
        contentAlignment = Alignment.Center
    ) {
        KitchenBackground()
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Flame
                Text(
                    text = "🔥",
                    fontSize = 50.sp,
                    modifier = Modifier
                        .offset(y = 45.dp)
                        .graphicsLayer(
                            scaleX = flameScale,
                            scaleY = flameScale,
                            alpha = flameScale.coerceIn(0.6f, 1f)
                        )
                )
                // Pan
                Text(
                    text = "🍳",
                    fontSize = 120.sp,
                    modifier = Modifier.offset(y = panBob.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Kitcheneering",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            
            Text(
                text = "Master Your Inventory",
                fontSize = 14.sp,
                color = Color.Black.copy(alpha = 0.6f),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun IntroPage(features: List<String>, startIndex: Int, onSkip: () -> Unit, onGetStarted: (() -> Unit)? = null) {
    var visibleFeatures by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        for (i in 1..features.size) {
            delay(800)
            visibleFeatures = i
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)))),
        contentAlignment = Alignment.Center
    ) {
        KitchenBackground()
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "✨ Why Kitcheneering? ✨",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                features.forEachIndexed { index, feature ->
                    AnimatedVisibility(
                        visible = visibleFeatures > index,
                        enter = fadeIn() + slideInHorizontally()
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.85f)),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier.width(24.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Text(
                                        text = "${index + startIndex}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = feature,
                                    fontSize = 14.sp,
                                    color = Color.Black,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.offset(x = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (onGetStarted != null) {
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 80.dp, start = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text("Get Started", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        TextButton(
            onClick = onSkip,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 80.dp, end = 24.dp)
        ) {
            Text("Skip >>", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
fun SummaryPage(onStart: () -> Unit) {
    val checklistItems = listOf(
        "✅ Tracks what’s available",
        "✅ Know what’s missing",
        "✅ Save time and reduce food waste"
    )
    
    var visibilityStage by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        delay(500)
        visibilityStage = 1
        for (i in 1..checklistItems.size) {
            delay(800)
            visibilityStage = 1 + i
        }
        delay(800)
        visibilityStage = 5
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)))),
        contentAlignment = Alignment.Center
    ) {
        KitchenBackground()
        
        Column(
            modifier = Modifier.padding(32.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = visibilityStage >= 1,
                enter = fadeIn(animationSpec = tween(1200))
            ) {
                Text(
                    text = "Smart Kitcheneering helps you manage your kitchen easily",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                checklistItems.forEachIndexed { index, item ->
                    AnimatedVisibility(
                        visible = visibilityStage >= (index + 2),
                        enter = fadeIn(tween(600)) + slideInHorizontally(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        Text(
                            text = item,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(64.dp))
            
            AnimatedVisibility(
                visible = visibilityStage >= 5,
                enter = fadeIn(animationSpec = tween(1000)) + scaleIn(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy))
            ) {
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .height(50.dp)
                        .graphicsLayer(
                            scaleX = glowScale,
                            scaleY = glowScale
                        )
                        .shadow(elevation = 12.dp * glowAlpha, shape = RoundedCornerShape(30.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text(" Don't just cook, Engineer your kitchen ✨", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

class MainActivity : ComponentActivity() {
    private val auth = FirebaseAuth.getInstance()
    private var mediaPlayer: MediaPlayer? = null

    companion object {
        fun scheduleReminders(context: Context) {
            val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, ReminderReceiver::class.java)

            val pendingIntent1 = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val pendingIntent2 = PendingIntent.getBroadcast(context, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

            // Cancel existing
            alarmManager.cancel(pendingIntent1)
            alarmManager.cancel(pendingIntent2)

            // Set for 9 AM
            val calendar1 = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar1.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent1)

            // Set for 6 PM
            val calendar2 = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 18)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar2.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent2)

            Log.d("Reminder", "Twice-daily reminders scheduled at 9 AM and 6 PM")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        FirebaseApp.initializeApp(this)
        
        // App Check Configuration
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(DebugAppCheckProviderFactory.getInstance())
            Log.d("AppCheck", "DebugAppCheckProviderFactory installed")
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
            Log.d("AppCheck", "PlayIntegrityAppCheckProviderFactory installed")
        }

        // Enable Firebase Database persistence
        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)
            FirebaseDatabase.getInstance().reference.keepSynced(true)
            Log.d("Firebase", "Persistence enabled successfully")
        } catch (e: Exception) {
            Log.e("Firebase", "Could not enable persistence", e)
        }

        setContent {
            val context = LocalContext.current
            
            // Request notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Log.d("Permission", "Notification permission granted")
                        MainActivity.scheduleReminders(context)
                    } else {
                        Log.w("Permission", "Notification permission denied")
                    }
                }
                
                LaunchedEffect(Unit) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            }

            var introStep by remember { mutableIntStateOf(0) }
            var user by remember { mutableStateOf(auth.currentUser) }
            
            DisposableEffect(Unit) {
                try {
                    mediaPlayer = MediaPlayer.create(context, R.raw.a).apply {
                        isLooping = true
                    }
                } catch (e: Exception) { Log.e("MediaPlayer", "Failed to init", e) }
                onDispose {
                    mediaPlayer?.release()
                    mediaPlayer = null
                }
            }

            LaunchedEffect(introStep) {
                if (introStep in 0..4) {
                    if (mediaPlayer?.isPlaying == false) mediaPlayer?.start()
                } else {
                    if (mediaPlayer?.isPlaying == true) mediaPlayer?.pause()
                }
            }

            val features1 = listOf(
                "Secure cloud-synced inventory 🔒",
                "Real-time Tracking 📊",
                "100+ essentials across 11 categories 📦",
                "Color-coded expiry warnings ⚠️",
                "Smart low-stock alerts & lists 🛒"
            )

            val features2 = listOf(
                " English and Arabic interface 🌍",
                " Interactive onboarding 📖",
                " Recipe Reminder Notes 📝",
                " Twice‑daily inventory reminders 🔔",
                " Step‑by‑step feature guide 💡"
            )

            when (introStep) {
                0 -> SplashScreen(onTimeout = { introStep = 1 })
                1 -> IntroPage(features = features1, startIndex = 1, onSkip = { introStep = 2 }, onGetStarted = { introStep = 5 })
                2 -> IntroPage(features = features2, startIndex = 6, onSkip = { introStep = 3 })
                3 -> SummaryPage(onStart = { introStep = 5 })
                else -> {
                    if (user == null) {
                        AuthScreen(onAuthSuccess = { user = auth.currentUser })
                    } else {
                        MainAppScreen(user!!.uid, onLogout = {
                            auth.signOut()
                            user = null
                        })
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.let { if (it.isPlaying) it.pause() }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

@Composable
fun KitchenBackground() {
    val items = remember { listOf("🍳", "🍎", "🥦", "🥛", "🥩", "🍗", "🍞", "❄️", "🧼", "🧂", "📦", "🥑", "🍔", "🍕", "🍷", "🍰", "🍦", "☕", "🍹") }
    val textMeasurer = rememberTextMeasurer()
    val style = TextStyle(fontSize = 32.sp, color = Color.Black.copy(alpha = 0.22f))
    val itemStyles = remember(items, style) {
        items.map { item -> textMeasurer.measure(item, style = style) }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val spacingPx = 100.dp.toPx()
        val columns = (size.width / spacingPx).toInt() + 1
        val rows = (size.height / spacingPx).toInt() + 1
        
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                val index = r * columns + c
                val itemIndex = index % items.size
                val layoutResult = itemStyles[itemIndex]
                val xOffset = spacingPx * c + (if (r % 2 == 1) spacingPx / 2 else 0f)
                val yOffset = spacingPx * r
                val jitterX = (((r * 31 + c * 71) % 40) - 20).dp.toPx()
                val jitterY = (((r * 43 + c * 17) % 40) - 20).dp.toPx()
                val rotation = ((r * 31 + c * 71) % 360).toFloat()
                
                rotate(rotation, pivot = Offset(xOffset + jitterX + 16.dp.toPx(), yOffset + jitterY + 16.dp.toPx())) {
                    drawText(
                        textLayoutResult = layoutResult,
                        topLeft = Offset(xOffset + jitterX, yOffset + jitterY)
                    )
                }
            }
        }
    }
}

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
    var isLogin by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val auth = FirebaseAuth.getInstance()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9)))),
        contentAlignment = Alignment.Center
    ) {
        KitchenBackground()
        Card(
            modifier = Modifier.padding(24.dp).fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isLogin) stringResource(R.string.title_welcome_back) else stringResource(R.string.title_create_account),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text(stringResource(R.string.label_email)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text(stringResource(R.string.label_password)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                        }
                    },
                    singleLine = true
                )

                if (!isLogin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = null },
                        label = { Text(stringResource(R.string.label_confirm_password)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }

                errorMessage?.let {
                    Text(it, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp), textAlign = TextAlign.Center)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (loading) {
                    CircularProgressIndicator(color = Color(0xFF2E7D32))
                } else {
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Please fill all fields"
                                return@Button
                            }
                            if (!isLogin && password != confirmPassword) {
                                errorMessage = "Passwords do not match"
                                return@Button
                            }
                            loading = true
                            if (isLogin) {
                                auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                    loading = false
                                    if (task.isSuccessful) onAuthSuccess() else errorMessage = task.exception?.message
                                }
                            } else {
                                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                                    loading = false
                                    if (task.isSuccessful) onAuthSuccess() else errorMessage = task.exception?.message
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Text(if (isLogin) stringResource(R.string.btn_log_in) else stringResource(R.string.btn_sign_up))
                    }
                }

                TextButton(onClick = { isLogin = !isLogin; errorMessage = null }) {
                    Text(if (isLogin) stringResource(R.string.btn_no_account) else stringResource(R.string.btn_already_account))
                }
            }
        }
    }
}

@Composable
fun MainAppScreen(userId: String, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    var inventoryItems by remember { mutableStateOf<Map<String, KitchenItem>>(emptyMap()) }

    val context = LocalContext.current
    val database = remember(userId) { FirebaseDatabase.getInstance().reference.child("users").child(userId).child("inventory") }

    LaunchedEffect(userId) {
        MainActivity.scheduleReminders(context)
        
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newItems = mutableMapOf<String, KitchenItem>()
                snapshot.children.forEach { child ->
                    try {
                        val item = child.getValue(KitchenItem::class.java)
                        if (item != null) newItems[child.key!!] = item
                    } catch (e: Exception) { Log.e("Firebase", "Error parsing ${child.key}: ${e.message}") }
                }
                inventoryItems = newItems
            }
            override fun onCancelled(error: DatabaseError) { Log.e("Firebase", "Error: ${error.message}") }
        })
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Inventory, null) },
                    label = { Text("Inventory") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF2E7D32), selectedTextColor = Color(0xFF2E7D32))
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Note, null) },
                    label = { Text("Notes") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF2E7D32), selectedTextColor = Color(0xFF2E7D32))
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Help, null) },
                    label = { Text("Help") },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF2E7D32), selectedTextColor = Color(0xFF2E7D32))
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        Column {
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(Icons.Default.ShoppingCart, null)
                        }
                    },
                    label = { Text("Shopping\nList", textAlign = TextAlign.Center) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Color(0xFF2E7D32), selectedTextColor = Color(0xFF2E7D32))
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> InventoryScreen(
                    userId = userId,
                    onLogout = onLogout,
                    items = inventoryItems,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedCategory = selectedCategory,
                    onSelectedCategoryChange = { selectedCategory = it }
                )
                1 -> KitchenNotesScreen(userId)
                2 -> HelpScreen()
                3 -> ShoppingListScreen(userId, inventoryItems)
            }
        }
    }
}

@Composable
fun InventoryScreen(
    userId: String, 
    onLogout: () -> Unit,
    items: Map<String, KitchenItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onSelectedCategoryChange: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<Pair<String, KitchenItem>?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val arabicContext = remember(context) {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag("ar"))
        context.createConfigurationContext(configuration)
    }
    val database = remember(userId) { FirebaseDatabase.getInstance().reference.child("users").child(userId).child("inventory") }

    val categories = remember(items) {
        val currentCategories = items.values.map { it.category }.filter { it.isNotBlank() }.distinct()
        val custom = currentCategories.filter { it.lowercase() !in baseCategories }
        listOf("All") + baseCategories + custom
    }

    val filteredItems = remember(items, searchQuery, selectedCategory, arabicContext) {
        items.filter { (key, item) ->
            val matchesCategory = selectedCategory == "All" || item.category.lowercase() == selectedCategory.lowercase()
            if (!matchesCategory) return@filter false
            
            if (searchQuery.isBlank()) return@filter true

            val localizedName = getLocalizedItemNameWithArabic(context, key, arabicContext)
            localizedName.contains(searchQuery, ignoreCase = true)
        }.toList()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF2E7D32)).padding(bottom = 12.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Kitcheneering",
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = Color.White, 
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.White) }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search items...", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            cursorColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    
                    Box {
                        Button(
                            onClick = { categoryExpanded = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.9f)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text(
                                text = getLocalizedCategory(context, selectedCategory).split(" (")[0],
                                color = Color(0xFF2E7D32),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF2E7D32))
                        }
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false },
                            modifier = Modifier.background(Color.White).widthIn(min = 150.dp)
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(getLocalizedCategory(context, cat), fontSize = 14.sp) },
                                    onClick = {
                                        onSelectedCategoryChange(cat)
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Color(0xFF2E7D32), contentColor = Color.White) {
                Icon(Icons.Default.Add, "Add Item")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF1F8E9))) {
            KitchenBackground()
            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (items.isEmpty()) "🧊" else "🔍", fontSize = 64.sp)
                        Text(
                            text = if (items.isEmpty()) "Your kitchen is empty!" else "No items in '${selectedCategory}'", 
                            fontSize = 18.sp, 
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), 
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                ) {
                    items(filteredItems.size) { index ->
                        val (key, item) = filteredItems[index]
                        KitchenItemCard(
                            key = key, 
                            item = item, 
                            arabicContext = arabicContext,
                            onEdit = { editingItem = key to item }, 
                            onDelete = { database.child(key).removeValue() }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddItemDialog(
            onDismiss = { showAddDialog = false }, 
            onItemAdded = { key, item ->
                Log.d("Firebase", "Adding item: $key to database")
                database.child(key).setValue(item)
                    .addOnSuccessListener { 
                        Log.d("Firebase", "Successfully added $key")
                        android.widget.Toast.makeText(context, "Added $key", android.widget.Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e -> 
                        Log.e("Firebase", "Failed to add $key", e)
                        android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                showAddDialog = false
            },
            existingCategories = categories.filter { it != "All" }
        )
    }

    editingItem?.let { (key, item) ->
        EditItemDialog(
            itemKey = key, 
            item = item, 
            onDismiss = { editingItem = null }, 
            onItemUpdated = { updatedItem ->
                if (updatedItem != null) database.child(key).setValue(updatedItem)
                editingItem = null
            },
            existingCategories = categories.filter { it != "All" }
        )
    }
}

@Composable
fun KitchenItemCard(key: String, item: KitchenItem, arabicContext: Context? = null, onEdit: () -> Unit, onDelete: () -> Unit) {
    val context = LocalContext.current
    val isExpired = item.expiryDate?.let { it < System.currentTimeMillis() } ?: false
    val isLowStock = isLowStock(key, item)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isExpired -> Color(0xFFFFEBEE).copy(alpha = 0.9f)
                isLowStock -> Color(0xFFFFF3E0).copy(alpha = 0.9f)
                else -> Color.White.copy(alpha = 0.9f)
            }
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).background(Color(0xFFE8F5E9), CircleShape), contentAlignment = Alignment.Center) {
                Text(getCategoryIcon(item.category), fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(getLocalizedItemNameWithArabic(context, key, arabicContext), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text("${item.quantity} ${getLocalizedUnit(context, item.unit)}", fontSize = 11.sp, color = Color.DarkGray)
                Text("Category: ${getLocalizedCategory(context, item.category)}", fontSize = 11.sp, color = Color.Gray)

                if (isLowStock) Text("Low Stock!", fontSize = 10.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                item.expiryDate?.let {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    Text("Expires: ${sdf.format(Date(it))}", fontSize = 10.sp, color = if (isExpired) Color.Red else Color.Gray)
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete", tint = Color.Red.copy(alpha = 0.6f)) }
        }
    }
}

@Composable
fun KitchenNotesScreen(userId: String) {
    var notes by remember { mutableStateOf<List<KitchenNote>>(emptyList()) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<KitchenNote?>(null) }
    var noteText by remember { mutableStateOf("") }
    var isSavingNote by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val database = remember(userId) { FirebaseDatabase.getInstance().reference.child("users").child(userId).child("notes") }

    LaunchedEffect(userId) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newNotes = mutableListOf<KitchenNote>()
                snapshot.children.forEach { child ->
                    child.getValue(KitchenNote::class.java)?.let { newNotes.add(it.copy(id = child.key ?: "")) }
                }
                notes = newNotes.sortedByDescending { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF2E7D32)).padding(16.dp), contentAlignment = Alignment.Center) {
                Text(text = "Kitchen Notes 📝", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddNoteDialog = true }, containerColor = Color(0xFF2196F3), contentColor = Color.White, shape = CircleShape) {
                Icon(Icons.Default.Add, "Add Note")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF1F8E9))) {
            KitchenBackground()
            if (notes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No notes yet! 📝", fontSize = 18.sp, color = Color.Gray)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(notes) { note -> 
                        NoteCard(
                            note = note, 
                            onEdit = { editingNote = note },
                            onDelete = { database.child(note.id).removeValue() }
                        ) 
                    }
                }
            }
        }
    }

    if (showAddNoteDialog) {
        LaunchedEffect(isSavingNote) {
            if (isSavingNote) {
                delay(15000) // 15 second timeout instead of 30
                if (isSavingNote) {
                    Log.w("NoteSave", "Save operation timeout after 15 seconds - Firebase not responding")
                    isSavingNote = false
                    android.widget.Toast.makeText(
                        context,
                        "⏱️ Save timeout (15s)\n\n🔧 Fix:\n" +
                        "1. Check internet\n" +
                        "2. Go to Firebase Console\n" +
                        "3. Realtime Database → Rules\n" +
                        "4. Replace with default rules\n" +
                        "5. Click Publish\n" +
                        "6. Try again",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false; noteText = "" },
            title = { Text("Add New Note", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)) },
            text = { OutlinedTextField(value = noteText, onValueChange = { noteText = it }, placeholder = { Text("Enter your note...") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteText.isNotBlank() && !isSavingNote) {
                            isSavingNote = true
                            Log.d("NoteSave", "🔵 Starting to save note: ${noteText.take(50)}...")

                            // Check authentication
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            Log.d("NoteSave", "👤 Current user: ${currentUser?.uid ?: "NOT AUTHENTICATED"}")

                            if (currentUser == null) {
                                Log.e("NoteSave", "❌ User not authenticated!")
                                isSavingNote = false
                                android.widget.Toast.makeText(context, "❌ Not logged in!\n\nPlease log out and log back in.", android.widget.Toast.LENGTH_LONG).show()
                                return@Button
                            }

                            Log.d("NoteSave", "🔐 Auth OK - User UID: ${currentUser.uid}")
                            Log.d("NoteSave", "📡 Attempting Firebase write...")

                            val ref = database.push()
                            Log.d("NoteSave", "🔑 Database reference created: ${ref.key}")

                            ref.setValue(KitchenNote(id = ref.key ?: "", text = noteText))
                                .addOnSuccessListener {
                                    Log.d("NoteSave", "✅ SUCCESS - Note saved!")
                                    noteText = ""
                                    showAddNoteDialog = false
                                    isSavingNote = false
                                    android.widget.Toast.makeText(context, "✅ Note added", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("NoteSave", "❌ FAILED - ${e.message}", e)
                                    isSavingNote = false

                                    val errorMsg = when {
                                        e.message?.contains("PERMISSION_DENIED") == true -> {
                                            Log.e("NoteSave", "🔒 PERMISSION_DENIED - Firebase Rules not updated!")
                                            "🔒 PERMISSION DENIED\n\n" +
                                            "Firebase Rules NOT updated!\n\n" +
                                            "📝 Read: FIREBASE_RULES_UPDATE_NOW.md\n" +
                                            "Then rebuild and try again"
                                        }
                                        e.message?.contains("Invalid") == true -> "⚠️ Authentication Error\n\nFix: Log in again"
                                        e.message?.contains("disconnect") == true -> "🌐 No connection\n\nCheck internet"
                                        else -> "❌ Error: ${e.message ?: "Unknown"}"
                                    }

                                    android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_LONG).show()
                                }
                        }
                    },
                    enabled = !isSavingNote && noteText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                        disabledContainerColor = Color(0xFF2E7D32).copy(alpha = 0.6f)
                    )
                ) {
                    if (isSavingNote) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save")
                    }
                }
            },
            dismissButton = { TextButton(onClick = { showAddNoteDialog = false; noteText = "" }) { Text("Cancel", color = Color.Gray) } }
        )
    }

    editingNote?.let { note ->
        var editText by remember { mutableStateOf(note.text) }
        var isUpdatingNote by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { editingNote = null },
            title = { Text("Edit Note", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20)) },
            text = { 
                OutlinedTextField(
                    value = editText, 
                    onValueChange = { editText = it }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp)
                ) 
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editText.isNotBlank() && !isUpdatingNote) {
                            isUpdatingNote = true
                            database.child(note.id).child("text").setValue(editText)
                                .addOnSuccessListener {
                                    editingNote = null
                                    isUpdatingNote = false
                                    android.widget.Toast.makeText(context, "✅ Note updated", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    isUpdatingNote = false
                                    android.widget.Toast.makeText(context, "❌ Update failed", android.widget.Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    enabled = !isUpdatingNote && editText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    if (isUpdatingNote) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Update")
                    }
                }
            },
            dismissButton = { TextButton(onClick = { editingNote = null }) { Text("Cancel", color = Color.Gray) } }
        )
    }
}

@Composable
fun NoteCard(note: KitchenNote, onEdit: () -> Unit, onDelete: () -> Unit) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() }, 
        shape = RoundedCornerShape(12.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)), 
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = note.text, fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = sdf.format(Date(note.timestamp)), fontSize = 10.sp, color = Color.Gray)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Edit Note", tint = Color(0xFF2E7D32).copy(alpha = 0.7f)) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Delete Note", tint = Color.Red.copy(alpha = 0.7f)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(onDismiss: () -> Unit, onItemAdded: (String, KitchenItem) -> Unit, existingCategories: List<String>) {
    val context = LocalContext.current
    val arabicContext = remember(context) {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag("ar"))
        context.createConfigurationContext(configuration)
    }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPredefined by remember { mutableStateOf<PredefinedItem?>(null) }
    var isCustomItemMode by remember { mutableStateOf(false) }

    var customItemName by remember { mutableStateOf("") }
    var quantityStr by remember { mutableStateOf("1") }
    // Removed minQuantityStr state, as 'Min' field is no longer needed
    var unit by remember { mutableStateOf("pcs") }
    var category by remember { mutableStateOf("pantry") }
    var expiryDate by remember { mutableStateOf<Long?>(null) }

    if (selectedPredefined != null || isCustomItemMode) {
        val itemName = if (isCustomItemMode) customItemName else getLocalizedItemNameWithArabic(context, selectedPredefined!!.key, arabicContext)
        if (selectedPredefined != null && !isCustomItemMode) {
            unit = selectedPredefined!!.unit
            category = selectedPredefined!!.category
        }

        AlertDialog(
            onDismissRequest = { selectedPredefined = null; isCustomItemMode = false },
            title = { Text("Add $itemName", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("Qty") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val current = quantityStr.replace(',', '.').toDoubleOrNull() ?: 0.0
                                    val newVal = (current - 1.0).coerceAtLeast(0.0)
                                    quantityStr = if (newVal == newVal.toLong().toDouble()) newVal.toLong().toString() else newVal.toString()
                                },
                                modifier = Modifier.size(40.dp).background(Color(0xFFB71C1C), RoundedCornerShape(8.dp))
                            ) { Icon(Icons.Default.Remove, null, tint = Color.White) }
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            IconButton(
                                onClick = {
                                    val current = quantityStr.replace(',', '.').toDoubleOrNull() ?: 0.0
                                    val newVal = current + 1.0
                                    quantityStr = if (newVal == newVal.toLong().toDouble()) newVal.toLong().toString() else newVal.toString()
                                },
                                modifier = Modifier.size(40.dp).background(Color(0xFF003300), RoundedCornerShape(8.dp))
                            ) { Icon(Icons.Default.Add, null, tint = Color.White) }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    var unitExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                        OutlinedTextField(value = getLocalizedUnit(context, unit), onValueChange = {}, readOnly = true, label = { Text("Unit") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                            listOf("pcs", "kg", "g", "ml", "l", "packet", "jar", "plate", "box", "bottle", "bag", "carton", "dozen", "can", "roll").forEach { u ->
                                DropdownMenuItem(text = { Text(getLocalizedUnit(context, u)) }, onClick = { unit = u; unitExpanded = false })
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    var catExpanded by remember { mutableStateOf(false) }
                    var isCustomCategory by remember { mutableStateOf(false) }
                    
                    if (!isCustomCategory) {
                        ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                            OutlinedTextField(value = getLocalizedCategory(context, category), onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                            ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                                existingCategories.forEach { cat ->
                                    DropdownMenuItem(text = { Text(getLocalizedCategory(context, cat)) }, onClick = { category = cat; catExpanded = false })
                                }
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("➕ Add New Custom Category...", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) },
                                    onClick = { isCustomCategory = true; category = ""; catExpanded = false }
                                )
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Custom Category Name") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = { isCustomCategory = false; category = "pantry" }) {
                                    Icon(Icons.Default.Close, "Cancel")
                                }
                            },
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val calendar = Calendar.getInstance()
                            DatePickerDialog(context, { _, y, m, d ->
                                val newDate = Calendar.getInstance()
                                newDate.set(y, m, d)
                                expiryDate = newDate.timeInMillis
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                        }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.1f))
                    ) {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        Text(if (expiryDate == null) "Set Expiry Date 📅" else "Expires: ${sdf.format(Date(expiryDate!!))}", color = Color.Black)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val key = if (isCustomItemMode) customItemName.lowercase().replace(" ", "_") else selectedPredefined!!.key
                    onItemAdded(key, KitchenItem(quantityStr.replace(',', '.').toDoubleOrNull() ?: 1.0, 1.0, unit, category, expiryDate)) // Always use 1.0 as minQuantity
                    isCustomItemMode = false; selectedPredefined = null
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { selectedPredefined = null; isCustomItemMode = false }) { Text("Back") } }
        )
    } else {
        val configuration = remember(context) { Configuration(context.resources.configuration).apply { setLocale(Locale.forLanguageTag("ar")) } }
        val arabicContext = remember(context, configuration) { context.createConfigurationContext(configuration) }

        val filteredPredefined = commonKitchenItems.filter { predefined ->
            val englishMatch = predefined.key.contains(searchQuery, ignoreCase = true) ||
                    (predefined.nameRes != -1 && context.getString(predefined.nameRes).contains(searchQuery, ignoreCase = true))
            val arabicMatch = if (predefined.nameRes != -1) try { arabicContext.getString(predefined.nameRes).contains(searchQuery, ignoreCase = true) } catch(_: Exception) { false } else false
            englishMatch || arabicMatch
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Add Item", fontSize = 15.sp) },
            text = {
                Column {
                    OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, label = { Text("Search or type new item") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), leadingIcon = { Icon(Icons.Default.Search, null) }, singleLine = true)
                    if (searchQuery.isNotBlank() && filteredPredefined.none { it.key.equals(searchQuery.replace(" ","_"), true) }) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { customItemName = searchQuery; isCustomItemMode = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))) { Text("Add \"$searchQuery\" as new item") }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(filteredPredefined) { predefined ->
                            ListItem(
                                headlineContent = { Text(getLocalizedItemNameWithArabic(context, predefined.key, arabicContext), fontWeight = FontWeight.SemiBold, fontSize = 14.sp) },
                                supportingContent = { Text("${getLocalizedCategory(context, predefined.category)} • ${predefined.unit}", fontSize = 12.sp) },
                                leadingContent = { Text(getCategoryIcon(predefined.category), fontSize = 24.sp) },
                                modifier = Modifier.clickable { selectedPredefined = predefined }
                            )
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemDialog(itemKey: String, item: KitchenItem, onDismiss: () -> Unit, onItemUpdated: (KitchenItem?) -> Unit, existingCategories: List<String>) {
    val context = LocalContext.current
    val arabicContext = remember(context) {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag("ar"))
        context.createConfigurationContext(configuration)
    }
    var quantityStr by remember { 
        val q = item.quantity
        mutableStateOf(if (q == q.toLong().toDouble()) q.toLong().toString() else q.toString())
    }
    var unit by remember { mutableStateOf(item.unit) }
    var category by remember { mutableStateOf(item.category) }
    var expiryDate by remember { mutableStateOf(item.expiryDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit ${getLocalizedItemNameWithArabic(context, itemKey, arabicContext)}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val current = quantityStr.replace(',', '.').toDoubleOrNull() ?: 0.0
                                val newVal = (current - 1.0).coerceAtLeast(0.0)
                                quantityStr = if (newVal == newVal.toLong().toDouble()) newVal.toLong().toString() else newVal.toString()
                            },
                            modifier = Modifier.size(40.dp).background(Color(0xFFB71C1C), RoundedCornerShape(8.dp))
                        ) { Icon(Icons.Default.Remove, null, tint = Color.White) }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        IconButton(
                            onClick = {
                                val current = quantityStr.replace(',', '.').toDoubleOrNull() ?: 0.0
                                val newVal = current + 1.0
                                quantityStr = if (newVal == newVal.toLong().toDouble()) newVal.toLong().toString() else newVal.toString()
                            },
                            modifier = Modifier.size(40.dp).background(Color(0xFF003300), RoundedCornerShape(8.dp))
                        ) { Icon(Icons.Default.Add, null, tint = Color.White) }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                var unitExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = unitExpanded, onExpandedChange = { unitExpanded = it }) {
                    OutlinedTextField(value = getLocalizedUnit(context, unit), onValueChange = {}, readOnly = true, label = { Text("Unit") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(unitExpanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = unitExpanded, onDismissRequest = { unitExpanded = false }) {
                        listOf("pcs", "kg", "g", "ml", "l", "packet", "jar", "plate", "box", "bottle", "bag", "carton", "dozen", "can", "roll").forEach { u ->
                            DropdownMenuItem(text = { Text(getLocalizedUnit(context, u)) }, onClick = { unit = u; unitExpanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                var catExpanded by remember { mutableStateOf(false) }
                var isCustomCategory by remember { mutableStateOf(false) }

                if (!isCustomCategory) {
                    ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
                        OutlinedTextField(value = getLocalizedCategory(context, category), onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(catExpanded) }, modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                        ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                            existingCategories.forEach { cat ->
                                DropdownMenuItem(text = { Text(getLocalizedCategory(context, cat)) }, onClick = { category = cat; catExpanded = false })
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("➕ Add New Custom Category...", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) },
                                onClick = { isCustomCategory = true; category = ""; catExpanded = false }
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Custom Category Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { isCustomCategory = false; category = "pantry" }) {
                                Icon(Icons.Default.Close, "Cancel")
                            }
                        },
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        expiryDate?.let { calendar.timeInMillis = it }
                        DatePickerDialog(context, { _, y, m, d ->
                            val newDate = Calendar.getInstance()
                            newDate.set(y, m, d)
                            expiryDate = newDate.timeInMillis                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray.copy(alpha = 0.1f))
                ) {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    Text(if (expiryDate == null) "Set Expiry Date 📅" else "Expires: ${sdf.format(Date(expiryDate!!))}", color = Color.Black)
                }
            }
        },
        confirmButton = { Button(onClick = { onItemUpdated(item.copy(quantity = quantityStr.replace(',', '.').toDoubleOrNull() ?: 1.0, minQuantity = item.minQuantity, unit = unit, category = category, expiryDate = expiryDate)) }) { Text("Update") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun HelpScreen() {
    val helpSections = listOf(
        "Adding Items ➕" to listOf(
            "Tap green \"+\" bottom right",
            "Search item name",
            "Set quantity, unit, category, expiry",
            "Tap Add"
        ),
        "Updating Items ✏️" to listOf(
            "Find item in list",
            "Tap item card",
            "Adjust with green \"+\" / red \"–\" or type quantity",
            "Tap Update"
        ),
        "Removing Items 🗑️" to listOf(
            "Tap red Delete bottom on the right next to the item",
            "Item removed permanently"
        ),
        "Kitchen Notes 📝" to listOf(
            "Tap Note icon top bar",
            "Add text via Add Note",
            "Date/time auto‑stamped",
            "Delete if not needed"
        ),
        "Shopping List & Low Stock 🛒" to listOf(
            "Items <4 pcs/packets, <0.6 L, <400 g (<100g for spices/tea/coffee) auto‑added",
            "Tap cart icon to view",
            "Orange border = low stock"
        ),
        "Searching & Filtering 🔍" to listOf(
            "Use search bar for names",
            "Use filter icon for categories (Fruits, Dairy, Frozen, ..)"
        ),
        "Expiry Alerts ⚠️" to listOf(
            "Red border = expired"
        )
    )

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF2E7D32)).padding(16.dp), contentAlignment = Alignment.Center) {
                Text(text = "✅ Help & Tips Quick Checklist", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF1F8E9))) {
            KitchenBackground()
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(helpSections.size) { index ->
                    val (title, points) = helpSections[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            points.forEach { point ->
                                Text(
                                    text = "• $point",
                                    fontSize = 12.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun isLowStock(key: String, item: KitchenItem): Boolean {
    val k = key.lowercase()
    if ((k == "tea" || k == "coffee" || item.category.lowercase() == "spices") && item.unit == "g") {
        return item.quantity < 100
    }

    return when (item.unit) {
        "l" -> item.quantity < 0.6
        "g" -> item.quantity < 400
        "kg" -> item.quantity < 0.4
        "pcs", "packet" -> item.quantity < 4
        "plate", "jar", "bag" -> item.quantity < 1
        else -> item.quantity <= item.minQuantity
    }
}

@Composable
fun ShoppingListScreen(userId: String, items: Map<String, KitchenItem>) {
    val context = LocalContext.current
    val arabicContext = remember(context) {
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(Locale.forLanguageTag("ar"))
        context.createConfigurationContext(configuration)
    }

    val lowStockItems = remember(items) {
        items.filter { (key, item) -> isLowStock(key, item) }.toList()
    }

    Scaffold(
        topBar = {
            Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF2E7D32)).padding(16.dp), contentAlignment = Alignment.Center) {
                Text(text = "Shopping List 🛒", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF1F8E9))) {
            KitchenBackground()
            if (lowStockItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 64.sp)
                        Text(
                            text = "All stocked up!",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
                ) {
                    items(lowStockItems.size) { index ->
                        val (key, item) = lowStockItems[index]
                        KitchenItemCard(
                            key = key,
                            item = item,
                            arabicContext = arabicContext,
                            onEdit = { /* No edit in shopping list */ },
                            onDelete = { /* No delete in shopping list */ }
                        )
                    }
                }
            }
        }
    }
}


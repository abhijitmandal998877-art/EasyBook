package com.example.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import android.util.Log
import com.example.data.database.CustomerEntity
import com.example.data.database.TransactionEntity
import com.example.ui.viewmodel.EasyBookViewModel
import com.example.utils.NotificationHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider

// Modern dynamic color system
data class AppColors(
    val dark: Color,
    val card: Color,
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val text: Color,
    val textDim: Color,
    val debtRed: Color,
    val creditGreen: Color,
    val surfaceVariant: Color
)

val LocalAppColors = staticCompositionLocalOf<AppColors> {
    error("No AppColors provided")
}

@Composable
fun getAppColors(isDark: Boolean): AppColors {
    return if (isDark) {
        AppColors(
            dark = Color(0xFF0F0E17),
            card = Color(0xFF1F1D2F),
            primary = Color(0xFFFF8906),
            secondary = Color(0xFFE53170),
            accent = Color(0xFF3DA9FC),
            text = Color(0xFFFFFFFE),
            textDim = Color(0xFFA7A9BE),
            debtRed = Color(0xFFFF4E4E),
            creditGreen = Color(0xFF00E676),
            surfaceVariant = Color(0xFF2E2B42)
        )
    } else {
        AppColors(
            dark = Color(0xFFF3F4F6), // clean light-gray background
            card = Color(0xFFFFFFFF), // crisp white card
            primary = Color(0xFFD97706), // rich dark orange for beautiful light mode contrast
            secondary = Color(0xFFDB2777), // beautiful vibrant pink
            accent = Color(0xFF2563EB), // vibrant deep blue
            text = Color(0xFF111827), // high contrast charcoal text
            textDim = Color(0xFF4B5563), // readable secondary gray
            debtRed = Color(0xFFDC2626), // professional crimson red
            creditGreen = Color(0xFF16A34A), // beautiful green
            surfaceVariant = Color(0xFFE5E7EB)
        )
    }
}

val CosmicDark: Color @Composable get() = LocalAppColors.current.dark
val CosmicCard: Color @Composable get() = LocalAppColors.current.card
val CosmicPrimary: Color @Composable get() = LocalAppColors.current.primary
val CosmicSecondary: Color @Composable get() = LocalAppColors.current.secondary
val CosmicAccent: Color @Composable get() = LocalAppColors.current.accent
val CosmicText: Color @Composable get() = LocalAppColors.current.text
val CosmicTextDim: Color @Composable get() = LocalAppColors.current.textDim
val DebtRed: Color @Composable get() = LocalAppColors.current.debtRed
val CreditGreen: Color @Composable get() = LocalAppColors.current.creditGreen

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EasyBookApp(viewModel: EasyBookViewModel) {
    val isDark by viewModel.isDarkMode.collectAsState()
    CompositionLocalProvider(LocalAppColors provides getAppColors(isDark)) {
        val context = LocalContext.current
        val currentScreen by viewModel.currentScreen.collectAsState()

        // Request Notification permission on startup
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    Log.d("EasyBookApp", "Notification permission granted.")
                } else {
                    Log.w("EasyBookApp", "Notification permission denied.")
                }
            }
        )

        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            NotificationHelper.createNotificationChannel(context)
        }

        val secondaryColor = CosmicSecondary
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(CosmicDark)
        ) {
        // Gradient decorative background circle
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(secondaryColor.copy(alpha = 0.15f), Color.Transparent),
                    center = center,
                    radius = size.width
                ),
                radius = size.width,
                center = center
            )
        }

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                (slideInVertically(initialOffsetY = { 1000 }) + fadeIn(animationSpec = spring()))
                    .with(slideOutVertically(targetOffsetY = { -1000 }) + fadeOut())
            },
            label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                "LOGIN" -> LoginScreen(viewModel)
                "SIGNUP" -> SignUpScreen(viewModel)
                "DASHBOARD" -> DashboardScreen(viewModel)
                "CUSTOMER_DETAIL" -> CustomerDetailScreen(viewModel)
                "SETTINGS" -> SettingsScreen(viewModel)
            }
        }
    }
}
}

// --- LOGIN SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: EasyBookViewModel) {
    val context = LocalContext.current
    val email by viewModel.loginEmail.collectAsState()
    val password by viewModel.loginPassword.collectAsState()
    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmailInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EasyBook",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CosmicPrimary,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.SansSerif
        )
        Text(
            text = "Digital Shopkeeper Ledger",
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            color = CosmicTextDim,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicCard),
            elevation = CardDefaults.cardElevation(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = viewModel.t("login"),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicText,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.loginEmail.value = it.lowercase() },
                    label = { Text(viewModel.t("email"), color = CosmicTextDim) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicText,
                        unfocusedTextColor = CosmicText,
                        focusedBorderColor = CosmicPrimary,
                        unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("login_email_input")
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.loginPassword.value = it },
                    label = { Text(viewModel.t("password"), color = CosmicTextDim) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else
                            Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password", tint = CosmicPrimary)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicText,
                        unfocusedTextColor = CosmicText,
                        focusedBorderColor = CosmicPrimary,
                        unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("login_password_input")
                )

                if (isLoading) {
                    CircularProgressIndicator(color = CosmicPrimary)
                } else {
                    Button(
                        onClick = {
                            viewModel.triggerFeedback(context)
                            isLoading = true
                            viewModel.login(context) { success, msg ->
                                isLoading = false
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_button")
                    ) {
                        Text(viewModel.t("login"), color = CosmicDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = viewModel.t("forgot_password"),
                    color = CosmicAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable {
                            viewModel.triggerFeedback(context)
                            showForgotDialog = true
                        }
                        .padding(8.dp)
                        .testTag("forgot_password_link")
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", color = CosmicTextDim)
            Text(
                text = "Register",
                color = CosmicSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable {
                        viewModel.triggerFeedback(context)
                        viewModel.currentScreen.value = "SIGNUP"
                    }
                    .padding(8.dp)
                    .testTag("register_link")
            )
        }

    }

    if (showForgotDialog) {
        // Pre-fill input with email from the main screen if available
        LaunchedEffect(showForgotDialog) {
            if (forgotEmailInput.isEmpty() && email.isNotEmpty()) {
                forgotEmailInput = email
            }
        }

        Dialog(onDismissRequest = { showForgotDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Forgot Password",
                        tint = CosmicAccent,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Forgot Password",
                        color = CosmicText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Enter your registered email address to receive password reset instructions.",
                        color = CosmicTextDim,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = forgotEmailInput,
                        onValueChange = { forgotEmailInput = it },
                        label = { Text("Registered Email Address", color = CosmicTextDim) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicAccent,
                            unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("forgot_email_input")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.triggerFeedback(context)
                                showForgotDialog = false
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicTextDim),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(viewModel.t("cancel"))
                        }

                        Button(
                            onClick = {
                                viewModel.triggerFeedback(context)
                                viewModel.forgotPassword(forgotEmailInput) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        showForgotDialog = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent)
                        ) {
                            Text(viewModel.t("confirm"), color = CosmicText)
                        }
                    }
                }
            }
        }
    }
}

// --- REGISTER SCREEN ---
@Composable
fun SignUpScreen(viewModel: EasyBookViewModel) {
    val context = LocalContext.current
    val name by viewModel.regName.collectAsState()
    val shopName by viewModel.regShopName.collectAsState()
    val email by viewModel.regEmail.collectAsState()
    val whatsapp by viewModel.regWhatsApp.collectAsState()
    val password by viewModel.regPassword.collectAsState()
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.currentScreen.value = "LOGIN"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "EasyBook Registration",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = CosmicPrimary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CosmicCard),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "Create Merchant Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = CosmicText,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { viewModel.regName.value = it.uppercase() },
                        label = { Text(viewModel.t("owner_name"), color = CosmicTextDim) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { viewModel.regShopName.value = it.uppercase() },
                        label = { Text(viewModel.t("shop_name"), color = CosmicTextDim) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { viewModel.regEmail.value = it.lowercase() },
                        label = { Text(viewModel.t("email"), color = CosmicTextDim) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { viewModel.regWhatsApp.value = it.uppercase() },
                        label = { Text(viewModel.t("whatsapp"), color = CosmicTextDim) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { viewModel.regPassword.value = it },
                        label = { Text(viewModel.t("password"), color = CosmicTextDim) },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible)
                                Icons.Filled.Visibility
                            else
                                Icons.Filled.VisibilityOff

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password", tint = CosmicPrimary)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )
                }

                item {
                    if (isLoading) {
                        CircularProgressIndicator(color = CosmicPrimary)
                    } else {
                        Button(
                            onClick = {
                                viewModel.triggerFeedback(context)
                                isLoading = true
                                viewModel.signUp(context) { success, msg ->
                                    isLoading = false
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(viewModel.t("register"), color = CosmicDark, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already registered? ", color = CosmicTextDim)
            Text(
                text = "Login",
                color = CosmicAccent,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable {
                        viewModel.triggerFeedback(context)
                        viewModel.currentScreen.value = "LOGIN"
                    }
                    .padding(8.dp)
            )
        }
    }
}

// --- DASHBOARD SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: EasyBookViewModel) {
    val context = LocalContext.current
    val owner by viewModel.currentOwner.collectAsState()
    val totalRemaining by viewModel.totalRemainingAmount.collectAsState()
    val customersList by viewModel.customers.collectAsState()
    val balances by viewModel.customerBalances.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val currency by viewModel.appCurrency.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    // Date filters State
    val filterStart by viewModel.filterStartDate.collectAsState()
    val filterEnd by viewModel.filterEndDate.collectAsState()

    // Currency Symbol Helper
    val currencySymbol = when (currency) {
        "INR" -> "₹"
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "₹"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = owner?.shopName ?: "EASYBOOK",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CosmicPrimary
                        )
                        Text(
                            text = "Merchant: ${owner?.name ?: ""}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            color = CosmicTextDim
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.triggerFeedback(context)
                        viewModel.currentScreen.value = "SETTINGS"
                    }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = CosmicAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDark)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.triggerFeedback(context)
                    showAddDialog = true
                },
                containerColor = CosmicPrimary,
                contentColor = CosmicDark,
                shape = CircleShape,
                modifier = Modifier.testTag("add_customer_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
        },
        containerColor = CosmicDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Dashboard Balance Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = viewModel.t("total_remaining"),
                        color = CosmicTextDim,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${String.format("%.2f", totalRemaining)}",
                        color = if (totalRemaining > 0) DebtRed else CreditGreen,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Collapsible Date Filter Trigger & Period Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CosmicDark.copy(alpha = 0.5f))
                            .clickable {
                                viewModel.triggerFeedback(context)
                                showDateRangePicker(context, viewModel)
                            }
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendar",
                            tint = CosmicAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val label = if (filterStart == 0L) {
                            "All Time Period (Tap to filter)"
                        } else {
                            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                            "Period: ${sdf.format(Date(filterStart))} to ${sdf.format(Date(filterEnd))}"
                        }
                        Text(
                            text = label,
                            color = CosmicText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (filterStart != 0L) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Clear Filter",
                            color = CosmicSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    viewModel.triggerFeedback(context)
                                    viewModel.clearDateFilter()
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Search bar
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text(viewModel.t("search_placeholder"), color = CosmicTextDim, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = CosmicTextDim) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CosmicText,
                    unfocusedTextColor = CosmicText,
                    focusedBorderColor = CosmicAccent,
                    unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.3f),
                    focusedContainerColor = CosmicCard,
                    unfocusedContainerColor = CosmicCard
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("search_bar")
            )

            // Customer List Title
            Text(
                text = "Customers ledger logs:",
                color = CosmicText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Filtering customer list
            val filteredCustomers = customersList.filter { customer ->
                customer.name.contains(query, ignoreCase = true) ||
                        customer.mobile.contains(query) ||
                        customer.uniqueId.contains(query, ignoreCase = true)
            }

            if (filteredCustomers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.t("no_customers"),
                        color = CosmicTextDim,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(filteredCustomers) { customer ->
                        val balanceOwed = balances[customer.uniqueId] ?: 0.0
                        CustomerListItem(
                            customer = customer,
                            balanceOwed = balanceOwed,
                            currencySymbol = currencySymbol,
                            onClick = {
                                viewModel.triggerFeedback(context)
                                viewModel.selectCustomer(customer)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomerDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }
}

// Custom Date Filter Selection Helper
fun showDateRangePicker(context: Context, viewModel: EasyBookViewModel) {
    val calendar = Calendar.getInstance()
    // Select Start Date
    DatePickerDialog(
        context,
        { _, sYear, sMonth, sDay ->
            val startCal = Calendar.getInstance().apply {
                set(sYear, sMonth, sDay, 0, 0, 0)
            }
            // Select End Date
            DatePickerDialog(
                context,
                { _, eYear, eMonth, eDay ->
                    val endCal = Calendar.getInstance().apply {
                        set(eYear, eMonth, eDay, 23, 59, 59)
                    }
                    viewModel.setDateFilter(startCal.timeInMillis, endCal.timeInMillis)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                setTitle("Select To Date")
                show()
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        setTitle("Select From Date")
        show()
    }
}

// --- CUSTOMER LIST ITEM ---
@Composable
fun CustomerListItem(
    customer: CustomerEntity,
    balanceOwed: Double,
    currencySymbol: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicCard),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("customer_item_${customer.uniqueId}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initial circle Avatar
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(CosmicAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = customer.name.firstOrNull()?.toString() ?: "C",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CosmicAccent
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = customer.name,
                    color = CosmicText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Phone",
                        tint = CosmicTextDim,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = customer.mobile,
                        color = CosmicTextDim,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "ID: ${customer.uniqueId}",
                    color = CosmicAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Balance indicator
            Column(
                horizontalAlignment = Alignment.End
            ) {
                if (balanceOwed > 0) {
                    Text(
                        text = "$currencySymbol${String.format("%.2f", balanceOwed)}",
                        color = DebtRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Owed Due",
                        color = DebtRed.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                } else {
                    Text(
                        text = "Settled",
                        color = CreditGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// --- ADD CUSTOMER DIALOG ---
@Composable
fun AddCustomerDialog(
    viewModel: EasyBookViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val name by viewModel.newCustomerName.collectAsState()
    val mobile by viewModel.newCustomerMobile.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicCard),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = viewModel.t("add_customer"),
                    color = CosmicText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { viewModel.newCustomerName.value = it.uppercase() },
                    label = { Text(viewModel.t("customer_name"), color = CosmicTextDim) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicText,
                        unfocusedTextColor = CosmicText,
                        focusedBorderColor = CosmicPrimary,
                        unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("new_customer_name_input")
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { viewModel.newCustomerMobile.value = it },
                    label = { Text(viewModel.t("mobile_number"), color = CosmicTextDim) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicText,
                        unfocusedTextColor = CosmicText,
                        focusedBorderColor = CosmicPrimary,
                        unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("new_customer_mobile_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.triggerFeedback(context)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicSecondary),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(viewModel.t("cancel"))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            viewModel.triggerFeedback(context)
                            viewModel.registerCustomer(context) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) {
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_customer_button")
                    ) {
                        Text(viewModel.t("save"), color = CosmicDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- CUSTOMER DETAIL SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(viewModel: EasyBookViewModel) {
    val context = LocalContext.current
    val customer by viewModel.selectedCustomer.collectAsState()
    val transactions by viewModel.selectedCustomerTransactions.collectAsState()
    val balances by viewModel.customerBalances.collectAsState()
    val currency by viewModel.appCurrency.collectAsState()

    var showEntryDialog by remember { mutableStateOf(false) }
    var isDebtType by remember { mutableStateOf(true) }
    var showDeleteAuthDialog by remember { mutableStateOf(false) }
    var deletePasswordInput by remember { mutableStateOf("") }

    BackHandler {
        viewModel.currentScreen.value = "DASHBOARD"
    }

    val currencySymbol = when (currency) {
        "INR" -> "₹"
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "₹"
    }

    val balanceOwed = balances[customer?.uniqueId] ?: 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer?.name ?: "LEDGER", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.triggerFeedback(context)
                        viewModel.currentScreen.value = "DASHBOARD"
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CosmicPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDark),
                actions = {
                    IconButton(onClick = {
                        viewModel.triggerFeedback(context)
                        showDeleteAuthDialog = true
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Customer", tint = DebtRed)
                    }
                }
            )
        },
        containerColor = CosmicDark
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Customer Header Cards
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "MOBILE: ${customer?.mobile}",
                        color = CosmicText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "UNIQUE ID: ${customer?.uniqueId}",
                        color = CosmicAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Remaining Balance Owed:", color = CosmicTextDim, fontSize = 12.sp)
                            Text(
                                text = "$currencySymbol${String.format("%.2f", balanceOwed)}",
                                color = if (balanceOwed > 0) DebtRed else CreditGreen,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        if (balanceOwed > 0) {
                            Button(
                                onClick = {
                                    viewModel.triggerFeedback(context)
                                    viewModel.payOffFullDebt(context) { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CreditGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("pay_off_button")
                            ) {
                                Text(viewModel.t("pay_off"), color = CosmicDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Ledger Actions (Debt vs Deposit)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.triggerFeedback(context)
                        isDebtType = true
                        showEntryDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DebtRed),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("debt_trigger_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Give Debt", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("GIVE CREDIT", color = CosmicText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.triggerFeedback(context)
                        isDebtType = false
                        showEntryDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CreditGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("deposit_trigger_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Receive Deposit", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RCV PAYMENT", color = CosmicDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Title
            Text(
                text = viewModel.t("all_entries") + ":",
                color = CosmicText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Ledger Transaction list
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = viewModel.t("no_transactions"),
                        color = CosmicTextDim,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(transactions) { entry ->
                        TransactionRowItem(
                            entry = entry,
                            currencySymbol = currencySymbol,
                            onDelete = {
                                viewModel.triggerFeedback(context)
                                viewModel.deleteLedgerEntry(entry.id) { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEntryDialog) {
        AddLedgerEntryDialog(
            viewModel = viewModel,
            isDebt = isDebtType,
            onDismiss = { showEntryDialog = false }
        )
    }

    if (showDeleteAuthDialog) {
        Dialog(onDismissRequest = { showDeleteAuthDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = DebtRed,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = viewModel.t("delete_customer"),
                        color = CosmicText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = viewModel.t("confirm_delete_customer"),
                        color = CosmicTextDim,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = deletePasswordInput,
                        onValueChange = { deletePasswordInput = it },
                        label = { Text("MERCHANT PASSWORD", color = CosmicTextDim) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = DebtRed,
                            unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("delete_customer_password")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.triggerFeedback(context)
                                showDeleteAuthDialog = false
                                deletePasswordInput = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(viewModel.t("cancel"))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                viewModel.triggerFeedback(context)
                                viewModel.deleteCustomer(context, deletePasswordInput) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        showDeleteAuthDialog = false
                                        deletePasswordInput = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DebtRed),
                            modifier = Modifier.weight(1f).testTag("delete_customer_confirm_btn")
                        ) {
                            Text(viewModel.t("confirm"), color = CosmicText)
                        }
                    }
                }
            }
        }
    }
}

// --- TRANSACTION ROW ITEM ---
@Composable
fun TransactionRowItem(
    entry: TransactionEntity,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    val isDebt = entry.amount < 0
    val amountAbs = if (entry.amount < 0) -entry.amount else entry.amount
    val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(entry.timestamp))

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicCard.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle status icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isDebt) DebtRed.copy(alpha = 0.15f) else CreditGreen.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDebt) Icons.Default.PlayArrow else Icons.Default.Check,
                    contentDescription = if (isDebt) "Debt" else "Deposit",
                    tint = if (isDebt) DebtRed else CreditGreen,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isDebt) "Credit Given (Debt)" else "Received (Deposit)",
                    color = CosmicText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formattedDate,
                    color = CosmicTextDim,
                    fontSize = 11.sp
                )
                if (entry.notes.isNotEmpty()) {
                    Text(
                        text = "Note: ${entry.notes}",
                        color = CosmicAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isDebt) "-" else "+"}$currencySymbol${String.format("%.2f", amountAbs)}",
                    color = if (isDebt) DebtRed else CreditGreen,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Delete entry",
                        tint = CosmicTextDim.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// --- ADD LEDGER ENTRY DIALOG ---
@Composable
fun AddLedgerEntryDialog(
    viewModel: EasyBookViewModel,
    isDebt: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val amount by viewModel.transAmount.collectAsState()
    val notes by viewModel.transNotes.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = CosmicCard),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isDebt) viewModel.t("record_debt") else viewModel.t("record_deposit"),
                    color = if (isDebt) DebtRed else CreditGreen,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { viewModel.transAmount.value = it },
                    label = { Text(viewModel.t("enter_amount"), color = CosmicTextDim) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicText,
                        unfocusedTextColor = CosmicText,
                        focusedBorderColor = if (isDebt) DebtRed else CreditGreen,
                        unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("entry_amount_input")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { viewModel.transNotes.value = it },
                    label = { Text(viewModel.t("notes"), color = CosmicTextDim) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CosmicText,
                        unfocusedTextColor = CosmicText,
                        focusedBorderColor = CosmicAccent,
                        unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("entry_notes_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.triggerFeedback(context)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicText),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(viewModel.t("cancel"))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            viewModel.triggerFeedback(context)
                            viewModel.addLedgerEntry(context, isDebt) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                if (success) {
                                    onDismiss()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isDebt) DebtRed else CreditGreen),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("entry_save_btn")
                    ) {
                        Text(viewModel.t("save"), color = if (isDebt) CosmicText else CosmicDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- SETTINGS SCREEN ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: EasyBookViewModel) {
    val context = LocalContext.current
    val owner by viewModel.currentOwner.collectAsState()
    val customersList by viewModel.customers.collectAsState()
    val lang by viewModel.appLanguage.collectAsState()
    val currency by viewModel.appCurrency.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    val oldPass by viewModel.profileOldPassword.collectAsState()
    val newPass by viewModel.profileNewPassword.collectAsState()

    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var deleteAccountPassInput by remember { mutableStateOf("") }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.currentScreen.value = "DASHBOARD"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.t("settings"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.triggerFeedback(context)
                        viewModel.currentScreen.value = "DASHBOARD"
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = CosmicPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicDark)
            )
        },
        containerColor = CosmicDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Store Profile Header Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shop, contentDescription = "Shop", tint = CosmicPrimary, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(text = owner?.shopName ?: "", color = CosmicText, fontSize = 20.sp, fontWeight = FontWeight.Black)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "Owner: ${owner?.name}", color = CosmicTextDim, fontSize = 14.sp)
                                Text(text = "WhatsApp: ${owner?.whatsApp}", color = CosmicTextDim, fontSize = 14.sp)
                                Text(text = "Email: ${owner?.email} (Cannot change)", color = CosmicAccent, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            IconButton(onClick = {
                                viewModel.triggerFeedback(context)
                                showEditProfileDialog = true
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = CosmicPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${viewModel.t("total_customers")}: ${customersList.size}",
                            color = CosmicSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Language, Currency, Theme, Sound & Vibration Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Preferences", color = CosmicPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // App Language Dropdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = "Language", tint = CosmicAccent)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = viewModel.t("language"), color = CosmicText, fontSize = 14.sp)
                            }

                            var langDropdownExpanded by remember { mutableStateOf(false) }
                            val langMap = mapOf("EN" to "English (Eng)", "BN" to "Bengali (বাংলা)", "HI" to "Hindi (हिन्दी)")
                            val currentLangName = langMap[lang] ?: "English (Eng)"

                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CosmicDark)
                                        .clickable {
                                            viewModel.triggerFeedback(context)
                                            langDropdownExpanded = true
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentLangName,
                                        color = CosmicPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand",
                                        tint = CosmicPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = langDropdownExpanded,
                                    onDismissRequest = { langDropdownExpanded = false },
                                    modifier = Modifier.background(CosmicCard)
                                ) {
                                    langMap.forEach { (code, name) ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = name,
                                                    color = if (lang == code) CosmicPrimary else CosmicText,
                                                    fontWeight = if (lang == code) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 14.sp
                                                )
                                            },
                                            onClick = {
                                                viewModel.triggerFeedback(context)
                                                viewModel.changeLanguage(code)
                                                langDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // App Currency Dropdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CurrencyRupee, contentDescription = "Currency", tint = CosmicAccent)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = viewModel.t("currency"), color = CosmicText, fontSize = 14.sp)
                            }

                            var currencyDropdownExpanded by remember { mutableStateOf(false) }
                            val currencyMap = mapOf(
                                "INR" to "Rupee (₹)",
                                "USD" to "Dollar ($)",
                                "EUR" to "Euro (€)",
                                "GBP" to "Pound (£)"
                            )
                            val currentCurrencyName = currencyMap[currency] ?: "Rupee (₹)"

                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CosmicDark)
                                        .clickable {
                                            viewModel.triggerFeedback(context)
                                            currencyDropdownExpanded = true
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = currentCurrencyName,
                                        color = CosmicAccent,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Expand",
                                        tint = CosmicAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = currencyDropdownExpanded,
                                    onDismissRequest = { currencyDropdownExpanded = false },
                                    modifier = Modifier.background(CosmicCard)
                                ) {
                                    currencyMap.forEach { (code, name) ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = name,
                                                    color = if (currency == code) CosmicAccent else CosmicText,
                                                    fontWeight = if (currency == code) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 14.sp
                                                )
                                            },
                                            onClick = {
                                                viewModel.triggerFeedback(context)
                                                viewModel.changeCurrency(code)
                                                currencyDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Dark Theme Day/Night mode switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Theme Mode",
                                    tint = CosmicAccent
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "Dark Theme Mode", color = CosmicText, fontSize = 14.sp)
                            }

                            Switch(
                                checked = isDark,
                                onCheckedChange = {
                                    viewModel.triggerFeedback(context)
                                    viewModel.toggleDarkMode(it)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CosmicAccent,
                                    checkedTrackColor = CosmicAccent.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }

            // Change Password Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicCard)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Change Password", color = CosmicPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = oldPass,
                            onValueChange = { viewModel.profileOldPassword.value = it },
                            label = { Text(viewModel.t("old_password"), color = CosmicTextDim) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CosmicText,
                                unfocusedTextColor = CosmicText,
                                focusedBorderColor = CosmicPrimary,
                                unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = newPass,
                            onValueChange = { viewModel.profileNewPassword.value = it },
                            label = { Text(viewModel.t("new_password"), color = CosmicTextDim) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = CosmicText,
                                unfocusedTextColor = CosmicText,
                                focusedBorderColor = CosmicPrimary,
                                unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.triggerFeedback(context)
                                viewModel.updateProfilePassword { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("UPDATE PASSWORD", color = CosmicText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }



            // Logout & Account deletion options
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.triggerFeedback(context)
                            viewModel.logout()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CosmicTextDim),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(viewModel.t("logout"), color = CosmicDark)
                    }

                    Button(
                        onClick = {
                            viewModel.triggerFeedback(context)
                            showDeleteAccountDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DebtRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(viewModel.t("delete_account"), color = CosmicText)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showDeleteAccountDialog) {
        Dialog(onDismissRequest = { showDeleteAccountDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DebtRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = viewModel.t("delete_account"), color = CosmicText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = viewModel.t("confirm_delete_owner"), color = CosmicTextDim, fontSize = 13.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = deleteAccountPassInput,
                        onValueChange = { deleteAccountPassInput = it },
                        label = { Text("MERCHANT PASSWORD", color = CosmicTextDim) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = DebtRed,
                            unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("delete_account_password")
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.triggerFeedback(context)
                                showDeleteAccountDialog = false
                                deleteAccountPassInput = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(viewModel.t("cancel"))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Button(
                            onClick = {
                                viewModel.triggerFeedback(context)
                                viewModel.deleteAccount(deleteAccountPassInput) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    if (success) {
                                        showDeleteAccountDialog = false
                                        deleteAccountPassInput = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DebtRed),
                            modifier = Modifier.weight(1f).testTag("delete_account_confirm_btn")
                        ) {
                            Text(viewModel.t("confirm"), color = CosmicText)
                        }
                    }
                }
            }
        }
    }

    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(owner?.name ?: "") }
        var editShopName by remember { mutableStateOf(owner?.shopName ?: "") }
        var editWhatsApp by remember { mutableStateOf(owner?.whatsApp ?: "") }

        Dialog(onDismissRequest = { showEditProfileDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CosmicCard),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Edit Profile Details",
                        color = CosmicPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Owner Name", color = CosmicTextDim) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editShopName,
                        onValueChange = { editShopName = it },
                        label = { Text("Shop Name", color = CosmicTextDim) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editWhatsApp,
                        onValueChange = { editWhatsApp = it },
                        label = { Text("WhatsApp/Mobile Number", color = CosmicTextDim) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = CosmicText,
                            unfocusedTextColor = CosmicText,
                            focusedBorderColor = CosmicPrimary,
                            unfocusedBorderColor = CosmicTextDim.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showEditProfileDialog = false },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CosmicTextDim),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CANCEL")
                        }

                        Button(
                            onClick = {
                                viewModel.triggerFeedback(context)
                                viewModel.updateOwnerProfileDetails(editName, editShopName, editWhatsApp) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        showEditProfileDialog = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CosmicPrimary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("SAVE", color = CosmicDark)
                        }
                    }
                }
            }
        }
    }
}

// Center wrapping helper for circle icons
@Composable
fun Modifier.wrapContentCenter(): Modifier = this.then(
    Modifier.wrapContentSize(Alignment.Center)
)

@Composable
fun Modifier.wrapContentSize(alignment: Alignment): Modifier {
    return this
}

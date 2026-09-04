package com.example.premiumcipher

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val Gold = Color(0xFFFFD700)
val GoldDim = Color(0xFFB8860B)
val Silver = Color(0xFFE8E8E8)
val Danger = Color(0xFFFF6B6B)
val Warning = Color(0xFFFFB300)

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            PremiumCipherApp(viewModel)
        }
    }
}

@Composable
fun PremiumCipherApp(viewModel: MainViewModel) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Gold,
            background = Color.Black,
            surface = Color.Black
        )
    ) {
        PremiumBackground {
            Crossfade(
                targetState = viewModel.unlocked,
                animationSpec = tween(450),
                label = "Root"
            ) { unlocked ->
                if (unlocked) {
                    MainScreen(viewModel)
                } else {
                    SetupScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun PremiumBackground(content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "background")

    val move by transition.animateFloat(
        initialValue = -0.25f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "move"
    )

    val shine by transition.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shine"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Color.Black)

            val center1 = Offset(size.width * (0.5f + move), size.height * 0.22f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x28FFD700), Color.Transparent),
                    center = center1,
                    radius = size.minDimension * 0.95f
                ),
                radius = size.minDimension * 0.95f,
                center = center1
            )

            val center2 = Offset(size.width * (0.2f - move), size.height * 0.85f)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x14FFFFFF), Color.Transparent),
                    center = center2,
                    radius = size.minDimension * 0.8f
                ),
                radius = size.minDimension * 0.8f,
                center = center2
            )

            withTransform({
                translate(
                    left = size.width * shine - size.width * 0.25f,
                    top = -size.height * 0.2f
                )
                rotate(degrees = 18f, pivot = Offset.Zero)
            }) {
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x10FFFFFF),
                            Color(0x14FFD700),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset.Zero,
                    size = Size(size.width * 0.28f, size.height * 1.8f)
                )
            }
        }

        content()
    }
}

@Composable
fun ReflectiveGoldCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "card")

    val shine by transition.animateFloat(
        initialValue = -0.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cardShine"
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Gold.copy(alpha = 0.22f),
                spotColor = Gold.copy(alpha = 0.16f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF050505))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        GoldDim,
                        Gold.copy(alpha = 0.75f),
                        GoldDim
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .drawWithContent {
                drawContent()

                val bandWidth = size.width * 0.34f
                val start = Offset(size.width * shine - bandWidth, -size.height * 0.12f)
                val end = Offset(size.width * shine, size.height * 1.12f)

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x0DFFFFFF),
                            Color(0x14FFD700),
                            Color.Transparent
                        ),
                        start = start,
                        end = end
                    )
                )
            }
    ) {
        content()
    }
}

@Composable
fun PremiumLogo(size: Dp = 100.dp) {
    val transition = rememberInfiniteTransition(label = "logo")

    val glow by transition.animateFloat(
        initialValue = 8f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoGlow"
    )

    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logoBreath"
    )

    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "Premium Cipher logo",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = glow.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Gold,
                spotColor = Gold
            )
            .clip(RoundedCornerShape(22.dp))
    )
}

@Composable
fun GoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val glow by animateDpAsState(
        targetValue = if (pressed) 30.dp else 12.dp,
        label = "buttonGlow"
    )

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.965f else 1f,
        label = "buttonScale"
    )

    val transition = rememberInfiniteTransition(label = "button")

    val shine by transition.animateFloat(
        initialValue = -1.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonShine"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.45f
            }
            .shadow(
                elevation = glow,
                shape = RoundedCornerShape(18.dp),
                ambientColor = Gold,
                spotColor = Gold
            )
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF8A6A00),
                        GoldDim,
                        Gold,
                        Color(0xFFFFF2B8)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color(0xFFFDF0B0).copy(alpha = 0.55f),
                shape = RoundedCornerShape(18.dp)
            )
            .drawWithContent {
                drawContent()

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33FFFFFF),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height * 0.32f
                    )
                )

                val bandWidth = size.width * 0.3f
                val start = Offset(size.width * shine - bandWidth, -size.height * 0.2f)
                val end = Offset(size.width * shine, size.height * 1.2f)

                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x2EFFFFFF),
                            Color.Transparent
                        ),
                        start = start,
                        end = end
                    )
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun GoldPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    show: Boolean,
    onToggleShow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()

    val glow by animateDpAsState(
        targetValue = if (focused) 18.dp else 4.dp,
        label = "fieldGlow"
    )

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = glow,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Gold,
                spotColor = Gold
            ),
        singleLine = true,
        visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(14.dp),
        interactionSource = interactionSource,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Gold,
            unfocusedBorderColor = GoldDim,
            focusedLabelColor = Gold,
            unfocusedLabelColor = GoldDim,
            cursorColor = Gold
        ),
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = onToggleShow) {
                Icon(
                    imageVector = if (show) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    contentDescription = "Toggle passphrase visibility",
                    tint = Gold
                )
            }
        }
    )
}

@Composable
fun OutputCard(
    title: String,
    text: String,
    onCopied: () -> Unit
) {
    val clipboard = LocalClipboardManager.current

    ReflectiveGoldCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = Gold,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(text))
                        onCopied()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy",
                        tint = Gold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            SelectionContainer {
                Text(
                    text = text,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PremiumLoader() {
    Box(
        modifier = Modifier
            .size(38.dp)
            .shadow(
                elevation = 14.dp,
                shape = CircleShape,
                ambientColor = Gold,
                spotColor = Gold
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Gold,
            modifier = Modifier.size(22.dp),
            strokeWidth = 2.dp
        )
    }
}

@Composable
fun SetupScreen(viewModel: MainViewModel) {
    var passphrase by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val shake = remember { Animatable(0f) }
    
    val isShort = CryptoEngine.passphraseLength(passphrase) < 12

    fun triggerShake() {
        scope.launch {
            shake.snapTo(0f)

            for (i in 0 until 3) {
                shake.animateTo(18f, tween(50))
                shake.animateTo(-18f, tween(50))
            }

            shake.animateTo(0f, tween(50))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ReflectiveGoldCard(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationX = shake.value
                    }
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PremiumLogo()

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "Premium Cipher",
                        color = Gold,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Set your master passphrase. It supports all languages and emoji.",
                        color = Color(0xFFBDBDBD),
                        fontSize = 14.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    GoldPasswordField(
                        label = "Passphrase",
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        show = showPass,
                        onToggleShow = { showPass = !showPass }
                    )

                    Spacer(Modifier.height(12.dp))

                    GoldPasswordField(
                        label = "Confirm passphrase",
                        value = confirm,
                        onValueChange = { confirm = it },
                        show = showConfirm,
                        onToggleShow = { showConfirm = !showConfirm }
                    )

                    Spacer(Modifier.height(10.dp))

                    if (isShort && passphrase.isNotEmpty()) {
                        Text(
                            text = "⚠ Warning: Short passphrase. 12+ characters recommended.",
                            color = Warning,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    error?.let {
                        Text(
                            text = it,
                            color = Danger,
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    GoldButton(
                        text = "Unlock",
                        onClick = {
                            if (passphrase.isEmpty()) {
                                error = "Passphrase cannot be empty."
                                triggerShake()
                            } else if (!CryptoEngine.passphraseEquals(passphrase, confirm)) {
                                error = "Passphrases do not match."
                                triggerShake()
                            } else {
                                error = null
                                viewModel.unlock(passphrase)
                                passphrase = ""
                                confirm = ""
                            }
                        }
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Developed by Ibrahim Khaled",
                color = Color(0xFF666666),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showMessage(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .fillMaxSize()
        ) {
            val wide = maxWidth > 720.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                ReflectiveGoldCard(Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PremiumLogo(size = 56.dp)

                        Spacer(Modifier.width(12.dp))

                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "Premium Cipher",
                                color = Gold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )

                            Text(
                                text = "AES-256-GCM • Secure CPHR3",
                                color = Color(0xFFBDBDBD),
                                fontSize = 12.sp
                            )
                        }

                        IconButton(onClick = { viewModel.lock() }) {
                            Icon(
                                imageVector = Icons.Outlined.Lock,
                                contentDescription = "Lock and change passphrase",
                                tint = Gold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                if (wide) {
                    Row(Modifier.fillMaxWidth()) {
                        EncryptPanel(
                            viewModel = viewModel,
                            modifier = Modifier.weight(1f),
                            showMessage = { msg -> showMessage(msg) }
                        )

                        Spacer(Modifier.width(16.dp))

                        DecryptPanel(
                            viewModel = viewModel,
                            modifier = Modifier.weight(1f),
                            showMessage = { msg -> showMessage(msg) }
                        )
                    }
                } else {
                    var selectedTab by remember { mutableStateOf(0) }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = Gold
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Text(
                                    text = "Encrypt",
                                    color = Gold
                                )
                            }
                        )

                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Text(
                                    text = "Decrypt",
                                    color = Gold
                                )
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Crossfade(
                        targetState = selectedTab,
                        animationSpec = tween(350),
                        label = "panels"
                    ) { tab ->
                        when (tab) {
                            0 -> EncryptPanel(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxWidth(),
                                showMessage = { msg -> showMessage(msg) }
                            )
                            else -> DecryptPanel(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxWidth(),
                                showMessage = { msg -> showMessage(msg) }
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Developed by Ibrahim Khaled",
                        color = Color(0xFF666666),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun EncryptPanel(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    showMessage: (String) -> Unit
) {
    var message by remember { mutableStateOf("") }
    var output by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    ReflectiveGoldCard(modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "Encrypt",
                color = Gold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = {
                    Text(
                        text = "Message",
                        color = Color(0xFF8F8F8F)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = GoldDim,
                    cursorColor = Gold
                )
            )

            Spacer(Modifier.height(12.dp))

            GoldButton(
                text = if (loading) "Encrypting..." else "Encrypt",
                enabled = message.isNotEmpty() && !loading,
                onClick = {
                    scope.launch {
                        loading = true

                        try {
                            val token = withContext(Dispatchers.IO) {
                                viewModel.withPassphrase { pass ->
                                    CryptoEngine.encryptSecure(message, pass)
                                }
                            }

                            output = token
                            showMessage("Encryption complete")
                        } catch (e: OutOfMemoryError) {
                            showMessage("Device ran out of memory during encryption.")
                        } catch (e: CryptoException) {
                            showMessage(e.message ?: "Encryption failed")
                        } catch (e: Exception) {
                            showMessage("Unexpected error")
                        } finally {
                            loading = false
                        }
                    }
                }
            )

            AnimatedVisibility(
                visible = loading,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(250))
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    PremiumLoader()
                }
            }

            AnimatedVisibility(
                visible = output != null,
                enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                output?.let {
                    Column {
                        Spacer(Modifier.height(12.dp))

                        OutputCard(
                            title = "Ciphertext",
                            text = it
                        ) {
                            showMessage("Copied ciphertext")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DecryptPanel(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    showMessage: (String) -> Unit
) {
    var token by remember { mutableStateOf("") }
    var output by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    ReflectiveGoldCard(modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(
                text = "Decrypt",
                color = Gold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = {
                    Text(
                        text = "Paste CPHR3 / CPHR2 / CPHR1 token",
                        color = Color(0xFF8F8F8F)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Gold,
                    unfocusedBorderColor = GoldDim,
                    cursorColor = Gold
                )
            )

            Spacer(Modifier.height(12.dp))

            GoldButton(
                text = if (loading) "Decrypting..." else "Decrypt",
                enabled = token.isNotBlank() && !loading,
                onClick = {
                    scope.launch {
                        loading = true

                        try {
                            val plaintext = withContext(Dispatchers.IO) {
                                viewModel.withPassphrase { pass ->
                                    CryptoEngine.decryptAny(token, pass)
                                }
                            }

                            output = plaintext
                            showMessage("Decryption complete")
                        } catch (e: OutOfMemoryError) {
                            showMessage("Not enough memory to decrypt this legacy token.")
                        } catch (e: CryptoException) {
                            showMessage(e.message ?: "Invalid passphrase or corrupted data")
                        } catch (e: Exception) {
                            showMessage("Unexpected error")
                        } finally {
                            loading = false
                        }
                    }
                }
            )

            AnimatedVisibility(
                visible = loading,
                enter = fadeIn(tween(250)),
                exit = fadeOut(tween(250))
            ) {
                Column {
                    Spacer(Modifier.height(12.dp))
                    PremiumLoader()
                }
            }

            AnimatedVisibility(
                visible = output != null,
                enter = fadeIn(tween(350)) + expandVertically(tween(350)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                output?.let {
                    Column {
                        Spacer(Modifier.height(12.dp))

                        OutputCard(
                            title = "Decrypted message",
                            text = it
                        ) {
                            showMessage("Copied decrypted message")
                        }
                    }
                }
            }
        }
    }
}

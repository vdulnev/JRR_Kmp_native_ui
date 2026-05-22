package com.jrr.jrrkmp_native_ui.presentation.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jrr.jrrkmp_native_ui.core.theme.AppColors
import com.jrr.jrrkmp_native_ui.core.theme.AppTypography
import com.jrr.jrrkmp_native_ui.core.theme.outlinedTextFieldColors
import com.jrr.jrrkmp_native_ui.core.theme.BoxBorder
import com.jrr.jrrkmp_native_ui.data.db.entity.SavedServerEntity
import com.jrr.jrrkmp_native_ui.data.repository.ServerRepository
import com.jrr.jrrkmp_native_ui.playback.AudioPlayerFacade
import com.jrr.jrrkmp_native_ui.domain.model.Zone
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerManagerScreen(
    facade: AudioPlayerFacade,
    serverRepository: ServerRepository,
    onConnectSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var accessKey by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("52199") }
    var useSsl by remember { mutableStateOf(false) }
    var sslPort by remember { mutableStateOf("52200") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var savedServers by remember { mutableStateOf<List<SavedServerEntity>>(emptyList()) }
    var isConnecting by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf(0) } // 0 = Access Key, 1 = Manual

    // Fetch saved servers
    LaunchedEffect(Unit) {
        savedServers = serverRepository.getAllServers()
    }

    val connectAction: (String, Int, Boolean, Int, String, String, String?) -> Unit = { h, p, ssl, sp, u, pass, friendly ->
        isConnecting = true
        scope.launch {
            try {
                val token = serverRepository.authenticate(h, p, ssl, sp, u, pass)
                if (token != null) {
                    // Check alive to get friendly name
                    val finalName = serverRepository.checkAlive(h, p, ssl, sp, token) ?: friendly ?: "JRiver Server"
                    // Save server profiles
                    val id = UUID.nameUUIDFromBytes("$h:$p".toByteArray()).toString()
                    val entity = SavedServerEntity(
                        id = id,
                        host = h,
                        port = p,
                        username = u,
                        passwordKey = pass,
                        friendlyName = finalName,
                        lastUsedAt = System.currentTimeMillis(),
                        authToken = token,
                        useSsl = ssl,
                        sslPort = sp
                    )
                    serverRepository.saveServer(entity)
                    // Set active connection on facade
                    facade.setServerConnection(h, p, ssl, sp, token)
                    facade.setZone(Zone.Local) // Default to Local on connection
                    
                    Toast.makeText(context, "Connected to $finalName", Toast.LENGTH_SHORT).show()
                    onConnectSuccess()
                } else {
                    Toast.makeText(context, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                isConnecting = false
                savedServers = serverRepository.getAllServers()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.bg1)
            .padding(16.dp)
    ) {
        // App title
        Text(
            text = "JRiver Remote".uppercase(),
            style = AppTypography.screenTitle.copy(color = AppColors.accent, fontSize = 24.sp),
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Connection forms card
        Card(
            colors = CardDefaults.cardColors(containerColor = AppColors.bg2),
            border = BoxBorder(AppColors.line),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color.Transparent,
                    contentColor = AppColors.accent,
                    divider = {}
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Access Key", style = AppTypography.itemTitle) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Manual IP", style = AppTypography.itemTitle) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (activeTab == 0) {
                    // Access Key Flow
                    OutlinedTextField(
                        value = accessKey,
                        onValueChange = { if (it.length <= 6) accessKey = it.uppercase() },
                        label = { Text("6-Digit Access Key", color = AppColors.text2) },
                        colors = outlinedTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    // Manual IP Flow
                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Host IP / Address", color = AppColors.text2) },
                        colors = outlinedTextFieldColors(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (useSsl) sslPort else port,
                            onValueChange = { if (useSsl) sslPort = it else port = it },
                            label = { Text("Port", color = AppColors.text2) },
                            colors = outlinedTextFieldColors(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                        ) {
                            Checkbox(
                                checked = useSsl,
                                onCheckedChange = { useSsl = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AppColors.accent,
                                    uncheckedColor = AppColors.text3
                                )
                            )
                            Text("Use HTTPS/SSL", style = AppTypography.itemSubtitle, color = AppColors.text)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Auth credentials
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = AppColors.text2) },
                    colors = outlinedTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = AppColors.text2) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = outlinedTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isConnecting) return@Button
                        if (activeTab == 0) {
                            if (accessKey.length != 6) {
                                Toast.makeText(context, "Enter a valid 6-digit key", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isConnecting = true
                            scope.launch {
                                val lookup = serverRepository.lookupAccessKey(accessKey)
                                if (lookup != null) {
                                     val resolvedHost = lookup.localIpList.firstOrNull() ?: lookup.ip
                                     val resolvedPort = (if (useSsl) lookup.httpsPort else lookup.port) ?: (if (useSsl) 52200 else 52199)
                                     connectAction(resolvedHost, resolvedPort, useSsl, resolvedPort, username, password, "Server ($accessKey)")
                                } else {
                                    Toast.makeText(context, "Lookup failed for $accessKey", Toast.LENGTH_SHORT).show()
                                    isConnecting = false
                                }
                            }
                        } else {
                            if (host.isEmpty()) {
                                Toast.makeText(context, "Enter a host address", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val pNum = port.toIntOrNull() ?: 52199
                            val spNum = sslPort.toIntOrNull() ?: 52200
                            connectAction(host, pNum, useSsl, spNum, username, password, null)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.accent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isConnecting) {
                        CircularProgressIndicator(color = AppColors.bg0, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Connect".uppercase(), style = AppTypography.chipMono, color = AppColors.bg0)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Offline mode entry
                OutlinedButton(
                    onClick = {
                        facade.setServerConnection("", 0, false, 0, null)
                        facade.setZone(Zone.Offline)
                        onConnectSuccess()
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.text2),
                    border = BoxBorder(AppColors.line2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enter Offline Mode".uppercase(), style = AppTypography.chipMono, color = AppColors.text2)
                }
            }
        }

        // Saved Servers section
        Text(
            text = "Saved Connections".uppercase(),
            style = AppTypography.sectionHeading,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(savedServers) { server ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.bg2)
                        .border(1.dp, AppColors.line, RoundedCornerShape(8.dp))
                        .clickable {
                            connectAction(
                                server.host,
                                server.port,
                                server.useSsl,
                                server.sslPort,
                                server.username,
                                server.passwordKey,
                                server.friendlyName
                             )
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = server.friendlyName ?: "JRiver Server",
                            style = AppTypography.itemTitle
                        )
                        Text(
                            text = "${server.host}:${if (server.useSsl) server.sslPort else server.port}",
                            style = AppTypography.itemSubtitle,
                            color = AppColors.text2
                        )
                    }

                    IconButton(onClick = {
                        scope.launch {
                            serverRepository.deleteServer(server)
                            savedServers = serverRepository.getAllServers()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Server",
                            tint = AppColors.error
                        )
                    }
                }
            }
        }
    }
}

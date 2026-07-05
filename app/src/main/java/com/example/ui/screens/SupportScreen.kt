package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import com.example.ui.WalletViewModel
import com.example.data.SupportTicket
import com.example.data.SupportMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportScreen(
    viewModel: WalletViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Support Links (Telegram)
    val allConfigs by viewModel.repository.getAllConfigsFlow().collectAsState(initial = emptyList())
    val rawLinksStr = allConfigs.find { it.key == "telegram_support_links" }?.value ?: ""
    val legacyLink = allConfigs.find { it.key == "telegram_support_link" }?.value ?: ""
    
    val activeSupportLinks = remember(rawLinksStr, legacyLink) {
        val parsed = if (rawLinksStr.isNotBlank()) {
            com.example.data.TelegramSupportConfig.parseList(rawLinksStr)
        } else if (legacyLink.isNotBlank()) {
            listOf(com.example.data.TelegramSupportConfig(legacyLink, true))
        } else {
            emptyList()
        }
        parsed.filter { it.isActive }
    }
    
    var showSupportSelectorDialog by remember { mutableStateOf(false) }
    var selectedTicketId by remember { mutableStateOf<Int?>(null) }
    var showCreateTicketDialog by remember { mutableStateOf(false) }

    if (showSupportSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showSupportSelectorDialog = false },
            title = { Text("Select Live Support Desk") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    activeSupportLinks.forEach { config ->
                        val label = if (config.link.startsWith("@")) config.link else config.link
                        Button(
                            onClick = {
                                val cleaned = config.link.replace("@", "").trim()
                                val url = if (config.link.startsWith("http")) config.link else "https://t.me/$cleaned"
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Ignore
                                }
                                showSupportSelectorDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSupportSelectorDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (currentUser == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val userTickets by viewModel.getTicketsForUser(currentUser!!.id).collectAsState(initial = emptyList())

    if (selectedTicketId == null) {
        // Tickets List View
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Support Tickets", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (activeSupportLinks.size > 1) {
                                    showSupportSelectorDialog = true
                                } else {
                                    val targetLink = activeSupportLinks.firstOrNull()?.link ?: "personal_number_admin"
                                    val cleaned = targetLink.replace("@", "").trim()
                                    val url = if (targetLink.startsWith("http")) targetLink else "https://t.me/$cleaned"
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = "Telegram Support", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateTicketDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Create Ticket")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // High-polish live support banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Telegram Live Support",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Need quick assistance? Connect directly to our support handlers on Telegram.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (activeSupportLinks.size > 1) {
                                    showSupportSelectorDialog = true
                                } else {
                                    val targetLink = activeSupportLinks.firstOrNull()?.link ?: "personal_number_admin"
                                    val cleaned = targetLink.replace("@", "").trim()
                                    val url = if (targetLink.startsWith("http")) targetLink else "https://t.me/$cleaned"
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Ignore
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Chat", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "My Support History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                if (userTickets.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Chat,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No active support tickets.",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Click the '+' button to open a support ticket. Only admins will review and reply to your ticket.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(userTickets) { ticket ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTicketId = ticket.id },
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = ticket.subject,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        TicketStatusBadge(status = ticket.status)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ID: #${ticket.id}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                        Text(
                                            text = "Updated: ${sdf.format(Date(ticket.lastUpdated))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create Ticket Dialog
        if (showCreateTicketDialog) {
            var subject by remember { mutableStateOf("") }
            var message by remember { mutableStateOf("") }
            var isError by remember { mutableStateOf(false) }
            var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
            
            val pickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                selectedImageUri = uri
            }

            AlertDialog(
                onDismissRequest = { showCreateTicketDialog = false },
                title = { Text("Open Support Ticket") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Please describe your problem or inquiry. An Admin will review and reply directly.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject (e.g., Deposit issue, Account help)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("Message details") },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            maxLines = 5
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { pickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                Icon(Icons.Filled.Chat, contentDescription = "Add Screenshot", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Attach Screenshot", fontSize = 12.sp)
                            }
                            
                            if (selectedImageUri != null) {
                                Box(modifier = Modifier.size(40.dp)) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Selected Image",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    IconButton(
                                        onClick = { selectedImageUri = null },
                                        modifier = Modifier
                                            .size(16.dp)
                                            .align(Alignment.TopEnd)
                                            .background(Color.Red, CircleShape)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }

                        if (isError) {
                            Text("Please fill out both fields.", color = Color.Red, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (subject.isNotBlank() && message.isNotBlank()) {
                                val imagePath = selectedImageUri?.let { uri ->
                                    saveUriToInternalStorage(context, uri)
                                }
                                viewModel.openTicket(
                                    userId = currentUser!!.id,
                                    username = currentUser!!.username,
                                    subject = subject.trim(),
                                    firstMessage = message.trim(),
                                    imagePath = imagePath
                                )
                                showCreateTicketDialog = false
                                subject = ""
                                message = ""
                                selectedImageUri = null
                                isError = false
                            } else {
                                isError = true
                            }
                        }
                    ) {
                        Text("Submit Ticket")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateTicketDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    } else {
        // Active Ticket Chat Screen
        val ticketFlow = viewModel.getTicketByIdFlow(selectedTicketId!!).collectAsState(initial = null)
        val ticketMessages by viewModel.getMessagesForTicket(selectedTicketId!!).collectAsState(initial = emptyList())
        var replyText by remember { mutableStateOf("") }
        var replyImageUri by remember { mutableStateOf<Uri?>(null) }
        val activeChatPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            replyImageUri = uri
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                ticketFlow.value?.subject ?: "Ticket Support",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                "Ticket ID: #${selectedTicketId}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedTicketId = null }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back to tickets")
                        }
                    },
                    actions = {
                        val status = ticketFlow.value?.status ?: "Open"
                        if (status != "Closed") {
                            IconButton(
                                onClick = {
                                    viewModel.closeTicket(selectedTicketId!!)
                                }
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Close Ticket", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Info Banner
                val currentStatus = ticketFlow.value?.status ?: "Open"
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Status: $currentStatus",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        if (currentStatus == "Closed") {
                            Text(
                                "Resolved",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        } else {
                            Text(
                                "Reviewed by Admin only",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ticketMessages) { msg ->
                        val isMe = msg.senderRole == "User"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                // Sender name
                                Text(
                                    text = if (isMe) "You" else msg.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )

                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = 12.dp,
                                        topEnd = 12.dp,
                                        bottomStart = if (isMe) 12.dp else 0.dp,
                                        bottomEnd = if (isMe) 0.dp else 12.dp
                                    )
                                ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (!msg.imagePath.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = File(msg.imagePath),
                                            contentDescription = "Attachment Screenshot",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(max = 200.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .padding(bottom = 8.dp),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                                        )
                                    }
                                    Text(
                                        text = msg.message,
                                        fontSize = 14.sp,
                                        color = if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                    Text(
                                        text = sdf.format(Date(msg.timestamp)),
                                        fontSize = 9.sp,
                                        color = (if (isMe) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer).copy(alpha = 0.6f),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                                }
                            }
                        }
                    }
                }

                // Bottom Input
                if (currentStatus == "Closed") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "This ticket has been marked resolved and closed.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Surface(
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (replyImageUri != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(60.dp)) {
                                        AsyncImage(
                                            model = replyImageUri,
                                            contentDescription = "Selected Image",
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                        IconButton(
                                            onClick = { replyImageUri = null },
                                            modifier = Modifier
                                                .size(18.dp)
                                                .align(Alignment.TopEnd)
                                                .background(Color.Red, CircleShape)
                                        ) {
                                            Icon(Icons.Filled.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Screenshot attached ready to send", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { activeChatPickerLauncher.launch("image/*") }
                                ) {
                                    Icon(Icons.Filled.Chat, contentDescription = "Add Screenshot", tint = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                OutlinedTextField(
                                    value = replyText,
                                    onValueChange = { replyText = it },
                                    placeholder = { Text("Write a reply to Admin...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = false,
                                    maxLines = 3,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                FloatingActionButton(
                                    onClick = {
                                        if (replyText.isNotBlank() || replyImageUri != null) {
                                            val imagePath = replyImageUri?.let { uri ->
                                                saveUriToInternalStorage(context, uri)
                                            }
                                            viewModel.sendSupportMessage(
                                                ticketId = selectedTicketId!!,
                                                senderId = currentUser!!.id,
                                                senderRole = "User",
                                                senderName = currentUser!!.username,
                                                messageText = replyText.trim(),
                                                imagePath = imagePath
                                            )
                                            replyText = ""
                                            replyImageUri = null
                                        }
                                    },
                                    modifier = Modifier.size(48.dp),
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    shape = CircleShape
                                ) {
                                    Icon(Icons.Filled.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TicketStatusBadge(status: String) {
    val containerColor = when (status) {
        "Open" -> Color(0xFFE8F5E9)
        "Pending Admin Response" -> Color(0xFFE3F2FD)
        "Pending User Response" -> Color(0xFFF3E5F5)
        else -> Color(0xFFECEFF1) // Closed
    }
    val contentColor = when (status) {
        "Open" -> Color(0xFF2E7D32)
        "Pending Admin Response" -> Color(0xFF1565C0)
        "Pending User Response" -> Color(0xFF6A1B9A)
        else -> Color(0xFF37474F) // Closed
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = status,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun saveUriToInternalStorage(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "support_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

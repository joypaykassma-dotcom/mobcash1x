package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.WalletViewModel
import com.example.ui.theme.PrimaryBlue

@Composable
fun SignupSuccessScreen(
    userId: Int,
    viewModel: WalletViewModel,
    onContinue: () -> Unit
) {
    val user by viewModel.getUserById(userId).collectAsState(initial = null)
    val context = androidx.compose.ui.platform.LocalContext.current
    var hasDownloaded by remember { mutableStateOf(false) }

    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryBlue)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Signup Successful!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Your Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                HorizontalDivider()
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Username:")
                    Text(user!!.username, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Number:")
                    Text(user!!.number, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Email:")
                    Text(user!!.email, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Password:")
                    Text(user!!.passwordHash, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Secret PIN:")
                    Text(user!!.withdrawPin, fontWeight = FontWeight.Bold)
                }
                if (user!!.accountType == "Cashier") {
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("EPOS Code:", color = MaterialTheme.colorScheme.primary)
                        Text(user!!.eposCode, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Please save your EPOS code. You need it to login.", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                try {
                    saveReceiptToGallery(context, user!!)
                    hasDownloaded = true
                    android.widget.Toast.makeText(
                        context, 
                        "Receipt saved to Gallery!", 
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    android.widget.Toast.makeText(
                        context, 
                        "Failed to save receipt: ${e.message}", 
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("Download / Save Receipt", color = Color.White)
        }
        
        if (hasDownloaded) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Login Now")
            }
        }
    }
}

private fun saveReceiptToGallery(context: android.content.Context, user: com.example.data.User) {
    val bitmap = android.graphics.Bitmap.createBitmap(800, 600, android.graphics.Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.FILL
    }
    canvas.drawRect(0f, 0f, 800f, 600f, paint)

    paint.apply {
        color = android.graphics.Color.BLACK
        textSize = 40f
        isAntiAlias = true
    }

    var y = 100f
    canvas.drawText("Signup Receipt - Do Not Share", 50f, y, paint)
    y += 60f
    paint.textSize = 30f
    canvas.drawText("Username: ${user.username}", 50f, y, paint)
    y += 50f
    canvas.drawText("Number: ${user.number}", 50f, y, paint)
    y += 50f
    canvas.drawText("Email: ${user.email}", 50f, y, paint)
    y += 50f
    canvas.drawText("Password: ${user.passwordHash}", 50f, y, paint)
    y += 50f
    canvas.drawText("Secret PIN: ${user.withdrawPin}", 50f, y, paint)
    if (user.accountType == "Cashier") {
        y += 50f
        paint.color = android.graphics.Color.RED
        canvas.drawText("EPOS Code: ${user.eposCode}", 50f, y, paint)
        paint.color = android.graphics.Color.BLACK
    }
    y += 50f
    canvas.drawText("Time: ${java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())}", 50f, y, paint)

    val values = android.content.ContentValues().apply {
        put(android.provider.MediaStore.Images.Media.DISPLAY_NAME, "receipt_${user.username}_${System.currentTimeMillis()}.png")
        put(android.provider.MediaStore.Images.Media.MIME_TYPE, "image/png")
        put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES)
    }

    val uri = context.contentResolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    if (uri != null) {
        context.contentResolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, out)
        }
    }
}


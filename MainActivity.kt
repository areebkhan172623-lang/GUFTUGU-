package com.guftagu.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.concurrent.TimeUnit

private val Green = Color(0xFF087F68)
private data class ChatMessage(val text: String = "", val sender: String = "", val sentAt: Long = 0L)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        setContent { MaterialTheme { GuftaguApp(this) } }
    }
}

@Composable
fun GuftaguApp(activity: ComponentActivity) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    var phone by remember { mutableStateOf("+92") }
    var otp by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("Apna mobile number country code ke sath likhein.") }
    var targetUid by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var chatId by remember { mutableStateOf("") }
    val user = auth.currentUser

    Surface(Modifier.fillMaxSize(), color = Color(0xFFF4F8F6)) {
        Column {
            Row(Modifier.fillMaxWidth().background(Green).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("💬  Guftagu", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.weight(1f))
                Text(if (user == null) "Welcome" else "Online", color = Color.White)
            }
            if (user == null) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Phone number se login", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(phone, { phone = it }, label = { Text("Mobile number (+923...)") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        if (!phone.startsWith("+") || phone.length < 10) { status = "Valid country code ke sath number likhein."; return@Button }
                        val options = PhoneAuthOptions.newBuilder(auth)
                            .setPhoneNumber(phone).setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(activity)
                            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                                    auth.signInWithCredential(credential).addOnSuccessListener { status = "Login successful." }
                                        .addOnFailureListener { status = it.localizedMessage ?: "Login failed" }
                                }
                                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) { status = e.localizedMessage ?: "Verification failed" }
                                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                                    verificationId = id; status = "SMS code bhej diya gaya. OTP enter karein."
                                }
                            }).build()
                        PhoneAuthProvider.verifyPhoneNumber(options)
                    }, modifier = Modifier.fillMaxWidth()) { Text("Send OTP") }
                    if (verificationId != null) {
                        OutlinedTextField(otp, { otp = it }, label = { Text("6 digit OTP") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = {
                            val credential = com.google.firebase.auth.PhoneAuthProvider.getCredential(verificationId!!, otp)
                            auth.signInWithCredential(credential).addOnSuccessListener { status = "Login successful." }
                                .addOnFailureListener { status = it.localizedMessage ?: "OTP incorrect" }
                        }, modifier = Modifier.fillMaxWidth()) { Text("Verify & Login") }
                    }
                    Text(status, color = Green)
                }
            } else {
                Column(Modifier.weight(1f).fillMaxWidth().padding(12.dp)) {
                    Text("New chat", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(targetUid, { targetUid = it }, label = { Text("Receiver ka Firebase User UID") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        if (targetUid.isBlank()) return@Button
                        chatId = listOf(user.uid, targetUid).sorted().joinToString("_")
                        db.collection("chats").document(chatId)
                            .set(mapOf("participants" to listOf(user.uid, targetUid).distinct()))
                        db.collection("chats").document(chatId).collection("messages")
                            .orderBy("sentAt", Query.Direction.ASCENDING)
                            .addSnapshotListener { snap, err ->
                                if (err == null && snap != null) messages = snap.documents.mapNotNull { it.toObject(ChatMessage::class.java) }
                            }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Open chat") }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        items(messages) { item ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = if (item.sender == user.uid) Arrangement.End else Arrangement.Start) {
                                Text(item.text, Modifier.background(if (item.sender == user.uid) Color(0xFFD8F4E8) else Color.White, RoundedCornerShape(14.dp)).padding(12.dp))
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(message, { message = it }, modifier = Modifier.weight(1f), placeholder = { Text("Message...") })
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            if (chatId.isNotBlank() && message.isNotBlank()) {
                                val payload = mapOf("text" to message.trim(), "sender" to user.uid, "sentAt" to System.currentTimeMillis())
                                db.collection("chats").document(chatId).collection("messages").add(payload)
                                    .addOnSuccessListener { message = "" }
                                    .addOnFailureListener { status = it.localizedMessage ?: "Send failed" }
                            } else status = "Pehle receiver UID ke zariye chat open karein."
                        }) { Text("Send") }
                    }
                    Text(status, color = Green, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = { auth.signOut(); messages = emptyList(); chatId = "" }, modifier = Modifier.align(Alignment.End)) { Text("Logout") }
            }
        }
    }
}

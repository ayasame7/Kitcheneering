package com.example.fridgetracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ReminderReceiver", "Received intent: ${intent.action}")
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("ReminderReceiver", "Re-scheduling reminders after boot")
            MainActivity.scheduleReminders(context)
            return
        }

        // Get the current user ID from SharedPreferences
        val sharedPref = context.getSharedPreferences("FridgeTracker", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("current_user_id", null)

        if (userId == null) {
            Log.w("ReminderReceiver", "No user logged in, skipping reminder")
            return
        }

        // Check for expiring items first
        checkAndShowExpiryNotifications(context, userId)

        // Show generic daily reminder
        showGenericReminder(context)
    }

    private fun checkAndShowExpiryNotifications(context: Context, userId: String) {
        val database = FirebaseDatabase.getInstance().reference.child("users").child(userId).child("inventory")
        val latch = CountDownLatch(1)
        var expiringItems: List<String> = emptyList()

        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itemsToNotify = mutableListOf<String>()
                val now = System.currentTimeMillis()
                val twoDaysInMillis = 2 * 24 * 60 * 60 * 1000

                snapshot.children.forEach { child ->
                    try {
                        val item = child.getValue(KitchenItem::class.java)
                        if (item != null && item.expiryDate != null) {
                            val daysUntilExpiry = (item.expiryDate!! - now) / (24 * 60 * 60 * 1000).toDouble()

                            // Show notification if item expires in 0-2 days (but not if already expired)
                            if (daysUntilExpiry in 0.0..2.0) {
                                itemsToNotify.add(child.key ?: "Unknown Item")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ExpiryCheck", "Error parsing item: ${e.message}")
                    }
                }

                expiringItems = itemsToNotify
                if (itemsToNotify.isNotEmpty()) {
                    Log.d("ExpiryCheck", "Found ${itemsToNotify.size} items expiring soon: $itemsToNotify")
                    MainActivity.showExpiryNotification(context, itemsToNotify)
                } else {
                    Log.d("ExpiryCheck", "No items expiring within 2 days")
                }
                latch.countDown()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ExpiryCheck", "Failed to fetch items: ${error.message}")
                latch.countDown()
            }
        })

        // Wait for Firebase query to complete (max 5 seconds)
        try {
            latch.await(5, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Log.w("ExpiryCheck", "Timeout waiting for Firebase query")
        }
    }

    private fun showGenericReminder(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val mainIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            mainIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Kitcheneering Reminder 🍳")
            .setContentText("Did you buy or eat something? Don't forget to update your kitchen!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        Log.d("ReminderReceiver", "Showing generic reminder notification")
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}

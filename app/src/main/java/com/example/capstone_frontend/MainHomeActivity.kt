package com.example.capstone_frontend

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main_home.*
import java.io.IOException
import io.socket.client.IO


class MainHomeActivity : AppCompatActivity() {
    lateinit var mSocket: Socket // for socket
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_home)

        setPermission()
        getToken()

        val type = intent.getStringExtra("type")
        val nickName = intent.getStringExtra("nickName")

        btn_profile.setOnClickListener {
            val profileIntent = Intent(this, MyProfileActivity::class.java)
            profileIntent.putExtra("type", type)
            profileIntent.putExtra("nickName", nickName)
            startActivity(profileIntent)
        }

        btn_friendlist.setOnClickListener {
            val friendListIntent = Intent(this, FriendListActivity::class.java)
            startActivity(friendListIntent)
        }

        btn_snaptalk.setOnClickListener {
            val snapTalkIntent = Intent(this, SnaptalkActivity::class.java)
            startActivity(snapTalkIntent)
        }

        btn_emergency_call.setOnClickListener {
            try {
                var callIntent = Intent(Intent.ACTION_CALL)
                callIntent.setData(Uri.parse("tel:119"))
                startActivity(callIntent)
            } catch (e: Exception) {
                Log.d("call", "전화 실패")   
            }
        }

        /*Push messages
        val test: MyFirebaseInstanceIDService = MyFirebaseInstanceIDService()
        test.onTokenRefresh()
        Log.d(
            "FCM Message TEST",
            "REFRESHED TOKEN IS RIGHT ABOVE !!!"
        )*/

            val channelId = "Dangerous situation"
            val name = "위험 상황 탐지"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(channelId, name, importance)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)


    }

    private fun setPermission() {
        val permission = object : PermissionListener {
            override fun onPermissionGranted() {
                // Toast.makeText(this@MainHomeActivity, "권한이 허용 되었습니다.", Toast.LENGTH_SHORT).show()
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) { // 설정해놓은 위험 권한들 중 거부한 경우 수행
                Toast.makeText(this@MainHomeActivity, "권한이 거부 되었습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permission)
            .setRationaleMessage("긴급 전화를 사용하시려면 권한을 허용해주세요.")
            .setDeniedMessage("권한을 거부하셨습니다. [앱 설정] -> [권한] 항목에서 허용해주세요.")
            .setPermissions(
                android.Manifest.permission.CALL_PHONE
            )
            .check()
    }

    private fun getToken() {
        //val db: DatabaseReference = Firebase.database.getReference("users")

        Thread(Runnable {
            try {
                FirebaseInstanceId.getInstance().instanceId
                        .addOnCompleteListener(OnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.i("로그 : ", "getInstanceId failed", task.exception)
                                return@OnCompleteListener
                            }
                            val token = task.result?.token.toString()

                            //db.child(id).child("token").setValue(token)
                    try {
                        mSocket = IO.socket("http://10.0.2.2:80")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("fail", "Failed to connect")
                    }
                    mSocket.connect()
                    var test = "for testing"
                    mSocket.emit("token", gson.toJson(TokenItem(token,"")))
                    Log.d(
                            "TOKEN", " added to server"
                    )
                        })
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }).start()
    }

}
package com.example.calllogs

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.CallLog
import android.provider.Telephony
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.Long
import java.util.*


class MainActivity : AppCompatActivity() {

    val READ_CALL_LOG_CODE = 101
    val READ_SMS_LOG_CODE = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPermissions()
        setContentView(R.layout.activity_main)
    }

    private fun getCallDetails() {
        val sb = StringBuffer()
        val managedCursor: Cursor = managedQuery(CallLog.Calls.CONTENT_URI, null, null, null, null)
        val number: Int = managedCursor.getColumnIndex(CallLog.Calls.NUMBER)
        val type: Int = managedCursor.getColumnIndex(CallLog.Calls.TYPE)
        val date: Int = managedCursor.getColumnIndex(CallLog.Calls.DATE)
        val duration: Int = managedCursor.getColumnIndex(CallLog.Calls.DURATION)
        sb.append("Call Details :")
        while (managedCursor.moveToNext()) {
            val phNumber: String = managedCursor.getString(number)
            val callType: String = managedCursor.getString(type)
            val callDate: String = managedCursor.getString(date)
            val callDayTime = Date(Long.valueOf(callDate))
            val callDuration: String = managedCursor.getString(duration)
            var dir: String? = null
            val dircode = callType.toInt()
            when (dircode) {
                CallLog.Calls.OUTGOING_TYPE -> dir = "OUTGOING"
                CallLog.Calls.INCOMING_TYPE -> dir = "INCOMING"
                CallLog.Calls.MISSED_TYPE -> dir = "MISSED"
            }
            sb.append("\nPhone Number:--- $phNumber \nCall Type:--- $dir \nCall Date:--- $callDayTime \nCall duration in sec :--- $callDuration")
            sb.append("\n----------------------------------")
        }
        managedCursor.close()
        Log.d("CALL LOGS", sb.toString())
    }

    private fun getSmsDetails() {
        getSmsConversation(this){ conversations ->
            val sb = StringBuffer()
            conversations?.forEach { conversation ->
                sb.append("\nNumber: ${conversation.number}\n Message One: ${conversation.message[0].body}\n")
                sb.append("\n----------------------------------")
                Log.d("MESSAGE", sb.toString())
            }
        }
    }

    private fun setupPermissions() {
        //val permission_call_log = ContextCompat.checkSelfPermission(this,
            //Manifest.permission.READ_CALL_LOG)
        val permission_sms_log = ContextCompat.checkSelfPermission(this,
            Manifest.permission.READ_SMS)

//        if (permission_call_log != PackageManager.PERMISSION_GRANTED) {
//            //makeRequestCall()
//        }
//        else{
//            //getCallDetails()
//        }

        if (permission_sms_log != PackageManager.PERMISSION_GRANTED) {
            makeRequestSms()
        }
        else {
            getSmsDetails()
        }
    }



    private fun makeRequestCall() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_CALL_LOG),
            READ_CALL_LOG_CODE)
    }

    private fun makeRequestSms() {
        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_SMS),
            READ_SMS_LOG_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_CALL_LOG_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i("TAG", "Permission to read call logs has been denied by user")
                } else {
                    Log.i("TAG", "Permission to read call logs has been granted by user")
                }
            }

            READ_SMS_LOG_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i("TAG", "Permission to read sms has been denied by user")
                } else {
                    Log.i("TAG", "Permission to read sms has been granted by user")
                }
            }


        }
    }


    fun getSmsConversation(context: Context, number: String? = null, completion: (conversations: List<Conversation>?) -> Unit) {
        val managedCursor: Cursor = managedQuery(Telephony.Sms.CONTENT_URI, null, null, null, null)

        val numbers = ArrayList<String>()
        val messages = ArrayList<Message>()
        var results = ArrayList<Conversation>()

        Log.d("NUMBERS", "$numbers")
        Log.d("MESSAGES", "$messages")


        while (managedCursor != null && managedCursor.moveToNext()) {
            val smsDate = managedCursor.getString(managedCursor.getColumnIndexOrThrow(Telephony.Sms.DATE))
            val number = managedCursor.getString(managedCursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
            val body = managedCursor.getString(managedCursor.getColumnIndexOrThrow(Telephony.Sms.BODY))

            numbers.add(number)
            messages.add(Message(number, body, Date(smsDate.toLong())))
        }

        managedCursor.close()

        numbers.forEach { number ->
            if (results.find { it.number == number } == null) {
                val msg = messages.filter { it.number == number }
                results.add(Conversation(number = number, message = msg))
            }
        }

        if (number != null) {
            results = results.filter { it.number == number } as ArrayList<Conversation>
        }

        completion(results)
    }

}

class Conversation(val number: String, val message: List<Message>)
class Message(val number: String, val body: String, val date: Date)


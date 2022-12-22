package com.example.voicerecorder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.Permission
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest
import java.util.logging.SimpleFormatter


val REQUEST_CODE = 200

class MainActivity : AppCompatActivity(), OnTimerTickListener {

    private var permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
    private var permission_granted = false

    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var fileName = ""

    private var isPaused = false
    private var isPlaying = false
    private var duration = ""

    private lateinit var timer: Timer
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        permission_granted = ActivityCompat.checkSelfPermission(this, permissions[0]) === PackageManager.PERMISSION_GRANTED

        if(!permission_granted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }

        db = Room.databaseBuilder(this, AppDatabase::class.java, "audioRecords").build()

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        timer = Timer(this)

        btnRecord.setOnClickListener {
            when {
                isPaused -> resumeRecording()
                isPlaying -> pauseRecording()
                else -> startRecording()
            }
            if(isPlaying) {
                Toast.makeText(this, "Started Recording", Toast.LENGTH_SHORT).show()
            }
            else if(isPaused) {
                Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show()
            }

            else {
                Toast.makeText(this, "Stop Recording", Toast.LENGTH_SHORT).show()
            }
        }

        btnDelete.setOnClickListener {
            stopRecording()
            File("$dirPath$fileName.mp3").delete()
            Toast.makeText(this, "Record Deleted", Toast.LENGTH_SHORT).show()
        }

        btnList.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }

        btnDone.setOnClickListener {
            stopRecording()

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            bottomSheetBG.visibility = View.VISIBLE
            filenameInput.setText(fileName)
        }

        btnCancel.setOnClickListener {
            File("$dirPath$fileName.mp3").delete()
            dismiss()
        }

        bottomSheetBG.setOnClickListener {
            dismiss()
        }

        btnOk.setOnClickListener {
            Toast.makeText(this, "Record Saved", Toast.LENGTH_SHORT).show()
            save()
            dismiss()
        }
    }

    private fun dismiss() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheetBG.visibility = View.GONE
        hideKeyboard(filenameInput)
    }

    private fun save() {
        val newFileName = filenameInput.text.toString()

        if(newFileName != fileName) {
            var newFile = File("$dirPath$newFileName.mp3")
            File("$dirPath$fileName.mp3").renameTo(newFile)

        }

        val filePath = "$dirPath$newFileName.mp3"
        var timestamp = Date().time

        var record = AudioRecord(fileName, filePath, timestamp, this.duration)
        GlobalScope.launch {
            db.audioRecordDao().addRecord(record)
        }

        Log.d("SAVING", "$record")

    }

    private fun hideKeyboard(view: View) {
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

    }

    private fun pauseRecording() {
        isPaused = true
        isPlaying = false
        recorder.pause()
        btnRecord.setImageResource(R.drawable.ic_play)
        timer.pause()
    }

    private fun resumeRecording() {
        isPaused = false
        isPlaying = true
        recorder.resume()
        btnRecord.setImageResource(R.drawable.ic_pause)
        timer.start()
    }

    private fun stopRecording() {

        if (!isPlaying && !isPaused) { return }

        isPaused = false
        isPlaying = false

        recorder.apply {
            stop();     // stop recording
            reset();    // set state to idle
            release();  // release resources back to the system
        }

        btnRecord.setImageResource(R.drawable.ic_play)

        btnDone.visibility = View.GONE
        btnList.visibility = View.VISIBLE

        tvTimer.text = "00:00:00"

        timer.stop()

    }




    fun startRecording() {
        if(!permission_granted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
        }

        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"

        val dateFormatter = SimpleDateFormat("yyyy.MM.DD_hh.mm.ss")
        val date = dateFormatter.format(Date())

        fileName = "audio_record${date}"

        timer.start()

        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$fileName.mp3")

            try {
                prepare()
            } catch(e: IOException) { }

            start()
        }

        btnRecord.setImageResource(R.drawable.ic_pause)

        isPlaying = true
        isPaused = false

        btnDone.visibility = View.VISIBLE
        btnList.visibility = View.GONE

    }

    override fun onTimerClick(duration: String) {

        tvTimer.text = timer.formatTime()
        this.duration = duration
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == REQUEST_CODE) {
            permission_granted = grantResults[0] === PackageManager.PERMISSION_GRANTED
        }
    }


}
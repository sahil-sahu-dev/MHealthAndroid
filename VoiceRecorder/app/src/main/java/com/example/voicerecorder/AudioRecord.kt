package com.example.voicerecorder

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.Duration

@Entity(tableName = "audioRecords")
data class AudioRecord(
    var fileName: String,
    var filePath: String,
    var timestamp: Long,
    var duration: String
) {
    @PrimaryKey(autoGenerate = true)
    var id = 0

    @Ignore
    var isChecked = false

}
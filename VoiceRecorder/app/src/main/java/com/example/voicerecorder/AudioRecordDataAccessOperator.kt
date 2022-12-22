package com.example.voicerecorder

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query


@Dao
interface AudioRecordDataAccessOperator {
    @Query("SELECT * FROM audioRecords")
    fun getAll(): List<AudioRecord>

    @Insert
    fun addRecord(vararg record: AudioRecord)

    @Delete
    fun deleteRecord(record: AudioRecord)



}
package com.example.voicerecorder

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Adapter(var records: ArrayList<AudioRecord>, var listener: OnItemClickListener): RecyclerView.Adapter<com.example.voicerecorder.Adapter.ViewHolder>() {

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var fileName: TextView = itemView.findViewById(R.id.tvFileName)
        var timestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition



            if(position != RecyclerView.NO_POSITION) {
                listener.onItemClickListener(position)

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.itemview_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position != RecyclerView.NO_POSITION) {
            var record = records[position]

            val simpleFormatter = SimpleDateFormat("dd/MM/yyyy")
            var date = Date(record.timestamp)
            var date_string = simpleFormatter.format(date)

            holder.fileName.text = record.fileName
            holder.timestamp.text = "${record.duration} $date_string"
        }
    }

    override fun getItemCount(): Int {
        return records.size
    }

}
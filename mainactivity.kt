package com.example.tcp_prot

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.experimental.xor

class MainActivity : AppCompatActivity() {
    val tag = "tcp_stt"
    var framed_data = ""
    private lateinit var mService: tcp_handler
    private var mBound: Boolean = false
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as tcp_handler.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mic.setOnClickListener {
            checkAudioPermission()
            speech_to_text()
        }
        send.setOnClickListener {
            if ("transmit" in message.text.toString()){
                packet_creator()
            }
        }
        connect.setOnClickListener {
            mService.create_socket(ip_address.text.toString(),port.text.toString().toInt())
        }
    }
    fun speech_to_text(){
        var speech = SpeechRecognizer.createSpeechRecognizer(this)
        var speech_intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speech_intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )
        speech_intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault()
        )
        speech.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(p0: Bundle?) {
                mic.setBackgroundColor(Color.GREEN)
            }

            override fun onBeginningOfSpeech() {
//                Log.d(tag,"on begning of speech")
            }

            override fun onRmsChanged(p0: Float) {

            }

            override fun onBufferReceived(p0: ByteArray?) {
//                Log.d(tag,"on buffered received :${p0}")
            }

            override fun onEndOfSpeech() {
//                Log.d(tag,"on end of speech")
                mic.setBackgroundColor(Color.RED)
            }

            override fun onError(p0: Int) {
                Log.d(tag,"error : ${p0}")

            }

            override fun onResults(text: Bundle?) {
                val result = text?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                var lower_case = mService.upper_to_lower(result.toString())
                message.setText(lower_case)
                data_handle(lower_case)
            }

            override fun onPartialResults(p0: Bundle?) {
                Log.d(tag,"partial result :${p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)}")
            }

            override fun onEvent(p0: Int, p1: Bundle?) {
                Log.d(tag,"on event ${p0}, bundle ${p1}")
//                val result = p1?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
//                text_vw.setText(result.toString())
            }
        })
        Log.d(tag,"start listerning")
        speech.startListening(speech_intent)
    }
    private fun checkAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {  // M = 23
            if (ContextCompat.checkSelfPermission(this, "android.permission.RECORD_AUDIO") != PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.programmingtech.offlinespeechtotext"))
                startActivity(intent)
                Toast.makeText(this, "Allow Microphone Permission", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onStart() {
        super.onStart()
        // Bind to LocalService
        Intent(this, tcp_handler::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }
    override fun onStop() {
        super.onStop()
        unbindService(connection)
        mBound = false
    }
    fun data_handle(data:String){
        Log.d(tag,"entered data_handle()")
        if ("create socket" in data.toString()){
            if (ip_address.text.toString() == ""){
                Log.d(tag,"enter the IP address")
            }
            if (port.text.toString() == ""){
                Log.d(tag,"enter the port number")
            }
            else{
                var ipaddress = ip_address.text.toString()
                var port = port.text.toString().toInt()
                var result = mService.create_socket(ipaddress,port)
                Log.d(tag,"not entered the loop : ${result}")
            }
        }
        else if ("socket status" in data){
            message.setText("status :${mService.s?.isConnected}")
        }
        else if("send data" in data){
            mService.send_data1("aa55".toByteArray())
        }
    }

    fun packet_creator(){
        var data = 170000000.toString(16).toByteArray()
        var packet_Name = "transmitter frequency configuration"
        var packet_Header  = 0xaa55.toString(16)
        var packet_ID  = ""
        var packet_Response = "0000"
        var data_Length = "0008"
        var packet_data_field = data
        if (packet_Name == "transmitter frequency configuration"){
            packet_ID = "5200"
        }
        var checksum = 0x52 xor 0x00 xor 0x00 xor 0x08
        mService.send_data1(packet_Header.toString().toByteArray())
    }
}

package com.example.tcp_prot

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import java.io.OutputStream
import java.lang.Exception
import java.net.Socket
import java.nio.charset.Charset
import java.util.*

open class tcp_handler : Service() {
    var tag = "tcp_ip"
    private val binder = LocalBinder()
    var socket_created: Boolean = false
    var connection_status: Boolean = false
    open lateinit var s :Socket
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): tcp_handler = this@tcp_handler
    }
    override fun onBind(p0: Intent?): IBinder? {
        return binder
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var data = intent?.getStringExtra("speech")
        if (data == "who is suresh"){
            intent?.putExtra("speech","LEAGEND")
        }
        return START_STICKY
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }
    fun upper_to_lower(data :String):String{
        var lower = data.toString().toLowerCase()
        Log.d(tag,lower)
        return lower
    }
    fun create_socket(ipaddress:String,port:Int):String{
        var result = ""
        Log.d(tag,"socket creation started")
        if ((socket_created == false)){
            CoroutineScope(Dispatchers.IO).launch {
                withTimeoutOrNull(200){
                    try {
                        CoroutineScope(Dispatchers.Default).launch {
                            Log.d(tag,"before socket creation")
                        }
                        s = Socket(ipaddress,port)
                        CoroutineScope(Dispatchers.Default).launch {
                            Log.d(tag,"after socket creation")
                        }
                        CoroutineScope(Dispatchers.Default).launch {
                            Log.d(tag,"Socket created")
                            Log.d(tag,"Socket status : ${s?.isConnected}")
                        }
                        socket_created = true
                        connection_status = s?.isConnected
                        if (connection_status){
                            rec_tcp_data()
                        }
                        result = "socket created successfully"
                    }
                    catch (ex: Exception){
                        result = "failed to create socket with error message : ${ex.message}"
                    }
                }
            }
            CoroutineScope(Dispatchers.Default).launch {
                Log.d(tag,"socket creation status : ${result}")
            }
        }
        else if(socket_created == true){
            result = "socket already created"
        }
        return result
    }
    fun rec_tcp_data(){
        CoroutineScope(Dispatchers.IO).async{
            var scanner = Scanner(s?.getInputStream())
            while (connection_status) {
                try {
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d(tag, "starting to received data")
                    }
                    var data = scanner.nextLine().toString()
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d(tag, "scanner : ${data}")
                        Log.d(tag, "received next line")
                    }
                } catch (ex: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Log.d(tag, ex.message.toString())
                        connection_status = false
                        Log.d(tag, "receiver time out")
                    }
                }
            }
        }
    }
    fun send_data1(data:ByteArray){
        CoroutineScope(Dispatchers.IO).launch {
            var send:OutputStream = s?.getOutputStream()
            send.write(data,0,1)
        }

    }
}

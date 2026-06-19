package com.audioshiftpro.app

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@CapacitorPlugin(name = "FFmpegConverter")
class FFmpegPlugin : Plugin() {

    private var progressCall: PluginCall? = null

    @PluginMethod
    fun convert(call: PluginCall) {
        val command = call.getString("command")
        if (command.isNullOrEmpty()) {
            call.reject("Missing command parameter")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Enable statistics for progress tracking
                FFmpegKitConfig.enableStatisticsCallback { stats ->
                    val pct = stats.time  // milliseconds processed
                    notifyListeners("ffmpegProgress", JSObject().apply {
                        put("time", pct)
                        put("size", stats.size)
                        put("bitrate", stats.bitrate)
                        put("speed", stats.speed)
                    })
                }

                val session = FFmpegKit.execute(command)
                val ret = JSObject()

                if (ReturnCode.isSuccess(session.returnCode)) {
                    ret.put("success", true)
                    ret.put("returnCode", 0)
                } else if (ReturnCode.isCancel(session.returnCode)) {
                    ret.put("success", false)
                    ret.put("error", "Cancelled")
                } else {
                    ret.put("success", false)
                    ret.put("error", session.failStackTrace ?: "FFmpeg failed with rc=${session.returnCode}")
                    ret.put("logs", session.allLogsAsString)
                }
                call.resolve(ret)
            } catch (e: Exception) {
                call.reject("FFmpeg error: ${e.message}")
            }
        }
    }

    @PluginMethod
    fun cancel(call: PluginCall) {
        FFmpegKit.cancel()
        call.resolve()
    }

    @PluginMethod
    fun getVersion(call: PluginCall) {
        val ret = JSObject()
        ret.put("version", com.arthenica.ffmpegkit.BuildConfig.VERSION)
        call.resolve(ret)
    }
}

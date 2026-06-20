package com.audioshiftpro.app

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

    @PluginMethod
    fun convert(call: PluginCall) {
        val command = call.getString("command")
        if (command.isNullOrEmpty()) {
            call.reject("Missing command parameter")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                FFmpegKitConfig.enableStatisticsCallback { stats ->
                    notifyListeners("ffmpegProgress", JSObject().apply {
                        put("time", stats.time)
                        put("size", stats.size)
                        put("speed", stats.speed)
                    })
                }

                val session = FFmpegKit.execute(command)
                val ret = JSObject()

                when {
                    ReturnCode.isSuccess(session.returnCode) -> {
                        ret.put("success", true)
                    }
                    ReturnCode.isCancel(session.returnCode) -> {
                        ret.put("success", false)
                        ret.put("error", "Cancelled")
                    }
                    else -> {
                        ret.put("success", false)
                        ret.put("error", session.failStackTrace ?: "FFmpeg failed rc=${session.returnCode}")
                        ret.put("logs", session.allLogsAsString)
                    }
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
}

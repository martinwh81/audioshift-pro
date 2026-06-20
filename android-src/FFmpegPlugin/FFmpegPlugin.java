package com.audioshiftpro.app;

import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegKitConfig;
import com.arthenica.ffmpegkit.ReturnCode;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "FFmpegConverter")
public class FFmpegPlugin extends Plugin {

    @PluginMethod
    public void convert(PluginCall call) {
        String command = call.getString("command");
        if (command == null || command.isEmpty()) {
            call.reject("Missing command parameter");
            return;
        }

        final String cmd = command;
        new Thread(() -> {
            try {
                FFmpegKitConfig.enableStatisticsCallback(stats -> {
                    JSObject progress = new JSObject();
                    progress.put("time", stats.getTime());
                    progress.put("size", stats.getSize());
                    progress.put("speed", stats.getSpeed());
                    notifyListeners("ffmpegProgress", progress);
                });

                com.arthenica.ffmpegkit.FFmpegSession session = FFmpegKit.execute(cmd);
                JSObject ret = new JSObject();

                if (ReturnCode.isSuccess(session.getReturnCode())) {
                    ret.put("success", true);
                } else if (ReturnCode.isCancel(session.getReturnCode())) {
                    ret.put("success", false);
                    ret.put("error", "Cancelled");
                } else {
                    ret.put("success", false);
                    ret.put("error", session.getFailStackTrace() != null ? session.getFailStackTrace() : "FFmpeg failed rc=" + session.getReturnCode());
                    ret.put("logs", session.getAllLogsAsString());
                }
                call.resolve(ret);
            } catch (Exception e) {
                call.reject("FFmpeg error: " + e.getMessage());
            }
        }).start();
    }

    @PluginMethod
    public void cancel(PluginCall call) {
        FFmpegKit.cancel();
        call.resolve();
    }
}

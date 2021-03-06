package com.pecpwee.lib.envmock;

import android.content.Context;

import com.pecpwee.lib.envmock.recorder.AbsRecorder;
import com.pecpwee.lib.envmock.recorder.RecordListener;
import com.pecpwee.lib.envmock.recorder.connect.ConnRecorder;
import com.pecpwee.lib.envmock.recorder.location.GpsRecorder;
import com.pecpwee.lib.envmock.recorder.telephony.TelephonyRecorder;
import com.pecpwee.lib.envmock.recorder.wifi.WifiRecorder;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by pw on 2017/8/19.
 */

public class RecordController {
    HashMap<String, ModulePack> mModuleStateMap;
    boolean isRecording = false;

    public RecordController(Context context) {

        if (!EnvMockInstaller.isRecordServiceInstalled) {
            throw new RuntimeException("you should installed the envMock service first");
        }

        mModuleStateMap = new HashMap<>();
        mModuleStateMap.put(Context.CONNECTIVITY_SERVICE, new ModulePack(true, new ConnRecorder()));
        mModuleStateMap.put(Context.WIFI_SERVICE, new ModulePack(true, new WifiRecorder()));
        mModuleStateMap.put(Context.LOCATION_SERVICE, new ModulePack(true, new GpsRecorder()));
        mModuleStateMap.put(Context.TELEPHONY_SERVICE, new ModulePack(true, new TelephonyRecorder(context)));
    }

    public void addRecordListener(RecordListener listener) {
        for (ModulePack pack : mModuleStateMap.values()) {
            pack.recorder.addRecordListener(listener);
        }
    }

    public void removeRecordListener(RecordListener listener) {
        for (ModulePack pack : mModuleStateMap.values()) {
            pack.recorder.removeRecordListener(listener);
        }
    }


    public synchronized void start() {
        if (isRecording) {
            throw new RuntimeException("you have started a recording");
        }
        Collection<ModulePack> modulePacks = mModuleStateMap.values();
        for (ModulePack modulePack : modulePacks) {
            if (modulePack.isEnable) {
                modulePack.recorder.start();
            }
        }
        isRecording = true;
    }

    public synchronized void stop() {
        if (!isRecording) {
            throw new RuntimeException("there is no record action are executing");
        }
        Collection<ModulePack> modulePacks = mModuleStateMap.values();
        for (ModulePack modulePack : modulePacks) {
            if (modulePack.isEnable) {
                modulePack.recorder.stop();
            }
        }

        isRecording = false;
    }


    public synchronized void setSampleInterval(long sampleInterval) {
        ensureNoRecording();
        RecordConfig.getInstance().setSampleInterval(sampleInterval);
    }

    public synchronized void setWifiRecordCountUpperLimit(int countLimit) {
        ensureNoRecording();
        RecordConfig.getInstance().setWifiScanUpperCount(countLimit);
    }

    public synchronized RecordController setWifiEnable(boolean isEnable) {
        ensureNoRecording();
        mModuleStateMap.get(Context.WIFI_SERVICE).isEnable = isEnable;
        return this;
    }

    public synchronized RecordController setGpsEnable(boolean isEnable) {
        ensureNoRecording();
        mModuleStateMap.get(Context.LOCATION_SERVICE).isEnable = isEnable;
        return this;

    }

    public synchronized RecordController setCellEnable(boolean isEnable) {
        ensureNoRecording();
        mModuleStateMap.get(Context.TELEPHONY_SERVICE).isEnable = isEnable;
        return this;
    }


    public synchronized RecordController setGpsRecordFilePath(String filepath) {
        ensureNoRecording();
        mModuleStateMap.get(Context.LOCATION_SERVICE).recorder.setFilePath(filepath);
        return this;
    }

    public RecordController setWifiRecordFilePath(String filepath) {
        ensureNoRecording();
        mModuleStateMap.get(Context.WIFI_SERVICE).recorder.setFilePath(filepath);

        return this;

    }

    public RecordController setCellRecordFilePath(String filepath) {
        ensureNoRecording();
        mModuleStateMap.get(Context.TELEPHONY_SERVICE).recorder.setFilePath(filepath);

        return this;
    }

    public RecordController setConnRecordFilePath(String filepath) {
        ensureNoRecording();
        mModuleStateMap.get(Context.CONNECTIVITY_SERVICE).recorder.setFilePath(filepath);

        return this;
    }

    private void ensureNoRecording() {
        if (isRecording) {
            throw new RuntimeException("you should stop the record before setting ");
        }
    }

    private static class ModulePack {
        public boolean isEnable;
        public AbsRecorder recorder;

        public ModulePack(boolean isEnable, AbsRecorder recorder) {
            this.isEnable = isEnable;
            this.recorder = recorder;
        }
    }

}

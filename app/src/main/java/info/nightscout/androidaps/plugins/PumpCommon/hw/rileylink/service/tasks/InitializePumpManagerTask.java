package info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.tasks;

import android.util.Log;

import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkConst;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.RileyLinkUtil;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.defs.RileyLinkError;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.defs.RileyLinkServiceState;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.defs.RileyLinkTargetDevice;
import info.nightscout.androidaps.plugins.PumpCommon.hw.rileylink.service.data.ServiceTransport;
import info.nightscout.androidaps.plugins.PumpMedtronic.util.MedtronicConst;
import info.nightscout.utils.SP;

/**
 * Created by geoff on 7/9/16.
 * <p>
 * This class is intended to be run by the Service, for the Service. Not intended for clients to run.
 */
public class InitializePumpManagerTask extends ServiceTask {

    private static final String TAG = "InitPumpManagerTask";
    private RileyLinkTargetDevice targetDevice;


    public InitializePumpManagerTask(RileyLinkTargetDevice targetDevice) {
        super();
        this.targetDevice = targetDevice;
    }


    public InitializePumpManagerTask(ServiceTransport transport) {
        super(transport);
    }


    @Override
    public void run() {

        double lastGoodFrequency = SP.getDouble(RileyLinkConst.Prefs.LastGoodDeviceFrequency, 0.0d);
        lastGoodFrequency = Math.round(lastGoodFrequency * 1000d) / 1000d;

        RileyLinkUtil.getRileyLinkServiceData().lastGoodFrequency = lastGoodFrequency;

        if (RileyLinkUtil.getRileyLinkTargetFrequency() == null) {
            String pumpFrequency = SP.getString(MedtronicConst.Prefs.PumpFrequency, null);

        }

        if ((lastGoodFrequency > 0.0d)
            && RileyLinkUtil.getRileyLinkCommunicationManager().isValidFrequency(lastGoodFrequency)) {

            RileyLinkUtil.setServiceState(RileyLinkServiceState.RileyLinkReady);

            Log.i(TAG, String.format("Setting radio frequency to %.2fMHz", lastGoodFrequency));
            RileyLinkUtil.getRileyLinkCommunicationManager().setRadioFrequencyForPump(lastGoodFrequency);

            boolean foundThePump = RileyLinkUtil.getRileyLinkCommunicationManager().tryToConnectToDevice();

            if (foundThePump) {
                RileyLinkUtil.setServiceState(RileyLinkServiceState.PumpConnectorReady);
                // RileyLinkUtil.sendNotification(new ServiceNotification(RT2Const.IPC.MSG_PUMP_pumpFound), null);
            } else {
                RileyLinkUtil.setServiceState(RileyLinkServiceState.PumpConnectorError,
                    RileyLinkError.NoContactWithDevice);
                RileyLinkUtil.sendBroadcastMessage(RileyLinkConst.IPC.MSG_PUMP_tunePump);
                // RileyLinkUtil.sendNotification(new ServiceNotification(RT2Const.IPC.MSG_PUMP_pumpLost), null);
            }

            // RileyLinkUtil.sendNotification(new ServiceNotification(RT2Const.IPC.MSG_note_Idle), null);
        } else {
            RileyLinkUtil.sendBroadcastMessage(RileyLinkConst.IPC.MSG_PUMP_tunePump);
        }
    }
}
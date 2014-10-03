package radio;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import se.bitcraze.fluff.UsbLinkAndroid;
import se.bitcraze.crazyflie.lib.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.CrazyradioLink;
import se.bitcraze.crazyflie.lib.DataAdapter;
import se.bitcraze.crazyflie.lib.Link;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.crtp.Port;
import se.bitcraze.crazyflie.lib.crtp.log.Log;
import se.bitcraze.crazyflie.lib.crtp.log.LogConfig;
import se.bitcraze.crazyflie.lib.crtp.log.LogData;
import se.bitcraze.crazyflie.lib.crtp.log.LogVariable;
import se.bitcraze.crazyflie.lib.crtp.toc.TableOfContents;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import java.util.Random;
import java.util.Formatter;
import java.nio.ByteBuffer;
import se.bitcraze.crazyflie.lib.crtp.VariableType;
import se.bitcraze.crazyflie.lib.crtp.CrtpPacket;
import se.bitcraze.crazyflie.lib.crtp.toc.TocItem;

/**
 * This class echoes a string called from JavaScript.
 */
public class Radio extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("connect")) {
            connect(args, callbackContext);
            return true;
        } else if (action.equals("updateOrientation")) {
            updateOrientation(args, callbackContext);
            return true;
        }
        return false;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        this.cordova = cordova;
        CordovaActivity activity = (CordovaActivity)cordova.getActivity();
        Context context = activity.getContext();
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        context.registerReceiver(mUsbReceiver, filter);

        _pitch = 0;
        _roll = 0;
        _yawRate = 0;
        _thrust = 0;
        _crazyradioUpdateThread = null;
    }

    private float _pitch;
    private float _roll;
    private float _yawRate;
    private float _thrust;
    private Link _crazyradioLink;
    private Thread _crazyradioUpdateThread;

    private void connect(JSONArray args, CallbackContext callbackContext) {
        android.util.Log.i("RADIO", "CONNECT CALLED");
        TableOfContents logToc = new TableOfContents(Port.LOG);
        linkConnect();

        //request TOC CRC
        logToc.verify(_crazyradioLink, true);

        //wait until all TOC data has been received
        long startTime = System.currentTimeMillis();
        while(!logToc.isTocFetchFinished(_crazyradioLink) && System.currentTimeMillis()-startTime < 3000){
            try {
                Thread.sleep(400);
            } catch (Exception e) {
            }
        }
        if(!logToc.isTocFetchFinished(_crazyradioLink)){
            callbackContext.error("Failed to fetch toc.");
        }

        final Log log = new Log(_crazyradioLink, logToc);
        _crazyradioLink.addDataListener(new DataAdapter() {
            @Override
            public void logDataReceived(LogData packet) {
                Map<String, Number> logVariables = log.parseLogVariables(packet);
                String yaw = Short.toString(logVariables.get("stabilizer.yaw").shortValue());

                executeJavascript("if(newCopterOrientation)newCopterOrientation(" + yaw + ");");
            }
        });
        TocItem yawItem = logToc.getItem("stabilizer.yaw");
        LogConfig config = log.createLogConfig(100);
        config.addLogVariable("stabilizer.yaw", VariableType.INT32_T, yawItem.getId());
        int logId = log.addLogConfiguration(config);
        log.startLogConfiguration(logId);

        _crazyradioUpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (_crazyradioLink != null) {
                        char thrust = (char)(int)(_thrust * 65535);
                        _crazyradioLink.send(new CommanderPacket(_roll, _pitch, _yawRate, thrust, false));
                        Thread.sleep(20, 0);
                    }
                } catch(Exception e) {
                }
            }
        });

        _crazyradioUpdateThread.start();

        callbackContext.success();
    }

    private void updateOrientation(JSONArray args, CallbackContext callbackContext) {
        try {
            _pitch = (float)args.getDouble(0);
            _roll = (float)args.getDouble(1);
            _yawRate = (float)args.getDouble(2);
            _thrust = (float)args.getDouble(3);
            callbackContext.success();
        } catch(Exception e) {
            
        }
    }

    private void executeJavascript(String js) {
        if(cordova == null) {
            return;
        }

        final CordovaActivity activity = (CordovaActivity)cordova.getActivity();
        if(activity != null) {
            activity.sendJavascript(js);
        }
    }

    private void echo(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) { 
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && UsbLinkAndroid.isCrazyradio(device)) {
                    if (_crazyradioLink != null) {
                        linkDisconnect();
                    }
                }
            }
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null && UsbLinkAndroid.isCrazyradio(device)) {
                }
            }
        }
    };

    private void linkConnect() {
        // ensure previous link is disconnected
        linkDisconnect();

        int radioChannel = 10;
        int radioDatarate =  0;

        final CordovaActivity activity = (CordovaActivity)cordova.getActivity();
        final Context context = activity.getContext();

        try {
            // create link
            _crazyradioLink = new CrazyradioLink(new UsbLinkAndroid(context));

            // add listener for connection status
            _crazyradioLink.addConnectionListener(new ConnectionAdapter() {
                @Override
                public void connectionSetupFinished(Link l) {
                }

                @Override
                public void connectionLost(Link l) {
                    linkDisconnect();
                }

                @Override
                public void connectionFailed(Link l) {
                    linkDisconnect();
                }

                @Override
                public void linkQualityUpdate(Link l, final int quality) {
                }
            });
        } catch (IllegalArgumentException e) {
            Toast.makeText(context, "Crazyradio not attached", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        _crazyradioLink.connect(new CrazyradioLink.ConnectionData(radioChannel, radioDatarate));
    }

    private void linkDisconnect() {
        if (_crazyradioLink != null) {
            _crazyradioLink.disconnect();
            _crazyradioLink = null;
            _crazyradioUpdateThread.interrupt();
            _crazyradioUpdateThread = null;
        }
    }
}
package radio;

import java.io.IOException;

import se.bitcraze.fluff.UsbLinkAndroid;
import se.bitcraze.crazyflie.lib.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.CrazyradioLink;
import se.bitcraze.crazyflie.lib.Link;

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

/**
 * This class echoes a string called from JavaScript.
 */
public class Radio extends CordovaPlugin {
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("echo")) {
            String message = args.getString(0);
            linkConnect();
            callbackContext.success();
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
    }

    private Link mCrazyradioLink;

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
                    if (mCrazyradioLink != null) {
                        linkDisconnect();
                    }
                }
                //executeJavascriptInMain("test()");
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
            mCrazyradioLink = new CrazyradioLink(new UsbLinkAndroid(context));

            // add listener for connection status
            mCrazyradioLink.addConnectionListener(new ConnectionAdapter() {
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
        mCrazyradioLink.connect(new CrazyradioLink.ConnectionData(radioChannel, radioDatarate));
    }

    private void linkDisconnect() {
        if (mCrazyradioLink != null) {
            mCrazyradioLink.disconnect();
            mCrazyradioLink = null;
        }
    }
}
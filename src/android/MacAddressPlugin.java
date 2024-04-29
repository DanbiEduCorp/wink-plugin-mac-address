package com.badrit.MacAddress;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * The Class MacAddressPlugin.
 */
public class MacAddressPlugin extends CordovaPlugin {

    public static String TAG = "MacAddressPlugin";
    public boolean isSynch(String action) {
        if (action.equals("getMacAddress")) {
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.cordova.api.Plugin#execute(java.lang.String,
     * org.json.JSONArray, java.lang.String)
     */
    @Override
    public boolean execute(String action, JSONArray args,
            CallbackContext callbackContext) {

        if (action.equals("getMacAddress")) {

            String macAddress = this.getMacAddress();

            if (macAddress != null) {
                JSONObject JSONresult = new JSONObject();
                try {
                    JSONresult.put("mac", macAddress);
                    PluginResult r = new PluginResult(PluginResult.Status.OK,
                            JSONresult);
                    callbackContext.success(macAddress);
                    r.setKeepCallback(true);
                    callbackContext.sendPluginResult(r);
                    return true;
                } catch (JSONException jsonEx) {
                    PluginResult r = new PluginResult(
                            PluginResult.Status.JSON_EXCEPTION);
                    callbackContext.error("error");
                    r.setKeepCallback(true);
                    callbackContext.sendPluginResult(r);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the mac address.
     *
     * @return the mac address
     */
    private String getMacAddress() {

        if (Build.VERSION.SDK_INT >= 31) {
            String address = get12MacAddress();
            LOG.d(TAG, "in get12macaddress " + address);
            return address;
        } else if (Build.VERSION.SDK_INT >= 23) { // Build.VERSION_CODES.M
            return getMMacAddress();
        }

        return getLegacyMacAddress();

    }

    /**
     * Gets the mac address on version < Marshmallow.
     *
     * @return the mac address
     */
    private String getLegacyMacAddress() {

        String macAddress = null;

        WifiManager wm = (WifiManager) this.cordova.getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        macAddress = wm.getConnectionInfo().getMacAddress();

        if (macAddress == null || macAddress.length() == 0) {
            macAddress = "02:00:00:00:00:00";
        }

        return macAddress;

    }

    /**
     * Gets the mac address on version >= Marshmallow.
     *
     * @return the mac address
     */
    private String getMMacAddress() {

        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02x", (b & 0xFF)) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }

                return res1.toString();
            }
        } catch (Exception ex) { }

        return "02:00:00:00:00:00";
    }

    private String get12MacAddress() {
        String rst = "";
        try {
            Uri uri = Uri.parse("content://com.danbiedu.device.info.provider/mac_address");
            String[] columns = {"RETURN_VALUE"};
            Cursor cursor = webView.getContext().getContentResolver().query(uri, columns, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                String str = cursor.getString(cursor.getColumnIndexOrThrow("RETURN_VALUE"));
                rst = str;
                cursor.close();
            } else {
                LOG.i(TAG, " cursor is null");
            }
        } catch (Exception e) {
            LOG.i(TAG, " get12MacAddress error " + e.getMessage());
        }
        return rst;
    }
}

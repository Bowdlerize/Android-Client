package uk.bowdlerize.gcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import uk.bowdlerize.MainActivity;
import uk.bowdlerize.cache.LocalCache;
import uk.bowdlerize.service.CensorCensusService;

public class CCBroadcastReceiver extends BroadcastReceiver
{
    static final String TAG = "CCBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);

        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty())
        {
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType))
            {
                sendNotification("Send error: " + extras.toString());
            }
            else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType))
            {
                sendNotification("Deleted messages on server: " + extras.toString());

            }
            // If it's a regular GCM message, do some work.
            else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType))
            {
                Intent receiveURLIntent = new Intent(context, CensorCensusService.class);

                //Add our extra info
                if(context.getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE).getBoolean("sendISPMeta",true))
                {
                    WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                    TelephonyManager telephonyManager =((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));

                    if(wifiMgr.isWifiEnabled() && null != wifiInfo.getSSID())
                    {
                        LocalCache lc = null;
                        Pair<Boolean,String> seenBefore = null;
                        try
                        {
                            lc = new LocalCache(context);
                            lc.open();
                            seenBefore = lc.findSSID(wifiInfo.getSSID().replaceAll("\"",""));
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        if(null != lc)
                            lc.close();

                        if(seenBefore.first)
                        {
                            extras.putString("isp",seenBefore.second);
                        }
                        else
                        {
                            extras.putString("isp","unknown");
                        }

                        try
                        {
                            extras.putString("sim",telephonyManager.getSimOperatorName());
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        try
                        {
                            extras.putString("isp",telephonyManager.getNetworkOperatorName());
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                        try
                        {
                            extras.putString("sim",telephonyManager.getSimOperatorName());
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }

                receiveURLIntent.putExtras(extras);
                context.startService(receiveURLIntent);
            }

            setResultCode(Activity.RESULT_OK);
        }
    }

    private void sendNotification(String msg)
    {
        Log.e(TAG,msg);
    }
}

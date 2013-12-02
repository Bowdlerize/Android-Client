package uk.bowdlerize.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.Pair;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import uk.bowdlerize.MainActivity;
import uk.bowdlerize.R;

public class CensorCensusService extends Service
{
    public static int NOTIFICATION_ID = 9000;
    static int NOTIFICATION_ID_COMPLETE = 9001;
    static String INTENT_FILTER = "uk.bowdlerize.service.censorcensusservice.CANCEL";
    NotificationManager mNotifyManager;
    NotificationCompat.Builder mBuilder;
    Context mContext;
    boolean forceCancel = false;
    BroadcastReceiver bR;
    int checkedCount = 0;
    int censoredCount = 0;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId)
    {
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this);
        mContext = this;

        checkedCount = getPreferences(this).getInt("checkedCount", 0);
        censoredCount = getPreferences(this).getInt("censoredCount", 0);

        //If this is user submitted send it up for further research
        new Thread
        (
            new Runnable()
            {
                @Override
                public void run()
                {
                    if(intent.getBooleanExtra("local",false))
                        submitURL(intent.getStringExtra("url"));
                }
            }
        ).start();

        /*bR = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Log.e("bR", "On receive!");
                forceCancel = true;
            }
        };
        this.registerReceiver(bR,new IntentFilter(INTENT_FILTER));

        Intent cancelIntent = new Intent(INTENT_FILTER);
        PendingIntent pt = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        */

        mBuilder.setStyle(new NotificationCompat.InboxStyle()
                .setBigContentTitle("Censor Census - URL Received")
                .addLine("Received a new url!")
                .addLine("Performing sanity checks...")
                .setSummaryText(Integer.toString(checkedCount) + " Checked / " + Integer.toString(censoredCount) + " Censored"))
                .setSmallIcon(R.drawable.ic_stat_in_progress)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_launcher))
                .setPriority(Notification.PRIORITY_MAX)
                .setTicker("Censor Census - URL Received")
                .setAutoCancel(false);
                //.addAction(R.drawable.ic_launcher,"Load Settings",pt)

        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

        new Thread
        (
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());
                        Pair<Boolean,Integer> wasCensored;

                        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                        mBuilder.setStyle(new NotificationCompat.InboxStyle()
                                .setBigContentTitle("Censor Census - Checking URL")
                                .addLine("Started at " + currentDateTimeString)
                                .addLine("Checking URL.....")
                                .addLine("MD5: " + intent.getStringExtra("hash"))
                                .setSummaryText(Integer.toString(checkedCount) + " Checked / " + Integer.toString(censoredCount) + " Censored")
                        );
                        mBuilder.setProgress(2,1,true);
                        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

                        //Do the actual check
                        wasCensored = checkURL(intent.getStringExtra("url"));

                        //We're complete - update the time
                        currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());

                        //Update our local stats
                        setCounts(wasCensored.first);

                        if(wasCensored.first)
                        {
                            mBuilder.setTicker("Found a possibly censored URL!");
                            mBuilder.setStyle(new NotificationCompat.InboxStyle()
                                    .setBigContentTitle("Censor Census - Waiting")
                                    .addLine("Last check: " + currentDateTimeString)
                                    .addLine("Last URL was possibly censored!")
                                    .addLine("MD5: " + intent.getStringExtra("hash"))
                                    .setSummaryText(Integer.toString(checkedCount) + " Checked / " + Integer.toString(censoredCount) + " Censored")
                            );
                        }
                        else
                        {
                            mBuilder.setStyle(new NotificationCompat.InboxStyle()
                                    .setBigContentTitle("Censor Census - Waiting")
                                    .addLine("Last check: " + currentDateTimeString)
                                    .addLine("Last URL wasn't censored!")
                                    .setSummaryText(Integer.toString(checkedCount) + " Checked / " + Integer.toString(censoredCount) + " Censored")
                            );
                        }
                        mBuilder.setProgress(0,0,false);
                        mBuilder.setSmallIcon(R.drawable.ic_stat_waiting);
                        mNotifyManager.notify(NOTIFICATION_ID, mBuilder.build());

                        //Send the details back regardless (will chew DB space but will give a clearer picture)
                        notifyBackEnd(intent.getStringExtra("hash"),"",wasCensored, intent.getStringExtra("isp"), intent.getStringExtra("sim"));

                        stopSelf();
                    }
                }
        ).start();


        return START_NOT_STICKY;
    }


    private void submitURL(String submittedURL)
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        JSONObject json;
        HttpPost httpost = new HttpPost("https://bowdlerize.co.uk/api/1/submiturl.php");

        httpost.setHeader("Accept", "application/json");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("url", submittedURL));

        try
        {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
            HttpResponse response = httpclient.execute(httpost);
            String rawJSON = EntityUtils.toString(response.getEntity());
            response.getEntity().consumeContent();
            Log.e("rawJSON",rawJSON);
            json = new JSONObject(rawJSON);

            //TODO In future versions we'll check for success and store it for later if it failed
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void notifyBackEnd(String md5Hash, String hmac, Pair<Boolean,Integer> results,String ISP, String SIM)
    {
        DefaultHttpClient httpclient = new DefaultHttpClient();
        JSONObject json;
        HttpPost httpost = new HttpPost("https://bowdlerize.co.uk/api/1/receiveresult.php");

        Log.e("notifyBackend",md5Hash + " / " + Boolean.toString(results.first));
        httpost.setHeader("Accept", "application/json");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("md5", md5Hash));
        nvps.add(new BasicNameValuePair("hmac", hmac));
        nvps.add(new BasicNameValuePair("isp", ISP));
        nvps.add(new BasicNameValuePair("sim", SIM));
        nvps.add(new BasicNameValuePair("censored", Boolean.toString(results.first)));
        nvps.add(new BasicNameValuePair("confidence", Integer.toString(results.second)));

        try
        {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            HttpResponse response = httpclient.execute(httpost);
            String rawJSON = EntityUtils.toString(response.getEntity());
            response.getEntity().consumeContent();
            Log.e("rawJSON",rawJSON);
            json = new JSONObject(rawJSON);

            //TODO In future versions we'll check for success and store it for later if it failed
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setCounts(boolean Censored)
    {
        checkedCount++;

        if(Censored)
            censoredCount++;

        saveCounts();
    }

    private int getCensoredCount()
    {
        return censoredCount;
    }

    private int getCheckedCount()
    {
        return checkedCount;
    }

    //Return whether the URL is censored and a confidence level (1-100)
    private Pair<Boolean,Integer> checkURL(String checkURL)
    {
        if(!checkURL.startsWith("http"))
            checkURL = "http://" + checkURL;

        Log.e("Checking url",checkURL);
        DefaultHttpClient client;
        HttpHead headRequest;
        HttpResponse response = null;

        client = new DefaultHttpClient();

        headRequest = new HttpHead(checkURL);
        headRequest.setHeader("User-Agent", "Claire Perry - The Internet Censor");

        try
        {
            response = client.execute(headRequest);
        }
        catch(ConnectTimeoutException CTE)
        {
            CTE.printStackTrace();
            return new Pair(true,5);
        }
        catch (NoHttpResponseException NHRE)
        {
            NHRE.printStackTrace();
            return new Pair(true,5);
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return new Pair(false,0);
        }
        catch (IllegalStateException ise)
        {
            ise.printStackTrace();
            return new Pair(false,0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Pair(false,0);
        }

        int statusCode = response.getStatusLine().getStatusCode();

        Log.e("checkURL code",Integer.toString(statusCode));
        if(statusCode == 403 || statusCode == 404 )
            return new Pair(true,25);

        String phrase = response.getStatusLine().getReasonPhrase();
        Log.e("checkURL phrase",phrase);
        if(phrase.contains("orbidden"))
            return new Pair(true,50);

        if(phrase.contains("blocked"))
            return new Pair(true,100);

        for(Header hdr : response.getAllHeaders())
        {
            Log.e("checkURL header",hdr.getName().toString() + " / " + hdr.getValue().toString());
        }

        return new Pair(false,1);
    }

    private void saveCounts()
    {
        SharedPreferences prefs = getPreferences(mContext);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("censoredCount", censoredCount);
        editor.putInt("checkedCount", checkedCount);
        editor.commit();
    }

    @Override
    public void onDestroy()
    {
        Log.e("onDestroy","Bye bye");
        saveCounts();
        super.onDestroy();
    }

    private SharedPreferences getPreferences(Context context)
    {
        return getSharedPreferences(MainActivity.class.getSimpleName(),Context.MODE_PRIVATE);
    }
}
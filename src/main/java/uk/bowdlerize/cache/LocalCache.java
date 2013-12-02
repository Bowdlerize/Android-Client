package uk.bowdlerize.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.Pair;


public class LocalCache
{
    private SQLiteDatabase database;
    private OpenHelper dbHelper;

    private String[] wifiColumns = {"SSID","ISP"};


    public LocalCache(Context context)
    {
        dbHelper = new OpenHelper(context);
    }

    public void open() throws SQLException
    {
        if(null != dbHelper)
            database = dbHelper.getWritableDatabase();
    }

    public void close()
    {
        if(null != dbHelper)
            dbHelper.close();
    }

    public boolean addSSID(String SSID, String ISP)
    {
        boolean success = false;
        Log.e("addSSID", SSID + " / " + ISP);
        try
        {
            database.beginTransaction();
            ContentValues values = new ContentValues(2);
            values.put("SSID", SSID);
            values.put("ISP", ISP);

            database.insert("wifiNetworks", null, values);
            database.setTransactionSuccessful();
            success = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            database.endTransaction();
        }

        Log.e("addSSID",Boolean.toString(success));

        return success;
    }

    public Pair<Boolean,String> findSSID(String SSID)
    {
        try
        {
            Log.e("findSSID", SSID);
            Cursor cursor = database.query("wifiNetworks", wifiColumns,  "SSID=?", new String[]{SSID}, null, null, null);

            if(cursor.moveToFirst())
            {
                Log.e("findSSID", cursor.getString(1));
                return new Pair(true,cursor.getString(1));
            }
            else
            {
                Log.e("findSSID", "Nope");
                return new Pair(false,"");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return new Pair(false,"");
        }
    }
}

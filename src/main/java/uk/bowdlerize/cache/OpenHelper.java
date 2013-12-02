package uk.bowdlerize.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class OpenHelper extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "bowdlerizeCache";


    OpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        try
        {
            db.execSQL("CREATE TABLE \"wifiNetworks\"  (\"id\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \"SSID\" TEXT, \"ISP\" TEXT )");
        }
        catch(SQLiteException s)
        {
            s.printStackTrace();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        //Log.v("onUpgrade", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        try
        {
            //db.execSQL("DROP TABLE IF EXISTS wifiNetworks");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        onCreate(db);
    }
}

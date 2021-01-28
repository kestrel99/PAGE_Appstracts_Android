package org.pagemeeting.App2011;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class DatabaseOpenHelper extends SQLiteAssetHelper {
    public static final String DATABASE_NAME = "pageabstracts.sqlite";
    private static final int DATABASE_VERSION = 1;

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME,  context.getExternalFilesDir(null).getAbsolutePath(), null, DATABASE_VERSION);
    }
}
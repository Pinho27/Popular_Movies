package pt.pinho.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    static final String DATABASE_NAME = "PopularMovies.db";
    static final String TABLE_NAME = "favourite_Movies";
    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " ("
                    + "id" + " TEXT PRIMARY KEY,"
                    + "vote_average" + " TEXT,"
                    + "title" + " TEXT,"
                    + "poster" + " TEXT,"
                    + "overview" + " TEXT,"
                    + "release_date" + " TEXT,"
                    + "background" + " TEXT)";
    private static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME ;

    DatabaseHelper(Context ctx){
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DROP);
        onCreate(db);
    }
}

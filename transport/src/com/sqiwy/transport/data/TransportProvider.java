/**
 * Created by abrysov
 */
package com.sqiwy.transport.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.sqiwy.transport.data.TransportProvider.Table.Advertisement;
import com.sqiwy.transport.data.TransportProvider.Table.AdvertisementPoint;
import com.sqiwy.transport.data.TransportProvider.Table.News;
import com.sqiwy.transport.data.TransportProvider.Table.Resources;
import com.sqiwy.transport.data.TransportProvider.Table.Route;
import com.sqiwy.transport.data.TransportProvider.Table.RoutePoint;
import com.sqiwy.transport.data.TransportProvider.Table.Vehicle;
import com.sqiwy.transport.data.TransportProvider.Table.Horoscopes;

public class TransportProvider extends ContentProvider {

	public static final String AUTHORITY = "com.sqiwy.transport.test.data.transportprovider";
	
	private static final String DB_NAME = "transportprovider.db";
	private static final int DB_VERSION = 22;
	
	public static final class Table {
		
		public static final class Vehicle implements BaseColumns {
			
			public static final class Index {
				public static final int ID = 0;
				public static final int NAME = 1;
				public static final int DESCRIPTION = 2;
				public static final int GUID = 3;
				public static final int VERSION = 4;
				public static final int MAP_RATIO = 5;
				public static final int ADS_RATIO = 6;
				public static final int CONTENT_RATIO = 7;
				public static final int STOPS_RADIUS = 8;
			}
			
			private static final String TABLE_NAME = "vehicle";
			
			public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
			
			public static final String GUID = "guid";
			public static final String NAME = "name";
			public static final String DESCRIPTION = "description";
			public static final String VERSION = "version";
			public static final String MAP_RATIO = "map_ratio";
			public static final String ADS_RATIO = "ads_ratio";
			public static final String CONTENT_RATIO = "content_ratio";
			public static final String STOPS_RADIUS = "stops_radius";

			private static void create(SQLiteDatabase db) {
				// CREATE TABLE vehicle (
				// 		_ID INT PRIMARY KEY,
				// 		name TEXT,
				// 		description TEXT,
                // 		guid TEXT UNIQUE,
				// 		version INTEGER);

				db.execSQL("CREATE TABLE vehicle ("
						+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ NAME + " TEXT, "
						+ DESCRIPTION + " TEXT, "
                        + GUID + " TEXT UNIQUE, "
                        + VERSION + " INTEGER, "
                        + MAP_RATIO + " REAL, " 
                        + ADS_RATIO + " REAL, " 
                        + CONTENT_RATIO + " REAL, "
						+ STOPS_RADIUS + " INTEGER);");
			}
		}
		
		public static final class Route implements BaseColumns  {
			private static final String TABLE_NAME = "route";
			
			public static final class Index {
				public static final int ID = 0;
				public static final int DURATION = 1;
				public static final int START_TIME = 2;
				public static final int END_TIME = 3;
				public static final int DIRECTION = 4;
				public static final int VEHICLE_GUID = 5;
			}
			
			public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
			
			public static final String DURATION = "duration";
			public static final String START_TIME = "start_time";
			public static final String END_TIME = "end_time";
			public static final String DIRECTION = "direction";
			
			public static final String VEHICLE_ID = "vehicle_id";
			
			private static void create(SQLiteDatabase db) {
				// CREATE TABLE route (
				// 		_ID INT PRIMARY KEY,
				// 		duration INT,
				// 		start INT,
				// 		end INT,
				// 		direction TEXT,
				// 		vehicle_id TEXT NOT NULL REFERENCES vehicle(_id));
				
				db.execSQL("CREATE TABLE " + TABLE_NAME  + " ("
						+ _ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ DURATION  + " INTEGER, "
						+ START_TIME  + " INTEGER, "
						+ END_TIME  + " INTEGER, "
						+ DIRECTION  + " TEXT, "
						+ VEHICLE_ID  + " INTEGER NOT NULL REFERENCES "
                        + Vehicle.TABLE_NAME + "(" + Vehicle._ID + ") ON DELETE CASCADE);");
			}
		}
		
		public static final class RoutePoint implements BaseColumns  {
			private static final String TABLE_NAME = "route_point";
			
			public static final class Index {
				public static final int ID = 0;
				public static final int NAME = 1;
				public static final int LATITUDE = 2;
				public static final int LONGITUDE = 3;
				public static final int ORDER = 4;
				public static final int ARRIVAL_TIME = 5;
				public static final int ROUTE_ID = 6;
			}
			
			public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
			
			public static final String NAME = "name";
			public static final String LATITUDE = "lat";
			public static final String LONGITUDE = "lon";
			public static final String ORDER = "point_order";
			public static final String ARRIVAL_TIME = "arrival_time";
			
			public static final String ROUTE_ID = "route_id";
			
			private static void create(SQLiteDatabase db) {
				// CREATE TABLE route_point (
				// 		_ID INT PRIMARY KEY,
				// 		lat REAL,
				// 		lon REAL,
				// 		point_order INT,
				// 		arrival INT,
				// 		route_id INT NOT NULL REFERENCES route);
				
				db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
						+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ NAME + " TEXT, "
						+ LATITUDE + " REAL, "
						+ LONGITUDE + " REAL, "
						+ ORDER  + " INTEGER, "
						+ ARRIVAL_TIME + " INTEGER, "
						+ ROUTE_ID + " INTEGER NOT NULL REFERENCES "
                        + Route.TABLE_NAME + " ON DELETE CASCADE);");
			}
		}
		
		public static final class Advertisement implements BaseColumns  {
			private static final String TABLE_NAME = "ads";
			
			public static final class Index {
				public static final int ID = 0;
				public static final int TYPE = 1;
				public static final int SHOW_DURATION = 2;
				public static final int TRIGGER = 3;
				public static final int VERSION = 4;
				public static final int GUID = 5;
                public static final int ADVERTISER_NAME = 6;
                public static final int NAME = 7;
                public static final int END_DATE = 8;
                public static final int MAX_HOUR_SHOWS = 9;
                public static final int START_DATE = 10;
                public static final int SHOWS = 11;

			}
			
			public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
			
			public static final String TYPE = "type";
			public static final String SHOW_DURATION = "show_duration";
			public static final String TRIGGER = "trigger";
			public static final String VERSION = "version";
			public static final String GUID = "guid";
            public static final String ADVERTISER_NAME = "advertiser_name";
            public static final String NAME = "name";
            public static final String END_DATE = "end_date";
            public static final String MAX_HOUR_SHOWS = "max_hour_shows";
            public static final String START_DATE = "start_date";
            public static final String SHOWS = "shows";
			
			
			private static void create(SQLiteDatabase db) {
				
				db.execSQL("CREATE TABLE " + TABLE_NAME  + " ("
						+ _ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ TYPE  + " TEXT, "
						+ SHOW_DURATION  + " INTEGER, "
						+ TRIGGER  + " TEXT, "
						+ VERSION + " INTEGER, "
						+ GUID + " TEXT UNIQUE, "
                        + ADVERTISER_NAME  + " TEXT, "
                        + NAME  + " TEXT, "
                        + END_DATE  + " TEXT, "
                        + MAX_HOUR_SHOWS  + " INTEGER, "
                        //+ DURACTION  + " INTEGER, "
                        + START_DATE  + " TEXT, "
                        + SHOWS + " INTEGER);");
			}
		}
		
		public static final class AdvertisementPoint implements BaseColumns  {
			private static final String TABLE_NAME = "ad_point";
			
			public static final class Index {
				public static final int ID = 0;
				public static final int NAME = 1;
				public static final int LATITUDE = 2;
				public static final int LONGITUDE = 3;
				public static final int RADIUS = 4;
				public static final int ADVERTISEMENT_ID = 5;
			}
			
			public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
			
			public static final String NAME = "name";
			public static final String LATITUDE = "lat";
			public static final String LONGITUDE = "lon";
			public static final String RADIUS = "radius";
			public static final String ADVERTISEMENT_ID = "ad_id";
			
			private static void create(SQLiteDatabase db) {
				// CREATE TABLE route_point (
				// 		_ID INT PRIMARY KEY,
				// 		lat REAL,
				// 		lon REAL,
				// 		point_order INT,
				// 		arrival INT,
				// 		route_id INT NOT NULL REFERENCES route);
				
				db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
						+ _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ NAME + " TEXT, "
						+ LATITUDE + " REAL, "
						+ LONGITUDE + " REAL, "
						+ RADIUS  + " REAL, "
						+ ADVERTISEMENT_ID + " INTEGER NOT NULL REFERENCES "
                        + Advertisement.TABLE_NAME + " ON DELETE CASCADE);");
			}
		}
		
		public static final class Resources implements BaseColumns  {
			private static final String TABLE_NAME = "resources";

            public static final class Index {
				public static final int ID = 0;
				public static final int ARCHIVE = 1;
				public static final int DOWNLOAD_URI = 2;
				public static final int STORAGE_URI = 3;
				public static final int ACCESS_URI = 4;
				public static final int BYTES = 5;
                public static final int SHA1 = 6;
				public static final int STATUS = 7;
				public static final int GUID = 9;
                public static final int ADS_GUID = 8;
			}
			
			public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
			
			public static final String ARCHIVE = "archive";
			public static final String DOWNLOAD_URI = "uri";
			public static final String STORAGE_URI = "storage_uri";
			public static final String ACCESS_URI = "access_uri";
			public static final String BYTES = "bytes";
			public static final String SHA1 = "sha1";
			public static final String STATUS = "status";
			public static final String GUID = "guid";
            public static final String ADS_GUID = "ads_guid";

			private static void create(SQLiteDatabase db) {
				
				db.execSQL("CREATE TABLE " + TABLE_NAME  + " ("
						+ _ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ ARCHIVE  + " BOOL, "
						+ DOWNLOAD_URI  + " TEXT, "
						+ STORAGE_URI  + " TEXT, "
						+ ACCESS_URI  + " TEXT, "
						+ BYTES  + " INTEGER, "
						+ SHA1  + " TEXT, "
						+ STATUS + " INTEGER, "
                        + ADS_GUID + " TEXT, "
						+ GUID + " TEXT UNIQUE);");
			}
		}

		public static final class News implements BaseColumns  {
			private static final String TABLE_NAME = "news";

			public static final class Index {
				public static final int ID = 0;
				public static final int DATE = 1;
				public static final int TITLE = 2;
			}

			public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

			public static final String DATE = "date";
			public static final String TITLE = "title";

			private static void create(SQLiteDatabase db) {

				db.execSQL("CREATE TABLE " + TABLE_NAME  + " ("
						+ _ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ DATE  + " INTEGER, "
						+ TITLE  + " TEXT);");
			}
		}

        public static final class Horoscopes implements BaseColumns {
            private static final String TABLE_NAME = "horoscopes";

            public static final class Index {
                public static final int ID = 0;
                public static final int DESCRIPTION = 1;
                public static final int TITLE = 2;
                public static final int CODE = 3;
            }

            public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

            public static final String DESCRIPTION = "description";
            public static final String TITLE = "title";
            public static final String CODE = "code";

            private static void create(SQLiteDatabase db) {

                db.execSQL("CREATE TABLE " + TABLE_NAME  + " ("
                        + _ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + DESCRIPTION  + " TEXT, "
                        + TITLE  + " TEXT,"
                        + CODE  + " TEXT);");
            }


        }

        public static final class Currencies implements BaseColumns {
            private static final String TABLE_NAME = "currencies";

            public static final class Index {
                public static final int ID = 0;
                public static final int CODE = 1;
                public static final int NAME = 2;
                public static final int SELL = 3;
                public static final int BUY = 4;
            }

            public static final Uri URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

            public static final String CODE = "code";
            public static final String NAME = "name";
            public static final String SELL = "sell";
            public static final String BUY = "buy";

            private static void create(SQLiteDatabase db) {

                db.execSQL("CREATE TABLE " + TABLE_NAME  + " ("
                        + _ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + CODE  + " TEXT, "
                        + NAME  + " TEXT, "
                        + SELL  + " TEXT, "
                        + BUY  + " TEXT);");
            }

        }
	}
	
	public static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Vehicle.create(db);
			Route.create(db);
			RoutePoint.create(db);
			Advertisement.create(db);
			Resources.create(db);
			AdvertisementPoint.create(db);
			News.create(db);
            Horoscopes.create(db);
            Table.Currencies.create(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String sql = "DROP TABLE IF EXISTS " + Vehicle.TABLE_NAME;
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS " + Route.TABLE_NAME;
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS " + RoutePoint.TABLE_NAME;
            db.execSQL(sql);
            
            sql = "DROP TABLE IF EXISTS " + Advertisement.TABLE_NAME;
            db.execSQL(sql);
            
            sql = "DROP TABLE IF EXISTS " + Resources.TABLE_NAME;
            db.execSQL(sql);
            
            sql = "DROP TABLE IF EXISTS " + AdvertisementPoint.TABLE_NAME;
            db.execSQL(sql);
            
            sql = "DROP TABLE IF EXISTS " + News.TABLE_NAME;
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS " + Horoscopes.TABLE_NAME;
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS " + Table.Currencies.TABLE_NAME;
            db.execSQL(sql);

            onCreate(db);
		}
		
		@Override
		public void onOpen(SQLiteDatabase db) {
			super.onOpen(db);
			if (!db.isReadOnly()) {
				db.execSQL("PRAGMA foreign_keys = ON;");
			}
		}
	}
	
	private DatabaseHelper mDatabaseHelper;
	
	private static final UriMatcher MATCHER;
	
	private static final int MATCH_VEHICLE			= 0x01;
	private static final int MATCH_VEHICLE_ID 		= 0x02;
	private static final int MATCH_ROUTE			= 0x03;
	private static final int MATCH_ROUTE_ID			= 0x04;
	private static final int MATCH_ROUTE_POINT		= 0x05;
	private static final int MATCH_ROUTE_POINT_ID	= 0x06;
	private static final int MATCH_AD				= 0x07;
	private static final int MATCH_AD_ID			= 0x08;
	private static final int MATCH_RES				= 0x09;
	private static final int MATCH_RES_ID			= 0x0a;
	private static final int MATCH_AD_POINT			= 0x0b;
	private static final int MATCH_AD_POINT_ID		= 0x0c;
	private static final int MATCH_NEWS				= 0x0e;
	private static final int MATCH_NEWS_ID			= 0x0f;
    private static final int MATCH_HOROSCOPES		= 0x10;
    private static final int MATCH_HOROSCOPES_ID	= 0x11;
    private static final int MATCH_CURRENCIES       = 0x12;
    private static final int MATCH_CURRENCIES_ID    = 0x13;
	
	static {
		MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		
		MATCHER.addURI(AUTHORITY, Vehicle.TABLE_NAME, MATCH_VEHICLE);
		MATCHER.addURI(AUTHORITY, Vehicle.TABLE_NAME + "/#", MATCH_VEHICLE_ID);
		
		MATCHER.addURI(AUTHORITY, Route.TABLE_NAME, MATCH_ROUTE);
		MATCHER.addURI(AUTHORITY, Route.TABLE_NAME + "/#", MATCH_ROUTE_ID);
		
		MATCHER.addURI(AUTHORITY, RoutePoint.TABLE_NAME, MATCH_ROUTE_POINT);
		MATCHER.addURI(AUTHORITY, RoutePoint.TABLE_NAME + "/#", MATCH_ROUTE_POINT_ID);
		
		MATCHER.addURI(AUTHORITY, Advertisement.TABLE_NAME, MATCH_AD);
		MATCHER.addURI(AUTHORITY, Advertisement.TABLE_NAME + "/#", MATCH_AD_ID);
		
		MATCHER.addURI(AUTHORITY, Resources.TABLE_NAME, MATCH_RES);
		MATCHER.addURI(AUTHORITY, Resources.TABLE_NAME + "/#", MATCH_RES_ID);
		
		MATCHER.addURI(AUTHORITY, AdvertisementPoint.TABLE_NAME, MATCH_AD_POINT);
		MATCHER.addURI(AUTHORITY, AdvertisementPoint.TABLE_NAME + "/#", MATCH_AD_POINT_ID);

		MATCHER.addURI(AUTHORITY, News.TABLE_NAME, MATCH_NEWS);
		MATCHER.addURI(AUTHORITY, Table.News.TABLE_NAME + "/#", MATCH_NEWS_ID);

        MATCHER.addURI(AUTHORITY, Horoscopes.TABLE_NAME, MATCH_HOROSCOPES);
        MATCHER.addURI(AUTHORITY, Horoscopes.TABLE_NAME + "/#", MATCH_HOROSCOPES_ID);

        MATCHER.addURI(AUTHORITY, Table.Currencies.TABLE_NAME, MATCH_CURRENCIES);
        MATCHER.addURI(AUTHORITY, Table.Currencies.TABLE_NAME + "/#", MATCH_CURRENCIES_ID);
	}
	
	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHelper(getContext());
		return true;
	}
	
	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
		
		Cursor cursor;
		long id;
		
		switch (MATCHER.match(uri)) {
		
		case MATCH_ROUTE_POINT:
			
			cursor = db.query(RoutePoint.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
			break;
			
		case MATCH_ROUTE_POINT_ID:
			
			id = ContentUris.parseId(uri);
			
			cursor = db.query(RoutePoint.TABLE_NAME, null, selectionById(RoutePoint._ID, id, selection), selectionArgs,
					null, null, sortOrder);
			break;
		
		case MATCH_ROUTE:
			
			cursor = db.query(Route.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
			break;
		
		case MATCH_ROUTE_ID:
			
			id = ContentUris.parseId(uri);
			
			cursor = db.query(Route.TABLE_NAME, null, selectionById(Route._ID, id, selection), selectionArgs,
					null, null, sortOrder);
			break;
		
		case MATCH_VEHICLE:
			
			cursor = db.query(Vehicle.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
			break;
		
		case MATCH_VEHICLE_ID:
			
			id = ContentUris.parseId(uri);
			
			cursor = db.query(Vehicle.TABLE_NAME, null, selectionById(Vehicle._ID, id, selection), selectionArgs,
					null, null, sortOrder);
			break;
		
		case MATCH_AD:
			
			cursor = db.query(Advertisement.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
			break;
			
		case MATCH_AD_ID:
			
			id = ContentUris.parseId(uri);
			
			cursor = db.query(Advertisement.TABLE_NAME, null, selectionById(Advertisement._ID, id, selection),
					selectionArgs, null, null, sortOrder);
			break;
		
		case MATCH_RES:
			
			cursor = db.query(Resources.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
			break;
			
		case MATCH_RES_ID:
			
			id = ContentUris.parseId(uri);
			
			cursor = db.query(Resources.TABLE_NAME, null, selectionById(Resources._ID, id, selection),
					selectionArgs, null, null, sortOrder);
			break;	
		
		case MATCH_AD_POINT:
			
			cursor = db.query(AdvertisementPoint.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
			break;
			
		case MATCH_AD_POINT_ID:
			
			id = ContentUris.parseId(uri);
			
			cursor = db.query(AdvertisementPoint.TABLE_NAME, null, selectionById(AdvertisementPoint._ID, id, selection), selectionArgs,
					null, null, sortOrder);
			break;
			
		case MATCH_NEWS:

			cursor = db.query(News.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
			break;

		case MATCH_NEWS_ID:

			id = ContentUris.parseId(uri);

			cursor = db.query(News.TABLE_NAME, null, selectionById(News._ID, id, selection),
					selectionArgs, null, null, sortOrder);
			break;

        case MATCH_HOROSCOPES:

            cursor = db.query(Horoscopes.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);
            break;

        case MATCH_HOROSCOPES_ID:

            id = ContentUris.parseId(uri);

            cursor = db.query(Horoscopes.TABLE_NAME, null, selectionById(Horoscopes._ID, id, selection),
                        selectionArgs, null, null, sortOrder);
            break;

        case MATCH_CURRENCIES:

            cursor = db.query(Table.Currencies.TABLE_NAME, null, selection, selectionArgs, null, null, sortOrder);

            break;

        case MATCH_CURRENCIES_ID:

            id = ContentUris.parseId(uri);

            cursor = db.query(Table.Currencies.TABLE_NAME, null, selectionById(Table.Currencies._ID, id, selection),
                    selectionArgs, null, null, sortOrder);

            break;

		default:
			
			throw new IllegalArgumentException("Uri [" + uri + "] is not supported.");
		}

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		
		long id;
		
		switch (MATCHER.match(uri)) {
		
		case MATCH_ROUTE_POINT:
			
			id = db.insert(RoutePoint.TABLE_NAME, null, values);
			break;
			
		case MATCH_ROUTE:
			
			id = db.insert(Route.TABLE_NAME, null, values);
			break;
		
		case MATCH_VEHICLE:
			
			id = db.insert(Vehicle.TABLE_NAME, null, values);
			break;
		
		case MATCH_AD:
			
			id = db.insertWithOnConflict(Advertisement.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			break;
		
		case MATCH_RES:
			
			id = db.insertWithOnConflict(Resources.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			break;

		case MATCH_AD_POINT:
			
			id = db.insert(AdvertisementPoint.TABLE_NAME, null, values);
			break;
			
		case MATCH_NEWS:

            id = db.insert(News.TABLE_NAME, null, values);
			break;

        case MATCH_HOROSCOPES:

            id = db.insert(Horoscopes.TABLE_NAME, null, values);
            break;

        case MATCH_CURRENCIES:

                id = db.insert(Table.Currencies.TABLE_NAME, null, values);
            break;

		default:
			
			throw new IllegalArgumentException("Uri [" + uri + "] is not supported");
		}

        if (id != -1) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
		
		return ContentUris.withAppendedId(uri, id);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		
		int rowsAffected;
		long id;
		
		switch (MATCHER.match(uri)) {
		
		case MATCH_ROUTE_POINT:
			
			rowsAffected = db.update(RoutePoint.TABLE_NAME, values, selection, selectionArgs);
			break;
			
		case MATCH_ROUTE_POINT_ID:
			
			id = ContentUris.parseId(uri);
			
			rowsAffected = db.update(RoutePoint.TABLE_NAME, values,
					selectionById(RoutePoint._ID, id, selection), selectionArgs);
			break;
		
		case MATCH_ROUTE:
			
			rowsAffected = db.update(Route.TABLE_NAME, values, selection, selectionArgs);
			break;
		
		case MATCH_ROUTE_ID:
			
			id = ContentUris.parseId(uri);
			
			rowsAffected = db.update(Route.TABLE_NAME, values, selectionById(Route._ID, id, selection), selectionArgs);
			break;
		
		case MATCH_VEHICLE:
			
			rowsAffected = db.update(Vehicle.TABLE_NAME, values, selection, selectionArgs);
			break;
		
		case MATCH_VEHICLE_ID:
			
			id = ContentUris.parseId(uri);
			
			rowsAffected = db.update(Vehicle.TABLE_NAME, values, selectionById(Vehicle._ID, id, selection), selectionArgs);
			break;
			
		case MATCH_AD:
			
			rowsAffected = db.update(Advertisement.TABLE_NAME, values, selection, selectionArgs);
			break;
		
		case MATCH_AD_ID:
			
			id = ContentUris.parseId(uri);
			
			rowsAffected = db.update(Advertisement.TABLE_NAME, values, selectionById(Advertisement._ID, id, selection), selectionArgs);
			break;
		
		case MATCH_RES:
			
			rowsAffected = db.update(Resources.TABLE_NAME, values, selection, selectionArgs);
			break;
		
		case MATCH_RES_ID:
			
			id = ContentUris.parseId(uri);
			
			rowsAffected = db.update(Resources.TABLE_NAME, values, selectionById(Resources._ID, id, selection), selectionArgs);
			break;
			
		case MATCH_AD_POINT:
			
			rowsAffected = db.update(AdvertisementPoint.TABLE_NAME, values, selection, selectionArgs);
			break;
			
		case MATCH_AD_POINT_ID:
			
			id = ContentUris.parseId(uri);
			
			rowsAffected = db.update(AdvertisementPoint.TABLE_NAME, values,
					selectionById(AdvertisementPoint._ID, id, selection), selectionArgs);
			break;

		case MATCH_NEWS:

			rowsAffected = db.update(News.TABLE_NAME, values, selection, selectionArgs);
			break;

		case MATCH_NEWS_ID:

			id = ContentUris.parseId(uri);

			rowsAffected = db.update(News.TABLE_NAME, values, selectionById(News._ID, id, selection), selectionArgs);
			break;

        case MATCH_HOROSCOPES:

                rowsAffected = db.update(Horoscopes.TABLE_NAME, values, selection, selectionArgs);
            break;

        case MATCH_HOROSCOPES_ID:

                id = ContentUris.parseId(uri);
                rowsAffected = db.update(Horoscopes.TABLE_NAME, values, selectionById(Horoscopes._ID, id, selection), selectionArgs);

            break;

        case MATCH_CURRENCIES:

                rowsAffected = db.update(Table.Currencies.TABLE_NAME, values, selection, selectionArgs);
            break;

        case MATCH_CURRENCIES_ID:

                id = ContentUris.parseId(uri);
                rowsAffected = db.update(Table.Currencies.TABLE_NAME, values, selectionById(Table.Currencies._ID, id, selection), selectionArgs);

            break;

		default:
			
			throw new IllegalArgumentException("Uri [" + uri + "] is not supported.");
		}

        if (rowsAffected > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
		
		return rowsAffected;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
		
		int rowsAffected;
		long id;
		
		switch (MATCHER.match(uri)) {
		
		case MATCH_ROUTE_POINT:
			
			rowsAffected = db.delete(RoutePoint.TABLE_NAME, selection, selectionArgs);
			break;
			
		case MATCH_ROUTE_POINT_ID:
			
			id = ContentUris.parseId(uri);
			
			rowsAffected = db.delete(RoutePoint.TABLE_NAME,
					selectionById(RoutePoint._ID, id, selection), selectionArgs);
			break;
		
		case MATCH_ROUTE:
			
			rowsAffected = db.delete(Route.TABLE_NAME, selection, selectionArgs);
			break;
		
		case MATCH_ROUTE_ID:
			
			id = ContentUris.parseId(uri);
			
			rowsAffected = db.delete(Route.TABLE_NAME, selectionById(Route._ID, id, selection), selectionArgs);
			break;
		
		case MATCH_VEHICLE:
			
			rowsAffected = db.delete(Vehicle.TABLE_NAME, selection, selectionArgs);
			break;
		
		case MATCH_VEHICLE_ID:
			
			id = ContentUris.parseId(uri);
			
			rowsAffected = db.delete(Vehicle.TABLE_NAME, selectionById(Vehicle._ID, id, selection), selectionArgs);
			break;
			
		case MATCH_AD:
			
			rowsAffected = db.delete(Advertisement.TABLE_NAME, selection, selectionArgs);
			break;
		
		case MATCH_AD_ID:
			
			id = ContentUris.parseId(uri);
			
			rowsAffected = db.delete(Advertisement.TABLE_NAME, selectionById(Advertisement._ID, id, selection), selectionArgs);
			break;
		
		case MATCH_RES:
			
			rowsAffected = db.delete(Resources.TABLE_NAME, selection, selectionArgs);
			break;
		
		case MATCH_RES_ID:
			
			id = ContentUris.parseId(uri);
			
			rowsAffected = db.delete(Resources.TABLE_NAME, selectionById(Resources._ID, id, selection), selectionArgs);
			break;
			
		case MATCH_AD_POINT:
			
			rowsAffected = db.delete(AdvertisementPoint.TABLE_NAME, selection, selectionArgs);
			break;
			
		case MATCH_AD_POINT_ID:
			
			id = ContentUris.parseId(uri);
			
			rowsAffected = db.delete(AdvertisementPoint.TABLE_NAME,
					selectionById(AdvertisementPoint._ID, id, selection), selectionArgs);
			break;

		case MATCH_NEWS:

			rowsAffected = db.delete(News.TABLE_NAME, selection, selectionArgs);
			break;

		case MATCH_NEWS_ID:

			id = ContentUris.parseId(uri);

			rowsAffected = db.delete(News.TABLE_NAME, selectionById(News._ID, id, selection), selectionArgs);
			break;

        case MATCH_HOROSCOPES:

                rowsAffected = db.delete(Horoscopes.TABLE_NAME, selection, selectionArgs);
            break;

        case MATCH_HOROSCOPES_ID:

            id = ContentUris.parseId(uri);
            rowsAffected = db.delete(Horoscopes.TABLE_NAME, selectionById(Horoscopes._ID, id, selection), selectionArgs);
            break;

        case MATCH_CURRENCIES:

            rowsAffected = db.delete(Table.Currencies.TABLE_NAME, selection, selectionArgs);
            break;

        case MATCH_CURRENCIES_ID:

            id = ContentUris.parseId(uri);
            rowsAffected = db.delete(Table.Currencies.TABLE_NAME, selectionById(Table.Currencies._ID, id, selection), selectionArgs);
            break;

		default:
			
			throw new IllegalArgumentException("Uri [" + uri + "] is not supported.");
		}

        if (rowsAffected > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
		
		return rowsAffected;
	}

	private static final String selectionById(String idColumn, long id, String originalSelection) {
		
		String result = "(" + idColumn + "=" + id +")"; 
		if (!TextUtils.isEmpty(originalSelection)) {
			result += " AND (" + originalSelection + ")";
		}
		
		return result;
	}

}

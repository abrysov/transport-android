/**
 * Created by abrysov
 */
package com.sqiwy.transport.data;


import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.sqiwy.transport.BuildConfig;
import com.sqiwy.transport.advertisement.Advertisement;
import com.sqiwy.transport.advertisement.AdvertisementResource;
import com.sqiwy.transport.data.TransportProvider.Table;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TransportProviderHelper {

	private static final String TAG = TransportProviderHelper.class.getName();

	public static Vehicle insertVehicle(ContentResolver resolver, Vehicle vehicle) {
		ContentValues values = new ContentValues();
		
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "insertVehicle vehicle.getMapRatio() = " + vehicle.getMapRatio());
		}
		
		values.put(Table.Vehicle.NAME, vehicle.getName());
		values.put(Table.Vehicle.DESCRIPTION, vehicle.getDescription());
		values.put(Table.Vehicle.GUID, vehicle.getGuid());
		values.put(Table.Vehicle.VERSION, -1); // set incorrect version to avoid query Vehicle during insert of points
		values.put(Table.Vehicle.MAP_RATIO, vehicle.getMapRatio());
		values.put(Table.Vehicle.ADS_RATIO, vehicle.getAdsRatio());
		values.put(Table.Vehicle.CONTENT_RATIO, vehicle.getContentRatio());
		values.put(Table.Vehicle.STOPS_RADIUS, vehicle.getStopsRadius());

		Uri uriId = resolver.insert(Table.Vehicle.URI, values);
		
		vehicle.setId(ContentUris.parseId(uriId));
		
		if (null != vehicle.getRoutes()) {
			insertRoutes(resolver, vehicle.getId(), vehicle.getRoutes());
		}

        // set real VERSION to allow selects of properly stored Vehicle
        values = new ContentValues();
        values.put(Table.Vehicle.VERSION, vehicle.getVersion());
        resolver.update(ContentUris.withAppendedId(Table.Vehicle.URI, vehicle.getId()), values, null, null);
        // now Vehicle is ready to use

		return vehicle;
	}
	
	public static Vehicle queryVehicle(ContentResolver resolver) {
        return queryVehicle(resolver, -1);
	}

	public static Vehicle queryVehicle(ContentResolver resolver, long id) {
		Cursor cursor;

        if (id != -1) {
            cursor = resolver.query(ContentUris.withAppendedId(Table.Vehicle.URI, id),
                    null, Table.Vehicle.VERSION + " != -1", null, null);
        } else {
            cursor = resolver.query(Table.Vehicle.URI, null, Table.Vehicle.VERSION + " != -1", null,
                    Table.Vehicle._ID + " DESC LIMIT 1");
        }

		Vehicle vehicle = null;

		if (cursor.moveToNext()) {
			vehicle = new Vehicle();
			vehicle.setId(cursor.getLong(Table.Vehicle.Index.ID));
			vehicle.setName(cursor.getString(Table.Vehicle.Index.NAME));
			vehicle.setDescription(cursor.getString(Table.Vehicle.Index.DESCRIPTION));
			vehicle.setGuid(cursor.getString(Table.Vehicle.Index.GUID));
			vehicle.setVersion(cursor.getInt(Table.Vehicle.Index.VERSION));
			vehicle.setMapRatio(cursor.getFloat(Table.Vehicle.Index.MAP_RATIO));
			vehicle.setAdsRatio(cursor.getFloat(Table.Vehicle.Index.ADS_RATIO));
			vehicle.setContentRatio(cursor.getFloat(Table.Vehicle.Index.CONTENT_RATIO));
			vehicle.setStopsRadius(cursor.getInt(Table.Vehicle.Index.STOPS_RADIUS));
		}
		cursor.close();

		if (vehicle != null) {
			vehicle.setRoutes(queryRoutes(resolver, vehicle.getId()));
		}

		return vehicle;
	}

	public static void deleteVehicle(ContentResolver resolver, long id) {
		resolver.delete(ContentUris.withAppendedId(Table.Vehicle.URI, id), null, null);
	}

	public static void deleteAllVehicles(ContentResolver resolver) {
		resolver.delete(Table.Vehicle.URI, null, null); // :FIXME should be removed all related routes and points
	}

	public static void insertRoutes(ContentResolver resolver, long vehicleId, List<Route> routes) {
		
		// Prepare insert operations
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		
		Builder builder;
		for (Route route : routes) {
			builder = ContentProviderOperation.newInsert(Table.Route.URI);
			builder.withValue(Table.Route.DURATION, route.getDuration());
			
			if (null != route.getStart()) {
				builder.withValue(Table.Route.START_TIME, route.getStart().getTime());
			}
			
			if (null != route.getEnd()) {
				builder.withValue(Table.Route.END_TIME, route.getEnd().getTime());
			}
			
			builder.withValue(Table.Route.DIRECTION, route.getDirection());
			
			builder.withValue(Table.Route.VEHICLE_ID, vehicleId);
			
			operations.add(builder.build());
		}
		
		// Execute operations
		try {
			ContentProviderResult[] batchResult = resolver.applyBatch(TransportProvider.AUTHORITY, operations);
			int index = 0;
			for (Route route : routes) {
				ContentProviderResult result = batchResult[index++];
				
				route.setId(ContentUris.parseId(result.uri));

				if (null != route.getPoints()) {
                    List<Point> points = new ArrayList<Point>(route.getPoints());
                    points.addAll(route.getBusStops());
					insertRoutePoints(resolver, route.getId(), points);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Batch failed for: vehicle [" + vehicleId + "], routes < " + routes + " >");
			// TODO: Do something about this? Throw custom exception?
		}
	}
	
	public static List<Route> queryRoutes(ContentResolver resolver, long vehicleId) {
		List<Route> result = null;
		
		Route route;
		Cursor cursor = resolver.query(Table.Route.URI, null, Table.Route.VEHICLE_ID + " = ?",
				new String[] {Long.toString(vehicleId)}, null);
		
		while (cursor.moveToNext()) {
			route = new Route();
			route.setId(cursor.getLong(Table.Route.Index.ID));
			route.setDuration(cursor.getLong(Table.Route.Index.DURATION));
			route.setStart(new Date(cursor.getLong(Table.Route.Index.START_TIME)));
			route.setEnd(new Date(cursor.getLong(Table.Route.Index.END_TIME)));
			route.setDirection(cursor.getString(Table.Route.Index.DIRECTION));
			
			if (null == result) {
				result = new ArrayList<Route>();
			}
			List<Point> points = queryRoutePoints(resolver, route.getId());
			if (points == null) {
				continue;
			}
            List<Point> busStops = new ArrayList<Point>();
            List<Point> geoPoints = new ArrayList<Point>();
            for (Point point : points) {
                if (point.getName() != null) {
                    busStops.add(point);
                } else {
                    geoPoints.add(point);
                }
            }
			route.setPoints(geoPoints);
            route.setBusStops(busStops);
			
			result.add(route);
		}
        cursor.close();
		
		return result;
	}
	
	public static void insertRoutePoints(ContentResolver resolver, long routeId, List<Point> points) {
		// Prepare insert operations
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		
		Builder builder;
		for (Point point : points) {
			builder = ContentProviderOperation.newInsert(Table.RoutePoint.URI);
			builder.withValue(Table.RoutePoint.NAME, point.getName());
			builder.withValue(Table.RoutePoint.LATITUDE, point.getLatitude());
			builder.withValue(Table.RoutePoint.LONGITUDE, point.getLongitude());
			builder.withValue(Table.RoutePoint.ORDER, point.getOrder());
			
			if (null != point.getArrivalTime()) {
				builder.withValue(Table.RoutePoint.ARRIVAL_TIME, point.getArrivalTime().getTime());
			}
			
			builder.withValue(Table.RoutePoint.ROUTE_ID, routeId);
			
			operations.add(builder.build());
		}
		
		// Execute operations
		try {
			ContentProviderResult[] batchResult = resolver.applyBatch(TransportProvider.AUTHORITY, operations);
			int index = 0;
			for (Point point : points) {
				ContentProviderResult result = batchResult[index++];
				
				point.setId(ContentUris.parseId(result.uri));
			}
		} catch (Exception e) {
			Log.e(TAG, "Batch failed for: vehicle [" + routeId + "], routes < " + points + " >");
			// TODO: Do something about this? Throw custom exception?
		}
	}
	
	public static List<Point> queryRoutePoints(ContentResolver resolver, long routeId) {
		List<Point> result = null;
		
		Point point;
		Cursor cursor = resolver.query(Table.RoutePoint.URI, null, Table.RoutePoint.ROUTE_ID + " = ?",
				new String[] {Long.toString(routeId)}, Table.RoutePoint.ORDER + " ASC");
		
		while (cursor.moveToNext()) {
			point = new Point();
			point.setId(cursor.getLong(Table.RoutePoint.Index.ID));
			point.setName(cursor.getString(Table.RoutePoint.Index.NAME));
			point.setLatitude(cursor.getDouble(Table.RoutePoint.Index.LATITUDE));
			point.setLongitude(cursor.getDouble(Table.RoutePoint.Index.LONGITUDE));
			point.setOrder(cursor.getInt(Table.RoutePoint.Index.ORDER));
			point.setArrivalTime(new Date(cursor.getLong(Table.RoutePoint.Index.ARRIVAL_TIME)));
			
			if (null == result) {
				result = new ArrayList<Point>();
			}
			
			result.add(point);
		}
        cursor.close();
		
		return result;
	}
	
	public static Advertisement insertAd(ContentResolver resolver, Advertisement ad) {
		ContentValues values = new ContentValues();
		
		values.put(Table.Advertisement.TYPE, ad.getType().toUpperCase());
		values.put(Table.Advertisement.SHOW_DURATION, ad.getShowDuration());
		values.put(Table.Advertisement.TRIGGER, ad.getTrigger());
		values.put(Table.Advertisement.VERSION, ad.getVersion());
		values.put(Table.Advertisement.GUID, ad.getGuid());
//        values.put(Table.Advertisement.ADVERTISER_NAME, ad.getAdvertiserName());
//        values.put(Table.Advertisement.NAME, ad.getName());
        values.put(Table.Advertisement.END_DATE, ad.getEndDate());
        values.put(Table.Advertisement.MAX_HOUR_SHOWS, ad.getMaxHourShows());
        //values.put(Table.Advertisement.DURACTION, ad.getShowDuration());
//        values.put(Table.Advertisement.START_DATE, ad.getStartDate());
        values.put(Table.Advertisement.SHOWS, ad.getShows());
		
		Uri uriId = resolver.insert(Table.Advertisement.URI, values);
		
		ad.setId(ContentUris.parseId(uriId));
		
		insertAdPoints(resolver, ad.getId(), ad.getPoints());
		
		return ad;
	}
	
	public static Advertisement queryAd(ContentResolver resolver, long id) {
		Cursor cursor = resolver.query(ContentUris.withAppendedId(Table.Advertisement.URI, id),
                    null, null, null, null);

		Advertisement ad = null;

		if (cursor.moveToNext()) {
			ad = parseAd(resolver, cursor);
		}
		cursor.close();

		if (null != ad) {
			// TODO: query ad points
		}

		return ad;
	}
	
	public static Advertisement queryAd(ContentResolver resolver, String guid) {
		Cursor cursor = resolver.query(Table.Advertisement.URI,
                    null, Table.Advertisement.GUID + " = ?", new String[] {guid}, null);

		Advertisement ad = null;

		if (cursor.moveToNext()) {
			ad = parseAd(resolver, cursor);
		}
		cursor.close();

		if (null != ad) {
			// TODO: query ad points?
		}

		return ad;
	}
	
	public static void deleteAd(ContentResolver resolver, long id) {
		resolver.delete(ContentUris.withAppendedId(Table.Advertisement.URI, id), null, null);
	}
	
	public static Advertisement parseAd(ContentResolver resolver, Cursor cursor) {
		
		Advertisement ad = new Advertisement();

		ad.setId(cursor.getLong(Table.Advertisement.Index.ID));
		ad.setShowDuration(cursor.getLong(Table.Advertisement.Index.SHOW_DURATION));
		ad.setTriggers(cursor.getString(Table.Advertisement.Index.TRIGGER));
		ad.setType(cursor.getString(Table.Advertisement.Index.TYPE));
//		ad.setStatus(cursor.getString(Table.Advertisement.Index.STATUS));
		ad.setGuid(cursor.getString(Table.Advertisement.Index.GUID));
		ad.setVersion(cursor.getInt(Table.Advertisement.Index.VERSION));
//        ad.setAdvertiserName(cursor.getString(Table.Advertisement.Index.ADVERTISER_NAME));
//        ad.setName(cursor.getString(Table.Advertisement.Index.NAME));
        ad.setEndDate(cursor.getString(Table.Advertisement.Index.END_DATE));
        ad.setMaxHourShows(cursor.getInt(Table.Advertisement.Index.MAX_HOUR_SHOWS));
        //ad.setShowDuration(cursor.getInt(Table.Advertisement.Index.DURACTION));
//        ad.setStartDate(cursor.getString(Table.Advertisement.Index.START_DATE));
        ad.setShows(cursor.getInt(Table.Advertisement.Index.SHOWS));

        List<AdvertisementResource> resources = queryResources(resolver, ad);
        ad.setResources(resources);

        ad.setPoints(queryAdPoints(resolver, ad.getId()));
		
		return ad;
	}
	
	public static void insertAdPoints(ContentResolver resolver, long adId, List<Point> points) {
		
		if (null == points) {
			return;
		}
		
		// Prepare insert operations
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		
		Builder builder;
		for (Point point : points) {
			builder = ContentProviderOperation.newInsert(Table.AdvertisementPoint.URI);
			builder.withValue(Table.AdvertisementPoint.NAME, point.getName());
			builder.withValue(Table.AdvertisementPoint.LATITUDE, point.getLatitude());
			builder.withValue(Table.AdvertisementPoint.LONGITUDE, point.getLongitude());
			builder.withValue(Table.AdvertisementPoint.RADIUS, point.getRadius());
			
			builder.withValue(Table.AdvertisementPoint.ADVERTISEMENT_ID, adId);
			
			operations.add(builder.build());
		}
		
		// Execute operations
		try {
			ContentProviderResult[] batchResult = resolver.applyBatch(TransportProvider.AUTHORITY, operations);
			int index = 0;
			for (Point point : points) {
				ContentProviderResult result = batchResult[index++];
				
				point.setId(ContentUris.parseId(result.uri));
			}
		} catch (Exception e) {
			Log.e(TAG, "Batch failed for: vehicle [" + adId + "], routes < " + points + " >");
			// TODO: Do something about this? Throw custom exception?
		}
	}
	
	public static List<Point> queryAdPoints(ContentResolver resolver, long adId) {
		List<Point> result = null;
		
		Point point;
		Cursor cursor = resolver.query(Table.AdvertisementPoint.URI, null,
				Table.AdvertisementPoint.ADVERTISEMENT_ID + " = ?", new String[] {Long.toString(adId)}, null);
		
		while (cursor.moveToNext()) {
			point = new Point();
			point.setId(cursor.getLong(Table.AdvertisementPoint.Index.ID));
			point.setName(cursor.getString(Table.AdvertisementPoint.Index.NAME));
			point.setLatitude(cursor.getDouble(Table.AdvertisementPoint.Index.LATITUDE));
			point.setLongitude(cursor.getDouble(Table.AdvertisementPoint.Index.LONGITUDE));
			point.setRadius(cursor.getFloat(Table.AdvertisementPoint.Index.RADIUS));

			if (null == result) {
				result = new ArrayList<Point>();
			}
			
			result.add(point);
		}
		
		return result;
	}
	
	public static AdvertisementResource insertRes(ContentResolver resolver, AdvertisementResource res, String adsGuid) {
		ContentValues values = new ContentValues();
		
		values.put(Table.Resources.ACCESS_URI, res.getAccessUri());
//		values.put(Table.Resources.ARCHIVE, res.isArchive());
		values.put(Table.Resources.BYTES, res.getBytes());
		values.put(Table.Resources.DOWNLOAD_URI, res.getUri());
		values.put(Table.Resources.GUID, res.getGuid());
		values.put(Table.Resources.STORAGE_URI, res.getStorageUri());
		values.put(Table.Resources.SHA1, res.getSha1());
        values.put(Table.Resources.ADS_GUID, adsGuid);
		
		Uri uriId = resolver.insert(Table.Resources.URI, values);
		
		res.setId(ContentUris.parseId(uriId));
		
		return res;
	}
	
	public static AdvertisementResource queryRes(ContentResolver resolver, long id) {
		Cursor cursor = resolver.query(ContentUris.withAppendedId(Table.Resources.URI, id),
                    null, null, null, null);

		AdvertisementResource res = null;

		if (cursor.moveToNext()) {
			res = parseRes(cursor);
		}
		cursor.close();

		return res;
	}
	
	public static AdvertisementResource queryRes(ContentResolver resolver, String guid) {
		Cursor cursor = resolver.query(Table.Resources.URI,
                    null, Table.Resources.GUID + " = ?", new String[] { guid }, null);

		AdvertisementResource res = null;

		if (cursor.moveToNext()) {
			res = parseRes(cursor);
		}
		cursor.close();

		return res;
	}

    public static List<AdvertisementResource> queryResources(ContentResolver resolver, Advertisement ad) {
        Cursor cursor = resolver.query(Table.Resources.URI,
                null, Table.Resources.ADS_GUID + " = ?", new String[] { ad.getGuid() }, null);

        ArrayList<AdvertisementResource> resources = new ArrayList<AdvertisementResource>(cursor.getCount());

        while (cursor.moveToNext()) {
            AdvertisementResource resource = parseRes(cursor);
            resource.setAd(ad);
            resources.add(resource);
        }
        cursor.close();

        return resources;
    }
	
	public static void deleteRes(ContentResolver resolver, long id) {
		resolver.delete(ContentUris.withAppendedId(Table.Resources.URI, id), null, null);
	}

	private static AdvertisementResource parseRes(Cursor cursor) {
		AdvertisementResource res = new AdvertisementResource();
		res.setId(cursor.getLong(Table.Resources.Index.ID));
		res.setAccessUri(cursor.getString(Table.Resources.Index.ACCESS_URI));
//		res.setArchive(0 != cursor.getInt(Table.Resources.Index.ARCHIVE));
		res.setBytes(cursor.getLong(Table.Resources.Index.BYTES));
		res.setGuid(cursor.getString(Table.Resources.Index.GUID));
		res.setAdsGuid(cursor.getString(Table.Resources.Index.ADS_GUID));
		res.setStorageUri(cursor.getString(Table.Resources.Index.STORAGE_URI));
		res.setUri(cursor.getString(Table.Resources.Index.DOWNLOAD_URI));
		res.setStatus(cursor.getInt(Table.Resources.Index.STATUS));
		res.setSha1(cursor.getString(Table.Resources.Index.SHA1));
		
		return res;
	}
	
	public static void markResourcesDownloaded(ContentResolver resolver, String downloadUri, String localUri) {
		
		ContentValues values = new ContentValues();
		values.put(Table.Resources.STATUS, AdvertisementResource.DOWNLOADED);
		
		resolver.update(Table.Resources.URI, values, Table.Resources.DOWNLOAD_URI + " = ? AND " + Table.Resources.STORAGE_URI + " = ?",
				new String[] {downloadUri, localUri});
	}

    public static void insertNews(ContentResolver resolver, List<News> newsList) {
        // Prepare insert operations
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        Builder builder;
        for (News news : newsList) {
            builder = ContentProviderOperation.newInsert(Table.News.URI);
            builder.withValue(Table.News.TITLE, news.getTitle());

            if (null != news.getDate()) {
                builder.withValue(Table.News.DATE, news.getDate().getTime());
            }

            operations.add(builder.build());
        }

        // Execute operations
        try {
            resolver.applyBatch(TransportProvider.AUTHORITY, operations);
        } catch (Exception e) {
            Log.e(TAG, "Batch failed for: news");
            // TODO: Do something about this? Throw custom exception?
        }
    }

    public static List<News> queryNews(ContentResolver resolver) {
        List<News> result = null;

        Cursor cursor = resolver.query(Table.News.URI, null, null, null, Table.News._ID + " DESC");

        while (cursor.moveToNext()) {
            News news = new News();
            news.setTitle(cursor.getString(Table.News.Index.TITLE));
            news.setDate(new Date(cursor.getLong(Table.News.Index.DATE)));

            if (null == result) {
                result = new ArrayList<News>();
            }

            result.add(news);
        }
        cursor.close();

        return result;
    }

    public static void deleteAllNews(ContentResolver resolver) {
        resolver.delete(Table.News.URI, null, null);
    }

    public static void insertHoroscope(ContentResolver resolver, List<Horoscope> horoscopesList){
        ArrayList<ContentProviderOperation> operationsHoroscope = new ArrayList<ContentProviderOperation>();
        Builder builder;

        for (Horoscope h : horoscopesList) {
            builder = ContentProviderOperation.newInsert(Table.Horoscopes.URI);

            builder.withValue(Table.Horoscopes.DESCRIPTION, h.getDescription());
            builder.withValue(Table.Horoscopes.TITLE, h.getTitle());
            builder.withValue(Table.Horoscopes.CODE, h.getCode());

            operationsHoroscope.add(builder.build());
        }

        // Execute operations
        try {
            resolver.applyBatch(TransportProvider.AUTHORITY, operationsHoroscope);
        } catch (Exception e) {
            Log.e(TAG, "Batch failed for: horoscopes");
            // TODO: Do something about this? Throw custom exception?
        }
    }

    public static List<Horoscope> queryHoroscopes (ContentResolver resolver) {

        List<Horoscope> result = null;

        Cursor cursor = resolver.query(Table.Horoscopes.URI, null, null, null, Table.Horoscopes._ID + " DESC");

        while (cursor.moveToNext()) {
            Horoscope horoscope = new Horoscope();
            horoscope.setTitle(cursor.getString(Table.Horoscopes.Index.TITLE));
            horoscope.setDescription(cursor.getString(Table.Horoscopes.Index.DESCRIPTION));
            horoscope.setCode(cursor.getString(Table.Horoscopes.Index.CODE));

            if (null == result) {
                result = new ArrayList<Horoscope>();
            }

            result.add(horoscope);
        }
        cursor.close();

        return result;
    }

    public static void deleteAllHoroscopes(ContentResolver resolver) {
        resolver.delete(Table.Horoscopes.URI, null, null);
    }

    public static List<Currency> queryCurrencies(ContentResolver resolver) {

        List<Currency> result = null;

        Cursor cursor = resolver.query(Table.Currencies.URI, null, null, null, Table.Currencies._ID + " DESC");

        while (cursor.moveToNext()) {
            Currency c = new Currency();
            c.setCode(cursor.getString(Table.Currencies.Index.CODE));
            c.setName(cursor.getString(Table.Currencies.Index.NAME));
            c.setSell(cursor.getString(Table.Currencies.Index.SELL));
            c.setBuy(cursor.getString(Table.Currencies.Index.BUY));

            if (null == result) {
                result = new ArrayList<Currency>();
            }

            result.add(c);
        }
        cursor.close();

        return result;
    }

    public static void insertCurrencies(ContentResolver resolver, List<Currency> coursesList){
        ArrayList<ContentProviderOperation> operationsCourses = new ArrayList<ContentProviderOperation>();
        Builder builder;

        for (Currency c : coursesList) {
            builder = ContentProviderOperation.newInsert(Table.Currencies.URI);

            builder.withValue(Table.Currencies.CODE, c.getCode());
            builder.withValue(Table.Currencies.NAME, c.getName());
            builder.withValue(Table.Currencies.SELL, c.getSell());
            builder.withValue(Table.Currencies.BUY, c.getBuy());

            operationsCourses.add(builder.build());
        }

        // Execute operations
        try {
            resolver.applyBatch(TransportProvider.AUTHORITY, operationsCourses);
        } catch (Exception e) {
            Log.e(TAG, "Batch failed for: courses");
            // TODO: Do something about this? Throw custom exception?
        }
    }

    public static void deleteAllCurrencies(ContentResolver resolver) {
        resolver.delete(Table.Currencies.URI, null, null);
    }

}

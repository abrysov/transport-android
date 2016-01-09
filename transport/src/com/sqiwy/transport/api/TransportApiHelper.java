/**
 * Created by abrysov
 */
package com.sqiwy.transport.api;

import android.content.ContentResolver;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.sqiwy.transport.BuildConfig;
import com.sqiwy.transport.R;
import com.sqiwy.transport.TransportApplication;
import com.sqiwy.transport.data.TransportProviderHelper;
import com.sqiwy.transport.util.EventBus;
import com.sqiwy.transport.util.PrefUtils;
import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.io.IOUtils;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedInput;

public final class TransportApiHelper {
    private static final String TAG = TransportApiHelper.class.getSimpleName();

    private static TransportApi sTransportApi;

    private static ScheduledExecutorService locationExecutor = Executors.newSingleThreadScheduledExecutor();

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);

        InputStream in = TransportApplication.getAppContext().getResources().openRawResource(
                R.raw.truststore);
        try {
            KeyStore keyStore = KeyStore.getInstance("BKS");
            keyStore.load(in, "t5ek71d".toCharArray());

            TrustManagerFactory factory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            factory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, factory.getTrustManagers(), null);

            OkHttpClient client = new OkHttpClient();
            client.setSslSocketFactory(sslContext.getSocketFactory());
            client.setHostnameVerifier(new AllowAllHostnameVerifier());

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("https://" + PrefUtils.getServerAddress())
                    //.setClient(new OkClient(client))
                    .setClient(new MockClient(client))
                    .build();
            if (BuildConfig.DEBUG) {
                restAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
            }
            sTransportApi = restAdapter.create(TransportApi.class);

            locationExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    ping();
                }
            }, 15, 15, TimeUnit.SECONDS);
        } catch (Exception e) {
            Log.w(TAG, "Failed to create OkHttpClient", e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    static class MockClient extends OkClient {
    	public MockClient(OkHttpClient client) {
    		super(client);
    	}
    	
    	@Override
    	public Response execute(Request request) throws IOException {
            Response response = super.execute(request);

    		Uri uri = Uri.parse(request.getUrl());
			if (uri.getPath().equals("/api/v1/ads0")) {
				response = new Response(request.getUrl(), 200, "nothing", request.getHeaders(),
						new TypedByteArray("application/json", getUrlBytes(
								"https://docs.google.com/uc?export=download&id=0B0lKX51T_sQFNEFUR2htRWktdjg")));
			} else if (uri.getPath().startsWith("/api/v1/media0")) {
				response = new Response(request.getUrl(), 200, "nothing", request.getHeaders(),
						new TypedByteArray("application/json", getAssetBytes("res.json")));
			}
            return response;
    	}
    	
    	private static byte[] getAssetBytes(String assetName) {
    		String result = null;
    		
    		InputStream is = null;
    		
    		try {
				is = TransportApplication.getAppContext().getAssets().open(assetName);
				result = IOUtils.toString(is);
			} catch (IOException e) {
				Log.e(TAG, "Error while reading asset file.", e);
			} finally {
				IOUtils.closeQuietly(is);
			}

    		return null == result ? null : result.getBytes();
    	}
    	
    	private static byte[] getUrlBytes(String url) {
    		String result = null;
    		
    		try {
				result = IOUtils.toString(new URL(url));
			} catch (Exception e) {
				Log.e(TAG, "Error while reading url file.", e);
			}
    		
    		return null == result ? null : result.getBytes();
    	}
    }
    
    public static TransportApi getApi() {
    	return sTransportApi;
    }
    
    private TransportApiHelper() {
    }

    public static void register(String number) {
        sTransportApi.register(number, new TransportCallback<RegisterResponse>(RegisterResponse.class));
    }

    public static GetRouteResponse getRoute() {
    	GetRouteResponse result = null;
        try {
        	result = sTransportApi.getRoute(PrefUtils.getDeviceGuid());
        } catch (RetrofitError error) {
            logError(error);
            return null;
        }
        if (BuildConfig.DEBUG) {
        	Log.d(TAG, "route loaded for deviceId: " + PrefUtils.getDeviceGuid() + ", deviceNum:" + PrefUtils.getDeviceNumber());
        }
        return result;
    }
    
    public static GetTestRouteResponse getTestRoute() {
    	GetTestRouteResponse result = null;
        try {
        	result = sTransportApi.getTestRoute(PrefUtils.getDeviceGuid());
        } catch (RetrofitError error) {
            logError(error);
            return null;
        }
        if (BuildConfig.DEBUG) {
        	Log.d(TAG, "test route loaded for deviceId: " + PrefUtils.getDeviceGuid());
        }
        return result;
    }

    public static void ping() {
        Location location = TransportApplication.getCurrentLocation();

        if (location != null) {
            try {
                PingResponse response = sTransportApi.ping(PrefUtils.getDeviceGuid(),
                        Collections.<GeoPoint>singletonList(new GeoPoint(location.getLatitude(), location.getLongitude())));
                if (response.getState().equals(PingResponse.STATE_NO_ROUTE)) {
                	//ContentResolver resolver = TransportApplication.getAppContext().getContentResolver();
                	//TransportProviderHelper.deleteAllVehicles(resolver);
                }
            } catch (RetrofitError error) {
                logError(error);
            }
        }
    }

    public static GetNewsResponse getNews() {
        // Request the news for the last 2 hours.
        try {
            return sTransportApi.getNews(PrefUtils.getDeviceGuid(),
                    (System.currentTimeMillis() - 2 * 60 * 60 * 1000) / 1000);
        } catch (RetrofitError error) {
            logError(error);
            return null;
        }
    }

    public static GetHoroscopeResponse getHoroscope () {
        try {
            return sTransportApi.getHoroscope(PrefUtils.getDeviceGuid());

        }catch (RetrofitError e){
            logError(e);
            return null;
        }
    }

    public static GetCurrencyResponse getCourse () {
        try {
            GetCurrencyResponse response = sTransportApi.getCurrency(PrefUtils.getDeviceGuid());
            return response;
        }catch (RetrofitError e){
            logError(e);
            return null;
        }
    }

    private static void logError(RetrofitError error) {
        TypedInput body;
        Response response = error.getResponse();
        if (response != null && (body = response.getBody()) != null
                && "application/json".equals(body.mimeType())) {
            try {
                Gson gson = new Gson();
                ErrorResponse errorResponse = gson.fromJson(IOUtils.toString(body.in(), "UTF-8"),
                        ErrorResponse.class);
                Log.w(TAG, errorResponse.toString());
            } catch (IOException e) {
                Log.w(TAG, "Failed to get ErrorResponse", e);
            }
        } else {
            Log.w(TAG, "Retrofit error: " + error.getMessage());
        }
    }

    private static class TransportCallback<T extends BaseResponse> implements Callback<T> {
        private final Class<T> mClazz;

        public TransportCallback(Class<T> clazz) {
            mClazz = clazz;
        }

        @Override
        public void success(T t, Response response) {
            EventBus.postEvent(t);
        }

        @Override
        public void failure(RetrofitError error) {
            logError(error);

            try {
                T response = mClazz.newInstance();
                response.success = false;
                EventBus.postEvent(response);
            } catch (Exception e) {
                Log.w(TAG, "Failed to instantiate the response object", e);
            }
        }
    }
    
	public static void sendAdReports(ArrayList<AdReport> adReports) {
		sTransportApi.sendReports(PrefUtils.getDeviceGuid(), adReports, new Callback<Void>() {
			@Override
			public void success(Void arg0, Response arg1) {
				if (BuildConfig.DEBUG) {
					Log.d(TAG, "Ads report was successfully sended");
				}
			}
			@Override
			public void failure(RetrofitError arg0) {
				Log.e(TAG, "Error while sending ads report");
			}
		});
	}
}

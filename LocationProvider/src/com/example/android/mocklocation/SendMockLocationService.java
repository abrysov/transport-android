/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.mocklocation;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;

import java.util.ArrayList;
import java.util.List;

/**
 * A Service that injects test Location objects into the Location Services back-end. All other
 * apps that are connected to Location Services will see the test location values instead of
 * real values, until the test is over.
 *
 * To use this service, define the mock location values you want to use in the class
 * MockLocationConstants.java, then call this Service with startService().
 */
public class SendMockLocationService extends Service implements
        ConnectionCallbacks, OnConnectionFailedListener {

    /**
     * Convenience class for passing test parameters from the Intent received in onStartCommand()
     * via a Message to the Handler. The object makes it possible to pass the parameters through the
     * predefined Message field Message.obj.
     */
    private class TestParam {

        public final String TestAction;
        public final int TestPause;
        public final int InjectionPause;

        public TestParam(String action, int testPause, int injectionPause) {

            TestAction = action;
            TestPause = testPause;
            InjectionPause = injectionPause;
        }
    }

    // Object that connects the app to Location Services
    LocationClient mLocationClient;

    // A background thread for the work tasks
    HandlerThread mWorkThread;

    // Indicates if the test run has started
    private boolean mTestStarted;

    /*
     * Stores an instance of the local broadcast manager. A local
     * broadcast manager ensures security, because broadcast intents are
     * limited to the current app.
     */
    private LocalBroadcastManager mLocalBroadcastManager;

    // Stores an instance of the object that dispatches work requests to the worker thread
    private Looper mUpdateLooper;

    // The Handler instance that does the actual work
    private UpdateHandler mUpdateHandler;

    // An array of test location data
    private TestLocation[] mLocationArray;

    // The time to wait before starting to inject the test locations
    private int mPauseInterval;

    // The time to wait between each test injection
    private int mInjectionInterval;

    // The type of test requested, either ACTION_START_ONCE or ACTION_START_CONTINUOUS
    private String mTestRequest;

    /**
     * Define a class that manages the work of injecting test locations, using the Android
     * Handler API. A Handler facilitates running the work on a separate thread, so that the test
     * loop doesn't block the UI thread.
     *
     * A Handler is an object that can run code on a thread. Handler methods allow you to associate
     * the object with a Looper, which dispatches Message objects to the Handler code. In turn,
     * Message objects contain data and instructions for the Handler's code. A Handler is
     * created with a default thread and default Looper, but you can inject the Looper from another
     * thread if you want. This is often done to associate a Handler with a HandlerThread thread
     * that runs in the background.
     */

    public class UpdateHandler extends Handler {

        /**
         * Create a new Handler that uses the thread of the HandlerThread that contains the
         * provided Looper object.
         *
         * @param inputLooper The Looper object of a HandlerThread.
         */
        public UpdateHandler(Looper inputLooper) {
            // Instantiate the Handler with a Looper connected to a background thread
            super(inputLooper);

        }

        /*
         * Do the work. The Handler's Looper dispatches a Message to handleMessage(), which then
         * runs the code it contains on the thread associated with the Looper. The Message object
         * allows external callers to pass data to handleMessage().
         *
         * handleMessage() assumes that the location client already has a connection to Location
         * Services.
         */
        @Override
        public void handleMessage(Message msg) {

            boolean testOnce = false;
            // Create a new Location to inject into Location Services
            Location mockLocation = new Location(LocationUtils.LOCATION_PROVIDER);

            // Time values to put into the mock Location
            long elapsedTimeNanos;
            long currentTime;

            // Get the parameters from the Message
            TestParam params = (TestParam) msg.obj;
            String action = params.TestAction;
            int pauseInterval = params.TestPause;
            int injectionInterval = params.InjectionPause;

            /*
             * Determine if this is a one-time run or a continuous run
             */
            if (TextUtils.equals(action, LocationUtils.ACTION_START_ONCE)) {
                testOnce = true;
            }

            // If a test run is not already in progress
            if (!mTestStarted) {

                // Flag that a test has started
                mTestStarted = true;

                // Start mock location mode in Location Services
                mLocationClient.setMockMode(true);

                // Remove the notification that testing is started
                removeNotification();

                // Add a notification that testing is in progress
                postNotification(getString(R.string.notification_content_test_running));

                /*
                 * Wait to allow the test to switch to the app under test, by putting the thread
                 * to sleep.
                 */
                try {
                    Thread.sleep((long) (pauseInterval * 1000));
                } catch (InterruptedException e) {
                    return;
                }

                // Get the device uptime and the current clock time
                elapsedTimeNanos = SystemClock.elapsedRealtimeNanos();
                currentTime = System.currentTimeMillis();

                /*
                 * Run the test loop, iterating through the array of test locations.
                 * Each test location is injected into Location Services, after which the
                 * thread is put to sleep for the requested interval.
                 *
                 * Uses a "do" loop so that one-time test and continuous test can share code.
                 */
                do {
                    for (int index = 0; index < mLocationArray.length; index++) {
                        /*
                         * Set the time values for the test location. Both an elapsed system uptime
                         * and the current clock time in UTC timezone must be specified.
                         */
                        mockLocation.setElapsedRealtimeNanos(elapsedTimeNanos);
                        mockLocation.setTime(currentTime);

                        // Set the location accuracy, latitude, and longitude
                        mockLocation.setAccuracy(mLocationArray[index].Accuracy);
                        mockLocation.setLatitude(mLocationArray[index].Latitude);
                        mockLocation.setLongitude(mLocationArray[index].Longitude);

                        // Inject the test location into Location Services
                        mLocationClient.setMockLocation(mockLocation);

                        // Wait for the requested update interval, by putting the thread to sleep
                        try {
                            Thread.sleep((long) (injectionInterval * 1000));
                        } catch (InterruptedException e) {
                            return ;
                        }

                            /*
                             * Change the elapsed uptime and clock time by the amount of time
                             * requested.
                             */
                            elapsedTimeNanos += (long) injectionInterval *
                                    LocationUtils.NANOSECONDS_PER_SECOND;
                            currentTime += injectionInterval *
                                    LocationUtils.MILLISECONDS_PER_SECOND;
                    }

                /*
                 * Run the "do" while "testOnce" is false. For a one-time test, testOnce is true,
                 * so the "do" loop runs only once. For a continuous test, testOnce is false, so the
                 * "do" loop runs indefinitely.
                 */
                } while (!testOnce);

                /*
                 * Testing is finished.
                 */

                // Turn mock mode off
                mLocationClient.setMockMode(false);

                // Flag that testing has stopped
                mTestStarted = false;

                // Clear the testing notification
                removeNotification();

                // Disconnect from Location Services
                mLocationClient.disconnect();

                // Send a message back to the main activity
                sendBroadcastMessage(LocationUtils.CODE_TEST_FINISHED, 0);

                // Stop the service
                stopSelf();

            // If a test run is already in progress
            } else {
                /*
                 * The Service received a request to start testing, but a test was already in
                 * progress. Send a message back to the main Activity, and ignore the request.
                 */
                sendBroadcastMessage(LocationUtils.CODE_IN_TEST,0);
            }
        }
    }

    /*
     * At startup, load the static mock location data from MockLocationConstants.java, then
     * create a HandlerThread to inject the locations and start it.
     */
    @Override
      public void onCreate() {
        /*
         * Load the mock location data from MockLocationConstants.java
         */
        mLocationArray = buildTestLocationArray();

        /*
         * Prepare to send status updates back to the main activity.
         * Get a local broadcast manager instance; broadcast intents sent via this
         * manager are only available within the this app.
         */
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);

        /*
         * Create a new background thread with an associated Looper that processes Message objects
         * from a MessageQueue. The Looper allows test Activities to send repeated requests to
         * inject mock locations from this Service.
         */
        mWorkThread = new HandlerThread("UpdateThread", Process.THREAD_PRIORITY_BACKGROUND);

        /*
         * Start the thread. Nothing actually runs until the Looper for this thread dispatches a
         * Message to the Handler.
         */
        mWorkThread.start();

        // Get the Looper for the thread
        mUpdateLooper = mWorkThread.getLooper();

        /*
         * Create a Handler object and pass in the Looper for the thread.
         * The Looper can now dispatch Message objects to the Handler's handleMessage() method.
         */
        mUpdateHandler = new UpdateHandler(mUpdateLooper);

        // Indicate that testing has not yet started
        mTestStarted = false;
      }

     /**
     * Post a notification to the notification bar. The title of the notification is fixed, but
     * the content comes from the input argument contentText. The notification does not contain
     * a content Intent, because the only destination it could have would be the main activity.
     * It's better to use the Recents button to go to the existing main activity.
     * @param contentText Text to use for the notification content (main line of expanded
     * notification).
     */
    private void postNotification(String contentText) {

        // An instance of NotificationManager is needed to issue a notification
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        /*
         * Instantiate a new notification builder, using the API version that's backwards
         * compatible to platform version 4.
         */
        NotificationCompat.Builder builder;

        // Get the notification title
        String contentTitle = this.getString(R.string.notification_title_test_start);

        // Add values to the builder
        builder = new NotificationCompat.Builder(this)
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.ic_notify)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText);

        /*
         * Post the notification. All notifications from InjectMockLocationService have the same
         * ID, so posting a new notification overwrites the old one.
         */
        notificationManager.notify(0, builder.build());
    }

    /**
     * Remove all notifications from the notification bar.
     */
    private void removeNotification() {

        // An instance of NotificationManager is needed to remove notifications
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Remove all notifications
        notificationManager.cancelAll();
    }

    /*
     * Since onBind is a static method, any subclass of Service must override it.
     * However, since this Service is not designed to be a bound Service, it returns null.
     */
    @Override
    public IBinder onBind(Intent inputIntent) {
        return null;
    }

    /*
     * Respond to an Intent sent by startService. onCreate() is called before this method,
     * to take care of initialization.
     *
     * This method responds to requests from the main activity to start testing.
     */
    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        // Get the type of test to run
        mTestRequest = startIntent.getAction();

        /*
         * If the incoming Intent was a request to run a one-time or continuous test
         */
        if (
               (TextUtils.equals(mTestRequest, LocationUtils.ACTION_START_ONCE))
               ||
               (TextUtils.equals(mTestRequest, LocationUtils.ACTION_START_CONTINUOUS))
           ) {

            // Get the pause interval and injection interval
            mPauseInterval = startIntent.getIntExtra(LocationUtils.EXTRA_PAUSE_VALUE, 2);
            mInjectionInterval = startIntent.getIntExtra(LocationUtils.EXTRA_SEND_INTERVAL, 1);

            // Post a notification in the notification bar that a test is starting
            postNotification(getString(R.string.notification_content_test_start));

            // Create a location client
            mLocationClient = new LocationClient(this, this, this);

            // Start connecting to Location Services
            mLocationClient.connect();

        } else if (TextUtils.equals(mTestRequest, LocationUtils.ACTION_STOP_TEST)) {

            // Remove any existing notifications
            removeNotification();

            // Send a message back to the main activity that the test is stopping
            sendBroadcastMessage(LocationUtils.CODE_TEST_STOPPED, 0);

            // Stop this Service
            stopSelf();
        }

        /*
         * Tell the system to keep the Service alive, but to discard the Intent that
         * started the Service
         */
        return Service.START_STICKY;
    }

    /**
     * Build an array of test location data for later use.
     *
     * @return An array of test location data
     */
    private TestLocation[] buildTestLocationArray() {
        int index = 0;
    	List<TestLocation> locations = new ArrayList<TestLocation>();

        // direct path
        //locations.add(new TestLocation(String.valueOf(++index), , , 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.707192, 37.395557, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.709271, 37.392725, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.710287, 37.391223, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.710891, 37.391094, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.711471, 37.391394, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.713937, 37.392982, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.715243, 37.401823, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.715363, 37.402509, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.715968, 37.402295, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.71737, 37.410749, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.718796, 37.418914, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.719261, 37.421489, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.719775, 37.424117, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.720446, 37.430072, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.720585, 37.431445, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.720784, 37.432239, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.721141, 37.435994, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.721388, 37.438623, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.721666, 37.441219, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.72192, 37.442882, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.722222, 37.444545, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.723183, 37.448622, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.724247, 37.452602, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.725564, 37.457742, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.72682, 37.464651, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.728029, 37.471517, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.731388, 37.488812, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.734287, 37.506236, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.736849, 37.52057, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.740183, 37.535676, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.744072, 37.545546, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.74615, 37.552027, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.746222, 37.554387, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.746439, 37.558421, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.745811, 37.564257, 3.0f));

        // reverse path
        locations.add(new TestLocation(String.valueOf(++index), 55.745739, 37.565202, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.74528, 37.568291, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.745232, 37.569793, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.74499, 37.570738, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.745014, 37.572325, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.746367, 37.572798, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.747333, 37.572883, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.748251, 37.572926, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.749435, 37.572454, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.750425, 37.572025, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.751946, 37.570008, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.751415, 37.567905, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.750715, 37.566661, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.750401, 37.565631, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.747092, 37.554387, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.745618, 37.549409, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.743855, 37.544688, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.740569, 37.536105, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.73825, 37.526234, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.737066, 37.520441, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.735689, 37.512931, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.734287, 37.505592, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.731388, 37.488812, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.72815, 37.471303, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.725854, 37.458664, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.724911, 37.454544, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.722978, 37.447056, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.722434, 37.444738, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.722011, 37.442464, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.721757, 37.440382, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.721479, 37.4374, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.720972, 37.4324, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.72102, 37.431563, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.720416, 37.425298, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.719944, 37.4221, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.719666, 37.420491, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.71743, 37.408217, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.717146, 37.406586, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.716457, 37.40311, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.716312, 37.401952, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.715406, 37.402499, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.715318, 37.401914, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.715291, 37.401748, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.71527, 37.401608, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.714147, 37.394233, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.714071, 37.393761, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.714017, 37.39353, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.713962, 37.393358, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.713893, 37.393192, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.713799, 37.39302, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.713666, 37.392865, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.713509, 37.39272, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.7133, 37.392591, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.711113, 37.391191, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.710956, 37.391138, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.710795, 37.391116, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.710629, 37.391143, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.710454, 37.391245, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.710221, 37.391443, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.710007, 37.391712, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.709713, 37.392125, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.709408, 37.392538, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.708704, 37.393514, 3.0f));
        locations.add(new TestLocation(String.valueOf(++index), 55.707991, 37.394469, 3.0f));




        // old version
//    	locations.add(new TestLocation(String.valueOf(++index), 55.745739, 37.565202, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.745279999999994, 37.568290999999995, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.745231999999994, 37.569793, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.744989999999994, 37.570738, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.746367, 37.572798, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.747333, 37.572883, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.748250999999996, 37.572925999999995, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.749435, 37.572454, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.750425, 37.572025, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.751946, 37.570008, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.751414999999994, 37.567904999999996, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.750715, 37.566660999999996, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.750401, 37.565630999999996, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.747091999999995, 37.554387, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.745618, 37.549409, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.743854999999996, 37.544688, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.740569, 37.536105, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.73825, 37.526233999999995, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.737066, 37.520441, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.735689, 37.512931, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.734286999999995, 37.505592, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.731387999999995, 37.488811999999996, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.72815, 37.471303, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.725854, 37.458664, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.724911, 37.454544, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.722978, 37.447055999999996, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.722434, 37.444738, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.722010999999995, 37.442464, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.721757, 37.440382, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.721478999999995, 37.4374, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.720971999999996, 37.4324, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.721019999999996, 37.431563, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.720416, 37.425298, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.719944, 37.4221, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.719666, 37.420491, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.71743, 37.408217, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.717146, 37.406586, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.716457, 37.40311, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.716311999999995, 37.401952, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.715405999999994, 37.402499, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.715317999999996, 37.401914, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.715291, 37.401748, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.71527, 37.401607999999996, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.714147, 37.394233, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.714071, 37.393761, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.714017, 37.39353, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.713961999999995, 37.393358, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.713893, 37.393192, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.713798999999995, 37.39302, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.713665999999996, 37.392865, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.713508999999995, 37.39272, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.7133, 37.392590999999996, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.711113, 37.391191, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.710955999999996, 37.391138, 3.0f));
//    	locations.add(new TestLocation(String.valueOf(++index), 55.710795, 37.391116, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.710629, 37.391143, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.710454, 37.391245, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.710221, 37.391442999999995, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.710007, 37.391712, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.709713, 37.392125, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.709407999999996, 37.392538, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.708704, 37.393513999999996, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.707991, 37.394469, 3.0f));

//        locations.add(new TestLocation(String.valueOf(++index), 55.749435,37.572454, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.750425,37.572025, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.751946,37.570008, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.751415,37.567905, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.750715,37.566661, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.750401,37.565631, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.748142,37.557992, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.745618,37.549409, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.743855,37.544688, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.740569,37.536105, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.738250,37.526234, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.737066,37.520441, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.735689,37.512931, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.734287,37.505592, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.731388,37.488812, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.728150,37.471303, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.725854,37.458664, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.724911,37.454544, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.722978,37.447056, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.722434,37.444738, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.722011,37.442464, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.721757,37.440382, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.721479,37.437400, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.720972,37.432400, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.721020,37.431563, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.720416,37.425298, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.719944,37.422100, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.719666,37.420491, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.717430,37.408217, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.717146,37.406586, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.716457,37.403110, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.716312,37.401952, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.715406,37.402499, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.715318,37.401914, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.715291,37.401748, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.715270,37.401608, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.714147,37.394233, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.714071,37.393761, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.714017,37.393530, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.713962,37.393358, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.713893,37.393192, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.713799,37.393020, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.713666,37.392865, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.713509,37.392720, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.713300,37.392591, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.711113,37.391191, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.710956,37.391138, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.710795,37.391116, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.710629,37.391143, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.710454,37.391245, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.710221,37.391443, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.710007,37.391712, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.709713,37.392125, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.709408,37.392538, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.708704,37.393514, 3.0f));
//        locations.add(new TestLocation(String.valueOf(++index), 55.707991,37.394469, 3.0f));

        // Return the temporary array
        return locations.toArray(new TestLocation[locations.size()]);
    }

    /*
     * Invoked by Location Services if a connection could not be established.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Send connection failure broadcast to main activity
        sendBroadcastMessage(LocationUtils.CODE_CONNECTION_FAILED, result.getErrorCode());

        // Shut down. Testing can't continue until the problem is fixed.
        stopSelf();
    }

    /**
     * Send a broadcast message back to the main Activity, indicating a change in status.
     *
     * @param code1 The main status code to return
     * @param code2 A subcode for the status code, or 0.
     */
    private void sendBroadcastMessage(int code1, int code2) {
      // Create a new Intent to send back to the main Activity
      Intent sendIntent = new Intent(LocationUtils.ACTION_SERVICE_MESSAGE);

      // Put the status codes into the Intent
      sendIntent.putExtra(LocationUtils.KEY_EXTRA_CODE1, code1);
      sendIntent.putExtra(LocationUtils.KEY_EXTRA_CODE2, code2);

      // Send the Intent
      mLocalBroadcastManager.sendBroadcast(sendIntent);

    }

    /*
     * When the client is connected, Location Services calls this method, which in turn
     * starts the testing cycle by sending a message to the Handler that injects the test locations.
     */
    @Override
    public void onConnected(Bundle arg0) {
        // Send message to main activity
        sendBroadcastMessage(LocationUtils.CODE_CONNECTED, 0);
        // Start injecting mock locations into Location Services
        // Get the HandlerThread's Looper and use it for our Handler
        mUpdateLooper = mWorkThread.getLooper();
        mUpdateHandler = new UpdateHandler(mUpdateLooper);

        // Get a message object from the global pool
        Message msg = mUpdateHandler.obtainMessage();

        TestParam testParams = new TestParam(mTestRequest, mPauseInterval, mInjectionInterval);

        msg.obj = testParams;

        // Fire off the injection loop
        mUpdateHandler.sendMessage(msg);
    }

    /*
     * If the client becomes disconnected without a call to LocationClient.disconnect(), Location
     * Services calls this method. If the test didn't finish, send a message to the main Activity.
     */
    @Override
    public void onDisconnected() {
        // If testing didn't finish, send an error message
        if (mTestStarted) {
            sendBroadcastMessage(LocationUtils.CODE_DISCONNECTED, LocationUtils.CODE_TEST_STOPPED);
        }
    }

}

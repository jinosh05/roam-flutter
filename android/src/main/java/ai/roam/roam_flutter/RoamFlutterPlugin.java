package ai.roam.roam_flutter;

import android.app.Activity;
import android.content.Context;
import android.location.Location;


import com.google.gson.Gson;
import com.roam.sdk.Roam;
import com.roam.sdk.RoamPublish;
import com.roam.sdk.RoamTrackingMode;
import com.roam.sdk.callback.RoamCallback;
import com.roam.sdk.callback.RoamCreateTripCallback;
import com.roam.sdk.callback.RoamLocationCallback;
import com.roam.sdk.callback.RoamLogoutCallback;

import com.roam.sdk.callback.RoamTripDetailCallback;
import com.roam.sdk.callback.RoamTripStatusCallback;
import com.roam.sdk.callback.RoamTripSummaryCallback;
import com.roam.sdk.models.RoamError;
import com.roam.sdk.models.RoamTripStatus;
import com.roam.sdk.models.RoamTripsStatus;
import com.roam.sdk.models.RoamUser;
import com.roam.sdk.models.createtrip.RoamCreateTrip;
import com.roam.sdk.models.gettrip.RoamTripDetail;
import com.roam.sdk.models.tripsummary.RoamTripSummary;
import com.roam.sdk.trips_v2.RoamTrip;
import com.roam.sdk.trips_v2.callback.RoamActiveTripsCallback;
import com.roam.sdk.trips_v2.callback.RoamDeleteTripCallback;
import com.roam.sdk.trips_v2.callback.RoamSyncTripCallback;
import com.roam.sdk.trips_v2.callback.RoamTripCallback;
import com.roam.sdk.trips_v2.models.EndLocation;
import com.roam.sdk.trips_v2.models.Error;
import com.roam.sdk.trips_v2.models.Errors;
import com.roam.sdk.trips_v2.models.Events;
import com.roam.sdk.trips_v2.models.RoamActiveTripsResponse;
import com.roam.sdk.trips_v2.models.RoamDeleteTripResponse;
import com.roam.sdk.trips_v2.models.RoamSyncTripResponse;
import com.roam.sdk.trips_v2.models.RoamTripResponse;
import com.roam.sdk.trips_v2.models.Route;
import com.roam.sdk.trips_v2.models.Routes;
import com.roam.sdk.trips_v2.models.StartLocation;
import com.roam.sdk.trips_v2.models.Stop;
import com.roam.sdk.trips_v2.models.User;
import com.roam.sdk.trips_v2.request.RoamTripStops;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import android.util.Log;

import androidx.annotation.NonNull;

import ai.roam.roam_flutter.json.JsonDecoder;
import ai.roam.roam_flutter.json.JsonEncoder;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/** RoamFlutterPlugin */
public class RoamFlutterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Activity activity;

  private static final String METHOD_INITIALIZE = "initialize";
  private static final String METHOD_GET_CURRENT_LOCATION = "getCurrentLocation";
  private static final String METHOD_CREATE_USER = "createUser";
  private static final String METHOD_UPDATE_CURRENT_LOCATION = "updateCurrentLocation";
  private static final String METHOD_START_TRACKING = "startTracking";
  private static final String METHOD_STOP_TRACKING = "stopTracking";
  private static final String METHOD_LOGOUT_USER = "logoutUser";
  private static final String METHOD_GET_USER = "getUser";
  private static final String METHOD_TOGGLE_LISTENER = "toggleListener";
  private static final String METHOD_GET_LISTENER_STATUS = "getListenerStatus";
  private static final String METHOD_TOGGLE_EVENTS = "toggleEvents";
  private static final String METHOD_SUBSCRIBE_LOCATION = "subscribeLocation";
  private static final String METHOD_SUBSCRIBE_USER_LOCATION = "subscribeUserLocation";
  private static final String METHOD_SUBSCRIBE_EVENTS = "subscribeEvents";
  private static final String METHOD_ENABLE_ACCURACY_ENGINE = "enableAccuracyEngine";
  private static final String METHOD_DISABLE_ACCURACY_ENGINE = "disableAccuracyEngine";
  private static final String METHOD_CREATE_TRIP = "createTrip";
  private static final String METHOD_UPDATE_TRIP = "updateTrip";
  private static final String METHOD_GET_TRIP_DETAILS = "getTripDetails";
  private static final String METHOD_GET_TRIP_STATUS = "getTripStatus";
  private static final String METHOD_SUBSCRIBE_TRIP_STATUS = "subscribeTripStatus";
  private static final String METHOD_UNSUBSCRIBE_TRIP_STATUS = "unSubscribeTripStatus";
  private static final String METHOD_START_TRIP = "startTrip";
  private static final String METHOD_START_QUICK_TRIP = "startQuickTrip";
  private static final String METHOD_PAUSE_TRIP = "pauseTrip";
  private static final String METHOD_RESUME_TRIP = "resumeTrip";
  private static final String METHOD_END_TRIP = "endTrip";
  private static final String METHOD_DELETE_TRIP = "deleteTrip";
  private static final String METHOD_GET_TRIP = "getTrip";
  private static final String METHOD_SYNC_TRIP = "syncTrip";
  static final String METHOD_SUBSCRIBE_TRIP = "subscribeTrip";
  static final String METHOD_UNSUBSCRIBE_TRIP = "unsubscribeTrip";
  private static final String METHOD_GET_ACTIVE_TRIPS = "getActiveTrips";
  private static final String METHOD_GET_TRIP_SUMMARY = "getTripSummary";
  private static final String METHOD_DISABLE_BATTERY_OPTIMIZATION = "disableBatteryOptimization";
  private static final String METHOD_OFFLINE_TRACKING = "offlineTracking";
  private static final String METHOD_ALLOW_MOCK_LOCATION = "allowMockLocation";

  private static final String TRACKING_MODE_PASSIVE = "passive";
  private static final String TRACKING_MODE_BALANCED = "balanced";
  private static final String TRACKING_MODE_ACTIVE = "active";
  private static final String TRACKING_MODE_CUSTOM = "custom";

  private Context context;

  private void setContext(Context context) {
    this.context = context;
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
    this.activity = activityPluginBinding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    this.activity = null;
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
    this.activity = activityPluginBinding.getActivity();
  }

  @Override
  public void onDetachedFromActivity() {
    this.activity = null;
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    this.context = flutterPluginBinding.getApplicationContext();
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "roam_flutter");
    channel.setMethodCallHandler(this);
  }

  public static void registerWith(PluginRegistry.Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_radar_io");
    final RoamFlutterPlugin plugin = new RoamFlutterPlugin();
    plugin.setContext(registrar.context());
    channel.setMethodCallHandler(plugin);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull final Result result) {
     try {
       switch (call.method) {
         case "getPlatformVersion":
           result.success("Android " + android.os.Build.VERSION.RELEASE);
           break;

         case METHOD_INITIALIZE:
           final String publishKey = call.argument("publishKey");
           Roam.initialize(this.context, publishKey);
           break;

         case METHOD_ALLOW_MOCK_LOCATION:
           final Boolean allow = call.argument("allowMockLocation");
           if(allow != null){
             Roam.allowMockLocation(allow);
           }
           break;

         case METHOD_GET_CURRENT_LOCATION:
           final Integer accuracy = call.argument("accuracy");
           Roam.getCurrentLocation(RoamTrackingMode.DesiredAccuracy.MEDIUM, accuracy, new RoamLocationCallback() {
             @Override
             public void location(Location location) {
               JSONObject coordinates = new JSONObject();
               JSONObject roamLocation = new JSONObject();
               try {
                 coordinates.put("latitude", location.getLatitude());
                 coordinates.put("longitude", location.getLongitude());
                 roamLocation.put("coordinate", coordinates);
                 roamLocation.put("altitude", location.getAltitude());
                 roamLocation.put("accuracy", location.getAccuracy());
                 String locationText = roamLocation.toString();
                 result.success(locationText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(RoamError roamError) {
               roamError.getCode();
               roamError.getMessage();
             }
           });
           break;

         case METHOD_CREATE_USER:
           final String description = call.argument("description");
           Roam.createUser(description, null, new RoamCallback() {
             @Override
             public void onSuccess(RoamUser roamUser) {
               JSONObject user = new JSONObject();
               try {
                 user.put("userId", roamUser.getUserId());
                 user.put("description", roamUser.getDescription());
                 String userText = user.toString();
                 result.success(userText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(RoamError roamError) {
               roamError.getMessage();
               roamError.getCode();
             }
           });
           break;

         case METHOD_GET_USER:
           final String userId = call.argument("userId");
           Roam.getUser(userId, new RoamCallback() {
             @Override
             public void onSuccess(RoamUser roamUser) {
               JSONObject user = new JSONObject();
               try {
                 user.put("userId", roamUser.getUserId());
                 user.put("description", roamUser.getDescription());
                 String userText = user.toString();
                 result.success(userText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(RoamError roamError) {
               roamError.getMessage();
               roamError.getCode();
             }
           });
           break;

         case METHOD_TOGGLE_LISTENER:
           final Boolean Events = call.argument("events");
           final Boolean Locations = call.argument("locations");
           Roam.toggleListener(Events, Locations, new RoamCallback() {
             @Override
             public void onSuccess(RoamUser roamUser) {
               JSONObject user = new JSONObject();
               try {
                 user.put("userId", roamUser.getUserId());
                 user.put("events", roamUser.getEventListenerStatus());
                 user.put("locations", roamUser.getLocationListenerStatus());
                 String userText = user.toString();
                 result.success(userText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(RoamError roamError) {
               roamError.getMessage();
               roamError.getCode();
             }
           });
           break;

         case METHOD_TOGGLE_EVENTS:
           final Boolean Geofence = call.argument("geofence");
           final Boolean Location = call.argument("location");
           final Boolean Trips = call.argument("trips");
           final Boolean MovingGeofence = call.argument("movingGeofence");
           Roam.toggleEvents(Geofence, Location, Trips, MovingGeofence, new RoamCallback() {
             @Override
             public void onSuccess(RoamUser roamUser) {
               JSONObject user = new JSONObject();
               try {
                 user.put("userId", roamUser.getUserId());
                 user.put("locationEvents", roamUser.getLocationEvents());
                 user.put("geofenceEvents", roamUser.getGeofenceEvents());
                 user.put("tripsEvents", roamUser.getTripsEvents());
                 user.put("movingGeofenceEvents", roamUser.getMovingGeofenceEvents());
                 String userText = user.toString();
                 result.success(userText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(RoamError roamError) {
               roamError.getMessage();
               roamError.getCode();
             }
           });
           break;

         case METHOD_GET_LISTENER_STATUS:
           Roam.getListenerStatus(new RoamCallback() {
             @Override
             public void onSuccess(RoamUser roamUser) {
               JSONObject user = new JSONObject();
               try {
                 user.put("userId", roamUser.getUserId());
                 user.put("events", roamUser.getEventListenerStatus());
                 user.put("locations", roamUser.getLocationListenerStatus());
                 user.put("locationEvents", roamUser.getLocationEvents());
                 user.put("geofenceEvents", roamUser.getGeofenceEvents());
                 user.put("tripsEvents", roamUser.getTripsEvents());
                 user.put("movingGeofenceEvents", roamUser.getMovingGeofenceEvents());
                 String userText = user.toString();
                 result.success(userText);
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }

             @Override
             public void onFailure(RoamError roamError) {
               roamError.getMessage();
               roamError.getCode();
             }
           });
           break;

         case METHOD_LOGOUT_USER:
           Roam.logout(new RoamLogoutCallback() {
             @Override
             public void onSuccess(String s) {

             }

             @Override
             public void onFailure(RoamError roamError) {
               roamError.getMessage();
               roamError.getCode();
             }
           });
           break;

         case METHOD_SUBSCRIBE_LOCATION:
           Roam.getListenerStatus(new RoamCallback() {
             @Override
             public void onSuccess(RoamUser roamUser) {
               String ownUserId = roamUser.getUserId();
               Roam.subscribe(Roam.Subscribe.LOCATION, ownUserId);
             }

             @Override
             public void onFailure(RoamError roamError) {
               roamError.getMessage();
               roamError.getCode();
             }
           });
           break;

         case METHOD_SUBSCRIBE_USER_LOCATION:
           final String otherUserId = call.argument("userId");
           Roam.subscribe(Roam.Subscribe.LOCATION, otherUserId);
           break;

         case METHOD_SUBSCRIBE_EVENTS:
           Roam.getListenerStatus(new RoamCallback() {
             @Override
             public void onSuccess(RoamUser roamUser) {
               String ownUserIdEvents = roamUser.getUserId();
               Roam.subscribe(Roam.Subscribe.EVENTS, ownUserIdEvents);
             }

             @Override
             public void onFailure(RoamError roamError) {
               roamError.getMessage();
               roamError.getCode();
             }
           });
           break;

         case METHOD_ENABLE_ACCURACY_ENGINE:
           Roam.enableAccuracyEngine();
           break;
         case METHOD_DISABLE_ACCURACY_ENGINE:
           Roam.disableAccuracyEngine();
           break;

         case METHOD_UPDATE_CURRENT_LOCATION:
           final Integer updateAccuracy = call.argument("accuracy");
           final String jsonString = call.argument("jsonObject");
           if (!jsonString.equals("")) {
             try {
               final JSONObject jsonObject = new JSONObject(jsonString);
               RoamPublish roamPublish = new RoamPublish.Builder()
                       .metadata(jsonObject)
                       .build();
               Roam.updateCurrentLocation(RoamTrackingMode.DesiredAccuracy.MEDIUM, updateAccuracy, roamPublish);
             } catch (Exception e) {
               e.printStackTrace();
             }
           }
           break;

         case METHOD_START_TRACKING:
           final String trackingMode = call.argument("trackingMode");

           switch (trackingMode) {
             case TRACKING_MODE_PASSIVE:
               RoamPublish roamPublish = new RoamPublish.Builder()
                       .build();
               Roam.publishAndSave(roamPublish);
               Roam.startTracking(RoamTrackingMode.PASSIVE);
               break;

             case TRACKING_MODE_BALANCED:
               RoamPublish roamPublishBalanced = new RoamPublish.Builder()
                       .build();
               Roam.publishAndSave(roamPublishBalanced);
               Roam.startTracking(RoamTrackingMode.BALANCED);
               break;

             case TRACKING_MODE_ACTIVE:
               RoamPublish roamPublishActive = new RoamPublish.Builder()
                       .build();
               Roam.publishAndSave(roamPublishActive);
               Roam.startTracking(RoamTrackingMode.ACTIVE);
               break;

             case TRACKING_MODE_CUSTOM:
               RoamTrackingMode customTrackingMode;
               final Map customMethods = call.argument("customMethods");
               if (customMethods.containsKey("distanceInterval")) {
                 final int distanceInterval = (int) customMethods.get("distanceInterval");
                 customTrackingMode = new RoamTrackingMode.Builder(distanceInterval, 30).setDesiredAccuracy(RoamTrackingMode.DesiredAccuracy.HIGH).build();
                 RoamPublish roamPublishCustom = new RoamPublish.Builder()
                         .build();
                 Roam.publishAndSave(roamPublishCustom);
                 Roam.startTracking(customTrackingMode);
               } else if (customMethods.containsKey("timeInterval")) {
                 final int timeInterval = (int) customMethods.get("timeInterval");
                 customTrackingMode = new RoamTrackingMode.Builder(timeInterval).setDesiredAccuracy(RoamTrackingMode.DesiredAccuracy.HIGH).build();
                 RoamPublish roamPublishCustom = new RoamPublish.Builder()
                         .build();
                 Roam.publishAndSave(roamPublishCustom);
                 Roam.startTracking(customTrackingMode);
               } else {
                 customTrackingMode = new RoamTrackingMode.Builder(15, 30).setDesiredAccuracy(RoamTrackingMode.DesiredAccuracy.HIGH).build();
                 RoamPublish roamPublishCustom = new RoamPublish.Builder()
                         .build();
                 Roam.publishAndSave(roamPublishCustom);
                 Roam.startTracking(customTrackingMode);
               }
               break;

             default:
               break;
           }
           break;

         case METHOD_STOP_TRACKING:
           Roam.stopTracking();
           break;


         case METHOD_OFFLINE_TRACKING:
           final boolean offlineTracking = call.argument("offlineTracking");
           Roam.offlineLocationTracking(offlineTracking);
           break;


         case METHOD_CREATE_TRIP:
           final String roamTripString = call.argument("roamTrip");
           if (roamTripString != null && !roamTripString.equals("")) {
             JSONObject roamTripJSON = new JSONObject(roamTripString);

             Log.e("TAG", "creating trip");

             RoamTrip trip = JsonDecoder.decodeRoamTrip(roamTripJSON);

             Log.e("TEST", "roam trip: " + new Gson().toJson(trip));


             Roam.createTrip(trip, new RoamTripCallback() {
               @Override
               public void onSuccess(RoamTripResponse roamTripResponse) {
                 Log.e("TEST", "create trip gson: " + new Gson().toJson(roamTripResponse));

                 JSONObject json = JsonEncoder.encodeRoamTripResponse(roamTripResponse);
                 result.success(json.toString());
               }

               @Override
               public void onError(Error error) {
                 JSONObject json = new JSONObject();
                 try {
                   json.put("error", JsonEncoder.encodeError(error));
                   result.success(json.toString());
                 } catch (JSONException e) {
                   e.printStackTrace();
                 }
               }
             });
           }

           break;


         case METHOD_UPDATE_TRIP:

           final String roamTripUpdateString = call.argument("roamTrip");
           if (roamTripUpdateString != null && !roamTripUpdateString.equals("")) {
             Log.e("TEST", roamTripUpdateString);
             JSONObject roamTripJSON = new JSONObject(roamTripUpdateString);
             RoamTrip trip = JsonDecoder.decodeRoamTrip(roamTripJSON);

             Log.e("TEST", "update trip: " + new Gson().toJson(trip));

               Roam.updateTrip(trip, new RoamTripCallback() {
                 @Override
                 public void onSuccess(RoamTripResponse roamTripResponse) {
                   JSONObject json = JsonEncoder.encodeRoamTripResponse(roamTripResponse);
                   result.success(json.toString());
                 }

                 @Override
                 public void onError(Error error) {
                   JSONObject json = new JSONObject();
                   try {
                     json.put("error", JsonEncoder.encodeError(error));
                     result.success(json.toString());
                   } catch (JSONException e) {
                     e.printStackTrace();
                   }
                 }
               });

             }

             break;


         case METHOD_GET_TRIP:

           final String getTripId = call.argument("tripId");

           Roam.getTrip(getTripId, new RoamTripCallback() {
             @Override
             public void onSuccess(RoamTripResponse roamTripResponse) {
               JSONObject json = JsonEncoder.encodeRoamTripResponse(roamTripResponse);
               result.success(json.toString());
             }

             @Override
             public void onError(Error error) {
               JSONObject json = new JSONObject();
               try {
                 json.put("error", JsonEncoder.encodeError(error));
                 result.success(json.toString());
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }
           });
           break;







             case METHOD_GET_TRIP_STATUS:
               final String statusTripId = call.argument("tripId");
               Roam.getTripStatus(statusTripId, new RoamTripStatusCallback() {

                 @Override
                 public void onSuccess(RoamTripsStatus roamTripsStatus) {
                   JSONObject trip = new JSONObject();
                   try {
                     trip.put("distance", roamTripsStatus.getDistance());
                     trip.put("speed", roamTripsStatus.getSpeed());
                     trip.put("duration", roamTripsStatus.getDuration());
                     trip.put("tripId", roamTripsStatus.getTripId());

                     String tripText = trip.toString();
                     result.success(tripText);
                   } catch (JSONException e) {
                     e.printStackTrace();
                   }
                 }

                 @Override
                 public void onFailure(RoamError roamError) {
                   roamError.getMessage();
                   roamError.getCode();
                 }
               });
               break;




             case METHOD_START_TRIP:
               final String startTripId = call.argument("tripId");
               Roam.startTrip(startTripId, new RoamTripCallback() {
                 @Override
                 public void onSuccess(RoamTripResponse roamTripResponse) {
                   JSONObject json = JsonEncoder.encodeRoamTripResponse(roamTripResponse);
                   result.success(json.toString());
                 }

                 @Override
                 public void onError(Error error) {
                   JSONObject json = new JSONObject();
                   try {
                     json.put("error", JsonEncoder.encodeError(error));
                     result.success(json.toString());
                   } catch (JSONException e) {
                     e.printStackTrace();
                   }
                 }
               });
               break;


         case METHOD_START_QUICK_TRIP:

           final String roamTripJSONString = call.argument("roamTrip");
           final String roamTrackingModeString = call.argument("roamTrackingMode");

             JSONObject roamTripJSON = new JSONObject(roamTripJSONString);
             RoamTrip roamTrip = JsonDecoder.decodeRoamTrip(roamTripJSON);

             JSONObject roamTrackingModeJSON = new JSONObject(roamTrackingModeString);
             RoamTrackingMode roamTrackingMode = JsonDecoder.decodeRoamTrackingMode(roamTrackingModeJSON);


             Roam.startTrip(roamTrip, roamTrackingMode, new RoamTripCallback() {
               @Override
               public void onSuccess(RoamTripResponse roamTripResponse) {
                 Log.e("TEST", "offline trip response: " + new Gson().toJson(roamTripResponse));
                 JSONObject json = JsonEncoder.encodeRoamTripResponse(roamTripResponse);
                 result.success(json.toString());
               }

               @Override
               public void onError(Error error) {
                 Log.e("TEST", "offline quick trip: " + new Gson().toJson(error));
                 JSONObject json = new JSONObject();
                 try {
                   json.put("error", JsonEncoder.encodeError(error));
                   result.success(json.toString());
                 } catch (JSONException e) {
                   e.printStackTrace();
                 }
               }
             });

           break;


         case METHOD_SUBSCRIBE_TRIP:

           final String subTripId = call.argument("tripId");
           Roam.subscribeTrip(subTripId);

           break;

         case METHOD_UNSUBSCRIBE_TRIP:
           final String unsubTripId = call.argument("tripId");
           Roam.unSubscribeTrip(unsubTripId);
           break;

             case METHOD_PAUSE_TRIP:
               final String pauseTripId = call.argument("tripId");
               Roam.pauseTrip(pauseTripId, new RoamTripCallback() {
                 @Override
                 public void onSuccess(RoamTripResponse roamTripResponse) {
                   JSONObject json = JsonEncoder.encodeRoamTripResponse(roamTripResponse);
                   result.success(json.toString());
                 }

                 @Override
                 public void onError(Error error) {
                   JSONObject json = new JSONObject();
                   try {
                     json.put("error", JsonEncoder.encodeError(error));
                     result.success(json.toString());
                   } catch (JSONException e) {
                     e.printStackTrace();
                   }
                 }
               });
               break;



             case METHOD_RESUME_TRIP:
               final String resumeTripId = call.argument("tripId");
               Roam.resumeTrip(resumeTripId, new RoamTripCallback() {
                 @Override
                 public void onSuccess(RoamTripResponse roamTripResponse) {
                   JSONObject json = JsonEncoder.encodeRoamTripResponse(roamTripResponse);
                   result.success(json.toString());
                 }

                 @Override
                 public void onError(Error error) {
                   JSONObject json = new JSONObject();
                   try {
                     json.put("error", JsonEncoder.encodeError(error));
                     result.success(json.toString());
                   } catch (JSONException e) {
                     e.printStackTrace();
                   }
                 }
               });
               break;



             case METHOD_END_TRIP:
               final String endTripId = call.argument("tripId");
               Boolean stopTracking = call.argument("stopTracking");

               if (stopTracking == null){
                 stopTracking = false;
               }

               Roam.endTrip(endTripId, stopTracking, new RoamTripCallback() {
                 @Override
                 public void onSuccess(RoamTripResponse roamTripResponse) {
                   JSONObject json = JsonEncoder.encodeRoamTripResponse(roamTripResponse);
                   result.success(json.toString());
                 }

                 @Override
                 public void onError(Error error) {
                   JSONObject json = new JSONObject();
                   try {
                     json.put("error", JsonEncoder.encodeError(error));
                     result.success(json.toString());
                   } catch (JSONException e) {
                     e.printStackTrace();
                   }
                 }
               });

               break;


         case METHOD_DELETE_TRIP:

           final String deleteTripId = call.argument("tripId");

           Roam.deleteTrip(deleteTripId, new RoamDeleteTripCallback() {
             @Override
             public void onSuccess(RoamDeleteTripResponse roamDeleteTripResponse) {
               JSONObject json = JsonEncoder.encodeRoamDeleteTripResponse(roamDeleteTripResponse);
               result.success(json.toString());
             }

             @Override
             public void onError(Error error) {
               JSONObject json = new JSONObject();
               try {
                 json.put("error", JsonEncoder.encodeError(error));
                 result.success(json.toString());
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }
           });

           return;


         case METHOD_SYNC_TRIP:

           final String syncTripId = call.argument("tripId");

           Roam.syncTrip(syncTripId, new RoamSyncTripCallback() {
             @Override
             public void onSuccess(RoamSyncTripResponse roamSyncTripResponse) {
               JSONObject json = JsonEncoder.encodeRoamSyncTripResponse(roamSyncTripResponse);
               result.success(json.toString());
             }

             @Override
             public void onError(Error error) {
               JSONObject json = new JSONObject();
               try {
                 json.put("error", JsonEncoder.encodeError(error));
                 result.success(json.toString());
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }
           });

           break;


         case METHOD_GET_ACTIVE_TRIPS:

           Boolean isLocal = call.argument("isLocal");

           if (isLocal == null){
             isLocal = false;
           }

           Roam.getActiveTrips(isLocal, new RoamActiveTripsCallback() {
             @Override
             public void onSuccess(RoamActiveTripsResponse roamActiveTripsResponse) {
               Log.e("ROAM", "active trips response: " +new Gson().toJson(roamActiveTripsResponse));
               JSONObject json = JsonEncoder.encodeRoamActiveTripsResponse(roamActiveTripsResponse);
               result.success(json.toString());
             }

             @Override
             public void onError(Error error) {
               JSONObject json = new JSONObject();
               try {
                 json.put("error", JsonEncoder.encodeError(error));
                 result.success(json.toString());
               } catch (JSONException e) {
                 e.printStackTrace();
               }
             }
           });

           break;






             case METHOD_GET_TRIP_SUMMARY:
               final String summaryTripId = call.argument("tripId");
               Roam.getTripSummary(summaryTripId, new RoamTripCallback() {
                 @Override
                 public void onSuccess(RoamTripResponse roamTripResponse) {
                   JSONObject json = JsonEncoder.encodeRoamTripResponse(roamTripResponse);
                   result.success(json.toString());
                 }

                 @Override
                 public void onError(Error error) {
                   JSONObject json = new JSONObject();
                   try {
                     json.put("error", JsonEncoder.encodeError(error));
                     result.success(json.toString());
                   } catch (JSONException e) {
                     e.printStackTrace();
                   }
                 }
               });
               break;

             case METHOD_DISABLE_BATTERY_OPTIMIZATION:
               Roam.disableBatteryOptimization();
               break;

             default:
               result.notImplemented();
               break;
           }


     } catch (Exception e) {
       e.printStackTrace();
     }
  }

    @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    this.context = null;
    channel.setMethodCallHandler(null);
  }








}

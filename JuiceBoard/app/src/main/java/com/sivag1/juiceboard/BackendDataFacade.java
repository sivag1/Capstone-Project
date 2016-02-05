package com.sivag1.juiceboard;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.sivag1.juiceboard.data.JuiceLevel;
import com.sivag1.juiceboard.data.MyProvider;
import com.splunk.mint.Mint;

import java.util.ArrayList;
import java.util.List;

import static nl.qbusict.cupboard.CupboardFactory.cupboard;


public class BackendDataFacade {

    public static void initializeBaaS(Context context) {
        Parse.initialize(context, "EWKm44HGUSbsPrqb2ME2HQcb6VVKmUnFOSqN8tav",
                "AEVDqq3qKFZ9h4nVmHlLJLoOKB68Z06BL1caoqVs");
        Mint.initAndStartSession(context, "d4514f5c");
    }

    public static void update(Context context, boolean isEnabled, Intent batteryStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String objectId = sp.getString("objectId", null);
        JuiceLevel juiceLevel = Utility.constructJuiceLevel(context, objectId, batteryStatus);

        if (objectId == null || objectId.length() == 0)
            if (!isEnabled)
                return;

        ParseObject po = mapJuiceLevelToPO(juiceLevel);
        po.saveInBackground();

//        context.getContentResolver().update(MyProvider.JUICELEVELS_URI,
//                cupboard().withEntity(JuiceLevel.class).toContentValues(juiceLevel),
//                "objectId = ", new String[]{objectId});

        fetchJuiceLevels(context, new ArrayList<JuiceLevel>());
    }

    private static ParseObject mapJuiceLevelToPO(JuiceLevel juiceLevel) {
        ParseObject po = new ParseObject("JuiceLevel");
        po.put("deviceModel", juiceLevel.getDeviceModel());
        po.put("deviceName", juiceLevel.getDeviceName());
        po.put("deviceId", juiceLevel.getDeviceId());
        po.put("chargingIndicator", juiceLevel.getChargingIndicator());
        po.put("pluggedIndicator", juiceLevel.getPluggedIndicator());
        po.put("lastKnownPercentage", juiceLevel.getLastKnownPercentage());
        po.put("batteryIcon", juiceLevel.getBatteryIcon());

        po.setACL(new ParseACL(ParseUser.getCurrentUser()));

        String objectId = juiceLevel.getObjectId();
        if (objectId != null && objectId.length() > 0)
            po.setObjectId(objectId);

        return po;
    }

    public static List<JuiceLevel> fetchJuiceLevels(Context context, List<JuiceLevel> listItems) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("JuiceLevel");
        List<ParseObject> parseObjects = null;

        try {
            query.setLimit(1000);
            parseObjects = query.find();
            mapPOToJuiceLevels(parseObjects, listItems);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Deleting and Inserting to DB
        //Deleting all to avoid re-insertion
        int y = context.getContentResolver().delete(MyProvider.JUICELEVELS_URI, null, null);

        //Bulk insert to DB
        ContentValues[] valuesArray = new ContentValues[listItems.size()];

        int i = 0;
        for (JuiceLevel jl : listItems) {
            ContentValues values = cupboard().withEntity(JuiceLevel.class).toContentValues(jl);
            valuesArray[i++] = values;
        }
        context.getContentResolver().bulkInsert(MyProvider.JUICELEVELS_URI, valuesArray);

        return listItems;
    }

    private static List<JuiceLevel> mapPOToJuiceLevels(List<ParseObject> parseObjects, List
            <JuiceLevel> listItems) {
        for (ParseObject po : parseObjects) {
            try {
                listItems.add(mapPOToJuiceLevelItem(po));
            } catch (Exception e) {
                //Do nothing if a record fails
                e.printStackTrace();
            }
        }
        return listItems;
    }

    private static JuiceLevel mapPOToJuiceLevelItem(ParseObject po) {
        JuiceLevel juiceLevel = new JuiceLevel();
        juiceLevel.setObjectId(po.getObjectId());
        juiceLevel.setLastKnownPercentage(po.getInt("lastKnownPercentage"));
        juiceLevel.setPluggedIndicator(po.getInt("pluggedIndicator"));
        juiceLevel.setDeviceName(po.getString("deviceName"));
        juiceLevel.setChargingIndicator(po.getInt("chargingIndicator"));
        juiceLevel.setDeviceId(po.getString("deviceId"));
        juiceLevel.setDeviceModel(po.getString("deviceModel"));
        juiceLevel.setBatteryIcon(po.getInt("batteryIcon"));
        juiceLevel.setLastUpdatedDate(po.getUpdatedAt());
        return juiceLevel;
    }

    public static String maintainUser(User user) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("accountId", user.getId());
        List<ParseUser> userList = null;
        try {
            userList = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }

        if (userList == null || userList.size() == 0) {
            ParseUser parseUser = new ParseUser();
            parseUser.setUsername(user.getId());
            parseUser.setPassword(user.getId());
            //parseUser.setEmail(user.getEmail());

// other fields can be set just like with ParseObject
            parseUser.put("accountId", user.getId());
            //parseUser.put("displayName", user.getDisplayName());

            try {
                parseUser.signUp();
            } catch (ParseException e) {
                e.printStackTrace();
                return e.getLocalizedMessage();
            }

//            parseUser.signUpInBackground(new SignUpCallback() {
//                public void done(ParseException e) {
//                    if (e == null) {
//                        // Hooray! Let them use the app now.
//                    } else {
//                        // Sign up didn't succeed. Look at the ParseException
//                        // to figure out what went wrong
//                    }
//                }
//            });
        }

        try {
            ParseUser.logIn(user.getId(), user.getId());
        } catch (ParseException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }

        return null;
    }

    public static String delete(Context context, boolean isDeleteAll) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String objectId = sp.getString("objectId", null);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("JuiceLevel");

        List<ParseObject> parseObjects = null;
        try {
            query.setLimit(1000);
            if (isDeleteAll) {
                parseObjects = query.find();
                context.getContentResolver().delete(MyProvider.JUICELEVELS_URI, null, null);
            }
            else {
                if (objectId != null) {
                    ParseObject po = query.get(objectId);
                    parseObjects = new ArrayList<ParseObject>();
                    parseObjects.add(po);
                    context.getContentResolver().delete(MyProvider.JUICELEVELS_URI, "objectId = ", new String[] {objectId});
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }

        if (parseObjects != null && parseObjects.size() > 0) {
            try {
                ParseObject.deleteAll(parseObjects);
            } catch (ParseException e) {
                e.printStackTrace();
                return e.getLocalizedMessage();
            }
        }

        return null;
    }

    public static String deleteUser(ParseUser parseUser) {
        List<ParseUser> userList = new ArrayList<ParseUser>();
        userList.add(parseUser);
        try {
            ParseUser.deleteAll(userList);
        } catch (ParseException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }

        return null;
    }
}

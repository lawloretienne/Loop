package com.etiennelawlor.loop.realm;

import android.content.Context;
import android.text.TextUtils;

import com.etiennelawlor.loop.LoopApplication;
import com.etiennelawlor.loop.realm.objects.RealmSuggestion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;

/**
 * Created by etiennelawlor on 10/8/15.
 */
public class RealmUtility {

    private static Realm mRealm;

    private static RealmConfiguration getRealmConfiguration(Context context) {
//        return new RealmConfiguration.Builder(context)
//                .name("loop.realm")
//                .schemaVersion(1)
//                .build();

        return new RealmConfiguration.Builder(context).build();
    }

    public static void saveQuery(String query){
        Context context = LoopApplication.getInstance().getApplicationContext();
        try{
            mRealm = Realm.getInstance(context);
        } catch (RealmMigrationNeededException e) {
            Realm.deleteRealm(getRealmConfiguration(context));
            mRealm = Realm.getInstance(context);
        }

        RealmSuggestion realmSuggestion = new RealmSuggestion();
        realmSuggestion.setToken(query);
        realmSuggestion.setTimestamp(new Date());
        mRealm.beginTransaction();
        // This will create a new one in Realm
        // realm.copyToRealm(obj);
        // This will update a existing one with the same id or create a new one instead
        mRealm.copyToRealmOrUpdate(realmSuggestion);

        mRealm.commitTransaction();

        mRealm.close();
    }

    public static void deleteQuery(String query){
        Context context = LoopApplication.getInstance().getApplicationContext();
        try{
            mRealm = Realm.getInstance(context);
        } catch (RealmMigrationNeededException e) {
            Realm.deleteRealm(getRealmConfiguration(context));
            mRealm = Realm.getInstance(context);
        }

        mRealm.beginTransaction();

        RealmSuggestion realmSuggestion
                = mRealm.where(RealmSuggestion.class)
                .equalTo("token", query)
                .findFirst();

        if(realmSuggestion != null){
            realmSuggestion.removeFromRealm();
        }

        mRealm.commitTransaction();

        mRealm.close();
    }

    public static List<String> getSuggestions(String query) {
        Context context = LoopApplication.getInstance().getApplicationContext();

        try{
            mRealm = Realm.getInstance(context);
        } catch (RealmMigrationNeededException e) {
            Realm.deleteRealm(getRealmConfiguration(context));
            mRealm = Realm.getInstance(context);
        }

        List<String> suggestions = new ArrayList<>();

        RealmResults<RealmSuggestion> realmResults
                = mRealm.where(RealmSuggestion.class)
                .contains("token", query)
                .findAll();

        realmResults.sort("timestamp", RealmResults.SORT_ORDER_DESCENDING); // Sort descending

        if (realmResults != null) {
            int size = (realmResults.size() > 5) ? 5 : realmResults.size();
            for (int i = 0; i < size; i++) {
                RealmSuggestion realmSuggestion = realmResults.get(i);
                if (realmSuggestion != null) {
                    String token = realmSuggestion.getToken();
                    if (!TextUtils.isEmpty(token)) {
                        suggestions.add(token);
                    }
                }
            }
        }

        mRealm.close();

        return suggestions;
    }
}

package com.etiennelawlor.loop.realm;

import android.text.TextUtils;

import com.etiennelawlor.loop.LoopApplication;
import com.etiennelawlor.loop.realm.objects.RealmSuggestion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.exceptions.RealmMigrationNeededException;
import timber.log.Timber;

/**
 * Created by etiennelawlor on 10/8/15.
 */
public class RealmUtility {

    private static Realm mRealm;

    public static void saveQuery(String query){
        try {
            mRealm = Realm.getInstance(LoopApplication.get().getApplicationContext());

            RealmSuggestion realmSuggestion = new RealmSuggestion();
            realmSuggestion.setToken(query);
            realmSuggestion.setTimestamp(new Date());
            mRealm.beginTransaction();
            // This will create a new one in Realm
            // realm.copyToRealm(obj);
            // This will update a existing one with the same id or create a new one instead
            mRealm.copyToRealmOrUpdate(realmSuggestion);


            mRealm.commitTransaction();

        } catch (RealmMigrationNeededException e) {
            // in this case you need migration.
            // https://github.com/realm/realm-java/tree/master/examples/migrationExample
        } finally {
            if (mRealm != null) {
                mRealm.close();
            }
        }
    }

    public static void deleteQuery(String query){
        try {
            mRealm = Realm.getInstance(LoopApplication.get().getApplicationContext());

            mRealm.beginTransaction();

            RealmSuggestion realmSuggestion
                    = mRealm.where(RealmSuggestion.class)
                    .equalTo("token", query)
                    .findFirst();

            if(realmSuggestion != null){
                realmSuggestion.removeFromRealm();
            }

            mRealm.commitTransaction();
        } catch (RealmMigrationNeededException e) {
            // in this case you need migration.
            // https://github.com/realm/realm-java/tree/master/examples/migrationExample
        } finally {
            if (mRealm != null) {
                mRealm.close();
            }
        }
    }

    public static List<String> getSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();

        try {
            mRealm = Realm.getInstance(LoopApplication.get().getApplicationContext());

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

        } catch (RealmMigrationNeededException e) {
            // in this case you need migration.
            // https://github.com/realm/realm-java/tree/master/examples/migrationExample
            Timber.e("RealmMigrationNeededException");
        } finally {
            if (mRealm != null) {
                mRealm.close();
            }
        }

        return suggestions;
    }
}

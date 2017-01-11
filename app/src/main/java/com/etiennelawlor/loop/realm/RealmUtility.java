package com.etiennelawlor.loop.realm;

import android.text.TextUtils;

import com.etiennelawlor.loop.realm.objects.RealmSuggestion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by etiennelawlor on 10/8/15.
 */
public class RealmUtility {

    public static void saveQuery(String query){
        Realm realm = Realm.getDefaultInstance();
        try {
            RealmSuggestion realmSuggestion = new RealmSuggestion();
            realmSuggestion.setToken(query);
            realmSuggestion.setTimestamp(new Date());
            realm.beginTransaction();
            // This will create a new one in Realm
            // realm.copyToRealm(obj);
            // This will update a existing one with the same id or create a new one instead
            realm.copyToRealmOrUpdate(realmSuggestion);

            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    public static void deleteQuery(String query){
        Realm realm = Realm.getDefaultInstance();
        try {
            realm.beginTransaction();

            RealmSuggestion realmSuggestion
                    = realm.where(RealmSuggestion.class)
                    .equalTo("token", query)
                    .findFirst();

            if(realmSuggestion != null){
                realmSuggestion.deleteFromRealm();
            }

            realm.commitTransaction();
        } finally {
            realm.close();
        }
    }

    public static List<String> getSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();

        Realm realm = Realm.getDefaultInstance();
        try {
            RealmResults<RealmSuggestion> realmResults
                    = realm.where(RealmSuggestion.class)
                    .contains("token", query)
                    .findAll();

            realmResults = realmResults.sort("timestamp", Sort.DESCENDING);

            if (realmResults != null) {
                int size = (realmResults.size() > 5) ? 5 : realmResults.size();
                for (int i = 0; i < size; i++) {
                    RealmSuggestion realmSuggestion = realmResults.get(i);
                    if (realmSuggestion != null) {
                        String token = realmSuggestion.getToken();
                        if (!TextUtils.isEmpty(token)) {
                            suggestions.add(suggestions.size(), token);
                        }
                    }
                }
            }
        } finally {
            realm.close();
        }

        return suggestions;
    }
}

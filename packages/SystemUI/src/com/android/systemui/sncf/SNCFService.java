/*
 * Copyright (C) 2023 The Android Open Source Project
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

package com.android.systemui.sncf;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;


import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SNCFService extends Service {
    private static final String TOKEN = "8cc3c574-5fc3-4728-9dea-b6cffc2aec4e";
    private static final String JOURNEY_ENDPOINT = "https://api.sncf.com/v1/coverage/sncf/journeys";
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    public static final String LOG = "SNCFService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG, "onBind");
        return new SNCFServiceBinder(this);
    }

    public CompletableFuture<List<Journey>> requestJourney(String from, String to,
            @Nullable Date date) {
        Log.i(LOG, "Request Journeys");
        return CompletableFuture.supplyAsync(() ->
                SNCFResponse.Companion.fromJson(requestStringJourney(from, to, date))
                        .getJourneys());
    }

    private String requestStringJourney(String from, String to, @Nullable Date date) {
        String query = "?count=15&from=" + from + "&to=" + to;
        if (date != null) {
            query += "datetime" + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(date.toInstant());
        }
        return request(JOURNEY_ENDPOINT, query);
    }

    private String request(String sUrl, String query) {
        try {
            URL url = new URL(sUrl + query);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", TOKEN);

            if (con.getResponseCode() != 200) {
                String result = new BufferedReader(new InputStreamReader(con.getErrorStream()))
                        .lines().parallel().collect(Collectors.joining("\n"));
                throw new RuntimeException("Got " + con.getResponseCode() + " " + result);
            }

            String result = new BufferedReader(new InputStreamReader(con.getInputStream()))
                    .lines().parallel().collect(Collectors.joining("\n"));
            con.disconnect();
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

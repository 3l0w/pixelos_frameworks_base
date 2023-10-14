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

package com.android.systemui.sncf

import android.util.Log
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.google.gson.annotations.SerializedName
import java.time.temporal.TemporalAccessor
import org.json.JSONObject

data class SNCFResponse(val journeys: List<Journey>) {
    companion object {
        fun fromJson(json: String): SNCFResponse {
            val obj = JSONObject(json);
            val journeysJson = obj.getJSONArray("journeys");
            val journeysObj = mutableListOf<JSONObject>();
            for (i in 0 until journeysJson.length()) journeysObj.add(journeysJson.getJSONObject(i))
            val journeys = journeysObj.map { Journey(it.getLong("duration"), it.getString("departure_date_time"), it.getString("arrival_date_time")) }

            return SNCFResponse(journeys)
        }
    }
}

data class Journey(@SerializedName("duration") val duration: Long, @SerializedName("departure_date_time") private val departureDateTime: String, @SerializedName("arrival_date_time") private val arrivalDateTime: String) {
    fun getDeparture() = parse(departureDateTime);
    fun getArrival() = parse(arrivalDateTime);
    override fun toString(): String {
        return "Journey(duration=$duration, departure='${getDeparture()}', arrival='${getArrival()}')"
    }

    private fun parse(string: String): TemporalAccessor {
        return DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").withZone(ZoneId.systemDefault()).parse(string)
    }
}

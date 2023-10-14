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

package com.android.systemui.qs.tiles.dialog;

import com.android.systemui.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import android.util.Log;
import androidx.recyclerview.widget.RecyclerView;

import com.android.systemui.sncf.Journey;

import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.util.ArrayList;
import java.util.List;

public class SNCFViewAdapter extends RecyclerView.Adapter<SNCFViewAdapter.SNCFViewHolder> {

    private final SNCFDialog mDialog;
    private List<Journey> mJourneyList;

    public SNCFViewAdapter(SNCFDialog dialog) {
        mDialog = dialog;
        mJourneyList = new ArrayList<>();
    }

    @NotNull
    @Override
    public SNCFViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        View holderView = LayoutInflater.from(context)
                .inflate(R.layout.sncf_list_item, viewGroup, false);
        Log.i("SNCFViewAdapter", "onCreateViewHolder");
        return new SNCFViewHolder(holderView);
    }

    @Override
    public void onBindViewHolder(@NotNull SNCFViewHolder viewHolder, int i) {
        if (i >= mJourneyList.size()) {
          Log.i("SNCFViewAdapter", "onBindEmpty");
            return;
        }
        Journey journey = mJourneyList.get(i);
        viewHolder.onBind(journey);
    }

    public void setJourneyList(List<Journey> journeyList) {
        mJourneyList = journeyList;
        notifyDataSetChanged();
        Log.i("SNCFViewAdapter", "dataSetChanged");
    }

    @Override
    public int getItemCount() {
        return mJourneyList.size();
    }

    static class SNCFViewHolder extends RecyclerView.ViewHolder {

        private final TextView mDeparture;
        private final TextView mArrival;
        private final LinearLayout mSNCFListLayout;

        public SNCFViewHolder(@NotNull View view) {
            super(view);
            mDeparture = view.requireViewById(R.id.departure);
            mArrival = view.requireViewById(R.id.arrival);
            mSNCFListLayout = view.requireViewById(R.id.sncf_list);
        }

        void onBind(@NotNull Journey journey) {
            Log.i("SNCFViewHolder", "onBind " + journey);
            mSNCFListLayout.setVisibility(View.VISIBLE);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String departure = formatter.format(journey.getDeparture());
            String arrival = formatter.format(journey.getArrival());
            mDeparture.setText(departure);
            mArrival.setText(arrival);
        }
    }
}

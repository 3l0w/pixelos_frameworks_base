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

import android.content.Intent;
import android.util.Log;

import com.android.systemui.sncf.SNCFService;
import com.android.systemui.sncf.SNCFServiceConnection;
import com.android.systemui.statusbar.phone.SystemUIDialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.os.Handler;

import com.android.systemui.R;

import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SNCFDialog extends SystemUIDialog implements Window.Callback {
    private static final String TAG = "SNCFDialog";
    private static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private static final List<Plan> PLAN_LIST = List.of(
            new Plan("admin:fr:35184", "Montauban", "admin:fr:35238", "Rennes"),
            new Plan("admin:fr:35238", "Rennes", "admin:fr:35184", "Montauban"),
            new Plan("admin:fr:35188", "Montfort", "admin:fr:35238", "Rennes"),
            new Plan("admin:fr:35238", "Rennes", "admin:fr:35188", "Montfort")
    );


    private final SNCFViewAdapter mAdapter;
    private final SNCFDialogFactory mSNCFDialogFactory;
    private final Handler mHandler;
    private final Context mContext;

    private SNCFServiceConnection mSNCFServiceConnection;
    private View mDialogView;
    private TextView mSNCFDialogTitle, mSNCFDialogSubTitle;
    private ProgressBar mProgressBar;
    private View mDivider;
    private RecyclerView mSNCFRecyclerView;
    private Button mDoneButton;

    private int currentPlan = 0;
    private SNCFService mSNCFService;

    public SNCFDialog(Context context, Handler handler, SNCFDialogFactory sncfDialogFactory) {
        super(context);
        mContext = getContext();
        mSNCFDialogFactory = sncfDialogFactory;
        mAdapter = new SNCFViewAdapter(this);
        mHandler = handler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            Log.d(TAG, "onCreate");
        }

        mDialogView = LayoutInflater.from(mContext).inflate(R.layout.sncf_dialog, null);
        final Window window = getWindow();
        window.setContentView(mDialogView);
        window.setWindowAnimations(R.style.Animation_InternetDialog);
        mSNCFDialogTitle = mDialogView.requireViewById(R.id.sncf_dialog_title);
        mSNCFDialogSubTitle = mDialogView.requireViewById(R.id.sncf_dialog_subtitle);
        mProgressBar = mDialogView.requireViewById(R.id.sncf_progress);
        mDivider = mDialogView.requireViewById(R.id.divider);
        mSNCFRecyclerView = mDialogView.requireViewById(R.id.sncf_list_layout);
        mDoneButton = mDialogView.requireViewById(R.id.done_button);

        mDoneButton.setOnClickListener((v) -> dismissDialog());
        mSNCFRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mSNCFRecyclerView.setAdapter(mAdapter);


        showProgress(true);
        mSNCFDialogSubTitle.setOnClickListener(v -> {
            if (this.mSNCFService == null) {
                return;
            }
            this.currentPlan++;
            if (this.currentPlan >= PLAN_LIST.size()) {
                this.currentPlan = 0;
            }
            this.update(mSNCFService);
        });
        mSNCFServiceConnection = new SNCFServiceConnection(this::update);
        Log.i(TAG, "bindService");
        Intent intent = new Intent(mContext, SNCFService.class);
        Log.i(TAG, "Class: " + SNCFService.class);
        Log.i(TAG, "Intent: " + intent);

        Log.i(TAG, "start: " + mContext.startService(intent));
        Log.i(TAG, "bind: " + mContext.bindService(
                new Intent(mContext, SNCFService.class),
                mSNCFServiceConnection,
                Context.BIND_AUTO_CREATE
        ));
    }

    public void dismissDialog() {
        Log.i(TAG, "dismissDialog");
        if (DEBUG) {
        }
        mSNCFDialogFactory.destroyDialog();
        dismiss();
        mContext.unbindService(mSNCFServiceConnection);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
        if (DEBUG) {
        }
        mSNCFDialogFactory.destroyDialog();
        mContext.unbindService(mSNCFServiceConnection);
    }

    private void showProgress(boolean show) {
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mDivider.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void update(SNCFService sncfService) {
        if (this.mSNCFService == null) {
            this.mSNCFService = sncfService;
        }
        Log.i(TAG, "update " + sncfService);
        Plan plan = PLAN_LIST.get(this.currentPlan);
        mSNCFDialogSubTitle.setText("Trajets de " + plan.fromName + " à " + plan.toName);
        showProgress(true);
        mAdapter.setJourneyList(new ArrayList<>());
        sncfService.requestJourney(plan.from, plan.to, null)
                .whenComplete((journeys, throwable) -> {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        Log.e(TAG, "Journey request error 1.3 " + throwable.getClass(), throwable);
                        return;
                    }
                    Log.i(TAG, "Got journeys: " + journeys);
                    if (journeys == null) {
                        Log.e(TAG, "Journeys is null!!");
                        return;
                    }

                    mHandler.post(() -> {
                        mAdapter.setJourneyList(journeys);
                        showProgress(false);
                    });

                });
    }

    private static class Plan {
        String from;
        String fromName;
        String to;
        String toName;

        public Plan(String from, String fromName, String to, String toName) {
            this.from = from;
            this.fromName = fromName;
            this.to = to;
            this.toName = toName;
        }
    }
}

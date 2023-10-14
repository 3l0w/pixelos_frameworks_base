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

package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;


import androidx.annotation.Nullable;

import com.android.systemui.R;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.dagger.qualifiers.Background;
import com.android.systemui.dagger.qualifiers.Main;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.logging.QSLogger;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.qs.tiles.dialog.SNCFDialogFactory;

import android.service.quicksettings.Tile;

import javax.inject.Inject;

public class SNCFTile extends QSTileImpl {
    public static final String TILE_SPEC = "sncf";

    private final SNCFDialogFactory mSNCFDialogFactory;
    private final Handler mHandler;

    @Inject
    SNCFTile(
            QSHost host,
            @Background Looper backgroundLooper,
            @Main Handler mainHandler,
            FalsingManager falsingManager,
            MetricsLogger metricsLogger,
            StatusBarStateController statusBarStateController,
            ActivityStarter activityStarter,
            QSLogger qsLogger,
            SNCFDialogFactory sncfDialogFactory
    ) {
        super(host, backgroundLooper, mainHandler, falsingManager, metricsLogger,
                statusBarStateController, activityStarter, qsLogger);
        mSNCFDialogFactory = sncfDialogFactory;
        mHandler = mainHandler;
    }

    @Override
    public State newTileState() {
        return new QSTile.State();
    }

    @Override
    protected void handleClick(@Nullable View view) {
        mHandler.post(() -> mSNCFDialogFactory.create(view));
    }

    @Override
    protected void handleUpdateState(State state, Object arg) {
        state.label = mContext.getString(R.string.quick_settings_sncf_label);
        state.icon = ResourceIcon.get(R.drawable.ic_qs_vpn);
        state.state = Tile.STATE_ACTIVE;
        state.secondaryLabel = null;
    }

    @Nullable
    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public CharSequence getTileLabel() {
        return "Trains";
    }
}

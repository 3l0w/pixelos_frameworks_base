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

import android.util.Log;
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import java.util.function.Consumer

class SNCFServiceBinder(val sncfService: SNCFService) : Binder("SNCF")

class SNCFServiceConnection(val consumer: Consumer<SNCFService>) : ServiceConnection {

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.i("SNCFServiceConnection", "Connected $name $service");
        consumer.accept((service as SNCFServiceBinder).sncfService)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        Log.i("SNCFServiceConnection", "Disconnect");
    }
}
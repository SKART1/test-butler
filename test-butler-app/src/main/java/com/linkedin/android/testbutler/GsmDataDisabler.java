/**
 * Copyright (C) 2016 LinkedIn Corp.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.android.testbutler;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.RemoteException;

import com.linkedin.android.testbutler.utils.ExceptionCreator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A helper class which can enable or disable data transferring
 */
/* package */ final class GsmDataDisabler {
    private static final String TAG = GsmDataDisabler.class.getSimpleName();

    public boolean setGsmState(Context context, boolean enabled) throws RemoteException {
        Object manager;
        Method method;

        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                try {
                    manager = context.getSystemService(Context.CONNECTIVITY_SERVICE);
                    if (manager != null) {
                        method = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
                    } else {
                        throw ExceptionCreator.createRemoteException(TAG, "No service " + ContextWrapper.TELEPHONY_SERVICE + " (TelephonyManager) found on device", null);
                    }

                    if (method != null) {
                        method.setAccessible(true);
                        method.invoke(manager, enabled);
                        method.setAccessible(false);
                    } else {
                        throw ExceptionCreator.createRemoteException(TAG, "No setDataEnabled(boolean) method inside TelephonyManager", null);
                    }
                } catch (NoSuchMethodException e) {
                    throw ExceptionCreator.createRemoteException(TAG, "No setDataEnabled(boolean) method inside TelephonyManager", e);
                } catch (InvocationTargetException e) {
                    throw ExceptionCreator.createRemoteException(TAG, "Invocation exception in setDataEnabled(boolean) method inside TelephonyManager", e);
                } catch (IllegalAccessException e) {
                    throw ExceptionCreator.createRemoteException(TAG, "IllegalAccessException exception in setDataEnabled(boolean) method inside TelephonyManager", e);
                }
            } else {
                throw ExceptionCreator.createRemoteException(TAG, "Api before " + Build.VERSION_CODES.KITKAT + " not supported because of WTF", null);
            }
        } else {
            try {
                manager = context.getSystemService(ContextWrapper.TELEPHONY_SERVICE);
                if (manager != null) {
                    method = manager.getClass().getDeclaredMethod("setDataEnabled", boolean.class);
                } else {
                    throw ExceptionCreator.createRemoteException(TAG, "No service " + ContextWrapper.TELEPHONY_SERVICE + " (TelephonyManager) found on device", null);
                }

                if (method != null) {
                    method.invoke(manager, enabled);
                } else {
                    throw ExceptionCreator.createRemoteException(TAG, "No setDataEnabled(boolean) method inside TelephonyManager", null);
                }
            } catch (NoSuchMethodException e) {
                throw ExceptionCreator.createRemoteException(TAG, "No setDataEnabled(boolean) method inside TelephonyManager", e);
            } catch (InvocationTargetException e) {
                throw ExceptionCreator.createRemoteException(TAG, "Invocation exception in setDataEnabled(boolean) method inside TelephonyManager", e);
            } catch (IllegalAccessException e) {
                throw ExceptionCreator.createRemoteException(TAG, "IllegalAccessException exception in setDataEnabled(boolean) method inside TelephonyManager", e);
            }
        }
        return true;
    }

    private RemoteException createException(@NonNull String message, @Nullable Exception exception) {
        RemoteException remoteException;
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            remoteException = new RemoteException(message);
        } else {
            Log.e(TAG, message, exception);
            remoteException = new RemoteException();
        }

        if(exception != null) {
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                remoteException.addSuppressed(exception);
            } else {
                Log.e(TAG, message, exception);
            }
        }
        return remoteException;
    }
}

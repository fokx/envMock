package com.pecpwee.lib.envmock.middle.connect;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

import com.pecpwee.lib.envmock.hook.CenterServiceManager;
import com.pecpwee.lib.envmock.middle.IMiddleService;
import com.pecpwee.lib.envmock.utils.reflect.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * Created by pw on 2017/9/4.
 *
 * * http://androidxref.com/7.1.1_r6/xref/frameworks/base/core/java/android/net/IConnectivityManager.aidl
 *
 *    /*
 * 46    Network getActiveNetwork();
 47    Network getActiveNetworkForUid(int uid, boolean ignoreBlocked);
 48    NetworkInfo getActiveNetworkInfo();
 49    NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked);

 50    NetworkInfo getNetworkInfo(int networkType);
 51    NetworkInfo getNetworkInfoForUid(in Network network, int uid, boolean ignoreBlocked);
 52    NetworkInfo[] getAllNetworkInfo();
 53    Network getNetworkForType(int networkType);
 54    Network[] getAllNetworks();
 55    NetworkCapabilities[] getDefaultNetworkCapabilitiesForUser(int userId);

 *


 */

public class MiddleConnectivityManager implements IMiddleService, IConnectivityListener {

    private HashMap<Network, NetworkWrapper> mNetworkMap = new HashMap<>();
    private Network[] mAllNetworks;
    private NetworkInfo[] mAllNetworkInfos;
    private NetworkCapabilities[] mAllNetworkCapacities;
    private NetworkInfo mActiveNetworkInfo;
    private Network mActiveNetwork;
    private ConnectivityManager connManager;

    public MiddleConnectivityManager() {
        connManager = (ConnectivityManager) CenterServiceManager.getInstance().getServiceFetcher(Context.CONNECTIVITY_SERVICE)
                .getOrigManagerObj();
    }


    @Override
    public void resetMockData() {
        mActiveNetwork = null;
        mAllNetworks = null;
        mAllNetworkCapacities = null;
        mAllNetworkInfos = null;
        mNetworkMap.clear();
    }


    public synchronized NetworkInfo[] getAllNetworkInfo() {
        return mAllNetworkInfos;
    }

    public synchronized NetworkCapabilities[] getAllNetworkCapacities() {
        return mAllNetworkCapacities;
    }

    public synchronized Network getActiveNetwork() {
        return mActiveNetwork;
    }

    public synchronized NetworkInfo getActiveNetworkInfo() {
        return mActiveNetworkInfo;
    }

    public synchronized NetworkInfo getNetworkInfo(int nettype) {
        if (getActiveNetworkInfo().getType() == nettype) {
            return mActiveNetworkInfo;
        }
        for (Network network : mNetworkMap.keySet()) {
            NetworkWrapper valueWrapper = mNetworkMap.get(network);
            if (valueWrapper.networkInfo.getType() == nettype) {
                return valueWrapper.networkInfo;
            }
        }
        return createDefaultNetworkInfo(nettype);
    }

    private NetworkInfo createDefaultNetworkInfo(int networkType) {
        NetworkInfo networkinfo = null;
        try {
            networkinfo = MethodUtils.invokeConstructor(NetworkInfo.class, networkType, 0,
                    getNetworkTypeName(networkType), "");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        try {
            MethodUtils.invokeMethod(networkinfo, "setDetailedState", NetworkInfo.DetailedState.DISCONNECTED, null, null);
            MethodUtils.invokeMethod(networkinfo, "setIsAvailable", true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return networkinfo;
    }

    public String getNetworkTypeName(int nettype) {
        try {
            return (String) MethodUtils.invokeStaticMethod(
                    ConnectivityManager.class
                    , "getNetworkTypeName"
                    , new Object[]{nettype});
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;

    }

    NetworkInfo getNetworkInfo(Network network) {
        if (network == null) {
            return null;
        }
        NetworkWrapper wrapper = mNetworkMap.get(network);
        if (wrapper == null) {
            return null;
        }
        return wrapper.networkInfo;
    }


    public Network[] getAllNetworks() {
        return mAllNetworks;
    }


    public NetworkCapabilities getNetworkCapabilities(Network network) {

        if (network == null) {
            return null;
        }
        NetworkWrapper wrapper = mNetworkMap.get(network);
        if (wrapper == null) {
            return null;
        }
        return wrapper.networkCapabilities;

    }


    @Override
    public synchronized void setActiveNetwork(Network network, NetworkInfo networkInfo) {
        this.mActiveNetwork = network;
        this.mActiveNetworkInfo = networkInfo;
    }

    @Override
    public synchronized void setAllNetworks(Network[] networks
            , NetworkInfo[] networkInfos
            , NetworkCapabilities[] capabilitiesArray) {
        this.mAllNetworks = networks;
        this.mAllNetworkInfos = networkInfos;
        this.mAllNetworkCapacities = capabilitiesArray;
        mNetworkMap.clear();
        for (int i = 0; i < networks.length; i++) {
            mNetworkMap.put(networks[i], new NetworkWrapper(networkInfos[i], capabilitiesArray[i]));
        }
    }

    public static class NetworkWrapper {
        private NetworkInfo networkInfo;
        private NetworkCapabilities networkCapabilities;

        public NetworkWrapper(NetworkInfo networkInfo, NetworkCapabilities networkCapabilities) {
            this.networkInfo = networkInfo;
            this.networkCapabilities = networkCapabilities;
        }
    }

}

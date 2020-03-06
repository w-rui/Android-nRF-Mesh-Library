package no.nordicsemi.android.nrfmeshprovisioner.viewmodels;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import no.nordicsemi.android.meshprovisioner.AllocatedGroupRange;
import no.nordicsemi.android.meshprovisioner.AllocatedSceneRange;
import no.nordicsemi.android.meshprovisioner.AllocatedUnicastRange;
import no.nordicsemi.android.meshprovisioner.ApplicationKey;
import no.nordicsemi.android.meshprovisioner.Group;
import no.nordicsemi.android.meshprovisioner.MeshManagerApi;
import no.nordicsemi.android.meshprovisioner.MeshManagerCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshNetwork;
import no.nordicsemi.android.meshprovisioner.MeshNetworkCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshProvisioningStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.MeshStatusCallbacks;
import no.nordicsemi.android.meshprovisioner.NetworkKey;
import no.nordicsemi.android.meshprovisioner.Provisioner;
import no.nordicsemi.android.meshprovisioner.Scene;
import no.nordicsemi.android.meshprovisioner.UnprovisionedBeacon;
import no.nordicsemi.android.meshprovisioner.models.SigModelParser;
import no.nordicsemi.android.meshprovisioner.provisionerstates.ProvisioningState;
import no.nordicsemi.android.meshprovisioner.provisionerstates.UnprovisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyAdd;
import no.nordicsemi.android.meshprovisioner.transport.ConfigAppKeyStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigCompositionDataGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigCompositionDataStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigDefaultTtlGet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigDefaultTtlStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelAppStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelPublicationStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigModelSubscriptionStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetworkTransmitSet;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNetworkTransmitStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigNodeResetStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigProxyStatus;
import no.nordicsemi.android.meshprovisioner.transport.ConfigRelayStatus;
import no.nordicsemi.android.meshprovisioner.transport.ControlMessage;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.GenericLevelStatus;
import no.nordicsemi.android.meshprovisioner.transport.GenericOnOffStatus;
import no.nordicsemi.android.meshprovisioner.transport.MeshMessage;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;
import no.nordicsemi.android.meshprovisioner.transport.ProxyConfigFilterStatus;
import no.nordicsemi.android.meshprovisioner.transport.VendorModelMessageStatus;
import no.nordicsemi.android.meshprovisioner.utils.MeshAddress;
import no.nordicsemi.android.nrfmeshprovisioner.adapter.ExtendedBluetoothDevice;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager;
import no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManagerCallbacks;
import no.nordicsemi.android.nrfmeshprovisioner.di.MeshApplication;
import no.nordicsemi.android.nrfmeshprovisioner.utils.ProvisionerStates;
import no.nordicsemi.android.nrfmeshprovisioner.utils.Utils;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static no.nordicsemi.android.nrfmeshprovisioner.ble.BleMeshManager.MESH_PROXY_UUID;

@SuppressWarnings("unused")
public class NrfMeshRepository implements MeshProvisioningStatusCallbacks, MeshStatusCallbacks, MeshManagerCallbacks, BleMeshManagerCallbacks {

    private static final String TAG = NrfMeshRepository.class.getSimpleName();
    private static final int ATTENTION_TIMER = 5;
    public static final String EXPORT_PATH = Environment.getExternalStorageDirectory() + File.separator +
            "Nordic Semiconductor" + File.separator + "nRF Mesh" + File.separator;
    private static final String EXPORTED_PATH = "sdcard" + File.separator + "Nordic Semiconductor" + File.separator + "nRF Mesh" + File.separator;

    // Connection States Connecting, Connected, Disconnecting, Disconnected etc.
    private final MutableLiveData<Boolean> mIsConnectedToProxy = new MutableLiveData<>();

    // Live data flag containing connected state.
    private MutableLiveData<Boolean> mIsConnected;

    // LiveData to notify when device is ready
    private final MutableLiveData<Void> mOnDeviceReady = new MutableLiveData<>();

    // Updates the connection state while connecting to a peripheral
    private final MutableLiveData<String> mConnectionState = new MutableLiveData<>();

    // Flag to determine if a reconnection is in the progress when provisioning has completed
    private final SingleLiveEvent<Boolean> mIsReconnecting = new SingleLiveEvent<>();
    private final MutableLiveData<UnprovisionedMeshNode> mUnprovisionedMeshNodeLiveData = new MutableLiveData<>();
    private final MutableLiveData<ProvisionedMeshNode> mProvisionedMeshNodeLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<Integer> mConnectedProxyAddress = new SingleLiveEvent<>();

    private boolean mIsProvisioningComplete = false; // Flag to determine if provisioning was completed

    // Holds the selected MeshNode to configure
    private MutableLiveData<ProvisionedMeshNode> mExtendedMeshNode = new MutableLiveData<>();

    // Holds the selected Element to configure
    private MutableLiveData<Element> mSelectedElement = new MutableLiveData<>();

    // Holds the selected mesh model to configure
    private MutableLiveData<MeshModel> mSelectedModel = new MutableLiveData<>();
    // Holds the selected app key to configure
    private MutableLiveData<NetworkKey> mSelectedNetKey = new MutableLiveData<>();
    // Holds the selected app key to configure
    private MutableLiveData<ApplicationKey> mSelectedAppKey = new MutableLiveData<>();
    // Holds the selected provisioner when adding/editing
    private MutableLiveData<Provisioner> mSelectedProvisioner = new MutableLiveData<>();

    private final MutableLiveData<Group> mSelectedGroupLiveData = new MutableLiveData<>();

    // Composition data status
    final SingleLiveEvent<ConfigCompositionDataStatus> mCompositionDataStatus = new SingleLiveEvent<>();

    // App key add status
    final SingleLiveEvent<ConfigAppKeyStatus> mAppKeyStatus = new SingleLiveEvent<>();

    //Contains the MeshNetwork
    private MeshNetworkLiveData mMeshNetworkLiveData = new MeshNetworkLiveData();
    private SingleLiveEvent<String> mNetworkImportState = new SingleLiveEvent<>();
    private SingleLiveEvent<MeshMessage> mMeshMessageLiveData = new SingleLiveEvent<>();

    // Contains the provisioned nodes
    private final MutableLiveData<List<ProvisionedMeshNode>> mProvisionedNodes = new MutableLiveData<>();

    private final MutableLiveData<List<Group>> mGroups = new MutableLiveData<>();

    private final MutableLiveData<TransactionStatus> mTransactionStatus = new SingleLiveEvent<>();

    private MeshManagerApi mMeshManagerApi;
    private BleMeshManager mBleMeshManager;
    private Handler mHandler;
    private UnprovisionedMeshNode mUnprovisionedMeshNode;
    private ProvisionedMeshNode mProvisionedMeshNode;
    private boolean mIsReconnectingFlag;
    private boolean mIsScanning;
    private boolean mSetupProvisionedNode;
    private ProvisioningStatusLiveData mProvisioningStateLiveData;
    private MeshNetwork mMeshNetwork;
    private boolean mIsCompositionDataReceived;
    private boolean mIsDefaultTtlReceived;
    private boolean mIsAppKeyAddCompleted;
    private boolean mIsNetworkRetransmitSetCompleted;
    private Uri uri;

    private final Runnable mReconnectRunnable = this::startScan;

    private final Runnable mScannerTimeout = () -> {
        stopScan();
        mIsReconnecting.postValue(false);
    };

    public NrfMeshRepository(final MeshManagerApi meshManagerApi,
                             final BleMeshManager bleMeshManager) {
        //Initialize the mesh api
        mMeshManagerApi = meshManagerApi;
        mMeshManagerApi.setMeshManagerCallbacks(this);
        mMeshManagerApi.setProvisioningStatusCallbacks(this);
        mMeshManagerApi.setMeshStatusCallbacks(this);
        mMeshManagerApi.setMeshNetworkCallbacks(this.networkCallbacks);
        mMeshManagerApi.loadExternalNetwork(getNetwork());

        //Initialize the ble manager
        mBleMeshManager = bleMeshManager;
        mBleMeshManager.setGattCallbacks(this);
        mHandler = new Handler();
    }

    void clearInstance() {
        mBleMeshManager = null;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    LiveData<Void> isDeviceReady() {
        return mOnDeviceReady;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    LiveData<String> getConnectionState() {
        return mConnectionState;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    LiveData<Boolean> isConnected() {
        return mIsConnected;
    }

    /**
     * Returns {@link SingleLiveEvent} containing the device ready state.
     */
    LiveData<Boolean> isConnectedToProxy() {
        return mIsConnectedToProxy;
    }

    LiveData<Boolean> isReconnecting() {
        return mIsReconnecting;
    }

    boolean isProvisioningComplete() {
        return mIsProvisioningComplete;
    }

    boolean isCompositionDataStatusReceived() {
        return mIsCompositionDataReceived;
    }

    boolean isDefaultTtlReceived() {
        return mIsDefaultTtlReceived;
    }

    boolean isAppKeyAddCompleted() {
        return mIsAppKeyAddCompleted;
    }

    boolean isNetworkRetransmitSetCompleted() {
        return mIsNetworkRetransmitSetCompleted;
    }

    final MeshNetworkLiveData getMeshNetworkLiveData() {
        return mMeshNetworkLiveData;
    }

    LiveData<List<ProvisionedMeshNode>> getNodes() {
        return mProvisionedNodes;
    }

    LiveData<List<Group>> getGroups() {
        return mGroups;
    }

    LiveData<String> getNetworkLoadState() {
        return mNetworkImportState;
    }

    ProvisioningStatusLiveData getProvisioningState() {
        return mProvisioningStateLiveData;
    }

    LiveData<TransactionStatus> getTransactionStatus() {
        return mTransactionStatus;
    }

    /**
     * Clears the transaction status
     */
    void clearTransactionStatus() {
        if (mTransactionStatus.getValue() != null) {
            mTransactionStatus.postValue(null);
        }
    }

    /**
     * Returns the mesh manager api
     *
     * @return {@link MeshManagerApi}
     */
    MeshManagerApi getMeshManagerApi() {
        return mMeshManagerApi;
    }

    /**
     * Returns the ble mesh manager
     *
     * @return {@link BleMeshManager}
     */
    BleMeshManager getBleMeshManager() {
        return mBleMeshManager;
    }

    /**
     * Returns the {@link MeshMessageLiveData} live data object containing the mesh message
     */
    LiveData<MeshMessage> getMeshMessageLiveData() {
        return mMeshMessageLiveData;
    }

    LiveData<Group> getSelectedGroup() {
        return mSelectedGroupLiveData;
    }

    /**
     * Reset mesh network
     */
    void resetMeshNetwork() {
        disconnect();
        SharedPreferences sp = MeshApplication.application.getSharedPreferences("localNetwork", Context.MODE_PRIVATE);
        sp.edit().clear().apply();
        mMeshManagerApi.loadExternalNetwork(getNetwork());
    }

    /**
     * Connect to peripheral
     *
     * @param context          Context
     * @param device           {@link ExtendedBluetoothDevice} device
     * @param connectToNetwork True if connecting to an unprovisioned node or proxy node
     */
    void connect(final Context context, final ExtendedBluetoothDevice device, final boolean connectToNetwork) {
        mMeshNetworkLiveData.setNodeName(device.getName());
        mIsProvisioningComplete = false;
        mIsCompositionDataReceived = false;
        mIsDefaultTtlReceived = false;
        mIsAppKeyAddCompleted = false;
        mIsNetworkRetransmitSetCompleted = false;
        //clearExtendedMeshNode();
        final LogSession logSession = Logger.newSession(context, null, device.getAddress(), device.getName());
        mBleMeshManager.setLogger(logSession);
        final BluetoothDevice bluetoothDevice = device.getDevice();
        initIsConnectedLiveData(connectToNetwork);
        mConnectionState.postValue("Connecting....");
        //Added a 1 second delay for connection, mostly to wait for a disconnection to complete before connecting.
        mHandler.postDelayed(() -> mBleMeshManager.connect(bluetoothDevice), 1000);
    }

    /**
     * Connect to peripheral
     *
     * @param device bluetooth device
     */
    private void connectToProxy(final ExtendedBluetoothDevice device) {
        initIsConnectedLiveData(true);
        mConnectionState.postValue("Connecting....");
        mBleMeshManager.connect(device.getDevice());
    }

    private void initIsConnectedLiveData(final boolean connectToNetwork) {
        if (connectToNetwork) {
            mIsConnected = new SingleLiveEvent<>();
        } else {
            mIsConnected = new MutableLiveData<>();
        }
    }

    /**
     * Disconnects from peripheral
     */
    void disconnect() {
        clearProvisioningLiveData();
        mIsProvisioningComplete = false;
        mBleMeshManager.disconnect();
    }

    void clearProvisioningLiveData() {
        stopScan();
        mHandler.removeCallbacks(mReconnectRunnable);
        mSetupProvisionedNode = false;
        mIsReconnectingFlag = false;
        mUnprovisionedMeshNodeLiveData.setValue(null);
        mProvisionedMeshNodeLiveData.setValue(null);
    }

    private void removeCallbacks() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public void identifyNode(final ExtendedBluetoothDevice device) {
        final UnprovisionedBeacon beacon = (UnprovisionedBeacon) device.getBeacon();
        if (beacon != null) {
            mMeshManagerApi.identifyNode(beacon.getUuid(), ATTENTION_TIMER);
        } else {
            final byte[] serviceData = Utils.getServiceData(device.getScanResult(), BleMeshManager.MESH_PROVISIONING_UUID);
            if (serviceData != null) {
                final UUID uuid = mMeshManagerApi.getDeviceUuid(serviceData);
                mMeshManagerApi.identifyNode(uuid, ATTENTION_TIMER);
            }
        }
    }

    private void clearExtendedMeshNode() {
        if (mExtendedMeshNode != null) {
            mExtendedMeshNode.postValue(null);
        }
    }

    LiveData<UnprovisionedMeshNode> getUnprovisionedMeshNode() {
        return mUnprovisionedMeshNodeLiveData;
    }

    LiveData<Integer> getConnectedProxyAddress() {
        return mConnectedProxyAddress;
    }

    /**
     * Returns the selected mesh node
     */
    LiveData<ProvisionedMeshNode> getSelectedMeshNode() {
        return mExtendedMeshNode;
    }

    /**
     * Sets the mesh node to be configured
     *
     * @param node provisioned mesh node
     */
    void setSelectedMeshNode(final ProvisionedMeshNode node) {
        mExtendedMeshNode.postValue(node);
    }

    /**
     * Returns the selected element
     */
    LiveData<Element> getSelectedElement() {
        return mSelectedElement;
    }

    /**
     * Set the selected {@link Element} to be configured
     *
     * @param element element
     */
    void setSelectedElement(final Element element) {
        mSelectedElement.postValue(element);
    }

    /**
     * Set the selected model to be configured
     *
     * @param appKey mesh model
     */
    void setSelectedAppKey(@NonNull final ApplicationKey appKey) {
        mSelectedAppKey.postValue(appKey);
    }

    /**
     * Returns the selected mesh model
     */
    LiveData<ApplicationKey> getSelectedAppKey() {
        return mSelectedAppKey;
    }

    /**
     * Selects provisioner for editing or adding
     *
     * @param provisioner {@link Provisioner}
     */
    void setSelectedProvisioner(@NonNull final Provisioner provisioner) {
        mSelectedProvisioner.postValue(provisioner);
    }

    /**
     * Returns the selected {@link Provisioner}
     */
    LiveData<Provisioner> getSelectedProvisioner() {
        return mSelectedProvisioner;
    }

    /**
     * Returns the selected mesh model
     */
    LiveData<MeshModel> getSelectedModel() {
        return mSelectedModel;
    }

    /**
     * Set the selected model to be configured
     *
     * @param model mesh model
     */
    void setSelectedModel(final MeshModel model) {
        mSelectedModel.postValue(model);
    }

    @Override
    public void onDataReceived(final BluetoothDevice bluetoothDevice, final int mtu, final byte[] pdu) {
        mMeshManagerApi.handleNotifications(mtu, pdu);
    }

    @Override
    public void onDataSent(final BluetoothDevice device, final int mtu, final byte[] pdu) {
        mMeshManagerApi.handleWriteCallbacks(mtu, pdu);
    }

    @Override
    public void onDeviceConnecting(final BluetoothDevice device) {
        mConnectionState.postValue("Connecting....");
    }

    @Override
    public void onDeviceConnected(final BluetoothDevice device) {
        mIsConnected.postValue(true);
        mConnectionState.postValue("Discovering services....");
        mIsConnectedToProxy.postValue(true);
    }

    @Override
    public void onDeviceDisconnecting(final BluetoothDevice device) {
        Log.v(TAG, "Disconnecting...");
        if (mIsReconnectingFlag) {
            mConnectionState.postValue("Reconnecting...");
        } else {
            mConnectionState.postValue("Disconnecting...");
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onDeviceDisconnected(final BluetoothDevice device) {
        Log.v(TAG, "Disconnected");
        mConnectionState.postValue("");
        if (mIsReconnectingFlag) {
            mIsReconnectingFlag = false;
            mIsReconnecting.postValue(false);
            mIsConnected.postValue(false);
            mIsConnectedToProxy.postValue(false);
        } else {
            mIsConnected.postValue(false);
            mIsConnectedToProxy.postValue(false);
            if (mConnectedProxyAddress.getValue() != null) {
                final MeshNetwork network = mMeshManagerApi.getMeshNetwork();
                network.setProxyFilter(null);

            }
            //clearExtendedMeshNode();
        }
        mSetupProvisionedNode = false;
        mConnectedProxyAddress.postValue(null);
    }

    @Override
    public void onLinklossOccur(final BluetoothDevice device) {
        Log.v(TAG, "Link loss occurred");
        mIsConnected.postValue(false);
    }

    @Override
    public void onServicesDiscovered(final BluetoothDevice device, final boolean optionalServicesFound) {
        mConnectionState.postValue("Initializing...");
    }

    @Override
    public void onDeviceReady(final BluetoothDevice device) {
        mOnDeviceReady.postValue(null);

        if (mBleMeshManager.isProvisioningComplete()) {
            if (mSetupProvisionedNode) {
                if (mMeshNetwork.getSelectedProvisioner().getProvisionerAddress() != null) {
                    mHandler.postDelayed(() -> {
                        //Adding a slight delay here so we don't send anything before we receive the mesh beacon message
                        final ProvisionedMeshNode node = mProvisionedMeshNodeLiveData.getValue();
                        if (node != null) {
                            final ConfigCompositionDataGet compositionDataGet = new ConfigCompositionDataGet();
                            mMeshManagerApi.createMeshPdu(node.getUnicastAddress(), compositionDataGet);
                        }
                    }, 2000);
                } else {
                    mSetupProvisionedNode = false;
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.PROVISIONER_UNASSIGNED);
                    clearExtendedMeshNode();
                }
            }
            mIsConnectedToProxy.postValue(true);
        }
    }

    @Override
    public boolean shouldEnableBatteryLevelNotifications(final BluetoothDevice device) {
        return false;
    }

    @Override
    public void onBatteryValueReceived(final BluetoothDevice device, final int value) {

    }

    @Override
    public void onBondingRequired(final BluetoothDevice device) {

    }

    @Override
    public void onBonded(final BluetoothDevice device) {

    }

    @Override
    public void onError(final BluetoothDevice device, final String message, final int errorCode) {
        Log.e(TAG, "Error: " + message + " Error Code: " + errorCode + " Device: " + device.getAddress());
        mConnectionState.postValue(message);
    }

    @Override
    public void onDeviceNotSupported(final BluetoothDevice device) {

    }

    @Override
    public void onNetworkLoaded(final MeshNetwork meshNetwork) {
        loadNetwork(meshNetwork);
        loadGroups();
    }

    @Override
    public void onNetworkUpdated(final MeshNetwork meshNetwork) {
        Log.d(TAG, "onNetworkUpdated.");
        saveNetwork(mMeshNetwork);

        loadNetwork(meshNetwork);
        loadGroups();
        updateSelectedGroup();
    }

    @Override
    public void onNetworkLoadFailed(final String error) {
        mNetworkImportState.postValue(error);
    }

    @Override
    public void onNetworkImported(final MeshNetwork meshNetwork) {
        //We can delete the old network after the import has been successful!
        //But let's make sure we don't delete the same network in case someone imports the same network ;)
        final MeshNetwork oldNet = mMeshNetwork;
        if (!oldNet.getMeshUUID().equals(meshNetwork.getMeshUUID())) {
            mMeshManagerApi.deleteMeshNetworkFromDb(oldNet);
        }
        loadNetwork(meshNetwork);
        loadGroups();
        mNetworkImportState.postValue(meshNetwork.getMeshName() + " has been successfully imported.\n" +
                "In order to start sending messages to this network, please change the provisioner address. " +
                "Using the same provisioner address will cause messages to be discarded due to the usage of incorrect sequence numbers " +
                "for this address. However if the network does not contain any nodes you do not need to change the address");
    }

    @Override
    public void onNetworkImportFailed(final String error) {
        mNetworkImportState.postValue(error);
    }

    @Override
    public void sendProvisioningPdu(final UnprovisionedMeshNode meshNode, final byte[] pdu) {
        mBleMeshManager.sendPdu(pdu, true);
    }

    @Override
    public void onMeshPduCreated(final byte[] pdu) {
        mBleMeshManager.sendPdu(pdu, false);
    }

    @Override
    public int getMtu() {
        return mBleMeshManager.getMtuSize();
    }

    @Override
    public int getProvMtu(UnprovisionedMeshNode unproved) {
        return mBleMeshManager.getMtuSize();
    }

    @Override
    public void onProvisioningStateChanged(final UnprovisionedMeshNode meshNode, final ProvisioningState.States state, final byte[] data) {
        Log.d(TAG, "onProvisioningStateChanged, state: " + state + ", unproved: " + meshNode);

        mUnprovisionedMeshNode = meshNode;
        mUnprovisionedMeshNodeLiveData.postValue(meshNode);
        switch (state) {
            case PROVISIONING_INVITE:
                mProvisioningStateLiveData = new ProvisioningStatusLiveData();
                break;
            case PROVISIONING_FAILED:
                mIsProvisioningComplete = false;
                break;
        }
        mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.fromStatusCode(state.getState()));

    }

    @Override
    public void onProvisioningFailed(final UnprovisionedMeshNode meshNode, final ProvisioningState.States state, final byte[] data) {
        Log.d(TAG, "onProvisioningFailed, state: " + state + ", unproved: " + meshNode);
        mUnprovisionedMeshNode = meshNode;
        mUnprovisionedMeshNodeLiveData.postValue(meshNode);
        if (state == ProvisioningState.States.PROVISIONING_FAILED) {
            mIsProvisioningComplete = false;
        }
        mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.fromStatusCode(state.getState()));

    }

    @Override
    public void onProvisioningCompleted(final ProvisionedMeshNode meshNode, final ProvisioningState.States state, final byte[] data) {
        Log.d(TAG, "onProvisioningCompleted, state: " + state + ", proved: " + meshNode);
        mProvisionedMeshNode = meshNode;
        mUnprovisionedMeshNodeLiveData.postValue(null);
        mProvisionedMeshNodeLiveData.postValue(meshNode);
        if (state == ProvisioningState.States.PROVISIONING_COMPLETE) {
            onProvisioningCompleted(meshNode);
        }
        mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.fromStatusCode(state.getState()));

    }

    private void onProvisioningCompleted(final ProvisionedMeshNode node) {
        mIsProvisioningComplete = true;
        mProvisionedMeshNode = node;
        mIsReconnecting.postValue(true);
        mBleMeshManager.disconnect();
        mBleMeshManager.refreshDeviceCache();
        loadNodes();
        mHandler.post(() -> mConnectionState.postValue("Scanning for provisioned node"));
        mHandler.postDelayed(mReconnectRunnable, 1000); //Added a slight delay to disconnect and refresh the cache
    }

    /**
     * Here we load all nodes except the current provisioner. This may contain other provisioner nodes if available
     */
    private void loadNodes() {
        final List<ProvisionedMeshNode> nodes = new ArrayList<>();
        for (final ProvisionedMeshNode node : mMeshNetwork.getNodes()) {
            if (!node.getUuid().equalsIgnoreCase(mMeshNetwork.getSelectedProvisioner().getProvisionerUuid())) {
                nodes.add(node);
            }
        }
        mProvisionedNodes.postValue(nodes);
    }

    @Override
    public void onTransactionFailed(final int dst, final boolean hasIncompleteTimerExpired) {
        mProvisionedMeshNode = mMeshNetwork.getNode(dst);
        mTransactionStatus.postValue(new TransactionStatus(dst, hasIncompleteTimerExpired));
    }

    @Override
    public void onUnknownPduReceived(final int src, final byte[] accessPayload) {
        final ProvisionedMeshNode node = mMeshNetwork.getNode(src);
        if (node != null) {
            mProvisionedMeshNode = node;
            updateNode(node);
        }
    }

    @Override
    public void onBlockAcknowledgementProcessed(final int dst, @NonNull final ControlMessage message) {
        final ProvisionedMeshNode node = mMeshNetwork.getNode(dst);
        if (node != null) {
            mProvisionedMeshNode = node;
            if (mSetupProvisionedNode) {
                mProvisionedMeshNodeLiveData.postValue(mProvisionedMeshNode);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.SENDING_BLOCK_ACKNOWLEDGEMENT);
            }
        }
    }

    @Override
    public void onBlockAcknowledgementReceived(final int src, @NonNull final ControlMessage message) {
        final ProvisionedMeshNode node = mMeshNetwork.getNode(src);
        if (node != null) {
            mProvisionedMeshNode = node;
            if (mSetupProvisionedNode) {
                mProvisionedMeshNodeLiveData.postValue(node);
                mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.BLOCK_ACKNOWLEDGEMENT_RECEIVED);
            }
        }
    }

    @Override
    public void onMeshMessageProcessed(final int dst, @NonNull final MeshMessage meshMessage) {
        final ProvisionedMeshNode node = mMeshNetwork.getNode(dst);
        if (node != null) {
            mProvisionedMeshNode = node;
            if (meshMessage instanceof ConfigCompositionDataGet) {
                if (mSetupProvisionedNode) {
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.COMPOSITION_DATA_GET_SENT);
                }
            } else if (meshMessage instanceof ConfigDefaultTtlGet) {
                if (mSetupProvisionedNode) {
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.SENDING_DEFAULT_TTL_GET);
                }
            } else if (meshMessage instanceof ConfigAppKeyAdd) {
                if (mSetupProvisionedNode) {
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.SENDING_APP_KEY_ADD);
                }
            } else if (meshMessage instanceof ConfigNetworkTransmitSet) {
                if (mSetupProvisionedNode) {
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.SENDING_NETWORK_TRANSMIT_SET);
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onMeshMessageReceived(final int src, @NonNull final MeshMessage meshMessage) {
        final ProvisionedMeshNode node = mMeshNetwork.getNode(src);
        if (node != null)
            if (meshMessage instanceof ProxyConfigFilterStatus) {
                mProvisionedMeshNode = node;
                setSelectedMeshNode(node);
                final ProxyConfigFilterStatus status = (ProxyConfigFilterStatus) meshMessage;
                final int unicastAddress = status.getSrc();
                Log.v(TAG, "Proxy configuration source: " + MeshAddress.formatAddress(status.getSrc(), false));
                mConnectedProxyAddress.postValue(unicastAddress);
                mMeshMessageLiveData.postValue(status);
            } else if (meshMessage instanceof ConfigCompositionDataStatus) {
                final ConfigCompositionDataStatus status = (ConfigCompositionDataStatus) meshMessage;
                if (mSetupProvisionedNode) {
                    mIsCompositionDataReceived = true;
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mConnectedProxyAddress.postValue(node.getUnicastAddress());
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.COMPOSITION_DATA_STATUS_RECEIVED);
                    mHandler.postDelayed(() -> {
                        final ConfigDefaultTtlGet configDefaultTtlGet = new ConfigDefaultTtlGet();
                        mMeshManagerApi.createMeshPdu(node.getUnicastAddress(), configDefaultTtlGet);
                    }, 500);
                } else {
                    updateNode(node);
                }
            } else if (meshMessage instanceof ConfigDefaultTtlStatus) {
                final ConfigDefaultTtlStatus status = (ConfigDefaultTtlStatus) meshMessage;
                if (mSetupProvisionedNode) {
                    mIsDefaultTtlReceived = true;
                    mProvisionedMeshNodeLiveData.postValue(node);
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.DEFAULT_TTL_STATUS_RECEIVED);
                    mHandler.postDelayed(() -> {
                        final ApplicationKey appKey = mMeshNetworkLiveData.getSelectedAppKey();
                        final int index = node.getAddedNetKeys().get(0).getIndex();
                        final NetworkKey networkKey = mMeshNetwork.getNetKeys().get(index);
                        final ConfigAppKeyAdd configAppKeyAdd = new ConfigAppKeyAdd(networkKey, appKey);

                        Log.d(TAG, "addAppKey, addr: 0x" + Integer.toHexString(node.getUnicastAddress()) + ", netKeyIndex: " + index + ", appKeyIndex: " + appKey.getKeyIndex());
                        mMeshManagerApi.createMeshPdu(node.getUnicastAddress(), configAppKeyAdd);
                    }, 1500);
                } else {
                    updateNode(node);
                    mMeshMessageLiveData.postValue(status);
                }
            } else if (meshMessage instanceof ConfigAppKeyStatus) {
                final ConfigAppKeyStatus status = (ConfigAppKeyStatus) meshMessage;
                if (mSetupProvisionedNode) {
                    if (status.isSuccessful()) {
                        mIsAppKeyAddCompleted = true;
                        mProvisionedMeshNodeLiveData.postValue(node);
                        mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.APP_KEY_STATUS_RECEIVED);
                        mHandler.postDelayed(() -> {
                            final ConfigNetworkTransmitSet networkTransmitSet = new ConfigNetworkTransmitSet(2, 1);
                            mMeshManagerApi.createMeshPdu(node.getUnicastAddress(), networkTransmitSet);
                        }, 1500);
                    }
                } else {
                    updateNode(node);
                    mMeshMessageLiveData.postValue(status);
                }
            } else if (meshMessage instanceof ConfigNetworkTransmitStatus) {
                if (mSetupProvisionedNode) {
                    mSetupProvisionedNode = false;
                    mIsNetworkRetransmitSetCompleted = true;
                    mProvisioningStateLiveData.onMeshNodeStateUpdated(ProvisionerStates.NETWORK_TRANSMIT_STATUS_RECEIVED);
                } else {
                    updateNode(node);
                    final ConfigNetworkTransmitStatus status = (ConfigNetworkTransmitStatus) meshMessage;
                    mMeshMessageLiveData.postValue(status);
                }
            } else if (meshMessage instanceof ConfigModelAppStatus) {
                if (updateNode(node)) {
                    final ConfigModelAppStatus status = (ConfigModelAppStatus) meshMessage;
                    final Element element = node.getElements().get(status.getElementAddress());
                    if (node.getElements().containsKey(status.getElementAddress())) {
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                        mSelectedModel.postValue(model);
                    }
                }

            } else if (meshMessage instanceof ConfigModelPublicationStatus) {

                if (updateNode(node)) {
                    final ConfigModelPublicationStatus status = (ConfigModelPublicationStatus) meshMessage;
                    if (node.getElements().containsKey(status.getElementAddress())) {
                        final Element element = node.getElements().get(status.getElementAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                        mSelectedModel.postValue(model);
                    }
                }

            } else if (meshMessage instanceof ConfigModelSubscriptionStatus) {

                if (updateNode(node)) {
                    final ConfigModelSubscriptionStatus status = (ConfigModelSubscriptionStatus) meshMessage;
                    if (node.getElements().containsKey(status.getElementAddress())) {
                        final Element element = node.getElements().get(status.getElementAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                        mSelectedModel.postValue(model);
                    }
                }

            } else if (meshMessage instanceof ConfigNodeResetStatus) {

                final ConfigNodeResetStatus status = (ConfigNodeResetStatus) meshMessage;
                mExtendedMeshNode.postValue(null);
                loadNodes();
                mMeshMessageLiveData.postValue(status);

            } else if (meshMessage instanceof ConfigRelayStatus) {
                if (updateNode(node)) {
                    final ConfigRelayStatus status = (ConfigRelayStatus) meshMessage;
                    mMeshMessageLiveData.postValue(status);
                }

            } else if (meshMessage instanceof ConfigProxyStatus) {
                if (updateNode(node)) {
                    final ConfigProxyStatus status = (ConfigProxyStatus) meshMessage;
                    mMeshMessageLiveData.postValue(status);
                }

            } else if (meshMessage instanceof GenericOnOffStatus) {
                if (updateNode(node)) {
                    final GenericOnOffStatus status = (GenericOnOffStatus) meshMessage;
                    if (node.getElements().containsKey(status.getSrcAddress())) {
                        final Element element = node.getElements().get(status.getSrcAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get((int) SigModelParser.GENERIC_ON_OFF_SERVER);
                        mSelectedModel.postValue(model);
                    }
                }
            } else if (meshMessage instanceof GenericLevelStatus) {

                if (updateNode(node)) {
                    final GenericLevelStatus status = (GenericLevelStatus) meshMessage;
                    if (node.getElements().containsKey(status.getSrcAddress())) {
                        final Element element = node.getElements().get(status.getSrcAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get((int) SigModelParser.GENERIC_LEVEL_SERVER);
                        mSelectedModel.postValue(model);
                    }
                }

            } else if (meshMessage instanceof VendorModelMessageStatus) {

                if (updateNode(node)) {
                    final VendorModelMessageStatus status = (VendorModelMessageStatus) meshMessage;
                    if (node.getElements().containsKey(status.getSrcAddress())) {
                        final Element element = node.getElements().get(status.getSrcAddress());
                        mSelectedElement.postValue(element);
                        final MeshModel model = element.getMeshModels().get(status.getModelIdentifier());
                        mSelectedModel.postValue(model);
                    }
                }
            }

        if (mMeshMessageLiveData.hasActiveObservers()) {
            mMeshMessageLiveData.postValue(meshMessage);
        }

        //Refresh mesh network live data
        mMeshNetworkLiveData.refresh(mMeshManagerApi.getMeshNetwork());
    }

    @Override
    public void onMessageDecryptionFailed(final String meshLayer, final String errorMessage) {
        Log.e(TAG, "Decryption failed in " + meshLayer + " : " + errorMessage);
    }

    /**
     * Loads the network that was loaded from the db or imported from the mesh cdb
     *
     * @param meshNetwork mesh network that was loaded
     */
    private void loadNetwork(final MeshNetwork meshNetwork) {
        mMeshNetwork = meshNetwork;
        if (mMeshNetwork != null) {

            if (!mMeshNetwork.isProvisionerSelected()) {
                final Provisioner provisioner = meshNetwork.getProvisioners().get(0);
                provisioner.setLastSelected(true);
                mMeshNetwork.selectProvisioner(provisioner);
            }
            //Load live data with mesh network
            mMeshNetworkLiveData.loadNetworkInformation(meshNetwork);
            //Load live data with provisioned nodes
            loadNodes();

            final ProvisionedMeshNode node = getSelectedMeshNode().getValue();
            if (node != null) {
                mExtendedMeshNode.postValue(mMeshNetwork.getNode(node.getUuid()));
            }
        }
    }



    private MeshNetworkCallbacks networkCallbacks = new MeshNetworkCallbacks() {
        @Override
        public void onMeshNetworkUpdated() {
            Log.d(TAG, "onMeshNetworkUpdated.");
            // saveNetwork(mMeshNetwork);
        }

        @Override
        public void onNetworkKeyAdded(@NonNull NetworkKey networkKey) {
            Log.d(TAG, "onNetworkKeyAdded: " + networkKey);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onNetworkKeyUpdated(@NonNull NetworkKey networkKey) {
            Log.d(TAG, "onNetworkKeyUpdated: " + networkKey);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onNetworkKeyDeleted(@NonNull NetworkKey networkKey) {
            Log.d(TAG, "onNetworkKeyDeleted: " + networkKey);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onApplicationKeyAdded(@NonNull ApplicationKey applicationKey) {
            Log.d(TAG, "onApplicationKeyAdded: " + applicationKey);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onApplicationKeyUpdated(@NonNull ApplicationKey applicationKey) {
            Log.d(TAG, "onApplicationKeyUpdated: " + applicationKey);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onApplicationKeyDeleted(@NonNull ApplicationKey applicationKey) {
            Log.d(TAG, "onApplicationKeyDeleted: " + applicationKey);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onProvisionerAdded(@NonNull Provisioner provisioner) {
            Log.d(TAG, "onProvisionerAdded: " + provisioner);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onProvisionerUpdated(@NonNull Provisioner provisioner) {
            Log.d(TAG, "onProvisionerUpdated: " + provisioner);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onProvisionersUpdated(@NonNull List<Provisioner> provisioner) {
            Log.d(TAG, "onProvisionersUpdated: " + provisioner);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onProvisionerDeleted(@NonNull Provisioner provisioner) {
            Log.d(TAG, "onProvisionerDeleted: " + provisioner);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onNodeDeleted(@NonNull ProvisionedMeshNode meshNode) {
            Log.d(TAG, "onNodeDeleted: " + meshNode);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onNodeAdded(@NonNull ProvisionedMeshNode meshNode) {
            Log.d(TAG, "onNodeAdded: " + meshNode);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onNodeUpdated(@NonNull ProvisionedMeshNode meshNode) {
            Log.d(TAG, "onNodeUpdated: " + meshNode);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onNodesUpdated() {
            Log.d(TAG, "onNodesUpdated.");
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onGroupAdded(@NonNull Group group) {
            Log.d(TAG, "onGroupAdded: " + group);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onGroupUpdated(@NonNull Group group) {
            Log.d(TAG, "onGroupUpdated: " + group);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onGroupDeleted(@NonNull Group group) {
            Log.d(TAG, "onGroupDeleted: " + group);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onSceneAdded(@NonNull Scene scene) {
            Log.d(TAG, "onSceneAdded: " + scene);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onSceneUpdated(@NonNull Scene scene) {
            Log.d(TAG, "onSceneUpdated: " + scene);
            saveNetwork(mMeshNetwork);
        }

        @Override
        public void onSceneDeleted(@NonNull Scene scene) {
            Log.d(TAG, "onSceneDeleted: " + scene);
            saveNetwork(mMeshNetwork);
        }
    };

    private MeshNetwork getNetwork() {
        SharedPreferences sp = MeshApplication.application.getSharedPreferences("localNetwork", Context.MODE_PRIVATE);

        String networkId = sp.getString("networkId", UUID.randomUUID().toString());
        MeshNetwork network = new MeshNetwork(networkId);


        for (NetworkKey key : stringToList(sp.getString("netKeys", ""), NetworkKey.CREATOR, new NetworkKey[] { toNordicNetKey(networkId) })) {
            network.addNetKey(key);
        }

        for (ApplicationKey key : stringToList(sp.getString("appKeys", ""), ApplicationKey.CREATOR, new ApplicationKey[] {
            toNordicAppKey(networkId, 0), toNordicAppKey(networkId, 1), toNordicAppKey(networkId, 2),
        })) {
            network.addAppKey(key);
        }

        Provisioner firstProv = null;
        for (Provisioner prov : stringToList(sp.getString("provisioners", ""), Provisioner.CREATOR, new Provisioner[] { toNordicProv(networkId, UUID.randomUUID().toString(), PROV_ADDR_START) })) {
            Log.d(TAG, "Loaded provisioner[0x" + Integer.toHexString(prov.getProvisionerAddress()) + "] seq: " + prov.getSequenceNumber() + "/0x" + Integer.toHexString(prov.getSequenceNumber()));

            prov.incrementSequenceNumber(0x20);
            network.addProvisioner(prov);
            if (firstProv == null) firstProv = prov;
        }

        if (firstProv != null)
            network.selectProvisioner(firstProv);

        network.setGlobalTtl(sp.getInt("ttl", 10));

        network.setIvIndex(sp.getInt("ivIndex", 0));
        network.setIvUpdateState(sp.getInt("ivState", 0));
        Log.d(TAG, "Loaded network ivIndex: " + network.getIvIndex() + "/0x" + Integer.toBinaryString(network.getIvIndex()));

        for (ProvisionedMeshNode node : stringToList(sp.getString("devices", ""), ProvisionedMeshNode.CREATOR, null)) {
            network.addNode(node);
        }

        for (Group node : stringToList(sp.getString("groups", ""), Group.CREATOR, new Group[] {
            toNordicGroup(networkId, GROUP_ADDR_START, "Group0"),
            toNordicGroup(networkId, GROUP_ADDR_START + 1, "Group1"),
            toNordicGroup(networkId, GROUP_ADDR_START + 2, "Group2"),
        })) {
            network.addGroup(node);
        }

        Log.i(TAG, "local network loaded.");
        return network;
    }

    private long lastSaveUtc = 0, lastTryUtc = 0;
    private Handler handler = new Handler(Looper.getMainLooper());
    private void saveNetwork(MeshNetwork network) {
        long utc = System.currentTimeMillis();
        lastTryUtc = utc;

        handler.postDelayed(() -> {
            if (lastTryUtc != utc && System.currentTimeMillis() - lastSaveUtc < 3000)
                return;

            lastSaveUtc = System.currentTimeMillis();

            SharedPreferences sp = MeshApplication.application.getSharedPreferences("localNetwork", Context.MODE_PRIVATE);
            SharedPreferences.Editor ed = sp.edit();
            ed.putString("networkId", network.getId());

            ed.putString("netKeys", arrayToString(network.getNetKeys()));
            ed.putString("appKeys", arrayToString(network.getAppKeys()));
            ed.putString("provisioners", arrayToString(network.getProvisioners()));
            ed.putString("devices", arrayToString(network.getNodes()));
            ed.putString("groups", arrayToString(network.getGroups()));
            // ed.putString("scenes", arrayToString(network.getScenes()));

            ed.putInt("ttl", network.getGlobalTtl());

            for (Provisioner prov : network.getProvisioners()) {
                Log.d(TAG, "Saved provisioner[0x" + Integer.toHexString(prov.getProvisionerAddress()) + "] seq: " + prov.getSequenceNumber() + "/0x" + Integer.toHexString(prov.getSequenceNumber()));
            }

            Log.d(TAG, "Saved network ivIndex: " + network.getIvIndex() + "/0x" + Integer.toBinaryString(network.getIvIndex()));
            ed.putInt("ivIndex", network.getIvIndex());
            ed.putInt("ivState", network.getIvUpdateState());

            ed.apply();

            Log.i(TAG, "local network saved.");
        }, 1000);
    }

    private String arrayToString(List<? extends Parcelable> items) {
        StringBuilder sb = new StringBuilder();

        boolean first = true;
        for (Parcelable item : items) {
            if (!first) {
                sb.append(",");
            }

            first = false;

            sb.append(Base64.encodeToString(marshall(item), Base64.NO_WRAP));
        }

        return sb.toString();
    }

    private <T extends Parcelable> List<T> stringToList(String str, @NonNull Parcelable.Creator<T> creator, T[] placement) {
        String[] items = str.length() > 0 ? str.split(",") : new String[0];
        List<T> list = new ArrayList<>(items.length);

        if (items.length == 0 && str.length() > 0)
            items = new String[] { str };

        for (String item : items) {
            if (item.length() == 0)
                continue;

            list.add(unmarshall(Base64.decode(item, Base64.NO_WRAP), creator));
        }

        Log.d(TAG, "Restored[" + items.length + "] " + creator.getClass());

        if (list.size() == 0 && placement != null) {
            list.addAll(Arrays.asList(placement));
        }

        return list;
    }

    private Random rand = new Random();
    private static final int
        GROUP_ADDR_START = 0xc000,
        GROUP_ADDR_END = 0xcc9a,
        DEV_ADDR_START = 0x0001,
        DEV_ADDR_END = 0x7000,
        PROV_ADDR_START = 0x7000,
        PROV_ADDR_END = 0x7FFF,
        SCENE_ADDR_START = 0x0001,
        SCENE_ADDR_END = 0x3333,

    NET_KEY_LEN = 16,
        APP_KEY_LEN = 16
            ;
    private byte[] randomBytes(int len) {
        byte[] bytes = new byte[len];
        rand.nextBytes(bytes);
        return bytes;
    }

    public Provisioner toNordicProv(String networkId, String devId, int addr) {
        Provisioner provisioner = new Provisioner(devId,
            new ArrayList<AllocatedUnicastRange>() {{ add(new AllocatedUnicastRange(DEV_ADDR_START, DEV_ADDR_END)); }},
            new ArrayList<AllocatedGroupRange>() {{ add(new AllocatedGroupRange(GROUP_ADDR_START, GROUP_ADDR_END)); }},
            new ArrayList<AllocatedSceneRange>() {{ add(new AllocatedSceneRange(SCENE_ADDR_START, SCENE_ADDR_END)); }},
            networkId);

        provisioner.assignProvisionerAddress(addr);
        provisioner.setSequenceNumber(0);
        // provisioner.setGlobalTtl(prov.tt);
        return provisioner;
    }

    public Group toNordicGroup(String networkId, int addr, String name) {
        Group g = new Group(addr, networkId);

        g.setName(name);

        return g;
    }

    public ApplicationKey toNordicAppKey(String networkId, int keyIndex) {
        int netKeyIdx = 0;
        ApplicationKey ak = new ApplicationKey(keyIndex, randomBytes(APP_KEY_LEN));
        ak.setBoundNetKeyIndex(0);
        ak.setMeshUuid(networkId);
        ak.setOldKey(randomBytes(APP_KEY_LEN));
        ak.setName("AppKey" + keyIndex);
        ak.setId((netKeyIdx << 16) + keyIndex);
        return ak;
    }

    public NetworkKey toNordicNetKey(String networkId) {
        int keyIndex = 0;
        NetworkKey ak = new NetworkKey(keyIndex, randomBytes(NET_KEY_LEN));
        ak.setMeshUuid(networkId);
        ak.setOldKey(randomBytes(NET_KEY_LEN));
        ak.setName("Primary");
        ak.setId(keyIndex << 16);
        // ak.setTimestamp(netKey.timestamp);
        ak.setPhase(NetworkKey.PHASE_0);
        // ak.setMinSecurity(netKey.);
        return ak;
    }

    public static byte[] marshall(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle(); // not sure if needed or a good idea
        return bytes;
    }

    public static <T extends Parcelable> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);
        return creator.createFromParcel(parcel);
    }

    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // this is extremely important!
        return parcel;
    }


    /**
     * We should only update the selected node, since sending messages to group address will notify with nodes that is not on the UI
     */
    private boolean updateNode(@NonNull final ProvisionedMeshNode node) {
        if (mProvisionedMeshNode.getUnicastAddress() == node.getUnicastAddress()) {
            mProvisionedMeshNode = node;
            mExtendedMeshNode.postValue(node);
            return true;
        }
        return false;
    }

    /**
     * Starts reconnecting to the device
     */
    private void startScan() {
        if (mIsScanning)
            return;

        mIsScanning = true;
        // Scanning settings
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                // Refresh the devices list every second
                .setReportDelay(0)
                // Hardware filtering has some issues on selected devices
                .setUseHardwareFilteringIfSupported(false)
                // Samsung S6 and S6 Edge report equal value of RSSI for all devices. In this app we ignore the RSSI.
                /*.setUseHardwareBatchingIfSupported(false)*/
                .build();

        // Let's use the filter to scan only for Mesh devices
        final List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid((MESH_PROXY_UUID))).build());

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.startScan(filters, settings, scanCallback);
        Log.v(TAG, "Scan started");
        mHandler.postDelayed(mScannerTimeout, 20000);
    }

    /**
     * stop scanning for bluetooth devices.
     */
    private void stopScan() {
        mHandler.removeCallbacks(mScannerTimeout);
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.stopScan(scanCallback);
        mIsScanning = false;
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, final ScanResult result) {
            //In order to connectToProxy to the correct device, the hash advertised in the advertisement data should be matched.
            //This is to make sure we connectToProxy to the same device as device addresses could change after provisioning.
            final ScanRecord scanRecord = result.getScanRecord();
            if (scanRecord != null) {
                final byte[] serviceData = Utils.getServiceData(result, MESH_PROXY_UUID);
                if (serviceData != null) {
                    if (mMeshManagerApi.isAdvertisedWithNodeIdentity(serviceData)) {
                        final ProvisionedMeshNode node = mProvisionedMeshNode;

                        try {
                            if (mMeshManagerApi.nodeIdentityMatches(node, serviceData)) {
                                stopScan();
                                mConnectionState.postValue("Provisioned node found");
                                onProvisionedDeviceFound(node, new ExtendedBluetoothDevice(result));
                            }
                        } catch (Throwable e) {
                            Log.e(TAG, "Should fix: ", e);
                            throw e;
                        }
                    }
                }
            }
        }

        @Override
        public void onBatchScanResults(@NonNull final List<ScanResult> results) {
            // Batch scan is disabled (report delay = 0)
        }

        @Override
        public void onScanFailed(final int errorCode) {

        }
    };

    private void onProvisionedDeviceFound(final ProvisionedMeshNode node, final ExtendedBluetoothDevice device) {
        mSetupProvisionedNode = true;
        mProvisionedMeshNode = node;
        mIsReconnectingFlag = true;
        //Added an extra delay to ensure reconnection
        mHandler.postDelayed(() -> connectToProxy(device), 2000);
    }

    /**
     * Generates the groups based on the addresses each models have subscribed to
     */
    private void loadGroups() {
        mGroups.postValue(mMeshNetwork.getGroups());
    }

    private void updateSelectedGroup() {
        final Group selectedGroup = mSelectedGroupLiveData.getValue();
        if (selectedGroup != null) {
            mSelectedGroupLiveData.postValue(mMeshNetwork.getGroup(selectedGroup.getAddress()));
        }
    }

    /**
     * Sets the group that was selected from the GroupAdapter.
     */
    void setSelectedGroup(final int address) {
        final Group group = mMeshNetwork.getGroup(address);
        if (group != null) {
            mSelectedGroupLiveData.postValue(group);
        }
    }
}

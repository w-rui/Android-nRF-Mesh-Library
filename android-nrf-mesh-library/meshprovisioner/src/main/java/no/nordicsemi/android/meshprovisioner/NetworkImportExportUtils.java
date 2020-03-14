package no.nordicsemi.android.meshprovisioner;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.meshprovisioner.transport.Element;
import no.nordicsemi.android.meshprovisioner.transport.InternalElementListDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.MeshModel;
import no.nordicsemi.android.meshprovisioner.transport.MeshModelListDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.NodeDeserializer;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

/**
 * Utility class to handle network imports and exports
 */
class NetworkImportExportUtils {

    private static final String TAG = NetworkImportExportUtils.class.getSimpleName();

    /**
     * Creates an AsyncTask to import the a mesh network
     *
     * @param uri       file path
     * @param callbacks internal callbacks to notify network import
     */
    static void importMeshNetwork(@NonNull final Uri uri,
                                  @NonNull final LoadNetworkCallbacks callbacks) throws JsonSyntaxException {
        throw new RuntimeException("Should implement with J2OBJC.");
    }

    /**
     * Creates an AsyncTask to import the a mesh network
     *
     * @param context     context
     * @param networkJson network json
     * @param callbacks   internal callbacks to notify network import
     */
    static void importMeshNetworkFromJson(@NonNull final String networkJson,
                                          @NonNull final LoadNetworkCallbacks callbacks) {
        throw new RuntimeException("Should implement with J2OBJC.");
    }

    /**
     * Exports the mesh network to a Json file
     *
     * @param network mesh network to be exported
     */
    @Nullable
    static String export(@NonNull final MeshNetwork network) {
        try {

            Type netKeyList = new TypeToken<List<NetworkKey>>() {
            }.getType();
            Type appKeyList = new TypeToken<List<ApplicationKey>>() {
            }.getType();
            Type allocatedUnicastRange = new TypeToken<List<AllocatedUnicastRange>>() {
            }.getType();
            Type allocatedGroupRange = new TypeToken<List<AllocatedGroupRange>>() {
            }.getType();
            Type allocatedSceneRange = new TypeToken<List<AllocatedSceneRange>>() {
            }.getType();
            Type nodeList = new TypeToken<List<ProvisionedMeshNode>>() {
            }.getType();
            Type meshModelList = new TypeToken<List<MeshModel>>() {
            }.getType();
            Type elementList = new TypeToken<List<Element>>() {
            }.getType();

            final GsonBuilder gsonBuilder = new GsonBuilder();

            gsonBuilder.registerTypeAdapter(netKeyList, new NetKeyDeserializer());
            gsonBuilder.registerTypeAdapter(appKeyList, new AppKeyDeserializer());
            gsonBuilder.registerTypeAdapter(allocatedUnicastRange, new AllocatedUnicastRangeDeserializer());
            gsonBuilder.registerTypeAdapter(allocatedGroupRange, new AllocatedGroupRangeDeserializer());
            gsonBuilder.registerTypeAdapter(allocatedSceneRange, new AllocatedSceneRangeDeserializer());
            gsonBuilder.registerTypeAdapter(nodeList, new NodeDeserializer());
            gsonBuilder.registerTypeAdapter(elementList, new InternalElementListDeserializer());
            gsonBuilder.registerTypeAdapter(meshModelList, new MeshModelListDeserializer());
            gsonBuilder.registerTypeAdapter(MeshNetwork.class, new MeshNetworkDeserializer());

            final Gson gson = gsonBuilder
                    .serializeNulls()
                    .setPrettyPrinting()
                    .create();
            return gson.toJson(network);
        } catch (final com.google.gson.JsonSyntaxException ex) {
            Log.e(TAG, "Error: " + ex.getMessage());
            return null;
        } catch (final Exception e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return null;
        }
    }

}

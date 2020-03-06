package no.nordicsemi.android.meshprovisioner;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.room.Database;
import no.nordicsemi.android.meshprovisioner.data.ApplicationKeyDao;
import no.nordicsemi.android.meshprovisioner.data.ApplicationKeysDao;
import no.nordicsemi.android.meshprovisioner.data.GroupDao;
import no.nordicsemi.android.meshprovisioner.data.GroupsDao;
import no.nordicsemi.android.meshprovisioner.data.MeshNetworkDao;
import no.nordicsemi.android.meshprovisioner.data.NetworkKeyDao;
import no.nordicsemi.android.meshprovisioner.data.NetworkKeysDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodeDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionedMeshNodesDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionerDao;
import no.nordicsemi.android.meshprovisioner.data.ProvisionersDao;
import no.nordicsemi.android.meshprovisioner.data.SceneDao;
import no.nordicsemi.android.meshprovisioner.data.ScenesDao;
import no.nordicsemi.android.meshprovisioner.transport.ProvisionedMeshNode;

@RestrictTo(RestrictTo.Scope.LIBRARY)
@SuppressWarnings("unused")
@Database(entities = {
        MeshNetwork.class,
        NetworkKey.class,
        ApplicationKey.class,
        Provisioner.class,
        ProvisionedMeshNode.class,
        Group.class,
        Scene.class},
        version = 7)
abstract class MeshNetworkDb {

    private static String TAG = MeshNetworkDb.class.getSimpleName();

    abstract MeshNetworkDao meshNetworkDao();

    abstract NetworkKeyDao networkKeyDao();

    abstract NetworkKeysDao networkKeysDao();

    abstract ApplicationKeyDao applicationKeyDao();

    abstract ApplicationKeysDao applicationKeysDao();

    abstract ProvisionerDao provisionerDao();

    abstract ProvisionersDao provisionersDao();

    abstract ProvisionedMeshNodeDao provisionedMeshNodeDao();

    abstract ProvisionedMeshNodesDao provisionedMeshNodesDao();

    abstract GroupsDao groupsDao();

    abstract GroupDao groupDao();

    abstract ScenesDao scenesDao();

    abstract SceneDao sceneDao();

    private static volatile MeshNetworkDb INSTANCE;

    /**
     * Returns the mesh database
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY)
    static MeshNetworkDb getDatabase(final Context context) {
        return INSTANCE;
    }

    void insertNetwork(@NonNull final MeshNetworkDao dao,
                       @NonNull final NetworkKeysDao netKeysDao,
                       @NonNull final ApplicationKeysDao appKeysDao,
                       @NonNull final ProvisionersDao provisionersDao,
                       @NonNull final ProvisionedMeshNodesDao nodesDao,
                       @NonNull final GroupsDao groupsDao,
                       @NonNull final ScenesDao scenesDao,
                       @NonNull final MeshNetwork meshNetwork) {
        new InsertNetworkAsyncTask(dao,
                netKeysDao,
                appKeysDao,
                provisionersDao,
                nodesDao,
                groupsDao,
                scenesDao,
                meshNetwork).execute();
    }

    void loadNetwork(@NonNull final MeshNetworkDao dao,
                     @NonNull final NetworkKeysDao netKeysDao,
                     @NonNull final ApplicationKeysDao appKeysDao,
                     @NonNull final ProvisionersDao provisionersDao,
                     @NonNull final ProvisionedMeshNodesDao nodesDao,
                     @NonNull final GroupsDao groupsDao,
                     @NonNull final ScenesDao scenesDao,
                     @NonNull final LoadNetworkCallbacks listener) {
        new LoadNetworkAsyncTask(dao,
                netKeysDao,
                appKeysDao,
                provisionersDao,
                nodesDao,
                groupsDao,
                scenesDao,
                listener).execute();
    }

    void updateNetwork(@NonNull final MeshNetworkDao dao, @NonNull final MeshNetwork meshNetwork) {
        new UpdateNetworkAsyncTask(dao).execute(meshNetwork);
    }

    void updateNetwork1(@NonNull final MeshNetwork meshNetwork,
                        @NonNull final MeshNetworkDao dao,
                        @NonNull final NetworkKeysDao netKeyDao,
                        @NonNull final ApplicationKeysDao appKeyDao,
                        @NonNull final ProvisionersDao provisionerDao,
                        @NonNull final ProvisionedMeshNodesDao nodeDao,
                        @NonNull final GroupsDao groupsDao,
                        @NonNull final ScenesDao sceneDao) {
        new UpdateNetworkAsyncTask1(dao,
                netKeyDao,
                appKeyDao,
                provisionerDao,
                nodeDao,
                groupsDao,
                sceneDao).execute(meshNetwork);
    }

    void deleteNetwork(@NonNull final MeshNetworkDao dao, @NonNull final MeshNetwork meshNetwork) {
        new DeleteNetworkAsyncTask(dao).execute(meshNetwork);
    }

    void insertNetKey(@NonNull final NetworkKeyDao dao, @NonNull final NetworkKey networkKey) {
        new InsertNetKeyAsyncTask(dao).execute(networkKey);
    }

    void updateNetKey(@NonNull final NetworkKeyDao dao, @NonNull final NetworkKey networkKey) {
        new UpdateNetKeyAsyncTask(dao).execute(networkKey);
    }

    void deleteNetKey(@NonNull final NetworkKeyDao dao, @NonNull final NetworkKey networkKey) {
        new DeleteNetKeyAsyncTask(dao).execute(networkKey);
    }

    void insertAppKey(@NonNull final ApplicationKeyDao dao, @NonNull final ApplicationKey applicationKey) {
        new InsertAppKeyAsyncTask(dao).execute(applicationKey);
    }

    void updateAppKey(@NonNull final ApplicationKeyDao dao, @NonNull final ApplicationKey applicationKey) {
        new UpdateAppKeyAsyncTask(dao).execute(applicationKey);
    }

    void deleteAppKey(@NonNull final ApplicationKeyDao dao, @NonNull final ApplicationKey applicationKey) {
        new DeleteAppKeyAsyncTask(dao).execute(applicationKey);
    }

    void insertProvisioner(@NonNull final ProvisionerDao dao, @NonNull final Provisioner provisioner) {
        new InsertProvisionerAsyncTask(dao).execute(provisioner);
    }

    void updateProvisioner(@NonNull final ProvisionerDao dao, @NonNull final Provisioner provisioner) {
        new UpdateProvisionerAsyncTask(dao).execute(provisioner);
    }

    void updateProvisioners(@NonNull final ProvisionerDao dao, @NonNull final List<Provisioner> provisioners) {
        new UpdateProvisionersAsyncTask(dao, provisioners).execute();
    }

    void deleteProvisioner(@NonNull final ProvisionerDao dao, @NonNull final Provisioner provisioner) {
        new DeleteProvisionerAsyncTask(dao).execute(provisioner);
    }

    void insertNode(@NonNull final ProvisionedMeshNodeDao dao, @NonNull final ProvisionedMeshNode node) {
        new InsertNodeAsyncTask(dao).execute(node);
    }

    void updateNode(@NonNull final ProvisionedMeshNodeDao dao, @NonNull final ProvisionedMeshNode node) {
        new UpdateNodeAsyncTask(dao).execute(node);
    }

    void updateNodes(@NonNull final ProvisionedMeshNodesDao dao, @NonNull final List<ProvisionedMeshNode> nodes) {
        new UpdateNodesAsyncTask(dao, nodes).execute();
    }

    void deleteNode(@NonNull final ProvisionedMeshNodeDao dao, @NonNull final ProvisionedMeshNode node) {
        new DeleteNodeAsyncTask(dao).execute(node);
    }

    void insertGroup(@NonNull final GroupDao dao, @NonNull final Group group) {
        new InsertGroupAsyncTask(dao).execute(group);
    }

    void updateGroup(@NonNull final GroupDao dao, @NonNull final Group group) {
        new UpdateGroupAsyncTask(dao).execute(group);
    }

    void updateGroups(@NonNull final GroupsDao dao, @NonNull final List<Group> groups) {
        new UpdateGroupsAsyncTask(dao, groups).execute();
    }

    void deleteGroup(@NonNull final GroupDao dao, @NonNull final Group group) {
        new DeleteGroupAsyncTask(dao).execute(group);
    }

    void insertScene(@NonNull final SceneDao dao, @NonNull final Scene scene) {
        new InsertSceneAsyncTask(dao).execute(scene);
    }

    void updateScene(@NonNull final SceneDao dao, @NonNull final Scene scene) {
        new UpdateSceneKeyAsyncTask(dao).execute(scene);
    }

    void deleteScene(@NonNull final SceneDao dao, @NonNull final Scene scene) {
        new DeleteSceneKeyAsyncTask(dao).execute(scene);
    }

    private static class InsertNetworkAsyncTask extends AsyncTask<Void, Void, Void> {

        private final MeshNetwork meshNetwork;
        private final MeshNetworkDao meshNetworkDao;
        private final NetworkKeysDao netKeysDao;
        private final ApplicationKeysDao appKeysDao;
        private final ProvisionersDao provisionersDao;
        private final ProvisionedMeshNodesDao nodesDao;
        private final GroupsDao groupsDao;
        private final ScenesDao scenesDao;

        InsertNetworkAsyncTask(@NonNull final MeshNetworkDao meshNetworkDao,
                               @NonNull final NetworkKeysDao netKeysDao,
                               @NonNull final ApplicationKeysDao appKeysDao,
                               @NonNull final ProvisionersDao provisionersDao,
                               @NonNull final ProvisionedMeshNodesDao nodesDao,
                               @NonNull final GroupsDao groupsDao,
                               @NonNull final ScenesDao scenesDao,
                               @NonNull final MeshNetwork meshNetwork) {
            this.meshNetworkDao = meshNetworkDao;
            this.netKeysDao = netKeysDao;
            this.appKeysDao = appKeysDao;
            this.provisionersDao = provisionersDao;
            this.nodesDao = nodesDao;
            this.groupsDao = groupsDao;
            this.scenesDao = scenesDao;
            this.meshNetwork = meshNetwork;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            meshNetworkDao.insert(meshNetwork);
            netKeysDao.insert(meshNetwork.netKeys);
            appKeysDao.insert(meshNetwork.appKeys);
            provisionersDao.insert(meshNetwork.provisioners);
            if (!meshNetwork.nodes.isEmpty()) {
                nodesDao.insert(meshNetwork.nodes);
            }

            if (meshNetwork.groups != null) {
                groupsDao.insert(meshNetwork.groups);
            }

            if (meshNetwork.scenes != null) {
                scenesDao.insert(meshNetwork.scenes);
            }
            return null;
        }
    }

    private static class LoadNetworkAsyncTask extends AsyncTask<Void, Void, MeshNetwork> {

        private final LoadNetworkCallbacks listener;
        private final MeshNetworkDao meshNetworkDao;
        private final NetworkKeysDao netKeysDao;
        private final ApplicationKeysDao appKeysDao;
        private final ProvisionersDao provisionersDao;
        private final ProvisionedMeshNodesDao nodesDao;
        private final GroupsDao groupsDao;
        private final ScenesDao sceneDao;

        LoadNetworkAsyncTask(@NonNull final MeshNetworkDao meshNetworkDao,
                             @NonNull final NetworkKeysDao netKeysDao,
                             @NonNull final ApplicationKeysDao appKeysDao,
                             @NonNull final ProvisionersDao provisionersDao,
                             @NonNull final ProvisionedMeshNodesDao nodesDao,
                             @NonNull final GroupsDao groupsDao,
                             @NonNull final ScenesDao sceneDao,
                             @NonNull final LoadNetworkCallbacks listener) {
            this.meshNetworkDao = meshNetworkDao;
            this.netKeysDao = netKeysDao;
            this.appKeysDao = appKeysDao;
            this.provisionersDao = provisionersDao;
            this.nodesDao = nodesDao;
            this.groupsDao = groupsDao;
            this.sceneDao = sceneDao;
            this.listener = listener;
        }

        @Override
        protected MeshNetwork doInBackground(final Void... params) {
            final MeshNetwork meshNetwork = meshNetworkDao.getMeshNetwork(true);
            if (meshNetwork != null) {
                meshNetwork.netKeys = netKeysDao.loadNetworkKeys(meshNetwork.getMeshUUID());
                meshNetwork.appKeys = appKeysDao.loadApplicationKeys(meshNetwork.getMeshUUID());
                meshNetwork.nodes = nodesDao.getNodes(meshNetwork.getMeshUUID());
                meshNetwork.provisioners = provisionersDao.getProvisioners(meshNetwork.getMeshUUID());
                meshNetwork.groups = groupsDao.loadGroups(meshNetwork.getMeshUUID());
            }
            return meshNetwork;
        }

        @Override
        protected void onPostExecute(final MeshNetwork meshNetwork) {
            super.onPostExecute(meshNetwork);
            listener.onNetworkLoadedFromDb(meshNetwork);
        }
    }

    private static class UpdateNetworkAsyncTask extends AsyncTask<MeshNetwork, Void, Void> {

        private MeshNetworkDao mAsyncTaskDao;

        UpdateNetworkAsyncTask(@NonNull final MeshNetworkDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final MeshNetwork... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class UpdateNetworkAsyncTask1 extends AsyncTask<MeshNetwork, Void, Void> {

        private final MeshNetworkDao meshNetworkDao;
        private final NetworkKeysDao netKeyDao;
        private final ApplicationKeysDao appKeyDao;
        private final ProvisionersDao provisionersDao;
        private final ProvisionedMeshNodesDao nodesDao;
        private final GroupsDao groupsDao;
        private final ScenesDao sceneDao;

        UpdateNetworkAsyncTask1(@NonNull final MeshNetworkDao meshNetworkDao,
                                @NonNull final NetworkKeysDao netKeysDao,
                                @NonNull final ApplicationKeysDao appKeysDao,
                                @NonNull final ProvisionersDao provisionersDao,
                                @NonNull final ProvisionedMeshNodesDao nodesDao,
                                @NonNull final GroupsDao groupsDao,
                                @NonNull final ScenesDao scenesDao) {
            this.meshNetworkDao = meshNetworkDao;
            this.netKeyDao = netKeysDao;
            this.appKeyDao = appKeysDao;
            this.provisionersDao = provisionersDao;
            this.nodesDao = nodesDao;
            this.groupsDao = groupsDao;
            this.sceneDao = scenesDao;
        }

        @Override
        protected Void doInBackground(@NonNull final MeshNetwork... params) {
            final MeshNetwork network = params[0];
            meshNetworkDao.update(network);
            netKeyDao.update(network.getNetKeys());
            appKeyDao.update(network.getAppKeys());
            provisionersDao.update(network.getProvisioners());
            nodesDao.update(network.getNodes());
            groupsDao.update(network.getGroups());
            sceneDao.update(network.getScenes());
            return null;
        }

        @Override
        protected void onPostExecute(final Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    private static class DeleteNetworkAsyncTask extends AsyncTask<MeshNetwork, Void, Void> {

        private MeshNetworkDao mAsyncTaskDao;

        DeleteNetworkAsyncTask(@NonNull final MeshNetworkDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final MeshNetwork... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class InsertNetKeyAsyncTask extends AsyncTask<NetworkKey, Void, Void> {

        private NetworkKeyDao mAsyncTaskDao;

        InsertNetKeyAsyncTask(@NonNull final NetworkKeyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final NetworkKey... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateNetKeyAsyncTask extends AsyncTask<NetworkKey, Void, Void> {

        private NetworkKeyDao mAsyncTaskDao;

        UpdateNetKeyAsyncTask(@NonNull final NetworkKeyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final NetworkKey... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteNetKeyAsyncTask extends AsyncTask<NetworkKey, Void, Void> {

        private NetworkKeyDao mAsyncTaskDao;

        DeleteNetKeyAsyncTask(@NonNull final NetworkKeyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final NetworkKey... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class InsertAppKeyAsyncTask extends AsyncTask<ApplicationKey, Void, Void> {

        private ApplicationKeyDao mAsyncTaskDao;

        InsertAppKeyAsyncTask(@NonNull final ApplicationKeyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ApplicationKey... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateAppKeyAsyncTask extends AsyncTask<ApplicationKey, Void, Void> {

        private ApplicationKeyDao mAsyncTaskDao;

        UpdateAppKeyAsyncTask(@NonNull final ApplicationKeyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ApplicationKey... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteAppKeyAsyncTask extends AsyncTask<ApplicationKey, Void, Void> {

        private ApplicationKeyDao mAsyncTaskDao;

        DeleteAppKeyAsyncTask(@NonNull final ApplicationKeyDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ApplicationKey... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class InsertProvisionerAsyncTask extends AsyncTask<Provisioner, Void, Void> {

        private final ProvisionerDao mAsyncTaskDao;

        InsertProvisionerAsyncTask(@NonNull final ProvisionerDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Provisioner... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateProvisionerAsyncTask extends AsyncTask<Provisioner, Void, Void> {

        private final ProvisionerDao mAsyncTaskDao;

        UpdateProvisionerAsyncTask(@NonNull final ProvisionerDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Provisioner... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class UpdateProvisionersAsyncTask extends AsyncTask<Void, Void, Void> {

        private final ProvisionerDao mAsyncTaskDao;
        private final List<Provisioner> provisioners;

        UpdateProvisionersAsyncTask(@NonNull final ProvisionerDao dao,
                                    @NonNull final List<Provisioner> provisioners) {
            mAsyncTaskDao = dao;
            this.provisioners = provisioners;
        }

        @Override
        protected Void doInBackground(final Void... voids) {
            mAsyncTaskDao.update(provisioners);
            return null;
        }
    }

    private static class DeleteProvisionerAsyncTask extends AsyncTask<Provisioner, Void, Void> {

        private final ProvisionerDao mAsyncTaskDao;

        DeleteProvisionerAsyncTask(@NonNull final ProvisionerDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Provisioner... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class InsertNodeAsyncTask extends AsyncTask<ProvisionedMeshNode, Void, Void> {

        private ProvisionedMeshNodeDao mAsyncTaskDao;

        InsertNodeAsyncTask(@NonNull final ProvisionedMeshNodeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ProvisionedMeshNode... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateNodeAsyncTask extends AsyncTask<ProvisionedMeshNode, Void, Void> {

        private ProvisionedMeshNodeDao mAsyncTaskDao;

        UpdateNodeAsyncTask(@NonNull final ProvisionedMeshNodeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ProvisionedMeshNode... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class UpdateNodesAsyncTask extends AsyncTask<Void, Void, Void> {

        private ProvisionedMeshNodesDao mAsyncTaskDao;
        private List<ProvisionedMeshNode> nodes;

        UpdateNodesAsyncTask(@NonNull final ProvisionedMeshNodesDao dao,
                             @NonNull final List<ProvisionedMeshNode> nodes) {
            mAsyncTaskDao = dao;
            this.nodes = nodes;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mAsyncTaskDao.update(nodes);
            return null;
        }
    }

    private static class DeleteNodeAsyncTask extends AsyncTask<ProvisionedMeshNode, Void, Void> {

        private ProvisionedMeshNodeDao mAsyncTaskDao;

        DeleteNodeAsyncTask(@NonNull final ProvisionedMeshNodeDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final ProvisionedMeshNode... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class InsertGroupAsyncTask extends AsyncTask<Group, Void, Void> {

        private final GroupDao mAsyncTaskDao;

        InsertGroupAsyncTask(@NonNull final GroupDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Group... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateGroupAsyncTask extends AsyncTask<Group, Void, Void> {

        private final GroupDao mAsyncTaskDao;

        UpdateGroupAsyncTask(@NonNull final GroupDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Group... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class UpdateGroupsAsyncTask extends AsyncTask<Void, Void, Void> {

        private final GroupsDao mAsyncTaskDao;
        private final List<Group> mGroups;

        UpdateGroupsAsyncTask(@NonNull final GroupsDao dao, @NonNull final List<Group> groups) {
            mAsyncTaskDao = dao;
            mGroups = groups;
        }

        @Override
        protected Void doInBackground(final Void... voids) {
            mAsyncTaskDao.update(mGroups);
            return null;
        }
    }

    private static class DeleteGroupAsyncTask extends AsyncTask<Group, Void, Void> {

        private GroupDao mAsyncTaskDao;

        DeleteGroupAsyncTask(@NonNull final GroupDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Group... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

    private static class InsertSceneAsyncTask extends AsyncTask<Scene, Void, Void> {

        private SceneDao mAsyncTaskDao;

        InsertSceneAsyncTask(@NonNull final SceneDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Scene... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class UpdateSceneKeyAsyncTask extends AsyncTask<Scene, Void, Void> {

        private final SceneDao mAsyncTaskDao;

        UpdateSceneKeyAsyncTask(@NonNull final SceneDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Scene... params) {
            mAsyncTaskDao.update(params[0]);
            return null;
        }
    }

    private static class DeleteSceneKeyAsyncTask extends AsyncTask<Scene, Void, Void> {

        private final SceneDao mAsyncTaskDao;

        DeleteSceneKeyAsyncTask(@NonNull final SceneDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Scene... params) {
            mAsyncTaskDao.delete(params[0]);
            return null;
        }
    }

}

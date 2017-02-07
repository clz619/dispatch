package win.sinno.dispatch.core;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import win.sinno.dispatch.constrant.DispatchConfig;
import win.sinno.dispatch.constrant.LoggerConfigs;
import win.sinno.dispatch.core.agent.LeaderLatchAgent;
import win.sinno.dispatch.core.agent.MachineAgent;
import win.sinno.dispatch.core.agent.ZkNodeAgent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 核心守护进程,dispath 启动入口
 * <p>
 * <p>
 * 1.维持zk交互
 * 2.leader,worker选举
 * 3.机器信息注册
 * <p>
 * 提供本机角色信息
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/4 上午11:19
 */
public class MainDaemon implements IMainDaemon {

    private static final Logger LOG = LoggerConfigs.DISPATCH_LOG;

    private String machineName;
    //
    private String zkHost;

    //命名空间
    private String namespace;

    //会话超时时间
    private int sessionTimeoutMs = DispatchConfig.ZK_SESSION_TIMEOUT_MS;

    //连接时间
    private int connTimeoutMs = DispatchConfig.ZK_CONN_TIMEOUT_MS;


    private CuratorFramework curatorClient;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private ZkPathManager zkPathManager;

    //////////////agent/////////////

    /**
     * zk 节点
     */
    private ZkNodeAgent zkNodeAgent;

    /**
     * leader 选举
     */
    private LeaderLatchAgent leaderLatchAgent;

    private MachineAgent machineAgent;

    ////////////machine info/////////////


    public MainDaemon(String zkHost, String namespace) {
        this.zkHost = zkHost;
        this.namespace = namespace;
        this.machineName = DispatchConfig.getMachineName();
    }


    public ZkPathManager getZkPathManager() {
        return zkPathManager;
    }

    public CuratorFramework getCuratorClient() {
        return curatorClient;
    }

    public ZkNodeAgent getZkNodeAgent() {
        return zkNodeAgent;
    }

    /**
     * 启动入口
     */
    public void start() {

        LOG.info("main daemon start begin.");
        try {
            initZkManager();

            startCuratorClient();

            //之后需要 curator支持

            initMachineAgent();

            initZkNodeAgent();

            startLeaderLatch();

            registerClient();
        } catch (Exception e) {
            //FIXME 注册机器失败
            LOG.error(e.getMessage(), e);
        }

        LOG.info("main daemon start end.");

    }

    /**
     * 关闭
     */
    public void stop() {

        LOG.info("main daemon stop begin.");

        stopCuratorClient();

        stopLeaderLatch();

        LOG.info("main daemon stop end.");

    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public void setConnTimeoutMs(int connTimeoutMs) {
        this.connTimeoutMs = connTimeoutMs;
    }

    //初始化zk 管理器
    protected void initZkManager() {
        //zk path manager
        this.zkPathManager = new ZkPathManager(this.namespace, machineName);
    }

    protected void startCuratorClient() {

        this.curatorClient = CuratorFrameworkFactory.builder().namespace(this.namespace).connectString(this.zkHost)
                .sessionTimeoutMs(this.sessionTimeoutMs).connectionTimeoutMs(this.connTimeoutMs)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();

        this.curatorClient.start();

        try {

            LOG.info("curator client connecting...");
            //阻塞 直到连接
            this.curatorClient.blockUntilConnected();

            LOG.info("curator client connected... enjoy it.");

        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }

    }

    /**
     * 初始化机器代理
     */
    protected void initMachineAgent() {
        LOG.info("init machine agent begin...");
        this.machineAgent = new MachineAgent();
        LOG.info("init machine agent end..,");
    }


    protected void initZkNodeAgent() throws Exception {
        this.zkNodeAgent = new ZkNodeAgent(this, curatorClient, zkPathManager, machineAgent);

        //节点监听
        this.zkNodeAgent.handler();
    }


    /**
     * 停止客户端
     */
    protected void stopCuratorClient() {
        LOG.info("curator client close begin...");

        if (this.curatorClient != null) {
            CloseableUtils.closeQuietly(this.curatorClient);
        }

        LOG.info("curator client close end...");
    }

    /**
     * 开始选举
     * 需要curator,zkPathManager
     */
    protected void startLeaderLatch() throws Exception {
        LOG.info("leader latch start begin...");

        this.leaderLatchAgent = new LeaderLatchAgent(this, curatorClient, zkPathManager.getLeaderLatchPath(), zkPathManager.getMachineName());

        this.leaderLatchAgent.handler();

        LOG.info("leader latch start begined...");
    }

    /**
     * 退出leader选举
     */
    protected void stopLeaderLatch() {
        LOG.info("leader latch stop begin...");

        if (this.leaderLatchAgent != null) {
            this.leaderLatchAgent.stop();
        }

        LOG.info("leader latch stop end...");
    }


    /**
     * 注册机器
     *
     * @throws Exception
     */
    protected void registerClient() throws Exception {
        LOG.info("register client begin...");

        this.zkNodeAgent.registerClient();

        LOG.info("register client end...");
    }


}

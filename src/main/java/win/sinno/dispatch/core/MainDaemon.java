package win.sinno.dispatch.core;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import win.sinno.dispatch.concurrent.DispatchThreadFactory;
import win.sinno.dispatch.constrant.DispatchConfig;
import win.sinno.dispatch.constrant.LoggerConfigs;
import win.sinno.dispatch.core.agent.*;

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

    //
    private CuratorFramework curatorFramework;

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

    private LeaderAgent leaderAgent;

    private WorkerAgent workerAgent;

    private ExecutorService executorService = Executors.newCachedThreadPool(new DispatchThreadFactory("MainDaemon"));


    public MainDaemon(String zkHost, String namespace) {
        this.zkHost = zkHost;
        this.namespace = namespace;
        this.machineName = DispatchConfig.getMachineName();
    }


    public ZkPathManager getZkPathManager() {
        return zkPathManager;
    }

    public CuratorFramework getCuratorFramework() {
        return curatorFramework;
    }

    public ZkNodeAgent getZkNodeAgent() {
        return zkNodeAgent;
    }

    public MachineAgent getMachineAgent() {
        return machineAgent;
    }

    /**
     * 启动入口
     * 1.启动机器代理
     * 2.初始化zk路径管理器
     * 3.启动curator客户端
     * 4.启动zk节点代理
     * 5.启动leader选举代理
     * 6.启各个动机器的服务代理
     */
    public void start() {

        LOG.info("main daemon start begin.");
        try {

            startMachineAgent();

            initZkPathManager();

            startCuratorClient();

            startZkNodeAgent();

            startLeaderLatchAgent();

            startMachineServiceAgent();
        } catch (Exception e) {
            //FIXME
            //TODO 中间有启动异常的，调用stop，并
            LOG.error(e.getMessage(), e);

            stop();
        }

        LOG.info("main daemon start end.");

    }

    /**
     * 关闭
     */
    public void stop() {

        LOG.info("main daemon stop begin.");

        stopLeaderLatch();

        stopCuratorClient();

        LOG.info("main daemon stop end.");

    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public void setConnTimeoutMs(int connTimeoutMs) {
        this.connTimeoutMs = connTimeoutMs;
    }

    //初始化zk 管理器
    protected void initZkPathManager() {
        //zk path manager
        this.zkPathManager = new ZkPathManager(this.namespace, machineName);
    }

    protected void startCuratorClient() {

        this.curatorFramework = CuratorFrameworkFactory.builder().namespace(this.namespace).connectString(this.zkHost)
                .sessionTimeoutMs(this.sessionTimeoutMs).connectionTimeoutMs(this.connTimeoutMs)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();

        this.curatorFramework.start();

        try {

            LOG.info("curator client connecting...");
            //阻塞 直到连接
            this.curatorFramework.blockUntilConnected();

            LOG.info("curator client connected... enjoy it.");

        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }

    }

    /**
     * 初始化机器代理
     */
    protected void startMachineAgent() {
        LOG.info("init machine agent begin...");
        this.machineAgent = new MachineAgent();
        LOG.info("init machine agent end..,");
    }


    protected void startZkNodeAgent() throws Exception {
        this.zkNodeAgent = new ZkNodeAgent(this, curatorFramework, zkPathManager, machineAgent);

        //节点监听
        this.zkNodeAgent.handler();

        ////注册机器
        LOG.info("register client begin...");

        this.zkNodeAgent.registerClient();

        LOG.info("register client end...");
    }


    /**
     * 停止客户端
     */
    protected void stopCuratorClient() {
        LOG.info("curator client close begin...");

        if (this.curatorFramework != null) {
            CloseableUtils.closeQuietly(this.curatorFramework);
        }

        LOG.info("curator client close end...");
    }

    /**
     * 开始选举
     * 需要curator,zkPathManager
     */
    protected void startLeaderLatchAgent() throws Exception {
        LOG.info("leader latch start begin...");

        this.leaderLatchAgent = new LeaderLatchAgent(this, curatorFramework, zkPathManager.getLeaderLatchPath(), zkPathManager.getMachineName(), zkNodeAgent);

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
     * 执行机器
     *
     * @throws Exception
     */
    protected void startMachineServiceAgent() throws Exception {
        //leader agent
        leaderAgent = new LeaderAgent(machineAgent);

        //worker agent
        workerAgent = new WorkerAgent(machineAgent);

        //exec
        executorService.execute(leaderAgent);

        executorService.execute(workerAgent);
    }


}

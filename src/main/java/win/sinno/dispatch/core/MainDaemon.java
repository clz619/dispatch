package win.sinno.dispatch.core;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import win.sinno.dispatch.constrant.LoggerConfigs;

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

    /**
     * 机器名
     */
    private String machineName;

    //
    private String zkHost;

    //命名空间
    private String namespace;

    //会话超时时间
    private int sessionTimeoutMs;

    //连接时间
    private int connTimeoutMs;


    private CuratorFramework curatorClient;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    public MainDaemon(String zkHost, String namespace) {
        this.zkHost = zkHost;
        this.namespace = namespace;
    }

    public MainDaemon(String machineName, String zkHost, String namespace) {
        //机器名
        this.machineName = machineName;
        this.zkHost = zkHost;
        this.namespace = namespace;
    }

    /**
     * 启动入口
     */
    public void start() {

        LOG.info("main daemon start begin.");

        startClient();

        LOG.info("main daemon start end.");

    }

    /**
     * 关闭
     */
    public void stop() {

        LOG.info("main daemon stop begin.");

        stopClient();

        LOG.info("main daemon stop end.");

    }

    public void setSessionTimeoutMs(int sessionTimeoutMs) {
        this.sessionTimeoutMs = sessionTimeoutMs;
    }

    public void setConnTimeoutMs(int connTimeoutMs) {
        this.connTimeoutMs = connTimeoutMs;
    }

    protected void startClient() {

        curatorClient = CuratorFrameworkFactory.builder().namespace(namespace).connectString(zkHost)
                .sessionTimeoutMs(sessionTimeoutMs).connectionTimeoutMs(connTimeoutMs)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)).build();

        curatorClient.start();

        try {

            LOG.info("curator client connecting...");
            //阻塞 直到连接
            curatorClient.blockUntilConnected();

            LOG.info("curator client connected... enjoy it.");

        } catch (InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }

    }

    /**
     * 停止客户端
     */
    protected void stopClient() {

        LOG.info("curator client close begin...");

        if (curatorClient != null) {
            CloseableUtils.closeQuietly(curatorClient);
        }

        LOG.info("curator client close end...");
    }

}

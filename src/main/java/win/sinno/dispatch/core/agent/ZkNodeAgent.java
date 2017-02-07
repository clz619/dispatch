package win.sinno.dispatch.core.agent;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import win.sinno.common.util.NetworkUtil;
import win.sinno.dispatch.constrant.LoggerConfigs;
import win.sinno.dispatch.core.MainDaemon;
import win.sinno.dispatch.core.ZkPathManager;

import java.net.InetAddress;
import java.util.Collection;

/**
 * zk node 处理
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/6 上午11:41
 */
public class ZkNodeAgent implements IAgent {

    private static final Logger LOG = LoggerConfigs.DISPATCH_LOG;

    /**
     * main daemon
     */
    private MainDaemon mainDaemon;

    /**
     * curator framework
     */
    private CuratorFramework curatorFramework;

    /**
     * zk path
     */
    private ZkPathManager zkPathManager;

    /////node monitor

    /**
     * leader node monitor
     */
//    private ZkNodeMonitor zkLeaderNodeMonitor;

    /**
     * client register node monitor
     */
//    private ZkNodeMonitor zkClientRegisterNodeMonitor;

    //机器注册节点监听
    private PathChildrenCache clientRegisterNodeCache;

    //leader节点监听
    private NodeCache leaderNodeCache;

    //机器代理
    private MachineAgent machineAgent;

    public ZkNodeAgent(MainDaemon mainDaemon, CuratorFramework curatorFramework, ZkPathManager zkPathManager, MachineAgent machineAgent) {
        this.mainDaemon = mainDaemon;
        this.curatorFramework = curatorFramework;
        this.zkPathManager = zkPathManager;

        //机器信息代理
        this.machineAgent = machineAgent;
    }

    /**
     * 处理
     * <p>
     * 添加节点监听器
     * leader节点 新增、更新、删除
     * client register 子节点的新增、更新、删除
     */
    @Override
    public void handler() throws Exception {
        //ingore
        listenerLeaderNode();

        listenerClientRegisterNode();
    }

    /**
     * 监听leader节点
     */
    public void listenerLeaderNode() throws Exception {

        leaderNodeCache = new NodeCache(curatorFramework, zkPathManager.getLeaderPath());

        leaderNodeCache.getListenable().addListener(new NodeCacheListener() {

            @Override
            public void nodeChanged() throws Exception {

                ChildData childData = leaderNodeCache.getCurrentData();

                LOG.info("leader node cache listener :{}", childData);

                if (childData != null) {

                    byte[] bytes = childData.getData();

                    if (bytes != null) {

                        String nodeVal = new String(bytes);

                        LOG.info("leader node val change :{}", nodeVal);
                    }
                }
            }
        });

        leaderNodeCache.start();

    }

    /**
     * 监听 机器注册节点
     */
    public void listenerClientRegisterNode() throws Exception {
        clientRegisterNodeCache = new PathChildrenCache(curatorFramework, zkPathManager.getClientRegisterPath(), false);
        clientRegisterNodeCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                //节点更新

                LOG.info("client register path children cache listener:-----type:{},data:{}", new Object[]{event.getType(), event.getData()});

                ChildData data = event.getData();

                switch (event.getType()) {
                    case CHILD_ADDED:
                        //TODO 注册了新的machine
                        LOG.info("CHILD_ADDED : {} data: {}", new Object[]{data.getPath(), data.getData() == null ? null : new String(data.getData())});
                        break;
                    case CHILD_UPDATED:
                        //TODO 更新machine信息
                        LOG.info("CHILD_UPDATED : {} data: {}", new Object[]{data.getPath(), data.getData() == null ? null : new String(data.getData())});
                        break;
                    case CHILD_REMOVED:
                        //TODO 移除machine信息
                        LOG.info("CHILD_REMOVED : {} data: {}", new Object[]{data.getPath(), data.getData() == null ? null : new String(data.getData())});
                        break;

                    default:
                        break;
                }
            }
        });
        clientRegisterNodeCache.start();
    }

    /**
     * 注册设为leader
     */
    public void registerLeaderNode() throws Exception {
        Stat stat = curatorFramework.checkExists().forPath(zkPathManager.getLeaderPath());

        if (stat == null) {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                    .forPath(zkPathManager.getLeaderPath(), zkPathManager.getMachineName().getBytes());
        } else {
            curatorFramework.setData().forPath(zkPathManager.getLeaderPath(), zkPathManager.getMachineName().getBytes());
        }

        LOG.info("zk node set leader path:{} , value:{}", new Object[]{zkPathManager.getLeaderPath(), zkPathManager.getMachineName()});
    }

    /**
     * 注册客户端，register节点注册，detail属性注册
     */
    public void registerClient() throws Exception {
        LOG.info("register client begin ...");

        registerClientNode();

        registerClientDetail();

        LOG.info("register client end ...");
    }

    /**
     * 注册机器在线时保持的临时节点
     */
    public void registerClientNode() throws Exception {
        Stat stat = curatorFramework.checkExists().forPath(zkPathManager.getCurrClientRegisterPath());

        if (stat == null) {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                    .forPath(zkPathManager.getCurrClientRegisterPath());

            LOG.info("register client node:{}", zkPathManager.getCurrClientRegisterPath());
        }
    }

    /**
     * id , name, status, online, ip
     */
    public void registerClientDetail() throws Exception {
        Stat stat = curatorFramework.checkExists().forPath(zkPathManager.getCurrClientDetailPath());

        if (stat == null) {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath(zkPathManager.getCurrClientDetailPath());
        }

        registerClientId();

        registerClientOnlineTs(System.currentTimeMillis());

        registerClientIp();

        LOG.info("register client detail:{}", zkPathManager.getCurrClientDetailPath());
    }

    /**
     * 记录机器id
     *
     * @throws Exception
     */
    private void registerClientId() throws Exception {

        Stat stat = curatorFramework.checkExists().forPath(zkPathManager.getCurrClientDeatilIdPath());

        if (stat == null) {
            //TODO 替换客户端的id获取逻辑
            // (客户端id，要求随机，与上一次的不一样，因为可能客户端闪断，造成之前数据重新分配，若分配与之前相同id，可能造成数据分配异常情况发生)
            String id = "" + System.currentTimeMillis();

            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath(zkPathManager.getCurrClientDeatilIdPath(), id.getBytes());

            LOG.info("register client id path:{} , value:{}",
                    new Object[]{zkPathManager.getCurrClientDeatilIdPath(), id});
        }

    }

    /**
     * 记录机器上线时间
     *
     * @param onlineTs
     * @throws Exception
     */
    private void registerClientOnlineTs(Long onlineTs) throws Exception {

        Stat stat = curatorFramework.checkExists().forPath(zkPathManager.getCurrClientDeatilOnlinePath());

        if (stat == null) {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath(zkPathManager.getCurrClientDeatilOnlinePath(), String.valueOf(onlineTs).getBytes());

            LOG.info("register client online ts path:{} , value:{}",
                    new Object[]{zkPathManager.getCurrClientDeatilOnlinePath(), onlineTs});
        }
    }

    /**
     * 注册机器ip
     *
     * @throws Exception
     */
    private void registerClientIp() throws Exception {

        Stat stat = curatorFramework.checkExists().forPath(zkPathManager.getCurrClientDeatilIpPath());
        Collection<InetAddress> ipv4AddressList = NetworkUtil.getIpv4AddressWithoutLoopback();

        StringBuilder ipSb = new StringBuilder();

        if (CollectionUtils.isNotEmpty(ipv4AddressList)) {
            for (InetAddress inetAddress : ipv4AddressList) {
                if (ipSb.length() > 0) {
                    ipSb.append(",");
                }
                ipSb.append(inetAddress.getHostAddress());
            }
        }

        if (stat == null) {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath(zkPathManager.getCurrClientDeatilIpPath(), ipSb.toString().getBytes());

            LOG.info("register client ip path:{} , value:{}",
                    new Object[]{zkPathManager.getCurrClientDeatilIpPath(), ipSb.toString()});
        }

    }


}

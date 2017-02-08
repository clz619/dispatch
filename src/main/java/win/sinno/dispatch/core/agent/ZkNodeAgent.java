package win.sinno.dispatch.core.agent;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import win.sinno.common.util.NetworkUtil;
import win.sinno.dispatch.constrant.LoggerConfigs;
import win.sinno.dispatch.core.MainDaemon;
import win.sinno.dispatch.core.ZkPathManager;
import win.sinno.dispatch.model.Machine;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Date;

/**
 * zk node 处理
 * machine
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
     * zk path.
     */
    private ZkPathManager zkPathManager;

    /**
     * 机器注册节点监听
     */
    private PathChildrenCache clientRegisterNodeCache;

    /**
     * leader节点监听
     */
    private NodeCache leaderNodeCache;

    /**
     * 机器代理
     */
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
                        //设置 leader机器名
                        machineAgent.setLeaderName(nodeVal);
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

                final ChildData data = event.getData();
                //注册路径为临时节点，需要进行当连接断开节点便会释放
                switch (event.getType()) {
                    case CHILD_ADDED:
                        //TODO 注册了新的machine
                        //所有machine的详细信息，都从zk进行获取
                        LOG.info("CHILD_ADDED : {} data: {}", new Object[]{data.getPath(), data.getData() == null ? null : new String(data.getData())});

                        //新的机器路径
                        new Thread() {
                            @Override
                            public void run() {

                                try {
                                    //休眠10s，等待机器属性注册完成
                                    Thread.sleep(10000l);
                                } catch (InterruptedException e) {
                                    LOG.error(e.getMessage(), e);
                                }

                                String newMachinePath = data.getPath();
                                String[] pathArray = newMachinePath.split("/");
                                //机器名
                                String machineName = pathArray[pathArray.length - 1];

                                try {
                                    Machine machine = getMachineDetailNode(machineName);

                                    if (Machine.MachineStatus.ONLINE.getValue().equals(machine.getStatus())) {
                                        machineAgent.online(machine);
                                    }

                                    if (Machine.MachineStatus.OFFLINE.getValue().equals(machine.getStatus())) {
                                        machineAgent.offline(machine);
                                    }


                                } catch (Exception e) {
                                    LOG.error(e.getMessage(), e);
                                }

                                //获取机器
                            }
                        }.start();

                        break;
                    case CHILD_REMOVED:
                        //TODO 移除machine信息
                        LOG.info("CHILD_REMOVED : {} data: {}", new Object[]{data.getPath(), data.getData() == null ? null : new String(data.getData())});


                        String newMachinePath = data.getPath();
                        String[] pathArray = newMachinePath.split("/");
                        //机器名
                        String machineName = pathArray[pathArray.length - 1];

                        try {

                            Machine machine = getMachineDetailNode(machineName);

                            machineAgent.offline(machine);

                        } catch (Exception e) {
                            LOG.error(e.getMessage(), e);
                        }


                        break;
                    case CHILD_UPDATED:
                        //TODO 更新machine信息
                        LOG.info("CHILD_UPDATED : {} data: {}", new Object[]{data.getPath(), data.getData() == null ? null : new String(data.getData())});
                        break;
                    default:
                        break;
                }
            }
        });
        clientRegisterNodeCache.start();
    }

    /**
     * 获取注册的节点详情
     *
     * @param machineName
     * @return
     */
    public Machine getMachineDetailNode(String machineName) throws Exception {

        Machine machine = null;

        if (StringUtils.isEmpty(machineName)) {
            return machine;
        }

        Stat stat = curatorFramework.checkExists().forPath(zkPathManager.getClientDetailPath(machineName));

        if (stat == null) {
            //节点不存在，则直接返回
            return machine;
        }

        //机器ip
        String id = getMachineIdNode(machineName);
        //ips 多个ip，中间以英文逗号隔开
        String ips = getMachineIpsNode(machineName);
        //上线时间
        String onlineTsStr = getMachineOnlinetsNode(machineName);

        //机器注册路径-包含机器名
        String clientRegisterPath = zkPathManager.getClientRegisterPath(machineName);

        //检测是否在线
        Stat registerStat = curatorFramework.checkExists().forPath(clientRegisterPath);

        Machine.MachineStatus machineStatus = registerStat == null ? Machine.MachineStatus.OFFLINE : Machine.MachineStatus.ONLINE;

        Long online = Long.valueOf(onlineTsStr);
        Date now = new Date(online);

        /////////////////////machine
        machine = new Machine();
        machine.setName(machineName);
        machine.setId(Long.valueOf(id));
        machine.setGmtCreate(now);
        machine.setGmtModified(now);

        machine.setOnlineTs(online);
        //机器状态
        machine.setStatus(machineStatus.getValue());

        return machine;
    }

    /**
     * 获取id node的值
     *
     * @param machineName
     * @return
     * @throws Exception
     */
    public String getMachineIdNode(String machineName) throws Exception {
        //id path
        String idPath = zkPathManager.getClientDeatilIdPath(machineName);

        //获取 路径的值
        return getValueFromPath(idPath);
    }

    /**
     * 获取machine ips node的值
     *
     * @param machineName
     * @return
     * @throws Exception
     */
    public String getMachineIpsNode(String machineName) throws Exception {
        String ipPath = zkPathManager.getClientDeatilIpPath(machineName);

        return getValueFromPath(ipPath);
    }

    /**
     * 获取machine online ts node的值
     *
     * @param machineName
     * @return
     * @throws Exception
     */
    public String getMachineOnlinetsNode(String machineName) throws Exception {
        String onlineTsPath = zkPathManager.getClientDeatilOnlinePath(machineName);

        return getValueFromPath(onlineTsPath);
    }

    /**
     * 获取zk路径path的值
     *
     * @param path
     * @return
     * @throws Exception
     */
    public String getValueFromPath(String path) throws Exception {
        Stat stat = curatorFramework.checkExists().forPath(path);

        if (stat == null) {
            return null;
        }

        byte[] bytes = curatorFramework.getData().forPath(path);
        if (bytes == null) {
            return null;
        }

        //ip byte.字符串
        return new String(bytes);

    }

    /**
     * 注册设为leader
     */
    public void registerLeaderNode() throws Exception {
        Stat stat = curatorFramework.checkExists().forPath(zkPathManager.getLeaderPath());

        //节点状态
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

        //机器注册路径
        Stat stat = curatorFramework.checkExists().forPath(zkPathManager.getCurrClientRegisterPath());

        if (stat == null) {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                    .forPath(zkPathManager.getCurrClientRegisterPath());

            LOG.info("register client node:{}", zkPathManager.getCurrClientRegisterPath());
        }
    }

    /**
     * id , name, status, online, ip
     * <p>
     */
    public void registerClientDetail() throws Exception {

        //机器详细信息路径
        Stat stat = curatorFramework.checkExists().forPath(zkPathManager.getCurrClientDetailPath());

        if (stat == null) {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath(zkPathManager.getCurrClientDetailPath());
        }

        registerClientId();

        registerClientName();

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
     * 记录机器名，实则路径中已包含，进行数据冗余
     *
     * @throws Exception
     */
    private void registerClientName() throws Exception {
        Stat stat = curatorFramework.checkExists().forPath(zkPathManager.getCurrClientDeatilNamePath());

        if (stat == null) {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath(zkPathManager.getCurrClientDeatilNamePath(), zkPathManager.getMachineName().getBytes());

            LOG.info("register client name path:{} , value:{}",
                    new Object[]{zkPathManager.getCurrClientDeatilNamePath(), zkPathManager.getMachineName()});
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

        Stat stat = curatorFramework.checkExists().forPath(zkPathManager.getCurrClientDeatilIpPath());

        if (stat == null) {
            curatorFramework.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath(zkPathManager.getCurrClientDeatilIpPath(), ipSb.toString().getBytes());

            LOG.info("register client ip path:{} , value:{}",
                    new Object[]{zkPathManager.getCurrClientDeatilIpPath(), ipSb.toString()});
        }

    }


    /**
     * 设置本机为leader
     *
     * @param isLeader
     */
    public void setLocalLeader(Boolean isLeader) {
        machineAgent.setIsLeader(isLeader);
    }

}

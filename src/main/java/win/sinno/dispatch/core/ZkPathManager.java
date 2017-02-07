package win.sinno.dispatch.core;

/**
 * zk path manager
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/6 上午10:32
 */
public class ZkPathManager {

    /**
     * 命名空间
     */
    private String namespace;

    /**
     * 机器名
     */
    private String machineName;

    /**
     * leader 选举路径
     */
    private String leaderLatchPath = "/dispatch/latch";

    /**
     * leader path
     */
    private String leaderPath = "/dispatch/leader";

    /**
     * client path
     */
    private String clientPath = "/dispatch/client";

    /**
     * 注册节点
     */
    public static final String MACHINE_REGISTER_NODE = "register";

    /**
     * 详情节点
     */
    public static final String MACHINE_DETAIL_NODE = "detail";

    /**
     * 机器注册路径
     */
    private String clientRegisterPath;

    /**
     * 机器详细属性路径
     */
    private String clientDetailPath;

    /**
     * 当前在线机器注册路径
     */
    private String currClientRegisterPath;

    /**
     * 当前机器详细属性路径
     */
    private String currClientDetailPath;

    private String currClientDeatilIdPath;

    private String currClientDeatilNamePath;

    private String currClientDeatilStausPath;

    private String currClientDeatilIpPath;

    private String currClientDeatilOnlinePath;

    /**
     * 机器名
     *
     * @param machineName
     */
    public ZkPathManager(String namespace, String machineName) {
        this.namespace = namespace;
        this.machineName = machineName;

        clientRegisterPath = clientPath + "/" + MACHINE_REGISTER_NODE;

        clientDetailPath = clientPath + "/" + MACHINE_DETAIL_NODE;

        currClientRegisterPath = clientRegisterPath + "/" + machineName;

        currClientDetailPath = clientDetailPath + "/" + machineName;

        currClientDeatilIdPath = currClientDetailPath + "/" + MachineAttributeNode.ID;

        currClientDeatilNamePath = currClientDetailPath + "/" + MachineAttributeNode.NAME;

        currClientDeatilStausPath = currClientDetailPath + "/" + MachineAttributeNode.STATUS;

        currClientDeatilIpPath = currClientDetailPath + "/" + MachineAttributeNode.IP;

        currClientDeatilOnlinePath = currClientDetailPath + "/" + MachineAttributeNode.ONLINE_TS;
    }

    public String getMachineName() {
        return machineName;
    }

    public String getLeaderLatchPath() {
        return leaderLatchPath;
    }

    public String getLeaderPath() {
        return leaderPath;
    }

    public String getClientPath() {
        return clientPath;
    }

    public String getClientRegisterPath() {
        return clientRegisterPath;
    }

    public String getClientDetailPath() {
        return clientDetailPath;
    }

    public String getCurrClientRegisterPath() {
        return currClientRegisterPath;
    }

    public String getCurrClientDetailPath() {
        return currClientDetailPath;
    }

    public String getCurrClientDeatilIdPath() {
        return currClientDeatilIdPath;
    }

    public String getCurrClientDeatilNamePath() {
        return currClientDeatilNamePath;
    }

    public String getCurrClientDeatilStausPath() {
        return currClientDeatilStausPath;
    }

    public String getCurrClientDeatilIpPath() {
        return currClientDeatilIpPath;
    }

    public String getCurrClientDeatilOnlinePath() {
        return currClientDeatilOnlinePath;
    }

    private static interface MachineAttributeNode {
        public static final String ID = "id";

        public static final String NAME = "name";

        public static final String STATUS = "status";

        public static final String IP = "ip";

        public static final String ONLINE_TS = "online_ts";

    }

}

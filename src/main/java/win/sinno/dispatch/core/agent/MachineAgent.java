package win.sinno.dispatch.core.agent;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import win.sinno.dispatch.constrant.DispatchConfig;
import win.sinno.dispatch.constrant.LoggerConfigs;
import win.sinno.dispatch.model.Machine;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 机器代理
 * 实时更新服务器的状态信息
 * worker及leader都可以获取这些数据
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/6 下午4:43
 */
public class MachineAgent implements IAgent {

    private static final Logger LOG = LoggerConfigs.DISPATCH_LOG;

    private ReentrantReadWriteLock rrwlock = new ReentrantReadWriteLock();

    private Lock wlock = rrwlock.writeLock();

    private Boolean isLeader;

    /**
     * leader 机器名
     */
    private String leaderMachineName;

    /**
     * 本机名
     */
    private String localMachineName = DispatchConfig.getMachineName();


    private Machine leader;

    private Set<Machine> workerSet = Sets.newHashSet();

    /**
     * 线上机器map
     */
    private Map<String, Machine> onlineMachineMap = Maps.newConcurrentMap();

    //线下机器map
    private Map<String, Machine> offMachineMap = Maps.newConcurrentMap();

    /**
     * leader 刷新标记
     */
    private boolean leaderRefreshFlag = false;

    /**
     * 处理
     */
    @Override
    public void handler() throws Exception {
        //ignore
    }

    //检测机器咋在线状态-
    public void checkMachine() {
        //TODO
    }

    /**
     * 上线机器中暑
     */
    private long onlineTotal = 0;

    /**
     * 当前上线机器数
     */
    private long onlineCount = 0;
    /**
     * 当前下线机器数
     */
    private long offlineTotal = 0;

    /**
     * 变更leader数
     */
    private int changeLeaderCount = 0;


    /**
     * 设置leader
     *
     * @param machineName
     */
    public synchronized void setLeaderName(String machineName) {

        this.leaderRefreshFlag = false;

        this.leaderMachineName = machineName;

        leader = null;

        if (!leaderRefreshFlag) {
            refreshMachineLeaderStatus();
        }

        changeLeaderCount++;
    }

    /**
     * 刷新线上机器isLeader标识状态
     */
    private synchronized void refreshMachineLeaderStatus() {

        Collection<Machine> machineCollects = onlineMachineMap.values();
        Iterator<Machine> iterator = machineCollects.iterator();

        Machine machine = null;
        while (iterator.hasNext()) {
            machine = iterator.next();

            boolean isLeader = leaderMachineName.equals(machine.getName());

            if (isLeader) {
                leaderRefreshFlag = true;
                leader = machine;
                workerSet.remove(machine);
            } else {
                if (machine.isLeader()) {
                    if (!workerSet.contains(machine)) {
                        workerSet.add(machine);
                    }
                }
            }
            machine.setIsLeader(isLeader);

            LOG.info("refresh machine :{}", machine);
        }
    }

    /**
     * leader 机器名
     *
     * @return
     */
    public synchronized String getLeaderMachineName() {
        return leaderMachineName;
    }


    /**
     * FIXME leader 机器 有可能 machine未更新
     *
     * @return
     */
    public Machine getLeaderMachine() {
        return onlineMachineMap.get(leaderMachineName);
    }

    /**
     * 获取客户端
     *
     * @return
     */
    public Set<Machine> getWorkerMachines() {
        return workerSet;
    }

    /**
     * 上线 机器
     *
     * @param machine
     */
    public synchronized void online(Machine machine) {
        LOG.info("online machine:{}", new Object[]{machine});

        onlineMachineMap.put(machine.getName(), machine);

        workerSet.add(machine);

        if (!leaderRefreshFlag) {
            refreshMachineLeaderStatus();
        }

        onlineTotal++;
        onlineCount = onlineMachineMap.size();
    }


    /**
     * 下线 机器
     *
     * @param machine
     */
    public synchronized void offline(Machine machine) {

        LOG.info("offline machine:{}", new Object[]{machine});

        //online 是否 含有
        boolean isContainsFlag = onlineMachineMap.containsKey(machine.getName());

        if (isContainsFlag) {
            onlineMachineMap.remove(machine.getName());
        }

        //塞进 下线机器map
        offMachineMap.put(machine.getName(), machine);

        offlineTotal++;
    }


    public void setIsLeader(Boolean isLeader) {
        this.isLeader = isLeader;
    }

    public Boolean isLeader() {
        return isLeader;
    }

    /**
     * 机器代理状态
     */
    public void status() {
        //状态
        StringBuilder status = new StringBuilder();
        status.append("\n=====machine status========================")
                .append("\n online count       : ").append(onlineCount)
                .append("\n online total       : ").append(onlineTotal)
                .append("\n offline total      : ").append(offlineTotal)
                .append("\n changeLeaderCount  : ").append(changeLeaderCount);


        LOG.info(status.toString());
    }
}

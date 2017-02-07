package win.sinno.dispatch.core.agent;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import win.sinno.dispatch.constrant.LoggerConfigs;
import win.sinno.dispatch.model.Machine;

import java.util.Map;
import java.util.Set;

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
    /**
     * 本机
     */
    private Machine localMachine;

    /**
     * leader机器
     */
    private Machine leaderMachine;

    /**
     * 在线的机器
     */
    private Set<Machine> machines = Sets.newConcurrentHashSet();

    /**
     * 下线的机器
     */
    private Set<Machine> offlineMachines = Sets.newConcurrentHashSet();

    /**
     * 工作机
     */
    private Set<Machine> workerMachines = Sets.newConcurrentHashSet();

    /**
     * 机器map
     */
    private Map<String, Machine> machineMap = Maps.newConcurrentMap();


    /**
     * 处理
     */
    @Override
    public void handler() throws Exception {

    }
}

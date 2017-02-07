package win.sinno.dispatch.core.agent;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.slf4j.Logger;
import win.sinno.dispatch.constrant.LoggerConfigs;
import win.sinno.dispatch.core.MainDaemon;

import java.io.IOException;

/**
 * 领导选举 代理
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/4 下午2:58
 */
public class LeaderLatchAgent implements IAgent {

    private static final Logger LOG = LoggerConfigs.DISPATCH_LOG;
    /**
     * 核心守护进程
     */
    private MainDaemon mainDaemon;

    private LeaderLatch leaderLatch;

    private CuratorFramework curatorFramework;

    private String latchPath;

    private String id;

    private boolean isLeader = Boolean.FALSE;

    //agent
    private ZkNodeAgent zkNodeAgent;


    public LeaderLatchAgent(MainDaemon mainDaemon, CuratorFramework curatorFramework, String latchPath, String id, ZkNodeAgent zkNodeAgent) {
        this.mainDaemon = mainDaemon;
        this.curatorFramework = curatorFramework;
        this.latchPath = latchPath;
        this.id = id;
        this.zkNodeAgent = zkNodeAgent;
    }


    /**
     * 处理
     */
    @Override
    public void handler() throws Exception {
        //leader 选举 - 重新 worker->leader 逻辑处理
        leaderLatch = new LeaderLatch(curatorFramework, latchPath, id, LeaderLatch.CloseMode.NOTIFY_LEADER);

        leaderLatch.addListener(new DispatchLeaderLatchListener());

        leaderLatch.start();

        LOG.info("leader latch start..");
    }

    public void stop() {
        if (leaderLatch != null) {
            try {
                leaderLatch.close(LeaderLatch.CloseMode.NOTIFY_LEADER);
                leaderLatch = null;
            } catch (IOException e) {
                LOG.error("leader latch close err.  " + e.getMessage(), e);
            }
        }
    }

    private void becomeLeader() {
        LOG.info("leader latch , localhost is leader.");

        isLeader = Boolean.TRUE;

        this.zkNodeAgent.setLocalLeader(isLeader);
        try {
            this.zkNodeAgent.registerLeaderNode();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void becomeWorker() {
        LOG.info("leader latch , localhost is worker.");

        isLeader = Boolean.FALSE;

        this.zkNodeAgent.setLocalLeader(isLeader);
    }


    /**
     * dispatch leader 选举 监听器
     */
    private class DispatchLeaderLatchListener implements LeaderLatchListener {

        @Override
        public void isLeader() {
            becomeLeader();
        }

        @Override
        public void notLeader() {
            becomeWorker();
        }
    }
}

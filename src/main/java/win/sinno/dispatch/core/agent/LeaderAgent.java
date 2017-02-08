package win.sinno.dispatch.core.agent;

import org.slf4j.Logger;
import win.sinno.dispatch.constrant.LoggerConfigs;

/**
 * leader 代理
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/8 上午9:45
 */
public class LeaderAgent implements IAgent, Runnable {

    private static final Logger LOG = LoggerConfigs.DISPATCH_LOG;

    private MachineAgent machineAgent;

    public LeaderAgent(MachineAgent machineAgent) {
        this.machineAgent = machineAgent;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        while (true) {

            if (machineAgent.isLeader()) {
                try {
                    handler();

                    Thread.sleep(3000l);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            } else {

                try {
                    Thread.sleep(10000l);
                } catch (InterruptedException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }

    }

    /**
     * 处理
     */
    @Override
    public void handler() throws Exception {

        // worker数量
        int workerSize = machineAgent.getWorkerSize();

        if (workerSize > 0) {
            LOG.info("leader doing.worker size:{}", workerSize);
        } else {
            LOG.info("leader doing.[wait worker join]");
        }
    }


}

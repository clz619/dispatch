package win.sinno.dispatch.core.agent;

/**
 * 领导选举 代理
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/4 下午2:58
 */
public class LeaderLatchAgent implements IAgent {


    public LeaderLatchAgent(){

    }
    /**
     * 处理
     */
    @Override
    public void handler() {
        //leader 选举 - 重新 worker->leader 逻辑处理
    }
}

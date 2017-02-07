package win.sinno.dispatch.core.node;

import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.slf4j.Logger;
import win.sinno.dispatch.constrant.LoggerConfigs;

/**
 * leader node 事件 监听
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/6 下午5:46
 */
public class ZkLeaderNodePathChildrenCacheListener implements NodeCacheListener {

    private static final Logger LOG = LoggerConfigs.DISPATCH_LOG;



//    @Override
//    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
//
//        LOG.info("leader node path children cache listener:-----type:{},data:{}", new Object[]{event.getType(), event.getData()});
//
//        ChildData data = event.getData();
//
//        switch (event.getType()) {
//            case CHILD_ADDED:
//                //TODO 选举出新的leader， 设置为leader
//                LOG.info("CHILD_ADDED : " + data.getPath() + " data:" + data.getData());
//                break;
//            case CHILD_UPDATED:
//                //TODO 选举出新的leader， 设置为leader
//                LOG.info("CHILD_UPDATED : " + data.getPath() + " data:" + data.getData());
//                break;
//            case CHILD_REMOVED:
//                //TODO 移除leader
//                LOG.info("CHILD_REMOVED : " + data.getPath() + " data:" + data.getData());
//                break;
//
//            default:
//                break;
//        }
//
//    }

    /**
     * Called when a change has occurred
     */
    @Override
    public void nodeChanged() throws Exception {
        //内容变化
    }
}

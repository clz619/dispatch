package win.sinno.dispatch.core.node;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import win.sinno.dispatch.constrant.LoggerConfigs;

/**
 * 机器注册节点监听器
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/6 下午6:03
 */
public class ZkClientRegisterNodePathChildrenCacheListener implements PathChildrenCacheListener {
    private static final Logger LOG = LoggerConfigs.DISPATCH_LOG;

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {

        LOG.info("leader node path children cache listener:-----type:{},data:{}", new Object[]{event.getType(), event.getData()});

        ChildData data = event.getData();

        switch (event.getType()) {
            case CHILD_ADDED:
                //TODO 注册了新的machine
                LOG.info("CHILD_ADDED : " + data.getPath() + " data:" + data.getData());
                break;
            case CHILD_UPDATED:
                //TODO 更新machine信息
                LOG.info("CHILD_UPDATED : " + data.getPath() + " data:" + data.getData());
                break;
            case CHILD_REMOVED:
                //TODO 移除machine信息
                LOG.info("CHILD_REMOVED : " + data.getPath() + " data:" + data.getData());
                break;

            default:
                break;
        }

    }
}

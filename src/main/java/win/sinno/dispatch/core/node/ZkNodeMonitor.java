package win.sinno.dispatch.core.node;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.slf4j.Logger;
import win.sinno.dispatch.constrant.LoggerConfigs;

/**
 * zk leader node 监听
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/6 下午5:44
 */
public class ZkNodeMonitor {

    private static final Logger LOG = LoggerConfigs.DISPATCH_LOG;

    private PathChildrenCache pathChildrenCache;

    private CuratorFramework curatorFramework;

    private String path;

    private PathChildrenCacheListener pathChildrenCacheListener;

    public ZkNodeMonitor(CuratorFramework curatorFramework, String path, PathChildrenCacheListener pathChildrenCacheListener) {
        this.curatorFramework = curatorFramework;
        this.path = path;
        this.pathChildrenCacheListener = pathChildrenCacheListener;
    }

    public void handler() throws Exception {
        this.pathChildrenCache = new PathChildrenCache(curatorFramework, path, false);
        this.pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        this.pathChildrenCache.start();

        LOG.info("zk node monitor path:{}", new Object[]{path});
    }

}

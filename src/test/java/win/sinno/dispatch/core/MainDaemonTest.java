package win.sinno.dispatch.core;

import org.junit.Test;

/**
 * 主 守护进程 est
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/4 上午11:58
 */
public class MainDaemonTest {

    @Test
    public void testInitClient() {

        String zkHost = "192.168.1.35:2181";
        String namespace = "sinno";
        MainDaemon mainDaemon = new MainDaemon(zkHost, namespace);

        mainDaemon.startClient();
    }
}

package win.sinno.dispatch.constrant;

import win.sinno.common.util.NetworkUtil;
import win.sinno.common.util.RuntimeUtil;

import java.net.UnknownHostException;

/**
 * dispatch
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/4 上午11:42
 */
public final class DispatchConfig {

    //zk default conf

    /**
     * session 超时时间 单位：毫秒
     */
    public static final int ZK_SESSION_TIMEOUT_MS = 30000;

    /**
     * conn 超时时间 单位：毫秒
     */
    public static final int ZK_CONN_TIMEOUT_MS = 15000;

    private static String machineName = null;

    /**
     * 本机名称
     *
     * @return
     */
    public static String getMachineName() {
        if (machineName == null) {
            synchronized (DispatchConfig.class) {
                if (machineName == null) {
                    try {
                        machineName = "machine#" + NetworkUtil.getHostName() + "#" + RuntimeUtil.getRunPid();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return machineName;
    }

}

package win.sinno.dispatch.core;

/**
 * 后台守护进程
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/4 下午2:44
 */
public interface IDaemon {

    /**
     * 启动
     */
    void start();

    /**
     * 关闭
     */
    void stop();
}

package win.sinno.dispatch.model;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 客户端机器
 *
 * @author : admin@chenlizhong.cn
 * @version : 1.0
 * @since : 2017/2/6 上午11:45
 */
public class Machine {

    /**
     * 机器注册id
     */
    private Long id;

    /**
     * 机器名
     */
    private String name;

    /**
     * 上线时间
     */
    private Long onlineTs;

    /**
     * 下线时间
     */
    private Long offlineTs;

    /**
     * 机器状态（online,offline）
     * 上线:online
     * 下线:offline
     */
    private String status;

    /**
     * ip列表
     */
    private List<String> ipAddrs;

    /**
     * 是否为leader
     */
    private boolean isLeader;

    private Date gmtCreate;

    private Date gmtModified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOnlineTs() {
        return onlineTs;
    }

    public void setOnlineTs(Long onlineTs) {
        this.onlineTs = onlineTs;
    }

    public Long getOfflineTs() {
        return offlineTs;
    }

    public void setOfflineTs(Long offlineTs) {
        this.offlineTs = offlineTs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getIpAddrs() {
        return ipAddrs;
    }

    public void setIpAddrs(List<String> ipAddrs) {
        this.ipAddrs = ipAddrs;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean leader) {
        isLeader = leader;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    @Override
    public int hashCode() {
        int hashcode = 17;

        if (id != null) {
            hashcode = hashcode * 31 + id.hashCode() - 1;
        }

        if (StringUtils.isNotEmpty(name)) {
            hashcode = hashcode * 31 + name.hashCode() - 1;
        }

        return hashcode;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Machine)) {
            return false;
        }

        Machine other = (Machine) obj;

        if (this.getId() == null || other.getId() == null || !this.getId().equals(other.getId())) {
            //id
            return false;
        }

        if (this.getName() == null || other.getName() == null || !this.getName().equals(other.getName())) {
            //id
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Machine{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", onlineTs=" + onlineTs +
                ", offlineTs=" + offlineTs +
                ", status='" + status + '\'' +
                ", ipAddrs=" + ipAddrs +
                ", isLeader=" + isLeader +
                ", gmtCreate=" + gmtCreate +
                ", gmtModified=" + gmtModified +
                '}';
    }
}

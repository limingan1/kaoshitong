package com.suntek.vdm.gw.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.suntek.vdm.gw.common.util.security.EncryptionMachine;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.core.enumeration.NodeType;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@Data
@Slf4j
@TableName(value = "v3_vdm_smc_node")
public class NodeData implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String name;

    private String areaCode;

    private String childAreaCode;

    private Integer type;

    private Integer businessType;

    private String smcVersion;

    private String ip;

    private Integer ssl;

    private String username;

    private String password;

    //welink节点使用
    private String vmrConfId;

    private Date createTime;

    private Date updateTime;

    private String enType;

    private String securityVersion;

    private Integer permissionSwitch;
    private String clientId;
    private String clientSecret;
    private String addressBookUrl;

    public boolean isHttps() {
        if (ssl.equals(1)) {
            return true;
        }
        return false;
    }


    public String getSmcVersion() {
        if (smcVersion == null) {
            return "3.0";
        }
        return smcVersion;
    }

    public boolean isRealNode() {
        if (businessType == null || businessType.equals(NodeBusinessType.SMC)) {
            return true;
        }
        return false;
    }

    public NodeType toNodeType() {
        return NodeType.valueOf(type);
    }


    public NodeBusinessType toNodeBusinessType() {
        return NodeBusinessType.valueOf(businessType);
    }


    /**
     * 解密密码
     *
     * @return
     * @throws Exception
     */
    public String decryptPassword() {
        try {
            byte[] iv = EncryptionMachine.MD5_16(username);
            String securityPassword = EncryptionMachine.decrypt(password, enType, iv);
            return securityPassword;
        } catch (Exception e) {
            log.error("decrypt password error:{}", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加密密码
     *
     * @throws Exception
     */
    public void encryptPassword() {
        try {
            byte[] iv = EncryptionMachine.MD5_16(username);
            String encryptPassword = EncryptionMachine.encrypt(password, CoreConfig.EN_TYPE, iv);
            this.password = encryptPassword;
        } catch (Exception e) {
            log.error("encrypt password error:{}", e);
            e.printStackTrace();
        }
    }


    public GwNode toGwNode() {
        GwNode gwNode = new GwNode();
        gwNode.setId(id);
        gwNode.setAreaCode(areaCode);
        gwNode.setIp(ip);
        gwNode.setName(name);
        gwNode.setSmcVersion(getSmcVersion());
        gwNode.setBusinessType(businessType);
        gwNode.setPermissionSwitch(permissionSwitch);
        gwNode.setDisplayDirectlyUnder(getDisplayDirectlyUnder());
        gwNode.setChild(new ArrayList<>());
        return gwNode;
    }

    public boolean getDisplayDirectlyUnder() {
        return businessType == 1 || (StringUtils.isNotBlank(clientId) && StringUtils.isNotBlank(clientSecret) && StringUtils.isNotBlank(addressBookUrl));
    }
    public GwId toGwId() {
        GwId GwId = new GwId(id, areaCode);
        return GwId;
    }

    @Override
    public String toString() {
        return "NodeData{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", areaCode='" + areaCode + '\'' +
                ", childAreaCode='" + childAreaCode + '\'' +
                ", type=" + type +
                ", businessType=" + businessType +
                ", smcVersion='" + smcVersion + '\'' +
                ", ip='" + ip + '\'' +
                ", ssl=" + ssl +
                ", username='" + username + '\'' +
                ", password='" + "******" + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", enType='" + enType + '\'' +
                ", securityVersion='" + securityVersion + '\'' +
                '}';
    }
}
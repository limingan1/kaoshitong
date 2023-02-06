package com.suntek.vdm.gw.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.common.util.security.EncryptionMachine;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@Data
@Slf4j
@TableName(value = "v3_vdm_vm_node")
public class VmNodeData  implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    private String parentId;

    private String name;

    private String areaCode;

    private String username;

    private String password;

    private String orgId;

    private Integer permissionSwitch;

    private Date createTime;

    private Date updateTime;

    private String enType;

    private String securityVersion;

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
        gwNode.setName(name);
        gwNode.setIsVmNode(true);
        gwNode.setOrgId(orgId);
        gwNode.setPermissionSwitch(permissionSwitch);
        gwNode.setParentId(parentId);
        gwNode.setBusinessType(1);
        gwNode.setDisplayDirectlyUnder(true);
        gwNode.setChild(new ArrayList<>());
        return gwNode;
    }

    public GwId toGwId() {
        GwId GwId = new GwId(id, areaCode);
        return GwId;
    }

}

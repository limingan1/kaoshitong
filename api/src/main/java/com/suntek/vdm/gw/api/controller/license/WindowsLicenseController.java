package com.suntek.vdm.gw.api.controller.license;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huawei.vdmserver.common.dto.license.*;
import com.huawei.vdmserver.common.dto.response.GlobalRestResponse;
import com.suntek.vdm.gw.api.service.SecureService;
import com.suntek.vdm.gw.core.annotation.PassLicense;
import com.suntek.vdm.gw.license.service.WindowLicenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/windowsLicense")
@ConditionalOnProperty(name = "cas.database.db_type",havingValue = "sqlserver")
public class WindowsLicenseController {

    @Autowired
    private WindowLicenseService windowLicenseService;
    @Autowired
    private SecureService secureService;
    @Autowired
    private HttpServletResponse response;

    @PassLicense
    @GetMapping("/getAllLicense")
    public ResponseEntity<?> getAllLicense() {
        secureService.setResponseHeader(response);
        GlobalRestResponse<?> allResource= windowLicenseService.getAllResource();
        GlobalRestResponse<?> expiredDate= windowLicenseService.getExpiredDate();
        GlobalRestResponse<?> vdcLicenseStatus= windowLicenseService.getVdcLicenseStatus();
        GlobalRestResponse<?> esnCode= windowLicenseService.getEsnCode();

        Resource rs = new Resource();
        String esn = "--";
        if(esnCode.getCode() == 0){
            esn = (String) esnCode.getData();
        }
        rs.setEsn(esn);
        Integer state = (Integer) vdcLicenseStatus.getData();
        String expired = (String) expiredDate.getData();
        rs.setState(state);
        rs.setExpiredDate(expired);
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        if( state == 5 ){//失效，获取宽限时间
            GlobalRestResponse<?> rvkTicketAndRvkTime = windowLicenseService.getRvkTicketAndRvkTime();
            if(rvkTicketAndRvkTime.getCode()==10067 && rvkTicketAndRvkTime.getData()!=null){
                LicRvkInfo licRvkInfo = (LicRvkInfo) rvkTicketAndRvkTime.getData();
                rs.setRvkTime(licRvkInfo.getRvkTime());
                rs.setRvkTicket(licRvkInfo.getRvkTicket());
                try {
                    Date parse = ft.parse(rs.getRvkTime());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(parse);
                    cal.add(Calendar.DAY_OF_MONTH, 60);
                    expired = ft.format(cal.getTime());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        String resource = (String) allResource.getData();
        LicenseResource licenseResource = JSONObject.parseObject(resource, LicenseResource.class);

        Resource rss = licenseResource.getResource();
        interfaceOpen(rss);
        if(state == 2 && rss.isIfNew()){
            expired="";
        }
        rss.setExpiredDate(expired);
        rss.setEsn(rs.getEsn());
        rss.setState(rs.getState());
        rss.setRvkTicket(rs.getRvkTicket());
        rss.setRvkTime(rs.getRvkTime());

        return new ResponseEntity(JSON.toJSONString(rss), HttpStatus.OK);
    }

    @PassLicense
    @PostMapping("/importLicense")
    public ResponseEntity<?> importLicense(MultipartFile file) {
        return  new ResponseEntity<>(JSON.toJSONString(windowLicenseService.importLicenseString(file,false)), HttpStatus.OK);
    }

    @PassLicense
    @PostMapping("/revokeLicense")
    public ResponseEntity<?> revokeLicense() {
        return  new ResponseEntity<>(JSON.toJSONString(windowLicenseService.revokeLicense()), HttpStatus.OK);
    }


    public void interfaceOpen(com.huawei.vdmserver.common.dto.license.Resource rs) {
        if (rs.getApiability() == null) {
            Apiability apiability = new Apiability();
            apiability.setTotal("0");
            apiability.setUsed("0");
            rs.setApiability(apiability);
        }
        if (rs.getWelink() == null) {
            Welink welink = new Welink();
            welink.setTotal("0");
            welink.setUsed("0");
            rs.setWelink(welink);
            return;
        }
        String welinkTotal = rs.getWelink().getTotal();
        String endWelinkTotal = welinkTotal.substring(0, 1);
        if (Integer.parseInt(endWelinkTotal) > 0) {
            Welink welink = new Welink();
            welink.setTotal("1");
            welink.setUsed("1");
            rs.setWelink(welink);
        } else {
            Welink welink = new Welink();
            welink.setTotal("0");
            welink.setUsed("0");
            rs.setWelink(welink);
        }


    }


}

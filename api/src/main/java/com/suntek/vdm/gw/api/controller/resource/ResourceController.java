package com.suntek.vdm.gw.api.controller.resource;

import com.suntek.vdm.gw.api.service.SecureService;
import com.suntek.vdm.gw.core.annotation.PassLicense;
import com.suntek.vdm.gw.core.annotation.PassToken;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.smc.service.impl.SmcHttpServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

@Controller
public class ResourceController {

    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private HttpServletResponse response;
    @Autowired
    private SecureService secureService;

    @PassLicense
    @PassToken
    @GetMapping("/index.html")
    public ModelAndView indexPage() {
        return defaultPage();
    }

    @PassLicense
    @PassToken
    @GetMapping("/")
    public ModelAndView defaultPage() {
        secureService.setResponseHeader(response);
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("ip", SmcHttpServiceImpl.ip == null ? "" : SmcHttpServiceImpl.ip);
        return mav;
    }

    @PassToken
    @PassLicense
    @GetMapping("/config")
    public ModelAndView configPage() {
        secureService.setResponseHeader(response);
        return new ModelAndView("config");
    }

    @PassToken
    @PassLicense
    @GetMapping("/license")
    public ModelAndView licensePage() {
        secureService.setResponseHeader(response);
        return new ModelAndView("license");
    }
    @PassToken
    @PassLicense
    @GetMapping("/virtualNode")
    public ModelAndView virtualNodePage() {
        secureService.setResponseHeader(response);
        NodeData local = nodeDataService.getLocal();
        ModelAndView virtualNode = new ModelAndView("virtualNode");
        virtualNode.addObject("notConfigLocal", local == null);
        return virtualNode;
    }
}

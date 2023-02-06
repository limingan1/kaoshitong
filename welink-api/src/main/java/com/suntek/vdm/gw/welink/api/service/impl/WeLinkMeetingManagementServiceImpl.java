package com.suntek.vdm.gw.welink.api.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.util.RandomId;
import com.suntek.vdm.gw.welink.api.request.GetMeetingDetailRequest;
import com.suntek.vdm.gw.welink.api.request.GetMeetingListRequest;
import com.suntek.vdm.gw.welink.api.request.ScheduleMeetingRequest;
import com.suntek.vdm.gw.welink.api.response.GetMeetingDetailResponse;
import com.suntek.vdm.gw.welink.api.response.GetMeetingListResponse;
import com.suntek.vdm.gw.welink.api.response.QueryUserResultDTO;
import com.suntek.vdm.gw.welink.api.response.ScheduleMeetingResponse;
import com.suntek.vdm.gw.welink.api.service.WeLinkMeetingManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class WeLinkMeetingManagementServiceImpl extends WeLinkBaseServiceImpl implements WeLinkMeetingManagementService {

    @Autowired
    @Qualifier("weLinkHttpServiceImpl")
    private HttpService httpService;

    /**
     * 预约会议
     *
     * @param request
     * @param token
     * @return
     * @throws MyHttpException
     */
    public ScheduleMeetingResponse scheduleMeeting(ScheduleMeetingRequest request, String token) throws MyHttpException {
        String response = httpService.post("/mmc/management/conferences", request, tokenHandle(token)).getBody();
        JSONArray respArr = JSONArray.parseArray(response);
        JSONObject respJson = respArr.getJSONObject(0);
        return JSONObject.toJavaObject(respJson, ScheduleMeetingResponse.class);
    }

    /**
     * 获取会议详情
     * @param request
     * @param token
     * @return
     * @throws MyHttpException
     */
    public GetMeetingDetailResponse getMeetingDetail(GetMeetingDetailRequest request, String token) throws MyHttpException {
        if (request.getOffset() == null) {
            request.setOffset(0);
        }
        if (request.getLimit() == null) {
            request.setLimit(20);
        }
        String url = String.format("/mmc/management/conferences/confDetail?offset=%s&limit=%s", request.getOffset(), request.getLimit());
        if (request.getSearchKey() != null) {
            url += "&searchKey=" + request.getSearchKey();
        }
        if (request.getConferenceID() != null) {
            url += "&conferenceID=" + request.getConferenceID();
        }
        String response = httpService.get(url, null, tokenHandle(token)).getBody();
        return JSON.parseObject(response, GetMeetingDetailResponse.class);
    }


    /**
     * 获取会议列表
     * @param request
     * @param token
     * @return
     * @throws MyHttpException
     */
    public GetMeetingListResponse getMeetingList(GetMeetingListRequest request, String token) throws MyHttpException {
        if (request.getOffset()==null){
            request.setOffset(0);
        }
        if (request.getLimit()==null){
            request.setLimit(20);
        }
        String url=String.format("/mmc/management/conferences/online?offset=%s&limit=%s&queryAll=true",request.getOffset(),request.getLimit());
        if (request.getSearchkey()!=null){
            url+="&searchKey="+request.getSearchkey();
        }
        String response = httpService.get(url,null, tokenHandle(token)).getBody();
        return JSON.parseObject(response, GetMeetingListResponse.class);
    }

    @Override
    public JSONObject getMeetingControlToken(String conferenceID, String password, String loginType,String ip)throws MyHttpException {
        String url = "/mmc/control/conferences/token?conferenceID=" + conferenceID;
        MultiValueMap<String,String> headers=new LinkedMultiValueMap<>();
        headers.set("X-Password",password);
        headers.set("X-Login-Type",loginType);
        String requestId = RandomId.getGUID();
        headers.set("RequestId",requestId);
        String body = httpService.get(url, "", headers).getBody();
        return JSON.parseObject(body);
    }

    @Override
    public JSONObject getConference(String token, String conferenceId,String ip) throws MyHttpException{
        String url = "/mmc/management/conferences/confDetail?conferenceID=" + conferenceId;
        String body = httpService.get(url, "", tokenHandle(token)).getBody();
        return JSON.parseObject(body);
    }
    @Override
    public JSONObject getWebSocketTemporaryToken(String conferenceID, String meetingControlToken, String wsUrl)throws MyHttpException{
        String ip = wsUrl.substring(6);
        String url = "https://" + ip + "/cms/open/rest/confctl/"+conferenceID+"/wstoken";
//        String url = "/cms/open/rest/confctl/"+conferenceID+"/wstoken";
        MultiValueMap<String,String> headers  = new LinkedMultiValueMap<>();
        headers.set("Conference-Authorization", meetingControlToken);
        String requestId = RandomId.getGUID();
        headers.set("RequestId",requestId);
        String body = httpService.get(url, "", headers).getBody();
//        boolean isRetry = false;
//        if(body!=null && body.contains("11072065")){
//            if(conference.getReTryToGetTokenCount()<5){
//                conference.setToken("");
//                isRetry = true;
//            }
//        }
//        if(isRetry){
//            meetingControlToken = conference.getToken();
//            return getWebSocketTemporaryToken(conferenceID,meetingControlToken,wsUrl);
//        }
        return JSON.parseObject(body);
    }

    @Override
    public void participantsControl(String url, String body, String token) throws MyHttpException {
        httpService.put(url, body, conferenceTokenHandle(token)).getBody();
    }

    @Override
    public QueryUserResultDTO getUserDcsMenber(String token) throws MyHttpException {
        String response = httpService.get("/usg/dcs/member", null, tokenHandle(token)).getBody();

        return JSONObject.parseObject(response, QueryUserResultDTO.class);
    }

    @Override
    public void deleteMeeting(String conferenceId, String token) throws MyHttpException {
        String response = httpService.delete(String.format("/mmc/management/conferences?conferenceID=%s&type=1", conferenceId),null, tokenHandle(token)).getBody();
    }
}

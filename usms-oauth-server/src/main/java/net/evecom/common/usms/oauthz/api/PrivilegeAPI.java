/*
 * Copyright (c) 2005, 2017, EVECOM Technology Co.,Ltd. All rights reserved.
 * EVECOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package net.evecom.common.usms.oauthz.api;

import net.evecom.common.usms.core.vo.ErrorStatus;
import net.evecom.common.usms.entity.PrivilegeEntity;
import net.evecom.common.usms.entity.UserEntity;
import net.evecom.common.usms.constant.Constants;
import net.evecom.common.usms.oauthz.service.OAuthService;
import net.evecom.common.usms.uma.service.PrivilegeService;
import net.evecom.common.usms.uma.service.UserService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 描述 权限相关API接口
 *
 * @author Wash Wang
 * @version 1.0
 * @created 2017/5/8 10:48
 */
@RestController
@RequestMapping("/v1/openapi/")
public class PrivilegeAPI {

    /**
     * 注入PrivilegeService
     */
    @Autowired
    private PrivilegeService privilegeService;

    /**
     * 注入OAuthService
     */
    @Autowired
    private OAuthService oAuthService;

    /**
     * 注入UserService
     */
    @Autowired
    private UserService userService;

    /**
     * 判断是否拥有该权限
     *
     * @param request
     * @return ResponseEntity
     */
    @RequestMapping(value = "privilege/exist", produces = "application/json; charset=UTF-8")
    public ResponseEntity hasPrivilege(HttpServletRequest request)
            throws OAuthProblemException, OAuthSystemException {
        String privilegeName = request.getParameter("privilege");
        if (StringUtils.isEmpty(privilegeName)) {
            ErrorStatus errorStatus = new ErrorStatus
                    .Builder(ErrorStatus.INVALID_PARAMS, Constants.INVALID_PARAMS)
                    .buildJSONMessage();
            return new ResponseEntity(errorStatus.getBody(), HttpStatus.BAD_REQUEST);
        }
        // 构建OAuth资源请求
        OAuthAccessResourceRequest oauthRequest = new OAuthAccessResourceRequest(request, ParameterStyle.QUERY);
        // 获取Access Token
        String accessToken = oauthRequest.getAccessToken();
        // 获取用户名
        String loginName = oAuthService.getLoginNameByAccessToken(accessToken);
        // 获得用户实体类
        UserEntity user = userService.getUserByLoginName(loginName);
        JSONObject jsonObject = new JSONObject();
        if (privilegeService.hasPrivilege(user.getId(), privilegeName)) {
            jsonObject.put("result", true);
        } else jsonObject.put("result", false);
        return new ResponseEntity(jsonObject.toString(), HttpStatus.OK);
    }

    /**
     * 获取权限列表
     *
     * @param request
     * @return ResponseEntity
     */
    @RequestMapping(value = "privileges", produces = "application/json; charset=UTF-8")
    public ResponseEntity getPrivileges(HttpServletRequest request) {
        String applicationName = request.getParameter("application");
        if (StringUtils.isEmpty(applicationName)) {
            ErrorStatus errorStatus = new ErrorStatus
                    .Builder(ErrorStatus.INVALID_PARAMS, Constants.INVALID_PARAMS)
                    .buildJSONMessage();
            return new ResponseEntity(errorStatus.getBody(), HttpStatus.BAD_REQUEST);
        }
        List<PrivilegeEntity> privileges = privilegeService.listPrivsByAppName(applicationName);
        // 构造操作
        JSONArray privJsonArr = new JSONArray();
        for (PrivilegeEntity privilege : privileges) {
            JSONObject privJson = new JSONObject();
            privJson.put("id", privilege.getId());
            privJson.put("label", privilege.getLabel());
            privJson.put("name", privilege.getName());
            privJsonArr.add(privJson);
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("privileges", privJsonArr);
        return new ResponseEntity(jsonObject.toString(), HttpStatus.OK);
    }

}

/*
 * Copyright (c) 2005, 2017, EVECOM Technology Co.,Ltd. All rights reserved.
 * EVECOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package net.evecom.common.usms.oauth2.service.impl;

import net.evecom.common.usms.core.util.MD5Util;
import net.evecom.common.usms.uma.service.ApplicationService;
import net.evecom.common.usms.oauth2.service.OAuthService;
import net.evecom.common.usms.uma.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuer;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 描述 OAuth Service层实现
 *
 * @author Wash Wang
 * @version 1.0
 * @created 2017/4/25 8:52
 */
@Transactional
@Service
public class OAuthServiceImpl implements OAuthService {
    /**
     * 授权码
     */
    private static final String AUTH_CODE = "authCode";
    /**
     * 访问令牌
     */
    private static final String ACCESS_TOKEN = "accessToken";

    /**
     * 令牌有效时间
     */
    @Value("${accessToken.expires}")
    private Long accessTokenExpires = 3600L;

    /**
     * 授权码有效时间
     */
    @Value("${authCode.expires}")
    private Long authCodeExpires = 300L;

    /**
     * 注入ApplicationService
     */
    @Autowired
    private ApplicationService applicationService;

    /**
     * 注入UserService
     */
    @Autowired
    private UserService userService;

    /**
     * redis模板对象
     */
    private RedisTemplate<String, String> redisTemplate;

    /**
     * redis值操作对象
     */
    private ValueOperations<String, String> valueOperations;

    @Autowired
    public OAuthServiceImpl(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
    }

    /**
     * 构建关联关系KEY
     *
     * @param loginName
     * @param clientId
     * @return
     */
    private String getRelationKey(String paramName, String loginName, String clientId) {
        StringBuffer sb = new StringBuffer();
        String relation = sb.append(paramName).append(":")
                .append(loginName).append(",").append(clientId)
                .toString();
        String result = MD5Util.getMD5(relation);
        return result;
    }

    /**
     * 存入授权码
     *
     * @param authCode
     * @param loginName
     */
    @Override
    public void addAuthCode(String authCode, String loginName, String clientId) {
        // 获得关联关系KEY
        String relationKey = getRelationKey(AUTH_CODE, loginName, clientId);

        // 设置authCode
        valueOperations.set(relationKey, authCode);
        valueOperations.set(authCode, loginName);

        // 设置key超时时间
        redisTemplate.expire(relationKey, authCodeExpires, TimeUnit.SECONDS);
        redisTemplate.expire(authCode, authCodeExpires, TimeUnit.SECONDS);
    }

    /**
     * 存入令牌
     *
     * @param accessToken
     * @param loginName
     * @param clientId
     */
    @Override
    public void addAccessToken(String accessToken, String loginName, String clientId) {
        // 获得关联关系KEY
        String relationKey = getRelationKey(ACCESS_TOKEN, loginName, clientId);

        // 设置Access Token
        valueOperations.set(relationKey, accessToken);
        valueOperations.set(accessToken, loginName);

        // 设置key超时时间
        redisTemplate.expire(relationKey, accessTokenExpires, TimeUnit.SECONDS);
        redisTemplate.expire(accessToken, accessTokenExpires, TimeUnit.SECONDS);
    }

    /**
     * 根据授权码获取相对应的用户名
     *
     * @param authCode
     * @return
     */
    @Override
    public String getLoginNameByAuthCode(String authCode) {
        return valueOperations.get(authCode);
    }

    /**
     * 根据令牌获取相对应的用户名
     *
     * @param accessToken
     * @return
     */
    @Override
    public String getLoginNameByAccessToken(String accessToken) {
        return valueOperations.get(accessToken);
    }

    /**
     * 检查授权码是否存在
     *
     * @param authCode
     * @return
     */
    @Override
    public boolean checkAuthCode(String authCode) {
        return valueOperations.get(authCode) != null;
    }

    /**
     * 检查令牌是否存在
     *
     * @param accessToken
     * @return
     */
    @Override
    public boolean checkAccessToken(String accessToken) {
        return valueOperations.get(accessToken) != null;
    }

    /**
     * 检查ClientId是否存在
     *
     * @param clientId
     * @return
     */
    @Override
    public boolean checkClientId(String clientId) {
        return applicationService.findByClientId(clientId) != null;
    }

    /**
     * 检查ClientSecret是否存在
     *
     * @param clientSecret
     * @return
     */
    @Override
    public boolean checkClientSecret(String clientSecret) {
        return applicationService.findByClientSecret(clientSecret) != null;
    }

    /**
     * 获得令牌超时时间
     *
     * @return
     */
    @Override
    public long getExpiresIn() {
        return accessTokenExpires;
    }

    /**
     * 删除关联关系所对应的值
     *
     * @param paramName
     * @param loginName
     * @param clientId
     */
    @Override
    public void deleteRelation(String paramName, String loginName, String clientId) {
        // 获得关联关系
        String relationKey = getRelationKey(paramName, loginName, clientId);
        String relationValue = valueOperations.get(relationKey);
        // 删除关系KEY
        if (StringUtils.isNotEmpty(relationKey)) redisTemplate.delete(relationKey);
        // 删除关系值
        if (StringUtils.isNotEmpty(relationValue)) redisTemplate.delete(relationValue);
    }

    /**
     * 获得新的AccessToken，并删除旧的AccessToken和已获得的AuthCode
     *
     * @param loginName
     * @param clientId
     * @param authCode
     * @return
     * @throws OAuthSystemException
     */
    @Override
    public String getNewAccessToken(String loginName, String clientId, String authCode) throws OAuthSystemException {
        OAuthIssuer oAuthIssuer = new OAuthIssuerImpl(new MD5Generator());
        final String accessToken = oAuthIssuer.accessToken();
        // 删除旧的Access Token
        deleteRelation(ACCESS_TOKEN, loginName, clientId);
        // 生成新的Access Token
        addAccessToken(accessToken, loginName, clientId);
        // 得到新的Access Token之后，删除授权码
        deleteRelation(AUTH_CODE, loginName, clientId);
        return accessToken;
    }

    /**
     * 删除旧的AuthCode，并获得新的AuthCode
     *
     * @param loginName
     * @param clientId
     * @return
     * @throws OAuthSystemException
     */
    @Override
    public String getNewAuthCode(String loginName, String clientId) throws OAuthSystemException {
        OAuthIssuerImpl oauthIssuer = new OAuthIssuerImpl(new MD5Generator());
        final String authCode = oauthIssuer.authorizationCode();
        // 删除旧的授权码
        deleteRelation(AUTH_CODE, loginName, clientId);
        // 生成新的Access Token
        addAuthCode(authCode, loginName, clientId);
        return authCode;
    }

}

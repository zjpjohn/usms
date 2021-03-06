/*
 * Copyright (c) 2005, 2017, EVECOM Technology Co.,Ltd. All rights reserved.
 * EVECOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package net.evecom.common.usms.uma.service;

import net.evecom.common.usms.core.service.BaseService;
import net.evecom.common.usms.core.util.SqlFilter;
import net.evecom.common.usms.entity.PrivilegeEntity;
import net.evecom.common.usms.entity.RoleEntity;
import net.evecom.common.usms.entity.UserEntity;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @author Pisces Lu
 * @version 1.0
 * @created 2017-5-8 18:28
 */
public interface RoleService extends BaseService<RoleEntity, Long> {

    /**
     * 根据用户ID来查找角色列表
     *
     * @param userId
     * @return
     */
    List<RoleEntity> listRolesByUserId(long userId);

    /**
     * 判断是否拥有该角色
     *
     * @param userId
     * @param roleName
     * @return
     */
    boolean hasRole(long userId, String roleName);

    /**
     * 查询所有角色列表
     *
     * @param page
     * @param size
     * @param sqlFilter
     * @return
     */
    Page<RoleEntity> listRolesByPage(int page, int size, SqlFilter sqlFilter);

    /**
     * 查找角色Id对应的权限列表
     *
     * @param roleId
     * @return
     */
    List<PrivilegeEntity> listTargetPrivileges(Long roleId);

    /**
     * 查找角色Id对应的未选择权限列表
     *
     * @param roleId
     * @return
     */
    List<PrivilegeEntity> listSourcePrivileges(Long roleId);

    /**
     * 更新角色对应的权限列表
     *
     * @param roleId
     * @param privileges
     */
    void updatePrivileges(Long roleId, String[] privileges);

    /**
     * 根据角色ID查找已选用户列表
     *
     * @param roleId
     * @return
     */
    List<Map<String, Object>> listTargetUsers(Long roleId, SqlFilter sqlFilter);

    /**
     * 根据角色ID查找未选用户列表
     *
     * @param roleId
     * @param institutionId
     * @param key
     * @return
     */
    List<Map<String, Object>> listSourceUsers(Long roleId, Long institutionId, String key);

    /**
     * 更新用户列表
     *
     * @param roleId
     * @param userIds
     */
    void updateUsers(Long roleId, String[] userIds);
}

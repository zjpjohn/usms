/*
 * Copyright (c) 2005, 2017, EVECOM Technology Co.,Ltd. All rights reserved.
 * EVECOM PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */
package net.evecom.common.usms.uma.controller;

import net.evecom.common.usms.core.vo.ResultStatus;
import net.evecom.common.usms.core.util.SqlFilter;
import net.evecom.common.usms.entity.PrivilegeEntity;
import net.evecom.common.usms.entity.RoleEntity;
import net.evecom.common.usms.vo.RoleVO;
import net.evecom.common.usms.uma.service.RoleService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * @author Pisces Lu
 * @version 1.0
 * @created 2017-5-17 15:38
 */
@Controller
@RequestMapping("/role")
public class RoleController {

    /**
     * 日志管理器
     */
    private static Logger logger = LoggerFactory.getLogger(RoleController.class);

    /**
     * @see RoleService
     */
    @Autowired
    private RoleService roleService;

    /**
     * 查找角色列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "list")
    public Page<RoleEntity> listRolesByPage(Integer page, Integer size, HttpServletRequest request) {
        SqlFilter sqlFilter = new SqlFilter();
        if (!StringUtils.isEmpty(request.getParameter("key"))) {
            sqlFilter.addOrFilter("QUERY_r#label_S_LK", request.getParameter("key"));
            sqlFilter.addOrFilter("QUERY_r#name_S_LK", request.getParameter("key"));
        }
        return roleService.listRolesByPage(page, size, sqlFilter);
    }

    @ResponseBody
    @RequestMapping(value = "find")
    public RoleEntity findOne(Long id) {
        return roleService.findOne(id);
    }

    @ResponseBody
    @RequestMapping(value = "delete")
    public ResultStatus delete(String columns) {
        String[] ids = columns.split(",");
        for (String id : ids) {
            roleService.delete(Long.valueOf(id));
        }
        return new ResultStatus(true, "");
    }

    @ResponseBody
    @RequestMapping(value = "saveOrUpdate")
    public ResultStatus saveOrUpdate(@RequestBody RoleVO roleVO) {
        RoleEntity roleEntity;
        try {
            // 将model转换为entity
            roleEntity = new RoleEntity(roleVO);
            roleEntity = roleService.saveOrUpdate(roleEntity);
            // 获得前端传来的集合
            String[] privilegeIds;
            if (StringUtils.isEmpty(roleVO.getPrivilegeIds())) {
                privilegeIds = null;
            } else {
                privilegeIds = roleVO.getPrivilegeIds().split(",");
            }
            // 更新权限
            roleService.updatePrivileges(roleEntity.getId(), privilegeIds);

            String[] userIds;
            if (StringUtils.isEmpty(roleVO.getUserIds())) {
                userIds = null;
            } else {
                userIds = roleVO.getUserIds().split(",");
            }
            // 更新角色
            roleService.updateUsers(roleEntity.getId(), userIds);
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return new ResultStatus(true, "");
    }

    /**
     * 查找已选用户列表
     *
     * @param roleId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "privileges/target")
    public List<PrivilegeEntity> listTargetPrivileges(Long roleId) {
        return roleService.listTargetPrivileges(roleId);
    }

    /**
     * 查找角色Id对应的未选择权限列表
     *
     * @param roleId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "privileges/source")
    public List<PrivilegeEntity> listSourcePrivileges(Long roleId) {
        return roleService.listSourcePrivileges(roleId);
    }

    /**
     * 根据角色ID查找已选用户列表
     *
     * @param roleId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "users/target")
    public List<Map<String, Object>> listTargetUsers(Long roleId) {
        SqlFilter sqlFilter = new SqlFilter();
        return roleService.listTargetUsers(roleId, sqlFilter);
    }

    /**
     * 根据角色ID查找未选用户列表
     *
     * @param roleId
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "users/source")
    public List<Map<String, Object>> listSourceUsers(Long roleId, Long institutionId, HttpServletRequest request) {
        SqlFilter sqlFilter = new SqlFilter();
        if (institutionId != null) {
            sqlFilter.addFilter("QUERY_u#institution_id_L_EQ", institutionId.toString());
        } else {
            sqlFilter.addFilter("QUERY_u#institution_id_L_EQNULL", "");
        }
        if (!StringUtils.isEmpty(request.getParameter("key"))) {
            sqlFilter.addFilter("QUERY_u#name_S_LK", request.getParameter("key"));
        }
        return roleService.listSourceUsers(roleId, sqlFilter);
    }

}

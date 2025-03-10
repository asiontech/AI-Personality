package com.shure.surdes.web.controller.system;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.shure.surdes.common.constant.Constants;
import com.shure.surdes.common.core.domain.AjaxResult;
import com.shure.surdes.common.core.domain.entity.SysMenu;
import com.shure.surdes.common.core.domain.entity.SysUser;
import com.shure.surdes.common.core.domain.model.LoginBody;
import com.shure.surdes.common.utils.SecurityUtils;
import com.shure.surdes.framework.web.service.SysLoginService;
import com.shure.surdes.framework.web.service.SysPermissionService;
import com.shure.surdes.survey.domain.SysUserPlus;
import com.shure.surdes.survey.service.ISysUserPlusService;
import com.shure.surdes.system.service.ISysMenuService;

/**
 * 登录验证
 *
 * @author ruoyi
 */
@RestController
public class SysLoginController {
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;
    
    @Autowired
    ISysUserPlusService sysUserPlusService;

    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody LoginBody loginBody) {
        AjaxResult ajax = AjaxResult.success();
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo() {
    	SysUser user = SecurityUtils.getLoginUser().getUser();
        SysUserPlus sysUserPlus = sysUserPlusService.getById(user.getUserId());
        sysUserPlus.setPassword(null); // 密码置空
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        ajax.put("sysUserPlus", sysUserPlus);
        return ajax;
    }
    
    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters() {
        Long userId = SecurityUtils.getUserId();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}

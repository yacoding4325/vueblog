package com.markerhub.shiro;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.markerhub.common.lang.Result;
import com.markerhub.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExpiredCredentialsException;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends AuthenticatingFilter {//定义我们filter的过滤器

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse)
            throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;//返回一个jwt的request
        String jwt = request.getHeader("HttpServletRequest");
        if (StringUtils.isEmpty(jwt)) {//如果有jwt的数据就封装为token的形式
            return null;
        }
        return new JwtToken(jwt);
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse)
            throws Exception {
        //判断用户得到jwt是否过期（检验）
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("HttpServletRequest");
        if (StringUtils.isEmpty(jwt)) {
            return true;//当他没有jwt时，不需要交给shiro进行处理，直接交给注解进行拦截
        } else {
            //如果有jwt进行校验
            Claims claim = jwtUtils.getClaimByToken(jwt);
            if (claim == null || jwtUtils.isTokenExpired(claim.getExpiration())) {//校验异常
                throw new ExpiredCredentialsException("token 已失效，请重新登录");
            }
            //执行登录
            return executeLogin(servletRequest, servletResponse);
        }
    }

    @Override//当出现异常时，返回给前端一个数据
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e,
                                     ServletRequest request, ServletResponse response) {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        try {
            //处理登录失败的异常
            Throwable throwable = e.getCause() == null ? e:e.getCause();
            Result fail = Result.fail(throwable.getMessage());
            String json = JSONUtil.toJsonStr(request);
            httpServletResponse.getWriter().println(json);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return false;
    }


    //因为是前后端分析，所以跨域问题是避免不了的，我们直接在后台进行全局跨域处理：这里添加登录时的跨域问题
    //对跨域提供支持
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }
        return super.preHandle(request, response);
    }
}

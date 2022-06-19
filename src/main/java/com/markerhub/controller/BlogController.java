package com.markerhub.controller;


import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.markerhub.common.lang.Result;
import com.markerhub.entity.Blog;
import com.markerhub.service.BlogService;
import com.markerhub.util.JwtUtils;
import com.markerhub.util.ShiroUtil;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.time.LocalDateTime;

@SuppressWarnings({"all"})
@RestController
//@RequestMapping("/blog")
public class BlogController {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    BlogService blogService;

    @GetMapping("/blogs")//分页查询
    public Result list(@RequestParam(defaultValue = "1") Integer currentPage){
//        if(currentPage == null || currentPage < 1) currentPage = 1;
        Page page = new Page(currentPage, 5);
        IPage pageData = blogService.page(page, new QueryWrapper<Blog>().orderByDesc("created"));
        return Result.success(pageData);
    }

    @GetMapping("/blog/{id}")
    public Result detail(@PathVariable (name = "id") Long id){
        Blog blog = blogService.getById(id);
        Assert.notNull(blog, "该博客已经删除！");//判断文章的存在与否
        return Result.success(blog);
    }

    @RequiresAuthentication//认证权限
    @PostMapping("/blog/edit")
    public Result edit(@Validated @RequestBody Blog blog) {
        System.out.println(blog.toString());
        Blog temp = null;//
        if (blog.getId() != null) {//判断文章id是否为空
            temp = blogService.getById(blog.getId());//只编辑自己的文章
            Assert.isTrue(temp.getUserId() == ShiroUtil.getProfile().getId(),"没有权限编辑");
        } else {//没有文章创建一个文章
            temp = new Blog();
            temp.setUserId(ShiroUtil.getProfile().getId());
            temp.setCreated(LocalDateTime.now());
            temp.setStatus(0);
        }
        BeanUtils.copyProperties(blog, temp, "id", "userId", "create", "status");
        blogService.saveOrUpdate(temp);
        return Result.success("操作成功",null);
    }
}

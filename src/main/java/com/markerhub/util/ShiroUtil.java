package com.markerhub.util;

import com.markerhub.shiro.AccountProfile;
import org.apache.shiro.SecurityUtils;

public class ShiroUtil {

    public static AccountProfile getProfile() {//强转为
        return (AccountProfile) SecurityUtils.getSubject().getPrincipal();
    }

}
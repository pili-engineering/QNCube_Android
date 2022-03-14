package com.qiniusdk.userinfoprovide

object UserInfoProvider {
    /**
     * 提供 用户ID
     */
    var getLoginUserIdCall : (()->String)={""}

    /**
     * 提供登陆用户头像
     */
    var getLoginUserAvatarCall:(()->String)={""}

    /**
     * 提供登陆以后名字
     */
    var getLoginUserNameCall :(()->String)={""}

}
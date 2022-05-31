package com.qncube.liveroomcore.datasource

import android.content.Context
import com.alibaba.fastjson.util.ParameterizedTypeImpl
import com.niucube.rtm.RtmCallBack
import com.nucube.http.OKHttpService
import com.nucube.http.PageData
import com.qiniu.jsonutil.JsonUtils
import com.qiniu.qnim.QNIMManager
import com.qncube.liveroomcore.QNLiveCallBack
import com.qncube.liveroomcore.backGround
import com.qncube.liveroomcore.getCode
import com.qncube.liveroomcore.innermodel.InnerUser
import com.qncube.liveroomcore.mode.QNLiveRoomInfo
import com.qncube.liveroomcore.mode.QNLiveUser

class UserDataSource {

    /**
     * 当前房间在线用户
     */
    suspend fun getOnlineUser(liveId: String, page_num: Int, page_size: Int): PageData<QNLiveUser> {

        val p = ParameterizedTypeImpl(
            arrayOf(QNLiveRoomInfo::class.java),
            PageData::class.java,
            PageData::class.java
        )

        val data: PageData<QNLiveUser> = OKHttpService.get(
            "/client/live/room/user_list",
            HashMap<String, String>().apply {
                put("live_id", liveId)
                put("page_num", page_num.toString())
                put("page_size", page_size.toString())
            },
            null,
            p
        )
        return data
    }

    /**
     * 使用用户ID搜索房间用户
     *
     * @param uid
     */
    suspend fun searchUserByUserId(uid: String): QNLiveUser {
        val p = ParameterizedTypeImpl(
            arrayOf(QNLiveUser::class.java),
            List::class.java,
            List::class.java
        )
        return OKHttpService.get<List<QNLiveUser>>("/client/user/users",  HashMap<String, String>().apply {
            put("user_ids", uid)
        }, null, p)[0]
    }

    /**
     * 使用用户im uid 搜索用户
     *
     * @param imUid
     * @param callBack
     */
    suspend fun searchUserByIMUid(imUid: String): QNLiveUser {
        val p = ParameterizedTypeImpl(
            arrayOf(QNLiveUser::class.java),
            List::class.java,
            List::class.java
        )
        return OKHttpService.get<List<QNLiveUser>>("/client/user/imusers", HashMap<String, String>().apply {
            put("im_user_ids", imUid)
        }, null, p)[0]
    }

    fun loginUser(context: Context, callBack: QNLiveCallBack<QNLiveUser>) {
        backGround {
            doWork {
                val user = OKHttpService.get("/client/user/profile", null, InnerUser::class.java)
                var isQnIm = false
                isQnIm = try {
                    QNIMManager.mRtmAdapter.isLogin
                    true
                } catch (e: NoClassDefFoundError) {
                    e.printStackTrace()
                    false
                }
                if (isQnIm) {
                    QNIMManager.init("cigzypnhoyno", context)
                    QNIMManager.mRtmAdapter.login(
                        user.userId,
                        user.imUid,
                        user.im_username,
                        user.im_password,
                        object : RtmCallBack {
                            override fun onSuccess() {
                                callBack.onSuccess(user)
                            }

                            override fun onFailure(code: Int, msg: String) {
                                callBack.onError(code, msg)
                                // callBack.onSuccess(user)
                            }
                        }
                    )
                }
            }
            catchError {
                callBack.onError(it.getCode(), it.message)
            }
        }
    }

    fun updateUser(
        avatar: String,
        nickName: String,
        extensions: HashMap<String, String>?,
        callBack: QNLiveCallBack<Void>
    ) {
        backGround {
            doWork {
                val user = QNLiveUser()
                user.avatar = avatar
                user.nick = nickName
                user.extensions = extensions

                OKHttpService.put("/client/user/user", JsonUtils.toJson(user), Any::class.java)
                callBack.onSuccess(null)
            }
            catchError {
                callBack.onError(it.getCode(), it.message)
            }
        }
    }
}
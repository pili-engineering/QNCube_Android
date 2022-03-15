


## higlevel-sdk

```javascript


                              +---------------+  +---> RtmManager  信令工具
                              |               |  |
                          +---+      rtm信令   +--+---> IMAdapter  im适配器
                          |   |               |  |
                          |   +---------------+  +---> InvitationManager //邀请/请求/接受/拒绝信令
                          |
                                                  +-------------------------------------> QNRTMadapter 七牛im实现的RTM适配器
                          |
+----------------------+  |
|  higlevel-sdk        |
+----------------------+  |
                          |
                          |
                          |
                          |
                          |
                          |                                                    |--->  MutableTrackRoom 多人互动房间
                          |                        +---> MutableLiver   +
                          |   +----------------+  |                            |--->  FixedSeatRoom 固定麦位类型房间
                          |   |                |
                          +---+       rtcroom  +- -+
                              |                |
                              +----------------+  |                            |--->  FastLivingRoom 快直播 (不带连麦pk //暂没有实现
                                                   +---> SingleLiver  +
                                                                               |--->  RtcLivingHostRoom rtc单人直播 （可连麦 PK//暂没有实现




```




## Rtm 信令


### RtmMnager
  功能：1频道消息，点对点消息信令收发；2rtcroom依赖的内置信令


#### 快速接入
- 设置适配器
```
  //im适配器 七牛im
  RtmManager.setRtmAdapter(QNRTMadapter()）

  //自定义im适配器
   RtmManager.setRtmAdapter( object : RtmAdapter {
            //发点对点
            override fun sendC2cMsg( msg: String, peerId: String,isDispatchToLocal: Boolean,  callBack: RtmCallBack? ) {}
            //发群消息
            override fun sendChannelMsg(msg: String, channelId: String, isDispatchToLocal: Boolean, callBack: RtmCallBack? ) {}
            //创建群
            override fun createChannel(channelId: String, callBack: RtmCallBack?) {}
            //加群
            override fun joinChannel(channelId: String, callBack: RtmCallBack?) {}
            //退出群
            override fun leaveChannel(channelId: String, callBack: RtmCallBack?) {}
            //销毁群
            override fun releaseChannel(channelId: String, callBack: RtmCallBack?) {}
            //获取im登陆用户uid
            override fun getLoginUserId(): String {}
            /**
             * 注册监听
             * @param c2cMessageReceiver  c2c消息接收器
             * @param channelMsgReceiver 群消息接收器
             */
            override fun registerOriginImListener(
                c2cReceiver: (msg: String, peerId: String) -> Unit,
                channelReceiver: (msg: String, peerId: String) -> Unit
            ) {}
        })
```

- 注册消息监听

 ```
  //注册群消息
    RtmManager.addRtmChannelListener(object : RtmMsgListener {
        override fun onNewMsg(msg: String, channelId: String):Boolean {}
    })
     //注册点对点监听
     RtmManager.addRtmC2cListener(object : RtmMsgListener {
        override fun onNewMsg(msg: String, channelId: String):Boolean {}
    })
```

- 频道公聊/点对点消息
```

  RtmManager.rtmClient.sendChannelMsg(“我是群消息”,
            RoomManager.mCurrentRoom?.provideImGroupId(),
            true,
            object : RtmCallBack {
                override fun onSuccess() {}
                override fun onFailure(code: Int, msg: String) {}
            })

   RtmManager.rtmClient.sendChannelMsg(“我是点对点消息”,
            peerId,
            false,
            object : RtmCallBack {
                override fun onSuccess() {}
                override fun onFailure(code: Int, msg: String) {}
            })
```





#### 接口文档


# Rtm


```javascript

单列模式 rtm
object RtmManager {

    //设置适配器
    fun setRtmAdapter(adapter:RtmAdapter)
    //
    fun addRtmC2cListener(msgListener: RtmMsgListener)
    fun removeRtmC2cListener(msgListener: RtmMsgListener)
    fun addRtmChannelListener(msgListener: RtmMsgListener)
    fun removeRtmChannelListener(msgListener: RtmMsgListener)
}


interface IMAdapter{

    /**
     * 发c2c消息
     * @param isDispatchToLocal 发送成功后是否马上往本地的监听分发这个消息
     *
     */
    fun sendC2cMsg(msg: String, peerId:String, callBack: RtmCallBack?)

    /**
     * 发频道消息
     */
    fun sendChannelMsg(msg:String,channelId:String，callBack: RtmCallBack?)

    /**
     * 创建频道
     */
    fun createChannel(channelId :String,callBack:RtmCallBack?)
    /**
     * 加入频道
     */
    fun joinChannel(channelId :String,callBack:RtmCallBack?)

    /**
     * 离开频道
     */
    fun leaveChannel(channelId :String,callBack:RtmCallBack?)

    /**
     * 销毁频道
     */
    fun releaseChannel(channelId :String,callBack:RtmCallBack?)


    /**
     * 注册监听
     * @param c2cMessageReceiver  c2c消息接收器
     * @param channelMsgReceiver 群消息接收器
     */
    fun registerOriginImListener(c2cMessageReceiver:(msg: String, peerId: String)->Unit , channelMsgReceiver:(msg: String, peerId: String)->Unit)
}


/**
 * im操作回调
 */
interface RtmCallBack {
    fun onSuccess()
    fun onFailure(code:Int,msg:String)
}


//消息回调
interface RtmMsgListener {
    /**
     * 收到消息 peerId 群ID/用户ID
     return true 拦截处理，不继续分发 false继续分发
     */
    fun onNewMsg(msg: String, peerId:String):Boolean
}

```





### InvitationManager
功能：邀请/请求  ---->  接受/拒绝 信令
 a向b 发起邀请或者请求 -> b注册监听收到请求可以调用接受或者拒绝


#### 快速接入


```
      //创建一个邀请处理器
        val pkInvitationProcessor  = object : InvitationProcessor("pk",30000，object：InvitationCallBack {
            //收到邀请
            override fun onReceiveInvitation(invitation: Invitation) {
                Log.d("onReceiveInvitation",
                    "你收到到了邀请/请求" +
                            " 频道ID ${invitation.channelId} " +
                            "邀请人 ${invitation.initiatorUid}" +
                            "自定义附加消息 ${invitation.msg}"
                )

                //接受
                accept(invitation,object: RtmCallBack{
                    override fun onSuccess() {}
                    override fun onFailure(code: Int, msg: String) {}
                });

                invitation.msg="我在忙"
                //拒绝
                reject(invitation,object: RtmCallBack{
                    override fun onSuccess() {}
                    override fun onFailure(code: Int, msg: String) {}
                });

            }
           //超时
            override fun onInvitationTimeout(invitation: Invitation) {

            }
            //对方取消
            override fun onReceiveCanceled(invitation: Invitation) {

            }
            //对方接受
            override fun onInviteeAccepted(invitation: Invitation) {

            }
            //对方拒绝
            override fun onInviteeRejected(invitation: Invitation) {

            }
        }）

        //注册邀请监听
        InvitationManager.addInvitationProcessor(pkInvitationProcessor)

        //发起邀请请求
        val invitation= pkInvitationProcessor.invite(
             "刘德华邀请你PK一把", //自定义消息 也可为json数据
             peerId, //对方UID
             channelId,//所在频道ID 可为空：""
            object: RtmCallBack{
                override fun onSuccess() {}
                override fun onFailure(code: Int, msg: String) {}
            }
        );
        //取消邀请
        pkInvitationProcessor.cancel(invitation, object: RtmCallBack{
            override fun onSuccess() {}
            override fun onFailure(code: Int, msg: String) {}
        })
```

#### 最佳实践
- pk邀请
- 连麦邀请
- 呼叫


#### 接口文档


```javascript

/**
 * 邀请系统
 */
object InvitationManager {

    /**
     * 添加 某种业务邀请监听
     */
    fun addInvitationProcessor(invitationProcessor: InvitationProcessor) {
        mInvitationProcessor.add(invitationProcessor)
    }

    fun removeInvitationProcessor(invitationProcessor: InvitationProcessor) {
        mInvitationProcessor.remove(invitationProcessor)
    }
}



interface InvitationCallBack{

    // 回调 收到
    public  void onReceiveInvitation(Invitation invitation);

    /**
     * 邀请超时
     *
     * @param invitation
     */
    public  void onInvitationTimeout(Invitation invitation);

    /**
     * 对方取消
     *
     * @param invitation
     */
    public  void onReceiveCanceled(Invitation invitation);

    /**
     * 对方接受
     *
     * @param invitation
     */
    public  void onInviteeAccepted(Invitation invitation);

    /**
     * 对方拒绝
     * @param invitation
     */
    public  void onInviteeRejected(Invitation invitation);
}

//邀请处理器
public  class InvitationProcessor {

    public static String ACTION_SEND = "invite_send";
    public static String ACTION_CANCEL = "invite_cancel";
    public static String ACTION_ACCEPT = "invite_accept";
    public static String ACTION_REJECT = "invite_reject";

    /**
     * 构造方法
     * @param invitationName 邀请业务名字 如 pk邀请 /上麦邀请 ...
     * @param timeoutThreshold  邀请过期时间
     */
    public InvitationProcessor(String invitationName, long timeoutThreshold,callback: InvitationCallBack)

    /**
     * 发送邀请
     *
     * @param msg
     * @param peerId
     * @param channelId
     * @param callback
     * @return 返回当前的邀请实体
     */

    public Invitation invite(String msg, String peerId, String channelId, RtmCallBack  callback) {
          生成唯一场次ID 和 时间戳
          生成Invitation
          发送信令
    }

    //取消
    public final void cancel(Invitation invitation, RtmCallBack callback) {}

    //接收
    public void accept(Invitation invitation, RtmCallBack callback) {}

    //拒绝
    public void reject(Invitation invitation, RtmCallBack callback) {}

}

//邀请实体
class Invitation{

    int  flag ;// 邀请场次   内部维护 private set
    String msg ;//本次操作带的自定义数据。 public set/get
    long timeStamp; //时间戳 private set
    String initiatorUid;//邀请方 private set
    String receiver;// 被邀请方  private set
    String channelId ;//可为空 空代表c2c  private set
}

```


# rtc

##  公共层


```javascript
enum class ClientRoleType(role:Int) {
     /**
     * 主播角色
     * rtc房间里的表演者 有权限发流
     */
    CLIENT_ROLE_BROADCASTER(0),
    /**
     * 观众角色
     * rtc房间里听众 不表演只有权限收rtc房间轨道流
     *
     */
    CLIENT_ROLE_AUDIENCE(1),

    /**拉流角色
     * 使用 rtmp hls等直播协议拉房间里的合流的角色 不在rtc房间里
     * 需要房间合流转推后得到流
     * 拉流角色不在rtc房间里 延迟大但是费用低
     */
    CLIENT_ROLE_PULLER(-1)
}

```



```javascript

/**
 * 房间实体抽象
 */
abstract class RoomEntity {

     //是否加入成功了
    var isJoined = false

    /**
     * 房间ID
     */
    abstract fun provideRoomId():String

    /**
     * 群ID
     */
    abstract  fun provideImGroupId():String

    /**
     * 推流地址
     */
    abstract fun providePushUri():String
    abstract fun providePullUri():String

    /**
     * 房间token
     */
    abstract fun provideRoomToken():String

}
```

RoomManager


```javascript
//单列模式 房间管理分发生命周期协调各个组建
object RoomManager {

    /**
     * 当前房间信息
     */
    var mCurrentRoom: RoomEntity? = null
        private set

    // 添加房间生命周期监听
    fun addRoomLifecycleMonitor(lifecycleMonitor: RoomLifecycleMonitor) {
        mRoomLifecycleMonitors.add(lifecycleMonitor)
    }

    fun removeRoomLifecycleMonitor(lifecycleMonitor: RoomLifecycleMonitor) {
        mRoomLifecycleMonitors.remove(lifecycleMonitor)
    }
}


/**
 * 房间生命周期
 */
abstract class RoomLifecycleMonitor {
    //进入房间中
    open fun onRoomEntering(roomEntity: RoomEntity){}

    //加入了房间
    open fun onRoomJoined(roomEntity: RoomEntity){}
    // 离开房间
    open fun onRoomLeft(roomEntity: RoomEntity?){}
    //关闭房间
    open fun onRoomClosed(roomEntity: RoomEntity?){}
}

```


音视频参数
```javascript
//视频轨道参数
class VideoTrackParams {
    var width = 480
    var height = 640
    var fps = 15
    var bitrate = 3420 * 1000
    var isMaster = true

}

//音频轨道参数
class AudioTrackParams {
    var bitrate =  64 * 1000

}

//屏幕轨道参数
class ScreenTrackParams {
    var width = 720
    var height = 1080
    var fps = 15
    private var tag = "screen" //屏幕
    var bitrate = 1.5 * 1000 * 1000
    var isMaster = false
}

```


麦位
```javascript

class MicSeat() {
    //用户ID
    var uid: String = ""
    //是不是我的座位
    var isMySeat = false

}


//麦位用户扩展信息
public class UserExtension {
     String uid;
     String userExtRoleType; //用户扩展类型
     String userExtProfile; //用户扩展资料
     String userExtensionMsg; //加入房间附加的扩展字段
	 ClientRoleType clientRoleType; // 内部角色
}

//用户麦位
class UserMicSeat extend MicSeat {

    var isOwnerOpenAudio = false //麦位主人是不是打开了声音
    var isOwnerOpenVideo = false //麦位主人是不是打开了视频
    var userExtension :UserExtension?=null //麦位用户扩展字段

}

//屏幕共享麦位
class ScreenMicSeat  extend MicSeat{
     var isVideoOpen = false

}

//用户自定义麦位
class CustomMicSeat extend MicSeat{
     var tag :String  //tag标记是啥麦位
     var isAudioOpen = false //是不是打开了声音
     var isVideoOpen = false

}

```
```javascriptinterface
//操作回调
 RtcOperationCallback {
    companion object{
        const val error_seat_status = 1 //麦位状态错误
        const val error_room_not_join = 2 //房间没有加入
        const val error_room_role_no_permission = 3 //角色没有权限
    }

    fun onSuccess()

    /**
     * @param errorCode  0 - 100 highlevelsdk报错  负数 im报错 -imErrorCode ,else rtc错误码
     */
    fun onFailure(errorCode:Int,msg:String)
}

```


```javascript
/**
 * 拉流端拉流播放器
 */
interface IAudiencePlayerView  {

    /**
     * 开始播放拉流地址
     * 1角色变跟为拉流端观众
     * 2拉流角色进入房间
     */
    fun startAudiencePlay(roomEntity: com.qiniu.bzcomp.comproom.RoomEntity)

    /**
     * 停止播放拉流地址
     * 1角色变跟为主播
     * 2拉流角色房间离开销毁
     */
    fun stopAudiencePlay()

}
```

```javascript
//观众/拉流 角色进入退出房间监听。（非主播角色进入房间
interface IAudienceJoinListener{
      //观众/拉流 角色加入房间
    fun onUserJoin(userExt: UserExtension)
      //观众/拉流 角色离开 用户角色断线没有回调
      //观众/拉流 角色离开 用户角色断线暂时没有回调 v5加
    fun onUserLeave(userExt: UserExtension)
}

 ```


```javascript

//屏幕共享监听
interface   ScreenMicSeatListener{
     fun onScreenMicSeatAdd(seat: ScreenMicSeat)
     fun onScreenMicSeatRemove(seat: ScreenMicSeat)
}


interface ScreenShareManager{

    //发布屏幕共享
    fun pubLocalScreenTrack(params: ScreenTrackParams)
    //取消屏幕共享
    fun unPubLocalScreenTrack()
    //添加屏幕共享监听
    fun addScreenMicSeatListener(listener:ScreenMicSeatListener)
    fun removeScreenMicSeatListener(listener:ScreenMicSeatListener)
    //设置屏幕共享预览
    fun setUserScreenWindowView(uid:Strng,view: QNSurfaceView)
    fun getUserScreenTrackInfo(uid:String):QNTrackInfo?
}

```



```javascript

//自定义轨道监听
interface   CustomMicSeatListener{
     fun onCustomMicSeatAdd(seat: CustomMicSeat)
     fun onCustomMicSeatRemove(seat: CustomMicSeat)
}



interface VideoChanel{
   sendVideoFrame(videoData :byteArray,width : int,height:int)
}

interface CustomTrackShareManager{
   fun getUserExtraTrackInfo(tag:Strng,uid:String,):QNTrackInfo?
   //发布自定义视频视频轨道
    fun pubCustomVideoTrack(trackTag:String, params: VideoTrackParams):VideoChanel
   // 发布自定义音频轨道 暂时不支持
    fun pubCustomAudioTrack(trackTag:String, params: AudioTrackParams)
    fun unPubCustomTrack(trackTag:String)
   //添加定义轨道事件
    fun addCustomMicSeatListener(listener:CustomMicSeatListener)
    fun removeCustomMicSeatListener(listener:CustomMicSeatListener)
    //设置自定义轨道预览
    fun setUserCustomVideoPreview(trackTag:Strng,uid:String,view: QNSurfaceView)
}

```

```javascript
//混流工具
interface MixStreamMananger{
    /**
     * 启动前台转推 默认实现推本地轨道
     */
     fun startForwardJob()

    /**
     * 停止前台推流
     */
    fun stopForwardJob()
    /**
     * 开始混流转推
     */
    fun startMixStreamJob(){}
    fun stopMixStreamJob(){}

    //设置混流配置
    fun setMixParams(params:MixStreamParams)

    //主动设置某个用户的摄像头混流参数
    fun updateUserCameraMergeOptions(uid:String,option MergeTrackOption)
    //音频合流只需指定要不要
    fun updateUserAudioMergeOptions(uid:String,isNeed:Boolean)
    //跟新用户屏幕共享混流参数
    fun updateUserScreenMergeOptions(uid:String,option MergeTrackOption)
    fun updateUserCustomVideoMergeOptions(extraTrackTag:Strng,uid:String,option MergeTrackOption)
    fun updateUserCustomAduioMergeOptions(extraTrackTag:Strng,uid:String,isNeed:Boolean)
}


class MixStreamParams{

 val mixStreamWidth: Int,
 val mixStringHeiht: Int,
 val mixStreamY: Int,
 val mixBitrate: Int,
 val fps: Int,
 val qnBackGround: QNBackGround?
}


class MergeTrackOption{
     var mX = 0
     var mY = 0
     var mZ = 0
     var mWidth = 0
     var mHeight = 0
     var mStretchMode: QNStretchMode? = null
}

```


子类房间 MutableTrackRoom



加入退出逻辑：


### MutableTrackRoom
 定义：陆陆续续有主播或者观众角色加入房间，UI层关心有人上麦有人下麦，麦上有音频/视频快关
 #### 本组件的优点：
  - 支持设置用户和主播角色，主播和用户角色切换自动切换拉流和订阅模式
  - 主播和用户角色都能走同一套麦位监听（有人上麦/下麦/麦上音视频状态变化）/用户角色进入离开监听/屏幕共享监听/定义轨道监听
  - 麦位上扩展自定义用户资料/扩展角色/ 麦位状态
  - 友好的api随时随地开启音视频模块，随时随地绑定用户预览窗口屏蔽track概念屏蔽track生命周期流程


![alt 属性文本](http://qrnlrydxa.hn-bkt.clouddn.com/mroom.png)

#### 快速接入

#### 最佳实践

#### 接口

```javascript

class MutableUserSeat extend UserSeat{
    var isMuteVideoByMe = false // 视频是不是被我屏蔽了
    var isMuteAudioByMe = false //音频是不是被我屏蔽了
}

//麦位监听
interface TrackSeatListener {

    //主播上麦 参数用户麦位
    fun onUserSitDown(micSeat: MutableUserSeat)
    //主播下麦
     //isOffLine 是不是断线
    fun onUserSitUp(micSeat:UserMicSeat,isOffLine:Boolean)
    //麦上视频状态变化
    fun onCameraStatusChanged(micSeat: MutableUserSeat)
    fun onMicAudioStatusChanged(micSeat: MutableUserSeat)

}


class MutableTrackRoom {

    /**
     * 维护座位
     * 摄像头和扬声器 做位一个座位  / 屏幕采集座为一个座位
     */
    val mMicSeats = ArrayList<MutableUserSeat>()

    //用户角色同步麦位   1用户角色使用信令监听可能出错，当前版本需业务方自己往业务服务器同步，后期sdk内部搞定 2用户角色初始化当前麦位信息，如果用户角色ui初始化需要显示当前所有麦状态
    fun userClientTypeSyncMicSeats(mMicSeats ： List<MutableUserSeat>)
     //添加麦位监听
    fun addMicSeatListener(MicSeatListener: MicSeatListener)
    fun removeMicSeatListener(MicSeatListener: MicSeatListener)


  //设置角色 CLIENT_ROLE_BROADCASTER / CLIENT_ROLE_AUDIENCE  支持角色切换
  //默认主播 -> 订阅播放
  //用户角色 -> 拉流播放 能监听麦位 不能操作麦位
    fun setClientRole(role:Int, callBack:RtcOperationCallback)
    //开启本地视频模块
    fun enableVideo(params: VideoTrackParams?)
    fun enableAudio(params: AudioTrackParams?)
    //禁用本地视频
    fun disableVideo()
    fun disableAudio()


    // 根据当前设置的角色是否加入rtc房间
     //加入房间
    fun joinRoom(
        roomEntity: RoomEntity,
        userExt: UserExtension?,
        callBack: RtcOperationCallback
    )

    fun leaveRoom(callBack: RtcOperationCallback)

    fun closeRoom()



    //公共方法


    //暴露出Engine(如果接入方要用到其他没有封装完全的方法)
     fun getRtcEngine():QNRTCEngine

     //设置观众模式的拉流播放器，sdk根据当前角色模式切换订阅播放/拉流播放
     fun setAudiencePlayerView(playerView:IAudiencePlayerView)
     //设置本地预览窗口 （需要开启视频模块）
     fun setlocalCameraWindowView(view: QNSurfaceView)
     //切换摄像头
     fun switchCamera()
     //关闭本地摄像头推流
     fun muteLocalVideo(muted:Boolean)
     fun muteLocalAudio(muted:Boolean)

     //屏蔽/不屏蔽 远端某个人视频
     fun muteRemoteVideo(uid:String,muted:Boolean)
     fun muteRemoteAudio(uid:String,muted:Boolean)
     //屏蔽/不屏蔽 远端所有音频
     fun muteAllRemoteAudio(muted:Boolean)
     fun muteAllRemoteVideo(muted:Boolean)

    //添加rtc事件监听
    fun addExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener: QNRTCEngineEventListener)
    fun removeExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener: QNRTCEngineEventListener)

    fun setDefaultBeauty(beautySetting: QNBeautySetting)

    //设置某人的摄像头预览窗口 可以在任何时候调用
    fun setUserMicSeatVideoPreviewView(uid:Strng,view: QNSurfaceView)

     // 获取某人的视频轨道 如果需要用到track做额外的操作
    fun getUserVideoTrackInfo(uid:String):QNTrackInfo?
    fun getUserAudioTrackInfo(uid:String):QNTrackInfo?

        //用户角色进入退出监听
    fun addIAudienceJoinListener( listener : IAudienceJoinListener)
    fun removeIAudienceJoinListener( listener : IAudienceJoinListener)

    //获取混流器工具
    fun getMixStreamManager():MixStreamManager
   //获的屏幕共享工具
    fun getScreenShareManager():ScreenShareManager
   //获取自定义推流工具
    fun getCustomTrackManager():CustomTrackShareManager
}


```

子类房间 FixedSeatRoom




![alt 属性文本](http://qrnlrydxa.hn-bkt.clouddn.com/fm-2.png)

### LazySitMutableLiverRoom

定义： 加入房间后作为观众身份。点击上麦成为主播 。UI层关心有人上麦 有人下麦，麦上有音频/视频快关
 #### 本组件的优点：
  - 主播和用户角色切换自动切换拉流和订阅模式
  - 主播和用户角色都能走同一套麦位监听（有人上麦/下麦/麦上音视频状态变化）/用户角色进入离开监听/屏幕共享监听/定义轨道监听
  - 麦位上扩展自定义用户资料/扩展角色/ 麦位状态。
  - 封装了常用麦位操作 屏蔽某人，管理员禁麦，锁麦
  - 友好的api随时随地开启音视频模块，随时随地绑定用户预览窗口屏蔽track概念屏蔽track生命周期流程




#### 最佳实践

- 语聊房
- 互动直播

#### 接口文档
```javascript

class LazysitUserMicSeat extend UserMicSeat {

    var isMuteVideoByMe = false // 视频是不是被我屏蔽了
    var isMuteAudioByMe = false //音频是不是被我屏蔽了
    var isForbiddenAudioByManager = false //管理员关麦克风
    var isForbiddenVideoByManager = false //管理员关摄像头

}

class UserMicSeatListener{

    /**
     * 有人上麦
     * @param seat
     */
    fun onUserSitDown(seat: LazysitUserMicSeat);

    /**
     * 有人下麦
     * @param seat
     * @param isOffLine 是不是断线 否则主动下麦
     */
    fun onUserSitUp(seat: LazysitUserMicSeat, isOffLine: Boolean);

    /**
     * 麦位麦克风变化
     * @param seat
     */
    fun onMicAudioStatusChanged(seat: LazysitUserMicSeat);

    /**
     * 麦位摄像头变化
     * @param seat
     */
    fun onCameraStatusChanged(seat: LazysitUserMicSeat);

    //麦位被管理员禁麦变化
    fun onVideoForbiddenStatusChanged(seat: LazysitUserMicSeat, msg: String);
    fun onAudioForbiddenStatusChanged(seat: LazysitUserMicSeat, msg: String);

    //  从麦位上踢出
    fun onKickOutFromMicSeat(seat: LazysitUserMicSeat, msg: String)
    // 从房间踢出
    fun onKickOutFromRoom(userId: String, msg: String)
    //自定义麦位信令
    fun onCustomSeatAction(seat: MicSeat, key: String, values: String)

}



class LazySitMutableLiverRoom {


    //用户角色同步麦位 用户角色使用信令监听可能出错，当前版本需业务方自己往业务服务器同步，后期sdk内部搞定
    fun userClientTypeSyncMicSeats(mMicSeats : List<LazysitUserMicSeat>)
    //添加麦位监听
    fun addUserMicSeatListener(listener: UserMicSeatListener)

    fun removeUserMicSeatListener(listener: UserMicSeatListener)


    fun kickOutFromMicSeat(uid: String,msg:String, callBack: RtcOperationCallback)

    fun kickOutFromRoom(uid:String,msg:String, callBack: RtcOperationCallback)
     //禁止开麦克风
    fun forbiddenMicSeatAudio(uid: String,isForbidden:Boolean,msg:String, callBack: RtcOperationCallback)

    //禁止开摄像头
    fun forbiddenMicSeatVideo(uid: String,isForbidden:Boolean,msg:String, callBack: RtcOperationCallback)

    /**
     * 自定义麦位操作
     * @param seat
     * @param action
     */
    fun sendCustomSeatAction(uid: String, key: String, values: String,callBack: RtcOperationCallback)


     //加入房间之前都是用户 选择麦位上麦后才切换成主播
      //加入房间
    fun joinRoom(
        roomEntity: RoomEntity,
        userExt: UserExtension?,
        callBack: RtcOperationCallback
    )


    /**
     * 上麦 指定麦位上麦 sdk内部加入rtc房间切换播放为rtc订阅
     * @param cameraParams 如果需要开启摄像头 否则null
     * @param micphoneParams 如果需要开启麦克风 否则null
     */
    fun sitDown( userExt:UserExtension, cameraParams: VideoTrackParams?, micphoneParams: AudioTrackParams?, callBack: RtcOperationCallback)

    /**
     * 下麦 sdk内部退出rtc房间 切换订阅播放到拉流播放
     * @param seat
     */
    fun sitUp(callBack:RtcOperationCallback)

    fun leaveRoom(callBack: RtcOperationCallback)

    fun closeRoom()


     //公共方法

    //暴露出Engine(如果接入方要用到其他没有封装完全的方法)
     fun getRtcEngine():QNRTCEngine

     //设置观众模式的拉流播放器，sdk根据当前角色模式切换订阅播放/拉流播放
     fun setAudiencePlayerView(playerView:IAudiencePlayerView)
     //设置本地预览窗口 （需要开启视频模块）
     fun setlocalCameraWindowView(view: QNSurfaceView)
     //切换摄像头
     fun switchCamera()
     //关闭本地摄像头推流
     fun muteLocalVideo(muted:Boolean)
     fun muteLocalAudio(muted:Boolean)

     //屏蔽/不屏蔽 远端某个人视频
     fun muteRemoteVideo(uid:String,muted:Boolean)
     fun muteRemoteAudio(uid:String,muted:Boolean)
     //屏蔽/不屏蔽 远端所有音频
     fun muteAllRemoteAudio(muted:Boolean)
     fun muteAllRemoteVideo(muted:Boolean)

    //添加rtc事件监听
    fun addExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener: QNRTCEngineEventListener)
    fun removeExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener: QNRTCEngineEventListener)

    fun setDefaultBeauty(beautySetting: QNBeautySetting)

    //设置某人的摄像头预览窗口 可以在任何时候调用
    fun setUserMicSeatVideoPreviewView(uid:Strng,view: QNSurfaceView)

     // 获取某人的视频轨道 如果需要用到track做额外的操作
    fun getUserVideoTrackInfo(uid:String):QNTrackInfo?
    fun getUserAudioTrackInfo(uid:String):QNTrackInfo?

        //用户角色进入退出监听
    fun addIAudienceJoinListener( listener : IAudienceJoinListener)
    fun removeIAudienceJoinListener( listener : IAudienceJoinListener)

    //获取混流器实现
    fun getMixStreamManager():MixStreamManager
   //获的屏幕共享实现
    fun getScreenShareManager():ScreenShareManager
   //获取自定义轨道工具
    fun getCustomTrackManager():CustomTrackShareManager
}

```










### LivingRoom

```javascript


//连麦监听

interface LinkMicSeatListener{
    /**
     * 有连麦用户加入
     * @param seat
     */
    fun onMicLinkerSitDown( seat: UserMicSeat);

    /**
     * 有连麦用户下麦
     * @param seat
     */
    fun onMicLinkerSitUp(seat:UserMicSeat,isOffLine:Boolean);

    /**
     * 麦位麦克风变化
     * @param seat
     * @param isOpen
     */
    fun onMicAudioStatusChange(seat:UserMicSeat);

    /**
     * 麦位摄像头变化
     * @param seat
     * @param isOpen
     */
    fun onCameraStatusChange( seat:UserMicSeat);

    /**
     * 连麦用户被踢
     */
    fun onKickOutFromMicSeat( seat:UserMicSeat)

}



//连麦管理
class LinkMicManager(){

      //用户角色初次进入房间 需要业务服务器同步一下当前房间正在连麦的人
    fun userClientTypeSyncMicSeats(mMicSeats ： List<UserMicSeat>)

    var linkers = List<UserMicSeat>()
    setLinkMicSeatListener(listener:LinkMicSeatListener)
    //踢麦
    fun kickOutfromMicSeat(seat:UserMicSeat,msg:String,callBack: RtcOperationCallback)

    /**
     * 上麦
     *
     */
    fun startLinkerMic( userExt:UserExtension,videoTrackParams: VideoTrackParams?,audioTrackParams: AudioTrackParams?,callBack: RtcOperationCallback)

    /**
     * 下麦
     * @param seat
     */
    fun stopLinkerMic(callBack:RtcOperationCallback)


}

calss PkSession{

     var initiatorLiver: UserMicSeat
     var receiverLiver: UserMicSeat
     //pk场次
     var pkSessionId:String
     //pk扩展字段
     var pkExtension:String;
}

interface PkListener{

     //pk开启
     fun onPKStart(pkSession：PkSession)
     fun onError(code:Int ,msg:String)
     fun onPKStop(code:Int,msg:String )
     fun onPkEvent(eventKey:String,value:String)
}

class PkManger{

     //用户角色初次进入房间 需要业务服务器同步一下当前房间pk状态.
    fun userClientTypeSyncPkStatus(pkSession: PkSession)


    fun setPkListener(listener: PkListener)
    var currentPkInfo: PkSession

     //pk开启  v4参数暂定
    fun startPk(
        pkSessionId: String,
        receiverUid: String,
        receiverRoomToken:String,
        pkExtension: String,
        callBack: RtcOperationCallback
    )

    fun stopPk(code: Int, msg: String, callBack: RtcOperationCallback)

    //pk场次中 a房间群和b主播房间群都要收到的事件 如两边粉丝刷礼物的数量
    fun sendPkEvent(eventKey: String, value: String, callBack: RtcOperationCallback)
   }





//主播麦位
interface LiverSeatListener {
    /**
     * 主播上麦
     * @param seat
     */
    fun onLiverSitDown(seat: UserMicSeat);

    /**
     * 主播下麦
     * @param seat
     */

    fun onLiverSitUp(seat: UserMicSeat, isOffLine: Boolean);

    /**
     * 麦位麦克风变化
     * @param seat
     * @param isOpen
     */
    fun onLiverAudioStatusChange(seat: UserMicSeat);

    /**
     * 麦位摄像头变化
     * @param seat
     * @param isOpen
     */
    fun onLiverCameraStatusChange(seat: UserMicSeat);


    /**
     * 用户踢出房间
     *
     */
    fun onKickOutFromRoom(userId: String,msg:String)

}
----------------------------------------------------------------------------------------------------------------------



//快直播房间
class FastLivingRoom {



  // 快直播sdk实现方法
     // 公共方法
   /设置角色 CLIENT_ROLE_BROADCASTER / CLIENT_ROLE_AUDIENCE  后期支持角色切换
  //默认主播 -> 订阅播放
  //用户角色 -> 拉流播放 能监听麦
    fun setClientRole(role:Int, callBack:RtcOperationCallback)

    //用户角色进入退出监听
    fun addIAudienceJoinListener( listener : IAudienceJoinListener)
    fun removeIAudienceJoinListener( listener : IAudienceJoinListener)

    //直播麦位监听
    fun addLiverSeatListener(liverSeatListener: LiverSeatListener)
    fun removeLiverSeatListener(liverSeatListener: LiverSeatListener)
    // 根据当前设置的角色是否加入rtc房间
     //加入房间
    fun joinRoom(
        roomEntity: RoomEntity,
        userExt: UserExtension?,
        callBack: RtcOperationCallback
    )

    fun leaveRoom(callBack: RtcOperationCallback)

    fun closeRoom()
    fun kickOutFromRoom(userId: String, msg: String, callBack: RtcOperationCallback)


    //主播方法

    fun setlocalCameraWindowView(view: QNSurfaceView)
     //切换摄像头
    fun switchCamera()
     //关闭本地摄像头推流
    fun muteLocalVideo(muted:Boolean)
    fun muteLocalAudio(muted:Boolean)
    fun enableVideo(params: VideoTrackParams?)
    fun enableAudio(params: AudioTrackParams?)
    fun disableVideo()
    fun disableAudio()

    //观众方法
     //设置观众模式的拉流播放器，sdk根据当前角色模式切换订阅播放/拉流播放
    fun setAudiencePlayerView(playerView:IAudiencePlayerView)
}




//rtc房间
class RtcLivingRoom {


     //暴露出Engine(如果接入方要用到其他没有封装完全的方法)
    fun getRtcEngine():QNRTCEngine

    fun getPkManager():PkManager()

    fun  getLinkMicManager(): LinkMicManager()

     //获取混流器实现
    fun getMixStreamHelper():MixStreamHelper


  // 公共方法


  /设置角色 CLIENT_ROLE_BROADCASTER / CLIENT_ROLE_AUDIENCE  后期支持角色切换
  //默认主播 -> 订阅播放
  //用户角色 -> 拉流播放 能监听麦
    fun setClientRole(role:Int, callBack:RtcOperationCallback)

    //用户角色进入退出监听
    fun addIAudienceJoinListener( listener : IAudienceJoinListener)
    fun removeIAudienceJoinListener( listener : IAudienceJoinListener)

    //直播麦位监听
    fun addLiverSeatListener(liverSeatListener: LiverSeatListener)
    fun removeLiverSeatListener(liverSeatListener: LiverSeatListener)
    // 根据当前设置的角色是否加入rtc房间
     //加入房间
    fun joinRoom(
        roomEntity: RoomEntity,
        userExt: UserExtension?,
        callBack: RtcOperationCallback
    )

    fun leaveRoom(callBack: RtcOperationCallback)

    fun closeRoom()
    fun kickOutFromRoom(userId: String, msg: String, callBack: RtcOperationCallback) {



    //主播方法

    fun setlocalCameraWindowView(view: QNSurfaceView)
     //切换摄像头
    fun switchCamera()
     //关闭本地摄像头推流
    fun muteLocalVideo(muted:Boolean)
    fun muteLocalAudio(muted:Boolean)
    fun enableVideo(params: VideoTrackParams?)
    fun enableAudio(params: AudioTrackParams?)
    fun disableVideo()
    fun disableAudio()

    //观众方法
     //设置观众模式的拉流播放器，sdk根据当前角色模式切换订阅播放/拉流播放
    fun setAudiencePlayerView(playerView:IAudiencePlayerView)
}

```



# highlevel sdk层

action ,data
## 内部信令

### 通用

```

用户收到主播屏幕共享
{"action": "rtc_pubScreen","data":{ScreenMicSeat实体  }}
{"action": "rtc_unPubScreen","data":{ScreenMicSeat实体}}

发布定义轨道
{"action": "rtc_pubCustomTrack","data":{CustomMicSeat实体}}
{"action": "rtc_unPubCustomTrack","data":{CustomMicSeat实体}}

用户收到主播开关麦
{"action": "rtc_microphoneStatus","data":{UserMicSeat实体}}
{"action": "rtc_cameraStatus","data":{UserMicSeat实体}}

踢人
{"action": "rtc_kickOutFromMicSeat","data":{seat:UserMicSeat实体,"msg":"违规操作XXX"}}
{"action": "rtc_kickOutFromRoom","data":{"uid":"12"，"msg":""}}


主播上下麦
{"action": "rtc_sitDown","data":{ UserMicSeat实体 }}
{"action": "rtc_sitUp","data":{ UserMicSeat实体 }}//


用户角色进入退出
{"action": "rtc_userJoin","data":{ UserExtension实体 }}
{"action": "rtc_userLeft","data":{ UserExtension实体 }}//

```

### 子类房间

#### 可变多麦位




#### 固定麦位
```
锁麦
{"action": "rtc_lockSeat","data":{ micSeat:UserMicSeat实体,"msg":"XXX" }}




//禁麦实体
class ForbiddenMicSeatMsg(
    var uid: String = "",
    var isForbidden: Boolean,
    var msg: String = ""
)
禁麦
{"action": "rtc_forbiddenAudio","data":{ forbiddenMicSeatMsg实体}
{"action": "rtc_forbiddenVideo","data":{ forbiddenMicSeatMsg实体 }



自定义
{"action": "rtc_customSeatAction","data":{“uid”:"","key":"asda","value":"xx" }}

```
####  单人直播
```
连麦者上麦
{"action": "rtc_linker_sitDown","data":{ UserMicSeat实体 }}
{"action": "rtc_linker_sitUp","data":{ UserMicSeat实体 }}
连麦踢人
{"action": "rtc_linker_kickOutFromMicSeat","data":{micSeat:UserMicSeat实体,"msg":"XXX" }}

pk
{"action": "rtc_onPKStart","data":{ PkSession实体 }}
{"action": "rtc_onError","data":{ "code":1,"msg":"ss" }}
{"action": "rtc_onPKStop","data":{ "code":1,"msg":"ss" }}
{"action": "rtc_onPkEvent","data":{ "eventKey":1,"value":"ss" }}


## 邀请信令
{"action": "invite_send","data":{ "invitationName":"pk" , "invitation":Invitation 邀请实体 }}
{"action": "invite_cancel","data":{ "invitationName":"pk" , "invitation":Invitation 邀请实体 }}
{"action": "invite_accept","data":{ "invitationName":"pk" , "invitation":Invitation 邀请实体 }}
{"action": "invite_reject","data":{ "invitationName":"pk" , "invitation":Invitation 邀请实体 }}
```

# demo 层：
公聊
```
{"action": "pub_chat_text","data":{"senderId":"","senderName":"" ,"msgContent":""}}

进入/退出
{"action": "welcome","data":{"senderId":"","senderName":"" ,"msgContent":"加入了直播间"}}
{"action": "quit_room","data":{"senderId":"","senderName":"" ,"msgContent":"离开了直播间"}}


弹幕
{"action": "living_danmu","data":{弹幕实体}}


 class DanmuEntity  {
     String content;
     String senderName;
     String senderUid;
     String senderRoomId;
     String senderAvatar;
}



礼物
{"action": "living_gift","data":{礼物实体}}

 class GiftMsg {
     String senderName;
     String senderUid;
     String senderRoomId;
     String senderAvatar;
     Gift sendGift;
     int number =0;

    class Gift {
     String giftName;
     String giftId;
     int giftRes;
    }
}


直播间点赞
{"action": "living_heart","data":{直播间点赞实体}}

 class HearMsg {
     int count;  //点赞数量
     String senderName;
     String senderUid;
     String senderRoomId;
}





```




local Role = require("Asset.Scripts.extend.BaseballAnimationRole")
local configure = require("Asset.Scripts.configure")
require("Asset.Scripts.I18N")
local BaseballScene =
    xe:Class(
    xe.Scene,
    {
        tag = "BaseballScene"
    }
)

function BaseballScene:GameStart()
    print("iiiiiiiiiiiiiii")
    -- local l_SuitList = {
    --     [1] = {name = I18N:Str("touch"), icon = "Tap the screen to swing a baseball", config = '{"color_s":78,"color_v":100,"tail_height":1.6,"color_h":176,"head_scale":1,"tail_fire_num":3,"text_vertical":1,"name":"Rocket02","tail_scale":1,"head_height":5.5}'},
    -- }
    local labelTouch = self.ScroeUIScene:GetChildByNameRecrusive("GuideHit_Tips_Label")
    dump(labelTouch,"kkkkkkkkkkk")
    labelTouch:SetString(I18N:Str("touch"))
    local labelfinal = self.ScroeUIScene:GetChildByNameRecrusive("GameOver_Words_Label")
    labelfinal:SetString(I18N:Str("Score"))
    -- local GameOverBtnNode = self.ScroeUIScene:GetChildByNameRecrusive("Btn_Image")
end

function BaseballScene:GetUIPosition(uiNode, pos3D)
    local viewport = _G_EngineInstance:GetCurViewPort()
    local screen_pos = XEUtility.TransformWorld2Screen(viewport, pos3D)
    local mat = uiNode:GetParent():GetLocalToScreenTransform()
    mat:Inverse()
    local ui_pos = mat * screen_pos

    ui_pos = XVECTOR2(ui_pos.x, ui_pos.y)
    return ui_pos
end

function BaseballScene:onStart()
    math.randomseed(os.time())
    --场景资源
    local Xscene01 = "Asset/daqie.xscene"

    --读取场景
    local world = self:GetWorld()
    world:LoadScene(Xscene01, false)
    self.world = world
    --游戏最长时间
    self.GameOverTime = 10
    --开场延迟时间onTick计时变量
    self.Delay = 0
    --第一次飞行时间onTick计时变量
    self.Time = 0
    --第二次飞行时间onTick计时变量
    self.Time2 = 0
    --击中慢放计时器
    self.CrrSlowTime = 0
    --飞行角色Actor数据，用于判定延时逻辑是否继续
    self.FlyRole = nil
    --开场延迟总时长配置
    self.Delaydt = 2
    --棒球下落总时长配置
    self.dt = configure.DownDuration
    self.DownOriginVelocity = configure.DownOriginVelocity

    --棒球飞出后飞行总是长配置
    self.dt2 = 0.75
    --棒球飞行结束到落地总是长配置
    self.dt3 = 0.5

    --落地翻滚时间总时长
    self.dt9 = 2
    --游戏时间
    self.GameTime = 0
    --是否击中的判断值
    self.IsHit = false
    --游戏运行阶段（0:延迟下落阶段、1:下落阶段、2:飞出阶段、3:落地弹跳阶段、4：击中后缓动、9:落地滑动阶段）
    self.GameState = 0
    --游戏当前运行阶段（0:延迟下落阶段、1:下落阶段、2:飞出阶段、3:落地弹跳阶段、4：击中后缓动、 9:落地滑动阶段）
    self.NowGameState = 0
    --FlyRole 的初始高度
    self.FlyRoleHight = 126.7756
    --飞行加速度，上升下降
    self.FlyOriginVelocity = XVECTOR3(0, 0, 0)
    --FlyRole的随机飞行高度
    local RandomFlyHigh = math.random(50, 100)
    self.RandomFlyHigh = RandomFlyHigh

    --默认位置设置
    self.RoleFlyInitialPosition = XVECTOR3(0, 126.8756, -42.1566)

    --高度值默认设置
    local FlyRoleHitHigh = nil
    self.FlyRoleHitHigh = FlyRoleHitHigh

    --球飞行距离
    self.FlyScore = 0
    --下落地面，向前距离 GameState = 3
    self.bounce = 0
    --在地面向前滑动距离，GameState = 9
    self.slide = 50

    --未击中的最终得分
    self.Score = self.slide + self.bounce

    --是否开始游戏
    self.StartGame = false

    local Actor = world:FindActor("front_camera")
    local Camera = Actor
    self.Camera = Camera
    self.CameraPos = self.Camera:GetActorLocation()
   
    local Actor = world:FindActor("Baseball_Actor")
    self.FlyRoleActor = Actor
    print(self.FlyRoleActor:GetActorName())
    self.FlyRolePos = self.FlyRoleActor:GetActorLocation()
    self.CameraOffset = self.CameraPos - self.FlyRolePos + XVECTOR3(10, 0, 0)

    -- --动画素材
    -- local Ani_idle = "Asset/Public/model/Astronaut04.seq"
    -- self.Ani_idle = Ani_idle
    -- self.Ani_idle = "Asset/Public/model/Astronaut04.seq"
    -- self.Ani_Hit01 = "Asset/Public/model/Astronaut05_hit_up.montage"
    -- self.Ani_Hit02 = "Asset/Public/model/Astronaut_hit.montage"
    -- self.Ani_HitMiss = "Asset/Public/model/Astronaut_miss.montage"

    --动画素材
    local Ani_idle = "Asset/Public/BaseBallPlayer/BaseBallPlayer_Idle.seq"
    self.Ani_idle = Ani_idle
    self.Ani_Hit01 = "Asset/Public/BaseBallPlayer/BaseBallPlayer_Hit01.montage"
    self.Ani_Hit02 = "Asset/Public/BaseBallPlayer/BaseBallPlayer_Hit01.montage"
    self.Ani_HitMiss = "Asset/Public/BaseBallPlayer/BaseBallPlayer_Hit01_miss.montage"

    local HitRoleActorName = "juese_2"
    local HitRoleAniState = nil
    self.HitRoleActorName = HitRoleActorName

    BaseballScene:PlayAnimation(self.world, HitRoleActorName, Ani_idle, true, 1)
    HitRoleAniState = Ani_idle

    --找到UIX加载UIscene
    local Actor = world:FindActor("Btn_1")
    local TouchUIScene = Actor:GetScene()
    self.TouchUIScene = TouchUIScene

    --找到UIX加载UIscene
    local Actor = world:FindActor("EndScroe_1")
    self.ScoreUIActor = Actor
    local ScroeUIScene = Actor:GetScene()
    self.ScroeUIScene = ScroeUIScene
    self:GameStart()
    BaseballScene:PlayAnimation(
        self.world,
        "Baseball_Actor",
        "Asset/Public/Baseball/Baseball_OP.seq",
        false,
        1,
        OpAniCallBack
    )

    --游戏结束界面，再来一局，按钮点击重置游戏数据
    local GameOverBtnNode = self.ScroeUIScene:GetChildByNameRecrusive("Btn_Image")
    GameOverBtnNode:AddTouchEventListener(
        function(node, type)
            print("-----------------------------------再来一局 重启游戏-----------------------------------")

            xe.Scheduler:scheduleOnce(
                function()
                    xe.Director:GetInstance():PopScene()

                    local scene = BaseballScene:new("com.wemomo.engine")
                    xe.Director:GetInstance():PushScene(scene)
                end,
                xe.Scheduler.INTERVAL_NEXT_TICK
            )
        end
    )

    --UI控件监听事件
    local BtnNode = TouchUIScene:GetChildByNameRecrusive("TouchImage")
    BtnNode:AddTouchEventListener(
        function(node, type)
            if XUIWidget.XUI_BEGAN == type then
                local YPosHigh_0 = configure.YPosHigh_0
                local YPosLow_0 = configure.YPosLow_0
                -- local YPosHigh = 10
                local VarA_0 = configure.VarA_0
                local VarB_0 = configure.VarB_0
                local bounce_0 = configure.bounce_0
                local slide_0 = configure.slide_0
                local FlyDuration_0 = configure.FlyDuration_0
                local FlyOriginVelocity_0 = configure.FlyOriginVelocity_0

                local YPosHigh_1 = configure.YPosHigh_1
                local YPosLow_1 = configure.YPosLow_1
                local VarA_1 = configure.VarA_1
                local VarB_1 = configure.VarB_1
                local bounce_1 = configure.bounce_1
                local slide_1 = configure.slide_1
                local FlyDuration_1 = configure.FlyDuration_1
                local FlyOriginVelocity_1 = configure.FlyOriginVelocity_1

                local YPosHigh_2 = configure.YPosHigh_2
                local YPosLow_2 = configure.YPosLow_2
                local VarA_2 = configure.VarA_2
                local VarB_2 = configure.VarB_2
                local bounce_2 = configure.bounce_2
                local slide_2 = configure.slide_2
                local FlyDuration_2 = configure.FlyDuration_2
                local FlyOriginVelocity_2 = configure.FlyOriginVelocity_2

                if YPosHigh_0 <= self.FlyRolePos.y and self.FlyRolePos.y < YPosLow_0 then
                    self.IsHit = true
                    self.HitType01 = 1

                    --随机飞行距离
                    local VarA = 500
                    local VarB = 1200
                    local VarN = 1
                    if VarA_0 >= VarB_0 then
                        VarN = VarB_0
                    else
                        VarN = VarA_0
                    end
                    self.FlyScore = math.random(VarA_0, VarB_0) * _G_GameConfig.coefficient
                    local FlyScoreScaleValue = self.FlyScore / VarN

                    self.dt2 = FlyDuration_0
                    self.FlyOriginVelocity = FlyOriginVelocity_0

                    self.bounce = bounce_0
                    self.slide = slide_0

                    print("飞行距离 = " .. self.FlyScore)
                    print("弹跳距离 = " .. bounce_0)
                    print("滑动距离 = " .. slide_0)
                    self.FlyRoleHitHigh = self.FlyRolePos

                    BtnNode = self.TouchUIScene:GetChildByNameRecrusive("TouchImage")
                    BtnNode:SetVisible(false)

                    BtnNode = self.ScroeUIScene:GetChildByNameRecrusive("GuideHitWidget")
                    BtnNode:SetVisible(false)

                    -- --击中后播放特效
                    -- local FXNode = self.ScroeUIScene:GetChildByNameRecrusive("UIFX_High")
                    -- FXNode:FxPlay()

                    self.Highttest = 2
                    -- --播放过场动画
                    -- local SceneSequenceActor = self.world:FindActor("Effect_1")
                    -- local SceneSequenceComponent = SceneSequenceActor:GetRootComponent()
                    -- SceneSequenceComponent:GetSequenceInstance():GetSeqAnimController():Play()

                    local Anicallback = function()
                        BaseballScene:PlayAnimation(self.world, self.HitRoleActorName, self.Ani_idle, true, 1)
                    end
                    BaseballScene:PlayAnimation(
                        self.world,
                        self.HitRoleActorName,
                        self.Ani_Hit02,
                        false,
                        0.05,
                        Anicallback
                    )

                    self.GameState = 4
                elseif YPosHigh_1 <= self.FlyRolePos.y and self.FlyRolePos.y < YPosLow_1 then
                    self.IsHit = true
                    self.HitType01 = 1

                    local VarN_1 = 1
                    if VarA_1 >= VarB_1 then
                        VarN_1 = VarB_1
                    else
                        VarN_1 = VarA_1
                    end
                    self.FlyScore = math.random(VarA_1, VarB_1) * _G_GameConfig.coefficient
                    local FlyScoreScaleValue = self.FlyScore / VarN_1

                    self.dt2 = FlyDuration_1
                    self.FlyOriginVelocity = FlyOriginVelocity_1

                    self.bounce = bounce_1
                    self.slide = bounce_1

                    print("飞行距离 = " .. self.FlyScore)
                    print("弹跳距离 = " .. bounce_1)
                    print("滑动距离 = " .. slide_1)

                    self.FlyRoleHitHigh = self.FlyRolePos

                    BtnNode = self.TouchUIScene:GetChildByNameRecrusive("TouchImage")
                    BtnNode:SetVisible(false)

                    BtnNode = self.ScroeUIScene:GetChildByNameRecrusive("GuideHitWidget")
                    BtnNode:SetVisible(false)

                    -- --击中后播放特效
                    -- local FXNode = self.ScroeUIScene:GetChildByNameRecrusive("UIFX_Mid")
                    -- FXNode:FxPlay()

                    self.Highttest = 1
                    -- --播放过场动画
                    -- local SceneSequenceActor = self.world:FindActor("Effect_huangse_1_1")
                    -- local SceneSequenceComponent = SceneSequenceActor:GetRootComponent()
                    -- SceneSequenceComponent:GetSequenceInstance():GetSeqAnimController():Play()

                    local Anicallback = function()
                        BaseballScene:PlayAnimation(self.world, self.HitRoleActorName, self.Ani_idle, true, 1)
                    end
                    BaseballScene:PlayAnimation(
                        self.world,
                        self.HitRoleActorName,
                        self.Ani_Hit02,
                        false,
                        0.05,
                        Anicallback
                    )
                    self.GameState = 4
                elseif YPosHigh_2 <= self.FlyRolePos.y and self.FlyRolePos.y < YPosLow_2 then
                    self.IsHit = true
                    self.HitType01 = 1

                    local VarN_2 = 1
                    if VarA_2 >= VarB_2 then
                        VarN_2 = VarB_2
                    else
                        VarN_2 = VarA_2
                    end
                    self.FlyScore = math.random(VarA_2, VarB_2) * _G_GameConfig.coefficient
                    local FlyScoreScaleValue = self.FlyScore / VarN_2

                    self.dt2 = FlyDuration_2
                    self.FlyOriginVelocity = FlyOriginVelocity_1

                    self.bounce = bounce_2
                    self.slide = slide_2

                    print("飞行距离 = " .. self.FlyScore)
                    print("弹跳距离 = " .. bounce_2)
                    print("滑动距离 = " .. slide_2)
                    self.FlyRoleHitHigh = self.FlyRolePos.y

                    BtnNode = self.TouchUIScene:GetChildByNameRecrusive("TouchImage")
                    BtnNode:SetVisible(false)

                    BtnNode = self.ScroeUIScene:GetChildByNameRecrusive("GuideHitWidget")
                    BtnNode:SetVisible(false)

                    local Anicallback = function()
                        BaseballScene:PlayAnimation(self.world, self.HitRoleActorName, self.Ani_idle, true, 1)
                    end
                    BaseballScene:PlayAnimation(
                        self.world,
                        self.HitRoleActorName,
                        self.Ani_Hit02,
                        false,
                        0.05,
                        Anicallback
                    )
                    self.GameState = 4
                else
                    local Anicallback = function()
                        BaseballScene:PlayAnimation(self.world, self.HitRoleActorName, self.Ani_idle, true, 1)
                    end
                    BaseballScene:PlayAnimation(
                        self.world,
                        self.HitRoleActorName,
                        self.Ani_HitMiss,
                        false,
                        1,
                        Anicallback
                    )
                end
            end
        end
    )

    local UIAnimIns = self.ScoreUIActor:LoadAnimationAsset("Asset/Public/UI/GameHit.uixanim")
    UIAnimIns = self.ScoreUIActor:GetUIAnimationIns()
    UIAnimIns:Apply()
    UIAnimIns:GetUIAnimController():SetLoop(true)
    UIAnimIns:GetUIAnimController():Play()


    xe.Scheduler:scheduleOnce(
                function()
                    if nil ~= gameOverCallback then
                        gameOverCallback(self.Score)
                    end
                end,
                10
            )
end

function BaseballScene:onEnd()
    tolua.setpeer(self, {})
end

function BaseballScene:Set2DPos(scene, MeshAcotrName, UIScene, UINodeName, String)
    local actor = scene:FindActor(MeshAcotrName)
    local uiNode = UIScene:GetChildByNameRecrusive(UINodeName)
    local pos3D = actor:GetActorLocation()
    local pos2D = BaseballScene:GetUIPosition(uiNode, pos3D)
    uiNode:SetString(String)
    uiNode:SetVisible(true)
    uiNode:SetPosition(pos2D)
end

function BaseballScene:Set2DPosTips(scene, MeshAcotrName, UIScene, UINodeName)
    local actor = scene:FindActor(MeshAcotrName)
    local uiNode = UIScene:GetChildByNameRecrusive(UINodeName)
    local pos3D = actor:GetActorLocation()
    local pos2D = BaseballScene:GetUIPosition(uiNode, pos3D)
    uiNode:SetVisible(true)
    uiNode:SetPosition(pos2D)
end

function BaseballScene:onTick(fDelta)
    BaseballScene:Set2DPos(self.world, "paizi_100M", self.ScroeUIScene, "100M_Label", "100m")
    BaseballScene:Set2DPos(self.world, "paizi_300M", self.ScroeUIScene, "300M_Label", "300m")
    BaseballScene:Set2DPos(self.world, "paizi_500M", self.ScroeUIScene, "500M_Label", "500m")
    BaseballScene:Set2DPos(self.world, "paizi_700M", self.ScroeUIScene, "700M_Label", "700m")
    BaseballScene:Set2DPos(self.world, "paizi_900M", self.ScroeUIScene, "900M_Label", "900m")
    BaseballScene:Set2DPos(self.world, "paizi_1100M", self.ScroeUIScene, "1100M_Label", "1100m")
    BaseballScene:Set2DPos(self.world, "paizi_1300M", self.ScroeUIScene, "1300M_Label", "1300m")
    BaseballScene:Set2DPos(self.world, "paizi_1500M", self.ScroeUIScene, "1500M_Label", "1500m")
    BaseballScene:Set2DPos(self.world, "paizi_1700M", self.ScroeUIScene, "1700M_Label", "1700m")
    BaseballScene:Set2DPos(self.world, "paizi_F100M", self.ScroeUIScene, "F100M_Label", "-100m")
    BaseballScene:Set2DPos(self.world, "paizi_F300M", self.ScroeUIScene, "F300M_Label", "-300m")
    BaseballScene:Set2DPosTips(self.world, "HitPoint", self.ScroeUIScene, "Hit_Widget")

    -- --UILabel对位置
    -- local actor = self.world:FindActor("paizi_100M")
    -- local uiNode = self.ScroeUIScene:GetChildByNameRecrusive("100M_Label")
    -- local pos3D = actor:GetActorLocation()
    -- local pos2D = BaseballScene:GetUIPosition(uiNode, pos3D)
    -- uiNode:SetVisible(true)
    -- uiNode:SetPosition(pos2D)

    --游戏运行时间累加
    self.GameTime = self.GameTime + fDelta

    local FlyActorcurrentScore = self.FlyRoleActor:GetActorLocation()
    local ScroeNode = self.ScroeUIScene:GetChildByNameRecrusive("CurrentScroe_Label")
    local currentscroe = keepDecimalTest(FlyActorcurrentScore.x, 0)
    if currentscroe > -100 then
        currentscroe = 0
        ScroeNode:SetString("当前得分 ： " .. currentscroe)
    else
        ScroeNode:SetString("当前得分 ： " .. -currentscroe)
    end
    -- print(currentscroe)

    --  每帧获取，GameState9_CallBack执行后不在获取分数，保证传出去的数与界面上一致
    if self.GameState9_CB_State ~= 1 then
        self.Score = keepDecimalTest(-FlyActorcurrentScore.x, 0)
    end

    -- print(self.Score)
    -- print(keepDecimalTest(-FlyActorcurrentScore.x,0))

    -- if self.IsHit == true and self.Scorestate == nil then
    --     --击中的最终得分
    --     self.Score = keepDecimalTest(FlyActorcurrentScore.x,0)
    --     print(keepDecimalTest(FlyActorcurrentScore.x,0))
    --     -- self.Score = self.FlyScore + self.bounce + self.slide
    --     -- print(keepDecimalTest(self.Score,0))
    --     self.Scorestate = 1
    -- end

    --飞行状态打印
    if self.NowGameState ~= self.oldGameState then
        print("self.NowGameState = " .. self.NowGameState)
        self.oldGameState = self.NowGameState
    end

    if self.GameState ~= self.GameState1111 then
        print("self.GameState = " .. self.GameState)
        self.GameState1111 = self.GameState
    end

    --获取飞行模型的高度值
    if self.FlyRoleActor == nil then
    else
        self.FlyRolePos = self.FlyRoleActor:GetActorLocation()
    end

    --判断self.Delay时间，延迟多少秒后球下落
    if self.Delaydt < self.Delay and self.FlyRole == nil then
        self.NowGameState = 1

        local touchBtnNode = self.TouchUIScene:GetChildByNameRecrusive("TouchImage")
        touchBtnNode:SetVisible(true)

        --拿到场景
        local world = self:GetWorld()
        local Actor = world:FindActor("Baseball_Actor")
        local FlyRole = Role(Actor)
        --初始化角色位置
        FlyRole.Actor:SetActorLocation(self.RoleFlyInitialPosition)
        --ActorData
        --持续时间
        local dt = self.dt
        --棒球移动初始位置
        local origin = FlyRole.Actor:GetActorLocation()
        --棒球移动终点位置
        local dest = XVECTOR3(0, -0.1, -42.1566)
        --初始加速度
        local originVelocity = self.DownOriginVelocity

        local callback = function()
            self.GameState = 9
        end

        local tickCallBack = function()
        end

        FlyRole:SetMovement(origin, dest, dt, callback, tickCallBack, originVelocity)
        self.FlyRole = FlyRole
        self.GameState = 1
    end

    --相机跟随飞行的球
    if self.FlyRoleActor == nil then
    else
        self.FlyRolePos = self.FlyRoleActor:GetActorLocation()
        self.CameraPos.x = self.FlyRolePos.x + self.CameraOffset.x
        self.Camera:SetActorLocation(self.CameraPos)
    end

    -- --游戏状态判断，是否处于球落地滑动阶段
    -- if self.FlyRolePos.y <= -0.1 and self.EndState ~= nil then
    --     self.GameState = 9
    --     self.EndState = nil
    -- end

    --判断游戏阶段self.GameState == 9 时，球飞出
    if self.GameState == 9 and self.GameState9 == nil then
        self.NowGameState = 9

        local OpBtnNode = self.TouchUIScene:GetChildByNameRecrusive("TouchImage")
        OpBtnNode:SetVisible(false)

        local _self = self
        local GameState9_callback = function()
            print("GameState9_callback")
            print("得分 = " .. keepDecimalTest(self.Score, 0))
            -- print("得分 = " .. keepDecimalTest(self.Score,0))
            local ScroeBtnNode = self.ScroeUIScene:GetChildByNameRecrusive("GameOver_Scroe_Label")
            ScroeBtnNode:SetString(keepDecimalTest(self.Score, 0) .. "m")
            local UIAnimIns = self.ScoreUIActor:LoadAnimationAsset("Asset/Public/UI/GameOverAni_01.uixanim")
            UIAnimIns = self.ScoreUIActor:GetUIAnimationIns()
            UIAnimIns:Apply()
            UIAnimIns:GetUIAnimController():SetLoop(false)
            UIAnimIns:GetUIAnimController():Play()
            self.GameState9_CB_State = 1

            xe.Scheduler:scheduleOnce(
                function()
                    print("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX", _self, gameOverCallback)
                    if nil ~= gameOverCallback then
                        print("AAAAAAAAAAAAAAAAAAAAA")
                        gameOverCallback(_self.Score)
                    end
                end,
                2
            )
        end

        local GameState9_tickCallBack = function()
        end

        local GameState9_origin = self.FlyRole.Actor:GetActorLocation()
        local ActorPos = self.FlyRoleActor:GetActorLocation()
        if ActorPos.x > 0 then
            ActorPos.x = 0
        end
        local GameState9_dest = XVECTOR3(ActorPos.x - self.slide, ActorPos.y, -42.1566)
        print("GameState9_dest目标点分数 = " .. keepDecimalTest(-ActorPos.x + self.slide, 0))
        local GameState9_dt = self.dt9
        local GameState9_originVelocity = XVECTOR3(0, 0, 0)
        self.FlyRole:SetMovement(
            GameState9_origin,
            GameState9_dest,
            GameState9_dt,
            GameState9_callback,
            GameState9_tickCallBack,
            GameState9_originVelocity
        )
        self.GameState9 = 1

        --播放落地动画
        BaseballScene:PlayAnimation(self.world, "Baseball_Actor", "Asset/Public/Baseball/Baseball_Down.seq", false, 1)
    end

    --判断游戏阶段self.GameState == 2 时，球飞出
    if self.GameState == 2 and self.GameState1 == nil then
        self.NowGameState = 2

        if self.Highttest == 2 then
            --播放过场动画
            local SceneSequenceActor = self.world:FindActor("Effect_1")
            local SceneSequenceComponent = SceneSequenceActor:GetRootComponent()
            SceneSequenceComponent:GetSequenceInstance():GetSeqAnimController():Play()
        elseif self.Highttest == 1 then
            --播放过场动画
            local SceneSequenceActor = self.world:FindActor("Effect_huangse_1_1")
            local SceneSequenceComponent = SceneSequenceActor:GetRootComponent()
            SceneSequenceComponent:GetSequenceInstance():GetSeqAnimController():Play()
        end

        local GameState1_callback = function()
            self.GameState = 9
            -- self.GameState = 3
        end

        local GameState1_tickCallBack = function()
        end

        local GameState1_origin = self.FlyRole.Actor:GetActorLocation()

        self.ActorPos = self.FlyRoleActor:GetActorLocation()

        local GameState1_dest = XVECTOR3(-self.FlyScore, 0, -42.1566)
        local GameState1_dt = self.dt2
        local GameState1_originVelocity = self.FlyOriginVelocity
        self.FlyRole:SetMovement(
            GameState1_origin,
            GameState1_dest,
            GameState1_dt,
            GameState1_callback,
            GameState1_tickCallBack,
            GameState1_originVelocity
        )
        self.GameState1 = 1

        --播放飞行动画
        BaseballScene:PlayAnimation(self.world, "Baseball_Actor", "Asset/Public/Baseball/Fly.montage", false, 1)
    end

    if self.GameState == 4 and self.GameState4 == nil then
        self.NowGameState = 4

        local GameState4_callback = function()
            self.GameState = 2
        end

        local GameState4_tickCallBack = function()
        end

        local GameState4_origin = self.FlyRole.Actor:GetActorLocation()

        self.ActorPos = self.FlyRoleActor:GetActorLocation()

        local GameState4_dest = XVECTOR3(GameState4_origin.x - 5, GameState4_origin.y, -42.1566)
        local GameState4_dt = 1
        local GameState4_originVelocity = XVECTOR3(0, 0, 0)
        self.FlyRole:SetMovement(
            GameState4_origin,
            GameState4_dest,
            GameState4_dt,
            GameState4_callback,
            GameState4_tickCallBack,
            GameState4_originVelocity
        )
        self.GameState4 = 1

        --播放飞行动画
        BaseballScene:PlayAnimation(self.world, "Baseball_Actor", "Asset/Public/Baseball/Fly.montage", false, 0.1)
    end

    -- --球落地效果
    -- if self.GameState == 3 and self.GameState3 == nil then

    --     self.NowGameState = 3

    --     local printpos = self.FlyRole.Actor:GetActorLocation()
    --     local GameState3_callback = function()
    --         self.GameState = 9
    --     end

    --     local GameState3_tickCallBack = function()
    --     end

    --     local GameState3_origin = self.FlyRole.Actor:GetActorLocation()
    --     local ActorPos = self.FlyRoleActor:GetActorLocation()
    --     local GameState3_dest = XVECTOR3(ActorPos.x - self.bounce, -0.1, -42.1566)
    --     print(keepDecimalTest(-ActorPos.x - self.bounce,0))
    --     local GameState3_dt = self.dt3
    --     local GameState3_originVelocity = XVECTOR3(0, 3, 0)

    --     self.FlyRole:SetMovement(GameState3_origin, GameState3_dest, GameState3_dt, GameState3_callback, GameState3_tickCallBack, GameState3_originVelocity)
    --     self.GameState3 = 1
    -- end

    --游戏累加时间与游戏总时长做判断
    if self.GameTime <= self.GameOverTime then
        --延迟计时,判断开始下落时机
        if self.Delay < self.Delaydt then
            self.Delay = self.Delay + fDelta
        else
            --判断是否落地且是否被击中
            if self.Time <= self.dt and self.IsHit ~= true then
                --落地前一直下落，停止下落计时
                self.Time = self.Time + fDelta
            else
                --落地后飞出去，且计时
                self.Time2 = self.Time2 + fDelta
            end
        end
    else
        --时间超过GameOverTime，完事要做的内容
        if self.GameTime > self.GameOverTime and self.Finish == nil then
            print("游戏结束" .. self.Time)
            self.Finish = 1
        end
    end

    if self.FlyRole ~= nil and self.HitType01 == nil then
        self.FlyRole:onTick(fDelta)
    elseif self.FlyRole ~= nil and self.HitType01 == 1 then
        self.CrrSlowTime = self.CrrSlowTime + fDelta
        if self.CrrSlowTime >= 1 then
            self.GameState = 2
            self.HitType01 = nil
        end
        self.FlyRole:onTick(fDelta)
    -- self.FlyRole:onTick(fDelta * 0.1)
    end
end

function BaseballScene:onResume()
end

function BaseballScene:onPause(...)
end

function BaseballScene:SetRoomInfo(roomInfo)
end

function BaseballScene:SetGameInfo(gameInfo)
end

function BaseballScene:GetGameScoreJson()
    return ""
end

function BaseballScene:SetUploadGameDataFunction(callback)
end

function BaseballScene:SetGameOverCallBack(callback)
    gameOverCallback = callback
end

--从场景，通过姓名找Actor，播放动画
--输入场景，Actor名字，动画路径，是否循环  数据
function BaseballScene:PlayAnimation(scene, ActorName, AnimaPath, pLoop, SlowPers, funcCallback)
    xe.Scheduler:scheduleOnce(
        function()
            if tolua.isnull(scene) then
                return
            end
            local Actor = scene:FindActor(ActorName)
            local comp = Actor:GetRootComponent()
            XEAnimController:UnloadAnimation(comp)
            XEAnimController:LoadAnimation(AnimaPath, comp)
            comp:GetAnimController():SetLoop(pLoop)
            comp:GetAnimController():Play()
            comp:GetAnimController():SetPlayRate(SlowPers)
            if funcCallback then
                local listener = xe.AnimationPlayListener:Create()
                local temp = function()
                    funcCallback()
                    comp:GetAnimController():RemoveListener(listener)
                end
                listener:RegisterHandler(temp, xe.Handler.XEANIMATIONPLAY_ONETIMEFINISHED_CALLNACK)
                comp:GetAnimController():AddListener(listener)
            end
        end,
        xe.Scheduler.INTERVAL_NEXT_TICK
    )
end

-- -- 保留n位小数
-- function keepDecimalTest(num, n)
--     if type(num) ~= "number" then
--         return num
--     end
--     n = n or 2

--     if num < 0 then
--         return -(math.abs(num) - math.abs(num) % 0.1 ^ n)
--     else
--         return num - num % 0.1 ^ n
--     end
-- end

-- 保留n位小数,四舍五入
function keepDecimalTest(nNum, n)
    if type(nNum) ~= "number" then
        return nNum
    end
    n = n or 0
    n = math.floor(n)
    if n < 0 then
        n = 0
    end
    local nDecimal = 10 ^ n
    local nTemp = math.floor(nNum * nDecimal)
    local nRet = nTemp / nDecimal
    return nRet
end

--根据击中高度判断，击中是否生效    Hight01 < Hight00 < Hight02
function BaseballScene:Score(Hight00, Hight01, Hight02, VarA, VarB, bounce, slide)
end

return BaseballScene

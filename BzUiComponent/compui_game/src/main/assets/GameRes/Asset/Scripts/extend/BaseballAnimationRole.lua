local MoveObject = require("Asset.Scripts.extend.BaseballMovementRole")

--从MovementRole派生出来
local Role = MoveObject:extend()

--构造函数，记录Actor
function Role:new(Actor)
    MoveObject.new(self, Actor)

    self.Actor = Actor
    self.AnimListener = nil
    self.AnimList = {}
    self.PlayRate = 1.0
    self.CurAnimPlayRate = 0.0
    self.CurAnimTickCallBack = nil

    self.AnimationQueue = {}
end

--设置播放速度
function Role:SetPlayRate(fPlayRate)

    --把播放速度保存起来
    self.PlayRate = fPlayRate

    --获取当前的 播放控制器， 设置播放控制器的播放速度，播放控制器详见 API XEAnimController
    local ctrl = self:GetCurrentAnimControll()
    if nil ~= ctrl then
        ctrl:SetPlayRate(self.PlayRate)
    end

end

--添加动画回调，内部会进行安全判断，当已存在动画监听时，先移除已存在的动画监听
function Role:safeSwitchListener(holder, listener)
    --Log("safe switch==>begine", holder, listener)

    local component = holder.Actor:GetRootComponent()
    --Log("safe switch==>", component)
    if nil ~= holder.AnimListener then
        component:GetAnimController():RemoveListener(holder.AnimListener)
    end
    component:GetAnimController():AddListener(listener)
    --Log("safe switch==> add ok.", component)

    holder.AnimListener = listener
end

--获取Actor
function Role:GetActor()
    return self.Actor
end

--获取Component
function Role:GetComponent()
    return self.Actor:GetRootComponent()
end

--设置动画映射，其实就是以一个Map来存储动画名称与动画路径。这样在不同的Actor播放不同的动画时，也可以以相同的逻辑来处理。
function Role:SetAnimation(AnimName, AnimPath)
    self.AnimList[AnimName] = AnimPath
end

--获取当前播放控制器
function Role:GetCurrentAnimControll()
    local comp = self:GetComponent()
    return comp:GetAnimController()
end

--播放动画的接口
function Role:playAnimationImpl(AnimName, IsLoop, PlayBeginCallBack, PlayEndCallBack, PlayTickCallBack)

    --通过名字来获取 动画文件 *.seq 或者 *.montage文件路径
    if nil == self.AnimList[AnimName] then
        return 0
    end

    --获取Actor的Component，手动卸载掉它的播放控制器XEAnimController
    local comp = self:GetComponent()
    XEAnimController:UnloadAnimation(comp)
    --安全性保护代码，其实没啥用，就是如果获取到了播放控制器，就停止播放。
    if nil ~= comp:GetAnimController() then
        comp:GetAnimController():Stop()
    end

    --通过LoadAnimation函数，为Component加载动画文件 *.seq 或者 *.montage
    XEAnimController:LoadAnimation(self.AnimList[AnimName], comp)
    --设置是否循环播放
    comp:GetAnimController():SetLoop(IsLoop)
    --调用播放
    comp:GetAnimController():Play()
    --设置播放速度
    self:SetPlayRate(self.PlayRate)

    --获取该动画的总时长，单位是纳秒，转换成秒
    local animTotalTime = comp:GetAnimController():GetTimeLength() / 1000 / 1000

    --调用动画播放开始的回调函数
    if nil ~= PlayBeginCallBack then
        PlayBeginCallBack(animTotalTime)
    end

    --记录当前动画播放百分比为0%
    self.CurAnimPlayRate = 0.0

    --如果有播放结束回调函数，那么添加监听器，自动在动画结束时调用回调函数
    if nil ~= PlayEndCallBack then
        local listener = xe.AnimationPlayListener:Create()
        listener:RegisterHandler(function()
            PlayEndCallBack()
            comp:GetAnimController():RemoveListener(listener)
        end, xe.Handler.XEANIMATIONPLAY_ONETIMEFINISHED_CALLNACK)
        self:safeSwitchListener(self, listener)
    end

    --记下播放动画时的每帧的回调函数
    self.CurAnimTickCallBack = PlayTickCallBack

    --返回播放时长
    return animTotalTime

end

--播放动画，参数： 1.动画名称 2.是否循环 3.播放时的回调() 4.播放结束的回调() 5.播放Tick的回调(与上一帧的间隔, 当前播放百分比)
function Role:PlayAnimation(AnimName, IsLoop, PlayBeginCallBack, PlayEndCallBack, PlayTickCallBack)

    local AnimData = {}
    AnimData.AnimName = AnimName
    AnimData.IsLoop = IsLoop
    AnimData.PlayBeginCallBack = PlayBeginCallBack
    AnimData.PlayEndCallBack = PlayEndCallBack
    AnimData.PlayTickCallBack = PlayTickCallBack

    --将所有参数压入队列，该队列会在onTick函数内，每帧被访问，取出队列头的所有参数，并播放动画。
    --这样实现的目的是，在某些情况下，会有当动画播放完毕后，在播放完毕的回调函数中切换其他动画的逻辑，而切换动画会导致当前动画的监听器被移除。相当于自己所持有的函数移除自己，出现各种风险。
    --所以当有切换动画的需要时，不会立即在动画的回调函数内切换动画，而是将当前帧的逻辑执行完成后，在下一帧的onTick方法内切换动画。
    table.insert(self.AnimationQueue, AnimData)
end

--每帧Tick，需要在场景的Tick中手动调用，以推进动画的进行
function Role:onTick(fDelta)

    --调用父类同名方法
    if nil ~= MoveObject.onTick then
        MoveObject.onTick(self, fDelta)
    end

    --如果动画队列中有动画需要被播放，那么播放队列首保存的动画
    if #self.AnimationQueue > 0 then

        local AnimData = self.AnimationQueue[#self.AnimationQueue]
        self:playAnimationImpl(AnimData.AnimName, AnimData.IsLoop, AnimData.PlayBeginCallBack, AnimData.PlayEndCallBack, AnimData.PlayTickCallBack)

        self.AnimationQueue = {}
    end


    local curAnim = self:GetCurrentAnimControll()
    if nil == curAnim then
        return
    end

    --执行播放动画时每帧的回调函数，默认有两个参数为 fDelta 和 当前播放动画的进度百分比
    local totalTime = curAnim:GetTimeLength()
    local curTime = curAnim:GetCurTime()
    self.CurAnimPlayRate = curTime / totalTime

    if nil ~= self.CurAnimTickCallBack then
        self.CurAnimTickCallBack(fDelta, self.CurAnimPlayRate)
    end

end

return Role
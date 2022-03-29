local Object = require("Asset.Scripts.extend.classic")

local Role = Object:extend()

--构造函数，保存Actor，构造初始移动状态
function Role:new(Actor)
    self.Actor = Actor
    self.Movement = {
        --速度
        Velocity = XVECTOR3(0, 0, 0),
        --加速度
        Acceleration = XVECTOR3(0, 0, 0),
        --移动起始点
        Origin = XVECTOR3(0, 0, 0),
        --移动目的点
        Destination = XVECTOR3(0, 0, 0),
        --移动总时长
        Duration = 0.0,
        --当前移动时长
        CurrentDuration = 0,
        --移动结束时执行的回调
        Callback = nil,
        --移动时，每帧都会执行的回调
        TickCallback = nil
    }
end

--起始位置，目标位置，时长，结束时的回调函数，每帧回调函数，初始速度
--默认为匀加速运动，会根据参数中的初速度、起始位置、目标位置、时长，自动确定加速度。
--当加速度为0时，运动即为匀速运动
function Role:SetMovement(origin, dest, dt, callback, tickCallBack, originVelocity)

    self.Movement.CurrentDuration = 0
    self.Movement.Origin = origin
    self.Movement.Destination = dest
    self.Movement.Duration = dt
    self.Movement.Callback = callback
    self.Movement.TickCallback = tickCallBack
    self.Movement.MovementState = true
    self.Actor:SetActorLocation(origin)

    if 0 >= dt then
        return
    end

    if nil == originVelocity or originVelocity.x == 0 then
        self.Movement.Velocity.x = (dest.x - origin.x) / dt
    else
        self.Movement.Velocity.x = originVelocity.x
    end

    if nil == originVelocity or originVelocity.y == 0 then
        self.Movement.Velocity.y = (dest.y - origin.y) / dt
    else
        self.Movement.Velocity.y = originVelocity.y
    end

    -- S = v0 * t + 1/2 * a * t*t
    self.Movement.Acceleration.y = ((dest.y - origin.y) - self.Movement.Velocity.y * dt) / (1 / 2 * dt * dt)
    self.Movement.Acceleration.x = ((dest.x - origin.x) - self.Movement.Velocity.x * dt) / (1 / 2 * dt * dt)
end

--停止移动
function Role:StopMovement()
    self.Movement.MovementState = false
end

--通过当前的移动参数，更新Actor的位置
function Role:updateMove(fDelta)

    if true == self.Movement.MovementState then

        --计算出当前移动时间，通过时间来决定当前移动是否完成
        self.Movement.CurrentDuration = self.Movement.CurrentDuration + fDelta
        if self.Movement.CurrentDuration >= self.Movement.Duration then
            self.Movement.CurrentDuration = self.Movement.Duration
        end

        -- S = v0 * t + 1/2 * a * t*t
        --计算当前时间会移动到的位置
        local pos = self.Actor:GetActorLocation()
        pos.x = self.Movement.Origin.x + self.Movement.Velocity.x * self.Movement.CurrentDuration + 0.5 * self.Movement.Acceleration.x * self.Movement.CurrentDuration * self.Movement.CurrentDuration
        pos.y = self.Movement.Origin.y + self.Movement.Velocity.y * self.Movement.CurrentDuration + 0.5 * self.Movement.Acceleration.y * self.Movement.CurrentDuration * self.Movement.CurrentDuration
        self.Actor:SetActorLocation(pos)

        --执行每帧回调
        if nil ~= self.Movement.TickCallback then
            self.Movement.TickCallback()
        end

        --通过时间来决定当前移动是否完成
        if self.Movement.CurrentDuration >= self.Movement.Duration then

            self.Movement.MovementState = false

            if nil ~= self.Movement.Callback then
                self.Movement.Callback()
            end

            return
        end

    end
end

--需要在场景的Tick中手动调用，以推进移动的进行
function Role:onTick(fDelta)

    --更新移动
    self:updateMove(fDelta)

end

return Role
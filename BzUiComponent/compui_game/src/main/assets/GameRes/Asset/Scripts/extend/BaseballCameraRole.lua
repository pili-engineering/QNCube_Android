local Object = require("Asset.Scripts.extend.classic")

local Camera = Object:extend()

--线性差值函数,需要支持乘法运算
function Lerp(a, b, smooth)

    if smooth < 0 then
        smooth = 0
    elseif smooth > 1 then
        smooth = 1
    end

    local res = a * (1 - smooth) + b * smooth
    return res

end

--构造函数
function Camera:new(cameraActor)
    --所控制的 摄像机类型Actor
    self.Actor = cameraActor
    --摄像机看向的目标Actor
    self.TargetActor = nil

    --摄像机相对于目标的距离
    self.Distance = XVECTOR3(0, 0, 0)
    --摄像机看向的点对于目标的偏移
    self.LookAtOffset = XVECTOR3(0, 0, 0)
    --当前摄像机看向的点对于目标的偏移（平滑过渡）
    self.CurrentLookAtOffset = XVECTOR3(0, 0, 0)
    --平滑值
    self.Smooth = 0.15
    --锁角度
    self.Lock = {}

end

function Camera:SetTargetActor(targetActor, distance, lookAtOffset, lock)
    self.TargetActor = targetActor
    if distance == nil then
        distance = XVECTOR3(0,0,0)
    end
    if lookAtOffset == nil then
        lookAtOffset = XVECTOR3(0,0,0)
    end
    if lock == nil then
        lock = {}
    end

    self.Lock = lock
    self.Distance = distance
    self.LookAtOffset = lookAtOffset

    self.Smooth = 0.15
end



function Camera:onTick(fDelta)


    if nil == self.TargetActor then
        return
    end

    local modelInstance = self.TargetActor:GetRootComponent():GetModelInstance()
    local targetPos = modelInstance:GetAbsolutePosition()

    local camCurPos = self.Actor:GetActorLocation()
    local camNowPos = targetPos + self.Distance
    
    self.CurrentLookAtOffset = Lerp(self.CurrentLookAtOffset, self.LookAtOffset, 0.2)
    local lookAtPos = targetPos + self.CurrentLookAtOffset


    local pos = Lerp(camCurPos, camNowPos, self.Smooth)
    if self.Lock.x ~= nil then
        pos.x = targetPos.x + self.Lock.x
    end
    if self.Lock.y ~= nil then
        pos.y = targetPos.y + self.Lock.y
    end
    if self.Lock.z ~= nil then
        pos.z = targetPos.z + self.Lock.z
    end
    self.Actor:SetActorLocation(pos)

    local comp = self.Actor:GetRootComponent()

    local dir = XVECTOR3(lookAtPos - pos)
    if 0 ~= dir:Normalize() then
        local up = self.Actor:GetActorUpVector()
        up:Normalize()
        comp:GetCamera():SetDirAndUp(dir, up)
    end

end



return Camera
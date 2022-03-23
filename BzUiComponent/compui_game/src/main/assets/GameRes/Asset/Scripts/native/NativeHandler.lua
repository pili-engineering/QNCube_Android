
NativeHandler = NativeHandler or {}

-- Natvie Call Lua CallBack Rigister

--[[
    消息接收回调
    function name(message table) end
]]
NativeHandler.messageReceivedCallback = nil;

--[[
    接收到房间信息回调
    function name(roomInfo table) end
]]
NativeHandler.roomInfoReceivedCallback = nil;

--[[
    接收到游戏相关信息回调
    function name(gameInfo table) end
]]
NativeHandler.gameInfoReceivedCallback = nil;

--[[
    移除游戏回调
    function name() end
]]
NativeHandler.removeLuaGameCallback = nil;


-- Lua Call Native Function

--[[
    用户信息
]]
function NativeHandler:userInfo()
    local userInfoString = xe.ScriptBridge:call("LiveGameHandler","userInfo","")
    local userInfo = XEJSON.decode(userInfoString)
    return userInfo;
end

--[[
    发送消息
]]
function NativeHandler:sendMessage(message)
    xe.ScriptBridge:call("LiveGameHandler","sendMessage",message)
end

--[[
    客户端goto跳转
]]
function NativeHandler:gotoAction(action)
    xe.ScriptBridge:call("LiveGameHandler","gotoAction",action)
end

--[[
    结束游戏调用
]]
function NativeHandler:removeGame(gameId)
    xe.ScriptBridge:call("LiveGameHandler","removeGame",gameId)
end

--[[
    客户端想要游戏数据
]]
function NativeHandler:uploadGameScore(data)
    xe.ScriptBridge:call("LiveGameHandler", "uploadGameScore", data)
end

--[[
    API 网络请求
]]
function NativeHandler:apiWithUrlParams(url, params, callback)

    local urlParams = urlParams or {}
    urlParams["url"] = url;
    urlParams["params"] = params;

    local paramsString = XEJSON.encode(urlParams)

    xe.ScriptBridge:callAsync("LiveGameHandler","apiWithUrlParams",paramsString,function (response)
        local responseTable = XEJSON.decode(response)
        if callback ~= nil then
            callback(responseTable)
        end
    end)
end


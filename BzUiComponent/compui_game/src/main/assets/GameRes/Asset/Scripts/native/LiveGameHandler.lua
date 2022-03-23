-- 直播游戏Lua层Bridge

require("Asset.Scripts.native.NativeHandler")

LiveGameHandler = LiveGameHandler or {}

function LiveGameHandler.receivedMessage(message)
    Log("Lua 收到调用 LiveGameHandler.receivedMessage:"..message);

	local messageTable = XEJSON.decode(message)

    if messageTable then

        if NativeHandler.messageReceivedCallback ~= nil then
            NativeHandler.messageReceivedCallback(messageTable)
        else
            Log("LiveGameHandler.receivedMessage: NativeHandler.messageReceivedCallback is nil !!!!!! ")
        end

    else
        Log("LiveGameHandler.receivedMessage: Error !!!!!! ")
    end

	return nil
end

function LiveGameHandler.roomInfo( roomInfo )
	-- body
	Log("Lua 收到调用 LiveGameHandler.roomInfo:"..roomInfo);

    local roomInfoTable = XEJSON.decode(roomInfo)

    if roomInfoTable ~= nil then
        if NativeHandler.roomInfoReceivedCallback ~= nil then
            NativeHandler.roomInfoReceivedCallback(roomInfoTable)
        end
    end

end

function LiveGameHandler.gameInfo( gameInfo )
	-- body
    Log("Lua 收到调用 LiveGameHandler.gameInfo:"..gameInfo);
    local gameInfoTable = XEJSON.decode(gameInfo)

    if gameInfoTable ~= nil then
        if NativeHandler.gameInfoReceivedCallback ~= nil then
            NativeHandler.gameInfoReceivedCallback(gameInfoTable)
        end
    end

end

function LiveGameHandler.removeLuaGame()
    -- body
    Log("Lua 收到调用 LiveGameHandler.removeLuaGame")

    if NativeHandler.removeLuaGameCallback ~= nil then
        NativeHandler.removeLuaGameCallback()
    end

end

function LiveGameHandler.getGameScore()

    Log("Lua 收到调用 LiveGameHandler.getGameScore")

    if NativeHandler.getGameScoreCallback ~= nil then
        NativeHandler.getGameScoreCallback()
    end

end
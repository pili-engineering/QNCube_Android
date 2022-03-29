require("Asset.Scripts.native.init")
local GameMainScene = require("Asset.Scripts.BaseballScene")
package.loaded["Asset.Scripts.BaseballGlobal"] = nil

function ONLINE_LOG(log)
    print(tostring(os.time()) .. "  LOG:  " .. tostring(log))
end

local App = {}

_G_GameConfig = {coefficient = 1.2}
function App.onStart(config)
    if config then
        local configObj = xjson.decode(config)
        if configObj.coefficient then
            _G_GameConfig.coefficient = configObj.coefficient
        end
        if configObj.language then
            I18N:SetLanguage(configObj.language)
        end
    end
    ONLINE_LOG("onStart")
    if App.mainScene then
        return
    end

    local scene = GameMainScene:new("com.wemomo.engine")
    App.mainScene = scene
    xe.Director:GetInstance():PushScene(scene)

    scene:SetGameOverCallBack(
        function(score)
            ONLINE_LOG("Romve Self.")

            if xe.Director:GetInstance():GetTopScene() == App.mainScene then
                ONLINE_LOG("PopScene")
                App.mainScene = nil
                xe.Director:GetInstance():PopScene()
            end
            NativeHandler:removeGame(xjson.encode({score = score}))
        end
    )
end

function App.onResume()
    ONLINE_LOG("onResume")
end

function App.onPause()
    ONLINE_LOG("onPause")
end

function App.onEnd()
    ONLINE_LOG("onEnd")
end

xe.AppDelegate = App

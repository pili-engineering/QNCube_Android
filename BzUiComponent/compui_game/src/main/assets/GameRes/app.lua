function checkTime(enable_y, enable_m, enable_d)
    local y = tonumber(os.date("%Y"))
    local m = tonumber(os.date("%m"))
    local d = tonumber(os.date("%d"))

    if y < enable_y then
        return true
    elseif y == enable_y and m < enable_m then
        return true
    elseif y == enable_y and m == enable_m and d <= enable_d then
        return true
    end
    error("超过授权期" .. enable_y .. "-" .. enable_m .. "-" .. enable_d)
end

checkTime(2022, 11, 1)
require("Asset.Scripts.BaseballApp")
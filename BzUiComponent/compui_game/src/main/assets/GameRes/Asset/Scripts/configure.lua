
configure = {}

--区域一    （YPosHigh_0/YPosLow_0）打击点高低区间判定
--区域一    （VarA_0/VarB_0）击中后距离随机最大最小值
--区域一    （bounce_0）落地后向前弹跳距离
--区域一    （slide_0）落地后向前滚动距离
--区域一    （FlyDuration_0）飞行阶段位移持续时间


--棒球下落速度配置
configure.DownDuration = 1.5
configure.DownOriginVelocity = XVECTOR3(0, -3, 0)

--击打按钮点击后延迟生效时间（主要有动画前摇效果的表现）
configure.BtnDelayTime = 1

--打击区域一
configure.YPosHigh_0 = 40
configure.YPosLow_0 = 60
configure.VarA_0 = 500
configure.VarB_0 = 800
configure.bounce_0 = 200
configure.slide_0 = 50
configure.FlyDuration_0 = 1.5
configure.FlyOriginVelocity_0 = XVECTOR3(0, 200, 0)

--打击区域二
configure.YPosHigh_1 = 25
configure.YPosLow_1 = 60
configure.VarA_1 = 900
configure.VarB_1 = 1000
configure.bounce_1 = 100
configure.slide_1 = 25
configure.FlyDuration_1 = 2
configure.FlyOriginVelocity_1 = XVECTOR3(0, 100, 0)

--打击区域三
configure.YPosHigh_2 = 5
configure.YPosLow_2 = 25
configure.VarA_2 = 200
configure.VarB_2 = 500
configure.bounce_2 = 50
configure.slide_2 = 10
configure.FlyDuration_2 = 0.75
configure.FlyOriginVelocity_2 = XVECTOR3(0, 50, 0)

-- local GameState1_originVelocity = XVECTOR3(0, 500, 0)
return configure


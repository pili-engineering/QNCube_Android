/*
 * Copyright (c) 2016  athou（cai353974361@163.com）.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hapi.ut;

/**
 * @author xx
 * @date 2016/10/8
 */
public class ClickUtil {

    // 双击事件记录最近一次点击的时间
    private static long lastClickTime = 0;

    /**
     * 按钮点击去重实现方法一 ，需要在onclick回调内调用
     */
    public static boolean isClickAvalible() {
        return isClickAvalible(500);
    }

    /**
     * 按钮点击去重实现方法一 ，需要在onclick回调内调用
     *
     * @param miniIntervalMills 最小间隔。
     */
    public static boolean isClickAvalible(long miniIntervalMills) {
        if (System.currentTimeMillis() - lastClickTime > miniIntervalMills) {
            lastClickTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}
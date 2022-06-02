package com.qncube.linkmicservice;


import com.nucube.rtclive.QNMergeOption;

import java.util.List;

/**
 * 主播端连麦器
 */
public interface QNAnchorHostMicLinker {

    /**
     * 混流适配器
     */
    public static interface MixStreamAdapter {

        /**
         * 混流布局适配
         *
         * @param micLinkers 所有连麦者
         * @return 返回重设后的每个连麦者的混流布局
         */
        List<QNMergeOption> onResetMixParam(List<QNMicLinker> micLinkers, QNMicLinker target, boolean isJoin);


    }

    /**
     * 设置混流适配器
     *
     * @param mixStreamAdapter
     */
    public void setMixStreamAdapter(MixStreamAdapter mixStreamAdapter);

}

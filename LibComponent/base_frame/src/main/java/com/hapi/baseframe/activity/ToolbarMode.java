package com.hapi.baseframe.activity;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@IntDef({ToolbarMode.Parallel, ToolbarMode.Layer})
public @interface ToolbarMode {
    /**
     * toolbar与content为平行的上下层关系
     */
    int Parallel = 0;
    /**
     * toolbar与content同一层
     */
    int Layer = 1;
}
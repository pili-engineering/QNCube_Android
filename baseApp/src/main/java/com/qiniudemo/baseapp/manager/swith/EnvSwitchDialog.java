package com.qiniudemo.baseapp.manager.swith;


import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.qiniu.baseapp.R;


public class EnvSwitchDialog extends Dialog implements View.OnClickListener {

    private Button devBtn;
    private Button betaBtn;
    private Button releaseBtn;

    public EnvSwitchDialog(Context context) {
        super(context);
        View contentView = LayoutInflater.from(context).inflate(R.layout.env_switch_dialog, null);
        devBtn = contentView.findViewById(R.id.dev_btn);
        betaBtn = contentView.findViewById(R.id.beta_btn);
        releaseBtn = contentView.findViewById(R.id.release_btn);

        setContentView(contentView);
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        setTitle("切换环境");
        devBtn.setOnClickListener(this);
        betaBtn.setOnClickListener(this);
        releaseBtn.setOnClickListener(this);
        switch (SwitchEnvHelper.get().getEnvType()) {
            case Dev:
                devBtn.setEnabled(false);
                break;
            case Beta:
                betaBtn.setEnabled(false);
                break;
            case Release:
            default:
                releaseBtn.setEnabled(false);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);
    }

    @Override
    public void onClick(View v) {
        if (v == devBtn) {
            SwitchEnvHelper.get().switchEnvType(EnvType.Dev);
        } else if (v == betaBtn) {
            SwitchEnvHelper.get().switchEnvType(EnvType.Beta);
        } else if (v == releaseBtn) {
            SwitchEnvHelper.get().switchEnvType(EnvType.Release);
        }
        dismiss();
    }
}
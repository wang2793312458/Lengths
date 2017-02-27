package com.velue.distance.utils;


import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.velue.distance.R;

public abstract class ConfirmDialog extends CustomDialog {

    private TextView content_confirm_dialog, tv_title;
    private CheckBox checkBox;
    private Button bt_ok;
    private Button bt_cancel;

    private RelativeLayout relativeLayout;

    public ConfirmDialog(Context context, boolean cancelable,
                         OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    public ConfirmDialog(Context context, int theme) {
        super(context, theme);
    }

    public ConfirmDialog(Context context) {
        super(context);
    }

    @Override
    protected void setContentView() {
        setContentView(R.layout.dialog_confirm);
    }



    @Override
    protected void initViews() {
        tv_title = (TextView) findViewById(R.id.tv_title);
        setDialogTitle(tv_title);

        content_confirm_dialog = (TextView) findViewById(R.id.tv_content_confirm_dialog);
        setDialogContent(content_confirm_dialog);

        relativeLayout = (RelativeLayout) findViewById(R.id.rtv_lyt);
        setRelativeVisibility(relativeLayout);

        checkBox = (CheckBox) findViewById(R.id.checkBox1);

        bt_ok = (Button) findViewById(R.id.bt_ok);
        setOkButtonText(bt_ok);
        bt_cancel = (Button) findViewById(R.id.bt_cancel);
    }

    @Override
    protected void setListener() {

        bt_ok.setOnClickListener(this);
        bt_cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.bt_cancel:
            dismiss();
            break;
        case R.id.bt_ok:
            //开始修改任务
            startMission();
            sendShortMessage(checkBox);
            dismiss();
            break;
        }
    }

    public abstract void setDialogContent(TextView content);
    public abstract void setDialogTitle(TextView title);
    public void setOkButtonText(TextView textView){
        textView.setText("确定");
    }

    public void sendShortMessage(CheckBox checkBox){

    }

    public void setRelativeVisibility(RelativeLayout relativeLayout){
        relativeLayout.setVisibility(View.GONE);
    }

    public abstract void startMission();

}

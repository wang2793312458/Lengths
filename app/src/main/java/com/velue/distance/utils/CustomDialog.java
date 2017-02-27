package com.velue.distance.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View.OnClickListener;

public abstract class CustomDialog extends Dialog implements OnClickListener {
	
	public CustomDialog(Context context, boolean cancelable,
						OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public CustomDialog(Context context, int theme) {
		super(context, theme);
	}

	public CustomDialog(Context context) {
		super(context);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView();
		initViews();
		setListener();
	}

	protected abstract void setContentView();
	
	protected abstract void initViews();
	
	protected abstract void setListener();
}

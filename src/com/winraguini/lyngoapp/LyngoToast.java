package com.winraguini.lyngoapp;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class LyngoToast {
	public static void showToast(Context context, String message, int duration) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );		
		View view = inflater.inflate(R.layout.toast_default_layout, null);
		View layout = (ViewGroup)view.findViewById(R.id.toast_layout_root);		
		
		TextView text = (TextView) layout.findViewById(R.id.tvToast);
		text.setText(message);
		
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();
	}

}

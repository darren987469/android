package com.example.keyevent;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.BaseInputConnection;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements OnClickListener{
	static final String TAG = MainActivity.class.getSimpleName();
		
	Button btnUp;
	Button btnDown;
	TextView txtMsg;
	
	BaseInputConnection mInputConnection;
	KeyEvent keyDown;
	KeyEvent keyUp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		btnUp = (Button) findViewById(R.id.btnUp);
		btnDown = (Button) findViewById(R.id.btnDown);
		txtMsg = (TextView) findViewById(R.id.txtMsg);

		mInputConnection = new BaseInputConnection(this.getWindow().getDecorView(), false);
		keyDown = new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DPAD_DOWN);
		keyUp = new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DPAD_UP);
		
		btnUp.setOnClickListener(this);
		btnDown.setOnClickListener(this);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.d(TAG, "Keyevent action: "+event.getAction() + ", keycode: "+event.getKeyCode());
		
		String msg = "";
		if(event.getAction() == KeyEvent.ACTION_UP)
			msg += "key action: up,";
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_DPAD_UP:
			msg += " key code: d pad up";
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			msg += " key code: d pad down";
		default:
			break;
		}
		txtMsg.setText(msg);

		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onClick(View v) {
		if(v == btnUp) {
			mInputConnection.sendKeyEvent(keyUp);
		} else if (v == btnDown) {
			mInputConnection.sendKeyEvent(keyDown);
		}
	}
}

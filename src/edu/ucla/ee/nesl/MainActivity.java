package edu.ucla.ee.nesl;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	public final String TAG = "MainActivity";
	
   

	public enum weekday {
		SUNDAY(0), MONDAY(1), TUESDAY(2), WEDNESDAY(3), THURSDAY(4), FRIDAY(5), SATURDAY(6);
		
		private int value;
		weekday(int value) {
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	};
	
	public void onBtnClicked(View v){
//        if(v.getId() == R.id.button1){
//        	Log.i(TAG, "Button clicked");
//        	Intent mServiceIntent = new Intent(this, MainService.class);
//        	mServiceIntent.putExtra("type", "start");
//        	startService(mServiceIntent);
///*            if (mBound) {
//            	Log.i(TAG, "Service bound");
//            	Toast.makeText(getBaseContext(), "Service bound", Toast.LENGTH_LONG).show();
//            	mService.startReceiver();
//            }
//            else {
//            	Log.i(TAG, "not bound");
//            	Toast.makeText(getBaseContext(), "Service not bound", Toast.LENGTH_LONG).show();
//            }*/
//            
//            
//        }
    }
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
/*		Intent bindIntent = new Intent(this, MainService.class);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
        
        Log.i(TAG, "bind to service");     */ 
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
}
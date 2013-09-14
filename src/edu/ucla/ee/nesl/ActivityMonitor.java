package edu.ucla.ee.nesl;

import com.google.protobuf.InvalidProtocolBufferException;

import edu.ucla.ee.nesl.FirewallConfigMessages.Action;
import edu.ucla.ee.nesl.FirewallConfigMessages.DateTime;
import edu.ucla.ee.nesl.FirewallConfigMessages.FirewallConfig;
import edu.ucla.ee.nesl.FirewallConfigMessages.Rule;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.os.FirewallConfigManager;
import android.util.Base64;
import android.util.Log;

public class ActivityMonitor extends BroadcastReceiver {
	public final String TAG = "MainService";
	public static final String actType = "actType";
	private FirewallConfigManager firewallManager;
	private static FirewallConfig.Builder configBuilder = FirewallConfig.newBuilder();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Detected intent " + intent.getAction());
		//Toast.makeText(context, "broadcast received", Toast.LENGTH_LONG).show();
		
		if (firewallManager == null) {
			firewallManager = (FirewallConfigManager)context.getSystemService(Context.FIREWALLCONFIG_SERVICE);
		}
//		for connection with ambulation code
//		if (intent.getAction().indexOf("ACTIVITY") >= 0) {
//			Toast.makeText(context, "act received", Toast.LENGTH_LONG).show();
//			if (intent.hasExtra("actType")) {
//				String mode = intent.getExtras().getString(actType);
//				if (mode != null && mode.indexOf("WALKING") >= 0) {
//					String config = getConfig();
//					firewallManager.setFirewallConfig(getConfig());
//					readConfig(config);
//					Toast.makeText(context, "walk detected, write rule", Toast.LENGTH_LONG).show();
//				}
//			}
//		}
		if (intent.getAction().indexOf("TEST_RULE") >= 0) {
			Log.d(TAG, "test writing rules");
			String config = getConfig(1);
			firewallManager.setFirewallConfig(config);
			//readConfig(config);
			configBuilder = FirewallConfig.newBuilder();
		}
	}

	// read the rule back
	public void readConfig(String serializedRule) {
		FirewallConfig firewallConfig = null;
        try {
        	byte[] byteArr = Base64.decode(serializedRule, Base64.DEFAULT);
            //ByteString byteString = ByteString.copyFromUtf8(serializedRule);
            //firewallConfig = FirewallConfig.parseFrom(byteString);
            firewallConfig = FirewallConfig.parseFrom(byteArr);
        } catch (InvalidProtocolBufferException ex) {
            Log.e(TAG, "InvalidProtocolBufferException");
        }   
        
        if(firewallConfig != null) {
        	Log.d(TAG, "Reading from the Firewall Config File");
        	Log.d(TAG, "number of rule=" + firewallConfig.getRuleCount());
        	for(Rule rule: firewallConfig.getRuleList()) {
        		Log.d(TAG, "ruleName = " + rule.getRuleName() + ": sensorType = " + rule.getSensorType() + ": pkgName = " + rule.getPkgName() + ": pkgUid = " + rule.getPkgUid());
        	}   
        }
	}
	
    public void testSuppression(String pkgName, int uid, int sensorType, String ruleName, 
    		int weekDay, int fromHr, int fromMin, int toHr, int toMin ) {
    	boolean toAddDate = false;
    	//FirewallConfig.Builder configBuilder = FirewallConfig.newBuilder();
    	Rule.Builder rule = Rule.newBuilder();
    	rule.setRuleName(ruleName);
    	rule.setPkgName(pkgName);
    	rule.setSensorType(sensorType);
    	rule.setPkgUid(uid);
    	
    	Action.Builder action = Action.newBuilder();
    	action.setActionType(Action.ActionType.ACTION_SUPPRESS);
    	
    	DateTime.Builder dateTime = DateTime.newBuilder();
    	if(weekDay != -1) {
    		toAddDate = true;
    		dateTime.addDayOfWeek(weekDay);
    	}
    	if((fromHr != -1) && (toHr != -1)) {
    		toAddDate = true;
    		dateTime.setFromHr(fromHr);
    		if(fromMin != -1)
    			dateTime.setFromMin(fromMin);
    		
    		dateTime.setToHr(toHr);
    		if(toMin != -1)
    			dateTime.setToMin(toMin);
    	}
    	if(toAddDate) {
    		rule.setDateTime(dateTime.build());
    	}
    	rule.setAction(action.build());
		configBuilder.addRule(rule.build());
    	//return configBuilder.build();
    }
    
    public void testPassThrough(String pkgName, int uid, int sensorType, String ruleName) {
    	//FirewallConfig.Builder configBuilder = FirewallConfig.newBuilder();
    	Rule.Builder rule = Rule.newBuilder();
    	rule.setRuleName(ruleName);
    	rule.setPkgName(pkgName);
    	rule.setSensorType(sensorType);
    	rule.setPkgUid(uid);
    	
    	Action.Builder action = Action.newBuilder();
    	action.setActionType(Action.ActionType.ACTION_PASSTHROUGH);
    	rule.setAction(action.build());
		configBuilder.addRule(rule.build());
    	//return configBuilder.build();
    	
    }
    
    // hard code the rule here.
    public String getConfig(int num) {   	
    	byte[] bs;		
    	// Default values for time fields is set to -1
    	// Get the pkgName and UID from /data/system/package.xml on the phone
		testSuppression("imoblife.androidsensorbox", 10034, Sensor.TYPE_ACCELEROMETER, "Rule1", -1, -1, -1, -1, -1);
    	//testPassThrough("imoblife.androidsensorbox", 10067, Sensor.TYPE_ACCELEROMETER, "Rule2");
    	for (int i = 1; i < num; i++) {
    		if (i % 2 == 0) {
    			testSuppression("abc.def.ghijklmn.opq.rst.uvwxyz", 10012 + i, Sensor.TYPE_ACCELEROMETER, "Rule" + i, -1, -1, -1, -1, -1);
    		}
    		else {
    			testPassThrough("abc.def.ghijklmn.opq.rst.uvwxyz", 10012 + i, Sensor.TYPE_ACCELEROMETER, "Rule" + i);
    		}
    	}
    	
		bs = configBuilder.build().toByteArray();
		String str = Base64.encodeToString(bs, Base64.DEFAULT);
		//Log.d("In getConfig", str);
		return str;
		
    }
}

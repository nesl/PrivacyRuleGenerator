package edu.ucla.ee.nesl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.os.FirewallConfigManager;
import android.util.Base64;
import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;

import edu.ucla.ee.nesl.FirewallConfigMessages.Action;
import edu.ucla.ee.nesl.FirewallConfigMessages.DateTime;
import edu.ucla.ee.nesl.FirewallConfigMessages.FirewallConfig;
import edu.ucla.ee.nesl.FirewallConfigMessages.Param;
import edu.ucla.ee.nesl.FirewallConfigMessages.Perturb;
import edu.ucla.ee.nesl.FirewallConfigMessages.Perturb.DistributionType;
import edu.ucla.ee.nesl.FirewallConfigMessages.Rule;
import edu.ucla.ee.nesl.FirewallConfigMessages.SensorValue;
import edu.ucla.ee.nesl.FirewallConfigMessages.VectorValue;

public class ActivityMonitor extends BroadcastReceiver {
	public final String TAG = "MainService";
	public static final String actType = "actType";
	private FirewallConfigManager firewallManager;
	private static FirewallConfig.Builder configBuilder = FirewallConfig.newBuilder();
	private static String app_name = "edu.ucla.nesl.mca";//"edu.ucla.cens.ambulation";//"edu.ucla.nesl.mca";
	private Context ctx;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Detected intent " + intent.getAction());
		ctx = context;
		//Toast.makeText(context, "broadcast received", Toast.LENGTH_LONG).show();
		
		if (firewallManager == null) {
			firewallManager = (FirewallConfigManager)context.getSystemService(Context.FIREWALLCONFIG_SERVICE);
		}
		if (intent.getAction().indexOf("TEST_RULE") >= 0) {
			Log.d(TAG, "test writing constant rule");
			String config = getConfig(1);
			Log.d(TAG, "system time=" + System.nanoTime());
			firewallManager.setFirewallConfig(config);
		}
		if (intent.getAction().indexOf("EMPTY_RULE") >= 0) {
			Log.d(TAG, "clear rule");
			String config = emptyConfig();
			firewallManager.setFirewallConfig(config);
		}
		if (intent.getAction().indexOf("BENCHMARK") >= 0) {
			Log.d(TAG, "add benchmark rule");
			String config = benchmarkRules();
			firewallManager.setFirewallConfig(config);
		}
		if (intent.getAction().indexOf("PASSTHRU") >= 0) {
			Log.d(TAG, "add passthru rule");
			String config = setPassThru();
			firewallManager.setFirewallConfig(config);
		}
		if (intent.getAction().indexOf("CONSTANT") >= 0) {
			Log.d(TAG, "add benchmark rule");
			String config = setConstant();
			firewallManager.setFirewallConfig(config);
		}
		if (intent.getAction().indexOf("SUPRESS") >= 0) {
			Log.d(TAG, "add benchmark rule");
			String config = setSupress();
			firewallManager.setFirewallConfig(config);
		}
		if (intent.getAction().indexOf("PERTURB") >= 0) {
			Log.d(TAG, "add benchmark rule");
			String config = setPerturb();
			firewallManager.setFirewallConfig(config);
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
    
    public void testConstant(String pkgName, int uid, int sensorType, String ruleName, float constant) {
    	Rule.Builder rule = Rule.newBuilder();
    	rule.setRuleName(ruleName);
    	rule.setPkgName(pkgName);
    	rule.setSensorType(sensorType);
    	rule.setPkgUid(uid);
    	
    	Action.Builder action = Action.newBuilder();
    	action.setActionType(Action.ActionType.ACTION_CONSTANT);
    	
    	VectorValue.Builder vv = VectorValue.newBuilder();
    	vv.setX(0.1f);
    	vv.setY(0.2f);
    	vv.setZ(10.0f);
    	
    	SensorValue.Builder sv = SensorValue.newBuilder();
    	sv.setVecVal(vv.build());
    	sv.setDefaultVal(constant);
    	
    	Param.Builder param = Param.newBuilder();
    	param.setConstantValue(sv.build());
    	
    	action.setParam(param.build());
    	rule.setAction(action.build());
    	configBuilder.addRule(rule.build()); 	
    }
    
    public void testPerturb(String pkgName, int uid, int sensorType, String ruleName) {
    	Rule.Builder rule = Rule.newBuilder();
    	rule.setRuleName(ruleName);
    	rule.setPkgName(pkgName);
    	rule.setSensorType(sensorType);
    	rule.setPkgUid(uid);
    	
    	Action.Builder action = Action.newBuilder();
    	action.setActionType(Action.ActionType.ACTION_PERTURB);
    	
    	Perturb.Builder p = Perturb.newBuilder();
    	p.setMean(1.0f);
    	p.setVariance(0.5f);
    	p.setUnifMax(10.0f);
    	p.setUnifMin(-10.0f);
    	p.setDistType(DistributionType.GAUSSIAN);
    	
    	Param.Builder param = Param.newBuilder();
    	param.setPerturb(p.build());
    	
    	action.setParam(param.build());
    	rule.setAction(action.build());
    	configBuilder.addRule(rule.build());
    	
    }
    
    // hard code the rule here.
    public String getConfig(int num) {
    	configBuilder = FirewallConfig.newBuilder();
    	byte[] bs;		
    	// Default values for time fields is set to -1
    	// Get the pkgName and UID from /data/system/package.xml on the phone
    	PackageManager pm = ctx.getPackageManager();
    	ApplicationInfo app  = null;
    	ApplicationInfo app1 = null;
    	try {
			app = pm.getApplicationInfo("edu.ucla.nesl.sensorfirewall", PackageManager.GET_META_DATA);
			app1 = pm.getApplicationInfo("imoblife.androidsensorbox", PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	for (int i = 0; i < num - 1; i++) {
    		testConstant("imoblife.androidsensorbox." + i, app1.uid, Sensor.TYPE_ACCELEROMETER, "Rule" + i, -10);
    	}
    	
		testConstant("edu.ucla.nesl.sensorfirewall", app.uid, Sensor.TYPE_ACCELEROMETER, "RuleA", -2);
    	
		bs = configBuilder.build().toByteArray();
		String str = Base64.encodeToString(bs, Base64.DEFAULT);
		//Log.d("In getConfig", str);
		return str;
		
    }
    
    public String emptyConfig() {
    	configBuilder = FirewallConfig.newBuilder();
    	PackageManager pm = ctx.getPackageManager();
    	ApplicationInfo app  = null;
    	try {
			app = pm.getApplicationInfo("edu.ucla.cens.ambulation", PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	testPassThrough("edu.ucla.cens.ambulation", app.uid, 1, "EmptyRule");
    	//testConstant("edu.ucla.cens.ambulation", app.uid, 1, "Rule" + 1, 1);
    	//testPerturb("edu.ucla.cens.ambulation", app.uid, 1, "Rule" + 1);
    	//testSuppression("edu.ucla.cens.ambulation", app.uid, 1, "rule1", -1, -1, -1, -1, -1);
    	byte[] bs = configBuilder.build().toByteArray();
    	String str = Base64.encodeToString(bs, Base64.DEFAULT);
		return str;
    }
    
    public String benchmarkRules() {
    	configBuilder = FirewallConfig.newBuilder();
    	ApplicationInfo app  = null;
    	PackageManager pm = ctx.getPackageManager();
    	try {
			app = pm.getApplicationInfo("edu.ucla.ee.nesl.privacyfilter.sensordump", PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	testConstant("edu.ucla.ee.nesl.privacyfilter.sensordump", app.uid, 1, "Rule" + 1, -1);
    	testConstant("edu.ucla.ee.nesl.privacyfilter.sensordump", app.uid, 2, "Rule" + 2, -2);
    	testConstant("edu.ucla.ee.nesl.privacyfilter.sensordump", app.uid, 3, "Rule" + 3, -3);
    	testConstant("edu.ucla.ee.nesl.privacyfilter.sensordump", app.uid, 4, "Rule" + 4, -4);
    	testConstant("edu.ucla.ee.nesl.privacyfilter.sensordump", app.uid, 6, "Rule" + 6, -6);
    	testConstant("edu.ucla.ee.nesl.privacyfilter.sensordump", app.uid, 9, "Rule" + 9, -9);
    	testConstant("edu.ucla.ee.nesl.privacyfilter.sensordump", app.uid, 10, "Rule" + 10, -10);
    	testConstant("edu.ucla.ee.nesl.privacyfilter.sensordump", app.uid, 11, "Rule" + 11, -11);
    	byte[] bs = configBuilder.build().toByteArray();
    	String str = Base64.encodeToString(bs, Base64.DEFAULT);
		return str;
    }
    
    public String setPassThru() {
    	configBuilder = FirewallConfig.newBuilder();
    	PackageManager pm = ctx.getPackageManager();
    	ApplicationInfo app  = null;
    	try {
			app = pm.getApplicationInfo(app_name, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	testPassThrough(app_name, app.uid, 1, "EmptyRule");
    	//testConstant("edu.ucla.cens.ambulation", app.uid, 1, "Rule" + 1, 1);
    	//testPerturb("edu.ucla.cens.ambulation", app.uid, 1, "Rule" + 1);
    	//testSuppression("edu.ucla.cens.ambulation", app.uid, 1, "rule1", -1, -1, -1, -1, -1);
    	byte[] bs = configBuilder.build().toByteArray();
    	String str = Base64.encodeToString(bs, Base64.DEFAULT);
		return str;
    }
    
    public String setConstant() {
    	configBuilder = FirewallConfig.newBuilder();
    	PackageManager pm = ctx.getPackageManager();
    	ApplicationInfo app  = null;
    	try {
			app = pm.getApplicationInfo(app_name, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	//testPassThrough("edu.ucla.cens.ambulation", app.uid, 1, "EmptyRule");
    	testConstant(app_name, app.uid, 1, "Rule" + 1, 1);
    	//testPerturb("edu.ucla.cens.ambulation", app.uid, 1, "Rule" + 1);
    	//testSuppression("edu.ucla.cens.ambulation", app.uid, 1, "rule1", -1, -1, -1, -1, -1);
    	byte[] bs = configBuilder.build().toByteArray();
    	String str = Base64.encodeToString(bs, Base64.DEFAULT);
		return str;
    }
    
    public String setSupress() {
    	configBuilder = FirewallConfig.newBuilder();
    	PackageManager pm = ctx.getPackageManager();
    	ApplicationInfo app  = null;
    	try {
			app = pm.getApplicationInfo(app_name, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	//testPassThrough("edu.ucla.cens.ambulation", app.uid, 1, "EmptyRule");
    	//testConstant("edu.ucla.cens.ambulation", app.uid, 1, "Rule" + 1, 1);
    	//testPerturb("edu.ucla.cens.ambulation", app.uid, 1, "Rule" + 1);
    	testSuppression(app_name, app.uid, 1, "rule1", -1, -1, -1, -1, -1);
    	byte[] bs = configBuilder.build().toByteArray();
    	String str = Base64.encodeToString(bs, Base64.DEFAULT);
		return str;
    }
    
    public String setPerturb() {
    	configBuilder = FirewallConfig.newBuilder();
    	PackageManager pm = ctx.getPackageManager();
    	ApplicationInfo app  = null;
    	try {
			app = pm.getApplicationInfo(app_name, PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
    	//testPassThrough("edu.ucla.cens.ambulation", app.uid, 1, "EmptyRule");
    	//testConstant("edu.ucla.cens.ambulation", app.uid, 1, "Rule" + 1, 1);
    	testPerturb(app_name, app.uid, 1, "Rule" + 1);
    	//testSuppression("edu.ucla.cens.ambulation", app.uid, 1, "rule1", -1, -1, -1, -1, -1);
    	byte[] bs = configBuilder.build().toByteArray();
    	String str = Base64.encodeToString(bs, Base64.DEFAULT);
		return str;
    }
}

package b4a.example;


import anywheresoftware.b4a.B4AMenuItem;
import android.app.Activity;
import android.os.Bundle;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BALayout;
import anywheresoftware.b4a.B4AActivity;
import anywheresoftware.b4a.ObjectWrapper;
import anywheresoftware.b4a.objects.ActivityWrapper;
import java.lang.reflect.InvocationTargetException;
import anywheresoftware.b4a.B4AUncaughtException;
import anywheresoftware.b4a.debug.*;
import java.lang.ref.WeakReference;

public class main extends Activity implements B4AActivity{
	public static main mostCurrent;
	static boolean afterFirstLayout;
	static boolean isFirst = true;
    private static boolean processGlobalsRun = false;
	BALayout layout;
	public static BA processBA;
	BA activityBA;
    ActivityWrapper _activity;
    java.util.ArrayList<B4AMenuItem> menuItems;
	public static final boolean fullScreen = true;
	public static final boolean includeTitle = true;
    public static WeakReference<Activity> previousOne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mostCurrent = this;
		if (processBA == null) {
			processBA = new BA(this.getApplicationContext(), null, null, "b4a.example", "b4a.example.main");
			processBA.loadHtSubs(this.getClass());
	        float deviceScale = getApplicationContext().getResources().getDisplayMetrics().density;
	        BALayout.setDeviceScale(deviceScale);
            
		}
		else if (previousOne != null) {
			Activity p = previousOne.get();
			if (p != null && p != this) {
                BA.LogInfo("Killing previous instance (main).");
				p.finish();
			}
		}
        processBA.setActivityPaused(true);
        processBA.runHook("oncreate", this, null);
		if (!includeTitle) {
        	this.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
        }
        if (fullScreen) {
        	getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,   
        			android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
		
        processBA.sharedProcessBA.activityBA = null;
		layout = new BALayout(this);
		setContentView(layout);
		afterFirstLayout = false;
        WaitForLayout wl = new WaitForLayout();
        if (anywheresoftware.b4a.objects.ServiceHelper.StarterHelper.startFromActivity(this, processBA, wl, true))
		    BA.handler.postDelayed(wl, 5);

	}
	static class WaitForLayout implements Runnable {
		public void run() {
			if (afterFirstLayout)
				return;
			if (mostCurrent == null)
				return;
            
			if (mostCurrent.layout.getWidth() == 0) {
				BA.handler.postDelayed(this, 5);
				return;
			}
			mostCurrent.layout.getLayoutParams().height = mostCurrent.layout.getHeight();
			mostCurrent.layout.getLayoutParams().width = mostCurrent.layout.getWidth();
			afterFirstLayout = true;
			mostCurrent.afterFirstLayout();
		}
	}
	private void afterFirstLayout() {
        if (this != mostCurrent)
			return;
		activityBA = new BA(this, layout, processBA, "b4a.example", "b4a.example.main");
        
        processBA.sharedProcessBA.activityBA = new java.lang.ref.WeakReference<BA>(activityBA);
        anywheresoftware.b4a.objects.ViewWrapper.lastId = 0;
        _activity = new ActivityWrapper(activityBA, "activity");
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (BA.isShellModeRuntimeCheck(processBA)) {
			if (isFirst)
				processBA.raiseEvent2(null, true, "SHELL", false);
			processBA.raiseEvent2(null, true, "CREATE", true, "b4a.example.main", processBA, activityBA, _activity, anywheresoftware.b4a.keywords.Common.Density, mostCurrent);
			_activity.reinitializeForShell(activityBA, "activity");
		}
        initializeProcessGlobals();		
        initializeGlobals();
        
        BA.LogInfo("** Activity (main) Create, isFirst = " + isFirst + " **");
        processBA.raiseEvent2(null, true, "activity_create", false, isFirst);
		isFirst = false;
		if (this != mostCurrent)
			return;
        processBA.setActivityPaused(false);
        BA.LogInfo("** Activity (main) Resume **");
        processBA.raiseEvent(null, "activity_resume");
        if (android.os.Build.VERSION.SDK_INT >= 11) {
			try {
				android.app.Activity.class.getMethod("invalidateOptionsMenu").invoke(this,(Object[]) null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	public void addMenuItem(B4AMenuItem item) {
		if (menuItems == null)
			menuItems = new java.util.ArrayList<B4AMenuItem>();
		menuItems.add(item);
	}
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		super.onCreateOptionsMenu(menu);
        try {
            if (processBA.subExists("activity_actionbarhomeclick")) {
                Class.forName("android.app.ActionBar").getMethod("setHomeButtonEnabled", boolean.class).invoke(
                    getClass().getMethod("getActionBar").invoke(this), true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (processBA.runHook("oncreateoptionsmenu", this, new Object[] {menu}))
            return true;
		if (menuItems == null)
			return false;
		for (B4AMenuItem bmi : menuItems) {
			android.view.MenuItem mi = menu.add(bmi.title);
			if (bmi.drawable != null)
				mi.setIcon(bmi.drawable);
            if (android.os.Build.VERSION.SDK_INT >= 11) {
				try {
                    if (bmi.addToBar) {
				        android.view.MenuItem.class.getMethod("setShowAsAction", int.class).invoke(mi, 1);
                    }
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			mi.setOnMenuItemClickListener(new B4AMenuItemsClickListener(bmi.eventName.toLowerCase(BA.cul)));
		}
        
		return true;
	}   
 @Override
 public boolean onOptionsItemSelected(android.view.MenuItem item) {
    if (item.getItemId() == 16908332) {
        processBA.raiseEvent(null, "activity_actionbarhomeclick");
        return true;
    }
    else
        return super.onOptionsItemSelected(item); 
}
@Override
 public boolean onPrepareOptionsMenu(android.view.Menu menu) {
    super.onPrepareOptionsMenu(menu);
    processBA.runHook("onprepareoptionsmenu", this, new Object[] {menu});
    return true;
    
 }
 protected void onStart() {
    super.onStart();
    processBA.runHook("onstart", this, null);
}
 protected void onStop() {
    super.onStop();
    processBA.runHook("onstop", this, null);
}
    public void onWindowFocusChanged(boolean hasFocus) {
       super.onWindowFocusChanged(hasFocus);
       if (processBA.subExists("activity_windowfocuschanged"))
           processBA.raiseEvent2(null, true, "activity_windowfocuschanged", false, hasFocus);
    }
	private class B4AMenuItemsClickListener implements android.view.MenuItem.OnMenuItemClickListener {
		private final String eventName;
		public B4AMenuItemsClickListener(String eventName) {
			this.eventName = eventName;
		}
		public boolean onMenuItemClick(android.view.MenuItem item) {
			processBA.raiseEventFromUI(item.getTitle(), eventName + "_click");
			return true;
		}
	}
    public static Class<?> getObject() {
		return main.class;
	}
    private Boolean onKeySubExist = null;
    private Boolean onKeyUpSubExist = null;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeydown", this, new Object[] {keyCode, event}))
            return true;
		if (onKeySubExist == null)
			onKeySubExist = processBA.subExists("activity_keypress");
		if (onKeySubExist) {
			if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK &&
					android.os.Build.VERSION.SDK_INT >= 18) {
				HandleKeyDelayed hk = new HandleKeyDelayed();
				hk.kc = keyCode;
				BA.handler.post(hk);
				return true;
			}
			else {
				boolean res = new HandleKeyDelayed().runDirectly(keyCode);
				if (res)
					return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	private class HandleKeyDelayed implements Runnable {
		int kc;
		public void run() {
			runDirectly(kc);
		}
		public boolean runDirectly(int keyCode) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keypress", false, keyCode);
			if (res == null || res == true) {
                return true;
            }
            else if (keyCode == anywheresoftware.b4a.keywords.constants.KeyCodes.KEYCODE_BACK) {
				finish();
				return true;
			}
            return false;
		}
		
	}
    @Override
	public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        if (processBA.runHook("onkeyup", this, new Object[] {keyCode, event}))
            return true;
		if (onKeyUpSubExist == null)
			onKeyUpSubExist = processBA.subExists("activity_keyup");
		if (onKeyUpSubExist) {
			Boolean res =  (Boolean)processBA.raiseEvent2(_activity, false, "activity_keyup", false, keyCode);
			if (res == null || res == true)
				return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public void onNewIntent(android.content.Intent intent) {
        super.onNewIntent(intent);
		this.setIntent(intent);
        processBA.runHook("onnewintent", this, new Object[] {intent});
	}
    @Override 
	public void onPause() {
		super.onPause();
        if (_activity == null)
            return;
        if (this != mostCurrent)
			return;
		anywheresoftware.b4a.Msgbox.dismiss(true);
        BA.LogInfo("** Activity (main) Pause, UserClosed = " + activityBA.activity.isFinishing() + " **");
        if (mostCurrent != null)
            processBA.raiseEvent2(_activity, true, "activity_pause", false, activityBA.activity.isFinishing());		
        processBA.setActivityPaused(true);
        mostCurrent = null;
        if (!activityBA.activity.isFinishing())
			previousOne = new WeakReference<Activity>(this);
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        processBA.runHook("onpause", this, null);
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
		previousOne = null;
        processBA.runHook("ondestroy", this, null);
	}
    @Override 
	public void onResume() {
		super.onResume();
        mostCurrent = this;
        anywheresoftware.b4a.Msgbox.isDismissing = false;
        if (activityBA != null) { //will be null during activity create (which waits for AfterLayout).
        	ResumeMessage rm = new ResumeMessage(mostCurrent);
        	BA.handler.post(rm);
        }
        processBA.runHook("onresume", this, null);
	}
    private static class ResumeMessage implements Runnable {
    	private final WeakReference<Activity> activity;
    	public ResumeMessage(Activity activity) {
    		this.activity = new WeakReference<Activity>(activity);
    	}
		public void run() {
            main mc = mostCurrent;
			if (mc == null || mc != activity.get())
				return;
			processBA.setActivityPaused(false);
            BA.LogInfo("** Activity (main) Resume **");
            if (mc != mostCurrent)
                return;
		    processBA.raiseEvent(mc._activity, "activity_resume", (Object[])null);
		}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
	      android.content.Intent data) {
		processBA.onActivityResult(requestCode, resultCode, data);
        processBA.runHook("onactivityresult", this, new Object[] {requestCode, resultCode});
	}
	private static void initializeGlobals() {
		processBA.raiseEvent2(null, true, "globals", false, (Object[])null);
	}
    public void onRequestPermissionsResult(int requestCode,
        String permissions[], int[] grantResults) {
        for (int i = 0;i < permissions.length;i++) {
            Object[] o = new Object[] {permissions[i], grantResults[i] == 0};
            processBA.raiseEventFromDifferentThread(null,null, 0, "activity_permissionresult", true, o);
        }
            
    }

public anywheresoftware.b4a.keywords.Common __c = null;
public static anywheresoftware.b4a.objects.Timer _reloj = null;
public static anywheresoftware.b4a.objects.Timer _timer1 = null;
public static anywheresoftware.b4a.ioio.B4AIOIO _yoyo = null;
public static anywheresoftware.b4a.ioio.B4AIOIO.B4AAnalogueInputWrapper _sensor = null;
public static anywheresoftware.b4a.ioio.B4AIOIO.B4APwmOutputWrapper _led_pin3 = null;
public static anywheresoftware.b4a.sql.SQL _poten = null;
public static anywheresoftware.b4a.sql.SQL.CursorWrapper _mi_cursor = null;
public static int _finish = 0;
public static float _valoranalogo = 0f;
public static int _rando = 0;
public anywheresoftware.b4a.objects.LabelWrapper _label1 = null;
public anywheresoftware.b4a.objects.ProgressBarWrapper _barraprogreso = null;
public static int _progreso = 0;
public anywheresoftware.b4a.objects.Serial.BluetoothAdmin _btadmin = null;
public anywheresoftware.b4a.objects.ListViewWrapper _listview1 = null;
public anywheresoftware.b4a.objects.LabelWrapper _label2 = null;
public anywheresoftware.b4a.objects.ButtonWrapper _button1 = null;
public anywheresoftware.b4a.objects.TabHostWrapper _tabhost1 = null;
public anywheresoftware.b4a.objects.LabelWrapper _label4 = null;
public anywheresoftware.b4a.objects.PanelWrapper _panel1 = null;
public anywheresoftware.b4a.objects.drawable.CanvasWrapper _canvas1 = null;
public static float _x1 = 0f;
public static float _y1 = 0f;
public static float _x2 = 0f;
public static float _y2 = 0f;
public b4a.example.grafico _grafico = null;

public static boolean isAnyActivityVisible() {
    boolean vis = false;
vis = vis | (main.mostCurrent != null);
vis = vis | (grafico.mostCurrent != null);
return vis;}
public static String  _activity_create(boolean _firsttime) throws Exception{
 //BA.debugLineNum = 57;BA.debugLine="Sub Activity_Create(FirstTime As Boolean)";
 //BA.debugLineNum = 60;BA.debugLine="Activity.LoadLayout(\"Layout_01\")";
mostCurrent._activity.LoadLayout("Layout_01",mostCurrent.activityBA);
 //BA.debugLineNum = 63;BA.debugLine="Panel1.Initialize(\"Panel1\")";
mostCurrent._panel1.Initialize(mostCurrent.activityBA,"Panel1");
 //BA.debugLineNum = 64;BA.debugLine="Activity.AddView(Panel1, 0, 0, 100%x, 50%y)";
mostCurrent._activity.AddView((android.view.View)(mostCurrent._panel1.getObject()),(int) (0),(int) (0),anywheresoftware.b4a.keywords.Common.PerXToCurrent((float) (100),mostCurrent.activityBA),anywheresoftware.b4a.keywords.Common.PerYToCurrent((float) (50),mostCurrent.activityBA));
 //BA.debugLineNum = 65;BA.debugLine="Canvas1.Initialize(Panel1)";
mostCurrent._canvas1.Initialize((android.view.View)(mostCurrent._panel1.getObject()));
 //BA.debugLineNum = 66;BA.debugLine="x1 = 100dip";
_x1 = (float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (100)));
 //BA.debugLineNum = 67;BA.debugLine="y1 = 10dip";
_y1 = (float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (10)));
 //BA.debugLineNum = 68;BA.debugLine="x2 = 150dip";
_x2 = (float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (150)));
 //BA.debugLineNum = 69;BA.debugLine="y2 = 20dip";
_y2 = (float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (20)));
 //BA.debugLineNum = 71;BA.debugLine="y1 = 30dip";
_y1 = (float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (30)));
 //BA.debugLineNum = 72;BA.debugLine="y2 = 30dip";
_y2 = (float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (30)));
 //BA.debugLineNum = 73;BA.debugLine="Canvas1.DrawLine(100, 700, 1000,700, Colors.Black";
mostCurrent._canvas1.DrawLine((float) (100),(float) (700),(float) (1000),(float) (700),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (2))));
 //BA.debugLineNum = 75;BA.debugLine="Canvas1.DrawLine(100, 700, 100,100, Colors.Black,";
mostCurrent._canvas1.DrawLine((float) (100),(float) (700),(float) (100),(float) (100),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (2))));
 //BA.debugLineNum = 77;BA.debugLine="Canvas1.DrawLine(100, 640, 1000,640, Colors.Black";
mostCurrent._canvas1.DrawLine((float) (100),(float) (640),(float) (1000),(float) (640),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 78;BA.debugLine="Canvas1.DrawLine(100, 580, 1000,580, Colors.Black";
mostCurrent._canvas1.DrawLine((float) (100),(float) (580),(float) (1000),(float) (580),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 79;BA.debugLine="Canvas1.DrawLine(100, 520, 1000,520, Colors.Black";
mostCurrent._canvas1.DrawLine((float) (100),(float) (520),(float) (1000),(float) (520),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 80;BA.debugLine="Canvas1.DrawLine(100, 460, 1000,460, Colors.Black";
mostCurrent._canvas1.DrawLine((float) (100),(float) (460),(float) (1000),(float) (460),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 81;BA.debugLine="Canvas1.DrawLine(100, 400, 1000,400, Colors.Black";
mostCurrent._canvas1.DrawLine((float) (100),(float) (400),(float) (1000),(float) (400),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 82;BA.debugLine="Canvas1.DrawLine(100, 340, 1000,340, Colors.Black";
mostCurrent._canvas1.DrawLine((float) (100),(float) (340),(float) (1000),(float) (340),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 83;BA.debugLine="Canvas1.DrawLine(100, 280, 1000,280, Colors.Black";
mostCurrent._canvas1.DrawLine((float) (100),(float) (280),(float) (1000),(float) (280),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 84;BA.debugLine="Canvas1.DrawLine(100, 220, 1000,220, Colors.Black";
mostCurrent._canvas1.DrawLine((float) (100),(float) (220),(float) (1000),(float) (220),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 85;BA.debugLine="Canvas1.DrawLine(100, 160, 1000,160, Colors.Black";
mostCurrent._canvas1.DrawLine((float) (100),(float) (160),(float) (1000),(float) (160),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 86;BA.debugLine="Canvas1.DrawLine(100, 100, 1000,100, Colors.Black";
mostCurrent._canvas1.DrawLine((float) (100),(float) (100),(float) (1000),(float) (100),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 89;BA.debugLine="Canvas1.DrawLine(190, 700, 190,100, Colors.Black,";
mostCurrent._canvas1.DrawLine((float) (190),(float) (700),(float) (190),(float) (100),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 90;BA.debugLine="Canvas1.DrawLine(280, 700, 280,100, Colors.Black,";
mostCurrent._canvas1.DrawLine((float) (280),(float) (700),(float) (280),(float) (100),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 91;BA.debugLine="Canvas1.DrawLine(370, 700, 370,100, Colors.Black,";
mostCurrent._canvas1.DrawLine((float) (370),(float) (700),(float) (370),(float) (100),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 92;BA.debugLine="Canvas1.DrawLine(460, 700, 460,100, Colors.Black,";
mostCurrent._canvas1.DrawLine((float) (460),(float) (700),(float) (460),(float) (100),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 93;BA.debugLine="Canvas1.DrawLine(540, 700, 540,100, Colors.Black,";
mostCurrent._canvas1.DrawLine((float) (540),(float) (700),(float) (540),(float) (100),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 94;BA.debugLine="Canvas1.DrawLine(630, 700, 630,100, Colors.Black,";
mostCurrent._canvas1.DrawLine((float) (630),(float) (700),(float) (630),(float) (100),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 95;BA.debugLine="Canvas1.DrawLine(720, 700, 720,100, Colors.Black,";
mostCurrent._canvas1.DrawLine((float) (720),(float) (700),(float) (720),(float) (100),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 96;BA.debugLine="Canvas1.DrawLine(810, 700, 810,100, Colors.Black,";
mostCurrent._canvas1.DrawLine((float) (810),(float) (700),(float) (810),(float) (100),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 97;BA.debugLine="Canvas1.DrawLine(900, 700, 900,100, Colors.Black,";
mostCurrent._canvas1.DrawLine((float) (900),(float) (700),(float) (900),(float) (100),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 98;BA.debugLine="Canvas1.DrawLine(990, 700, 990,100, Colors.Black,";
mostCurrent._canvas1.DrawLine((float) (990),(float) (700),(float) (990),(float) (100),anywheresoftware.b4a.keywords.Common.Colors.Black,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (0.5))));
 //BA.debugLineNum = 102;BA.debugLine="Canvas1.DrawText(\"0.30\", 140, 750,Typeface.DEFAUL";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"0.30",(float) (140),(float) (750),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 103;BA.debugLine="Canvas1.DrawText(\"1\", 270, 750,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"1",(float) (270),(float) (750),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 104;BA.debugLine="Canvas1.DrawText(\"1.30\", 320, 750,Typeface.DEFAUL";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"1.30",(float) (320),(float) (750),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 105;BA.debugLine="Canvas1.DrawText(\"2\", 450, 750,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"2",(float) (450),(float) (750),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 106;BA.debugLine="Canvas1.DrawText(\"2.30\", 500, 750,Typeface.DEFAUL";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"2.30",(float) (500),(float) (750),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 107;BA.debugLine="Canvas1.DrawText(\"3\", 620, 750,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"3",(float) (620),(float) (750),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 108;BA.debugLine="Canvas1.DrawText(\"3.30\", 680, 750,Typeface.DEFAUL";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"3.30",(float) (680),(float) (750),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 109;BA.debugLine="Canvas1.DrawText(\"4\", 800, 750,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"4",(float) (800),(float) (750),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 110;BA.debugLine="Canvas1.DrawText(\"4.30\", 860, 750,Typeface.DEFAUL";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"4.30",(float) (860),(float) (750),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 111;BA.debugLine="Canvas1.DrawText(\"5\", 990, 750,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"5",(float) (990),(float) (750),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 113;BA.debugLine="Canvas1.DrawText(\"10\", 40, 660,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"10",(float) (40),(float) (660),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 114;BA.debugLine="Canvas1.DrawText(\"20\", 40, 600,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"20",(float) (40),(float) (600),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 115;BA.debugLine="Canvas1.DrawText(\"30\", 40, 540,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"30",(float) (40),(float) (540),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 116;BA.debugLine="Canvas1.DrawText(\"40\", 40, 480,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"40",(float) (40),(float) (480),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 117;BA.debugLine="Canvas1.DrawText(\"50\", 40, 420,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"50",(float) (40),(float) (420),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 118;BA.debugLine="Canvas1.DrawText(\"60\", 40, 360,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"60",(float) (40),(float) (360),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 119;BA.debugLine="Canvas1.DrawText(\"70\", 40, 300,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"70",(float) (40),(float) (300),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 120;BA.debugLine="Canvas1.DrawText(\"80\", 40, 240,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"80",(float) (40),(float) (240),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 121;BA.debugLine="Canvas1.DrawText(\"90\", 40, 180,Typeface.DEFAULT ,";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"90",(float) (40),(float) (180),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 122;BA.debugLine="Canvas1.DrawText(\"100\", 30, 120,Typeface.DEFAULT";
mostCurrent._canvas1.DrawText(mostCurrent.activityBA,"100",(float) (30),(float) (120),anywheresoftware.b4a.keywords.Common.Typeface.DEFAULT,(float) (14),anywheresoftware.b4a.keywords.Common.Colors.Black,BA.getEnumFromString(android.graphics.Paint.Align.class,"LEFT"));
 //BA.debugLineNum = 126;BA.debugLine="If FirstTime Then";
if (_firsttime) { 
 //BA.debugLineNum = 127;BA.debugLine="poten.Initialize(File.DirInternal,\"puntos.db\", T";
_poten.Initialize(anywheresoftware.b4a.keywords.Common.File.getDirInternal(),"puntos.db",anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 128;BA.debugLine="poten.BeginTransaction";
_poten.BeginTransaction();
 //BA.debugLineNum = 129;BA.debugLine="Try";
try { //BA.debugLineNum = 130;BA.debugLine="poten.ExecNonQuery(\"DROP TABLE valor\")";
_poten.ExecNonQuery("DROP TABLE valor");
 //BA.debugLineNum = 131;BA.debugLine="poten.ExecNonQuery(\"CREATE TABLE IF NOT EXISTS";
_poten.ExecNonQuery("CREATE TABLE IF NOT EXISTS valor (id_valor INTEGER PRIMARY KEY AUTOINCREMENT, val INTEGER, tiempo INTEGER) ");
 //BA.debugLineNum = 132;BA.debugLine="poten.TransactionSuccessful";
_poten.TransactionSuccessful();
 //BA.debugLineNum = 133;BA.debugLine="ToastMessageShow(\"Crea la base\",True)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToCharSequence("Crea la base"),anywheresoftware.b4a.keywords.Common.True);
 } 
       catch (Exception e62) {
			processBA.setLastException(e62); //BA.debugLineNum = 135;BA.debugLine="Log(\"Error de creacion de base de datos: \"&Last";
anywheresoftware.b4a.keywords.Common.LogImpl("5131150","Error de creacion de base de datos: "+anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA).getMessage(),0);
 };
 //BA.debugLineNum = 137;BA.debugLine="poten.EndTransaction";
_poten.EndTransaction();
 //BA.debugLineNum = 138;BA.debugLine="BtAdmin.Initialize(\"BtAdmin\")";
mostCurrent._btadmin.Initialize(processBA,"BtAdmin");
 //BA.debugLineNum = 139;BA.debugLine="reloj.Initialize(\"reloj\", 100)";
_reloj.Initialize(processBA,"reloj",(long) (100));
 //BA.debugLineNum = 140;BA.debugLine="Conectar_IOIO";
_conectar_ioio();
 };
 //BA.debugLineNum = 142;BA.debugLine="finish = 0";
_finish = (int) (0);
 //BA.debugLineNum = 144;BA.debugLine="Timer1.Initialize(\"Timer1\",1000)";
_timer1.Initialize(processBA,"Timer1",(long) (1000));
 //BA.debugLineNum = 145;BA.debugLine="Timer1.Enabled=True";
_timer1.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 147;BA.debugLine="ListView1.Clear";
mostCurrent._listview1.Clear();
 //BA.debugLineNum = 148;BA.debugLine="End Sub";
return "";
}
public static String  _activity_pause(boolean _userclosed) throws Exception{
 //BA.debugLineNum = 206;BA.debugLine="Sub Activity_Pause (UserClosed As Boolean)";
 //BA.debugLineNum = 208;BA.debugLine="If UserClosed Then";
if (_userclosed) { 
 //BA.debugLineNum = 209;BA.debugLine="Desconectar_IOIO";
_desconectar_ioio();
 };
 //BA.debugLineNum = 211;BA.debugLine="End Sub";
return "";
}
public static String  _activity_resume() throws Exception{
 //BA.debugLineNum = 201;BA.debugLine="Sub Activity_Resume";
 //BA.debugLineNum = 204;BA.debugLine="End Sub";
return "";
}
public static String  _button1_click() throws Exception{
 //BA.debugLineNum = 261;BA.debugLine="Sub Button1_Click";
 //BA.debugLineNum = 262;BA.debugLine="StartActivity(Grafico)";
anywheresoftware.b4a.keywords.Common.StartActivity(processBA,(Object)(mostCurrent._grafico.getObject()));
 //BA.debugLineNum = 263;BA.debugLine="End Sub";
return "";
}
public static String  _conectar_ioio() throws Exception{
 //BA.debugLineNum = 213;BA.debugLine="Sub Conectar_IOIO";
 //BA.debugLineNum = 214;BA.debugLine="YOYO.Initialize";
_yoyo.Initialize();
 //BA.debugLineNum = 215;BA.debugLine="If BtAdmin.IsEnabled Then YOYO.Connect(\"yoyo\",YOY";
if (mostCurrent._btadmin.IsEnabled()) { 
_yoyo.Connect(processBA,"yoyo",_yoyo.CONN_BT,(int) (1),(int) (0));};
 //BA.debugLineNum = 217;BA.debugLine="End Sub";
return "";
}
public static String  _conexion_pines() throws Exception{
 //BA.debugLineNum = 234;BA.debugLine="Sub Conexion_pines";
 //BA.debugLineNum = 235;BA.debugLine="YOYO.OpenAnalogInput(\"potenciometro\",39)";
_yoyo.OpenAnalogInput(processBA,"potenciometro",(int) (39));
 //BA.debugLineNum = 236;BA.debugLine="YOYO.OpenPwmOutput(\"led_pin3\",3,led_pin3.OP_NORMA";
_yoyo.OpenPwmOutput(processBA,"led_pin3",(int) (3),_led_pin3.OP_NORMAL,(int) (50));
 //BA.debugLineNum = 237;BA.debugLine="End Sub";
return "";
}
public static String  _desconectar_ioio() throws Exception{
 //BA.debugLineNum = 219;BA.debugLine="Sub Desconectar_IOIO";
 //BA.debugLineNum = 220;BA.debugLine="sensor.Close";
_sensor.Close();
 //BA.debugLineNum = 221;BA.debugLine="led_pin3.close";
_led_pin3.Close();
 //BA.debugLineNum = 222;BA.debugLine="YOYO.Disconnect";
_yoyo.Disconnect(processBA);
 //BA.debugLineNum = 223;BA.debugLine="End Sub";
return "";
}
public static String  _globals() throws Exception{
 //BA.debugLineNum = 34;BA.debugLine="Sub Globals";
 //BA.debugLineNum = 37;BA.debugLine="Private Label1 As Label";
mostCurrent._label1 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 40;BA.debugLine="Dim barraProgreso As ProgressBar";
mostCurrent._barraprogreso = new anywheresoftware.b4a.objects.ProgressBarWrapper();
 //BA.debugLineNum = 41;BA.debugLine="Dim progreso As Int";
_progreso = 0;
 //BA.debugLineNum = 44;BA.debugLine="Dim BtAdmin As BluetoothAdmin";
mostCurrent._btadmin = new anywheresoftware.b4a.objects.Serial.BluetoothAdmin();
 //BA.debugLineNum = 45;BA.debugLine="Private ListView1 As ListView";
mostCurrent._listview1 = new anywheresoftware.b4a.objects.ListViewWrapper();
 //BA.debugLineNum = 46;BA.debugLine="Private Label2 As Label";
mostCurrent._label2 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 47;BA.debugLine="Private Button1 As Button";
mostCurrent._button1 = new anywheresoftware.b4a.objects.ButtonWrapper();
 //BA.debugLineNum = 48;BA.debugLine="Private TabHost1 As TabHost";
mostCurrent._tabhost1 = new anywheresoftware.b4a.objects.TabHostWrapper();
 //BA.debugLineNum = 49;BA.debugLine="Private Label4 As Label";
mostCurrent._label4 = new anywheresoftware.b4a.objects.LabelWrapper();
 //BA.debugLineNum = 50;BA.debugLine="Dim Panel1 As Panel";
mostCurrent._panel1 = new anywheresoftware.b4a.objects.PanelWrapper();
 //BA.debugLineNum = 51;BA.debugLine="Dim Canvas1 As Canvas";
mostCurrent._canvas1 = new anywheresoftware.b4a.objects.drawable.CanvasWrapper();
 //BA.debugLineNum = 52;BA.debugLine="Dim  x1, y1, x2, y2 As Float";
_x1 = 0f;
_y1 = 0f;
_x2 = 0f;
_y2 = 0f;
 //BA.debugLineNum = 55;BA.debugLine="End Sub";
return "";
}
public static String  _led_pin3_open(boolean _noerror,Object _result) throws Exception{
 //BA.debugLineNum = 238;BA.debugLine="Sub led_pin3_open(noerror As Boolean, result As Ob";
 //BA.debugLineNum = 239;BA.debugLine="If noerror Then";
if (_noerror) { 
 //BA.debugLineNum = 241;BA.debugLine="led_pin3=result";
_led_pin3.setObject((ioio.lib.api.PwmOutput)(_result));
 }else {
 //BA.debugLineNum = 244;BA.debugLine="Log(\"Error: \"&LastException.Message)";
anywheresoftware.b4a.keywords.Common.LogImpl("5720902","Error: "+anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA).getMessage(),0);
 };
 //BA.debugLineNum = 246;BA.debugLine="End Sub";
return "";
}
public static String  _potenciometro_open(boolean _noerror,Object _result) throws Exception{
 //BA.debugLineNum = 248;BA.debugLine="Sub potenciometro_open(noerror As Boolean, result";
 //BA.debugLineNum = 249;BA.debugLine="If noerror Then";
if (_noerror) { 
 //BA.debugLineNum = 250;BA.debugLine="sensor=result";
_sensor.setObject((ioio.lib.api.AnalogInput)(_result));
 //BA.debugLineNum = 251;BA.debugLine="reloj.Enabled=True";
_reloj.setEnabled(anywheresoftware.b4a.keywords.Common.True);
 }else {
 //BA.debugLineNum = 253;BA.debugLine="Log(\"Error: \"&LastException.Message)";
anywheresoftware.b4a.keywords.Common.LogImpl("5786437","Error: "+anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA).getMessage(),0);
 };
 //BA.debugLineNum = 255;BA.debugLine="End Sub";
return "";
}

public static void initializeProcessGlobals() {
    
    if (main.processGlobalsRun == false) {
	    main.processGlobalsRun = true;
		try {
		        main._process_globals();
grafico._process_globals();
		
        } catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
}public static String  _process_globals() throws Exception{
 //BA.debugLineNum = 19;BA.debugLine="Sub Process_Globals";
 //BA.debugLineNum = 22;BA.debugLine="Dim reloj As Timer";
_reloj = new anywheresoftware.b4a.objects.Timer();
 //BA.debugLineNum = 23;BA.debugLine="Dim Timer1 As Timer";
_timer1 = new anywheresoftware.b4a.objects.Timer();
 //BA.debugLineNum = 24;BA.debugLine="Dim YOYO As IOIO";
_yoyo = new anywheresoftware.b4a.ioio.B4AIOIO();
 //BA.debugLineNum = 25;BA.debugLine="Dim sensor As AnalogInput";
_sensor = new anywheresoftware.b4a.ioio.B4AIOIO.B4AAnalogueInputWrapper();
 //BA.debugLineNum = 26;BA.debugLine="Dim led_pin3 As PwmOutput";
_led_pin3 = new anywheresoftware.b4a.ioio.B4AIOIO.B4APwmOutputWrapper();
 //BA.debugLineNum = 27;BA.debugLine="Dim poten As SQL";
_poten = new anywheresoftware.b4a.sql.SQL();
 //BA.debugLineNum = 28;BA.debugLine="Dim Mi_Cursor As Cursor";
_mi_cursor = new anywheresoftware.b4a.sql.SQL.CursorWrapper();
 //BA.debugLineNum = 29;BA.debugLine="Dim finish As Int";
_finish = 0;
 //BA.debugLineNum = 30;BA.debugLine="Dim ValorAnalogo As Float";
_valoranalogo = 0f;
 //BA.debugLineNum = 31;BA.debugLine="Dim rando As Int";
_rando = 0;
 //BA.debugLineNum = 32;BA.debugLine="End Sub";
return "";
}
public static String  _reloj_tick() throws Exception{
 //BA.debugLineNum = 149;BA.debugLine="Sub reloj_Tick";
 //BA.debugLineNum = 150;BA.debugLine="Try";
try { //BA.debugLineNum = 151;BA.debugLine="ValorAnalogo = sensor.Read";
_valoranalogo = _sensor.getRead();
 //BA.debugLineNum = 152;BA.debugLine="progreso = sensor.Read * 100";
_progreso = (int) (_sensor.getRead()*100);
 //BA.debugLineNum = 153;BA.debugLine="barraProgreso.Progress = progreso";
mostCurrent._barraprogreso.setProgress(_progreso);
 //BA.debugLineNum = 154;BA.debugLine="led_pin3.DutyCycle(ValorAnalogo,0)";
_led_pin3.DutyCycle(processBA,_valoranalogo,(int) (0));
 //BA.debugLineNum = 155;BA.debugLine="Label1.Text=ValorAnalogo";
mostCurrent._label1.setText(BA.ObjectToCharSequence(_valoranalogo));
 } 
       catch (Exception e8) {
			processBA.setLastException(e8); //BA.debugLineNum = 158;BA.debugLine="Log(\"Error: \"&LastException.Message)";
anywheresoftware.b4a.keywords.Common.LogImpl("5196617","Error: "+anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA).getMessage(),0);
 //BA.debugLineNum = 159;BA.debugLine="Desconectar_IOIO";
_desconectar_ioio();
 //BA.debugLineNum = 160;BA.debugLine="Conectar_IOIO";
_conectar_ioio();
 };
 //BA.debugLineNum = 163;BA.debugLine="End Sub";
return "";
}
public static String  _timer1_tick() throws Exception{
int _i = 0;
 //BA.debugLineNum = 164;BA.debugLine="Sub Timer1_tick";
 //BA.debugLineNum = 165;BA.debugLine="finish= finish+1";
_finish = (int) (_finish+1);
 //BA.debugLineNum = 166;BA.debugLine="If finish == 10 Or finish == 20 Or finish == 30";
if (_finish==10 || _finish==20 || _finish==30 || _finish==40 || _finish==50 || _finish==60 || _finish==70 || _finish==80 || _finish==90 || _finish==100 || _finish==110 || _finish==120 || _finish==130 || _finish==140 || _finish==150 || _finish==160 || _finish==170 || _finish==180 || _finish==190 || _finish==200 || _finish==210 || _finish==220 || _finish==230 || _finish==240 || _finish==250 || _finish==260 || _finish==270 || _finish==280 || _finish==290 || _finish==300) { 
 //BA.debugLineNum = 167;BA.debugLine="ListView1.Clear";
mostCurrent._listview1.Clear();
 //BA.debugLineNum = 169;BA.debugLine="poten.BeginTransaction";
_poten.BeginTransaction();
 //BA.debugLineNum = 170;BA.debugLine="Try";
try { //BA.debugLineNum = 171;BA.debugLine="poten.ExecNonQuery2(\"INSERT INTO valor (val,ti";
_poten.ExecNonQuery2("INSERT INTO valor (val,tiempo) VALUES (?,?)",anywheresoftware.b4a.keywords.Common.ArrayToList(new String[]{BA.NumberToString(_valoranalogo*100),BA.NumberToString(_finish)}));
 //BA.debugLineNum = 172;BA.debugLine="rando = Rnd(0,256)";
_rando = anywheresoftware.b4a.keywords.Common.Rnd((int) (0),(int) (256));
 //BA.debugLineNum = 173;BA.debugLine="Canvas1.DrawCircle((finish*3+100), -ValorAn";
mostCurrent._canvas1.DrawCircle((float) ((_finish*3+100)),(float) (-_valoranalogo*600+700),(float) (_valoranalogo*100),anywheresoftware.b4a.keywords.Common.Colors.ARGB((int) (170),_rando,_rando,_rando),anywheresoftware.b4a.keywords.Common.True,(float) (anywheresoftware.b4a.keywords.Common.DipToCurrent((int) (1))));
 //BA.debugLineNum = 174;BA.debugLine="poten.TransactionSuccessful";
_poten.TransactionSuccessful();
 } 
       catch (Exception e11) {
			processBA.setLastException(e11); //BA.debugLineNum = 177;BA.debugLine="Log(\"catch: \"&LastException.Message)";
anywheresoftware.b4a.keywords.Common.LogImpl("5262157","catch: "+anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA).getMessage(),0);
 };
 //BA.debugLineNum = 179;BA.debugLine="poten.EndTransaction";
_poten.EndTransaction();
 //BA.debugLineNum = 181;BA.debugLine="Mi_Cursor=poten.ExecQuery(\"SELECT * FROM valor";
_mi_cursor.setObject((android.database.Cursor)(_poten.ExecQuery("SELECT * FROM valor ORDER BY id_valor ASC")));
 //BA.debugLineNum = 182;BA.debugLine="If Mi_Cursor.RowCount>0 Then";
if (_mi_cursor.getRowCount()>0) { 
 //BA.debugLineNum = 183;BA.debugLine="For i = 0 To Mi_Cursor.RowCount-1";
{
final int step16 = 1;
final int limit16 = (int) (_mi_cursor.getRowCount()-1);
_i = (int) (0) ;
for (;_i <= limit16 ;_i = _i + step16 ) {
 //BA.debugLineNum = 184;BA.debugLine="Mi_Cursor.Position=i";
_mi_cursor.setPosition(_i);
 //BA.debugLineNum = 185;BA.debugLine="ListView1.AddSingleLine( \" Valor:  \" & Mi_Cur";
mostCurrent._listview1.AddSingleLine(BA.ObjectToCharSequence(" Valor:  "+_mi_cursor.GetString("val")+" Tiempo: "+_mi_cursor.GetString("tiempo")));
 }
};
 };
 };
 //BA.debugLineNum = 200;BA.debugLine="End Sub";
return "";
}
public static String  _yoyo_connected(boolean _noerror) throws Exception{
 //BA.debugLineNum = 225;BA.debugLine="Sub yoyo_connected(noerror As Boolean)";
 //BA.debugLineNum = 226;BA.debugLine="If noerror Then";
if (_noerror) { 
 //BA.debugLineNum = 227;BA.debugLine="ToastMessageShow(\"IOIO Conectado\",True)";
anywheresoftware.b4a.keywords.Common.ToastMessageShow(BA.ObjectToCharSequence("IOIO Conectado"),anywheresoftware.b4a.keywords.Common.True);
 //BA.debugLineNum = 228;BA.debugLine="Conexion_pines";
_conexion_pines();
 }else {
 //BA.debugLineNum = 230;BA.debugLine="Log(LastException.Message)";
anywheresoftware.b4a.keywords.Common.LogImpl("5589829",anywheresoftware.b4a.keywords.Common.LastException(mostCurrent.activityBA).getMessage(),0);
 };
 //BA.debugLineNum = 232;BA.debugLine="End Sub";
return "";
}
}

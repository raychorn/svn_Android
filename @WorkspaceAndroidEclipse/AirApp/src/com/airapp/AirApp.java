/*
 * Extended Template for Android Air Apps
 * Ver.1
 * 
 * Created by: Rosario Conti
 * Date: Sept.2010
 * Desc: Expanding your Air application using Java
 * 
 * Features: Menu with 2 options 
 * 1) Exit
 * 2) About (Pop up Dialog)
 * 
 * Use: 
 * 1) Copy the new app.xml and text.swf in your asset folder
 * and your icon in resource folder.
 * 2) Publish
 */

package com.airapp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import dalvik.system.DexClassLoader;
import android.content.DialogInterface;
import android.view.View;
import android.view.View.OnClickListener;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.Context;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;


public class AirApp extends Activity implements OnClickListener
{

	
	private static boolean dexLoaded = false;
	private static DexClassLoader dexLoader;
	private static Class<?> androidActWrapperClass;
	private static Object androidActWrapper;
	private static String RUNTIME_PACKAGE_ID = "com.adobe.air";

	private String XmlPath = "/data/data/com.airapp/files/app.xml";
	private String RootDir = "/data/data/com.airapp/files";
	private String ExtraArgs = "-nodebug";

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Log.i("AirApp", "[ Activity CREATED ]");
		setContentView(R.layout.mainros);
		
		try {
			copyAsset("test.swf");
			copyAsset("app.xml");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		SharedPreferences lSharedPrefs = getSharedPreferences("AirApp_prefs", MODE_PRIVATE);
		SharedPreferences.Editor lEditor = lSharedPrefs.edit();
		lEditor.putString("app_xml_path", XmlPath);
		lEditor.putString("app_root_dir", RootDir);
		lEditor.putString("app_extra_args", ExtraArgs);
		lEditor.commit();

		loadDexAndCreateActivityWrapper();
			try 
			{
				Method on_create_method = androidActWrapperClass.getMethod("onCreate", Activity.class, String[].class);
				String args[] = {XmlPath, RootDir , ExtraArgs, "true", "false"};
				InvokeMethod(on_create_method, this, args);
			}
			catch(Exception e)
			{			
				Log.i("ERROR", "Error in launching AIR app" + e);
			}
	}

	private Object InvokeMethod(Method method, Object... args)
	{
		if(!dexLoaded)
			return null;
		Object retval = null;

		try
		{
			if(args != null)
				retval = method.invoke(androidActWrapper, args);
			else
				retval = method.invoke(androidActWrapper);
		}
		catch(Exception e)
		{
			Log.i("ERROR", "error invoking method " + method + " exception = " + e);
		}
		return retval;
	}

	@Override
	public void onStart()
	{   
		super.onStart();
		Log.i("", "[ Activity STARTED ]");
		
		try
		{
			if ( dexLoaded )
			{
				Method method = androidActWrapperClass.getMethod("onRestart");
				InvokeMethod(method);
				Log.i("", "[ Activity RESTARTED ]");
			}
		}
		catch(Exception e)
		{
			Log.i("ERROR", "error invoking method onRestart exception = " + e);
		}
	}   

	@Override
	public void onRestart()
	{   
		super.onRestart();
		

		try
		{
			if ( dexLoaded )
			{
				Method method = androidActWrapperClass.getMethod("onRestart");
				InvokeMethod(method);
				Log.i("", "[ Activity RESTARTED ]");
			}
		}
		catch(Exception e)
		{
			Log.i("ERROR", "error invoking method onRestart exception = " + e);
		}
		
	}   

	@Override
	public void onPause()
	{
		super.onPause();
		try
		{
			if ( dexLoaded )
			{
				Method method = androidActWrapperClass.getMethod("onPause");
				InvokeMethod(method);
			}
		}
		catch(Exception e)
		{
			Log.i("ERROR", "error invoking method onPause exception = " + e);
		}
	}

	@Override
	public void onResume()
	{
		super.onResume();
		if ( dexLoaded ==false ){
		
			loadDexAndCreateActivityWrapper();
			try 
			{
				Method on_create_method = androidActWrapperClass.getMethod("onCreate", Activity.class, String[].class);
				String args[] = {XmlPath, RootDir , ExtraArgs, "true", "false"};
				InvokeMethod(on_create_method, this, args);
			}
			catch(Exception e)
			{			
				Log.i("ERROR", "Error in launching AIR app" + e);
			}
		}
		
			
	}


 
	@Override
	public void onConfigurationChanged (Configuration newConfig)
     	{
		super.onConfigurationChanged(newConfig);

		try
		{
			Method method = androidActWrapperClass.getMethod("onConfigurationChanged", Configuration.class);
			InvokeMethod(method, newConfig);
			Log.i("" , "[ onConfigurationChanged ]");
		}
		catch(Exception e)
		{
			Log.i("ERROR", "error invoking method onConfigurationChanged exception = " + e); 
		}
	}

	private void loadDexAndCreateActivityWrapper()
	{
		try 
		{
			
			if(!dexLoaded)
			{
				Context con = createPackageContext(RUNTIME_PACKAGE_ID, CONTEXT_INCLUDE_CODE|CONTEXT_IGNORE_SECURITY);
				
				dexLoader = new DexClassLoader(RUNTIME_PACKAGE_ID, 
						getFilesDir().getAbsolutePath(),
						null,
						con.getClassLoader());

				Log.i("[Dex Loader]", "Loading dex");
				androidActWrapperClass = dexLoader.loadClass("com.adobe.air.AndroidActivityWrapper");
				
				if(androidActWrapperClass != null)
					dexLoaded = true;
			}

			Method methodCreateAndroidActivityWrapper = androidActWrapperClass.getMethod("CreateAndroidActivityWrapper", Activity.class);
			Log.i("[Dex Loader]", "Invoking CreateAndroidActivityWrapper");
			androidActWrapper = methodCreateAndroidActivityWrapper.invoke(null, this);
		}
		catch(Exception e)
		{	
			Log.i("[Dex Loader]", "Error in loading dex and creating activity wrapper- " + e);
		}
	}
	
	
    private void copyAsset(String filename) throws IOException{
    	InputStream myInput = this.getAssets().open(filename);
    	String outFileName = RootDir + "/" + filename;
    	OutputStream myOutput = new FileOutputStream(outFileName);
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
    }
	//-----------------------------------------------------------------------------------------------------------------------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		boolean retval = super.onCreateOptionsMenu(menu);

		int ABOUT_ID = 3;
		int EXIT_ID = 4;
		int PREFERENCES_GROUP_ID = 0;

		menu.add(PREFERENCES_GROUP_ID, EXIT_ID, 0, R.string.menu_exit).setIcon(R.drawable.exitbn); //btn_dialog  / ic_delete
		menu.add(PREFERENCES_GROUP_ID, ABOUT_ID, 0, R.string.menu_about).setIcon(android.R.drawable.ic_menu_info_details);
		
		try
		{
			Method method = androidActWrapperClass.getMethod("onCreateOptionsMenu", Menu.class, boolean.class);
			return (Boolean)InvokeMethod(method, menu, retval);
		}
		catch(Exception e)
		{
			Log.e("ERROR", "error invoking method onCreateOptionsMenu exception = " + e);
			return retval;
		}
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		boolean retval = super.onKeyDown(keyCode, event);
		
	    if (keyCode == KeyEvent.KEYCODE_BACK ) {
	    	moveTaskToBack(true);
	    	return true;
	    }

	    return retval;
	}

	@Override  
	public boolean onOptionsItemSelected(MenuItem item)
	{
		boolean retval = super.onOptionsItemSelected(item);

		switch(item.getItemId()){
			case 3:
				/** About */
				popUp("[AirApp]", getString(R.string.message));
			    break;
			case 4:
				/** Exit */
				System.exit(RESULT_OK);
			    break;
		}

		return retval;
	}

	
    public void popUp(final String title, final String msg){
       AlertDialog.Builder builder;

       builder = new AlertDialog.Builder(this);
 	   builder.setTitle(title);
 	   builder.setMessage(msg)
	          .setCancelable(true)
	          .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
	              public void onClick(DialogInterface dialog, int id) {
	                   dialog.dismiss();
	              }
	          });

 	   AlertDialog alert = builder.create();
 	   alert.show();
 	
 }

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		
	}	
	
}


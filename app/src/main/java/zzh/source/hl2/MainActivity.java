package zzh.source.hl2;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.Handler;
import android.app.Dialog;
import android.content.DialogInterface;
import java.lang.Thread;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.util.Linkify;
import android.util.Log;
import android.widget.LinearLayout.LayoutParams;
import android.view.*;
import android.widget.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;

import eightbitlab.com.blurview.BlurAlgorithm;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderEffectBlur;
import eightbitlab.com.blurview.RenderScriptBlur;
import android.view.WindowManager; 
import zzh.source.hl2.UpdateService;
import zzh.source.hl2.UpdateSystem;
import zzh.source.hl2.ExtractAssets;
import zzh.source.hl2.DirchActivity;

import androidx.appcompat.app.AppCompatActivity;
import org.libsdl.app.SDLActivity;

public class MainActivity extends AppCompatActivity {
	public static String PKG_NAME;

	public static boolean can_write = true;
	static EditText cmdArgs, GamePath = null, EnvEdit, res_width, res_height;
	public SharedPreferences mPref;
	public static final int sdk = Integer.valueOf(Build.VERSION.SDK).intValue();
	static CheckBox useVolumeButtons, check_updates;

	final static int REQUEST_PERMISSIONS = 42;

	public void applyPermissions( final String permissions[], final int code ) {
		List<String> requestPermissions = new ArrayList<String>();
		for( int i = 0; i < permissions.length; i++ ) {
			if( checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED )
				requestPermissions.add(permissions[i]);
		}

		if( !requestPermissions.isEmpty() ) {
			String[] requestPermissionsArray = new String[requestPermissions.size()];
			for( int i = 0; i < requestPermissions.size(); i++ )
				requestPermissionsArray[i] = requestPermissions.get(i);
			requestPermissions(requestPermissionsArray, code);
		}
	}

	public void onRequestPermissionsResult( int requestCode,  String[] permissions,  int[] grantResults ) {
		if( requestCode == REQUEST_PERMISSIONS ) {
			if( grantResults[0] == PackageManager.PERMISSION_DENIED ) {
				Toast.makeText( this, R.string.srceng_launcher_error_no_permission, Toast.LENGTH_LONG ).show();
				finish();
			}
		}
	}

	public static String getDefaultDir() {
		File dir = Environment.getExternalStorageDirectory();
		if (dir == null || !dir.exists())
			return "/sdcard/";
		return dir.getPath();
	}

	public static String getAndroidDataDir() {
		String path = getDefaultDir() + "/Android/data/" + PKG_NAME + "/files";
		File directory = new File(path);
		if (!directory.exists())
			directory.mkdirs();
		return path;
	}

	public static void changeButtonsStyle( ViewGroup parent )
	{
		if( sdk >= 21 )
			return;

		for( int i = parent.getChildCount() - 1; i >= 0; i-- )
		{
			try
			{
				final View child = parent.getChildAt(i);

				if( child == null )
					continue;

				if( child instanceof ViewGroup )
				{
					changeButtonsStyle((ViewGroup) child);
					// DO SOMETHING WITH VIEWGROUP, AFTER CHILDREN HAS BEEN LOOPED
				}
				else if( child instanceof Button )
				{
					final Button b = (Button)child;
					final Drawable bg = b.getBackground();
					if(bg!= null)bg.setAlpha( 96 );
					b.setTextColor( 0xFFFFFFFF );
					b.setTextSize( 15f );
					//b.setText(b.getText().toString().toUpperCase());
					b.setTypeface( b.getTypeface(),Typeface.BOLD );
				}
				else if( child instanceof EditText )
				{
					final EditText b = ( EditText )child;
					b.setBackgroundColor( 0xFF272727 );
					b.setTextColor( 0xFFFFFFFF );
					b.setTextSize( 15f );
				}
			}
			catch( Exception e )
			{
			}
		}
	}


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PKG_NAME = getApplication().getPackageName();
		requestWindowFeature(1);

		if (sdk >= 21)
			super.setTheme(0x01030224);
		else
			super.setTheme(0x01030005);

		mPref = getSharedPreferences("mod", 0);

		setContentView(R.layout.activity_launcher);
        
         //这是blurvie
        BlurView blurView = findViewById(R.id.bodyblur);
        float radius = 8f; //模糊半径
        View decorView = getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();
        blurView.setupWith(rootView, new RenderScriptBlur(this))
                .setFrameClearDrawable(windowBackground) // 可选
                .setBlurRadius(radius);

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      getWindow().setDecorFitsSystemWindows(false);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
     
      int flags = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
      decorView.setSystemUiVisibility(flags);
      getWindow().getAttributes().layoutInDisplayCutoutMode =
          WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      getWindow()
          .setFlags(
              WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
              WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    } else {
      getWindow()
          .setFlags(
              WindowManager.LayoutParams.FLAG_FULLSCREEN,
              WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
        showAboutDialog();

		LinearLayout body = (LinearLayout)findViewById(R.id.body);

		cmdArgs = (EditText)findViewById(R.id.edit_cmdline);
		EnvEdit = (EditText)findViewById(R.id.edit_env);
		GamePath = (EditText)findViewById(R.id.edit_gamepath);

//		immersiveMode = (CheckBox)findViewById(R.id.checkbox_immersive_mode);

/*		useVolumeButtons = (CheckBox)findViewById(R.id.checkbox_use_volume);

		useVolumeButtons.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if( isChecked )
					Toast.makeText(MainActivity.this, R.string.srceng_launcher_volume_buttons_desc, 5000).show();
			}
		});*/

		Button button = (Button)findViewById(R.id.button_launch);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				MainActivity.this.startSource(v);
			}
		});

		Button aboutButton = (Button) findViewById(R.id.button_about);
		aboutButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Dialog dialog = new Dialog(MainActivity.this);
				dialog.setTitle(R.string.srceng_launcher_about);
				ScrollView scroll = new ScrollView(MainActivity.this);
				scroll.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				scroll.setPadding(5,5,5,5);
				TextView text = new TextView(MainActivity.this);
				text.setText(R.string.srceng_launcher_about_text);
				text.setLinksClickable(true);
				text.setTextIsSelectable(true);
				Linkify.addLinks(text, Linkify.WEB_URLS|Linkify.EMAIL_ADDRESSES);
				scroll.addView(text);
				dialog.setContentView(scroll);
				dialog.show();
			}
		});

		Button dirButton = findViewById(R.id.button_gamedir);
		dirButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, DirchActivity.class);
				intent.addFlags(268435456);
				startActivity(intent);
			}
		});

/*		if (sdk >= 19) {
			immersiveMode.setChecked(true);
		}*/

		check_updates = (CheckBox)findViewById(R.id.checkbox_check_updates);
		String last_commit = getResources().getString(R.string.srceng_update);

		cmdArgs.setText(mPref.getString("argv", "-console"));
		GamePath.setText(mPref.getString("gamepath", getDefaultDir() + "/srceng"));
		EnvEdit.setText(mPref.getString("env", "LIBGL_USEVBO=0"));

//		useVolumeButtons.setChecked(mPref.getBoolean("use_volume_buttons", false));
		check_updates.setChecked(mPref.getBoolean("check_updates", true));

		changeButtonsStyle((ViewGroup)this.getWindow().getDecorView());

		// permissions check
		if( sdk >= 23 )
			applyPermissions( new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO }, REQUEST_PERMISSIONS );

		if( last_commit == null || last_commit.isEmpty() )
			check_updates.setVisibility(View.GONE);
		else if( check_updates.isChecked() ) {
			UpdateSystem update = new UpdateSystem(this);
			update.execute();
		}

	}

	public void saveSettings(SharedPreferences.Editor editor)
	{
		String argv = cmdArgs.getText().toString();
		String gamepath = GamePath.getText().toString();
		String env = EnvEdit.getText().toString();

		editor.putString("argv", argv);
		editor.putString("gamepath", gamepath);
		editor.putString("env", env);
//		editor.putBoolean("use_volume_buttons", useVolumeButtons.isChecked());
		editor.putBoolean("check_updates", check_updates.isChecked());
		editor.commit();
	}

	public void startSource(View view)
	{
		String gamepath = GamePath.getText().toString();

		SharedPreferences.Editor editor = mPref.edit();
		saveSettings(editor);

//		boolean rodir = mPref.getBoolean("rodir", false);
//		boolean can_write = writeTest(gamepath);

		if (sdk >= 19)
			editor.putBoolean("immersive_mode", true /*immersiveMode.isChecked()*/ );
		else
			editor.putBoolean("immersive_mode", false);

		editor.commit();

		Intent intent = new Intent(MainActivity.this, SDLActivity.class);
		intent.addFlags(268435456);
		startActivity(intent);


/*		if (can_write || rodir) {
			if (can_write) {
				editor.putBoolean("rodir", false);
				editor.commit();
				rodir = false;
			}

			if (sdk >= 19)
				editor.putBoolean("immersive_mode", true ); //immersiveMode.isChecked() );
			else
				editor.putBoolean("immersive_mode", false);

			editor.commit();

			Intent intent = new Intent(this, SDLActivity.class);
			intent.addFlags(268435456);
			startActivity(intent);

			return;
		}

		new AlertDialog.Builder(this).setTitle("Warning").setMessage(
			this.getResources().getString(R.string.srceng_launcher_error_test_write) + getAndroidDataDir()
		).setPositiveButton(R.string.srceng_launcher_ok, (DialogInterface.OnClickListener) null).show();

		editor.putBoolean("rodir", true);
		editor.commit();
*/
	}

	public void onPause()
	{
		Log.v("SRCAPK", "onPause");
		saveSettings(mPref.edit());
		super.onPause();
	}
    private void showAboutDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.srceng_app_name); // 设置对话框标题

    //设置对话框内容
    String aboutContent =
     "  此启动器由ZZH魔改\n  使用方法\n  选择游戏目录下的一个文件\n  注意 选择的文件必须和hl2 episodic ep2 bin platform hl2 同一个文件夹内\n 然后点击刷新按钮开始 \n (en) \n how to launch . first choose file in gamepatch !! the game example hl2 ep2....bin must in gamepatch \n after that refresh launch! \n just test and fix 版本";
        builder.setMessage(aboutContent);
    //设置确定按钮
    builder.setPositiveButton(
        "ZZH",
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {

           Intent intent = new Intent();
            intent.setAction(intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://m.bilibili.com/space/3493284326410960"));
            startActivity(intent);
            
          }
        });

    // 创建并显示对话框
    AlertDialog dialog = builder.create();
    // 创建自定义背景Drawable
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setColor(Color.parseColor("#80808080")); // 设置背景颜色为浅灰色
    gradientDrawable.setCornerRadius(20); // 设置圆角半径

    // 设置Drawable为Dialog的背景
    dialog.getWindow().setBackgroundDrawable(gradientDrawable);

    // 忽略刘海屏
    WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      lp.layoutInDisplayCutoutMode =
          WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
    }
    dialog.getWindow().setAttributes(lp);

    dialog.show();
  }
}


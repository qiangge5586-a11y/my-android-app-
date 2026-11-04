package cn.autohack.hondahack;
import android.app.Activity;import android.content.Intent;import android.os.Build;import android.os.Bundle;import android.speech.tts.TextToSpeech;import android.view.View;import android.widget.*;import java.util.Locale;
/** Local typed-command helper with optional on-device TTS; it does not record microphone audio. */
public final class VoiceAssistantActivity extends Activity implements TextToSpeech.OnInitListener {
 private TextToSpeech tts;private boolean ready=false;private EditText command;private TextView result;
 @Override protected void onCreate(Bundle b){super.onCreate(b);tts=new TextToSpeech(this,this);LinearLayout r=new LinearLayout(this);r.setOrientation(1);int p=dp(18);r.setPadding(p,p,p,p);
 TextView title=new TextView(this);title.setText("语音助手 / 本地助手");title.setTextSize(22);r.addView(title);
 TextView note=new TextView(this);note.setText("输入简单命令，本地识别关键词并提供帮助；可选使用系统文字转语音播报。不使用麦克风，也不上传语音。");note.setPadding(0,dp(10),0,dp(10));r.addView(note);
 command=new EditText(this);command.setSingleLine(true);command.setHint("例如：设备信息、应用管理、导航、本地视频");r.addView(command);
 Button run=new Button(this);run.setText("执行命令");run.setOnClickListener(new View.OnClickListener(){public void onClick(View v){handle(command.getText().toString());}});r.addView(run);
 result=new TextView(this);result.setText("可用命令：设备信息、应用管理、导航、本地视频、诊断信息");result.setPadding(0,dp(14),0,0);r.addView(result);setContentView(r);}
 private void handle(String raw){String q=raw==null?"":raw.trim();if(q.isEmpty()){reply("请输入命令");return;}
 if(q.contains("设备")){reply("正在打开设备信息");startActivity(new Intent(this,DeviceInfoActivity.class));}
 else if(q.contains("应用")){reply("正在打开应用管理");startActivity(new Intent(this,AppCatalogActivity.class));}
 else if(q.contains("导航")||q.contains("地图")){reply("正在打开导航，请在页面输入目的地");startActivity(new Intent(this,NavigationActivity.class));}
 else if(q.contains("视频")){reply("正在打开本地视频选择器");startActivity(new Intent(this,MediaActivity.class));}
 else if(q.contains("诊断")){reply("正在打开诊断信息");startActivity(new Intent(this,DiagnosticsActivity.class));}
 else {reply("暂不支持这个命令。可用：设备信息、应用管理、导航、本地视频或诊断信息。");}}
 private void reply(String text){result.setText(text);if(ready)tts.speak(text,TextToSpeech.QUEUE_FLUSH,null);}
 @Override public void onInit(int status){if(status==TextToSpeech.SUCCESS){ready=true;int lang=tts.setLanguage(new Locale("zh","CN"));if(lang==TextToSpeech.LANG_MISSING_DATA||lang==TextToSpeech.LANG_NOT_SUPPORTED)tts.setLanguage(Locale.getDefault());}}
 @Override protected void onDestroy(){if(tts!=null){tts.stop();tts.shutdown();}super.onDestroy();}
 private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
}
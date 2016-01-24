package com.example.threaddownloader;

import java.io.File;

import net.download.DownloadProgressListener;
import net.download.FileDownloader;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	/**
	 * 下载路劲
	 */
	private EditText edt_path;
	/**
	 * 下载进度百分比
	 */
	private TextView txv_result;
	private Button btn_download;
	private ProgressBar pgb_rogressBar;
	
	// handler的作用是用于往创建Hander对象所在的线程所绑定的消息队列发送消息
	private Handler handler = new Handler();
	
	private final class UIHandler extends Handler {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				int size = msg.getData().getInt("size");
				pgb_rogressBar.setProgress(size);
				float num = (float) pgb_rogressBar.getProgress() / (float) pgb_rogressBar.getMax();
				int result = (int)(num*100);
				txv_result.setText(result + "%");
				if(pgb_rogressBar.getProgress() == pgb_rogressBar.getMax()){
					Toast.makeText(getApplicationContext(), "下载完成", 1).show();
				}
				break;
			case -1:
				Toast.makeText(getApplicationContext(), "下载失败", 1).show();
				break;
			default:
				break;
			}
			
			
			
		}
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		edt_path = (EditText) this.findViewById(R.id.path);
		txv_result = (TextView) this.findViewById(R.id.resultView);
		btn_download = (Button) this.findViewById(R.id.downloadbutton);
		pgb_rogressBar = (ProgressBar) this.findViewById(R.id.progressBar);
		
		btn_download.setOnClickListener(new ButtonClickListener());
	}
	
	private final class ButtonClickListener implements OnClickListener{
		public void onClick(View arg0) {
			String path = edt_path.toString();
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				// 保存的文件路径
				File saveDir = Environment.getExternalStorageDirectory();	// 取得当前项目下的路径
				download(path, saveDir);
			} else {
				Toast.makeText(getApplicationContext(), "获取失败", 1).show();
			}
		}

		/*
		 * 由于用户的输入事件（点击button，触摸屏幕...）是由主线程负责处理的，如果主线程处于工作状态
		 * 此时用户产生的输入事件如果没能在5秒内得到处理，系统就会报“应用无相应”错误
		 * 所以在主线程里不能执行一件比较耗时的工作，否则会因主线程阻塞而无法处理用户的输入事件
		 * 导致“用户无响应”错误的出现，耗时的工作应该在子线程里执行
		 * */
		private void download(String path, File saveDir) {
			DownloadTask task = new DownloadTask(path, saveDir);
			new Thread(task).start();
		}
		
		/*
		 * UI控件画面的重绘（更新）是由主线程负责处理的，如果在子线程中更新UI控件的值，更新后的值不会重现在屏幕上
		 * 一定要在主线程里更新UI控件的值，这样才能在屏幕上显示出来，不能在子线程中更新UI控件的值
		 * */
		private final class DownloadTask implements Runnable {
			private String path;
			private File saveDir;
			public DownloadTask(String path, File saveDir) {
				this.path = path;
				this.saveDir = saveDir;
			}

			public void run() {
				try {
					FileDownloader loader = new FileDownloader(getApplicationContext(), path, saveDir, 3);
					pgb_rogressBar.setMax(loader.getFileSize());	// 设置进度条的最大刻度
					loader.download(new DownloadProgressListener() {
						public void onDownloadSize(int size) {
							Message msg = new Message();
							msg.what = 1;
							msg.getData().putInt("size", size);
							handler.sendMessage(msg);
							
						}
					});
				} catch (Exception e) {
						e.printStackTrace();
						handler.sendMessage(handler.obtainMessage(-1));
				}
			}
		}
	}


}

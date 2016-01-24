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
	 * ����·��
	 */
	private EditText edt_path;
	/**
	 * ���ؽ��Ȱٷֱ�
	 */
	private TextView txv_result;
	private Button btn_download;
	private ProgressBar pgb_rogressBar;
	
	// handler������������������Hander�������ڵ��߳����󶨵���Ϣ���з�����Ϣ
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
					Toast.makeText(getApplicationContext(), "�������", 1).show();
				}
				break;
			case -1:
				Toast.makeText(getApplicationContext(), "����ʧ��", 1).show();
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
				// ������ļ�·��
				File saveDir = Environment.getExternalStorageDirectory();	// ȡ�õ�ǰ��Ŀ�µ�·��
				download(path, saveDir);
			} else {
				Toast.makeText(getApplicationContext(), "��ȡʧ��", 1).show();
			}
		}

		/*
		 * �����û��������¼������button��������Ļ...���������̸߳�����ģ�������̴߳��ڹ���״̬
		 * ��ʱ�û������������¼����û����5���ڵõ�����ϵͳ�ͻᱨ��Ӧ������Ӧ������
		 * ���������߳��ﲻ��ִ��һ���ȽϺ�ʱ�Ĺ���������������߳��������޷������û��������¼�
		 * ���¡��û�����Ӧ������ĳ��֣���ʱ�Ĺ���Ӧ�������߳���ִ��
		 * */
		private void download(String path, File saveDir) {
			DownloadTask task = new DownloadTask(path, saveDir);
			new Thread(task).start();
		}
		
		/*
		 * UI�ؼ�������ػ棨���£��������̸߳�����ģ���������߳��и���UI�ؼ���ֵ�����º��ֵ������������Ļ��
		 * һ��Ҫ�����߳������UI�ؼ���ֵ��������������Ļ����ʾ���������������߳��и���UI�ؼ���ֵ
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
					pgb_rogressBar.setMax(loader.getFileSize());	// ���ý����������̶�
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

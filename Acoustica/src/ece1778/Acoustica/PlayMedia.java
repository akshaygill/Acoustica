package ece1778.Acoustica;

import java.io.IOException;

import android.media.MediaPlayer;
import android.os.Environment;

public class PlayMedia {
	
	private MediaPlayer mp;
	
	public PlayMedia(MediaPlayer mp) {
		super();
		this.mp = mp;
	}

	
	
	
	public void startPlaying(String filename){
		String filePath = Environment.getExternalStorageDirectory()+"/Acoustica_Recordings/"+ filename;
		//MediaPlayer mediaPlayer = new  MediaPlayer();
		try{
			if(mp.isPlaying())
				mp.stop();
			
			mp.reset();

			
		}
		catch(IllegalStateException e){
			// TODO Auto-generated catch block
						e.printStackTrace();
		}
		try {
			mp.setDataSource(filePath);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			mp.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		mp.start();
		
	}
	
	public void stopPlaying(){
		mp.stop();
		try{
		mp.reset();
		} catch(IllegalStateException e){
			e.printStackTrace();
		}
	}
	}



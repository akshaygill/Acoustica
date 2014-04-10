package ece1778.Acoustica.Filebrowser;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ece1778.Acoustica.PlayMedia;
import ece1778.Acoustica.R;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FileBrowse  extends Activity{
	/*file variables*/
	
	private ListView listViewfile;
	private String directory;
	private ArrayList<String> FilesInFolder;
	private SimpleAdapter simpleAdapt;
	PlayMedia pm;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listview);
		pm = new PlayMedia(new MediaPlayer());
		listViewfile=(ListView)findViewById(R.id.listView);
		directory = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Acoustica_Recordings/";
		List<Map<String, String>> musicFilesMap = new ArrayList<Map<String,String>>(); 
		
        String[] musicFilesArray= wavFileNames(directory);
		
		
		
		//testing if filenames are stored 
		for(int i=0;i<musicFilesArray.length;i++)
		{
			Log.d("musicfilenames ",musicFilesArray[i]);
		}
		
		//adding the filenames from string array to HashMap
		if(musicFilesArray!=null)
		{
		for(int i=0;i<musicFilesArray.length;i++)
		{
			musicFilesMap.add(FileNameHash("MusicFileKey",musicFilesArray[i]));
		}
		}
		
		// view file list using inbuilt list and text1 ids of listview
		if(musicFilesMap!=null)
		{
			simpleAdapt = new SimpleAdapter(this,musicFilesMap,android.R.layout.simple_list_item_1,new String []{"MusicFileKey"},new int[]{android.R.id.text1});
			listViewfile.setAdapter(simpleAdapt);
		}
		
		//adding the click feature on the list
		listViewfile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long id){
			TextView text1=(TextView) view;
			//play the file using mediaplayer
			
			pm.startPlaying(text1.getText().toString());
			//test to check the file name
			//Toast.makeText(getApplicationContext(), "Item with id ["+id+"] - Position ["+position+"] - Music ["+text1.getText()+"]", Toast.LENGTH_SHORT).show(); 
			}
		}

			
			);
		
	}

	//Function to retrieve wave files
	public static String[] wavFileNames(String directoryPath) {

	    File dir = new File(directoryPath);

	    Collection<String> files  =new ArrayList<String>();

	    if(dir.isDirectory()){
	        File[] listFiles = dir.listFiles();

	        for(File file : listFiles){
	            if(file.isFile() ) {
	            	if(file.getName().endsWith(".wav"))
	                files.add(file.getName());
	            }
	        }
	    }

	    return files.toArray(new String[]{});
	}
	
	//we need to use hashmap for simpleAdapter 
	private HashMap<String, String> FileNameHash(String key, String name) {
	    HashMap<String, String> musicfilehash = new HashMap<String, String>();
	    musicfilehash.put(key, name);
	    return musicfilehash;
	}
	//options menu
	@Override
	public void onBackPressed() {
		pm.stopPlaying();
		finish();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    //inflater.inflate(R.menu.game_menu, menu);
	    return true;
	}
}

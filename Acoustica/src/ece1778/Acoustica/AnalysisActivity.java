package ece1778.Acoustica;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;



import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import ece1778.Acoustica.Filebrowser.FileBrowse;
import ece1778.Acoustica.Stitching.*;

public class AnalysisActivity extends Activity {

	private static final String TAG = "AnalysisActivity";       /* Used in Log messages */
	private static final String TAG2 = "PreAnalysisActivity";   /* Used in Log messages */
	private static final String TAG3 = "PostAnalysisActivity";  /* Used in Log messages */
	private static final String LIBRARY_ACTIVITY = "LibraryActivity";
	private static final String LISTEN_ACTIVITY = "ListenActivity";

	/* Layout variables */
	private ProgressBar progressBar;
	private TextView TextViewAnalysis;
	private String CallActivity;
	private int[] pattern;
	private TextView Pattern;

	/* Algorithm variables */
	private int TempoValue_bpm;
	private int TempoValue_ms;
	private String KeyValue = "Unknown";
	private String AccidentalValue = "Unknown"; 
	private ArrayList<Double>  FrequencyList    = new ArrayList<Double>();    /* Frequencies [Hz] received from ListenActivity */
	private ArrayList<Double>  DurationList     = new ArrayList<Double>();    /* Duration [ms] received from ListenActivity */
	private ArrayList<String>  NoteList         = new ArrayList<String>();    /* Final note list as strings */
	private ArrayList<Integer> NoteListInteger  = new ArrayList<Integer>();   /* Final note list as integers 1 to 88 */
	private ArrayList<Integer> RepetitionList;   			/* Final REPETITION recognition list */
	private ArrayList<Integer> ScalarMotionList;   			/* Final SCALAR MOTION recognition list */
	private ArrayList<Integer> TriadList;   				/* Final TRIAD recognition list */
	private ArrayList<Integer> MotiveList;	 				/* Final MOTIVE recognition list */
	private ArrayList<Double>  NoteDuration     = new ArrayList<Double>();    /* Final note duration as a fraction of beat list */

	// TODO remove later private PatternRecognition myPatternRecognition;
	private boolean boNoteAnalysisCompleted;
	

	/* Status flags for async task handling and status report */
	AtomicBoolean boTotalAnalysisDone     = new AtomicBoolean(false);
	AtomicBoolean boUnknownKEY            = new AtomicBoolean(false);

	
	//Added by Akshay
	//Begin
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_FOLDER = "Acoustica_Recordings";
	private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
	private static final String AUDIO_RECORDER_TEMP_OUTPUT_FILE = "record_temp_out.raw";
	FileOutputStream os = null;
	//End
	
	
	//Added by Amanjot 
	//Begin
	MediaPlayer mp = new MediaPlayer();
	PlayMedia pm = new PlayMedia(mp);
	//End
	
	//Added by Akshay
	String pattern_onSet = "1111";
	//end
	Stitching stitch ;
	/*****************************************************************/
	/*                 onCreate                                      */
	/*****************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_analysis);

		Bundle extras = getIntent().getExtras();
		CallActivity = extras.getString("activity_string");

		if (CallActivity.equals(LISTEN_ACTIVITY)) {
			TempoValue_ms = extras.getInt("tempo_ms");
			FrequencyList = (ArrayList<Double>) getIntent().getSerializableExtra("noteFrequency");
			DurationList  = (ArrayList<Double>) getIntent().getSerializableExtra("noteDuration");
			pattern_onSet = (String) getIntent().getSerializableExtra("pattern_onSet");
			
		}
		if (CallActivity.equals(LIBRARY_ACTIVITY)) {
			KeyValue = extras.getString("key_string");
			NoteListInteger = (ArrayList<Integer>) getIntent().getSerializableExtra("noteList");
			NoteDuration  = (ArrayList<Double>) getIntent().getSerializableExtra("noteDuration");
		}

		TempoValue_bpm = extras.getInt("tempo_bpm");


		//TODO remove? myPatternRecognition = new PatternRecognition();


//		TextViewAnalysis = (TextView)findViewById(R.id.analysisTextView);
		//Pattern = (TextView)findViewById(R.id.pattern);
		boNoteAnalysisCompleted = false;

		boTotalAnalysisDone.set(false);
		boUnknownKEY.set(false); 

	}

	/*****************************************************************/
	/*                 onResume                                      */
	/*****************************************************************/
	@Override
	protected void onResume() {
		super.onResume();

		if (boNoteAnalysisCompleted == false) {
			if (CallActivity.equals(LISTEN_ACTIVITY)) {
				/* Start analysis */
//				progressBar.setVisibility(View.VISIBLE);   /* Make progress bar visible */
				
			}
			
			boNoteAnalysisCompleted = true;
		}
	}

	/*****************************************************************/
	/*                 onCreateOptionsMenu                           */
	/*****************************************************************/
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	

	//Music library Button 
	public void getFileNames(View view){
		if(pm != null)
		{
			pm.stopPlaying();
		}
		Intent filenameIntent = new Intent(this,FileBrowse.class);
		startActivity(filenameIntent);
	}


	/*****************************************************************/
	/*                 DONE Button user click method                 */
	/*****************************************************************/
	
	public void doneAnalysis(View view) {
	stitch=new Stitching();
  
    Log.d("Analysis Activity", "The pattern found is : " + pattern_onSet);
    String drum_file = "f"+maptoFile(pattern_onSet)+".raw";
    
   // File file1 = new File(path.getPath());
    File file1 = new File(Environment.getExternalStorageDirectory() + "/Acoustica_Recordings/drum_loops/"+drum_file);
    File file2 = new File(Environment.getExternalStorageDirectory() + "/Acoustica_Recordings/record_temp.raw");
    
        stitch.BeginStitch(file1, file2);
        Button b = (Button)findViewById(R.id.saveAnalysisButton);
    
		b.setEnabled(true);
		
 }
	
	public void PlayFunc(View v){
		pm.startPlaying(stitch.AUDIO_RECORDER_WAV_OUTPUT_FILE);
	}

	/*****************************************************************/
	/*                 onBackPressed                                 */
	/*****************************************************************/
	@Override
	public void onBackPressed() {
		if(pm != null)
		{
		pm.stopPlaying();
		}
		finish();
	}

	
	
	//Added by Amanjot 
	//Begin
	
	public int[] getFileList()
	{

		int[] filenames=null;
		

		final String state = Environment.getExternalStorageState();
		Log.d("Path",state);
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			String path = Environment.getExternalStorageDirectory().toString()+"/Acoustica_Recordings/drum_loops/";
			File f = new File(path);        
			File files[] = f.listFiles();
			//File[] files = .listFiles();
			filenames = new int[files.length];
			if (files!=null){
				for(int i = 0; i<files.length; i++)
				{
					int a = files.length;
					String filename = files[i].getName();
					filename = filename.replaceAll("f", "");
					filename = filename.replaceAll(".raw","");
					//String filename = fields[count].getName();
			       // filename = filename.replaceAll("f", "");
			        filenames[i]=Integer.parseInt(filename);
					Log.d("FileName",filename);
				}
				
			}
			
		} else {
			Log.d("FileName","else_Block");
		}

		 return filenames;
	}
	
	
	public int[] getRawFiles(){
	    Field[] fields = R.raw.class.getFields();
	    int[] filenames = new int[fields.length];
	    // loop for every file in raw folder
	    for(int count=0; count < fields.length; count++){

	        int rid=0;
			try {
				rid = fields[count].getInt(fields[count]);
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
	        // Use that if you just need the file name
			
	        String filename = fields[count].getName();
	        if (filename.equals("clicktrack"))
	        {}
	        else{
	        filename = filename.replaceAll("f", "");
	        filename = filename.replaceAll("raw","");
	        filenames[count]=Integer.parseInt(filename);
	        Log.d("FileName_raw",filename);
	        }
	      
	    }
	    return filenames;
	}
	public String maptoFile(String pattern){
		pattern = pattern.replaceAll("\\s+","");
		
		int pattern_num = Integer.parseInt(pattern);
		//Integer pattern_num = new Integer(pattern_num0);
		int [] filenames = getFileList();
//		int [] filenames = getRawFiles();
		Arrays.sort(filenames);
		int [] difference = new int[filenames.length];
		int min = 6666;
		int minindex = 0;
		for (int i = 0;i<difference.length;i++)
		{
			 
			difference[i] = Math.abs(filenames[i]-pattern_num);
			int current = difference[i];
			if(current <min)
			{
				min = current;
				minindex = i;
			}
		}
		Log.d("Pattern matched", String.valueOf(filenames[minindex]));
		return String.valueOf(filenames[minindex]);
		
//		if(pattern_num>4444)
//		{
//			return String.valueOf(4444);
//		}
//		else if(Arrays.binarySearch(filenames,pattern_num)>0)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num));
//			return String.valueOf(pattern_num);
//			
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-1)>0&&((pattern_num-1)%10!=0))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-1));
//			return String.valueOf(pattern_num-1);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-10)>0&&((pattern_num-10)%100>10))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-10));
//			return String.valueOf(pattern_num-10);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-100)>0&&(pattern_num-100)%1000>100)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-100));
//			return String.valueOf(pattern_num-100);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-1000)>0&&(pattern_num-1000>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-1000));
//			return String.valueOf(pattern_num-1000);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-2)>0&&((pattern_num-2)%10!=0))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-2));
//			return String.valueOf(pattern_num-2);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-20)>0&&((pattern_num-20)%100>10))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-20));
//			return String.valueOf(pattern_num-20);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-200)>0&&(pattern_num-200)%1000>100)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-200));
//			return String.valueOf(pattern_num-200);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-2000)>0&&(pattern_num-2000>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-2000));
//			return String.valueOf(pattern_num-2000);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-3)>0&&((pattern_num-3)%10!=0))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-3));
//			return String.valueOf(pattern_num-3);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-30)>0&&((pattern_num-30)%100>10))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-30));
//			return String.valueOf(pattern_num-30);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-300)>0&&(pattern_num-300)%1000>100)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-300));
//			return String.valueOf(pattern_num-300);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-3000)>0&&(pattern_num-3000>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-3000));
//			return String.valueOf(pattern_num-3000);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-11)>0&&((pattern_num-11)%100>10))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-11));
//			return String.valueOf(pattern_num-11);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-110)>0&&(pattern_num-110)%1000>100)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-110));
//			return String.valueOf(pattern_num-110);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-101)>0&&(pattern_num-101)%1000>100)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-101));
//			return String.valueOf(pattern_num-101);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-1001)>0&&(pattern_num-1001>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-1001));
//			return String.valueOf(pattern_num-1001);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-1100)>0&&(pattern_num-1100>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-1100));
//			return String.valueOf(pattern_num-1100);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-1010)>0&&(pattern_num-1010>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-1010));
//			return String.valueOf(pattern_num-1010);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-111)>0&&(pattern_num-111)%1000>100)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-111));
//			return String.valueOf(pattern_num-111);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-1110)>0&&(pattern_num-1110>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-1110));
//			return String.valueOf(pattern_num-1110);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-1011)>0&&(pattern_num-1011>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-1011));
//			return String.valueOf(pattern_num-1011);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-1101)>0&&(pattern_num-1101>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-1101));
//			return String.valueOf(pattern_num-1101);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-22)>0&&((pattern_num-22)%100>10))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-22));
//			return String.valueOf(pattern_num-22);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-220)>0&&(pattern_num-220)%1000>100)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-220));
//			return String.valueOf(pattern_num-220);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-202)>0&&(pattern_num-202)%1000>100)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-202));
//			return String.valueOf(pattern_num-202);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-2002)>0&&(pattern_num-2002>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-2002));
//			return String.valueOf(pattern_num-2002);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-2200)>0&&(pattern_num-2200>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-2200));
//			return String.valueOf(pattern_num-2200);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-2020)>0&&(pattern_num-2020>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-2020));
//			return String.valueOf(pattern_num-2020);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-222)>0&&(pattern_num-222)%1000>100)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-222));
//			return String.valueOf(pattern_num-222);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-2220)>0&&(pattern_num-2220>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-2220));
//			return String.valueOf(pattern_num-2220);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-2022)>0&&(pattern_num-2022>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-2022));
//			return String.valueOf(pattern_num-2022);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-2202)>0&&(pattern_num-2202>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-2202));
//			return String.valueOf(pattern_num-2202);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-33)>0&&((pattern_num-33)%100>10))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-33));
//			return String.valueOf(pattern_num-33);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-330)>0&&(pattern_num-330)%1000>100)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-330));
//			return String.valueOf(pattern_num-330);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-303)>0&&(pattern_num-303)%1000>100)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-303));
//			return String.valueOf(pattern_num-303);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-3003)>0&&(pattern_num-3003>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-3003));
//			return String.valueOf(pattern_num-3003);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-3300)>0&&(pattern_num-3300>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-3300));
//			return String.valueOf(pattern_num-3300);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-3030)>0&&(pattern_num-3030>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-3030));
//			return String.valueOf(pattern_num-3030);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-333)>0&&(pattern_num-333)%1000>100)
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-333));
//			return String.valueOf(pattern_num-333);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-3330)>0&&(pattern_num-3330>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-3330));
//			return String.valueOf(pattern_num-3330);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-3033)>0&&(pattern_num-3033>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-3033));
//			return String.valueOf(pattern_num-3033);
//		}
//		else if (Arrays.binarySearch(filenames,pattern_num-3303)>0&&(pattern_num-3303>1000))
//		{
//			Log.d("Pattern Found",String.valueOf(pattern_num-3303));
//			return String.valueOf(pattern_num-3303);
//		}
//
//		else
//		{
//			Log.d("Pattern Found",String.valueOf(2222));
//			return String.valueOf(2222);
//		}
		
		}
		 	
}

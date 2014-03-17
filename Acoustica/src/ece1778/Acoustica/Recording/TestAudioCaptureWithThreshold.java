package ece1778.Acoustica.Recording;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import ece1778.Acoustica.R;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

public class TestAudioCaptureWithThreshold extends Activity 
{
private static final String TAG = TestAudioCaptureWithThreshold.class.getSimpleName();
private static final int RECORDER_BPP = 16;
private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
private static final String AUDIO_RECORDER_TEMP_FILE = "record_temp.raw";
private static final String AUDIO_RECORDER_TEMP_OUTPUT_FILE = "record_temp_out.raw";

FileOutputStream os = null;

int bufferSize ;
int frequency = 44100; //8000;
int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
boolean started = false;
boolean preStart=false;
RecordAudio recordTask;

short threshold=1800; 
Button startButton;
Button stopButton;
Button stitchButton;
boolean debug=false;
AudioRecord audioRecord;

int storeNum=0;
int index=0;
int bufferLen=index;
int foundPeak=0;
byte[] byteBuffer;
int i=0;

@Override
protected void onCreate(Bundle savedInstanceState) {
   Log.w(TAG, "onCreate");
   super.onCreate(savedInstanceState);
   setContentView(R.layout.activity_test_audio_capture_with_threshold);
   startButton =(Button) findViewById(R.id.button1);
   stopButton =(Button) findViewById(R.id.button2);
   stitchButton=(Button) findViewById(R.id.button3);
   startButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
	        // Do something in response to button click
	    	startAquisition();
	    	
	    }
	});
   
   stopButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
	        // Do something in response to button click
	    	stopAquisition();
	    	recordTask.cancel(true);
	    }
	});
   stitchButton.setOnClickListener(new View.OnClickListener() {
	    public void onClick(View v) {
	        // Do something in response to button click
	    	//stopAquisition();
	    	//recordTask.cancel(true);
	    	BeginStitch();
	    }
	});
   
}


@Override
protected void onResume() {
   Log.w(TAG, "onResume");
   super.onResume();
}

@Override
protected void onDestroy() {
   Log.w(TAG, "onDestroy");
   stopAquisition();
   super.onDestroy();
}

public class RecordAudio extends AsyncTask<Void, Double, Void> 
{
   @Override
   protected Void doInBackground(Void... arg0) 
   {
       Log.w(TAG, "doInBackground");
       try {
               String filename = getTempFilename();
           try 
           {
               os = new FileOutputStream(filename);               
           } 
           catch (FileNotFoundException e) 
           {
               e.printStackTrace();
           }   

           bufferSize = AudioRecord.getMinBufferSize(frequency, 
           channelConfiguration, audioEncoding); 
           audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency, 
                   channelConfiguration, audioEncoding, bufferSize); 

           short[] buffer = new short[bufferSize];
           int[] bufferReadResult = new int[bufferSize];
           audioRecord.startRecording();           

//          while (started) {
//               int bufferReadResult = audioRecord.read(buffer, 0,bufferSize);
//               //Log.d("bufferReadResult",String.valueOf(bufferReadResult));
//              if(AudioRecord.ERROR_INVALID_OPERATION != bufferReadResult){
//                    //check signal
//                 //put a threshold
//                    int foundPeak=searchThreshold(buffer,threshold);
//                    Log.d("foundpeak",String.valueOf(foundPeak));
//                    
//                   
//                      if (foundPeak>-1){ //found signal
//                                               //record signal
//                          byte[] byteBuffer =ShortToByte(buffer,bufferReadResult);
//                          
//                          //Log.d("byteBufferValue",byteBuffer.toString());
//                      try {
//                              os.write(byteBuffer);
//                      } catch (IOException e) {
//                              e.printStackTrace();
//                    }
//                       }else{//count the time
//                           //don't save signal
//                       }
//                      
//
//            	   
//                               //show results
//                       //here, with publichProgress function, if you calculate the total saved samples, 
//                       //you can optionally show the recorded file length in seconds:      publishProgress(elsapsedTime,0);
//
//
//              }
//           }
           
          
           while(preStart)
           {
        	   int bufferReadResult1 = audioRecord.read(buffer, 0,bufferSize);
        	   if(AudioRecord.ERROR_INVALID_OPERATION != bufferReadResult1)
        	   {
						int foundPeak = searchThreshold(buffer, threshold);

						if (foundPeak == -1)
						{
							started = false;
							Log.d("started",String.valueOf(started));}
						else 
						{
							started = true;
							preStart = false;
						}
				}
           }
           
           //New Algorithm
           while(started)
           {        	   
        	   //BufferReadResult stores the number of shorts that were read
        	   //buffer stores the short data being read
                bufferReadResult[index]=audioRecord.read(buffer, 0, bufferSize);  
                Log.d("bufferReadResult",String.valueOf(buffer));
                //Convert short data in buffer to byte and store in byteBuffer
                byteBuffer=ShortToByte(buffer,bufferReadResult[index]);
                index++;
                //Write byteBuffer to temp file
                os.write(byteBuffer);                
           }
                     
           audioRecord.stop();
           
           //new algorithm continues
   //        Log.d("index",String.valueOf(index));
//           for(int i=0;i<index;i++)
//           {
//        	    foundPeak=searchThreshold(buffer,threshold);
//        	   if(foundPeak>-1){
//        		   storeNum=i;
//        		   Log.d("storeNum",String.valueOf(storeNum));
//        		   Log.d("foundPEakinside",String.valueOf(foundPeak));
//        		   break;
//        	   }
//        	   
//           }
//           Log.d("foundPEakoutside",String.valueOf(foundPeak));
//           for(int j=0;j<index;j++)
//           {
//        	  // bufferReadResult[index]=audioRecord.read(buffer, 0,bufferSize) ;  
//        	   byte[] byteBuffer=ShortToByte(buffer,bufferReadResult[j]);
//        	   os.write(byteBuffer);
//           }          

           //close file
             try 
             {
                   os.close();
             } 
             catch (IOException e) 
             {
                   e.printStackTrace();
             }

             copyWaveFile(getTempFilename(),getFilename());
             //Todo: Commented for raw
             //deleteTempFile();
       } 
       
       catch (Throwable t) 
       {
           t.printStackTrace();
           Log.e("AudioRecord", "Recording Failed");
       }
       return null;

   } //fine do doInBackground

     byte [] ShortToByte(short [] input, int elements) 
     {
    	 int short_index, byte_index;
    	 int iterations = elements; //input.length;
    	 byte [] buffer = new byte[iterations * 2];

    	 short_index = byte_index = 0;

    	 for(/*NOP*/; short_index != iterations; /*NOP*/)
    	 {
    		 buffer[byte_index]     = (byte) (input[short_index] & 0x00FF); 
    		 buffer[byte_index + 1] = (byte) ((input[short_index] & 0xFF00) >> 8);

    		 ++short_index; byte_index += 2;
    	 }

    	 return buffer;
     }
     
     short [] ByteToShort(byte [] input, int elements) 
     {
    	 int short_index, byte_index;
    	 int iterations = elements; //input.length;
    	 short [] buffer = new short[iterations/2];

    	 short_index = byte_index = 0;

    	 for(/*NOP*/; short_index != iterations; /*NOP*/)
    	 {
    		 buffer[short_index] = (short) (input[byte_index] & 0xFF | input[byte_index+1] << 8); 

    		 ++short_index; byte_index += 2;
    	 }

    	 return buffer;
     }


   int searchThreshold(short[]arr,short thr)
   {
       int peakIndex;
       int arrLen=arr.length;
       for (peakIndex=0;peakIndex<arrLen;peakIndex++)
       {
           if ((arr[peakIndex]>=thr) || (arr[peakIndex]<=-thr))
           {
               //se supera la soglia, esci e ritorna peakindex-mezzo kernel.

               return peakIndex;
           }           
       }
       return -1; //not found
   }

   /*
   @Override
   protected void onProgressUpdate(Double... values) {
       DecimalFormat sf = new DecimalFormat("000.0000");           
       elapsedTimeTxt.setText(sf.format(values[0]));
   }
   */

   private String getFilename()
   {
       String filepath = Environment.getExternalStorageDirectory().getPath();
       File file = new File(filepath,AUDIO_RECORDER_FOLDER);

       if(!file.exists()){
               file.mkdirs();
       }

       return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
   }


   private String getTempFilename()
   {
       String filepath = Environment.getExternalStorageDirectory().getPath();
       File file = new File(filepath,AUDIO_RECORDER_FOLDER);

       if(!file.exists()){
               file.mkdirs();
       }

       File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

      //Todo:commented for raw
      // if(tempFile.exists())
      //         tempFile.delete();

       return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
   }
   
   private String getTempOutputFilename()
   {
       String filepath = Environment.getExternalStorageDirectory().getPath();
       File file = new File(filepath,AUDIO_RECORDER_FOLDER);

       if(!file.exists()){
               file.mkdirs();
       }

       File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_OUTPUT_FILE);

      //Todo:commented for raw
      // if(tempFile.exists())
      //         tempFile.delete();

       return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_OUTPUT_FILE);
   }

   
   private void deleteTempFile() 
   {
           File file = new File(getTempFilename());
           file.delete();
   }

   private void copyWaveFile(String inFilename,String outFilename)
   {
       FileInputStream in = null;
       FileOutputStream out = null;
       long totalAudioLen = 0;
       long totalDataLen = totalAudioLen + 36;
       long longSampleRate = frequency;
       int channels = 1;
       long byteRate = RECORDER_BPP * frequency * channels/8;

       byte[] data = new byte[bufferSize];

       try {
               in = new FileInputStream(inFilename);
               out = new FileOutputStream(outFilename);
               totalAudioLen = in.getChannel().size();
               totalDataLen = totalAudioLen + 36;


               WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                               longSampleRate, channels, byteRate);

               while(in.read(data) != -1){
                       out.write(data);
               }

               in.close();
               out.close();
       } catch (FileNotFoundException e) {
               e.printStackTrace();
       } catch (IOException e) {
               e.printStackTrace();
       }
   }

   private void WriteWaveFileHeader(
                   FileOutputStream out, long totalAudioLen,
                   long totalDataLen, long longSampleRate, int channels,
                   long byteRate) throws IOException {

           byte[] header = new byte[44];

           header[0] = 'R';  // RIFF/WAVE header
           header[1] = 'I';
           header[2] = 'F';
           header[3] = 'F';
           header[4] = (byte) (totalDataLen & 0xff);
           header[5] = (byte) ((totalDataLen >> 8) & 0xff);
           header[6] = (byte) ((totalDataLen >> 16) & 0xff);
           header[7] = (byte) ((totalDataLen >> 24) & 0xff);
           header[8] = 'W';
           header[9] = 'A';
           header[10] = 'V';
           header[11] = 'E';
           header[12] = 'f';  // 'fmt ' chunk
           header[13] = 'm';
           header[14] = 't';
           header[15] = ' ';
           header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
           header[17] = 0;
           header[18] = 0;
           header[19] = 0;
           header[20] = 1;  // format = 1
           header[21] = 0;
           header[22] = (byte) channels;
           header[23] = 0;
           header[24] = (byte) (longSampleRate & 0xff);
           header[25] = (byte) ((longSampleRate >> 8) & 0xff);
           header[26] = (byte) ((longSampleRate >> 16) & 0xff);
           header[27] = (byte) ((longSampleRate >> 24) & 0xff);
           header[28] = (byte) (byteRate & 0xff);
           header[29] = (byte) ((byteRate >> 8) & 0xff);
           header[30] = (byte) ((byteRate >> 16) & 0xff);
           header[31] = (byte) ((byteRate >> 24) & 0xff);
           header[32] = (byte) (channels * 16 / 8);  // block align
           header[33] = 0;
           header[34] = RECORDER_BPP;  // bits per sample
           header[35] = 0;
           header[36] = 'd';
           header[37] = 'a';
           header[38] = 't';
           header[39] = 'a';
           header[40] = (byte) (totalAudioLen & 0xff);
           header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
           header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
           header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

           out.write(header, 0, 44);
   }

} //Fine Classe RecordAudio (AsyncTask)

@Override
public boolean onCreateOptionsMenu(Menu menu) {
   //getMenuInflater().inflate(R.menu.test_audio_capture_with_threshold,
     //      menu);
   return true;

}


public void resetAquisition() {
   Log.w(TAG, "resetAquisition");
   stopAquisition();
 startButton.setText("WAIT");
 startAquisition();
}

public void stopAquisition() {
   Log.w(TAG, "stopAquisition");
   if (started) {
       started = false;
       recordTask.cancel(true);
       
   }
}

public void BeginStitch()
{
	   File file1 = null;
	   File file2 = null;
	   
	   try
	   {
		   file1 = new File(Environment.getExternalStorageDirectory() + "/AudioRecorder/record_temp1.raw");
		   file2 = new File(Environment.getExternalStorageDirectory() + "/AudioRecorder/record_temp2.raw"); 
	   }
	   catch (Exception e)
	   {
		   e.printStackTrace();
	   }
	   
	   Stitch(file1,file2);
}

private void Stitch(File filename1, File filename2)
{
	   FileInputStream is1 = null;
	   FileInputStream is2 = null;
	   byte[] byteBuffer1 = new byte[1000000];
	   byte[] byteBuffer2 = new byte[1000000];
	   short[] shortBuffer1 = null;
	   short[] shortBuffer2 = null;
	   short[] shortBufferOut = null;
	   byte[] byteBufferOut = null;
	   
	   try 
	   {
	        is1 = new FileInputStream(filename1);
	        is2 = new FileInputStream(filename2);  
	        try 
	        	{
	     	   is1.read(byteBuffer1);
				} 
	        catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        try 
	        	{
	     	   is2.read(byteBuffer2);
	        	} 
	        catch (IOException e)
	        	{
	     	   // TODO Auto-generated catch block
	     	   e.printStackTrace();
	        	}
	        shortBuffer1 = new short[byteBuffer1.length/2];
	        shortBuffer2 = new short[byteBuffer2.length/2];           
	   } 
	    catch (FileNotFoundException e) 
	    {
	        e.printStackTrace();
	    }   	   
	   try
	   {
		   shortBuffer1 = ByteToShort(byteBuffer1,byteBuffer1.length/2);
		   shortBuffer2 = ByteToShort(byteBuffer2,byteBuffer2.length/2);		   
	   }
	   catch (Exception e)
	   {
		   e.printStackTrace();
	   }
	   
	   //Check which length is more
	   if(shortBuffer1.length > shortBuffer2.length)
		   shortBufferOut = new short[shortBuffer1.length];
	   else
		   shortBufferOut = new short[shortBuffer2.length];
	   
	   for(int i=0;i<shortBufferOut.length;i++)
	   {
		   if (i < shortBuffer1.length && i < shortBuffer2.length)
			   shortBufferOut[i] = (short) (shortBuffer1[i] + shortBuffer2[i]);
		   
		   //Todo: handle cases when one file bigger than the other
	   }
	   
	   //Convert short data in buffer to byte and store in byteBuffer
    byteBufferOut=ShortToByte(shortBufferOut,shortBufferOut.length);
    
    String filename_out = getTempOutputFilename();
    try 
    {
        os = new FileOutputStream(filename_out);               
    } 
    catch (FileNotFoundException e) 
    {
        e.printStackTrace();
    }   

    try
    {
 	   //Write byteBuffer to temp file
 	   os.write(byteBufferOut);   
    }
    catch (Exception e)
    {
 	   e.printStackTrace();
    }	   
    
    //Generate output wave file
    copyWaveFile(filename_out, getFilename());
}

private String getTempOutputFilename()
{
    String filepath = Environment.getExternalStorageDirectory().getPath();
    File file = new File(filepath,AUDIO_RECORDER_FOLDER);

    if(!file.exists()){
            file.mkdirs();
    }

    File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_OUTPUT_FILE);

   //Todo:commented for raw
   // if(tempFile.exists())
   //         tempFile.delete();

    return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_OUTPUT_FILE);
}



byte [] ShortToByte(short [] input, int elements) 
{
	 int short_index, byte_index;
	 int iterations = elements; //input.length;
	 byte [] buffer = new byte[iterations * 2];

	 short_index = byte_index = 0;

	 for(/*NOP*/; short_index != iterations; /*NOP*/)
	 {
		 buffer[byte_index]     = (byte) (input[short_index] & 0x00FF); 
		 buffer[byte_index + 1] = (byte) ((input[short_index] & 0xFF00) >> 8);

		 ++short_index; byte_index += 2;
	 }

	 return buffer;
}

short [] ByteToShort(byte [] input, int elements) 
{
	 int short_index, byte_index;
	 int iterations = elements; //input.length;
	 short [] buffer = new short[iterations];

	 short_index = byte_index = 0;

	 for(/*NOP*/; short_index != iterations-1; /*NOP*/)
	 {
		 buffer[short_index] = (short) (input[byte_index] & 0xFF | input[byte_index + 1] << 8); 

		 ++short_index; byte_index += 2;
	 }

	 return buffer;
}

public void startAquisition(){
   Log.w(TAG, "startAquisition");
   Handler handler = new Handler(); 
   handler.postDelayed(new Runnable() { 
        public void run() { 

           //elapsedTime=0;
           preStart = true;
           recordTask = new RecordAudio();
           recordTask.execute();
           //startButton.setText("RESET");
        } 
   }, 500); 
}


private void WriteWaveFileHeader(
        FileOutputStream out, long totalAudioLen,
        long totalDataLen, long longSampleRate, int channels,
        long byteRate) throws IOException 
        {

		byte[] header = new byte[44];
		
		header[0] = 'R';  // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f';  // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1;  // format = 1
		header[21] = 0;
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		header[32] = (byte) (channels * 16 / 8);  // block align
		header[33] = 0;
		header[34] = RECORDER_BPP;  // bits per sample
		header[35] = 0;
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		
		out.write(header, 0, 44);
}

private void copyWaveFile(String inFilename,String outFilename)
{
    FileInputStream in = null;
    FileOutputStream out = null;
    long totalAudioLen = 0;
    long totalDataLen = totalAudioLen + 36;
    long longSampleRate = frequency;
    int channels = 1;
    long byteRate = RECORDER_BPP * frequency * channels/8;

    byte[] data = new byte[bufferSize];

    try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;


            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                            longSampleRate, channels, byteRate);

            while(in.read(data) != -1){
                    out.write(data);
            }

            in.close();
            out.close();
    } catch (FileNotFoundException e) {
            e.printStackTrace();
    } catch (IOException e) {
            e.printStackTrace();
    }
}

private String getFilename()
{
    String filepath = Environment.getExternalStorageDirectory().getPath();
    File file = new File(filepath,AUDIO_RECORDER_FOLDER);

    if(!file.exists()){
            file.mkdirs();
    }

    return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + AUDIO_RECORDER_FILE_EXT_WAV);
}

}
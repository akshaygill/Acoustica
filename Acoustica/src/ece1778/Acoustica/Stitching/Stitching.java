package ece1778.Acoustica.Stitching;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import ece1778.Acoustica.R;
import ece1778.Acoustica.Recording.TestAudioCaptureWithThreshold;
import ece1778.Acoustica.Recording.TestAudioCaptureWithThreshold.RecordAudio;
import java.lang.Object;
import android.widget.Toast;

public class Stitching
{	
	private static final String TAG = TestAudioCaptureWithThreshold.class.getSimpleName();
	private static final int RECORDER_BPP = 16;
	private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
	private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
	private static final String AUDIO_RECORDER_TEMP_OUTPUT_FILE = "record_temp_out.raw";
	private static final String AUDIO_RECORDER_WAV_OUTPUT_FILE = "final_out.wav";

	FileOutputStream os = null;

	int bufferSize = 1000000;
	int frequency = 44100; //8000;
	int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
	int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	
	short threshold=1800; 
	Button stitchButton;
	boolean debug=false;

	int storeNum=0;
	int index=0;
	int bufferLen=index;
	int foundPeak=0;
	byte[] byteBuffer;
	int i=0;
	GenerateWave waveGen;
	   
	public Boolean BeginStitch(File file1, File file2)
	{			
		  /* File file1 = null;
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
		   */
		   Stitch(file1,file2);
		   
		   return true;
	}
	
	public void startWaveGen()
	{
	   Log.w(TAG, "Starting Wave Generation");
	   Handler handler = new Handler(); 
	   handler.postDelayed(new Runnable() { 
	        public void run() { 
	           //elapsedTime=0;
	           waveGen = new GenerateWave();
	           waveGen.execute();
	           //startButton.setText("RESET");
	        } 
	   }, 500); 
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
		        
		        try
		        {
			 	   is1.close();
			 	   is2.close();
		        }
		        
	             catch (IOException e) 
	             {
	                e.printStackTrace();
	             }
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
	    try
	    {
	    	os.close();
	    }
	    catch (IOException e) 
        {
           e.printStackTrace();
        }
	    
	    //Generate output wave file
	    startWaveGen();
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

	private String getFilename()
	{
	    String filepath = Environment.getExternalStorageDirectory().getPath();
	    File file = new File(filepath,AUDIO_RECORDER_FOLDER);

	    if(!file.exists()){
	            file.mkdirs();
	    }

	    return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_WAV_OUTPUT_FILE);
	}	
	
	public class GenerateWave extends AsyncTask<Void, Double, Void> 
	{
		@Override
		protected Void doInBackground(Void... arg0) 
		{
			
			copyWaveFile(getTempOutputFilename(), getFilename());
			return null;
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
	
	}
}

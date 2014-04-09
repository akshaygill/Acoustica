package ece1778.Acoustica;

import ece1778.Acoustica.Filebrowser.FileBrowse;
import ece1778.Acoustica.Recording.*;
import ece1778.Acoustica.Stitching.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

public class Main extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}

	/*****************************************************************/
    /*                 START Button user click method                */
    /*****************************************************************/
    public void AcquireTempo(View view) { 	
    	
    	/***** setting the parameters for recording and starting the recording activity*/
    	Intent listenIntent = new Intent(this, ListenActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt("tempo_value", 120);
		bundle.putInt("min_freq", 20);
		bundle.putInt("max_freq", 20000);
		
		listenIntent.putExtras(bundle);
		startActivity(listenIntent);        

    }
    
    /*****************************************************************/
    /*                 EXIT Button user click method                 */
    /*****************************************************************/
    public void exitMain(View view) {
    	finish();
    }
    
}

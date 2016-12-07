package com.example.dustin.ModRSVP;


import android.app.DialogFragment;
import android.content.Context;

import android.content.res.AssetManager;
import android.os.Environment;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.os.Vibrator;

import com.opencsv.CSVReader;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.spritzinc.android.sdk.SpritzSDK;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

// this is the section of imports for epub reading
import nl.siegmann.epublib.domain.Book;

import nl.siegmann.epublib.epub.EpubReader;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "Log";
    Vibrator v;
    TextView firstTextView;

    //all of these should be added to all apps
    ImageButton slowdown;
    ImageButton speedup;

    ImageButton inctexsize;
    ImageButton dectexsize;
    ImageButton pausedInd;

    ImageButton player;
    ImageButton pauser;

    MenuItem hider;
    public int lastloc = 2;






    //timer vars
    private long starttime;
    private long endTime;
    private boolean started = false;


    public String literature;
    public InputStream is = null;
    private int counter = 0;

    public int textsize = 32;
    public int textchg = 5;
    public  String[] litSplit;
    public int wpm = 250;
    public boolean threadSuspended = false;
    public boolean threadStarted = false;
    public boolean hidden = false;
    Thread RSVP;
    private Toast toast;
    private int pnum;
    private int mode;
    private boolean passageflag = false;
    private String passage;
    private String[][] orders;

    private int PassageCount=0;




    private long mDelay= 60000/wpm;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpritzSDK.init(this,
                "395014a08680bbf1c",
                "5d933fc1-3ea5-4431-aa21-0ff857dccb98",
                "https://sdk.spritzinc.com/android/examples/login_success.html");
        setContentView(R.layout.activity_main);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        slowdown = (ImageButton) findViewById(R.id.slower);
        speedup = (ImageButton) findViewById(R.id.faster);

        inctexsize = (ImageButton) findViewById(R.id.inctex);
        dectexsize = (ImageButton) findViewById(R.id.dectex);

        pausedInd = (ImageButton) findViewById(R.id.pause_ind);
        pausedInd.setVisibility(View.VISIBLE);

        hider = (MenuItem) findViewById(R.id.hide_button);

        pauser = (ImageButton) findViewById(R.id.imageButton4);
        pauser.setVisibility(View.INVISIBLE);
        player = (ImageButton) findViewById(R.id.imageButton3);


        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);

        loadText("AIWL.epub",lastloc);



        //DialogFragment modeFrag = new ModeDialogFragment();


        //modeFrag.show(getFragmentManager(),"Mode Frag");

        loadorders("Random_Orders_Spritz.csv");


        System.out.println(litSplit[1]);
        firstTextView = (TextView) findViewById(R.id.word_landing);
        firstTextView.setSelected(true);




        RSVP = new Thread(new Runnable() {
            @Override
            public void run() {

                synchronized (this){
                while(true)
                    try {

                        if(!threadSuspended) {

                            Thread.sleep(mDelay);


                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (counter<litSplit.length&!passageflag) {

                                        firstTextView.setText(litSplit[counter]);
                                        counter=counter+1;
                                        mDelay=(long)((60000*delayValue()) / wpm);

                                    }
                                    if (counter==litSplit.length){
                                        passageflag=true;
                                        lastloc++;
                                        displayText("next chapter");
                                        loadText("AIWL.epub", lastloc);


                                    }
                                    if (passageflag){
                                        endTime = System.nanoTime()-starttime;

                                        threadSuspended = true;
                                        v.vibrate(1000);
                                        pauser.setVisibility(View.INVISIBLE);
                                        player.setVisibility(View.VISIBLE);
                                        passageflag = false;
                                        String pdata = "data.txt";
                                        writeStringToTextFile(Integer.toString(pnum)+"\t"+"Spritz"+"\t"+passage+"\t"+Long.toString(endTime)+"\t"+Integer.toString(wpm)+"\t"+Integer.toString(textsize)+"\t"+Long.toString(litSplit.length)+"\n", pdata);
                                        counter++;
                                    }
                                }
                            });

                        }

                    } catch(InterruptedException e){
                        System.out.println("got interrupted!");
                    }

                }

            }
        });



    }



    public void onPnumSelectValue(int which) {
        pnum = which+1;
        displayText("Participant " + Integer.toString(pnum)+ " Selected");
    }
    public void onModeSelectValue(int id) {
        mode = id;
        if (mode==-2){
            DialogFragment pnumFrag = new PnumDialogFragment();
            pnumFrag.show(getFragmentManager(), "PnumFrag");
        }

    }

    public void onnextSelectValue(int id) {
        if (id == -2){

            if (PassageCount<1) {
                loadText("passage"+orders[pnum-1][PassageCount]+".txt",0);
                firstTextView.setText("X");
                PassageCount++;
            }
            else{
                displayText("You have completed all of the passages for this reading type");
            }


        }
    }

    public void NextRSVP(View view) {
        if(counter<litSplit.length){

            firstTextView.setText(litSplit[counter]);
            counter++;
            if (!threadSuspended) {
                threadSuspended = true;
                pauser.setVisibility(View.INVISIBLE);
                player.setVisibility(View.VISIBLE);
                pausedInd.setVisibility(View.VISIBLE);
            }
        }


    }

    public void PrevRSVP(View view) {

        if(counter>0){
            counter--;
            firstTextView.setText(litSplit[counter]);
            if (!threadSuspended) {
                threadSuspended = true;
                pauser.setVisibility(View.INVISIBLE);
                player.setVisibility(View.VISIBLE);
                pausedInd.setVisibility(View.VISIBLE);
            }

        }


    }
    private void displayText(final String message) {

        toast.setText(message);
        toast.show();
    }
    private void loadText(final String file, final int loc) {
        try {
            AssetManager am = this.getAssets();
            InputStream is = am.open(file);
            // Load Book from inputStream
            Book book = (new EpubReader()).readEpub(is);
            InputStream was = book.getSpine().getSpineReferences().get(loc).getResource().getInputStream();

            passage=file;
            is.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(was));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line +"\n");

            }
            was.close();
            literature = sb.toString();
            counter = 0;
            litSplit = Html.fromHtml(literature).toString().split("\\s+");
            started = false;
            threadSuspended = true;
            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
            pausedInd.setVisibility(View.VISIBLE);



        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    public void slowRSVP(View view) {
        wpm -= 10;

        // show end user new wpm

        displayText(Long.toString(wpm) + " wpm");

    }


    public void fastRSVP(View view) {
        wpm += 10;

        // show end user new wpm
        displayText(Long.toString(wpm) + " wpm");

    }

    public double delayValue(){
        double delayer =1;

        if (counter<litSplit.length) {
            String tempword = litSplit[counter];

        if (counter>0&counter<litSplit.length&(tempword.length())>0) {
            delayer = Math.pow(((tempword.length()) / 5f), .5);
        }} else{
            delayer = 1;
        }

        return delayer;
    }

    public void StartRSVP(View view) {
        if (!threadStarted){
            RSVP.start();
            threadStarted = true;
            player.setVisibility(View.INVISIBLE);
            pauser.setVisibility(View.VISIBLE);
            pausedInd.setVisibility(View.INVISIBLE);
        }
        if (!started){
            starttime=System.nanoTime();
            started = true;

        }

        if (threadSuspended&counter!=litSplit.length+1){
            threadSuspended = false;
            player.setVisibility(View.INVISIBLE);
            pauser.setVisibility(View.VISIBLE);
            pausedInd.setVisibility(View.INVISIBLE);

        }
        if (counter==litSplit.length+1 & mode==-2){
            DialogFragment nextPasFrag = new NextPassageDialog();
            nextPasFrag.show(getFragmentManager(), "nextPasFrag");
        }

    }
    public void pauseRSVP(View view) {


        if (!threadSuspended) {
            threadSuspended = true;
            pauser.setVisibility(View.INVISIBLE);
            player.setVisibility(View.VISIBLE);
            pausedInd.setVisibility(View.VISIBLE);
        }



    }

    public void incTextsize(View view){
        textsize+=textchg;
        firstTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);

        // show end user new font size
        displayText(Integer.toString(textsize) + "pt");



    }

    public void decTextsize(View view){
        textsize-=textchg;
        firstTextView.setTextSize(TypedValue.COMPLEX_UNIT_PT, textsize);


        // show end user new font size
        displayText(Integer.toString(textsize) + "pt");


    }






    //action bar stuff
    public boolean onCreateOptionsMenu(Menu menu){
        //make it appear
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
        //Deals with the actual clicking
        switch (item.getItemId()) {
            case R.id.demo_1:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("practice1.txt",0);
                displayText("Demo Text Initiated");
                firstTextView.setText("X");
                return true;
            case R.id.passage_1:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage1.txt",0);
                displayText("Passage 1 Initiated");
                firstTextView.setText("X");
                return true;
            case R.id.passage_2:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage2.txt",0);
                displayText("Passage 2 Initiated");
                firstTextView.setText("X");
                return true;
            case R.id.passage_3:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage3.txt",0);
                displayText("Passage 3 Initiated");
                firstTextView.setText("X");
                return true;
            case R.id.passage_4:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage4.txt",0);
                displayText("Passage 4 Initiated");
                firstTextView.setText("X");
                return true;
            case R.id.passage_5:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage5.txt",0);
                displayText("Passage 5 Initiated");
                firstTextView.setText("X");
                return true;
            case R.id.passage_6:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage6.txt",0);
                displayText("Passage 6 Initiated");
                firstTextView.setText("X");
                return true;
            case R.id.passage_7:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage7.txt",0);
                displayText("Passage 7 Initiated");
                firstTextView.setText("X");
                return true;
            case R.id.passage_8:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage8.txt",0);
                displayText("Passage 8 Initiated");
                firstTextView.setText("X");
                return true;
            case R.id.passage_9:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage9.txt",0);
                displayText("Passage 9 Initiated");
                firstTextView.setText("X");
                return true;
            case R.id.passage_10:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                loadText("passage10.txt",0);
                displayText("Passage 10 Initiated");
                firstTextView.setText("X");
                return true;
            case R.id.exit_the_app:
                finish();
                return true;

            case R.id.hide_button:

                if (!hidden) {
                    slowdown.setVisibility(View.INVISIBLE);
                    speedup.setVisibility(View.INVISIBLE);

                    inctexsize.setVisibility(View.INVISIBLE);
                    dectexsize.setVisibility(View.INVISIBLE);
                    hidden = true;

                } else {
                    slowdown.setVisibility(View.VISIBLE);
                    speedup.setVisibility(View.VISIBLE);

                    inctexsize.setVisibility(View.VISIBLE);
                    dectexsize.setVisibility(View.VISIBLE);
                    hidden = false;

                }
                return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }


    public File getDataStorageDir(String DataName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), DataName);
        if (!file.mkdirs()) {
            Log.e(LOG_TAG, "Directory not created");
        }
        return file;
    }

    private void writeStringToTextFile(String s, String f){
        File sdCard = getDataStorageDir(f);
        File dir = new File (sdCard.getAbsolutePath());
        dir.mkdirs();
        File file = new File(dir, f);
        try{
            FileOutputStream f1 = new FileOutputStream(file,true); //True = Append to file, false = Overwrite
            PrintStream p = new PrintStream(f1);
            p.print(s);
            p.close();
            f1.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }   }

    private void loadorders(final String file) {
        try {
            orders = new String[30][2];


            InputStreamReader is = new InputStreamReader(getAssets().open(file));


            CSVReader reader = new CSVReader(new BufferedReader(is));
            String[] nextLine;


            int i = 0;

            while ((nextLine = reader.readNext()) != null){
                orders[i][0]=nextLine[0];
                orders[i][1]=nextLine[1];
                i++;
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

    }



}

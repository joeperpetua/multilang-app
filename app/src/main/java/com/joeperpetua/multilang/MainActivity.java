package com.joeperpetua.multilang;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONArrayRequestListener;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    public ArrayList<String[]> langs = new ArrayList<String[]>();

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidNetworking.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        // to parse:
        // language: langs.get(index)[0]
        // id: langs.get(index)[1]

        String[] temp = {"en", ""};
        langs.add(temp);

        String[] temp1 = {"es", ""};
        langs.add(temp1);

        String[] temp2 = {"fr", ""};
        langs.add(temp2);

        String[] temp3 = {"de", ""};
        langs.add(temp3);

        String[] temp4 = {"", ""};
        langs.add(temp4);



        EditText editText = (EditText) findViewById(R.id.mainInput);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    try {
                        runTranslation(v);
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handled = true;
                }
                return handled;
            }
        });

        final ConstraintLayout cl = (ConstraintLayout) findViewById(R.id.constraintLayout);

        final LinearLayout lm = (LinearLayout) findViewById(R.id.linearMain);
        lm.setBaselineAligned(false);

        LinearLayout.LayoutParams langIndicatorParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        langIndicatorParams.setMargins(16, 32, 0, 16);

        LinearLayout.LayoutParams langFieldParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        langFieldParams.setMargins(16, 32, 16, 16);


        for(int i=0; i < langs.size(); i++)
        {
            // Create LinearLayout
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            //ll.setGravity(Gravity.FILL);
            ll.setBaselineAligned(false);

            // Create langIndicator TextView
            TextView langIndicator = new TextView(this);
            langIndicator.setLayoutParams(langIndicatorParams);
            if (i != langs.size() - 1){
                langIndicator.setText(langs.get(i)[0].toUpperCase() + ": ");
            }
            langIndicator.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            ll.addView(langIndicator);

            // Create langField TextView
            TextView langField = new TextView(this);
            langField.setGravity(Gravity.FILL);

            Integer viewIDtoInteger = new Integer(0);

            if (i == langs.size() - 1){
                // generate dynamic ID and store it in langs array
                viewIDtoInteger = new Integer(999999999);

                // langField.setBackgroundColor(Color.parseColor("#000000"));
            }else{
                // generate dynamic ID and store it in langs array
                viewIDtoInteger = new Integer(langField.generateViewId());


            }

            // assign the id to the view
            String viewIDtoString = viewIDtoInteger.toString();
            String[] tempArray = {langs.get(i)[0], viewIDtoString};
            langs.set(i, tempArray);
            langField.setId(Integer.parseInt(langs.get(i)[1]));

            langField.setLayoutParams(langFieldParams);
            langField.setTextIsSelectable(true);
            langField.setText("");
            langField.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f);
            ll.addView(langField);


            // add constraints


            //Add to LinearLayout defined in XML
            lm.addView(ll);
            Log.i("TAG", "onCreate: added views for " + i);
        }

    }

    public void translate(String target, String text, Integer id){
        AndroidNetworking.get("https://apiml.joeper.myds.me/translate")
                .addQueryParameter("tl", target)
                .addQueryParameter("q", text)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            TextView transField = (TextView) findViewById(id);
                            transField.setText(response.getString("result"));
                            Log.i("TAG", "onResponse: " + response.getString("result"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e("Api get error", "onError: ", error);
                        Toast toast = Toast.makeText(getApplicationContext(), "API error: " + error, Toast.LENGTH_SHORT);
                        toast.show();
                        Exception exception = new Exception("Error occurred when translating");
                        exception.printStackTrace();
                    }
                });
        ;
    }


    public void runTranslation(View view) throws ExecutionException, InterruptedException {
        // get field to translate
        EditText mainInput = (EditText) findViewById(R.id.mainInput);
        String mainInputText = mainInput.getText().toString();

        if(!mainInputText.isEmpty()){
            // run the translation and get result
            Log.i("", "runTranslation: call API");

            hideKeyboard(this);

            Toast toast = Toast.makeText(getApplicationContext(), "Translating...", Toast.LENGTH_SHORT);
            toast.show();

            for (int i = 0; i < langs.size(); i++){
                if (i != langs.size() - 1){
                    translate(langs.get(i)[0], mainInputText, Integer.parseInt(langs.get(i)[1]));
                }else{
                    Handler handler = new Handler();
                    int finalI = i;
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            Log.i("", "runTranslation: skipping last item");
                            TextView previousItem = (TextView) findViewById(Integer.parseInt(langs.get(finalI -1)[1]));

                            TextView lastItem = (TextView) findViewById(Integer.parseInt("999999999"));
                            Log.i("", "runTranslation: last height === " + lastItem.getHeight());


                            LinearLayout.LayoutParams langFieldParamsLast = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (previousItem.getHeight() * 0.75) );
                            langFieldParamsLast.setMargins(16, 32, 16, 16);

                            lastItem.setLayoutParams(langFieldParamsLast);


                            Log.i("", "runTranslation: last height added === " + lastItem.getHeight());
                        }
                    }, 5000);   //3 seconds
                }

            }
        }

    }
}
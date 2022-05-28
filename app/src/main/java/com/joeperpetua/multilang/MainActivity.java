package com.joeperpetua.multilang;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
    public ArrayList<String[]> langs = new ArrayList<>();

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



        EditText editText = findViewById(R.id.mainInput);
        editText.setOnEditorActionListener((view, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                try {
                    runTranslation(view);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e("TAG", "onCreate: ",e);
                }
                handled = true;
            }
            return handled;
        });

        final LinearLayout lm = findViewById(R.id.linearMain);
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

            int viewIDtoInteger;

            if (i == langs.size() - 1){
                // generate dynamic ID and store it in langs array
                viewIDtoInteger = 999999999;

                // langField.setBackgroundColor(Color.parseColor("#000000"));
            }else{
                // generate dynamic ID and store it in langs array
                viewIDtoInteger = View.generateViewId();


            }

            // assign the id to the view
            String viewIDtoString = Integer.toString(viewIDtoInteger);
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

        CharSequence text = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        String text_formatted;
        if (text != null){
            text_formatted = text.toString();
            Log.i("INTENT", "onCreate: intent text: " + text_formatted);
            EditText mainInput = findViewById(R.id.mainInput);
            mainInput.setText(text_formatted);
            translate(langs, text_formatted);
        }

    }

    public void translate(ArrayList<String[]> target, String text){

        //Log.i("TAG", "translate: ----" + langs);
        StringBuilder tl = new StringBuilder();
        for (int i = 0; i < target.size(); i++) {
            // concatenate tl by a comma, except for the last element
            if (i == target.size() - 1){
                tl.append(target.get(i)[0]);
            }else {
                tl.append(target.get(i)[0]).append(",");
            }
        }

        //AndroidNetworking.get(url)
        AndroidNetworking.get("https://apiml.joeper.myds.me/translate")
                .addQueryParameter("tl", tl.toString())
                .addQueryParameter("q", text)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        JSONArray results;
                        try {
                            results = response.getJSONArray("translations");
                            Log.i("TAG", "onResponse: " + results);

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject tmp = results.getJSONObject(i);
                                TextView transField = findViewById(Integer.parseInt(target.get(i)[1]));
                                transField.setText(tmp.getString("result"));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        fixLastItem();
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e("Api get error", "onError: " + error.getErrorBody() + "\n" + error.getResponse());
                        Toast toast = Toast.makeText(getApplicationContext(), "API error: " + error, Toast.LENGTH_SHORT);
                        toast.show();
                        Exception exception = new Exception("Error occurred when translating");
                        exception.printStackTrace();
                    }
                });
    }

    public void fixLastItem(){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                int last = langs.size() - 1;
                Log.i("", "runTranslation: skipping last item");
                TextView previousItem = findViewById(Integer.parseInt(langs.get(last - 1)[1]));

                TextView lastItem = findViewById(Integer.parseInt("999999999"));
                Log.i("", "runTranslation: last height === " + lastItem.getHeight());


                LinearLayout.LayoutParams langFieldParamsLast = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (previousItem.getHeight() * 0.75) );
                langFieldParamsLast.setMargins(16, 32, 16, 16);

                lastItem.setLayoutParams(langFieldParamsLast);


                Log.i("", "runTranslation: last height added === " + lastItem.getHeight());
            }
        }, 1000);   //3 seconds
    }


    public void runTranslation(View view) throws ExecutionException, InterruptedException {
        // get field to translate
        EditText mainInput = findViewById(R.id.mainInput);
        String mainInputText = mainInput.getText().toString();

        if(!mainInputText.isEmpty()){
            // run the translation and get result
            Log.i("", "runTranslation: call API");

            hideKeyboard(this);

            Toast toast = Toast.makeText(getApplicationContext(), "Translating...", Toast.LENGTH_SHORT);
            toast.show();

            translate(langs, mainInputText);

        }

    }
}
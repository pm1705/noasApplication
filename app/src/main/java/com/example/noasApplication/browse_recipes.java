package com.example.noasApplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.noasApplication.main_screen_activity.current_user;

public class browse_recipes extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    EditText search_input;
    ListView results;
    Switch kosher_switch;
    Spinner time;

    String search_text, current_id, current_name, current_description, recieved_option;
    int current_time;
    boolean current_kosher;

    ArrayList<String> recipes_ids, recipes_names, recipes_descriptions, recipes_cal, matching_results, matching_results_info;
    ArrayList<Integer> recipes_time;
    ArrayList<Boolean> recipes_kosher;

    Intent recieved_intent, show_recipe;

    ValueEventListener stuListener;

    String[] time_lst = {"All", "Breakfast", "Lunch", "Dinner"};
    int time_int;

    String[] image_links;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_recipes);

        search_input = (EditText) findViewById(R.id.search_choose_product);
        results = (ListView) findViewById(R.id.results);
        kosher_switch = (Switch) findViewById(R.id.kosher_switch);
        time = (Spinner) findViewById(R.id.time);

        time.setOnItemSelectedListener(this);
        time_int = -1;
        ArrayAdapter<String> gender_adp = new ArrayAdapter<String>(this,
                R.layout.support_simple_spinner_dropdown_item,time_lst);
        time.setAdapter(gender_adp);

        results.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        results.setOnItemClickListener(this);

        search_text = search_input.getText().toString();

        recieved_intent = getIntent();
        recieved_option = recieved_intent.getStringExtra("option");

        show_recipe = new Intent(this, RecipePage.class);

        recipes_ids = new ArrayList<String>();
        recipes_names = new ArrayList<String>();
        recipes_descriptions = new ArrayList<String>();
        matching_results = new ArrayList<String>();
        matching_results_info = new ArrayList<String>();
        recipes_time = new ArrayList<Integer>();
        recipes_kosher = new ArrayList<Boolean>();
        recipes_cal = new ArrayList<String>();


        stuListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dS) {

                recipes_ids.clear();
                recipes_names.clear();
                recipes_descriptions.clear();
                recipes_time.clear();
                recipes_kosher.clear();
                recipes_cal.clear();

                for(DataSnapshot data : dS.getChildren()) {

                    if (recieved_option.equals("regular")) {
                        recipes_ids.add("" + data.getKey());
                        recipes_names.add("" + data.child("name").getValue().toString());
                        recipes_descriptions.add("" + data.child("description").getValue().toString());
                        recipes_time.add(Integer.valueOf(data.child("time").getValue().toString()));
                        recipes_kosher.add((Boolean) data.child("kosher").getValue());
                        recipes_cal.add("" + data.child("cal").getValue().toString());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        };
        com.example.noasApplication.FBRefs.refRecipes.addValueEventListener(stuListener);
    }


    public void update_results(View view) {

        matching_results.clear();
        matching_results_info.clear();

        search_text = search_input.getText().toString();

        for (int i = 0; i< recipes_ids.size(); i++){

            current_id = recipes_ids.get(i);
            current_name = recipes_names.get(i);
            current_description = recipes_descriptions.get(i);
            current_kosher = recipes_kosher.get(i);
            current_time = recipes_time.get(i);

            if ((current_name.contains(search_text) || current_description.contains(search_text))){
                if (((kosher_switch.isChecked() && current_kosher) || !kosher_switch.isChecked()) &&
                        (current_time == time_int || time_int == 0) ) {
                    matching_results.add(current_id);
                    matching_results_info.add(current_name + ", " + current_description + " (" + recipes_cal.get(i) + ")");

                    int finalI = i;
                    image_links = new String[recipes_ids.size()];
                    FBRefs.storageRef.child("recipe_images").child((Integer.toString(i))).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            image_links[finalI] = uri.toString();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            image_links[finalI] = "exit";
                        }
                    });
                }
            }
        }

        image_links = new String[]{"https://firebasestorage.googleapis.com/v0/b/myfinalproj-6e9bf.appspot.com/o/a%40a9?alt=media&token=310eca64-a17b-47e1-84bc-02b148e56321",
                "https://firebasestorage.googleapis.com/v0/b/myfinalproj-6e9bf.appspot.com/o/a%40a9?alt=media&token=310eca64-a17b-47e1-84bc-02b148e56321",
                "https://firebasestorage.googleapis.com/v0/b/myfinalproj-6e9bf.appspot.com/o/a%40a9?alt=media&token=310eca64-a17b-47e1-84bc-02b148e56321"};
        customAdapter customadp = new customAdapter(getApplicationContext(),
                matching_results_info, image_links);
        results.setAdapter(customadp);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        show_recipe.putExtra("id",matching_results.get(i));
        startActivity(show_recipe);
    }

    public void onPause() {
        if (stuListener!=null) {
            com.example.noasApplication.FBRefs.refRecipes.removeEventListener(stuListener);
        }
        super.onPause();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        time_int = i;
        update_results(view);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
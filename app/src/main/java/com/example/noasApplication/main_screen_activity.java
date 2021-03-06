package com.example.noasApplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.BarringInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class main_screen_activity extends AppCompatActivity {

    Intent peronal_page_intent, new_recipe_intent, browse_recipes_intent, add_restaurant_intent,
            back_to_temp, vitamin_table_intent, bar_code_intent, weights;

    int logged_user_id; // logged_user_id
    String log; //logged_user_id_to_str

    ArrayList<String> recipes, favorites;

    TextView info_display, daily_cals;
    public static com.example.noasApplication.User current_user;

    ImageView pfpview;
    Uri curImage;

    double cal_goal;


    SharedPreferences.Editor editor; // זיכרון פנימי

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen_activity);

        SharedPreferences logged_information = getSharedPreferences("INFO",MODE_PRIVATE);
        editor = logged_information.edit();

        logged_user_id = logged_information.getInt("key_id",0);
        log = Integer.toString(logged_user_id);

        info_display = (TextView) findViewById(R.id.userinfo);

        pfpview = (ImageView) findViewById(R.id.pfpview);

        daily_cals = (TextView)findViewById(R.id.daily_cals);
        cal_goal = calc_cal_goal();

        daily_cals.setText("Daily calories counter: " + cal_goal);



        FBRefs.refUsers.addListenerForSingleValueEvent(new ValueEventListener() { // מוציא מידע
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                for(DataSnapshot data : dS.getChildren()) {
                    if (data.getKey().equals(log)){

                        System.out.println(log);

                        current_user = new com.example.noasApplication.User(
                                data.child("username").getValue().toString(),
                                data.child("email").getValue().toString(),
                                data.child("pass").getValue().toString(),
                                data.child("location").getValue().toString(),
                                Integer.valueOf(data.child("gender").getValue().toString()),
                                Integer.valueOf(data.child("weight").getValue().toString()),
                                Integer.valueOf(data.child("height").getValue().toString()),
                                Integer.valueOf(data.child("age").getValue().toString()),
                                Integer.valueOf(data.child("activityLevel").getValue().toString()),
                                logged_user_id);

                        FBRefs.storageRef.child(current_user.getEmail()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                curImage = uri;
                                System.out.println(curImage.toString());
                                Picasso.get().load(curImage.toString()).into(pfpview);
                                info_display.setText("Logged in as: " + current_user.getName());
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                                info_display.setText("Logged in as: " + current_user.getName());
                            }
                        });
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        peronal_page_intent = new Intent(this, personal_page.class);
        new_recipe_intent = new Intent(this, AddRecipe.class);
        browse_recipes_intent = new Intent(this, browse_recipes.class);
        add_restaurant_intent = new Intent(this, AddRestaruant.class);
        back_to_temp = new Intent(this, temp_screen.class);
        vitamin_table_intent = new Intent(this, VitaminTable.class);
//        weights = new Intent(this, WeightsTB.class);
    }

    public void personal_page(View view) {
        startActivity(peronal_page_intent);
    }

    public void vitamin_table_button(View view) {
        startActivity(vitamin_table_intent);
    }

    public void new_recipe(View view) {
        startActivity(new_recipe_intent);
    }

    public void new_restaurant(View view) { // רק לאדמינים
        if (current_user.getEmail().equals("nr8112@bs.amalnet.k12.il") && current_user.getPassword().equals("noa123456")){startActivity(add_restaurant_intent);}
    }

    public void log_out(View view) {
        log_out_help();
    }

    public void log_out_help() {
        editor.putBoolean("logged_in",false);
        editor.putInt("key_id",-1);
        editor.commit();
        startActivity(back_to_temp);
    }

    public void browse_recipes(View view) {
        browse_recipes_intent.putExtra("option", "regular");
        startActivity(browse_recipes_intent);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        String itm = item.getTitle().toString();

        if (itm.equals("Log Out")) log_out_help();
        if (itm.equals("Account")) startActivity(peronal_page_intent);
        if (itm.equals("Recipes")){
            browse_recipes_intent.putExtra("option", "regular");
            startActivity(browse_recipes_intent);
        }
//        if (itm.equals("Weights Table")){
//            startActivity(weights);
//        }

        return true;
    }

    private double calc_cal_goal(){
        int gender, age, height, weight, activity_lvl;
        double BMR = 0;
        gender = current_user.getGender();
        age = current_user.getAge();
        height = current_user.getHeight();
        weight = current_user.getWeight();
        activity_lvl = current_user.getActivity_level();

        if (gender == 0) { // male
            BMR = 66 + 13.8 * weight + 5 * height - 6.8 * age;

        }
        else if (gender == 1) { // female
            BMR = 655 + 9.6 * weight + 1.8 * weight - 4.7 * age;
        }

        switch(activity_lvl) {
            case 1:
                BMR *= 1.2;
                break;
            case 2:
                BMR *= 1.375;
                break;
            case 3:
                BMR *= 1.55;
                break;
            case 4:
                BMR *= 1.725;
                break;
            case 5:
                BMR *= 1.9;
                break;
        }

        return BMR;
    }

    public void inc_goal(View view) {
        cal_goal += 300;
        daily_cals.setText("Daily calories counter: " + cal_goal);
    }

    public void dep_goal(View view) {
        cal_goal -= 300;
        daily_cals.setText("Daily calories counter: " + cal_goal);
    }
}
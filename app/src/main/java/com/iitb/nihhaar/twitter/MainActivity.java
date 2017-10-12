package com.iitb.nihhaar.twitter;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private SearchView searchView;
    private List<String> suggestions;
    private final static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // App Bar or Action Bar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        myPostsFragment();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main_menu,menu);

        /* Get the search view */
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setMaxWidth(Integer.MAX_VALUE);

        /* Handle back press on search view */
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                /* Replace with main fragment here. This is called when the search bar is closed (back button pressed) */
                Toast.makeText(MainActivity.this, "Pressed", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        /* Setting the dropdown width */
        final AutoCompleteTextView searchEditText = (AutoCompleteTextView) searchView.findViewById(R.id.search_src_text);
        final View dropDownAnchor = searchView.findViewById(searchEditText.getDropDownAnchor());
        if (dropDownAnchor != null) {
            dropDownAnchor.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {

                    // calculate width of DropdownView
                    int point[] = new int[2];
                    dropDownAnchor.getLocationOnScreen(point);
                    // x coordinate of DropDownView
                    int dropDownPadding = point[0] + searchEditText.getDropDownHorizontalOffset();

                    Rect screenSize = new Rect();
                    getWindowManager().getDefaultDisplay().getRectSize(screenSize);
                    // screen width
                    int screenWidth = screenSize.width();
                    // set DropDownView width
                    searchEditText.setDropDownWidth(screenWidth - dropDownPadding * 2);
                }
            });
        }

        /* Storing the suggestions */
        suggestions = new ArrayList<>();

        /* Setup suggestion adapter */
        searchView.setSuggestionsAdapter(new SimpleCursorAdapter(
                MainActivity.this, android.R.layout.simple_list_item_1, null,
                new String[] { SearchManager.SUGGEST_COLUMN_TEXT_1 },
                new int[] { android.R.id.text1 }, 0));

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                //searchView.setQuery(suggestions.get(position), false);
                //searchView.clearFocus();
                changeUserFragment(suggestions.get(position));
                return true;
            }
        });

        /* Set listeners on the search view */
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public  boolean onQueryTextChange(String newText){
                        // Do some filtering if required.
                        String url = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/SearchUser";
                        if (newText.length() >= 3)
                            new SearchSuggestionsFetch(MainActivity.this, new MyInterface() {
                                @Override
                                public void myMethod(Cursor result) {
                                    searchView.getSuggestionsAdapter().changeCursor(result);
                                }
                            }).execute(url, newText);
                        else
                            searchView.getSuggestionsAdapter().changeCursor(null);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextSubmit(String query){
                        String url = "http://" + AppUtils.servIP + ":" + AppUtils.servPort + "/" + AppUtils.webApp + "/GetUserId";
                        new UserFetch(MainActivity.this).execute(url, query);
                        return false;
                    }
                }
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /* Handle your other action bar items.
         * Return true if handled else false.
         */
        switch(item.getItemId()){
            case R.id.action_search:
                return true;
            case R.id.add_post:
                Intent addPost = new Intent(MainActivity.this, AddPostActivity.class);
                startActivity(addPost);
                return true;
            case R.id.view_posts:
                return true;
            case R.id.logout:
                AppUtils.logOut(this);
                return true;
            default:
                return false;
        }

    }

    private void myPostsFragment(){
        // Create new fragment and transaction
        Fragment newFragment = new PostsFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // Don't add the transaction to the back stack
        transaction.add(R.id.fragment_container, newFragment);

        // Commit the transaction
        transaction.commit();
    }

    private void changeUserFragment(String uid){
        // Create new fragment and transaction
        Fragment newFragment = new UserFragment();
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        newFragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // Don't add the transaction to the back stack
        transaction.replace(R.id.fragment_container, newFragment);

        // Commit the transaction
        transaction.commit();
    }

    private void changeInvalidUser(){
        // Create new fragment and transaction
        Fragment newFragment = new InvalidUserFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // Don't add the transaction to the back stack
        transaction.replace(R.id.fragment_container, newFragment);

        // Commit the transaction
        transaction.commit();
    }

    private class UserFetch extends AsyncTask<String, Void, String> {
        private String mTAG = "UserFetch";
        private Context mContext;

        public UserFetch(Context mContext){
            this.mContext = mContext;
        }

        @Override
        protected String doInBackground(String... args) {

            String result = "0";
            if(AppUtils.isNetworkAvailable(mContext)){
                try {
                    /* Authenticate with the server, if success store the credentials in shred prefs */
                    URLConnection conn = new URL(args[0]).openConnection();
                    conn.setDoOutput(true);

                    /* Create the post data */
                    String data = URLEncoder.encode("uid", "UTF-8")
                            + "=" + URLEncoder.encode(args[1], "UTF-8");

                    /* Post the data */
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(data);
                    writer.flush();
                    writer.close();

                    /* Get the response from server */
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    result = stringBuilder.toString();

                } catch (IOException ex){
                    result = "1";
                    ex.printStackTrace();
                }
            }

            // Parse the result
            if(!(result.equals("1") && result.equals("0"))) {
                try {
                    Log.d(mTAG, result);
                    JSONObject jsonObject = new JSONObject(result);
                    Boolean status = jsonObject.getBoolean("status");
                    if(status) {
                       Boolean isValid = jsonObject.getBoolean("data");
                        if(isValid){
                            changeUserFragment(args[1]);
                        }
                        else{
                            changeInvalidUser();
                        }
                    }
                    else
                        AppUtils.logOut(mContext);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }

    /*
     * We are using this interface to generate a toast message on UI thread using the asynctask.
     * Note that this can also be done using the 'runOnUiThread' method.
     */
    interface MyInterface {
        void myMethod(Cursor result);
    }

    /*
     * AsyncTask to fetch suggestions from http server
     */
    public class SearchSuggestionsFetch extends AsyncTask<String, Void, Cursor> {

        private String mTAG = "SearchSuggestionsFetch";
        private Context mContext;
        private MyInterface mListener;

        public SearchSuggestionsFetch(Context mContext, MyInterface mListener){
            this.mContext = mContext;
            this.mListener = mListener;
        }

        private final String[] sAutocompleteColNames = new String[] {
                BaseColumns._ID,                         // necessary for adapter
                SearchManager.SUGGEST_COLUMN_TEXT_1      // the full search term
        };

        @Override
        protected Cursor doInBackground(String... params) {

            MatrixCursor cursor = new MatrixCursor(sAutocompleteColNames);

            /* Get suggestions from server */
            String result = "0";
            if(AppUtils.isNetworkAvailable(mContext)) {
                try {
                    /* Authenticate with the server, if success store the credentials in shred prefs */
                    URLConnection conn = new URL(params[0]).openConnection();
                    conn.setDoOutput(true);

                    /* Get the id from shared prefs */
                    String data = URLEncoder.encode("uid", "UTF-8")
                            + "=" + URLEncoder.encode(params[1], "UTF-8");

                    /* Post the data */
                    OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
                    writer.write(data);
                    writer.flush();
                    writer.close();

                    /* Get the response from server */
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    result = stringBuilder.toString();

                } catch (IOException ex) {
                    result = "1";
                    Log.e(mTAG, "Unable to connect to server");
                    ex.printStackTrace();
                }
            }

            // Parse your search terms into the MatrixCursor
            if(!(result.equals("1") && result.equals("0"))) {
                try {
                    Log.d(mTAG, result);
                    JSONObject jsonObject = new JSONObject(result);
                    Boolean status = jsonObject.getBoolean("status");
                    if(status) {
                        JSONArray jsonArr = jsonObject.getJSONArray("data").getJSONArray(0);
                        for (int index = 0; index < jsonArr.length(); index++) {
                            JSONObject jobj = (JSONObject)jsonArr.getJSONObject(index);
                            String term = jobj.getString("uid") + " | " + jobj.getString("name") + " | " + jobj.getString("email");
                            suggestions.add(index, jobj.getString("uid"));
                            Object[] row = new Object[]{index, term};
                            cursor.addRow(row);
                        }
                    }
                    else
                        AppUtils.logOut(mContext);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor result) {
            if (mListener != null)
                mListener.myMethod(result);
        }

    }
}

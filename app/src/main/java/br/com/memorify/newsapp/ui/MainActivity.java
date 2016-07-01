package br.com.memorify.newsapp.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import br.com.memorify.newsapp.R;
import br.com.memorify.newsapp.model.Story;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final String STORY_RESULT_KEY = "STORY_RESULT_KEY";

    private RecyclerView storyListView;
    private View loadingView;
    private TextView emptyView;

    private StoryAdapter storyAdapter;

    private ArrayList<Story> stories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null && savedInstanceState.containsKey(STORY_RESULT_KEY)) {
            stories = savedInstanceState.getParcelableArrayList(STORY_RESULT_KEY);
        }

        bindViews();
        setupViews();

        if (savedInstanceState == null) {
            fetchNews();
        }
    }

    private void bindViews() {
        storyListView = (RecyclerView) findViewById(R.id.story_list);
        loadingView = findViewById(R.id.loading_progress);
        emptyView = (TextView) findViewById(R.id.empty_view);
    }

    private void setupViews() {
        storyListView.setHasFixedSize(true);
        storyListView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        storyAdapter = new StoryAdapter(getBaseContext(), stories, new StoryAdapter.ItemClickListener() {
            @Override
            public void onItemClicked(Story story) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(story.websiteURL));
                startActivity(browserIntent);
            }
        });
        storyListView.setAdapter(storyAdapter);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(STORY_RESULT_KEY, stories);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            fetchNews();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void fetchNews() {
        if (isOnline()) {
            new FetchNewsTask().execute();
        } else {
            showMessage(R.string.no_internet_connection);
        }
    }

    public class FetchNewsTask extends AsyncTask<Void, Void, Void> {

        private List<Story> result;
        private int errorResId;

        @Override
        protected void onPreExecute() {
            showProgress(true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;

            final String BOOKS_BASE_URL = "https://content.guardianapis.com/search";

            final String QUERY_PARAM = "q";
            final String API_KEY_PARAM = "api-key";
            final String SHOW_FIELDS_PARAM = "show-fields";
            final String PAGE_SIZE_PARAM = "page-size";

            final String QUERY_VALUE = "dota";
            final String API_KEY_VALUE = "ae950907-f27b-4ea9-a082-5fc2c268e756";
            final String SHOW_FIELDS_VALUE = "thumbnail,trailText";
            final String PAGE_SIZE_VALUE = "20";

            try {
                Uri builtUri = Uri.parse(BOOKS_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, QUERY_VALUE)
                        .appendQueryParameter(API_KEY_PARAM, API_KEY_VALUE)
                        .appendQueryParameter(SHOW_FIELDS_PARAM, SHOW_FIELDS_VALUE)
                        .appendQueryParameter(PAGE_SIZE_PARAM, PAGE_SIZE_VALUE)
                        .build();
                URL url = new URL(builtUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                switch (urlConnection.getResponseCode()) {
                    case 200:
                        String resultJsonStr = getStringFromInputStream(urlConnection.getInputStream());
                        result = getStoriesDataFromJSON(resultJsonStr);
                        break;
                    default:
                        errorResId = R.string.internal_error;
                        Log.e(TAG, "URL: " + url);
                        Log.e(TAG, "responseCode: " + urlConnection.getResponseCode());
                        Log.e(TAG, "responseMessage: " + urlConnection.getResponseMessage());
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
                errorResId = R.string.no_internet_connection;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                errorResId = R.string.internal_error;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            return null;
        }

        private String getStringFromInputStream(InputStream inputStream) throws IOException {
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // do nothing
                return null;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            if (buffer.length() == 0) {
                return null;
            }

            try {
                reader.close();
            } catch (final IOException e) {
                Log.e(TAG, "Error closing stream", e);
            }

            return buffer.toString();
        }

        private List<Story> getStoriesDataFromJSON(String resultJsonStr) throws JSONException {
            final String RESPONSE_KEY = "response";
            final String RESULTS_KEY = "results";

            List<Story> stories = new ArrayList<>();

            JSONObject resultJson = new JSONObject(resultJsonStr);
            JSONObject response = resultJson.getJSONObject(RESPONSE_KEY);
            if (response != null) {
                JSONArray results = response.optJSONArray(RESULTS_KEY);
                if (results != null) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject storyJSON = results.getJSONObject(i);
                        Story story = Story.fromJSON(storyJSON);
                        stories.add(story);
                    }
                }
            }
            return stories;
        }

        @Override
        protected void onCancelled() {
            loadingView.setVisibility(View.GONE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            showProgress(false);
            boolean hasError = errorResId != 0;
            if (hasError) {
                showMessage(errorResId);
            } else {
                if (result != null) {
                    stories.clear();
                    stories.addAll(result);
                    storyAdapter.notifyDataSetChanged();

                    if (result.size() == 0) {
                        showMessage(R.string.no_result);
                    } else {
                        showList();
                    }
                }
            }
        }
    }

    private void showMessage(@StringRes int message) {
        storyListView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
        emptyView.setText(message);
    }

    private void showList() {
        storyListView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
    }

    private void showProgress(boolean shouldShowProgress) {
        loadingView.setVisibility(shouldShowProgress ? View.VISIBLE : View.INVISIBLE);
    }
}

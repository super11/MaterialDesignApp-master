package com.example.javier.MaterialDesignApp.Tabs.TabsViews;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.javier.MaterialDesignApp.DetailActivity;
import com.example.javier.MaterialDesignApp.Model.AFilmModel;
import com.example.javier.MaterialDesignApp.Model.PageModel;
import com.example.javier.MaterialDesignApp.PlayerActivity;
import com.example.javier.MaterialDesignApp.R;
import com.example.javier.MaterialDesignApp.RecyclerView.RecyclerViewAdapters.DesignAdapter;
import com.example.javier.MaterialDesignApp.RecyclerView.RecyclerViewAdapters.FILMSAdapter;
import com.example.javier.MaterialDesignApp.RecyclerView.RecyclerViewClasses.Design;
import com.example.javier.MaterialDesignApp.RecyclerView.RecyclerViewDecorations.DividerItemDecoration;
import com.example.javier.MaterialDesignApp.RecyclerView.RecyclerViewUtils.ItemClickSupport;
import com.example.javier.MaterialDesignApp.Tabs.TabsUtils.SlidingTabLayout;
import com.example.javier.MaterialDesignApp.Utils.DAL;
import com.example.javier.MaterialDesignApp.Utils.JsonParser;
import com.example.javier.MaterialDesignApp.Utils.ScrollManagerToolbarTabs;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class TabDesignMaterialDesign extends Fragment {

    String urlPost;
    JSONObject jsonObjectDesignPosts;
    JSONArray jsonArrayDesignContent;
    ArrayList<Design> designs;
    SwipeRefreshLayout swipeRefreshLayout;
    String[] designTitle, designExcerpt, designImage, designImageFull, designContent;
    int postNumber = 99;
    Boolean error = false;
    RecyclerView recyclerView;
    RecyclerView.Adapter recyclerViewAdapter;
    View view;
    SharedPreferences sharedPreferences;
    Toolbar toolbar;
    TypedValue typedValueToolbarHeight = new TypedValue();
    SlidingTabLayout tabs;
    int recyclerViewPaddingTop;
    ///////////////////////////MY DEFINE/////////////////////////
    ArrayList<AFilmModel> films = new ArrayList<>();
    private String json;
    Listener delegate;
    private boolean loading = true;
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    LinearLayoutManager mLayoutManager;
    private int page = 1;
    String urlPostMore;
    PageModel Page;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.tab_design, container, false);

        // Get shared preferences
        sharedPreferences = getActivity().getSharedPreferences("VALUES", Context.MODE_PRIVATE);

        // Setup RecyclerView News
        recyclerViewDesign(view);

        // Setup swipe to refresh
        swipeToRefresh(view);

        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        return view;
    }

    private void recyclerViewDesign(View view) {

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewDesign);
        tabs = (SlidingTabLayout) view.findViewById(R.id.tabs);

        // Divider
        recyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(android.R.drawable.divider_horizontal_bright)));

        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        urlPost = "http://nhatphim.com/index/api?type=no&page=0&limit=20";

        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        films = new ArrayList<>();
        new AsyncTaskNewsParseJson().execute(urlPost);

        ItemClickSupport itemClickSupport = ItemClickSupport.addTo(recyclerView);
        itemClickSupport.setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClick(RecyclerView parent, View view, int position, long id) {
                AFilmModel aFilmModel = films.get(position);
                sharedPreferences.edit().putString("TITLE", aFilmModel.getTitle_en()).apply();
                sharedPreferences.edit().putString("EXCERPT", aFilmModel.getTitle_vn()).apply();
                sharedPreferences.edit().putString("IMAGE", aFilmModel.getImages()).apply();
                Gson gson = new Gson();
                SharedPreferences appSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
                SharedPreferences.Editor prefsEditor = appSharedPrefs.edit();
                json = gson.toJson(films.get(position));
                prefsEditor.putString("AFilmModel", json);
                prefsEditor.commit();
                delegate.GetFinish();
            }
        });
        delegate = new Listener() {
            @Override
            public void GetFinish() {

                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                startActivity(intent);
            }
        };
        delegate = new Listener() {
            @Override
            public void GetFinish() {

                Intent intent = new Intent(getActivity(), PlayerActivity.class);
                startActivity(intent);
            }
        };
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {

                        loading = false;

                        String Link = "http://nhatphim.com/index/api?type=no&page=" + page + "&limit=20";
                        LoadMore(Link);
                        Toast toast = Toast.makeText(getActivity(), String.valueOf(films.size()), Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }

            }
        });

    }

    public void LoadMore(String link) {
        urlPostMore = link;
        recyclerViewAdapter.notifyItemInserted(films.size());
        new AsyncTaskNewsParseJsonLoadMore().execute(urlPostMore);

    }

    public class AsyncTaskNewsParseJsonLoadMore extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
        }

        // get JSON Object
        @Override
        protected String doInBackground(String... url) {

            urlPostMore = url[0];
            try {
                jsonObjectDesignPosts = JsonParser.readJsonFromUrl(urlPostMore);
                films.addAll(DAL.getFilms(jsonObjectDesignPosts));
                PageModel _page = DAL.getPage(jsonObjectDesignPosts);
                page = Integer.parseInt(_page.getPage());
                page++;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                error = true;
            }
            return null;
        }

        // Set facebook items to the textviews and imageviews
        @Override
        protected void onPostExecute(String result) {
            swipeRefreshLayout = (android.support.v4.widget.SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
            swipeRefreshLayout.setRefreshing(false);
            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
            recyclerViewAdapter.notifyDataSetChanged();
            loading = true;

        }

    }

    interface Listener {
        public void GetFinish();
    }

    private void swipeToRefresh(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        int start = recyclerViewPaddingTop - convertToPx(48), end = recyclerViewPaddingTop + convertToPx(16);
        swipeRefreshLayout.setProgressViewOffset(true, start, end);
        TypedValue typedValueColorPrimary = new TypedValue();
        TypedValue typedValueColorAccent = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.colorPrimary, typedValueColorPrimary, true);
        getActivity().getTheme().resolveAttribute(R.attr.colorAccent, typedValueColorAccent, true);
        final int colorPrimary = typedValueColorPrimary.data, colorAccent = typedValueColorAccent.data;
        swipeRefreshLayout.setColorSchemeColors(colorPrimary, colorAccent);
        loading = true;
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTaskNewsParseJson().execute(urlPost);
            }
        });
    }

    public int convertToPx(int dp) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (dp * scale + 0.5f);
    }

    public void toolbarHideShow() {
        toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.post(new Runnable() {
            @Override
            public void run() {
                ScrollManagerToolbarTabs manager = new ScrollManagerToolbarTabs(getActivity());
                manager.attach(recyclerView);
                manager.addView(toolbar, ScrollManagerToolbarTabs.Direction.UP);
                manager.setInitialOffset(toolbar.getHeight());
            }
        });
    }

    public class AsyncTaskNewsParseJson extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
        }

        // get JSON Object
        @Override
        protected String doInBackground(String... url) {

            urlPost = url[0];
            try {
                jsonObjectDesignPosts = JsonParser.readJsonFromUrl(urlPost);
                jsonArrayDesignContent = jsonObjectDesignPosts.getJSONArray("row");
                films.clear();
                Page = DAL.getPage(jsonObjectDesignPosts);
                films = DAL.getFilms(jsonObjectDesignPosts);
                sharedPreferences.edit().putString("DEVELOP", jsonArrayDesignContent.toString()).apply();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                designTitle = new String[0];
                error = true;
            }
            return null;
        }

        // Set facebook items to the textviews and imageviews
        @Override
        protected void onPostExecute(String result) {

            // Create the recyclerViewAdapter
            recyclerViewAdapter = new FILMSAdapter(getActivity(), films);
            recyclerView.setAdapter(recyclerViewAdapter);

            swipeRefreshLayout = (android.support.v4.widget.SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
            swipeRefreshLayout.setRefreshing(false);

            // Create the recyclerViewAdapter
            recyclerViewAdapter = new FILMSAdapter(getActivity(), films);
            recyclerView.setAdapter(recyclerViewAdapter);

            swipeRefreshLayout = (android.support.v4.widget.SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
            swipeRefreshLayout.setRefreshing(false);

            ProgressBar progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
            progressBar.setVisibility(View.GONE);
        }
    }
}


package com.eventsequor.huetest;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;



import com.eventsequor.huetest.data.ColorBoxData;
import com.eventsequor.huetest.model.BoxModel;
import com.eventsequor.huetest.model.OutPutResult;
import com.eventsequor.huetest.utils.ColoredBoxAdapter;
import com.eventsequor.huetest.utils.ColoredBoxTouchHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int[] ACTION_MENUS = {
            R.string.label_menu_check,
            R.string.label_menu_test_again,
            R.string.label_menu_about
    };

    private boolean isDraggable = true;
    private boolean isReallyExit = false;

    private List<BoxModel> mColoredBoxes;
    private ColoredBoxAdapter mColoredBoxAdapter;
    private ItemTouchHelper mItemTouchHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize adapter
        mColoredBoxes = new ArrayList<>();
        mColoredBoxAdapter = new ColoredBoxAdapter(mColoredBoxes, this::onColoredBoxStartDrag);
        mColoredBoxAdapter.setHasStableIds(true);

        // Initialize test RecyclerView using GridLayoutManager
        RecyclerView mRecyclerView = findViewById(R.id.test_recycler);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mColoredBoxAdapter);
        GridLayoutManager manager = new GridLayoutManager(this, 10);
        mRecyclerView.setLayoutManager(manager);

        // Initialize adapter with item touch listener
        ItemTouchHelper.Callback callback = new ColoredBoxTouchHelper(mColoredBoxAdapter);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        for (int i = 0; i < ACTION_MENUS.length; i++) {
            menu.add(0, i, i, ACTION_MENUS[i]).setShowAsAction(
                    i == 0 ? MenuItem.SHOW_AS_ACTION_IF_ROOM : MenuItem.SHOW_AS_ACTION_NEVER
            );
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Add action to menu | NOTE: better compare with item label
        if (item.getItemId() == 0) onCheckResult();
        else if (item.getItemId() == 1) onTestAgain();
        else if (item.getItemId() == 2) onAbout();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        populateColoredBoxData();
        super.onResume();
    }

    // Populate data
    private void populateColoredBoxData() {
        mColoredBoxes.clear();
        mColoredBoxes.addAll(ColorBoxData.getAll());
        mColoredBoxAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        // Handling user touch back on Android navigation button
        if (isReallyExit) super.onBackPressed();
        Toast.makeText(this, R.string.confirm_exit, Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> isReallyExit = false, 2000);
        isReallyExit = true;
    }

    // Handling colored box drag
    private void onColoredBoxStartDrag(RecyclerView.ViewHolder holder) {
        if (isDraggable) mItemTouchHelper.startDrag(holder);
    }

    // Showing result and disable boxes to move
    private void onCheckResult() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.warning)
                .setMessage(R.string.check_confirm)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.label_menu_check, (d, w) -> {
                    isDraggable = false;
                    mColoredBoxAdapter.showValue(true);
                    onCalculateResult();
                }).create();
        dialog.show();
    }

    /**
     * Calculate error node using Farnsworth Munsell formula
     * Formula I've learn from this <a href="https://www.xritephoto.com/documents/literature/gmb/en/gmb_fm100_instructions_en.pdf">document</a>
     * The Farnsworth-Munsell 100-Hue Test for the examination of Color Discrimination by Dean Farnsworth revisied 1957
     */
    private void onCalculateResult() {
        System.out.println("Pass from here");
        // NOTE: Better not do this in view layer
        int[] val = new int[mColoredBoxes.size()];
        System.out.println(mColoredBoxes.size());
        val[0] = 0;
        for (int i = 0; i < val.length - 2; i++) {
            int valLeft = 0, valRight = 0;
            int left = mColoredBoxes.get(i).getPosition();
            int mid = mColoredBoxes.get(i + 1).getPosition();
            int right = mColoredBoxes.get(i + 2).getPosition();
            // Calculating error score | For detail read in document (page 5) mention above
            if (left > mid) valLeft = left - mid;
            else if (left < mid) valLeft = mid - left;
            if (mid > right) valRight = mid - right;
            else if (mid < right) valRight = right - mid;
            val[i + 1] = (valLeft + valRight) - 2;
        }
        val[val.length - 1] = 0;
        int totalErrorScore = 0;
        for (int i : val) totalErrorScore += i;
        String testId = String.valueOf(System.currentTimeMillis());
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        try {
            OutPutResult outPutResult = new OutPutResult(totalErrorScore, mColoredBoxes);
            String jsonString = ow.writeValueAsString(outPutResult);
            writeToFile(jsonString, testId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        onShowResult(totalErrorScore, testId);
    }

    private void writeToFile(String jsonString, String testId) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);

        File targetFile = new File(dir, String.format("%s.json", testId));
        FileWriter writer = null;
        try {
            writer = new FileWriter(targetFile);
            writer.write(jsonString);
            writer.close();
        } catch (IOException e) {
            try {
                Context context = getApplicationContext();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(String.format("%s.json", testId), Context.MODE_APPEND));
                outputStreamWriter.write(jsonString);
                outputStreamWriter.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        System.out.println();
    }

    // Show the calculate result
    private void onShowResult(int totalErrorScore, String testId) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle(R.string.test_result)
                .setMessage(getString(R.string.result_details, testId, totalErrorScore))
                .setNegativeButton(R.string.close, (d, w) -> d.dismiss())
                .setPositiveButton(R.string.label_menu_test_again, (d, w) -> onTestAgain())
                .create();
        dialog.show();
    }

    // Retry test
    private void onTestAgain() {
        isDraggable = true;
        mColoredBoxAdapter.showValue(false);
        populateColoredBoxData();
    }

    // Show about app
    private void onAbout() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.label_menu_about)
                .setMessage(R.string.about_details)
                .setPositiveButton(R.string.ok, null)
                .create().show();
    }
}

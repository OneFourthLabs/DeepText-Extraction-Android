package org.ai4bharat.indian_doc_xtract;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import indian_docs_processor.DocProcessor;
import indian_docs_processor.docs.AadharFront;
import indian_docs_processor.docs.DocumentBase;
import indian_docs_processor.docs.PanCard;
import utils.ImageUtils;

public class Result extends AppCompatActivity {

    String rawPrediction, parsedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setUI();
    }

    public void setUI() {
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        Bitmap imageBitmap = ImageUtils.readBitmapFromAppDirectory(getIntent().getStringExtra("result_path"), getApplicationContext());

        ImageView imageView = findViewById(R.id.image);
        imageView.setImageBitmap(imageBitmap);

        rawPrediction = getIntent().getStringExtra("pred");
        String docName = getIntent().getStringExtra("docType");
        DocumentBase doc = DocProcessor.parseDocFromResult(docName, rawPrediction);

        parsedText = doc == null ? "ERROR: Unable to parse any specific document type" : doc.toString();
    }

    public void btnDisplayPredictions_Click(View view) {
        String prediction = "";
        switch (view.getId()) {
            case R.id.btnDisplayPredictions:
                prediction = parsedText; break;
            case R.id.btnDisplayRaw:
                prediction = rawPrediction; break;
            default:
                return;
        }
        if (prediction.isEmpty())
            prediction = "<<ERROR>>\n\n" +
                    "I am sorry, for I could not find any text that is comprehensible to my inanimate self's perception.";

        new AlertDialog.Builder(this)
                .setMessage(prediction)
                .show();
    }

}

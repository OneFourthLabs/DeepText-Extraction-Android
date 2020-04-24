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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

    }

    public void btnDisplayPredictions_Click(View view) {
        String prediction = getIntent().getStringExtra("pred");
        DocProcessor.DocType docType = DocProcessor.detectDocType(prediction);
        DocumentBase doc = DocProcessor.getDocFromString(docType, prediction);
        if (doc != null && !doc.toString().isEmpty()) {
            prediction = docType.toString() + "\n\n" + doc.toString() + "\n\n---RAW OUTPUT---\n\n" + prediction;
        }
        if (prediction.isEmpty())
            prediction = "<<ERROR>>\n\n" +
                    "I am sorry, for I could not find any text that is comprehensible to my inanimate self's perception.";

        new AlertDialog.Builder(this)
                .setMessage(prediction)
                .show();
    }

    public void btnDisplayAadhar_Click(View view) {
        String prediction = getIntent().getStringExtra("pred");
        AadharFront aadharFront = (AadharFront) DocProcessor.getDocFromString(DocProcessor.DocType.AADHAR_FRONT, prediction);
        prediction = aadharFront != null ? aadharFront.toString() : "";
        if (prediction.isEmpty())
            prediction = "Unable to extract Aadhar Details...";

        new AlertDialog.Builder(this)
                .setMessage(prediction)
                .show();
    }

    public void btnDisplayPAN_Click(View view) {
        String prediction = getIntent().getStringExtra("pred");
        PanCard panCard = (PanCard) DocProcessor.getDocFromString(DocProcessor.DocType.PAN_CARD, prediction);
        prediction = panCard != null ? panCard.toString() : "";
        if (prediction.isEmpty())
            prediction = "Unable to extract PAN Details...";

        new AlertDialog.Builder(this)
                .setMessage(prediction)
                .show();
    }

}

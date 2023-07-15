package org.pytorch.demo.objectdetection;

import static androidx.camera.core.CameraX.getContext;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ResultView extends View {

    private OnResultClickListener mListener;

    private final static int TEXT_X = 40;
    private final static int TEXT_Y = 35;
    private final static int TEXT_WIDTH = 260;
    private final static int TEXT_HEIGHT = 50;

    private Paint mPaintRectangle;
    private Paint mPaintText;
    private ArrayList<Result> mResults;
    private static Map<String, String> mConstellationNames = new HashMap<>();

    public void setmImgScaleX(float mImgScaleX) {
        this.mImgScaleX = mImgScaleX;
    }

    public void setmImgScaleY(float mImgScaleY) {
        this.mImgScaleY = mImgScaleY;
    }

    public void setmIvScaleX(float mIvScaleX) {
        this.mIvScaleX = mIvScaleX;
    }

    public void setmIvScaleY(float mIvScaleY) {
        this.mIvScaleY = mIvScaleY;
    }

    public void setmStartX(float mStartX) {
        this.mStartX = mStartX;
    }

    public void setmStartY(float mStartY) {
        this.mStartY = mStartY;
    }

    private float mImgScaleX, mImgScaleY, mIvScaleX, mIvScaleY, mStartX, mStartY, scaleX, scaleY;

    static {
        mConstellationNames.put("ORI", "orion.html");
        mConstellationNames.put("GEM", "gemini.html");
        mConstellationNames.put("CNC", "cancer.html");
        mConstellationNames.put("CMI", "canisminor.html");
        mConstellationNames.put("CMA", "canismajor.html");
        mConstellationNames.put("MON", "monoceros.html");
        mConstellationNames.put("LEP", "lepus.html");
        mConstellationNames.put("SEX", "sextans.html");
        mConstellationNames.put("PYX", "pyxis.html");
        mConstellationNames.put("TRI", "triangulum.html");
        mConstellationNames.put("ARI", "aries.html");
        mConstellationNames.put("LEO", "leo.html");
        mConstellationNames.put("LMI", "leo.html");
        mConstellationNames.put("LYN", "lynx.html");
        mConstellationNames.put("VIR", "virgo.html");
        mConstellationNames.put("VEL", "vela.html");
        mConstellationNames.put("CEN", "centaurus.html");
        mConstellationNames.put("CRT", "crater.html");
        mConstellationNames.put("ANT", "antlia.html");
        mConstellationNames.put("HYA", "hydra.html");
        mConstellationNames.put("PUP", "puppis.html");
        mConstellationNames.put("COL", "columba.html");
        mConstellationNames.put("CAR", "carina.html");
        mConstellationNames.put("CAS", "cassiopeia.html");
        mConstellationNames.put("PIC", "pictor.html");
        mConstellationNames.put("DOR", "dorado.html");
        mConstellationNames.put("AND", "andromeda.html");
        mConstellationNames.put("TAU", "taurus.html");
        mConstellationNames.put("AUR", "auriga.html");
        mConstellationNames.put("HOR", "horologium.html");
        mConstellationNames.put("CAE", "caelum.html");
        mConstellationNames.put("SCL", "sculptor.html");
        mConstellationNames.put("CET", "cetus.html");
        mConstellationNames.put("FOR", "fornax.html");
        mConstellationNames.put("PHE", "phoenix.html");
        mConstellationNames.put("CAM", "camelopardalis.html");
        mConstellationNames.put("ERI", "eridanus.html");
        mConstellationNames.put("PEG", "pegasus.html");
        mConstellationNames.put("PER", "perseus.html");
        mConstellationNames.put("PSC", "pisces.html");
        mConstellationNames.put("UMA", "ursamajor.html");
        mConstellationNames.put("UMI", "ursaminor.html");
        mConstellationNames.put("CEP", "cepheus.html");
        mConstellationNames.put("CHA", "chamaeleon.html");
        mConstellationNames.put("CIR", "circinus.html");
        mConstellationNames.put("COM", "comaberenices.html");
        mConstellationNames.put("CRA", "coronaaustr.html");
        mConstellationNames.put("CRB", "coronaborealis.html");
        mConstellationNames.put("CRU", "crux.html");
        mConstellationNames.put("CRV", "corvus.html");
        mConstellationNames.put("CVN", "canesvenatici.html");
        mConstellationNames.put("CYG", "cygnus.html");
        mConstellationNames.put("DEL", "delphinus.html");
        mConstellationNames.put("DRA", "draco.html");
        mConstellationNames.put("EQU", "equuleus.html");
        mConstellationNames.put("GRU", "grus.html");
        mConstellationNames.put("HER", "hercules.html");
        mConstellationNames.put("HYI", "hydrus.html");
        mConstellationNames.put("IND", "indus.html");
        mConstellationNames.put("LAC", "lacerta.html");
        mConstellationNames.put("LIB", "libra.html");
        mConstellationNames.put("LUP", "lupus.html");
        mConstellationNames.put("LYR", "lyra.html");
        mConstellationNames.put("MEN", "mensa.html");
        mConstellationNames.put("MIC", "microscop.html");
        mConstellationNames.put("MUS", "musca.html");
        mConstellationNames.put("NOR", "norma.html");
        mConstellationNames.put("OCT", "octans.html");
        mConstellationNames.put("OPH", "ophiuchus.html");
        mConstellationNames.put("PAV", "pavo.html");
        mConstellationNames.put("PSA", "pisaustrinus.html");
        mConstellationNames.put("RET", "reticulum.html");
        mConstellationNames.put("SCO", "scorpius.html");
        mConstellationNames.put("SCT", "scutum.html");
        mConstellationNames.put("SER", "serpens.html");
        mConstellationNames.put("SGE", "sagitta.html");
        mConstellationNames.put("SGR", "sagittarius.html");
        mConstellationNames.put("APS", "apus.html");
        mConstellationNames.put("AQL", "aquila.html");
        mConstellationNames.put("AQR", "aquarius.html");
        mConstellationNames.put("ARA", "ara.html");
        mConstellationNames.put("VOL", "volans.html");
        mConstellationNames.put("VUL", "vulpecula.html");
        mConstellationNames.put("BOO", "bootes.html");
        mConstellationNames.put("CAP", "capricorn.html");
        mConstellationNames.put("TUC", "tucana.html");
        mConstellationNames.put("TRA", "triangulaustr.html");
        mConstellationNames.put("TEL", "telescop.html");
    }


    public interface OnResultClickListener {
        void onResultClick(Result result);
    }

    public ResultView(Context context) {
        super(context);
        init();
    }

    public ResultView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaintRectangle = new Paint();
        mPaintRectangle.setColor(Color.YELLOW);
        mPaintText = new Paint();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            for (Result result : mResults) {
                if (result.rect.contains((int) event.getX(), (int) event.getY())) {
                    String url;
                    String constellationName = mConstellationNames.get(PrePostProcessor.mClasses[result.classIndex]);
                    url = "http://maps.seds.org/Stars_en/Fig/" + constellationName;

                    WebViewActivity.start(getContext(), url);
                    performClick();
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        System.out.println("LAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        if (mResults == null) return;
        for (Result result : mResults) {
            mPaintRectangle.setStrokeWidth(5);
            mPaintRectangle.setStyle(Paint.Style.STROKE);
            canvas.drawRect(result.rect, mPaintRectangle);

            Path mPath = new Path();
            RectF mRectF = new RectF(result.rect.left, result.rect.top, result.rect.left + TEXT_WIDTH, result.rect.top + TEXT_HEIGHT);
            mPath.addRect(mRectF, Path.Direction.CW);
            mPaintText.setColor(Color.MAGENTA);
            canvas.drawPath(mPath, mPaintText);
            System.out.println("Drawing bounding box with the following coordinates:");
            System.out.println("Left: " + result.rect.left);
            System.out.println("Top: " + result.rect.top);
            System.out.println("Right: " + result.rect.left + TEXT_WIDTH);
            System.out.println("Bottom: " + result.rect.top + TEXT_HEIGHT);
            System.out.println("Constellation: " + PrePostProcessor.mClasses[result.classIndex]);

            mPaintText.setColor(Color.WHITE);
            mPaintText.setStrokeWidth(0);
            mPaintText.setStyle(Paint.Style.FILL);
            mPaintText.setTextSize(32);
            canvas.drawText(String.format("%s %.2f", PrePostProcessor.mClasses[result.classIndex], result.score), result.rect.left + TEXT_X, result.rect.top + TEXT_Y, mPaintText);
        }
    }

    public void setResults(ArrayList<Result> results) {
        mResults = results;
    }
}
package ru.jaromirchernyavsky.youniverse.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Editable;
import android.text.Layout;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import com.google.android.material.motion.MotionUtils;

import org.apache.commons.text.StringEscapeUtils;

import ru.jaromirchernyavsky.youniverse.R;
import ru.jaromirchernyavsky.youniverse.Utilities;

public class AIWriter implements TextWatcher, View.OnFocusChangeListener {
    private long delay = 1000; // 1 seconds after user stops typing
    private long last_text_edit = 0;
    private Handler handler = new Handler();
    private ImageButton img;
    private RelativeLayout parent;
    private EditText editText;
    private AnimatorSet animatorSet;
    private CharSequence toAdd;
    private boolean asReverse;
    private ObjectAnimator etScale;
    private Editable s;
    private Runnable input_finish_checker = new Runnable() {
        public void run() {
            if (System.currentTimeMillis() > (last_text_edit + delay - 500) && parent.getVisibility()==View.GONE) {
                editText.setVisibility(View.GONE);
                img.setImageResource(R.drawable.baseline_auto_awesome_24);
                parent.setVisibility(View.VISIBLE);
                if(asReverse){
                    asReverse=false;
                    animatorSet.reverse();
                }
                animatorSet.start();
            }
        }
    };
    public AIWriter(ImageButton img, CharSequence toAdd){
        this.toAdd = toAdd;
        this.img = img;
        parent = ((RelativeLayout)img.getParent());
        editText = (EditText) parent.getChildAt(0);
        animatorSet = new AnimatorSet();
        asReverse = false;
        etScale = ObjectAnimator.ofFloat(editText, "scaleX",0f,1f);
        etScale.setDuration(200);
        ObjectAnimator moveAnimX = ObjectAnimator.ofFloat(parent,"scaleX",0f, 1f);
        ObjectAnimator moveAnimY = ObjectAnimator.ofFloat(parent,"scaleY",0f, 1f);
        animatorSet.playTogether(moveAnimX,moveAnimY);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(200);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                if(asReverse==true){
                   asReverse=false;
                   parent.setVisibility(View.GONE);
                }else{
                    View.OnClickListener magicOCL = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {editText.setVisibility(View.VISIBLE);
                            etScale.start();
                            editText.requestFocus();
                            img.setImageResource(R.drawable.send);
                            img.setOnClickListener(v1 -> {
                                if(editText.length()>0&&editText.length()<250){
                                    Utilities.generateChatGPT("{\"role\":\"system\",\"content\":\""+sys_prompt()+"\"},{\"role\":\"user\",\"content\":\""+
                                            StringEscapeUtils.escapeJava(editText.getText().toString())+"\"}",(String fin)->{
                                        ((Activity)parent.getContext()).runOnUiThread(() -> {s.append(fin);});
                                        return null;
                                    },()->{img.setOnClickListener(this::onClick);img.setImageResource(R.drawable.baseline_auto_awesome_24);});
                                    editText.getText().clear();
                                    etScale.reverse();
                                    etScale.addListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                           animatorSet.reverse();
                                           asReverse=true;
                                           etScale.removeAllListeners();
                                        }
                                    });
                                }
                            });

                        }
                    };
                    img.setOnClickListener(magicOCL);
                }

            }
        });
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() > 0) {
            last_text_edit = System.currentTimeMillis();
            handler.postDelayed(input_finish_checker, delay);
            this.s = s;
        } else {

        }
    }



    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(editText.isFocused()) return;
        if(editText.getVisibility()==View.VISIBLE){
            editText.getText().clear();
            etScale.reverse();
            etScale.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animatorSet.reverse();
                    asReverse=true;
                    etScale.removeAllListeners();
                }
            });
        }
        animatorSet.reverse();
        asReverse=true;
    }

    private String sys_prompt(){
        return StringEscapeUtils.escapeJava("Ты помощник в написании "+toAdd+" для персонажа. Ты должен написать короткое продолжения того, что уже написал пользователь с учетом тех команд" +
                ", которых он дал. Вот что пользователь написал до: \n" +
                s.toString()+
                "\n"+
                "Ты НЕ должен писать никаких приветствий или ответов на подобии \"Да, конечно\". Ты ДОЛЖЕН СРАЗУ писать продолжение. Оно ДОЛЖНО БЫТЬ коротким и не больше одного абзаца или параграфа!"
        );
    }
}

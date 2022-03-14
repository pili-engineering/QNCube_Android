package com.hapi.ut.anim;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.util.Log;
import android.view.View;


/**
 * Created by xx on 2018/1/24.
 * 属性动画工具类，支持多个动画同时播放，支持每个动画设置时间或者整体设置时间
 * 每个动画可以通过拿到自己的ObjectAnimator自定义操作
 * 动画组可以通过Builder控制AnimatorSet和animatorSetBuilder的行为
 * <p>
 * 调用demo参考MainActivity
 */
public class AnimatorBuilder {
    public static final String ANIMATOR_TYPE_TRANSLATION_X = "translationX";
    public static final String ANIMATOR_TYPE_TRANSLATION_Y = "translationY";
    public static final String ANIMATOR_TYPE_SCALE_X = "scaleX";
    public static final String ANIMATOR_TYPE_SCALE_Y = "scaleY";
    public static final String ANIMATOR_TYPE_ROTATION = "rotation";
    public static final String ANIMATOR_TYPE_ROTATION_X = "rotationX";
    public static final String ANIMATOR_TYPE_ROTATION_Y = "rotationY";
    public static final String ANIMATOR_TYPE_ALPHA = "alpha";

    public static final String TAG = "AnimatorBuilder";

    public static final long ANIMATOR_DURATION_DEFAULT = 1000;
    public static final long ANIMATOR_DURATION_INVALID = -1;

    public static AnimatorSetWrapper playFloat(View view, String type, float... values) {
        return new AnimatorSetWrapper().playFloat(view, type, values);
    }

    public static AnimatorSetWrapper play(AnimatorWrapper animatorWrapper) {
        return new AnimatorSetWrapper().play(animatorWrapper);
    }

    public static AnimatorWrapper ofFloat(View view, String type, float... values) {
        return new AnimatorWrapper(view, type, values);
    }

    public static class AnimatorSetWrapper {

        /**
         * 使用then()方法执行的动画，在动画组(必须在统一的时间下)播放完成后顺序播放
         * 已废弃，使用替代的Builder方案
         */
//        private List<Animator> animatorsThen = new ArrayList<>();

        /**
         * 执行的动画组
         */
        private AnimatorSet animatorSet;
        private AnimatorSet.Builder animatorSetBuilder;

        private AnimatorSetWrapper() {
            animatorSet = new AnimatorSet();
        }

        /**
         * 构造动画Builder对象，仅在初始化构造时调用一次
         * @param view 需要执行动画的view
         * @param type 执行的动画类型
         * @param values 属性动画值
         * @return
         */
        public AnimatorSetWrapper playFloat(View view, String type, float... values) {
            animatorSetBuilder = animatorSet.play(ofFloat(view, type, values).getObjectAnimator());
            return this;
        }

        /**
         * 构造动画Builder对象，仅在初始化构造时调用一次，
         * 当外部需要自定义动画信息时，通过获取AnimatorWrapper对Animator进行自定义
         * @param animatorWrapper
         * @return
         */
        public AnimatorSetWrapper play(AnimatorWrapper animatorWrapper) {
            animatorSetBuilder = animatorSet.play(animatorWrapper.getObjectAnimator());
            return this;
        }

        /**
         * 需要和play一起执行的动画
         * @param view 需要执行动画的view
         * @param type 执行的动画类型
         * @param values 属性动画值
         * @return
         */
        public AnimatorSetWrapper withFloat(View view, String type, float... values) {
            animatorSetBuilder.with(ofFloat(view, type, values).getObjectAnimator());
            return this;
        }

        /**
         * 需要和play一起执行的动画
         * @param animatorWrapper 当外部需要自定义动画信息时，通过获取AnimatorWrapper对Animator进行自定义
         * @return
         */
        public AnimatorSetWrapper with(AnimatorWrapper animatorWrapper) {
            animatorSetBuilder.with(animatorWrapper.getObjectAnimator());
            return this;
        }

        /**
         * 接下来执行的动画
         * @param view 需要执行动画的view
         * @param type 执行的动画类型
         * @param values 属性动画值
         * @return
         */
        public AnimatorSetWrapper thenFloat(View view, String type, float... values) {
            return then(AnimatorBuilder.playFloat(view, type, values));
        }

        /**
         * 接下来执行的动画
         * @param animatorWrapper
         * @return
         */
        public AnimatorSetWrapper then(AnimatorWrapper animatorWrapper) {
            return then(AnimatorBuilder.play(animatorWrapper));
        }

        /**
         * 接下来执行的动画组
         * @param animatorSetWrapper
         * @return
         */
        public AnimatorSetWrapper then(AnimatorSetWrapper animatorSetWrapper) {
            animatorSetBuilder.before(animatorSetWrapper.animatorSet);
            animatorSetBuilder = animatorSetWrapper.animatorSetBuilder;
            return this;
        }

        /**
         * 兼容所有属性动画
         *
         * @return
         */
        private AnimatorSetWrapper build() {
            if (animatorSet.getChildAnimations().size() == 0) {
                Log.e(TAG, "objectAnimator size 0");
                return this;
            }
            clearOldAnimation();
            return this;
        }

        private void clearOldAnimation() {
            for (Animator animator : animatorSet.getChildAnimations()) {
                if (animator instanceof ObjectAnimator) {
                    Object target = ((ObjectAnimator) animator).getTarget();
                    if (target instanceof View) {
                        View view = (View) target;
                        if (null != view.getAnimation()) {
                            view.getAnimation().cancel();
                        }
                        view.clearAnimation();
                    }
                } else {
                    Log.e(TAG, "Animator Type is not ObjectAnimator");
                }
            }
        }

        public AnimatorSet start() {
            beforeStart();
            animatorSet.start();
            return animatorSet;
        }


        private void beforeStart() {
            build();
//            handleAnimatorsThenList();
        }

//        private void handleAnimatorsThenList() {
//            if (animatorsThen.size() > 0) {
//                AnimatorSet set = new AnimatorSet();
//                set.playSequentially(animatorsThen);
//                animatorSetBuilder.before(set);
//            }
//        }

        public AnimatorSetWrapper setDuration(long duration) {
            animatorSet.setDuration(duration);
            return this;
        }

        public AnimatorSetWrapper addAnimatorListener(Animator.AnimatorListener animatorListener) {
            if (null != animatorListener) {
                animatorSet.addListener(animatorListener);
            }
            return this;
        }

        public AnimatorSetWrapper setInterpolator(TimeInterpolator timeInterpolator) {
            if (null != timeInterpolator) {
                animatorSet.setInterpolator(timeInterpolator);
            }
            return this;
        }

        public AnimatorSet getAnimatorSet() {
            return animatorSet;
        }

    }

    public static class AnimatorWrapper {
        private ObjectAnimator objectAnimator;

        private AnimatorWrapper(View view, String type, float... values) {
            objectAnimator = ObjectAnimator.ofFloat(view, type, values);
        }

        public ObjectAnimator getObjectAnimator() {
            return objectAnimator;
        }

        public AnimatorWrapper setDuration(long duration){
             objectAnimator.setDuration(duration);
             return this;
        }

        public AnimatorWrapper setInterpolator(TimeInterpolator value){
            objectAnimator.setInterpolator(value);
            return this;
        }

        public AnimatorWrapper setFloatValues(float... values){
            objectAnimator.setFloatValues(values);
            return this;
        }

        public AnimatorWrapper setIntValues(int... values){
            objectAnimator.setIntValues(values);
            return this;
        }

        public AnimatorWrapper setObjectValues(Object... values){
            objectAnimator.setObjectValues(values);
            return this;
        }

        public AnimatorWrapper setCurrentPlayTime(long playTime){
            objectAnimator.setCurrentPlayTime(playTime);
            return this;
        }

        public AnimatorWrapper setEvaluator(TypeEvaluator value){
            objectAnimator.setEvaluator(value);
            return this;
        }

        public AnimatorWrapper setRepeatCount(int value){
            objectAnimator.setRepeatCount(value);
            return this;
        }

        public AnimatorWrapper setRepeatMode(int value){
            objectAnimator.setRepeatMode(value);
            return this;
        }

        public AnimatorWrapper setStartDelay(long startDelay){
            objectAnimator.setStartDelay(startDelay);
            return this;
        }

    }


}

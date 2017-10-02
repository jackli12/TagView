

package com.example.xugaopan.affinitydialog.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.xugaopan.affinitydialog.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.xugaopan.affinitydialog.widget.Utils.dp2px;


public class TagContainerLayout extends ViewGroup {

    /**
     * Default interval(dp)
     */
    private static final float DEFAULT_INTERVAL = 5;

    TagListener tagListener;

    /**
     * 垂直距离
     */
    private int mVerticalInterval;
    /**
     * The list to store the tags color info
     */
    private List<int[]> mColorArrayList;
    /**
     * 水平距离
     */
    private int mHorizontalInterval;
    /**
     * TagContainerLayout border width(default 0.5dp)
     */
    private float mBorderWidth = 0.5f;
    /**
     * TagContainerLayout border radius(default 10.0dp)
     */
    private float mBorderRadius = 10.0f;
    /**
     * TagView average height
     */
    private int mChildHeight;

    /**
     * TagContainerLayout border color(default #22FF0000)
     */
    private int mBorderColor = Color.parseColor("#22FF0000");
    /**
     * TagContainerLayout background color(default #11FF0000)
     */
    private int mBackgroundColor = Color.parseColor("#11FF0000");
    /**
     * The container layout gravity(default left)
     */
    private int mGravity = Gravity.LEFT;
    /**
     * The max line count of TagContainerLayout
     */
    private int mMaxLines = 0;
    /**
     * Tags
     */
    private List<String> mTags;
    /**
     * Whether to support 'letters show with RTL(eg: Android to diordnA)' style(default false)
     */
    private Paint mPaint;
    private RectF mRectF;
    private List<View> mChildViews;
    private int[] mViewPos;
    /**
     * The ripple effect color alpha(the value may between 0 - 255, default 128)
     */
    /**
     * Enable draw cross icon(default false)
     */
    private boolean mEnableCross = false;
    private Map<Integer, Boolean> selectMap = new HashMap<>();

    public TagContainerLayout(Context context) {
        this(context, null);
    }

    public TagContainerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagContainerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public void setOnTagListener(TagListener tagListener) {
        this.tagListener = tagListener;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.AndroidTagView,
                defStyleAttr, 0);
        mVerticalInterval = (int) attributes.getDimension(R.styleable.AndroidTagView_vertical_interval,
                dp2px(context, DEFAULT_INTERVAL));
        mHorizontalInterval = (int) attributes.getDimension(R.styleable.AndroidTagView_horizontal_interval,
                dp2px(context, DEFAULT_INTERVAL));
        mBorderWidth = attributes.getDimension(R.styleable.AndroidTagView_container_border_width,
                dp2px(context, mBorderWidth));
        mBorderRadius = attributes.getDimension(R.styleable.AndroidTagView_container_border_radius,
                dp2px(context, mBorderRadius));
        mBorderColor = attributes.getColor(R.styleable.AndroidTagView_container_border_color,
                mBorderColor);
        mBackgroundColor = attributes.getColor(R.styleable.AndroidTagView_container_background_color,
                mBackgroundColor);
        mGravity = attributes.getInt(R.styleable.AndroidTagView_container_gravity, mGravity);
        mMaxLines = attributes.getInt(R.styleable.AndroidTagView_container_max_lines, mMaxLines);
        attributes.recycle();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRectF = new RectF();
        mChildViews = new ArrayList<>();
        setWillNotDraw(false);

        if (isInEditMode()) {
            addTag("sample tag");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);
        final int childCount = getChildCount();
        int lines = childCount == 0 ? 0 : getChildLines(childCount);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        if (childCount == 0) {
            setMeasuredDimension(0, 0);
        } else if (heightSpecMode == MeasureSpec.AT_MOST
                || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            Log.d("setMeasuredDimension", "mVerticalInterval=" + mVerticalInterval);
            setMeasuredDimension(widthSpecSize, (mVerticalInterval + mChildHeight) * lines
                    + mVerticalInterval + getPaddingTop() + getPaddingBottom());
        } else {
            Log.d("setMeasuredDimension", "mVerticalInterval:" + mVerticalInterval);
            setMeasuredDimension(widthSpecSize, heightSpecSize);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRectF.set(0, 0, w, h);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount;
        if ((childCount = getChildCount()) <= 0) {
            return;
        }
        int availableW = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int curRight = getMeasuredWidth() - getPaddingRight();
        int curTop = getPaddingTop();
        int curLeft = getPaddingLeft();
        int sPos = 0;
        mViewPos = new int[childCount * 2];

        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != GONE) {
                int width = childView.getMeasuredWidth();
                if (mGravity == Gravity.RIGHT) {
                    if (curRight - width < getPaddingLeft()) {//如果右边的距离小于左边的距离
                        curRight = getMeasuredWidth() - getPaddingRight();
                        curTop += mChildHeight + mVerticalInterval;
                    }
                    mViewPos[i * 2] = curRight - width;
                    mViewPos[i * 2 + 1] = curTop;
                    curRight -= width + mHorizontalInterval;
                } else if (mGravity == Gravity.CENTER) {
                    if (curLeft + width - getPaddingLeft() > availableW) {
                        int leftW = getMeasuredWidth() - mViewPos[(i - 1) * 2]
                                - getChildAt(i - 1).getMeasuredWidth() - getPaddingRight();
                        for (int j = sPos; j < i; j++) {
                            mViewPos[j * 2] = mViewPos[j * 2] + leftW / 2;
                        }
                        sPos = i;
                        curLeft = getPaddingLeft();
                        curTop += mChildHeight + mVerticalInterval;
                    }
                    mViewPos[i * 2] = curLeft;
                    mViewPos[i * 2 + 1] = curTop;
                    curLeft += width + mHorizontalInterval;

                    if (i == childCount - 1) {
                        int leftW = getMeasuredWidth() - mViewPos[i * 2]
                                - childView.getMeasuredWidth() - getPaddingRight();
                        for (int j = sPos; j < childCount; j++) {
                            mViewPos[j * 2] = mViewPos[j * 2] + leftW / 2;
                        }
                    }
                } else {
                    if (curLeft + width - getPaddingLeft() > availableW) {
                        curLeft = getPaddingLeft();
                        curTop += mChildHeight + mVerticalInterval;
                    }
                    mViewPos[i * 2] = curLeft;
                    mViewPos[i * 2 + 1] = curTop;
                    curLeft += width + mHorizontalInterval;
                }
            }
        }

        // layout all child views
        for (int i = 0; i < mViewPos.length / 2; i++) {
            View childView = getChildAt(i);
            childView.layout(mViewPos[i * 2], mViewPos[i * 2 + 1] - (int) Utils.dp2px(getContext(), 10f),
                    mViewPos[i * 2] + childView.getMeasuredWidth(),
                    mViewPos[i * 2 + 1] + mChildHeight + (int) Utils.dp2px(getContext(), 20f));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

//        mPaint.setStyle(Paint.Style.FILL);
//        mPaint.setColor(mBackgroundColor);
//        canvas.drawRoundRect(mRectF, mBorderRadius, mBorderRadius, mPaint);

//        mPaint.setStyle(Paint.Style.STROKE);
//        mPaint.setStrokeWidth(mBorderWidth);
//        mPaint.setColor(mBorderColor);
//        canvas.drawRoundRect(mRectF, mBorderRadius, mBorderRadius, mPaint);
    }


    private int getChildLines(int childCount) {
        int availableW = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int lines = 1;
        for (int i = 0, curLineW = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            int dis = childView.getMeasuredWidth() + mHorizontalInterval;
            int height = childView.getMeasuredHeight();
            mChildHeight = i == 0 ? height : Math.max(mChildHeight, height);
            curLineW += dis;
            if (curLineW - mHorizontalInterval > availableW) {
                lines++;
                curLineW = dis;
            }
        }

        return mMaxLines <= 0 ? lines : mMaxLines;
    }

    private void onSetTag() {
        if (mTags == null) {
            throw new RuntimeException("NullPointer exception!");
        }
        removeAllTags();
        if (mTags.size() == 0) {
            return;
        }
        for (int i = 0; i < mTags.size(); i++) {
            onAddTag(mTags.get(i), mChildViews.size());
        }
        postInvalidate();
    }

    private void onAddTag(String text, int position) {
        if (position < 0 || position > mChildViews.size()) {
            return;
        }

        final View tagView = View.inflate(getContext(), R.layout.tag, null);
        TextView      title = (TextView) tagView.findViewById(R.id.tag_name);
        title.setText(text);
        title.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tagListener != null) {
                    int pos = mChildViews.indexOf(tagView);
                    tagListener.onTagClick(pos);
                }
            }
        });

        tagView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d("tagView","onLongClick");
                return false;
            }
        });

        ImageView imgDelete= (ImageView) tagView.findViewById(R.id.delete);
        if(mEnableCross){
            imgDelete.setVisibility(VISIBLE);
        }
        imgDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tagListener != null) {
                    int pos = mChildViews.indexOf(tagView);
                    tagListener.removeTag(pos);
                }
            }
        });

        mChildViews.add(position, tagView);
        if (position < mChildViews.size()) {
            for (int i = position; i < mChildViews.size(); i++) {
                mChildViews.get(i).setTag(i);
                selectMap.put(i, false);//设置全部反选
            }
        } else {
            tagView.setTag(position);
        }
        addView(tagView, position);
    }


    /**
     * 单选
     *
     * @param position
     * @return
     */
    public void setSelect(int position) {
        if (mChildViews != null) {
            LinearLayout child = (LinearLayout) mChildViews.get(position);
            if (child != null) {
                TextView tag = (TextView) child.getChildAt(0);
                if (tag != null) {
                    selectMap.put(position, true);
                    tag.setBackgroundResource(R.drawable.text_select_bg);
                }
            }
        }
    }

    /**
     * 反选
     *
     * @param position
     */
    public void unSelect(int position) {
        if (mChildViews != null) {
            LinearLayout child = (LinearLayout) mChildViews.get(position);
            if (child != null) {
                TextView tag = (TextView) child.getChildAt(0);
                if (tag != null) {
                    selectMap.put(position, false);
                    tag.setBackgroundResource(R.drawable.text_bg);
                }
            }
        }
    }

    public boolean isSelcet(int position) {
        return selectMap.get(position);
    }

    /**
     * 全选
     */
    public void selectAll(boolean isAll) {
        for (int i = 0; i < mChildViews.size(); i++) {
            LinearLayout child = (LinearLayout) mChildViews.get(i);
            if (child != null) {
                TextView tag = (TextView) child.getChildAt(0);
                if (tag != null) {
                    if (isAll) {
                        tag.setBackgroundResource(R.drawable.text_select_bg);
                    } else {
                        tag.setBackgroundResource(R.drawable.text_bg);
                    }
                    selectMap.put(i, isAll);
                }
            }
        }
    }

    private void onRemoveTag(int position) {
        if (position < 0 || position >= mChildViews.size()) {
            return;
        }
        mChildViews.remove(position);
        removeViewAt(position);
        for (int i = position; i < mChildViews.size(); i++) {
            mChildViews.get(i).setTag(i);
        }
        // TODO, make removed view null?
    }

    public void showCross() {
            for (View root : mChildViews) {
                LinearLayout linear = (LinearLayout) root;
                ImageView cross = (ImageView) linear.getChildAt(1);
                if (cross != null) {
                    if (mEnableCross) {
                    cross.setVisibility(VISIBLE);
                    }else {
                       cross.setVisibility(GONE);
                    }
            }
        }
    }


    /**
     * 添加多个tag
     *
     * @param tags
     */
    public void setTags(List<String> tags) {
        mTags = tags;
        onSetTag();
    }


    /**
     * 设置多个tag的颜色
     *
     * @param tags
     * @param colorArrayList
     */
    public void setTags(List<String> tags, List<int[]> colorArrayList) {
        mTags = tags;
        mColorArrayList = colorArrayList;
        onSetTag();
    }


    public void setTags(String... tags) {
        mTags = Arrays.asList(tags);
        onSetTag();
    }

    /**
     * 添加单个tag
     *
     * @param text
     */
    public void addTag(String text) {
        addTag(mChildViews.size(), text);
    }

    /**
     * 添加指定位置tag
     *
     * @param text
     * @param position
     */
    public void addTag(int position, String text) {
        onAddTag(text, position);
        postInvalidate();
    }

    /**
     * 删除指定位置的tag
     *
     * @param position
     */
    public void removeTag(int position) {
        onRemoveTag(position);
        postInvalidate();
    }

    /**
     * 删除所有tag
     */
    public void removeAllTags() {
        mChildViews.clear();
        removeAllViews();
        postInvalidate();
    }


    public int getVerticalInterval() {
        return mVerticalInterval;
    }

    /**
     * 设置tag垂直距离
     *
     * @param interval
     */
    public void setVerticalInterval(float interval) {
        mVerticalInterval = (int) dp2px(getContext(), interval);
        postInvalidate();
    }

    public int getHorizontalInterval() {
        return mHorizontalInterval;
    }

    /**
     * 设置tag水平距离
     *
     * @param interval
     */
    public void setHorizontalInterval(float interval) {
        mHorizontalInterval = (int) dp2px(getContext(), interval);
        postInvalidate();
    }

    public float getBorderWidth() {
        return mBorderWidth;
    }

    /**
     * 设置边框宽度
     *
     * @param width
     */
    public void setBorderWidth(float width) {
        this.mBorderWidth = width;
    }

    public float getBorderRadius() {
        return mBorderRadius;
    }

    /**
     * 设置边框弧度
     *
     * @param radius
     */
    public void setBorderRadius(float radius) {
        this.mBorderRadius = radius;
    }


    public int getBorderColor() {
        return mBorderColor;
    }

    /**
     * 设置边框颜色
     *
     * @param color
     */
    public void setBorderColor(int color) {
        this.mBorderColor = color;
    }

    /**
     * 设置背景色
     *
     * @return
     */
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    /**
     * Set TagContainerLayout background color.
     *
     * @param color
     */
    public void setBackgroundColor(int color) {
        this.mBackgroundColor = color;
    }

    public int getGravity() {
        return mGravity;
    }

    /**
     * 设置子view位置，默认从左往右
     *
     * @param gravity
     */
    public void setGravity(int gravity) {
        this.mGravity = gravity;
    }


    public int getMaxLines() {
        return mMaxLines;
    }

    /**
     * 设置最大行数
     *
     * @param maxLines max line count
     */
    public void setMaxLines(int maxLines) {
        mMaxLines = maxLines;
        postInvalidate();
    }


    public boolean isEnableCross() {
        return mEnableCross;
    }

    /**
     * 是否显示删除
     *
     * @param mEnableCross
     */
    public void setEnableCross(boolean mEnableCross) {
        this.mEnableCross = mEnableCross;
        showCross();
    }


    public interface TagListener {
        void onTagClick(int position);
        void removeTag(int position);
    }


}

package com.example.yuhao.mynestedscrolling.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChild2;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

import com.example.yuhao.mynestedscrolling.R;


public class PullToRefresh extends LinearLayout implements NestedScrollingChild2 ,NestedScrollingParent {
    private static final String TAG = "PullToRefresh";

    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private NestedScrollingParentHelper mNestedScrollingParentHelper;


    private int mLastY;
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private int mNestedOffsetY;

    // pull state
    private static final int PULL_UP_STATE = 0;
    private static final int PULL_DOWN_STATE = 1;
    // refresh states
    private static final int PULL_TO_REFRESH = 2;
    private static final int RELEASE_TO_REFRESH = 3;
    private static final int REFRESHING = 4;

    private Scroller mScroller;
    /**
     * last y
     */
    private int mLastMotionY;
    /**
     * lock
     */
    private boolean mLock;
    /**
     * header view
     */
    private View mHeaderView;
    /**
     * footer view
     */
    private View mFooterView;
    /**
     * list or grid
     */
    private AdapterView<?> mAdapterView;

    /**
     *  RecyclerView
     */

    private RecyclerView mRecyclerView;
    /**
     * header view height
     */
    private int mHeaderViewHeight;
    private int mHeaderImageHeight;
    /**
     * footer view height
     */
    private int mFooterViewHeight;
    /**
     * header view image
     */
    private ImageView mHeaderImageView;
    /**
     * footer view image
     */
    private ImageView mFooterImageView;
    /**
     * header tip text
     */
    private TextView mHeaderTextView;
    /**
     * footer tip text
     */
    private TextView mFooterTextView;
    /**
     * layout inflater
     */
    private LayoutInflater mInflater;
    /**
     * header view current state
     */
    private int mHeaderState;
    /**
     * footer view current state
     */
    private int mFooterState;
    /**
     * pull state,pull up or pull down;PULL_UP_STATE or PULL_DOWN_STATE
     */
    private int mPullState;
    /**
     * footer refresh listener
     */
    private OnFooterRefreshListener mOnFooterRefreshListener;
    /**
     * footer refresh listener
     */
    private OnHeaderRefreshListener mOnHeaderRefreshListener;

    /**
     * Button Refresh Listener
     */
    private OnClickButtonRefreshListener mOnClickButtonRefreshListener;

    private OnHeaderUpdateTextListener mOnHeaderUpdateTextListener;
    /**
     * 是否允许上拉加载
     */
    private boolean isFooterLoad;
    /**
     * 是否允许下拉刷新
     */
    private boolean isHeaderLoad;

    private boolean  isPermitRefreshNoChildView = false;

    private int mHeaderTextId;

    private int mFooterTextId;
    private int gridSpacing = 0;


    public PullToRefresh(Context context) {
        super(context);
        init();
    }

    public PullToRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefresh, 0, 0);
        gridSpacing = (int) a.getDimension(R.styleable.PullToRefresh_gridSpacing,0);
        a.recycle();
        init();
    }

    public void setmHeaderTextId(int mHeaderTextId){
        this.mHeaderTextId = mHeaderTextId;
    }

    public void setmFooterTextId(int mFooterTextId) {
        this.mFooterTextId = mFooterTextId;
    }

    public void setIsHeaderLoad(boolean isHeaderLoad) {
        this.isHeaderLoad = isHeaderLoad;
    }

    public void setIsFooterLoad(boolean isFooterLoad) {
        this.isFooterLoad = isFooterLoad;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
      //  addFooterView();
        initContentAdapterView();
    }

    private void init() {
        getScrollingChildHelper();
        getScrollingParentHelper();
        isHeaderLoad = true;
        mHeaderTextId = R.string.alert_refresh_header;
        mFooterTextId = R.string.alert_refresh_footer;
        mInflater = LayoutInflater.from(getContext());
        addHeaderView();
    }

    private void addHeaderView(){
        mHeaderView = mInflater.inflate(R.layout.refresh_alert_header, this, false);
        mHeaderImageView = (ImageView) mHeaderView
                .findViewById(R.id.refresh_alert_header_image);
        mHeaderTextView = (TextView) mHeaderView
                .findViewById(R.id.refresh_alert_header_text);
        measureView(mHeaderView);
        mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        mHeaderImageHeight = mHeaderImageView.getMeasuredHeight();
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
                mHeaderViewHeight);
        // 设置topMargin的值为负的header View高度,即将其隐藏在最上方
        params.topMargin = -(mHeaderViewHeight);
        addView(mHeaderView, params);
    }

    private void addFooterView(){
        mFooterView = mInflater.inflate(R.layout.refresh_alert_footer, this, false);
        mFooterImageView = (ImageView) mFooterView
                .findViewById(R.id.refresh_alert_footer_image);
        mFooterTextView = (TextView) mFooterView
                .findViewById(R.id.refresh_alert_footer_text);
        measureView(mFooterView);
        mFooterViewHeight = mFooterView.getMeasuredHeight();
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                mFooterViewHeight);
        addView(mFooterView, params);
    }

    private void measureView(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int lpHeight = p.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
                    MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0,
                    MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
    }

    private void initContentAdapterView() {
        int count = getChildCount();
        View view = null;
        for(int i = 0; i < count;i++){
            view = getChildAt(i);
            if(view instanceof AdapterView<?>){
                mAdapterView = (AdapterView<?>) view;
            }

            if(view instanceof RecyclerView){
                mRecyclerView = (RecyclerView)view;
            }
        }
    }

    // 滑动距离坐标(让ViewPager滑动)
    private float xDistance, yDistance, xLast, yLast;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        return super.onInterceptTouchEvent(e);
        /*
        int y = (int) e.getRawY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 首先拦截down事件,记录y坐标
                mLastMotionY = y;
                xDistance = e.getRawX();
                yDistance = e.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                // deltaY > 0 是向下运动,< 0是向上运动
                xLast = e.getRawX();
                yLast = e.getRawY();
                int deltaY = y - mLastMotionY;
                if(Math.abs(xLast-xDistance) > Math.abs(yLast-yDistance)){
                    return false;
                }
                if (isRefreshViewScroll(deltaY)) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return false;
        */
    }
/*
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLock) {
            return true;
        }
        int y = (int) event.getRawY();

        final int action = MotionEventCompat.getActionMasked(event);
        int yt = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            {
                mLastY = yt;
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
            }
                break;
            case MotionEvent.ACTION_MOVE:

            {
                int dy = mLastY - yt;
                int oldY = getScrollY();
                if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset)) {
                    dy -= mScrollConsumed[1];
                }
                mLastY = yt - mScrollOffset[1];
                if (dy < 0) {
                    int newScrollY = Math.max(0, oldY + dy);
                    dy -= newScrollY - oldY;
                    if (dispatchNestedScroll(0, newScrollY - dy, 0, dy, mScrollOffset)) {
                        mLastY -= mScrollOffset[1];
                    }
                }
            }

                int deltaY = y - mLastMotionY;
                if (mPullState == PULL_DOWN_STATE) {
                    // PullToRefreshView执行下拉
                    headerPrepareToRefresh(deltaY);
                } else if (mPullState == PULL_UP_STATE) {
                    footerPrepareToRefresh(deltaY);
                }
                mLastMotionY = y;

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                int topMargin = getHeaderTopMargin();
                if (mPullState == PULL_DOWN_STATE) {
                    if (topMargin >= 0) {
                        // 开始刷新
                        headerRefreshing();
                    } else {
                        // 还没有执行刷新，重新隐藏
                        setHeaderTopMargin(-mHeaderViewHeight);
                    }
                } else if (mPullState == PULL_UP_STATE) {
                    if (Math.abs(topMargin) >= mHeaderViewHeight
                            + mFooterViewHeight) {
                        // 开始执行footer 刷新
                        footerRefreshing();
                    } else {
                        // 还没有执行刷新，重新隐藏
                        setHeaderTopMargin(-mHeaderViewHeight);
                    }
                }

                stopNestedScroll();
                break;
        }
        return super.onTouchEvent(event);
    }
*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastY = y;
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            case MotionEvent.ACTION_MOVE:
                int dy = mLastY - y;
                int oldY = getScrollY();
                if (dispatchNestedPreScroll(0, dy, mScrollConsumed, mScrollOffset)) {
                    dy -= mScrollConsumed[1];
                }
                mLastY = y - mScrollOffset[1];
                if (dy < 0) {
                    int newScrollY = Math.max(0, oldY + dy);
                    dy -= newScrollY - oldY;
                    if (dispatchNestedScroll(0, newScrollY - dy, 0, dy, mScrollOffset)) {
                        mLastY -= mScrollOffset[1];
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopNestedScroll();
                break;
        }
        return true;
    }

    private boolean isRefreshViewScroll(int deltaY) {
        if (mHeaderState == REFRESHING || mFooterState == REFRESHING) {
            return false;
        }
        //对于ListView和GridView
        if (mAdapterView != null) {
            // 子view(ListView or GridView)滑动到最顶端
            if (deltaY > 0) {
                View child = mAdapterView.getChildAt(0);
                if (child == null) {
                    // 如果mAdapterView中没有数据,不拦截
                    if(isPermitRefreshNoChildView &&
                            mAdapterView.getFirstVisiblePosition() == 0
                            && isHeaderLoad){
                        mPullState = PULL_DOWN_STATE;
                        return true;
                    }
                    return false;
                }
                if (mAdapterView.getFirstVisiblePosition() == 0
                        && isHeaderLoad && child.getTop() == 0) {
                    mPullState = PULL_DOWN_STATE;
                    return true;
                }
                int top = child.getTop();
                int padding = mAdapterView.getPaddingTop();
                if (mAdapterView.getFirstVisiblePosition() == 0
                        && isHeaderLoad && Math.abs(top - padding) <= 8) {//这里之前用3可以判断,但现在不行,还没找到原因
                    mPullState = PULL_DOWN_STATE;
                    return true;
                }

            } else if (deltaY < 0) {
                View lastChild = mAdapterView.getChildAt(mAdapterView.getChildCount() - 1);
                if (lastChild == null) {
                    // 如果mAdapterView中没有数据,不拦截
                    return false;
                }
                //允许上拉加载
                // 最后一个子view的Bottom小于父View的高度说明mAdapterView的数据没有填满父view,
                // 等于父View的高度说明mAdapterView已经滑动到最后
                if (lastChild.getBottom() <= getHeight() && isFooterLoad
                        && mAdapterView.getLastVisiblePosition() == mAdapterView
                        .getCount() - 1) {
                    mPullState = PULL_UP_STATE;
                    return true;
                }
            }
        }
        //支持RecyclerView
        if (mRecyclerView != null) {
            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager ||
                    layoutManager instanceof GridLayoutManager) {

                // 子view(ListView or GridView)滑动到最顶端
                if (deltaY > 0) {
                    View child = mRecyclerView.getChildAt(0);
                    if (child == null) {
                        // 如果mAdapterView中没有数据,不拦截
                        if(isPermitRefreshNoChildView && isHeaderLoad){
                            mPullState = PULL_DOWN_STATE;
                            return true;
                        }else{
                            return false;
                        }

                    }

                    int firstVisiblePosition = 0;
                    if (layoutManager instanceof LinearLayoutManager) {
                        firstVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    } else {
                        firstVisiblePosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
                    }

                    if ((firstVisiblePosition == 0 || (firstVisiblePosition == 1))
                            && isHeaderLoad && child.getTop() == 0) {
                        mPullState = PULL_DOWN_STATE;
                        return true;
                    }
                    int top = child.getTop();
                    //解决给RecyclerView addGridSpacingItemDecoration 无法下拉问题 gridSpacing需要和GridSpacingItemDecoration 的 spacing参数保持一致
                    int padding = mRecyclerView.getPaddingTop() + gridSpacing;
                    if ((firstVisiblePosition == 0 || (firstVisiblePosition == 1))
                            && isHeaderLoad && Math.abs(top - padding) <= 8) {//这里之前用3可以判断,但现在不行,还没找到原因
                        mPullState = PULL_DOWN_STATE;
                        return true;
                    }
                } else if (deltaY < 0) {
                    View lastChild = mRecyclerView.getChildAt(mRecyclerView
                            .getChildCount() - 1);
                    if (lastChild == null) {
                        // 如果mAdapterView中没有数据,不拦截
                        if(isPermitRefreshNoChildView && isFooterLoad){
                            mPullState = PULL_UP_STATE;
                            return true;
                        }else{
                            return false;
                        }
                    }
                    //允许上拉加载
                    int lastVisiblePosition;
                    if (layoutManager instanceof LinearLayoutManager) {
                        lastVisiblePosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else {
                        lastVisiblePosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                    }

                    if (lastChild.getBottom() <= getHeight() && isFooterLoad
                            && lastVisiblePosition == mRecyclerView.getAdapter().getItemCount() - 1) {
                        mPullState = PULL_UP_STATE;
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void setPermitToRefreshNoChildView(boolean isPermit){
        isPermitRefreshNoChildView = isPermit;
    }

    private void headerPrepareToRefresh(int deltaY) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        if(params.topMargin == -mHeaderViewHeight){
            if(mOnHeaderUpdateTextListener != null){
                mOnHeaderUpdateTextListener.onUpdateHeaderText();
            }
        }
        int newTopMargin = changingHeaderViewTopMargin(deltaY);
        if (newTopMargin >= 0 && mHeaderState != RELEASE_TO_REFRESH) {
            mHeaderTextView.setText(R.string.alert_refresh_load);
            mHeaderState = RELEASE_TO_REFRESH;
        } else if (newTopMargin < 0 && newTopMargin > -mHeaderViewHeight) {// 拖动时没有释放
            mHeaderTextView.setText(mHeaderTextId);
            if(mHeaderImageHeight + newTopMargin >= 0) {
                float scale = (float) ((mHeaderImageHeight + newTopMargin) * 1.0 / mHeaderImageHeight);
                mHeaderImageView.setScaleX(scale);
                mHeaderImageView.setScaleY(scale);
            }
            mHeaderState = PULL_TO_REFRESH;
        }
    }

    private void footerPrepareToRefresh(int deltaY) {
        int newTopMargin = changingHeaderViewTopMargin(deltaY);
        if (Math.abs(newTopMargin) >= (mHeaderViewHeight + mFooterViewHeight)
                && mFooterState != RELEASE_TO_REFRESH) {
            mFooterTextView.setText(R.string.alert_refresh_load);

        } else if (Math.abs(newTopMargin) < (mHeaderViewHeight + mFooterViewHeight)) {
            mFooterTextView.setText(mFooterTextId);
            float scale = (float) ((mFooterViewHeight + newTopMargin) * 1.0 / mFooterViewHeight);
            mFooterImageView.setScaleX(scale);
            mFooterImageView.setScaleY(scale);
            mFooterState = PULL_TO_REFRESH;
        }
    }

    private int getHeaderTopMargin() {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        return params.topMargin;
    }

    private void headerRefreshing() {
        mHeaderState = REFRESHING;
        setHeaderTopMargin(0);
        mHeaderTextView.setText(R.string.alert_refresh_loading);
        if (mOnHeaderRefreshListener != null) {
            mOnHeaderRefreshListener.onHeaderRefresh(this);
        }
    }

    private void footerRefreshing() {
        mFooterState = REFRESHING;
        int top = mHeaderViewHeight + mFooterViewHeight;
        setHeaderTopMargin(-top);
        mFooterTextView.setText(R.string.alert_refresh_loading);
        if (mOnFooterRefreshListener != null) {
            mOnFooterRefreshListener.onFooterRefresh(this);
        }
    }

    private void setHeaderTopMargin(int topMargin) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        params.topMargin = topMargin;
        mHeaderView.setLayoutParams(params);
    }

    public void startHeaderRefresh(){
        headerRefreshing();
    }

    private int changingHeaderViewTopMargin(int deltaY) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        float newTopMargin = params.topMargin + deltaY * 0.3f;
        //这里对上拉做一下限制,因为当前上拉后然后不释放手指直接下拉,会把下拉刷新给触发了,感谢网友yufengzungzhe的指出
        //表示如果是在上拉后一段距离,然后直接下拉
        if(deltaY>0&&mPullState == PULL_UP_STATE&& Math.abs(params.topMargin) <= mHeaderViewHeight){
            return params.topMargin;
        }
        //同样地,对下拉做一下限制,避免出现跟上拉操作时一样的bug
        if(deltaY<0&&mPullState == PULL_DOWN_STATE&& Math.abs(params.topMargin)>=mHeaderViewHeight){
            return params.topMargin;
        }
        params.topMargin = (int) newTopMargin;
        mHeaderView.setLayoutParams(params);
        invalidate();
        return  params.topMargin;
    }

    public void onHeaderRefreshComplete() {

        int startX_t =0;
        int startY_t = 0;
        int endY = -mHeaderViewHeight;
        int endX = -mHeaderViewHeight;
        int dx = endX-endY; //增量x
        int dy = endY - startY_t; //增量y
        int duration = Math.abs(dy)*10; // 变化需要的时间

        if(duration > 400){
            duration = 400;
        }

        mScroller.startScroll(startX_t, startY_t, dx, dy, duration);

       // setHeaderTopMargin(-mHeaderViewHeight);
        mHeaderTextView.setText(mHeaderTextId);
        mHeaderState = PULL_TO_REFRESH;
    }

    public void onFooterRefreshComplete() {
        setHeaderTopMargin(-mHeaderViewHeight);
        mFooterTextView.setText(mFooterTextId);
        mFooterState = PULL_TO_REFRESH;
    }

    public void setOnHeaderRefreshListener(OnHeaderRefreshListener headerRefreshListener) {
        mOnHeaderRefreshListener = headerRefreshListener;
    }

    public void setOnFooterRefreshListener(OnFooterRefreshListener footerRefreshListener) {
        mOnFooterRefreshListener = footerRefreshListener;
    }

    public void setOnClickButtonRefreshListener(OnClickButtonRefreshListener clickButtonRefreshListener) {
        mOnClickButtonRefreshListener = clickButtonRefreshListener;
    }
    public void setonHeaderUpdateTextListener(OnHeaderUpdateTextListener headerUpdateText){
        mOnHeaderUpdateTextListener = headerUpdateText;
    }
    public interface OnFooterRefreshListener {
        public void onFooterRefresh(PullToRefresh view);
    }

    public interface OnHeaderRefreshListener {
        public void onHeaderRefresh(PullToRefresh view);
    }

    public interface OnClickButtonRefreshListener {
        public void OnClickButtonRefresh(PullToRefresh view);
    }

    public interface OnHeaderUpdateTextListener {
        public void onUpdateHeaderText();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();

        if(mScroller.computeScrollOffset()){ //如果正在计算的过程中
            
            setHeaderTopMargin(mScroller.getCurrY());
            //更新滚动的位置
            invalidate();

        }
    }

    private boolean isButtonRefresh = false;
    public void onClickButtonRefresh(){
        if(!isButtonRefresh && mHeaderState != REFRESHING) {

            int startX_t = -mHeaderViewHeight;
            int startY_t = -mHeaderViewHeight;
            int endY = 0;
            int endX = 0;
            int dx = endX - endY; //增量x
            int dy = endY - startY_t; //增量y
            int duration = Math.abs(dy) * 10; // 变化需要的时间

            if (duration > 400) {
                duration = 400;
            }
            mHeaderState = REFRESHING;
            mScroller.startScroll(startX_t, startY_t, dx, dy, duration);
            invalidate();
            if(mOnClickButtonRefreshListener != null){
                mOnClickButtonRefreshListener.OnClickButtonRefresh(this);
            }
        }
        isButtonRefresh = true;

    }

    //初始化helper对象
    private NestedScrollingChildHelper getScrollingChildHelper() {
        if (mNestedScrollingChildHelper == null) {
            mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
            mNestedScrollingChildHelper.setNestedScrollingEnabled(true);
        }
        return mNestedScrollingChildHelper;
    }

    //初始化helper对象
    private NestedScrollingParentHelper getScrollingParentHelper() {
        if (mNestedScrollingParentHelper == null) {
            mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);

        }
        return mNestedScrollingParentHelper;
    }


    //实现一下接口
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        getScrollingChildHelper().setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return getScrollingChildHelper().isNestedScrollingEnabled();
    }


    @Override
    public void stopNestedScroll() {
        getScrollingChildHelper().stopNestedScroll();
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return getScrollingChildHelper().dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return getScrollingChildHelper().dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return getScrollingChildHelper().startNestedScroll(axes);
    }

    @Override
    public boolean startNestedScroll(int axes, int type) {
        return getScrollingChildHelper().startNestedScroll(axes, type);
    }

    @Override
    public void stopNestedScroll(int type) {
        getScrollingChildHelper().stopNestedScroll(type);
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return getScrollingChildHelper().hasNestedScrollingParent();
    }

    @Override
    public boolean hasNestedScrollingParent(int type) {
        return getScrollingChildHelper().hasNestedScrollingParent(type);
    }


    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow);
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow, int type) {
        return getScrollingChildHelper().dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow, type);
    }


    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow,
                                           int type) {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type);
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        //通知协作
        return true;
    }
    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        getScrollingParentHelper().onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);//fu buju xie做
    }
    @Override
    public void onStopNestedScroll(View target) {
        getScrollingParentHelper().onStopNestedScroll(target);
        stopNestedScroll();
    }
    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        //把子的消耗传上去
        dispatchNestedScroll(0, dyConsumed, 0, dyUnconsumed, null);
    }
    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        dispatchNestedPreScroll(dx, dy, consumed, null);
    }
    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX,velocityY,consumed);
    }
    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityX);
//        return false;
    }
    @Override
    public int getNestedScrollAxes() {
        return 0;
    }
}

package work.wanghao.imagetag.widegt;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import work.wanghao.imagetag.R;
import work.wanghao.imagetag.widegt.callback.OnMaxNumTagCallback;

import static work.wanghao.imagetag.widegt.PxUtils.sp2px;

@SuppressWarnings("unused") public class TagLayout extends FrameLayout
    implements OnTouchListener, View.OnClickListener {
  private final static String TAG = TagLayout.class.getSimpleName();
  private static final int CLICK_RANGE = 5;
  private final static int DEFAULT_MAX_TAG = 20;

  private Context mContext;

  private int mStartX = 0;//按下点位置X坐标
  private int mStartY = 0;//按下点位置Y坐标
  private int mStartTouchViewLeft = 0;
  private int mStartTouchViewTop = 0;

  private TagView mCurrentTouchChildTagView;
  private ImageView mImageView;

  private int maxTagNum = DEFAULT_MAX_TAG;

  private OnMaxNumTagCallback mOnMaxNumTagCallback;

  public void setOnMaxNumTagCallback(OnMaxNumTagCallback onMaxNumTagCallback) {
    mOnMaxNumTagCallback = onMaxNumTagCallback;
  }

  public TagLayout(Context context) {
    this(context, null);
  }

  public TagLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
    init();
  }

  private void init() {
    this.setOnTouchListener(this);
    initView();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
      /*设置父容器为正方形*/
    ViewGroup.LayoutParams layoutParams = getLayoutParams();
    layoutParams.height = getScreenWidth();
    setLayoutParams(layoutParams);
  }

  private void initView() {
    View rootView =
        LayoutInflater.from(mContext).inflate(R.layout.view_container_tagging, this, true);
    mImageView = (ImageView) rootView.findViewById(R.id.image);
  }

  @NonNull public ImageView getImageView() {
    return mImageView;
  }

  @Override public void onClick(View v) {
    removeView(v);
  }

  @Override public boolean onTouch(View v, MotionEvent event) {
    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        mCurrentTouchChildTagView = null;
        mStartX = (int) event.getX();
        mStartY = (int) event.getY();
        if (hasView(mStartX, mStartY)) {
          mStartTouchViewLeft = mCurrentTouchChildTagView.getLeft();
          mStartTouchViewTop = mCurrentTouchChildTagView.getTop();
        } else {
          addItem(mStartX, mStartY);
        }
        break;
      case MotionEvent.ACTION_MOVE:
        moveView((int) event.getX(), (int) event.getY());
        break;
      case MotionEvent.ACTION_UP:
        int endX = (int) event.getX();
        int endY = (int) event.getY();
        //如果挪动的范围很小，则判定为单击
        if (mCurrentTouchChildTagView != null
            && Math.abs(endX - mStartX) < CLICK_RANGE
            && Math.abs(endY - mStartY) < CLICK_RANGE) {
          //当前点击的view
          mCurrentTouchChildTagView.showCloseButton();
          //mCurrentTouchChildTagView.getLocationInContainer();
        }
        mCurrentTouchChildTagView = null;
        break;
    }
    return true;
  }

  private void addItem(int x, int y) {
    if (getTagViewNum() >= maxTagNum) {
      if (mOnMaxNumTagCallback != null) mOnMaxNumTagCallback.accept();
      return;
    }


    /*对Y坐标进行有效范围默认校正*/
    if (y + TagView.DEFAULT_HEIGHT / 2 > getHeight()) {
      y = getHeight() - TagView.DEFAULT_HEIGHT / 2;
    } else if (y < TagView.DEFAULT_HEIGHT / 2) {
      y = TagView.DEFAULT_HEIGHT / 2;
    }
      /*对X坐标进行有效范围矫正*/
    if (x + TagView.DEFAULT_WIDTH / 2 >= getWidth()) {
      /*对于右边屏幕的宽度而言，这种程度的校正并没有什么卵用，还需要后续动态校正，当然也可以在此处计算字体宽度*/
      x = getWidth() - TagView.DEFAULT_WIDTH / 2;
    } else if (x < TagView.DEFAULT_WIDTH / 2) {
      x = TagView.DEFAULT_WIDTH / 2;
    }

    View view;
    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    if (y < getHeight() * 0.8) {
      params.topMargin = y - TagView.DEFAULT_HEIGHT / 2;
      view = new TagView(getContext(), TagView.Direction.TOP, this);
    } else {
      params.topMargin = y - TagView.DEFAULT_HEIGHT / 2;
      view = new TagView(getContext(), TagView.Direction.BOTTOM, this);
    }

    params.leftMargin = x - TagView.DEFAULT_WIDTH / 2;

    /*其中leftMargin和topMargin分别为当前x，y的坐标点减去TagView的默认宽高的一半得到的数值*/
    addView(view, params);
  }

  private void moveView(int x, int y) {
    /**
     * 如果TagView为锁定状态，将不允许移动
     */
    if (mCurrentTouchChildTagView == null
        || mCurrentTouchChildTagView.getStatus() == TagView.Status.LOCK) {
      return;
    }
    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    params.leftMargin = x - mStartX + mStartTouchViewLeft;
    params.topMargin = y - mStartY + mStartTouchViewTop;

    //改变箭头朝向
    if (mCurrentTouchChildTagView instanceof TagView) {
      if (y < getHeight() * 0.8) {
        params.topMargin = y - TagView.DEFAULT_HEIGHT / 2;
        mCurrentTouchChildTagView.changeDirection(TagView.Direction.TOP);
      } else {
        params.topMargin = y - TagView.DEFAULT_HEIGHT / 2;
        mCurrentTouchChildTagView.changeDirection(TagView.Direction.BOTTOM);
      }
    }
    mCurrentTouchChildTagView.controlMoveArrow(x, mStartX, true);
    //限制子控件移动必须在视图范围内
    if (params.leftMargin < 0
        || (params.leftMargin + mCurrentTouchChildTagView.getWidth()) > getWidth()) {
      params.leftMargin = mCurrentTouchChildTagView.getLeft();
      mCurrentTouchChildTagView.controlMoveArrow(x, mStartX, false);
    }
    if (params.topMargin < 0
        || (params.topMargin + mCurrentTouchChildTagView.getHeight()) > getHeight()) {
      params.topMargin = mCurrentTouchChildTagView.getTop();
    }

    mCurrentTouchChildTagView.setLayoutParams(params);
  }

  /**
   * 判断给定的坐标中是否包含TagView
   *
   * @return 否包含TagView返回True，否则false
   */
  private boolean hasView(int x, int y) {
    //循环获取子view，判断xy是否在子view上，即判断是否按住了子view
    for (int index = 0; index < this.getChildCount(); index++) {
      View view = this.getChildAt(index);
      //判定View为TagView 而不是背景ImageView
      if (view instanceof TagView) {
        int left = (int) view.getX();
        int top = (int) view.getY();
        int right = view.getRight();
        int bottom = view.getBottom();
        Rect rect = new Rect(left, top, right, bottom);
        boolean contains = rect.contains(x, y);
        //如果是与子view重叠则返回真,表示已经有了view不需要添加新view了
        if (contains) {
          mCurrentTouchChildTagView = (TagView) view;
          mCurrentTouchChildTagView.bringToFront();
          return true;
        }
      }
    }
    mCurrentTouchChildTagView = null;
    return false;
  }

  private int getScreenWidth() {
    return mContext.getResources().getDisplayMetrics().widthPixels;
  }

  /**
   * @return 返回当前父容器包含的Tag数量
   */
  public int getTagViewNum() {
    int tagNum = 0;
    for (int index = 0; index < this.getChildCount(); index++) {
      View view = getChildAt(index);
      if (view instanceof TagView) {
        tagNum++;
      }
    }
    return tagNum;
  }

  /**
   * 设置能够添加TAG的最大数量，超过该指将会调用mOnMaxNumTagCallback
   *
   * @param maxTag 最大TAG数量
   */
  public void setAcceptMaxTag(int maxTag) {
    maxTagNum = maxTag;
  }

  public void addTagViews(List<TagLocationData> tagLocations) {
    for (TagLocationData data : tagLocations) {
      addTagView(data);
    }
  }

  public List<TagLocationData> getTagData() {
    List<TagLocationData> list = new ArrayList<>();
    for (int index = 0; index < this.getChildCount(); index++) {
      View view = this.getChildAt(index);
      //判定View为TagView 而不是背景ImageView
      if (view instanceof TagView) {
        list.add(((TagView) view).getLocationData());
      }
    }
    return list;
  }

  /**
   * 通过数据对象来创建Tagging，注意，此方法只适用于从服务器下发的数据用于填充
   */
  private void addTagView(TagLocationData data) {
    View view;
    LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    double y = data.getLocationY(getHeight()) + TagView.DEFAULT_ARROW_HEIGHT;
    if (y < getHeight() * 0.8) {
      params.topMargin = (int) (y + TagView.DEFAULT_ARROW_HEIGHT - TagView.DEFAULT_HEIGHT / 2);
      view = new TagView(getContext(), TagView.Direction.TOP, this);
    } else {
      params.topMargin = (int) (y + TagView.DEFAULT_ARROW_HEIGHT - TagView.DEFAULT_HEIGHT / 2);
      view = new TagView(getContext(), TagView.Direction.BOTTOM, this);
    }

    /*
      在创建TagView之后将会填充Text造成TagView的宽度超过或者小于默认值，因此需要保证高度正确的同时做
      箭头的偏移矫正
     */
    double x = data.getLocationX(getWidth());


     /*对X坐标进行有效范围矫正*/
    if (x < TagView.DEFAULT_WIDTH / 2) {
      x = TagView.DEFAULT_WIDTH / 2;
    }

    params.leftMargin = (int) (x - TagView.DEFAULT_WIDTH / 2);

    if (x + TagView.DEFAULT_WIDTH / 2 >= getWidth()) {
      TextPaint textPaint = new TextPaint();
      textPaint.setTextSize(sp2px(mContext, 11));//设置字体大小
      float width =
          Layout.getDesiredWidth(data.getTagText(), textPaint) + TagView.DEFAULT_EMPTY_WIDTH / 2;
      x = (int) (getWidth() - width / 2);
      params.leftMargin = (int) (x - width / 2);
    } else {
      params.leftMargin = (int) (x - TagView.DEFAULT_WIDTH / 2);
    }


    /*((TagView) view).setText("我是超长超长的字符标签");*/
    ((TagView) view).setText(data.getTagText());
    ((TagView) view).setRegulateData(data);

    /*其中leftMargin和topMargin分别为当前x，y的坐标点减去TagView的默认宽高的一半得到的数值*/
    addView(view, params);
  }

  /**
   * 移除所有的TagView 用于测试目的
   */
  public void removeAllTagView() {
    List<View> temp = new ArrayList<>();
    for (int index = 0; index < getChildCount(); index++) {
      View view = getChildAt(index);
      if (view instanceof TagView) {
        temp.add(view);
      }
    }
    for (View view : temp) {
      removeView(view);
    }
  }
}

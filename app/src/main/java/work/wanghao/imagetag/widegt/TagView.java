package work.wanghao.imagetag.widegt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import work.wanghao.imagetag.R;

/**
 * @author doublemine
 *         Created  on 2017/06/25 15:45.
 *         Summary:
 */

@SuppressWarnings("unused") public class TagView extends FrameLayout {
  private final static String TAG = TagView.class.getSimpleName();
  private static final int CLICK_RANGE = 5;

  /*TagView的默认宽高，如需精确适配，请在更新View_tag布局之后，通过getWidth()和getHeight()手动获取一次*/
  public static final int DEFAULT_WIDTH = 223;
  public static final int DEFAULT_HEIGHT = 174;

  public static final int DEFAULT_ARROW_HEIGHT = 42;
  public static final int DEFAULT_ARROW_WIDTH = 95;

  public static final int DEFAULT_EMPTY_WIDTH = 136;

  public enum Direction {TOP, BOTTOM}

  public enum Status {Normal, LOCK}

  private Context mContext;
  private TextView mTextView;
  private ImageView mArrowTop;
  private ImageView mArrowBottom;
  private ImageView mClose;

  private Direction mDirection = Direction.TOP;
  private Status mStatus = Status.Normal;

  private int mCenterX;//箭头指向的中心坐标点X的偏移量，用于设置箭头居中的参考坐标

  private OnClickListener mOnClickListener;

  private TagLocationData mRegulateData;//校正数据
  private boolean mResetLocationX;

  public TagView(@NonNull Context context, Direction direction, OnClickListener listener) {
    super(context);
    this.mContext = context;
    this.mDirection = direction;
    mOnClickListener = listener;
    init();
  }

  private void init() {
    initView();
    directionChange();
  }

  private void initView() {
    View rootView = LayoutInflater.from(mContext).inflate(R.layout.view_tag, this, true);
    mTextView = (TextView) rootView.findViewById(R.id.tv_tagging);
    mArrowBottom = (ImageView) rootView.findViewById(R.id.arrow_bottom);
    mArrowTop = (ImageView) rootView.findViewById(R.id.arrow_top);
    mClose = (ImageView) rootView.findViewById(R.id.close);

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        resetLocationX();
        /*判断箭头是否需要校正，新建TAG==居中，渲染==校正*/
        if (mRegulateData == null) {
          setArrowCenter();
        } else {
          regulateArrow();
        }
      }
    });

    mClose.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        if (mOnClickListener != null) mOnClickListener.onClick(TagView.this);
      }
    });
  }

  private void directionChange() {
    switch (mDirection) {
      case TOP: {
        int margin = ((LayoutParams) mArrowBottom.getLayoutParams()).leftMargin;
        mArrowBottom.setVisibility(GONE);
        mArrowTop.setVisibility(VISIBLE);
        FrameLayout.LayoutParams params = (LayoutParams) mArrowTop.getLayoutParams();
        params.leftMargin = margin;
        mArrowTop.setLayoutParams(params);
        break;
      }
      case BOTTOM: {
        int margin = ((LayoutParams) mArrowTop.getLayoutParams()).leftMargin;
        mArrowTop.setVisibility(GONE);
        mArrowBottom.setVisibility(VISIBLE);
        FrameLayout.LayoutParams params = (LayoutParams) mArrowBottom.getLayoutParams();
        params.leftMargin = margin;
        mArrowBottom.setLayoutParams(params);
        break;
      }
    }
  }

  public void changeDirection(Direction direction) {
    if (mDirection == direction) return;
    mDirection = direction;
    directionChange();
  }

  /**
   * 父容器调用来改变view的箭头坐标
   *
   * @param x 当前x坐标点
   * @param startX 此次触摸事件起始x坐标点
   * @param reset true：将重置箭头到中心位置
   */
  protected void controlMoveArrow(int x, int startX, boolean reset) {
   /* 算法有改进空间，startX位置为此次触摸事件第一次按下X点坐标，在当前触摸事件内暂时没有更新，会造成reset操作
        需要手动滑动到超过该点才会判定为对应的左滑或者右滑*/
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);

    if (getCurrentArrow().getId() == R.id.arrow_top) {
      params.gravity = Gravity.TOP;
    } else {
      params.gravity = Gravity.BOTTOM;
    }

    if (reset) {
      if (x > startX) {//右滑
        if (((LayoutParams) getCurrentArrow().getLayoutParams()).leftMargin < mCenterX) {
          params.leftMargin = ((LayoutParams) getCurrentArrow().getLayoutParams()).leftMargin + 10;
        }
      } else if (x < startX) {//左滑
        if (((LayoutParams) getCurrentArrow().getLayoutParams()).leftMargin > mCenterX) {
          params.leftMargin = ((LayoutParams) getCurrentArrow().getLayoutParams()).leftMargin - 10;
        }
      }
    } else {
      if (x > startX) {//右滑
        params.leftMargin = ((LayoutParams) getCurrentArrow().getLayoutParams()).leftMargin + 3;
      } else if (x < startX) {//左滑
        params.leftMargin = ((LayoutParams) getCurrentArrow().getLayoutParams()).leftMargin - 3;
      }
    }

    //限制子控件移动必须在视图范围内
    if ((params.leftMargin + getCurrentArrow().getWidth()) > getWidth() - 6) {
      return;
    } else if (params.leftMargin < 6) return;

    getCurrentArrow().setLayoutParams(params);
  }

  /*设置箭头水平居中*/
  private void setArrowCenter() {
    mCenterX = setArrowLeftMargin(getWidth() / 2 - getCurrentArrow().getWidth() / 2);
  }

  /**
   * 校正箭头到指定X坐标，需要保证Y坐标提前已经正确指定(高度不会参与校正)
   */
  private void regulateArrow() {
    int left = (int) (mRegulateData.getLocationX(getScreenWidth())
        - getLeft()
        - getCurrentArrow().getWidth() / 2);
    setArrowLeftMargin(left);
    mRegulateData = null;
  }

  private int setArrowLeftMargin(int leftMargin) {
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
    if (getCurrentArrow().getId() == R.id.arrow_top) {
      params.gravity = Gravity.TOP;
    } else {
      params.gravity = Gravity.BOTTOM;
    }
    params.leftMargin = leftMargin;
    getCurrentArrow().setLayoutParams(params);
    return params.leftMargin;
  }

  /**
   * @return 返回当前箭头对象
   */
  private View getCurrentArrow() {
    return mArrowBottom.getVisibility() == GONE ? mArrowTop : mArrowBottom;
  }

  private int getScreenWidth() {
    return mContext.getResources().getDisplayMetrics().widthPixels;
  }

  /**
   * @return 返回当前view箭头的X偏移量
   */
  protected int getChildLeftMargin() {
    return ((LayoutParams) getCurrentArrow().getLayoutParams()).leftMargin;
  }

  /**
   * 显示关闭按钮，点击该按钮将移除该View
   */
  public void showCloseButton() {
    if (mClose.getVisibility() == GONE) {
      mClose.setVisibility(VISIBLE);
    } else {
      mClose.setVisibility(GONE);
    }
  }

  /**
   * 给View设置TAG
   *
   * @param text 对应的TAG text
   */
  public void setText(String text) {
    mTextView.setText(text);
    /**
     * 如果TagView设置了文本，那么将被锁定而无法移动
     */
    mStatus = Status.LOCK;
  }

  public Status getStatus() {
    return mStatus;
  }

  /**
   * @return 返回当前TAG指示的坐标数组
   */
  public double[] getLocationInContainer() {

    double percentY = getTop() / (double) getScreenWidth();
    double percentX = (getCurrentArrow().getLeft() + getCurrentArrow().getWidth() / 2d + getLeft())
        / (double) getScreenWidth();

    return new double[] {
        percentX, percentY
    };
  }

  /**
   * @return 返回当前TAG所包含的坐标以及标签文本对象
   */
  public TagLocationData getLocationData() {
    double percentY = (double) getTop() / (double) getScreenWidth();
    double percentX =
        (double) (getCurrentArrow().getLeft() + getCurrentArrow().getWidth() / 2 + getLeft())
            / (double) getScreenWidth();

    return new TagLocationData(percentX, percentY, mTextView.getText().toString().trim());
  }

  /**
   * @return 返回当前TAG的Text内容
   */
  public String getTagText() {
    return mTextView.getText().toString().trim();
  }

  public void setRegulateData(TagLocationData regulateData) {
    mRegulateData = regulateData;
  }

  /**
   * @param reset 是否重设当前TagView的X轴位置
   */
  public void resetCurrentXPosition(boolean reset) {
    mResetLocationX = reset;
  }

  private void resetLocationX() {
    if (mResetLocationX) {
      LayoutParams layoutParams = (LayoutParams) getLayoutParams();
      layoutParams.leftMargin = getScreenWidth() - getWidth();
      setLayoutParams(layoutParams);

      mResetLocationX = false;
    }
  }
}

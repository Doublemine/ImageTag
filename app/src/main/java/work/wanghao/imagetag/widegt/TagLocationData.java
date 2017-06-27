package work.wanghao.imagetag.widegt;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author doublemine
 *         Created  on 2017/06/26 23:28.
 *         Summary:
 */

public class TagLocationData implements Parcelable {
  private double percentX;
  private double percentY;
  private String tagText;

  public TagLocationData(double percentX, double percentY, String tagText) {
    this.percentX = percentX;
    this.percentY = percentY;
    this.tagText = tagText;
  }

  public double getPercentX() {
    return percentX;
  }

  public double getPercentY() {
    return percentY;
  }

  /**
   * @param containerSize 基数
   * @return 将百分比转化为对应的坐标数值
   */
  public double getLocationX(double containerSize) {
    return containerSize * percentX;
  }

  /**
   * @param containerSize 基数
   * @return 将百分比转化为对应的坐标数值
   */
  public double getLocationY(double containerSize) {
    return containerSize * percentY;
  }

  public String getTagText() {
    return tagText;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeDouble(this.percentX);
    dest.writeDouble(this.percentY);
    dest.writeString(this.tagText);
  }

  protected TagLocationData(Parcel in) {
    this.percentX = in.readDouble();
    this.percentY = in.readDouble();
    this.tagText = in.readString();
  }

  public static final Creator<TagLocationData> CREATOR = new Creator<TagLocationData>() {
    @Override public TagLocationData createFromParcel(Parcel source) {
      return new TagLocationData(source);
    }

    @Override public TagLocationData[] newArray(int size) {
      return new TagLocationData[size];
    }
  };
}

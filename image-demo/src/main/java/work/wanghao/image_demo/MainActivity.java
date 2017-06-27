package work.wanghao.image_demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import java.util.ArrayList;
import java.util.List;
import work.wanghao.imagetag.widegt.TagLayout;
import work.wanghao.imagetag.widegt.TagLocationData;

public class MainActivity extends AppCompatActivity {

  private Button save;
  private Button set;
  private Button clear;
  private TagLayout mTagLayout;

  private List<TagLocationData> mTagLocationDatas;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mTagLocationDatas = new ArrayList<>();
    save = (Button) findViewById(R.id.save);
    set = (Button) findViewById(R.id.set);
    clear = (Button) findViewById(R.id.clear);
    mTagLayout = (TagLayout) findViewById(R.id.container);
    save.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mTagLocationDatas.clear();
        mTagLocationDatas = mTagLayout.getTagData();
      }
    });

    set.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mTagLayout.addTagViews(mTagLocationDatas);
      }
    });
    clear.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mTagLayout.removeAllTagView();
      }
    });
  }
}

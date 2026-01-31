package android.studio.zero.regular.expression.preview;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 宿主 Activity，用于展示 RegexPreviewFragment
 * @author android_zero
 */
public class RegexPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regex_preview);

        // 设置 Toolbar 标题和返回按钮
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Regex Preview");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setElevation(0); // 去除阴影，配合扁平化设计
        }

        // 动态加载 Fragment (仅在第一次创建时加载，防止旋转屏幕后重复叠加)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new RegexPreviewFragment())
                    .commit();
        }
    }

    /**
     * 处理 Toolbar 左上角返回按钮点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // 结束当前 Activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
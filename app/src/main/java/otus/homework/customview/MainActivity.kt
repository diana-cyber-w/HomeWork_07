package otus.homework.customview

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import otus.homework.customview.data.Item
import otus.homework.customview.view.CategoryChartView
import otus.homework.customview.view.OnCategoryClickListener
import otus.homework.customview.view.PieChartView

class MainActivity : AppCompatActivity(), OnCategoryClickListener {
    private lateinit var pieChartView: PieChartView
    private lateinit var categoryChartView: CategoryChartView
    private var clickedCategory = MutableStateFlow<String?>(null)
    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val values = Json.decodeFromString<List<Item>>(
            this.resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() })

        pieChartView = findViewById(R.id.pieChartView)
        pieChartView.setValues(values)
        pieChartView.setOnCategoryClickListener(this)

        categoryChartView = findViewById(R.id.categoryChartView)
        button = findViewById(R.id.button)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                clickedCategory.collect {
                    clickedCategory.value?.let {
                        categoryChartView.putValues(it, values)
                    }
                }
            }
        }

        button.setOnClickListener {
            val intent = Intent(this, MotionActivity::class.java)
            val options =
                ActivityOptions.makeSceneTransitionAnimation(this, button, "shared_button")
            startActivity(intent, options.toBundle())
        }
    }

    override fun onCategoryClick(category: String) {
        Toast.makeText(this, "Выбрана категория: $category", Toast.LENGTH_SHORT).show()
        clickedCategory.value = category
    }
}
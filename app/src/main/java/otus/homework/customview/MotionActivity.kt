package otus.homework.customview

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout

class MotionActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var motionLayout: MotionLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.motion_activity)

        button = findViewById(R.id.button)
        motionLayout = findViewById(R.id.motionLayout)

        button.setOnClickListener {
            motionLayout.transitionToEnd()
        }
    }
}
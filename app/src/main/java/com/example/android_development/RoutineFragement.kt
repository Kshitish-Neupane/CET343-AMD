package com.example.android_development

import android.content.Intent
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.sqrt

class RoutineFragment : Fragment(R.layout.fragment_reminders),
    SensorEventListener {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: RoutineAdapter
    private lateinit var sensorManager: SensorManager
    private lateinit var daysContainer: LinearLayout

    private var selectedDay = "Sun"

    private var accel = 0f
    private var accelCurrent = SensorManager.GRAVITY_EARTH
    private var accelLast = SensorManager.GRAVITY_EARTH

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rv = view.findViewById(R.id.rvReminders)
        daysContainer = view.findViewById(R.id.daysContainer)
        val btnSms = view.findViewById<Button>(R.id.btnSendSms)

        adapter = RoutineAdapter(mutableListOf()) { }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        setupDays()
        loadDay("Sun")
        attachSwipe()
        setupShake()

        btnSms.setOnClickListener {
            sendSms(adapter.getSelected())
        }
    }

    // Days
    private fun setupDays() {
        val days = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
        daysContainer.removeAllViews()

        days.forEach { day ->
            val b = Button(requireContext()).apply {
                text = day
                isAllCaps = false
                setOnClickListener {
                    selectedDay = day
                    loadDay(day)
                }
            }
            daysContainer.addView(b)
        }
    }

    private fun loadDay(day: String) {
        val prefs = requireContext().getSharedPreferences("planner", 0)
        val list = mutableListOf<RoutineItem>()

        prefs.all.forEach { (key, value) ->
            if (!key.startsWith("${day}_")) return@forEach
            val raw = value as? String ?: return@forEach

            raw.split("\n").forEach { line ->
                val p = line.split("|")
                if (p.size >= 4) {
                    list.add(
                        RoutineItem(
                            title = p[0],
                            equipment = p[1],
                            reps = p[2],
                            sets = p[3],
                            imageUri = p.getOrNull(4)
                        )
                    )
                }
            }
        }

        adapter.setItems(list)
    }

    //Swipe
    private fun attachSwipe() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {

            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                t: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {

                val pos = vh.adapterPosition
                if (pos < 0) return

                if (dir == ItemTouchHelper.RIGHT) {
                    adapter.markComplete(pos)
                    adapter.notifyItemChanged(pos)
                } else {
                    val item = adapter.getItem(pos)
                    adapter.deleteAt(pos)
                    deleteFromPlanner(item)
                }
            }



            override fun onChildDraw(
                c: Canvas,
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                state: Int,
                active: Boolean
            ) {
                val paint = Paint().apply {
                    textSize = 48f
                    typeface = Typeface.DEFAULT_BOLD
                }

                val y = vh.itemView.top + vh.itemView.height * 0.7f

                if (dX > 0) {
                    paint.color = Color.parseColor("#FF9800")
                    c.drawText("COMPLETED", 40f, y, paint)
                } else {
                    paint.color = Color.RED
                    c.drawText("DELETE", rv.width - 280f, y, paint)
                }

                super.onChildDraw(c, rv, vh, dX, dY, state, active)
            }

        }).attachToRecyclerView(rv)
    }


    private fun deleteFromPlanner(item: RoutineItem) {
        val prefs = requireContext().getSharedPreferences("planner", 0)

        prefs.all.forEach { (key, value) ->
            if (value !is String) return@forEach

            val updated = value
                .split("\n")
                .filterNot { it.startsWith(item.title + "|") }
                .joinToString("\n")

            prefs.edit().putString(key, updated).apply()
        }
    }


    //ShakeReset
    private fun setupShake() {
        sensorManager = requireContext().getSystemService(SensorManager::class.java)
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onSensorChanged(e: SensorEvent) {
        val x = e.values[0]
        val y = e.values[1]
        val z = e.values[2]

        accelLast = accelCurrent
        accelCurrent = sqrt(x*x + y*y + z*z)
        accel = accel * 0.9f + (accelCurrent - accelLast)

        if (accel > 12) {
            adapter.resetAll()
            Toast.makeText(requireContext(), "Routine reset", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    //SMS
    private fun sendSms(items: List<RoutineItem>) {
        if (items.isEmpty()) return

        val text = items.joinToString("\n") {
            "${it.title} (${it.equipment}, ${it.reps} x ${it.sets})"
        }

        startActivity(
            Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:")
                putExtra("sms_body", text)
            }
        )
    }
}

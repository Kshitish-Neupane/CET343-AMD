package com.example.android_development

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var tvPlanSummary: TextView
    private lateinit var daysContainer: LinearLayout
    private var selectedDay = "Sun"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvPlanSummary = view.findViewById(R.id.tvPlanSummary)
        daysContainer = view.findViewById(R.id.daysContainer)

        setupDays()
        updatePlanSummary(selectedDay)

        view.findViewById<View>(R.id.btnAddPlan).setOnClickListener {
            showPlanDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        updatePlanSummary(selectedDay)
    }

    private fun setupDays() {
        val days = listOf("Sun","Mon","Tue","Wed","Thu","Fri","Sat")
        daysContainer.removeAllViews()

        days.forEach { day ->
            val btn = Button(requireContext()).apply {
                text = day
                isAllCaps = false
                setBackgroundResource(R.drawable.day_chip)
                setTextColor(resources.getColor(R.color.black, null))
                setOnClickListener {
                    selectedDay = day
                    updateDaySelection()
                    updatePlanSummary(day)
                }
            }
            daysContainer.addView(btn)
        }
        updateDaySelection()
    }

    private fun updateDaySelection() {
        for (i in 0 until daysContainer.childCount) {
            val b = daysContainer.getChildAt(i) as Button
            if (b.text == selectedDay) {
                b.setBackgroundResource(R.drawable.day_chip_selected)
                b.setTextColor(resources.getColor(R.color.white, null))
            } else {
                b.setBackgroundResource(R.drawable.day_chip)
                b.setTextColor(resources.getColor(R.color.black, null))
            }
        }
    }

    private fun showPlanDialog() {
        val ctx = context ?: return
        val v = layoutInflater.inflate(R.layout.dialog_plan_selector, null)

        val cbStrength = v.findViewById<CheckBox>(R.id.cbStrength)
        val cbHiit = v.findViewById<CheckBox>(R.id.cbHiit)
        val cbYoga = v.findViewById<CheckBox>(R.id.cbYoga)
        val cbRest = v.findViewById<CheckBox>(R.id.cbRest)

        v.findViewById<TextView>(R.id.tvStrength).setOnClickListener {
            openRoutine(StrengthActivity::class.java)
        }
        v.findViewById<TextView>(R.id.tvHiit).setOnClickListener {
            openRoutine(HiitActivity::class.java)
        }
        v.findViewById<TextView>(R.id.tvYoga).setOnClickListener {
            openRoutine(YogaActivity::class.java)
        }

        cbRest.setOnCheckedChangeListener { _, checked ->
            cbStrength.isEnabled = !checked
            cbHiit.isEnabled = !checked
            cbYoga.isEnabled = !checked
            if (checked) {
                cbStrength.isChecked = false
                cbHiit.isChecked = false
                cbYoga.isChecked = false
            }
        }

        val d = AlertDialog.Builder(ctx).setView(v).create()

        v.findViewById<View>(R.id.btnSavePlan).setOnClickListener {
            val prefs = ctx.getSharedPreferences("planner", 0)
            prefs.edit()
                .putBoolean("${selectedDay}_strength_selected", cbStrength.isChecked)
                .putBoolean("${selectedDay}_hiit_selected", cbHiit.isChecked)
                .putBoolean("${selectedDay}_yoga_selected", cbYoga.isChecked)
                .putBoolean("${selectedDay}_rest", cbRest.isChecked)
                .apply()
            updatePlanSummary(selectedDay)
            d.dismiss()
        }

        d.show()
    }



    private fun openRoutine(cls: Class<*>) {
        val i = Intent(requireContext(), cls)
        i.putExtra("DAY", selectedDay)
        startActivity(i)
    }


    private fun updatePlanSummary(day: String) {
        val prefs = requireContext().getSharedPreferences("planner", 0)
        val sb = StringBuilder()

        fun parseBlock(title: String, key: String) {
            val raw = prefs.getString(key, null) ?: return
            sb.append("$title:\n")
            raw.split("\n").forEach { line ->
                val parts = line.split("||")
                if (parts.size >= 4) {
                    val name = parts[0]
                    val equip = parts[1]
                    val reps = parts[2]
                    val sets = parts[3]
                    sb.append("• $name ($equip, $reps reps × $sets sets)\n")
                }
            }
            sb.append("\n")
        }

        parseBlock("Full-Body Strength Training", "${day}_strength")
        parseBlock("HIIT Cardio", "${day}_hiit")
        parseBlock("Yoga", "${day}_yoga")

        tvPlanSummary.text =
            if (sb.isNotEmpty()) sb.toString()
            else "No workouts planned for $day"
    }

}

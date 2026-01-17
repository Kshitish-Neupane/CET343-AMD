package com.example.android_development

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HiitActivity : AppCompatActivity() {

    private lateinit var adapter: RoutineAdapter
    private var selectedImageUri: String? = null
    private var ivPreviewGlobal: ImageView? = null

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri.toString()
                ivPreviewGlobal?.setImageURI(uri)
                ivPreviewGlobal?.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine)

        findViewById<TextView>(R.id.tvTitle).text = "HIIT Cardio"
        findViewById<CheckBox>(R.id.cbRecommended)
            .setOnCheckedChangeListener { _, isChecked ->
                adapter.setRecommended(isChecked)
            }



        val list = mutableListOf(
            RoutineItem("Burpees", "Bodyweight", "30s", "4", recommended = true),
            RoutineItem("Mountain Climbers", "Bodyweight", "40s", "4", recommended = true),
            RoutineItem("Jump Squats", "Bodyweight", "30s", "3", recommended = true)
        )

        adapter = RoutineAdapter(list) { item ->
            showEditDialog(item)
        }

        findViewById<RecyclerView>(R.id.rvList).apply {
            layoutManager = LinearLayoutManager(this@HiitActivity)
            adapter = this@HiitActivity.adapter
        }

        findViewById<View>(R.id.btnAddWorkout).setOnClickListener {
            showAddDialog()
        }

        findViewById<View>(R.id.btnDelete).setOnClickListener {
            adapter.deleteSelected()
        }

        findViewById<View>(R.id.btnSaveRoutine).setOnClickListener {

            val day = intent.getStringExtra("DAY") ?: return@setOnClickListener
            val prefs = getSharedPreferences("planner", MODE_PRIVATE)

            val selected = adapter.getSelected()

            val text = selected.joinToString("\n") {
                val uri = it.imageUri ?: ""
                "${it.title}||${it.equipment}||${it.reps}||${it.sets}||$uri"
            }

            val keySuffix = when {
                findViewById<TextView>(R.id.tvTitle).text.toString().contains("Strength", true) -> "strength"
                findViewById<TextView>(R.id.tvTitle).text.toString().contains("HIIT", true) -> "hiit"
                else -> "yoga"
            }

            prefs.edit().putString("${day}_${keySuffix}", text).apply()
            finish()

        }
    }

    private fun showAddDialog() {
        selectedImageUri = null
        val v = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null, false)

        val etWorkout = v.findViewById<EditText>(R.id.etWorkout)
        val etEquip = v.findViewById<EditText>(R.id.etEquipment)
        val etSets = v.findViewById<EditText>(R.id.etSets)
        val etReps = v.findViewById<EditText>(R.id.etReps)
        val btnAddImage = v.findViewById<Button>(R.id.btnAddImage)
        val ivPreview = v.findViewById<ImageView>(R.id.ivPreview)

        ivPreview.visibility = View.GONE
        ivPreviewGlobal = ivPreview

        val dialog = AlertDialog.Builder(this).setView(v).create()

        btnAddImage.setOnClickListener {
            pickImage.launch("image/*")
        }

        v.findViewById<View>(R.id.btnAdd).setOnClickListener {
            adapter.addItem(
                RoutineItem(
                    title = etWorkout.text.toString(),
                    equipment = etEquip.text.toString(),
                    reps = etReps.text.toString(),
                    sets = etSets.text.toString(),
                    recommended = false,
                    imageUri = selectedImageUri
                )

            )
            dialog.dismiss()
        }

        v.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showEditDialog(item: RoutineItem) {
        val v = LayoutInflater.from(this).inflate(R.layout.dialog_add_item, null, false)

        val etWorkout = v.findViewById<EditText>(R.id.etWorkout)
        val etEquip = v.findViewById<EditText>(R.id.etEquipment)
        val etSets = v.findViewById<EditText>(R.id.etSets)
        val etReps = v.findViewById<EditText>(R.id.etReps)
        val ivPreview = v.findViewById<ImageView>(R.id.ivPreview)
        val btnAddImage = v.findViewById<Button>(R.id.btnAddImage)

        etWorkout.setText(item.title)
        etEquip.setText(item.equipment)
        etSets.setText(item.sets)
        etReps.setText(item.reps)

        if (item.imageUri != null) {
            ivPreview.setImageURI(Uri.parse(item.imageUri))
            ivPreview.visibility = View.VISIBLE
        }

        ivPreviewGlobal = ivPreview
        selectedImageUri = item.imageUri

        val dialog = AlertDialog.Builder(this).setView(v).create()

        btnAddImage.setOnClickListener { pickImage.launch("image/*") }

        v.findViewById<View>(R.id.btnAdd).setOnClickListener {
            item.title = etWorkout.text.toString()
            item.equipment = etEquip.text.toString()
            item.reps = etReps.text.toString()
            item.sets = etSets.text.toString()
            item.imageUri = selectedImageUri
            adapter.notifyDataSetChanged()
            dialog.dismiss()
        }

        v.findViewById<View>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}

package com.javierf.pitstopsparcial.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.javierf.pitstopsparcial.R
import com.javierf.pitstopsparcial.model.PitStop
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PitStopFormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PITSTOP = "EXTRA_PITSTOP"   // se devuelve a la lista
        const val EXTRA_EDIT_ID = "EXTRA_EDIT_ID"   // gancho para modo edición futuro
    }

    // Views
    private lateinit var spPiloto: Spinner
    private lateinit var spEscuderia: Spinner
    private lateinit var etTiempo: EditText
    private lateinit var spCompuesto: Spinner
    private lateinit var etNeumaticos: EditText
    private lateinit var spEstado: Spinner
    private lateinit var tvMotivo: TextView
    private lateinit var etMotivo: EditText
    private lateinit var etMecanico: EditText
    private lateinit var etFechaHora: EditText
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button

    // Fecha / hora seleccionadas
    private var fechaSel: LocalDate = LocalDate.now()
    private var horaSel: LocalTime = LocalTime.now()
    private val dtf: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    // Para edición (en el futuro)
    private var editId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pitstop_form)

        bindViews()
        setupSpinners()
        setupEstadoMotivoToggle()
        setupFechaHoraPicker()
        loadIfEditing()
        setupButtons()
    }

    private fun bindViews() {
        spPiloto = findViewById(R.id.spPiloto)
        spEscuderia = findViewById(R.id.spEscuderia)
        etTiempo = findViewById(R.id.etTiempo)
        spCompuesto = findViewById(R.id.spCompuesto)
        etNeumaticos = findViewById(R.id.etNeumaticos)
        spEstado = findViewById(R.id.spEstado)
        tvMotivo = findViewById(R.id.tvMotivo)
        etMotivo = findViewById(R.id.etMotivo)
        etMecanico = findViewById(R.id.etMecanico)
        etFechaHora = findViewById(R.id.etFechaHora)
        btnGuardar = findViewById(R.id.btnGuardar)
        btnCancelar = findViewById(R.id.btnCancelar)
    }

    // Helper para cargar arrays en Spinners
    private fun Spinner.fromArray(arrayRes: Int) {
        adapter = ArrayAdapter.createFromResource(
            this@PitStopFormActivity,
            arrayRes,
            android.R.layout.simple_spinner_item
        ).also {
            (it as ArrayAdapter<*>).setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun setupSpinners() {
        spPiloto.fromArray(R.array.pilotos_array)
        spEscuderia.fromArray(R.array.escuderias_array)
        spCompuesto.fromArray(R.array.compuestos_array)
        spEstado.fromArray(R.array.estados_array)
    }

    private fun setupEstadoMotivoToggle() {
        fun toggleMotivo() {
            val fallido = spEstado.selectedItem?.toString() == "Fallido"
            tvMotivo.isEnabled = fallido
            etMotivo.isEnabled = fallido
            tvMotivo.alpha = if (fallido) 1f else 0.4f
            etMotivo.alpha = if (fallido) 1f else 0.4f
            if (!fallido) etMotivo.text.clear()
        }
        toggleMotivo()
        spEstado.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long
            ) = toggleMotivo()

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) = Unit
        })
    }

    private fun setupFechaHoraPicker() {
        // valor inicial
        etFechaHora.setText(LocalDateTime.of(fechaSel, horaSel).format(dtf))

        etFechaHora.setOnClickListener {
            val now = LocalDateTime.now()

            DatePickerDialog(
                this@PitStopFormActivity,
                { _, y, m, d ->
                    fechaSel = LocalDate.of(y, m + 1, d)

                    TimePickerDialog(
                        this@PitStopFormActivity,
                        { _, hh, mm ->
                            horaSel = LocalTime.of(hh, mm)
                            val dt = LocalDateTime.of(fechaSel, horaSel)
                            etFechaHora.setText(dt.format(dtf))
                        },
                        now.hour, now.minute, true
                    ).show()
                },
                now.year, now.monthValue - 1, now.dayOfMonth
            ).show()
        }
    }

    private fun loadIfEditing() {
        editId = intent.getLongExtra(EXTRA_EDIT_ID, 0L).takeIf { it > 0L }
        // Si luego conectamos Room/Repo, aquí precargamos los campos.
    }

    private fun setupButtons() {
        btnCancelar.setOnClickListener { finish() }

        btnGuardar.setOnClickListener {
            val errors = validateInputs()
            if (errors.isNotEmpty()) {
                Toast.makeText(this, errors.joinToString("\n"), Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val pit = buildModel()
            setResult(RESULT_OK, Intent().putExtra(EXTRA_PITSTOP, pit))
            finish()
        }
    }

    private fun validateInputs(): List<String> {
        val errs = mutableListOf<String>()

        val tiempo = etTiempo.text.toString().toDoubleOrNull()
        if (tiempo == null || tiempo <= 0.0) errs += "Tiempo inválido (usa decimales, p.ej. 2.35)."

        val neums = etNeumaticos.text.toString().toIntOrNull()
        if (neums == null || neums !in 0..4) errs += "Neumáticos cambiados debe estar entre 0 y 4."

        val mecanico = etMecanico.text.toString().trim()
        if (mecanico.isEmpty()) errs += "Mecánico principal es obligatorio."

        val estado = spEstado.selectedItem?.toString() ?: ""
        val motivo = etMotivo.text.toString().trim()
        if (estado == "Fallido" && motivo.isEmpty()) errs += "Motivo del fallo es obligatorio cuando el estado es Fallido."

        return errs
    }

    private fun buildModel(): PitStop {
        val piloto = spPiloto.selectedItem.toString()
        val escuderia = spEscuderia.selectedItem.toString()
        val tiempo = etTiempo.text.toString().toDouble()
        val compuesto = spCompuesto.selectedItem.toString()
        val neums = etNeumaticos.text.toString().toInt()
        val estado = spEstado.selectedItem.toString()
        val motivo = etMotivo.text.toString().trim().ifEmpty { null }
        val dt = LocalDateTime.of(fechaSel, horaSel)
        val mecanico = etMecanico.text.toString().trim()

        return PitStop(
            id = editId ?: 0L,
            piloto = piloto,
            escuderia = escuderia,
            tiempoSegundos = tiempo,
            compuesto = compuesto,
            neumaticosCambiados = neums,
            estado = estado,
            motivoFallo = motivo,
            mecanicoPrincipal = mecanico,
            fechaHora = dt
        )
    }
}

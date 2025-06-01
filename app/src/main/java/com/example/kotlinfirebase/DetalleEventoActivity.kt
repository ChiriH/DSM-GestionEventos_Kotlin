package com.example.kotlinfirebase
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlinfirebase.databinding.ActivityDetalleEventoBinding
import com.example.kotlinfirebase.model.Evento
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class DetalleEventoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleEventoBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var eventoId: String
    private var rolUsuario: String? = null
    private var estadoEvento: String = ""


    private val editarEventoLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val cambiosGuardados = data?.getBooleanExtra("cambiosGuardados", false) ?: false
            if (cambiosGuardados) {
                cargarDetalleEvento() // refrescar datos
                Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleEventoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        eventoId = intent.getStringExtra("eventoId") ?: ""

        cargarDetalleEvento()
        configurarListeners()
    }
    override fun onResume() {
        super.onResume()
        cargarDetalleEvento() // vuelve a obtener datos de Firestore
    }
    private fun cargarDetalleEvento() {
        db.collection("eventos").document(eventoId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.nombreEvento.text = document.getString("nombre")
                    binding.fechaEvento.text = document.getString("fecha")
                    binding.ubicacionEvento.text = document.getString("ubicacion")
                    binding.horaEvento.text = document.getString("hora")
                    binding.descripcionEvento.text = document.getString("descripcion")
                    estadoEvento = document.getString("estado") ?: ""

                    verificarRolYConfigurarUI()
                } else {
                    Toast.makeText(this, "Evento no encontrado", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar evento", Toast.LENGTH_SHORT).show()
            }
    }

    //Verificar rol de usuario y asi mostrar las debidas opciones
    private fun verificarRolYConfigurarUI() {
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { document ->
                rolUsuario = document.getString("rol")

                if (rolUsuario == "admin") {
                    binding.layoutAdminButtons.visibility = View.VISIBLE
                    binding.btnConfirmar.visibility = View.GONE
                    binding.btnCancelar.visibility = View.GONE
                    binding.layoutFeedback.visibility = View.GONE

                    if(estadoEvento=="cerrado"){
                        binding.btnEditarEvento.visibility = View.GONE
                        binding.btnCerrarEvento.visibility = View.GONE
                        binding.txtCerrado.visibility = View.VISIBLE
                    }else{
                        binding.txtCerrado.visibility = View.GONE
                    }
                } else {
                    binding.layoutAdminButtons.visibility = View.GONE

                    if (estadoEvento == "cerrado") {
                        binding.btnConfirmar.visibility = View.GONE
                        binding.btnCancelar.visibility = View.GONE
                        binding.layoutFeedback.visibility = View.VISIBLE
                        verificarConfirmacionEnEventoYResena()



                    } else {
                        // Verificar si el usuario ya está confirmado en el evento
                        db.collection("eventos").document(eventoId).get()
                            .addOnSuccessListener { evento ->
                                val confirmados = evento.get("confirmados") as? List<*>
                                if (confirmados != null && confirmados.contains(userId)) {
                                    // Ya confirmado
                                    binding.btnConfirmar.visibility = View.GONE
                                    binding.btnCancelar.visibility = View.VISIBLE
                                } else {
                                    // Aún no confirmado
                                    binding.btnConfirmar.visibility = View.VISIBLE
                                    binding.btnCancelar.visibility = View.GONE
                                }
                                binding.layoutFeedback.visibility = View.GONE
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al verificar confirmación", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar rol", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configurarListeners() {

            // BOTON admin para editar evento
        binding.btnEditarEvento.setOnClickListener {
            db.collection("eventos").document(eventoId)
                .update("mensajeCambio", "El evento ha sido editado.")
                .addOnSuccessListener {
                    val intent = Intent(this, EditarEventoActivity::class.java)
                    intent.putExtra("eventoId", eventoId)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "No se pudo guardar el mensaje de edición", Toast.LENGTH_SHORT).show()
                }
        }
        // BOTON admin para cerrar evento
        binding.btnCerrarEvento.setOnClickListener {
            db.collection("eventos").document(eventoId)
                .update(mapOf(
                    "estado" to "cerrado",
                    "mensajeCambio" to "El evento ha sido cerrado."
                ))
                .addOnSuccessListener {
                    Toast.makeText(this, "Evento cerrado", Toast.LENGTH_SHORT).show()
                    mensajeCambio("Evento cerrado correctamente")
                    estadoEvento = "cerrado"
                    verificarRolYConfigurarUI() // volver a actualizar UI
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cerrar evento", Toast.LENGTH_SHORT).show()
                }
        }
        // BOTON usuario para confirmar asistencia a evento
        binding.btnConfirmar.setOnClickListener {
            Toast.makeText(this, "Asistencia confirmada", Toast.LENGTH_SHORT).show()
            val eventoRef = db.collection("eventos").document(eventoId)

            eventoRef.update("confirmados", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                .addOnSuccessListener {
                    Toast.makeText(this, "Asistencia confirmada", Toast.LENGTH_SHORT).show()
                    binding.btnConfirmar.visibility = View.GONE
                    binding.btnCancelar.visibility = View.VISIBLE
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al confirmar asistencia", Toast.LENGTH_SHORT).show()
                }
        }
        // BOTON usuario para enviar reseña de evento confirmado
        binding.btnEnviarResena.setOnClickListener {
            val valor = binding.editTextResena.text.toString().toIntOrNull()

            if (valor != null && valor in 1..5) {
                val resena = hashMapOf(
                    "eventoId" to eventoId,
                    "usuarioId" to userId,
                    "puntuacion" to valor
                )
                db.collection("resenas").add(resena)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Reseña enviada", Toast.LENGTH_SHORT).show()
                        binding.editTextResena.setText("")
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Error al enviar reseña", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Ingresa un número del 1 al 5", Toast.LENGTH_SHORT).show()
            }
        }

        // BOTON usuario para cancelar asistencia a evento
        binding.btnCancelar.setOnClickListener {
            val eventoRef = db.collection("eventos").document(eventoId)

            eventoRef.update("confirmados", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                .addOnSuccessListener {
                    Toast.makeText(this, "Asistencia cancelada", Toast.LENGTH_SHORT).show()
                    binding.btnCancelar.visibility = View.GONE
                    binding.btnConfirmar.visibility = View.VISIBLE
                    binding.layoutFeedback.visibility = View.GONE
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al cancelar asistencia", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun verificarResenaExistente() {
        db.collection("resenas")
            .whereEqualTo("eventoId", eventoId)
            .whereEqualTo("usuarioId", userId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val resena = documents.documents[0]
                    val puntuacion = resena.getLong("puntuacion")
                    binding.layoutFeedback.visibility = View.VISIBLE
                    binding.editTextResena.visibility = View.GONE
                    binding.btnEnviarResena.visibility = View.GONE
                    binding.txtResenaExistente.visibility = View.VISIBLE
                    binding.txtResenaExistente.text = "Ya has evaluado este evento con un $puntuacion ⭐"
                } else {
                    // Mostrar campos para evaluar
                    binding.editTextResena.visibility = View.VISIBLE
                    binding.btnEnviarResena.visibility = View.VISIBLE
                    binding.txtResenaExistente.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar reseña", Toast.LENGTH_SHORT).show()
            }
    }

    private fun verificarConfirmacionEnEventoYResena() {
        db.collection("eventos").document(eventoId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val confirmados = document.get("confirmados") as? List<*>
                    if (confirmados != null && confirmados.contains(userId)) {
                        // Está confirmado, puede dejar reseña
                        verificarResenaExistente()
                    } else {
                        // No está confirmado, no puede dejar reseña
                        
                        binding.layoutFeedback.visibility = View.VISIBLE
                        binding.editTextResena.visibility = View.GONE
                        binding.btnEnviarResena.visibility = View.GONE
                        binding.txtResenaExistente.visibility = View.VISIBLE
                        binding.txtResenaExistente.text = "No participaste, no puedes dejar una reseña"
                        binding.txtResenaExistente.setTextColor(binding.root.context.getColor(R.color.rojo)) // color personalizado

                    }
                } else {
                    Toast.makeText(this, "Evento no encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al verificar asistencia", Toast.LENGTH_SHORT).show()
            }
    }
    private fun mensajeCambio(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
    }

}
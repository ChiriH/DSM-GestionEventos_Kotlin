package com.example.kotlinfirebase

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.example.kotlinfirebase.databinding.ActivityRegisterBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth


        // Configurar Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // registro con correo/contraseña
        binding.registrarBtn.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isEmpty()) {
                Snackbar.make(binding.root, "Ingresa tu correo electrónico", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.password.error = "La contraseña es obligatoria"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.email.error = "Correo no válido"
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid ?: return@addOnCompleteListener

                        // Aquí consultamos si es el primer usuario
                        val db = FirebaseFirestore.getInstance()
                        db.collection("usuarios").get()
                            .addOnSuccessListener { documentos ->
                                val rol = if (documentos.isEmpty) "admin" else "usuario"

                                val nuevoUsuario = mapOf(
                                    "email" to email,
                                    "rol" to rol
                                )

                                db.collection("usuarios").document(uid).set(nuevoUsuario)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Registro exitoso como $rol", Toast.LENGTH_SHORT).show()
                                        // Luego de guardar el usuario, se redirige a MainActivity
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(this, "Error al guardar el usuario", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error al verificar usuarios", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        binding.move.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }
        binding.googleBtn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }


    }
    public override fun onStart() {
        super.onStart()

        //Revisar si el usuario es valido y actualizar la UI
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
                Toast.makeText(this, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser ?: return@addOnCompleteListener
                    val uid = user.uid
                    val email = user.email ?: ""

                    val db = FirebaseFirestore.getInstance()
                    val userDocRef = db.collection("usuarios").document(uid)

                    userDocRef.get().addOnSuccessListener { document ->
                        if (!document.exists()) {
                            // Usuario nuevo, definir rol
                            db.collection("usuarios").get()
                                .addOnSuccessListener { documentos ->
                                    val rol = if (documentos.isEmpty) "admin" else "usuario"
                                    val nuevoUsuario = mapOf(
                                        "email" to email,
                                        "rol" to rol
                                    )
                                    userDocRef.set(nuevoUsuario)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "Bienvenido $rol", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this, MainActivity::class.java))
                                            finish()
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(this, "Error al guardar el usuario", Toast.LENGTH_SHORT).show()
                                        }
                                }
                        } else {
                            // Usuario ya existe, simplemente sigue
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Error al verificar usuario", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Firebase authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

}

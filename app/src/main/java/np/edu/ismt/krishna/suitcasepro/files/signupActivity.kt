package np.edu.ismt.krishna.suitcasepro.files

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import np.edu.ismt.krishna.suitcasepro.database.TestDatabase
import np.edu.ismt.krishna.suitcasepro.database.User
import np.edu.ismt.krishna.suitcasepro.databinding.ActivitySignupBinding

class signupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSignup.setOnClickListener {
            //TODO Validation of input fields

            //Check for valid email
            //Check password and confirm password matches
            //Check if full name have space and atleast two words
            val fullName = binding.name.text.toString().trim()
            val email = binding.signupEmail.text.toString().trim()
            val password = binding.signupPassword.text.toString().trim()
            val conformPassword = binding.signupConfirm.text.toString().trim()
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || conformPassword.isEmpty()) {
                Uiutiity.showToast(this, "Fill in the required fields")

            } else if (!fullName.contains(" ") && fullName.split(" ").size >= 2) {
                Uiutiity.showToast(this, "Name must be separated by space")
            } else if (fullName.split(" ").size < 2) {
                Uiutiity.showToast(this, "Name must contain two words")
            } else if (!isValidEmail(email)) {
                Uiutiity.showToast(this, "The email is not valid.")
            } else if (password != conformPassword) {
                Uiutiity.showToast(this, "Passwords must match")
            } else {
                val user = User(
                    fullName,
                    email,
                    password,
                    conformPassword
                )

                val testDatabase = TestDatabase
                    .getInstance(this)
                val userDao = testDatabase.getUserDao()
                Thread {
                    try {
                        val registeredUser = userDao.checkUserExist(email)
                        if (registeredUser == null) {
                            userDao.insertNewUser(user)
                            runOnUiThread {

                                Uiutiity.showToast(this, "New User Inserted...")
                                val login = Intent(this, LoginActivity::class.java)
                                clearInputFields()
                                startActivity(login)
                            }
                        } else {
                            runOnUiThread {
                                Uiutiity.showToast(this, "User already exist...")
                            }
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        runOnUiThread {
                            Uiutiity.showToast(this, "Couldn't Insert User. Try Again...")
                            clearInputFields()
                        }
                    }
                }.start()
            }
        }



        binding.btnSignin2.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.backArrow.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$"
        return email.matches(emailRegex.toRegex())
    }

    private fun clearInputFields() {
        binding.signupEmail.text.clear()
        binding.name.text.clear()
        binding.signupPassword.text.clear()
        binding.signupConfirm.text.clear()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
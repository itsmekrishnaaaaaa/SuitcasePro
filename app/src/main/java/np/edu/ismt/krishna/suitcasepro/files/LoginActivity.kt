package np.edu.ismt.krishna.suitcasepro.files

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import np.edu.ismt.krishna.suitcasepro.database.TestDatabase
import np.edu.ismt.krishna.suitcasepro.database.User
import np.edu.ismt.krishna.suitcasepro.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val tag = "LoginActivity"

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.btnSignIn.setOnClickListener {
            Log.i(tag, "Login Button Clicked...")

            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()

            Log.i(tag, "Email ::: ".plus(email))
            Log.i(tag, "Password ::: ".plus(password))

            if (email.isBlank()) {
                Toast.makeText(
                    this,
                    "Enter a valid email...",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (password.isBlank()) {
                Toast.makeText(
                    applicationContext,
                    "Enter a password...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //TODO Do remote authentication

                /**
                 * Local Authentication via db
                 */
                val testDatabase = TestDatabase.getInstance(applicationContext)
                val userDao = testDatabase.getUserDao()

                Thread {
                    try {
                        val userInDb = userDao.getSpecificUser(email, password)
                        if (userInDb == null) {
                            runOnUiThread {
                                showToast("Email or Password is incorrect")
                            }
                        } else {
                            runOnUiThread {
                                showToast("Logged In Successfully")
                            }
                            onSuccessfulLogin(userInDb)
                        }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        runOnUiThread {
                            showToast("Couldn't login. Please try again...")
                        }
                    }
                }.start()
            }
        }

        binding.btnSignup.setOnClickListener {
            val intent = Intent ( this , signupActivity::class.java)
            startActivity(intent)
            finish()

        }

        binding.arrowBack.setOnClickListener {
            val intent = Intent ( this , HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun onSuccessfulLogin(user: User) {
        //Writing to SharedPref
        Log.i(tag, "onsuccesfull login called")
//        val sharedPreferences = getSharedPreferences(
//            AppConstants.FILE_SHARED_PREF_LOGIN,
//            Context.MODE_PRIVATE
//        )
//        val sharedPrefEditor = sharedPreferences.edit()
//        sharedPrefEditor.putBoolean(
//            AppConstants.KEY_IS_LOGGED_IN,
//            true
//        )
//        sharedPrefEditor.apply()
        val sharedPreferences = getSharedPreferences(
            AppConstants.FILE_SHARED_PREF_LOGIN,
            Context.MODE_PRIVATE
        )
        val editor = sharedPreferences.edit()
        editor.putString("username", user.fullName)
        editor.putString("email", user.email)
        editor.apply()
        Log.d("ProfileEditActivity", "User email: ${user.email}")
        Log.d("ProfileEditActivity", "User fullName: ${user.fullName}")
        editor.putBoolean(AppConstants.KEY_IS_LOGGED_IN, true).apply()
        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra(AppConstants.KEY_LOGIN_DATA, user)
        Log.i( tag, "User: $user")
        intent.putExtra("userId", user.email)// Make sure 'user' is not null
        Log.i( tag, "User ID: ${user.id}")
        startActivity(intent)
        finish()
        Log.i(tag , "Leaving onSuccessfulLogin")
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}

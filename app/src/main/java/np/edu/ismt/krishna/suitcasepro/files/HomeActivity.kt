package np.edu.ismt.krishna.suitcasepro.files

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import np.edu.ismt.krishna.suitcasepro.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.btnlogIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()

        }

        binding.btnsignUp.setOnClickListener {
            val intent = Intent(this, signupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

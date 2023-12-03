package np.edu.ismt.krishna.suitcasepro.files

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import np.edu.ismt.krishna.suitcasepro.R
import np.edu.ismt.krishna.suitcasepro.database.TestDatabase
import np.edu.ismt.krishna.suitcasepro.database.UserDao
import np.edu.ismt.krishna.suitcasepro.databinding.ActivityDashboardBinding
import np.edu.ismt.krishna.suitcasepro.fragments.HomeFragment
import np.edu.ismt.krishna.suitcasepro.fragments.ItemsFragment
import np.edu.ismt.krishna.suitcasepro.fragments.ProfileFragment

class DashboardActivity : AppCompatActivity() {


    private lateinit var dashboardBinding: ActivityDashboardBinding
    private val homeFragment = HomeFragment()
    private val itemsFragment = ItemsFragment()
    private val profileFragment = ProfileFragment()
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dashboardBinding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(dashboardBinding.root)

        Log.d("DashboardActivity", "Initializing UserDao...")
        initUserDao()

        setUpViews()

        val userId = intent.getIntExtra(AppConstants.KEY_USER_ID, -1)
        Log.d("DashboardActivity", "Received UserId: $userId")
        lifecycleScope.launch {

            val username = fetchUsername(userId)
            Log.d("UsernameDebug", "Username: $username")
            // Create a bundle to pass the username to fragments
            val bundle = Bundle().apply {
                putString("username", username.toString())
            }

            // Pass the username to the ProfileFragment
            val profileFragment = ProfileFragment()
            profileFragment.arguments = bundle

            // Pass the username to the HomeFragment
            val homeFragment = HomeFragment()
            homeFragment.arguments = bundle

            // Replace the fragments in the fragment container
            supportFragmentManager.beginTransaction()
                .replace(R.id.fcv_dashboard, profileFragment)
                .replace(R.id.fcv_dashboard, homeFragment)
                .commit()
        }
    }


    private suspend fun fetchUsername(userId: Int): String? {
        val user = userDao.getUsernameById(userId)
        Log.d("DashboardActivity", "User: $user")
        return user?.fullName
    }

    private fun initUserDao() {
        Log.d("DashboardActivity", "Initializing user dao")
        val testDatabase = TestDatabase.getInstance(applicationContext)
        userDao = testDatabase.getUserDao()
    }

    private fun setUpViews() {
        setUpFragmentContainerView()
        setUpBottomNavigationView()
    }

    private fun setUpFragmentContainerView() {
        loadFragmentInFragmentContainerView(homeFragment)
    }



    private fun setUpBottomNavigationView() {
        dashboardBinding.bnvDashboard.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.menu_home -> {
                    loadFragmentInFragmentContainerView(homeFragment)
                    true
                }
                R.id.menu_add -> {
                    loadFragmentInFragmentContainerView(itemsFragment)
                    true
                }
                else -> {
                    loadFragmentInFragmentContainerView(profileFragment)
                    true
                }
            }
        }
    }




    private fun loadFragmentInFragmentContainerView(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fcv_dashboard, fragment)
            .commit()
    }

}
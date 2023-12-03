package np.edu.ismt.krishna.suitcasepro.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import np.edu.ismt.krishna.suitcasepro.Adaptar.HomeViewModel
import np.edu.ismt.krishna.suitcasepro.R
import np.edu.ismt.krishna.suitcasepro.databinding.FragmentProfileBinding
import np.edu.ismt.krishna.suitcasepro.files.AppConstants
import np.edu.ismt.krishna.suitcasepro.files.LoginActivity
import np.edu.ismt.krishna.suitcasepro.files.ProfileEditActivity


class ProfileFragment : Fragment() {
    private lateinit var profileBinding: FragmentProfileBinding
    private lateinit var viewModel: HomeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        profileBinding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]
        // Observe the username and update UI when it changes
        setUpViews()


        viewModel.username.observe(viewLifecycleOwner, Observer { username ->
            profileBinding.username.text = username
            Log.d("HomeFragment", "Username observed: $username")
        })
        return profileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences(
            AppConstants.FILE_SHARED_PREF_LOGIN,
            Context.MODE_PRIVATE
        )
        val username = sharedPreferences.getString("username", null)
        Log.d("HomeFragment", "Retrieved username from SharedPreferences: $username")

        val imageUriPath = sharedPreferences.getString("imageUriPath", null)
        val profileImageView = view.findViewById<ImageView>(R.id.profile_img)

        if (!imageUriPath.isNullOrEmpty()) {
            Glide.with(this)
                .load(Uri.parse(imageUriPath))
                .circleCrop()
                .into(profileImageView)
        }

        // Update the ViewModel with the username
        viewModel.setUsername(username)
    }


    private fun setUpViews() {
        setUpButtons()
    }

    private fun setUpButtons() {
        profileBinding.btnLogOut.setOnClickListener {
            showLogoutConfirmationDialog()
        }
        profileBinding.btnProfile.setOnClickListener {
            val intent= Intent(requireActivity(), ProfileEditActivity::class.java)
            Log.d("HomeFragment", "starting editactivity")
            startActivity(intent)
        }
    }
    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                logoutAndNavigateToLogin()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    private fun logoutAndNavigateToLogin() {
        viewModel.setUsername(null)
        viewModel.setImageUri(null)

        val sharedPreferences =
            requireActivity().getSharedPreferences(AppConstants.FILE_SHARED_PREF_LOGIN, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("username")
        editor.apply()

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()


        val intent = Intent(requireActivity(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
    companion object {
        @JvmStatic
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

}
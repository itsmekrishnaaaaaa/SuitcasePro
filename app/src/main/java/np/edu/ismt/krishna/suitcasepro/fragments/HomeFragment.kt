package np.edu.ismt.krishna.suitcasepro.fragments


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import np.edu.ismt.krishna.suitcasepro.Adaptar.HomeViewModel
import np.edu.ismt.krishna.suitcasepro.Adaptar.MyAdapter
import np.edu.ismt.krishna.suitcasepro.R
import np.edu.ismt.krishna.suitcasepro.database.Product
import np.edu.ismt.krishna.suitcasepro.database.TestDatabase
import np.edu.ismt.krishna.suitcasepro.databinding.FragmentHomeBinding
import np.edu.ismt.krishna.suitcasepro.files.AppConstants
import np.edu.ismt.krishna.suitcasepro.files.ItemsDetailViewActivity

class HomeFragment : Fragment(), MyAdapter.ProductAdapterListener {


    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var productRecyclerAdapter: MyAdapter


    private val startItemDetailsActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == ItemsDetailViewActivity.RESULT_CODE_REFRESH) {
            setUpRecyclerView()
        } else {
            //Do Nothing
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        val products = arguments?.getParcelableArrayList<Product>(ARG_ALL_ITEMS)

        // Observe the username and update UI when it changes
        viewModel.username.observe(viewLifecycleOwner, Observer { username ->
            binding.username1.text = ("Welcome,\n ").plus(username)
            Log.d("HomeFragment", "Username observed: $username")
        })
        setUpViews()
        return binding.root

    }

    private fun setUpViews() {
        setUpRedirectButton()
        setUpRecyclerView()
    }

    private fun setUpRedirectButton() {
        binding.seeall.setOnClickListener {
            val shopFragment = ItemsFragment()
            val transaction: FragmentTransaction = requireFragmentManager().beginTransaction()
            transaction.replace(R.id.recycleView, shopFragment)
            transaction.commit()

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("HomeFragment", "onViewCreated called")

        // Retrieve the username from SharedPreferences
        val sharedPreferences = requireActivity().getSharedPreferences(
            AppConstants.FILE_SHARED_PREF_LOGIN,
            Context.MODE_PRIVATE
        )
        val username = sharedPreferences.getString("username", null)
        Log.d("HomeFragment", "Retrieved username from SharedPreferences: $username")
        // Update the ViewModel with the username
        viewModel.setUsername(username)
    }

    override fun setUpRecyclerView() {
        //TODO fetch data from source (remote server)
        val testDatabase = TestDatabase.getInstance(requireActivity().applicationContext)
        val productDao = testDatabase.getProductDao()

        Thread {
            try {
                val products = productDao.getAllProducts()
                if (products.isEmpty()) {
                    requireActivity().runOnUiThread {
                        populateRecyclerView(products)
                        Toast.makeText(requireActivity(), "No items added", Toast.LENGTH_SHORT)
                            .show()
                    }
                } else {
                    requireActivity().runOnUiThread {
                        populateRecyclerView(products)
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                requireActivity().runOnUiThread {
                    Toast.makeText(requireActivity(), "Couldn't load items", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }.start()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Release the binding when the fragment's view is destroyed
    }


    private fun populateRecyclerView(products: List<Product>) {
        productRecyclerAdapter = MyAdapter(
            products,
            this,
            requireActivity().applicationContext
        )
        binding.rv2.adapter = productRecyclerAdapter
        binding.rv2.layoutManager = LinearLayoutManager(requireActivity())
    }

    companion object {
        @JvmStatic
        fun newInstance(purchasedItems: List<Product>, allItems: List<Product>) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_PURCHASED_ITEMS, ArrayList(purchasedItems))
                    putParcelableArrayList(ARG_ALL_ITEMS, ArrayList(allItems))
                }
            }
        private const val ARG_PURCHASED_ITEMS = "arg_purchased_items"
        private const val ARG_ALL_ITEMS = "arg_all_items"
    }

    override fun onItemClicked(product: Product, position: Int) {
        val intent = Intent(requireActivity(), ItemsDetailViewActivity::class.java).apply {
            this.putExtra(AppConstants.KEY_PRODUCT, product)
            this.putExtra(AppConstants.KEY_PRODUCT_POSITION, position)

        }
        startItemDetailsActivity.launch(intent)
    }
}



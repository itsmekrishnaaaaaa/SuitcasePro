package np.edu.ismt.krishna.suitcasepro.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import np.edu.ismt.krishna.suitcasepro.Adaptar.MyAdapter
import np.edu.ismt.krishna.suitcasepro.database.Product
import np.edu.ismt.krishna.suitcasepro.database.TestDatabase
import np.edu.ismt.krishna.suitcasepro.databinding.FragmentItemsBinding
import np.edu.ismt.krishna.suitcasepro.files.AddorUpdateActivity
import np.edu.ismt.krishna.suitcasepro.files.AppConstants
import np.edu.ismt.krishna.suitcasepro.files.ItemsDetailViewActivity
import np.edu.ismt.krishna.suitcasepro.files.Uiutiity


class ItemsFragment : Fragment(), MyAdapter.ProductAdapterListener {
    private lateinit var shopBinding: FragmentItemsBinding
    private lateinit var productRecyclerAdapter: MyAdapter

    private val startAddItemActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == AddorUpdateActivity.RESULT_CODE_COMPLETE) {
            //TODO fetch data from db again and populate
            setUpRecyclerView()
        } else {
            // TODO do nothing
            Uiutiity.showToast(requireActivity(), "Add Item Cancelled...")
        }
    }

    private val startDetailViewActivity = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == ItemsDetailViewActivity.RESULT_CODE_REFRESH) {
            setUpRecyclerView()
        } else {
            //Do Nothing
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        shopBinding = FragmentItemsBinding.inflate(inflater, container, false)
        setUpViews()
        return shopBinding.root
    }

    companion object {
        @JvmStatic
        fun newInstance() = ItemsFragment()
    }

    private fun setUpViews() {
        setUpFloatingActionButton()
        setUpRecyclerView()
    }

    private fun setUpFloatingActionButton() {
        shopBinding.fabAdd.setOnClickListener {
            val intent = Intent(requireActivity(), AddorUpdateActivity::class.java)
            startAddItemActivity.launch(intent)
        }
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
                        Uiutiity.showToast(requireActivity(), "No Items Added...")
                    }
                } else {
                    requireActivity().runOnUiThread {
                        populateRecyclerView(products)
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                requireActivity().runOnUiThread {
                    Uiutiity.showToast(requireActivity(), "Couldn't load items....")
                }
            }
        }.start()
    }

    private fun populateRecyclerView(products: List<Product>) {
        productRecyclerAdapter = MyAdapter(
            products,
            this,
            requireActivity().applicationContext
        )
        shopBinding.recycleView.adapter = productRecyclerAdapter
        shopBinding.recycleView.layoutManager = LinearLayoutManager(requireActivity())
    }

    override fun onItemClicked(product: Product, position: Int) {
        val intent = Intent(requireActivity(), ItemsDetailViewActivity::class.java).apply {
            this.putExtra(AppConstants.KEY_PRODUCT, product)
            this.putExtra(AppConstants.KEY_PRODUCT_POSITION, position)
        }
        startDetailViewActivity.launch(intent)
    }
}
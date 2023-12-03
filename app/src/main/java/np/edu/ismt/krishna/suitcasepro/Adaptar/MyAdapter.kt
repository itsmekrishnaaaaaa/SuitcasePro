package np.edu.ismt.krishna.suitcasepro.Adaptar

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import np.edu.ismt.krishna.suitcasepro.R
import np.edu.ismt.krishna.suitcasepro.database.Product
import np.edu.ismt.krishna.suitcasepro.files.BitmapScalar
import java.io.IOException

class MyAdapter(
    private val products: List<Product>,
    private val listener: ProductAdapterListener,
    private val context: Context
) : RecyclerView.Adapter<MyAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var itemRootLayout: ConstraintLayout
        var itemImage: ImageView
        var itemTitle: TextView
        var itemDescription: TextView
        var itemLocation: TextView
        var itemPrice: TextView
        var checkBox: CheckBox

        init {
            itemRootLayout = view.findViewById(R.id.item_root_layout)
            itemImage = view.findViewById(R.id.iv_item_image)
            itemTitle = view.findViewById(R.id.tv_item_title)
            itemDescription = view.findViewById(R.id.tv_item_description)
            itemLocation = view.findViewById(R.id.tv_item_location)
            itemPrice = view.findViewById(R.id.tv_item_price)
            checkBox = view.findViewById(R.id.checkBox)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_product, parent, false)

        return ProductViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  products.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.itemImage.post {
            var bitmap: Bitmap?
            try {
                bitmap = MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    Uri.parse(products[position].image)
                )
                bitmap = BitmapScalar.stretchToFill(
                    bitmap,
                    holder.itemImage.width,
                    holder.itemImage.height
                )
                holder.itemImage.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        holder.itemTitle.text = products[position].title
        holder.itemDescription.text = products[position].description
        holder.itemLocation.text = products[position].location
        holder.itemPrice.text = products[position].price
        holder.checkBox.tag = position
        holder.checkBox.isChecked = getCheckboxState(products[position].id)

        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            saveCheckboxState(products[position].id, isChecked)
            if (isChecked) {
                showToast("Item marked as purchased: ${products[position].title}")
            }
        }

        holder.itemRootLayout.setOnClickListener {
            listener.onItemClicked(products[position], position)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveCheckboxState(productId: Int, isChecked: Boolean) {
        val sharedPreferences = context.getSharedPreferences(
            "checkbox_states",
            Context.MODE_PRIVATE
        )
        sharedPreferences.edit().putBoolean(productId.toString(), isChecked).apply()
    }

    private fun getCheckboxState(productId: Int): Boolean {
        val sharedPreferences = context.getSharedPreferences(
            "checkbox_states",
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getBoolean(productId.toString(), false)
    }

    interface ProductAdapterListener {
        fun onItemClicked(product: Product, position: Int)
        fun setUpRecyclerView()
    }
}
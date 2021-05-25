package com.shervarly.lena.goalplanner

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import android.widget.*
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.j256.ormlite.dao.Dao
import dbmodule.DatabaseHelper
import dbmodule.ProductDTO
import java.sql.SQLException
import java.util.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ProductsSettingActivity : AppCompatActivity() {
    private lateinit var purchasedProductsListView: ListView
    private lateinit var categoryTitle: String
    private lateinit var productDAO: Dao<ProductDTO, Int>
    private var productsToBuy: List<ProductDTO> = ArrayList()
    private var purchasedProducts: List<ProductDTO> = ArrayList()
    private lateinit var databaseHelper: DatabaseHelper
    private lateinit var touchHelper: ItemTouchHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products_setting)

        categoryTitle = intent.getStringExtra(CATEGORY_TITLE) ?: ""

        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.title = categoryTitle
        }
        val actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        databaseHelper = getDBHelper()
        productDAO = databaseHelper.getProductsDao()

        purchasedProductsListView = findViewById(R.id.purchased_products_list)
        updateUI()
    }

    fun addProduct(view: View){
        val productsTextView: EditText = (view.parent as View).findViewById(R.id.new_product)
        val productName = productsTextView.text.toString().capitalize(Locale.ROOT)

        val purchased = false
        val newProduct = ProductDTO(0, categoryTitle, productName, purchased, productsToBuy.size )

        productDAO.createIfNotExists(newProduct)
        updateUI()
        productsTextView.text.clear()
    }

    fun resetBasket(view: View) {
        val purchasedProducts = databaseHelper.getProductsByCategoryAndPurchasedStatus(categoryTitle, true)
        purchasedProducts.forEach {
            product -> product.purchased = false
            productDAO.update(product)
        }
        updateUI()
    }

    private fun getDBHelper(): DatabaseHelper{
        return OpenHelperManager.getHelper(this, DatabaseHelper::class.java)
    }

    fun updatePurchasedStatus(product: ProductDTO){
        try {
            val productToUpdate = databaseHelper.getProductsById(product.productId)
            productToUpdate.purchased = !productToUpdate.purchased
            productDAO.update(productToUpdate)
        } catch (e: SQLException){
            e.printStackTrace()
        }
        updateUI()
    }

    private fun updateUI(){
        productsToBuy = databaseHelper.getProductsByCategoryAndPurchasedStatus(categoryTitle, false).sortedBy { prod -> prod.order }
        purchasedProducts = databaseHelper.getProductsByCategoryAndPurchasedStatus(categoryTitle, true)
        purchasedProductsListView.adapter = PurchasedCustomAdapter(this, R.layout.item_purchased, purchasedProducts)

        val productsRecyclerView: RecyclerView = findViewById(R.id.recycler_view_products)
        val adapter = ProductsAdapter( productsToBuy)
        val callback: ItemTouchHelper.Callback = ItemMoveCallbackListener(adapter)
        touchHelper = ItemTouchHelper(callback)
        touchHelper.attachToRecyclerView(productsRecyclerView)
        productsRecyclerView.adapter = adapter
        productsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    fun removeProduct(product: ProductDTO){
        try {
            productDAO.delete(product)
        } catch (e: SQLException){
            e.printStackTrace()
        }
        updateUI()
    }

    inner class ProductsAdapter(private val products: List<ProductDTO>): IAdapter,
        RecyclerView.Adapter<ProductsSettingActivity.ProductsAdapter.ProductViewHolder>(), ItemMoveCallbackListener.Listener {
        inner class ProductViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            private val productNameField = itemView.findViewById(R.id.product_title) as TextView
            private val productPurchasedCheckBox = itemView.findViewById(R.id.product_done) as Button

            fun bind(productDTO: ProductDTO) {
                productPurchasedCheckBox.setOnClickListener {
                    updatePurchasedStatus(productDTO)
                }
                productNameField.text = productDTO.productName

                productNameField.setOnDragListener { _, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        touchHelper.startDrag(this)
                    }
                    return@setOnDragListener true
                }
            }
        }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):
                    ProductsSettingActivity.ProductsAdapter.ProductViewHolder {
                val view =  LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
                return ProductViewHolder(view)
            }

            override fun onBindViewHolder(holder: ProductsSettingActivity.ProductsAdapter.ProductViewHolder, position: Int) {
                val product = products[position]
                holder.bind(product)
            }

            override fun getItemCount() = products.size

            override fun onRowMoved(fromPosition: Int, toPosition: Int) {
                if (fromPosition < toPosition) {
                    for (i in fromPosition until toPosition) {
                        Collections.swap(products, i, i + 1)
                    }
                } else {
                    for (i in fromPosition downTo toPosition + 1) {
                        Collections.swap(products, i, i - 1)
                    }
                }
                val currentOrder = products[fromPosition].order
                products[fromPosition].order = products[toPosition].order
                products[toPosition].order = currentOrder
                productDAO.update(products[fromPosition])
                productDAO.update(products[toPosition])
                notifyItemMoved(fromPosition, toPosition)
            }

            override fun onRowSelected(itemViewHolder: CategoryActivity.CategoryViewAdapter.CategoryViewHolder) {
            }

            override fun onRowClear(itemViewHolder: CategoryActivity.CategoryViewAdapter.CategoryViewHolder) {
            }
    }

    inner class PurchasedCustomAdapter(context: Context, resource: Int, private val purchasedProducts: List<ProductDTO>): ArrayAdapter<ProductDTO>(context, resource, purchasedProducts) {

        override fun getView(position: Int, viewParam: View?, parent: ViewGroup): View {
            var convertView = viewParam
            if (convertView == null)
                convertView = LayoutInflater.from(context).inflate(R.layout.item_purchased, parent, false)

            val productNameField = convertView?.findViewById(R.id.purchased_title) as TextView
            productNameField.text = purchasedProducts[position].productName
            productNameField.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            productNameField.setOnClickListener {
                updatePurchasedStatus(purchasedProducts[position])
            }

            val deleteButton = convertView.findViewById(R.id.delete_item) as Button
            deleteButton.setOnClickListener {
                val productToDelete = purchasedProducts[position]
                removeProduct(productToDelete)
            }

            return convertView
        }
    }
}
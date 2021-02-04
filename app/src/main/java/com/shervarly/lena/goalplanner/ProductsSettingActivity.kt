package com.shervarly.lena.goalplanner

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.Toolbar
import android.support.v7.widget.ShareActionProvider
import android.view.*
import android.widget.*
import com.j256.ormlite.android.apptools.OpenHelperManager
import com.j256.ormlite.dao.Dao
import dbmodule.DatabaseHelper
import dbmodule.ProductDTO
import java.sql.SQLException
import java.util.*
import android.widget.TextView
import android.widget.ArrayAdapter


class ProductsSettingActivity : AppCompatActivity() {
    private lateinit var productsListView: ListView
    private lateinit var purchasedPoductsListView: ListView
    private lateinit var categoryTitle: String
    private lateinit var productDAO: Dao<ProductDTO, Int>
    private var allProducts: List<ProductDTO> = ArrayList()
    private var purchasedProducts: List<ProductDTO> = ArrayList()
    private lateinit var databaseHelper: DatabaseHelper
    lateinit var shareActionProvider: ShareActionProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products_setting)

        categoryTitle = intent.getStringExtra(CATEGORY_TITLE)
        val textForMyTitle = "$categoryTitle"

        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            supportActionBar?.title = textForMyTitle
        }
        var actionBar: ActionBar? = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        databaseHelper = getDBHelper()
        productDAO = databaseHelper.getProductsDao()

        productsListView = findViewById(R.id.products_list)
        purchasedPoductsListView = findViewById(R.id.purchased_products_list)
        updateUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val shareButton: MenuItem = menu.findItem(R.id.action_share)
        shareActionProvider = MenuItemCompat.getActionProvider(shareButton) as ShareActionProvider
        val message = StringBuilder()
        message.append("Just look! My products for the next  are: \n")
        productDAO.queryForAll()
                .map { productDTO -> productDTO.productName }
                .forEach{productName -> message.append(productName + "\n")}
        setShareActionIntent(message.toString())
        return super.onCreateOptionsMenu(menu)
    }

    private fun setShareActionIntent(message: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, message)
        shareActionProvider.setShareIntent(intent)
    }

    fun addProduct(view: View){
        val productsTextView: EditText = (view.parent as View).findViewById(R.id.new_product)
        val productName = productsTextView.text.toString().capitalize()

        val purchased = false
        val newProduct = ProductDTO(0, categoryTitle, productName, purchased )

        productDAO.createIfNotExists(newProduct)
        updateUI()
        productsTextView.text.clear()
    }

    fun resetBasket(view: View) {
        var purchasedProducts = databaseHelper.getProductsByCategoryAndPurchasedStatus(categoryTitle, true)
        purchasedProducts.forEach {
            product -> product.purchased = false
            productDAO.update(product)
        }
        updateUI()
    }

    private fun getDBHelper(): DatabaseHelper{
        return OpenHelperManager.getHelper(this, DatabaseHelper::class.java)
    }

    fun updateProduct(product: ProductDTO){
        try {
            var productToUpdate = databaseHelper.getProductsById(product.productId)
            productToUpdate.purchased = !productToUpdate.purchased
            productDAO.update(productToUpdate)
        } catch (e: SQLException){
            e.printStackTrace()
        }
        updateUI()
    }

    private fun updateUI(){
        allProducts = databaseHelper.getProductsByCategoryAndPurchasedStatus(categoryTitle, false)
        purchasedProducts = databaseHelper.getProductsByCategoryAndPurchasedStatus(categoryTitle, true)
        productsListView.adapter = CustomAdapter(this, R.layout.item, allProducts)
        purchasedPoductsListView.adapter = PurchasedCustomAdapter(this, R.layout.item_purchased, purchasedProducts)
    }

    fun removeProduct(position: Int){
        try {
            productDAO.delete(productDAO.queryForAll()[position])
        } catch (e: SQLException){
            e.printStackTrace()
        }
        updateUI()
    }

    inner class CustomAdapter(context: Context, resource: Int, private val productsToBuy: List<ProductDTO>): ArrayAdapter<ProductDTO>(context, resource, productsToBuy) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            if (convertView == null)
                convertView = LayoutInflater.from(context).inflate(R.layout.item, parent, false)

            val productNameField = convertView?.findViewById(R.id.product_title) as TextView
            val productPurchasedCheckBox = convertView?.findViewById(R.id.product_purchased_status) as CheckBox

            productPurchasedCheckBox.setOnClickListener { view ->
                productPurchasedCheckBox.isChecked = true
                updateProduct(productsToBuy[position])
            }
            productNameField.text = productsToBuy[position].productName

            return convertView
        }
    }

    inner class PurchasedCustomAdapter(context: Context, resource: Int, private val purchasedProducts: List<ProductDTO>): ArrayAdapter<ProductDTO>(context, resource, purchasedProducts) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            if (convertView == null)
                convertView = LayoutInflater.from(context).inflate(R.layout.item_purchased, parent, false)

            val productNameField = convertView?.findViewById(R.id.purchased_title) as TextView
            productNameField.text = purchasedProducts[position].productName
            productNameField.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            productNameField.setOnClickListener {view ->
                updateProduct(purchasedProducts[position])
            }

            val deleteButton = convertView?.findViewById(R.id.delete_item) as Button
            deleteButton.setOnClickListener {view ->
                removeProduct(position)
            }

            return convertView
        }
    }
}
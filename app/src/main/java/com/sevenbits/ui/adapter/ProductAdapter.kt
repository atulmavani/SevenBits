package com.sevenbits.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sevenbits.R
import com.sevenbits.model.Product
import java.util.ArrayList
import android.content.Context
import androidx.annotation.Nullable
import com.bumptech.glide.Glide
import android.util.SparseBooleanArray
import android.widget.*


class ProductAdapter(context: Context, productarraylist: ArrayList<Product>) :
    ArrayAdapter<Product>(context, 0, productarraylist) {
    var ischecked = false
    var productlist = productarraylist
    override fun getView(position: Int, @Nullable convertView: View?, parent: ViewGroup): View {
        var listitemView = convertView
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(context).inflate(R.layout.row_layout_product_list, parent, false)
        }
        val product: Product = productlist[position]
        val chk_select = listitemView!!.findViewById<CheckBox>(R.id.chk_select)
        val img_product = listitemView.findViewById<ImageView>(R.id.img_product)
        val txt_product_name = listitemView.findViewById<TextView>(R.id.txt_product_name)
        val txt_product_oldprice = listitemView.findViewById<TextView>(R.id.txt_product_oldprice)
        val txt_product_newprice = listitemView.findViewById<TextView>(R.id.txt_product_newprice)
        if(ischecked){
            chk_select.visibility = View.VISIBLE
            chk_select.isChecked = product.isselected
        }else{
            chk_select.visibility = View.GONE
        }
        txt_product_newprice.text = context.getString(R.string.currency_symbol,product.newprice.toString())
        txt_product_oldprice.text = context.getString(R.string.currency_symbol,product.oldprice.toString())
        txt_product_name.text = product.name
        Glide.with(context)
            .load(product.image)
            .into(img_product)
        chk_select.setTag(position)
        chk_select.setOnClickListener{
            var rb  = it as CheckBox
            var clickpos = rb.getTag() as Int
            productlist[clickpos].isselected = rb.isChecked
        }
        return listitemView
    }

    fun update(){
        ischecked = true
        notifyDataSetChanged()
    }
}
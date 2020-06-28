package com.example.searchgithub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.searchgithub.R
import com.example.searchgithub.model.Item
import kotlinx.android.synthetic.main.user_list.view.*

class AdapterUsers(var users: MutableList<Item>, var usersFilter: MutableList<Item>) : RecyclerView.Adapter<AdapterUsers.ViewHolder>(), Filterable {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.user_list, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        users[position].let { holder.bindData(it) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindData(data: Item) {
            Glide.with(itemView.context).load(data.avatarUrl).into(itemView.imagePhoto)
            itemView.textFullName.text = data.login
        }
    }

    fun deleteData() {
        users.clear()
        notifyDataSetChanged()
    }

    fun addAll(list: List<Item>) {
        users.addAll(list)
        usersFilter.addAll(list)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString()

                usersFilter = if (charString.isEmpty()) {
                    users
                } else {
                    val filterList: MutableList<Item> = arrayListOf()
                    for (row in users) {
                        if (row.login!!.toLowerCase().contains(charString.toLowerCase())) {
                            filterList.add(row)
                        }
                    }

                    filterList
                }

                val filterResult = FilterResults()
                filterResult.values = usersFilter
                return filterResult
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                usersFilter = results.values as ArrayList<Item>
                notifyDataSetChanged()
            }
        }
    }
}
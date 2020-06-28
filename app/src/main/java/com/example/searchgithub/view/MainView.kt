package com.example.searchgithub.view

import android.os.Handler
import com.example.searchgithub.model.Item

interface MainView {

    fun showLoading() {
        return
    }

    fun hideLoading() {
        return
    }

    fun loading(b: Boolean) {
        return
    }

    fun showData(data: List<Item>, q: String) {
        return
    }

    fun clearListData() {
        return
    }

    fun searchData(q: String, p: Int, pP: Int) {
        return
    }

    fun handlerSearchData(handler: Handler, filtered: Boolean) {
        return
    }

    fun countSearch() {
        return
    }

    fun handleNotFound(c: String, m: String) {
        return
    }
}
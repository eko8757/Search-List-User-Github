package com.example.searchgithub

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.searchgithub.adapter.AdapterUsers
import com.example.searchgithub.model.Item
import com.example.searchgithub.service.BaseApi
import com.example.searchgithub.utils.CustomEditText
import com.example.searchgithub.utils.PaginationListener
import com.example.searchgithub.utils.gone
import com.example.searchgithub.utils.visible
import com.example.searchgithub.view.MainView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainView {

    private val user by lazy { BaseApi.create() }
    private val delay: Long = 1500
    private val perPage: Int = 100
    private var adapter: AdapterUsers? = null
    private var totalPage: Int = 0
    private var currentPage: Int = 1
    private var nextPage: Boolean = false
    private var isSearch: Boolean = false
    private var queryCurrent: String = ""
    private var querySearched: Int = 0
    private var lastTextChanged: Long = 0
    var disposable: Disposable? = null
    private var userLinearLayout = LinearLayoutManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        recyclerView.layoutManager = userLinearLayout
        setListener()
    }
    override fun showLoading() {
        loadingLayout.visible()
    }

    override fun hideLoading() {
        loadingLayout.gone()
    }

    private fun setListener() {
        recyclerView.addOnScrollListener(object : PaginationListener(userLinearLayout) {
            override fun onLoadMore(current_page: Int) {
                if (nextPage) {
                    currentPage++
                    searchData(queryCurrent, currentPage, perPage)
                }
            }

        })

        textSearch.onChange()
    }

    override fun showData(data: List<Item>, q: String) {
        errorLayout.gone()
        if (adapter == null) {
            adapter = AdapterUsers(data.toMutableList(), data.toMutableList())
            recyclerView.adapter = adapter
        } else {
            if (queryCurrent != q || (data.count() == 1 && currentPage == 1))
                adapter!!.deleteData()

            adapter!!.addAll(data)
            adapter!!.notifyItemRangeChanged(0, adapter!!.itemCount)
        }
    }

    override fun clearListData() {
        if (adapter != null) {
            errorLayout.gone()
            queryCurrent = ""
            totalPage = 0
            currentPage = 1
            adapter!!.deleteData()
            adapter!!.notifyItemRangeChanged(0, 0)
        }
    }

    override fun searchData(q: String, p: Int, pP: Int) {
        textSearch.isEnabled = true
        if (!isSearch) {
            loading(true)
            disposable = user.getSearch(q, p, pP)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { result ->
                        loading(false)
                        val count = result.items.count()
                        if (count > 0) {
                            totalPage += count
                            showData(result.items, q)
                            nextPage = (result.totalCount!! > totalPage && result.totalCount > perPage)

                        } else {
                            clearListData()
                            handleNotFound(
                                getString(R.string.text_404),
                                getString(R.string.text_data_is_not_found)
                            )
                        }
                    },
                    { error ->
                        loading(false)
                        clearListData()
                        handleNotFound(
                            getString(R.string.text_403),
                            getString(R.string.text_api_rate_is_limit)
                        )

                        Handler().postDelayed(
                            { searchData(textSearch.text.toString(), currentPage, perPage) },
                            60000
                        )

                        countSearch()
                    }
                )

            queryCurrent = q
        }
    }

    private val handlerSearch = Runnable {
        if (System.currentTimeMillis() > lastTextChanged + delay - 500 && textSearch.text.toString()
                .isNotEmpty()
        ) {
            clearListData()
            searchData(textSearch.text.toString(), currentPage, perPage)
        }
    }

    private fun CustomEditText.onChange() {
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty())
                    clearListData()
            }

            override fun afterTextChanged(s: Editable) {
                if (s.isNotEmpty()) {
                    if (querySearched > textSearch.text.toString().length)
                        handlerSearchData(handler, false)
                    else {
                        if (adapter != null && totalPage > 1) {
                            if (!nextPage)
                                adapter?.filter?.filter(textSearch.text.toString())
                            else
                                handlerSearchData(handler, true)
                        } else {
                            handlerSearchData(handler, false)
                        }
                    }
                } else {
                    clearListData()
                }

                querySearched = textSearch.text.toString().length
            }
        })
    }

    override fun handlerSearchData(handler: Handler, filtered: Boolean) {
        if (adapter != null && adapter?.itemCount!! > 0 && filtered) {
            adapter?.filter?.filter(textSearch.text.toString())
            if (adapter?.itemCount!! == 0) {
                clearListData()
                handleNotFound(
                    getString(R.string.text_404),
                    getString(R.string.text_data_is_not_found)
                )
            }
        } else {
            lastTextChanged = System.currentTimeMillis()
            handler.postDelayed(handlerSearch, delay)
        }
    }

    override fun countSearch() {
        textSearch.isEnabled = false
        Handler().postDelayed({
            val count = Integer.parseInt(textCode.text.toString()) - 1
            textCode.text = count.toString()
            if (count > 0)
                countSearch()

        }, 1000)
    }

    override fun loading(b: Boolean) {
        errorLayout.gone()
        isSearch = b
        if (b) {
            showLoading()
        } else {
            hideLoading()
        }
    }

    override fun handleNotFound(c: String, m: String) {
        errorLayout.visible()
        textCode.text = c
        textDescription.text = m
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }
}
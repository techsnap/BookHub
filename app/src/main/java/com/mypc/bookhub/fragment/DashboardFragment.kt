package com.mypc.bookhub.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.mypc.bookhub.R
import com.mypc.bookhub.adapter.DashboardAdapter
import com.mypc.bookhub.model.Book
import com.mypc.bookhub.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap

class DashboardFragment : Fragment() {

    // Initialize variables
    var bookInfoList = arrayListOf<Book>()

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: LinearLayoutManager
    lateinit var adapter: DashboardAdapter

    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar
//
//    var ratingComparator = Comparator<Book> { book1, book2 ->
//        // If ratings are equal, then sort by names
//        if (book1.bookRating.compareTo(book2.bookRating, true) == 0) {
//            book1.bookName.compareTo(book2.bookName, true)
//        } else {
//            book1.bookRating.compareTo(book2.bookRating, true)
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Display Sort action button
        setHasOptionsMenu(true)

        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)
        progressLayout.visibility = View.VISIBLE

        // Initialize recyclerView, LayoutManager
        recyclerView = view.findViewById(R.id.recyclerDashboard)
        layoutManager = LinearLayoutManager(activity)

        // Get book data
        getBookData()

        return view
    }

    // Code for getting book data from internet
    fun getBookData() {
        val queue = Volley.newRequestQueue(activity as Context)
        val url = "https://www.googleapis.com/books/v1/volumes?q=the"

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
                // Response handling
                try {
                    // MAIN Code
                    progressLayout.visibility = View.GONE
                    responseListener(it)
                } catch (e: JSONException) {
                    if (activity != null) {
                        Toast.makeText(activity as Context, "Unexpected response error", Toast.LENGTH_SHORT).show()
                    }

                }

            }, Response.ErrorListener {
                // Error handling
                if (activity != null) {
                    Toast.makeText(activity as Context, "Volley error occured!", Toast.LENGTH_SHORT).show()
                }
            }) //{
//                override fun getHeaders(): MutableMap<String, String> {
//                    val headers = HashMap<String, String>()
//                    headers["Content-type"] = "application/json"
////                    headers["token"] = "d44c4ab299d905"
//                    return headers
//                }
//            }
            queue.add(jsonObjectRequest)

        } else {
            var dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Not connected to Internet")

            // Open Settings Button
            dialog.setPositiveButton("Open Settings") {text, listener ->
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                activity?.finish()
            }
            // Exit the app
            dialog.setNegativeButton("Exit") {text, listener ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create().show()
        }
    }

    // Response listener code for jsonObjectRequest
    fun responseListener(it: JSONObject) {
        val items = it.getJSONArray("items")

        // For each object in JSON array
        for (i in 0 until items.length()) {
            // Store book data
            val bookJsonObject = items.getJSONObject(i)
            val bookInfo = bookJsonObject.getJSONObject("volumeInfo")
            val saleInfo = bookJsonObject.getJSONObject("saleInfo")

            val authorsArray = bookInfo.getJSONArray("authors")
            var authors = ""
            for (j in 0 until authorsArray.length()) {
                authors = authorsArray.getString(j) + "," + authors
            }

            // Handle retailPrice issue
            var retailPrice: String
            if (saleInfo.getString("saleability") == "FOR_SALE") {
                retailPrice = saleInfo.getJSONObject("retailPrice").getString("currencyCode") + " " +
                        saleInfo.getJSONObject("retailPrice").getDouble("amount").toString()
            } else {
                retailPrice = "NA"
            }

            val bookObject = Book(
                bookJsonObject.getString("id"),
                bookInfo.getString("title"),
                authors,
                bookInfo.getString("publishedDate"),
                bookInfo.getInt("pageCount").toString() + " Pages",
                retailPrice,
                bookInfo.getJSONObject("imageLinks").getString("smallThumbnail")
            )
            bookInfoList.add(bookObject)
        }

        // Set adapter and layoutManager
        adapter = DashboardAdapter(activity as Context, bookInfoList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = layoutManager

        /*// Add divider line
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                recyclerView.context,
                layoutManager.orientation
            )
        )*/
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater?.inflate(R.menu.menu_dashboard, menu)
    }

    // Sort Icon functionality
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_sort) {
//            Collections.sort(bookInfoList, ratingComparator)
            bookInfoList.reverse()
        }

        adapter.notifyDataSetChanged()

        return super.onOptionsItemSelected(item)
    }
}

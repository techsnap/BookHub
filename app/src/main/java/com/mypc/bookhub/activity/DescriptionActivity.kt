package com.mypc.bookhub.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.mypc.bookhub.R
import com.mypc.bookhub.database.BookDatabase
import com.mypc.bookhub.database.BookEntity
import com.mypc.bookhub.model.Book
import com.mypc.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject

class DescriptionActivity : AppCompatActivity() {

    lateinit var txtBookName: TextView
    lateinit var txtBookAuthor: TextView
    lateinit var txtPublishedDate: TextView
    lateinit var txtBookPages: TextView
    lateinit var txtBookPrice: TextView
    lateinit var imgBookImage: ImageView

    lateinit var txtBookDesc: TextView
    lateinit var btnAddToFav: Button

    lateinit var progressLayout: RelativeLayout
    lateinit var progressBar: ProgressBar

    lateinit var toolbar: Toolbar

    var bookId: String? = "100"
    lateinit var book: Book

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        txtBookName = findViewById(R.id.txtBookName)
        txtBookAuthor = findViewById(R.id.txtBookAuthor)
        txtPublishedDate = findViewById(R.id.txtPublishedDate)
        txtBookPages = findViewById(R.id.txtBookPages)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        imgBookImage = findViewById(R.id.imgBookImage)

        txtBookDesc = findViewById(R.id.txtBookDescription)
        btnAddToFav = findViewById(R.id.btnAddToFavourites)

        progressLayout = findViewById(R.id.progressLayout)
        progressBar = findViewById(R.id.progressBar)
        progressLayout.visibility = View.VISIBLE

        // Set toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Book Details"

        // Get book id from Intent
        if (intent != null) {
            bookId = intent.getStringExtra("book_id")
            book = intent.getSerializableExtra("book") as Book
        }else {
            finish()
            Toast.makeText(this@DescriptionActivity, "Error", Toast.LENGTH_SHORT).show()
        }

        if (bookId == "100") {
            finish()
            Toast.makeText(this@DescriptionActivity, "Error", Toast.LENGTH_SHORT).show()
        }

        // If all OK, then POST the request and set the content
        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url = "https://www.googleapis.com/books/v1/volumes/" + bookId

//        // Used for posting data
//        val jsonParams = JSONObject()
//        jsonParams.put("book_id", bookId)

        //Check for connection first
        if (ConnectionManager().checkConnectivity(this)) {
            val jsonObjectRequest = object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
                try {
                    // Hide progress bar
                    progressLayout.visibility = View.GONE

                    val bookInfo = it.getJSONObject("volumeInfo")

                    // Set content
                    val bookImageUrl = bookInfo.getJSONObject("imageLinks").getString("thumbnail")
                    txtBookName.text = book.bookName
                    txtBookAuthor.text = book.bookAuthor
                    txtPublishedDate.text = book.publishedDate
                    txtBookPrice.text = book.bookPrice
                    txtBookDesc.text = bookInfo.getString("description")
                    Picasso.get().load(bookImageUrl).error(R.drawable.default_book_cover).into(imgBookImage)

                    // Init a Book Table
                    val bookEntity = BookEntity(
                        bookId as String,
                        txtBookName.text.toString(),
                        txtBookAuthor.text.toString(),
                        txtBookPrice.text.toString(),
                        txtBookDesc.text.toString(),
                        bookImageUrl
                    )

                    // Check if book is fav
                    val checkFavTask = DBAsyncTask(applicationContext, bookEntity, 1)
                    val isFav = checkFavTask.execute().get()

                    if (isFav) {
                        // Already added
                        btnAddToFav.text = "Remove from Favourites"
                        val color = ContextCompat.getColor(applicationContext, R.color.colorFavourites)
                        btnAddToFav.setBackgroundColor(color)
                    } else {
                        // Not added
                    }

                    // If button clicked
                    btnAddToFav.setOnClickListener {
                        // Check if book is fav
                        val isFav = DBAsyncTask(applicationContext, bookEntity, 1).execute().get()

                        if (!isFav) {
                            // Add to fav
                            val addTask = DBAsyncTask(applicationContext, bookEntity, 2).execute()
                            val result = addTask.get()

                            if (result) {
                                Toast.makeText(this@DescriptionActivity, "Added", Toast.LENGTH_SHORT).show()

                                btnAddToFav.text = "Remove from Favourites"
                                val color = ContextCompat.getColor(applicationContext, R.color.colorFavourites)
                                btnAddToFav.setBackgroundColor(color)
                            } else {
                                // Error
                                Toast.makeText(this@DescriptionActivity, "Error", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // In fav, then remove it
                            val addTask = DBAsyncTask(applicationContext, bookEntity, 3).execute()
                            val result = addTask.get()

                            if (result) {
                                Toast.makeText(this@DescriptionActivity, "Removed", Toast.LENGTH_SHORT).show()
                                btnAddToFav.text = "Add to Favourites"
                                val color = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                                btnAddToFav.setBackgroundColor(color)
                            } else {
                                // Error
                                Toast.makeText(this@DescriptionActivity, "Error", Toast.LENGTH_SHORT).show()
                            }
                        }// End
                    }
                } catch (e: JSONException) {
                    Toast.makeText(this@DescriptionActivity, "JSON Error", Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener {
                Toast.makeText(this@DescriptionActivity, "Volley Error", Toast.LENGTH_SHORT).show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
//                    headers["token"] = "d44c4ab299d905"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)
        } else {
            // Not connected to internet
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Error")
            dialog.setMessage("Not connected to Internet")

            // Open Settings Button
            dialog.setPositiveButton("Open Settings") {text, listener ->
                startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                finish()
            }
            // Exit the app
            dialog.setNegativeButton("Exit") {text, listener ->
                ActivityCompat.finishAffinity(this)
            }
            dialog.create().show()
        }
    }

    // AsyncTask for database handling
    class DBAsyncTask(val context: Context, val bookEntity: BookEntity, val mode: Int): AsyncTask<Void, Void, Boolean>() {

        // Create Database
        val database = Room.databaseBuilder(context, BookDatabase::class.java, "book-db").build()

        override fun doInBackground(vararg params: Void?): Boolean {
            when (mode) {
                1 -> {
                    // Check for book
                    val book: BookEntity? = database.bookDao().getBookById(bookEntity.book_id.toString())
                    return book != null
                }
                2 -> {
                    // Insert the book
                    database.bookDao().insertBook(bookEntity)
                    return true
                }
                3 -> {
                    // Delete the book
                    database.bookDao().deleteBook(bookEntity)
                    return true
                }
            }
            database.close()
            return false
        }
    }
}

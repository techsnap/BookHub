package com.mypc.bookhub.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.mypc.bookhub.R
import com.mypc.bookhub.adapter.FavouritesRecyclerAdapter
import com.mypc.bookhub.database.BookDatabase
import com.mypc.bookhub.database.BookEntity

class FavouritesFragment : Fragment() {

    lateinit var recyclerFavourites: RecyclerView
    lateinit var recyclerAdapter: FavouritesRecyclerAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager

    var bookList = listOf<BookEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        // Init
        recyclerFavourites = view.findViewById(R.id.recyclerFavourites)
        layoutManager = GridLayoutManager(activity, 2)

        bookList = GetFavouritesAsyncTask(activity as Context).execute().get()

        if (activity != null) {
            recyclerAdapter = FavouritesRecyclerAdapter(activity as Context, bookList)
            recyclerFavourites.adapter = recyclerAdapter
            recyclerFavourites.layoutManager = layoutManager
        }

        return view
    }

    // AsyncTask for db handling
    class GetFavouritesAsyncTask(val context: Context): AsyncTask<Void, Void, List<BookEntity>>() {

        override fun doInBackground(vararg params: Void?): List<BookEntity> {
            val db = Room.databaseBuilder(context, BookDatabase::class.java, "book-db").build()

            return db.bookDao().getAllBooks()
        }

    }
}

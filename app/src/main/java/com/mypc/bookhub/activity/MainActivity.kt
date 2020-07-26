package com.mypc.bookhub.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.mypc.bookhub.fragment.DashboardFragment
import com.mypc.bookhub.fragment.FavouritesFragment
import com.mypc.bookhub.fragment.ProfileFragment
import com.mypc.bookhub.R

class MainActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var frameLayout: FrameLayout
    lateinit var toolbar: Toolbar
    lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawerLayout)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        frameLayout = findViewById(R.id.frame)
        toolbar = findViewById(R.id.toolbar)
        navigationView = findViewById(R.id.navigationView)

        setToolbar()

        // Open dashboard frag when app starts
        openDashboard()

        // Adding hamburger icon
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity,
            drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        // Adding functionality to menu items
        navigationView.setNavigationItemSelectedListener {

            /* Check current item and uncheck previous item
            * Redundant code */
            /*
            var previousItem: MenuItem? = null
            if (previousItem != null) {
                previousItem?.isChecked = false
            }
            it.isCheckable = true
            it.isChecked = true
            previousItem = it*/

            when (it.itemId) {
                R.id.dashboard -> {
                    openDashboard()
                }
                R.id.favourites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            FavouritesFragment()
                        )
                        .commit()

                    supportActionBar?.title = "Favourites"
                }
                R.id.profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            ProfileFragment()
                        )
                        .commit()

                    supportActionBar?.title = "Profile"
                }
                R.id.aboutApp -> {
                    Toast.makeText(this, "About App", Toast.LENGTH_SHORT).show()

                    supportActionBar?.title = "About"
                }
            }

            // Close drawer when menu item clicked
            drawerLayout.closeDrawers()
            return@setNavigationItemSelectedListener true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_search, menu)

        val menuItem = menu?.findItem(R.id.action_search)
        val searchView = menuItem?.actionView as SearchView
        searchView.queryHint = "Search for Books"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(this@MainActivity, "${searchView.queryHint}", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        }
        )
        return super.onCreateOptionsMenu(menu)
    }

    fun setToolbar() {
        // Adding title
        setSupportActionBar(toolbar)
        supportActionBar?.title = "BookHub"

        // Adding Home Button
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // Add functionality to hamburger icon
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    fun openDashboard() {
        // Add fragment to frame
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.frame,
                DashboardFragment()
            )
            .commit()

        // Add title for fragment
        supportActionBar?.title = "Dashboard"

        // Necessary for selecting menu item when back button is pressed
        navigationView.setCheckedItem(R.id.dashboard)
    }

    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.frame)

        when (frag) {
            !is DashboardFragment -> openDashboard()

            else -> super.onBackPressed()
        }
    }
}
